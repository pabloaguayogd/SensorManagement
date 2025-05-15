package com.sensor.app.verticles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sensor.app.entities.AlarmState;
import com.sensor.app.entities.SensorValue;
import com.sensor.app.servicios.SensorValueServicio;
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
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.core.buffer.Buffer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ServiciosVerticle extends AbstractVerticle {
	
	MqttClient mqttClient;
	
    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())

            .create();

    private static final Logger logger = LoggerFactory.getLogger(ServiciosVerticle.class);
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

        router.post("/api/business/sensorData").handler(this::handleAddSensorData);
        router.get("/api/business/sensorValues/:sensor_id/latest").handler(this::handleLatestSensorValues);
        router.get("/api/business/alarmState/:alarm_id/latest").handler(this::handleLatestAlarmStates);
        router.get("/api/business/group/:group_id/sensorValues/latest").handler(this::handleGroupLatestSensorValue);
        router.get("/api/business/group/:group_id/alarmStates/latest").handler(this::handleGroupLatestAlarmState);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8081, http -> {
                    if (http.succeeded()) {
                        System.out.println("Capa de servicios iniciado en http://localhost:8081");
                        startPromise.complete();
                    } else {
                        System.out.println(ControllerErrors.ERROR_INICIAR_SERVER + http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });
        
        /* ---------------------------------------------MQTT--------------------------------------------------------

        MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
        mqttClient.connect(1883, "10.100.134.199");*/
    }


    private void handleAddSensorData(RoutingContext routingContext){

        Integer id_sensor = routingContext.body().asJsonObject().getInteger("sensor_id");
        Float valor = routingContext.body().asJsonObject().getFloat("valor");

        try{
        	// TODO Necesitamos la id del grupo para poder publicar con MQTT
        	// Comprobar si el valor pasa el umbral (ON) si no lo pasa (OFF)
        	// Los valores se reciben desde la API no necesitamos subscribirnos a nada ya que usaremos MQTT sólo para publicar.
            SensorValueServicio.addSensorValue(id_sensor, valor);
            // Leer el grupo del dispositivo
            int groupID = SensorValueServicio.getGroupOfDevice(id_sensor);
            String strGroupID= String.valueOf(groupID);
            
            // Umbral es el límite establecido para que al sobrepasar se encienda el actuador
            float umbral = 100;
            List<SensorValue> valuesList = SensorValueServicio.getLatestSensorValue(id_sensor, 10);
            for(int i = 0; i < valuesList.size(); i++) {
            	if (valuesList.get(i).getValue() > umbral) {
            		// TODO solucionar error de clase
            		mqttClient.publish(strGroupID, Buffer.buffer("ON"), MqttQoS.AT_LEAST_ONCE, false, false);
            		}
            	else {
            		mqttClient.publish(strGroupID, Buffer.buffer("OFF"), MqttQoS.AT_LEAST_ONCE, false, false);
            	}
            }
            
            /*if (valor > umbral)
             *  mqttClient.publish("GRUPO1", Buffer.buffer("ON"), MqttQoS.AT_LEAST_ONCE, false, false);
             *  else
             *  mqttClient.publish("GRUPO1", Buffer.buffer("OFF"), MqttQoS.AT_LEAST_ONCE, false, false);
             */
            
            
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"msg\": \"Valor añadido correctamente al sensor "+id_sensor+".\"}");

        }catch (RuntimeException error){
            routingContext.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"error\": \""+error.getMessage()+"\"}");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }


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
            List<AlarmState> res =  SensorValueServicio.getLatestActuatorState(id_actuador,10);



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
        Integer id_group = Integer.parseInt(routingContext.pathParam("id_grupo"));

        try{
            Map<Integer, SensorValue> sens_val =  SensorValueServicio.getLastestValueOfSensorInGroup(id_group);

            String rawDataJson = gson.toJson(sens_val);

            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"msg\": \"Ultimos valores de los sensores del grupo "+id_group+".\", \"data\": "+rawDataJson+"}");

        } catch (ExecutionException | InterruptedException e) {
            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"error\": \""+e.getMessage()+"\"}");
        }


    }

    private void handleGroupLatestAlarmState(RoutingContext routingContext){
        Integer id_group = Integer.parseInt(routingContext.pathParam("group_id"));

        try{
            Map<Integer, AlarmState> sens_val =  SensorValueServicio.getLastestStateOfActuatorInGroup(id_group, getVertx());

            String rawDataJson = gson.toJson(sens_val);

            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"msg\": \"Ultimos estados de los actuadores del grupo "+id_group+".\", \"data\": "+rawDataJson+"}");

        } catch (ExecutionException | InterruptedException e) {
            routingContext.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"error\": \""+e.getMessage()+"\"}");
        }


    }



}
