package com.sensor.app.verticles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sensor.app.entities.*;
import com.sensor.app.servicios.*;
import com.sensor.app.util.CRUDConnection;
import com.sensor.app.util.ControllerErrors;
import com.sensor.app.util.LocalDateTimeAdapter;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClientOptions;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DevicesServiciosVerticle extends AbstractVerticle {
	
	MqttClient mqttClient;

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private static final Logger logger = LoggerFactory.getLogger(DevicesServiciosVerticle.class);
    public void start(Promise<Void> startPromise) {
        CRUDConnection.getWebClient(getVertx());

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Middleware de logging
        router.route().handler(ctx -> {
            long start = System.currentTimeMillis();

            ctx.addBodyEndHandler(v -> {
                long duration = System.currentTimeMillis() - start;
                logger.info(String.join(" ", ctx.request().method().toString(), ctx.request().path(), String.valueOf(ctx.response().getStatusCode()), String.valueOf(duration)+"ms" ));
            });

            ctx.next();
        });

        router.route().handler(CorsHandler.create(".*."));
        router.post("/api/control/sensorData").handler(this::handleAddSensorData);
        router.post("/api/control/setAlarms").handler(this::handleSetAlarms);
        router.get("/api/control/sensorValues/:sensor_id/latest").handler(this::handleLatestSensorValues);
        router.get("/api/control/alarmState/:alarm_id/latest").handler(this::handleLatestAlarmStates);
        router.get("/api/control/group/:group_id/sensorValues/latest").handler(this::handleGroupLatestSensorValue);
        router.get("/api/control/group/:group_id/alarmStates/latest").handler(this::handleGroupLatestAlarmState);

        this.mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true).setAckTimeout(5));
        this.mqttClient.connect(1883, "localhost");

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8081, http -> {
                    if (http.succeeded()) {
                        System.out.println("Capa de servicios de dispositivos iniciado en http://localhost:8081");
                        startPromise.complete();
                    } else {
                        System.out.println(ControllerErrors.ERROR_INICIAR_SERVER + http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });

    }

    private void handleSetAlarms(RoutingContext routingContext) {
        Integer group_id = routingContext.body().asJsonObject().getInteger("group_id");
        String state = routingContext.body().asJsonObject().getString("alarm_state");

        CRUDConnection.getWebClient().get(8080,"localhost", "/api/groups/"+group_id+"/alarms")
                .send()
                .onSuccess(res -> {

                    CRUDConnection.getWebClient().get(8080,"localhost", "/api/groups/"+group_id)
                            .send()
                            .onSuccess(res2 -> {

                                if(res.statusCode() == 404){
                                    routingContext.response()
                                            .setStatusCode(404)
                                            .putHeader("Content-Type", "application/json")
                                            .end("{\"msg\": \"No existen alarmas en el grupo.\"}");
                                }

                                Alarm[] alarmArray = gson.fromJson(res.bodyAsString(), Alarm[].class);
                                List<Alarm> alarms = Arrays.asList(alarmArray);

                                Group group = gson.fromJson(res2.bodyAsString(), Group.class);


                                alarms.forEach(alarm -> {
                                    System.out.println("Enviando mensaje a MQTT: "+group.getMqttChannel() + "/alarms/" + alarm.getAlarm_id());

                                    AlarmState alarmState = new AlarmState();
                                    alarmState.setActuatorId(alarm.getAlarm_id());
                                    alarmState.setState(state.equals("ON"));
                                    alarmState.setTimestamp(LocalDateTime.now());

                                    CRUDConnection.getWebClient().post(8080, "localhost", "/api/alarmStates").sendBuffer(Buffer.buffer(gson.toJson(alarmState)), res3 -> {});

                                    mqttClient.publish(
                                            group.getMqttChannel() + "/alarms/" + alarm.getAlarm_id(),
                                            Buffer.buffer(state),
                                            MqttQoS.AT_LEAST_ONCE,
                                            false,
                                            false
                                    );
                                });

                                routingContext.response()
                                        .setStatusCode(200)
                                        .putHeader("Content-Type", "application/json")
                                        .end("{\"msg\": \"Alarma activada correctamente.\"}");

                            }).onFailure(error -> {
                                routingContext.response()
                                        .setStatusCode(500)
                                        .putHeader("Content-Type", "application/json")
                                        .end("{\"error\": \"" + error.getMessage() + "\"}");
                            });

                })
                .onFailure(error -> {
                    routingContext.response()
                            .setStatusCode(500)
                            .putHeader("Content-Type", "application/json")
                            .end("{\"error\": \"" + error.getMessage() + "\"}");
                });


}


    private void handleAddSensorData(RoutingContext routingContext){

        Integer sensor_id = routingContext.body().asJsonObject().getInteger("sensor_id");
        Float valor = routingContext.body().asJsonObject().getFloat("valor");

        System.out.println(sensor_id);

        CRUDConnection.getWebClient().get(8080,"localhost", "/api/sensors/"+sensor_id)
                .send()
                .onSuccess(res -> {
                    System.out.println(res.bodyAsString());
                    Sensor sensor = gson.fromJson(res.bodyAsString(), Sensor.class);

                    CRUDConnection.getWebClient().get(8080,"localhost", "/api/devices/"+sensor.getDeviceId())
                            .send()
                            .onSuccess(res2 -> {
                                Device sensor_device =  gson.fromJson(res.bodyAsString(), Device.class);

                                //Ver los valores y calcular el umbral del sensor
                                // (No tiene mucho sentido en nuestro caso de uso, ya que tenemos muchos dispositivos con distintos umbrales, estos no son muy predecibles,
                                // cada sensor deberia disparar la alarma desde su firmware, ir a handleSetAlarms)

                                try{

                                    SensorValueServicio.addSensorValue(sensor_id, valor);

                                    routingContext.response()
                                            .setStatusCode(201)
                                            .putHeader("Content-Type", "application/json")
                                            .end("{\"msg\": \"Valor añadido correctamente al sensor "+sensor_id+".\"}");

                                }catch (RuntimeException error){
                                    routingContext.response()
                                            .setStatusCode(404)
                                            .putHeader("Content-Type", "application/json")
                                            .end("{\"error\": \""+error.getMessage()+"\"}");
                                } catch (ExecutionException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }



                            })
                            .onFailure(error -> {
                                routingContext.response()
                                        .setStatusCode(500)
                                        .putHeader("Content-Type", "application/json")
                                        .end("{\"error\": \"" + error.getMessage() + "\"}");
                            });

                })
                .onFailure(error -> {
                    routingContext.response()
                            .setStatusCode(500)
                            .putHeader("Content-Type", "application/json")
                            .end("{\"error\": \"" + error.getMessage() + "\"}");
                });







    }

    private void handleLatestSensorValues(RoutingContext routingContext){
        Integer id_sensor = Integer.parseInt(routingContext.pathParam("sensor_id"));

        try{
            List<SensorValue> res =  SensorValueServicio.getLatestSensorValue(id_sensor,10);

            String rawDataJson = gson.toJson(res);

            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"msg\": \"Últimos 10 valores del sensor "+id_sensor+".\", \"data\": "+rawDataJson+"}");

        }catch (RuntimeException error){
            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"error\": \""+error.getMessage()+"\"}");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLatestAlarmStates(RoutingContext routingContext){
        Integer id_actuador = Integer.parseInt(routingContext.pathParam("id_actuador"));

        try{
            List<AlarmState> res =  AlarmStateService.getLatestAlarmState(id_actuador,10);



            String rawDataJson = gson.toJson(res);

            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"msg\": \"Últimos 10 activaciones de la alarma "+id_actuador+".\", \"data\": "+rawDataJson+"}");

        }catch (RuntimeException error){
            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"error\": \""+error.getMessage()+"\"}");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleGroupLatestSensorValue(RoutingContext routingContext){
        Integer id_group = Integer.parseInt(routingContext.pathParam("group_id"));

        CRUDConnection.getWebClient().get(8080,"localhost", "/api/sensorValues/group/"+id_group).send()
                .onSuccess(res -> {

                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("Content-Type", "application/json")
                            .end("{\"msg\": \"Ultimos valores de los sensores del grupo "+id_group+".\", \"data\": "+res.bodyAsString()+"}");
                })
                .onFailure(error -> {
                    routingContext.response()
                            .setStatusCode(500)
                            .putHeader("Content-Type", "application/json")
                            .end("{\"error\": \""+error.getMessage()+"\"}");
                });

    }

    private void handleGroupLatestAlarmState(RoutingContext routingContext){
        Integer id_group = Integer.parseInt(routingContext.pathParam("group_id"));

        CRUDConnection.getWebClient().get(8080,"localhost", "/api/alarmStates/group/"+id_group).send()
                .onSuccess(res -> {

                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("Content-Type", "application/json")
                            .end("{\"msg\": \"Ultimos valores de las alarmas del grupo "+id_group+".\", \"data\": "+res.bodyAsString()+"}");
                })
                .onFailure(error -> {
                    routingContext.response()
                            .setStatusCode(500)
                            .putHeader("Content-Type", "application/json")
                            .end("{\"error\": \""+error.getMessage()+"\"}");
                });



    }



}