package com.sensor.app.mysql.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.sensor.app.mysql.entities.ActuatorState;
import com.sensor.app.mysql.entities.SensorValue;
import com.sensor.app.mysql.servicios.SensorValueServicio;
import com.sensor.app.util.LocalDateTimeAdapter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ServiciosVerticle extends AbstractVerticle {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())

            .create();


    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/api/business/sensorData").handler(this::handleAddSensorData);
        router.get("/api/business/sensorValues/:id_sensor/latest").handler(this::handleLatestSensorValues);
        router.get("/api/business/actuatorStates/:id_actuador/latest").handler(this::handleLatestActuatorStates);
        router.get("/api/business/group/:id_grupo/sensorValues/latest").handler(this::handleGroupLatestSensorValue);
        router.get("/api/business/group/:id_grupo/actuatorStates/latest").handler(this::handleGroupLatestActuatorState);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8081, http -> {
                    if (http.succeeded()) {
                        System.out.println("Capa de lógica iniciado en http://localhost:8081");
                        startPromise.complete();
                    } else {
                        System.out.println("Error al iniciar servidor HTTP: " + http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });

    }


    private void handleAddSensorData(RoutingContext routingContext){

        Integer id_sensor = routingContext.body().asJsonObject().getInteger("id_sensor");
        Float valor = routingContext.body().asJsonObject().getFloat("valor");

        try{
            SensorValueServicio.addSensorValue(id_sensor, valor);

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
        Integer id_sensor = Integer.parseInt(routingContext.pathParam("id_sensor"));

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

    private void handleLatestActuatorStates(RoutingContext routingContext){
        Integer id_actuador = Integer.parseInt(routingContext.pathParam("id_actuador"));

        try{
            List<ActuatorState> res =  SensorValueServicio.getLatestActuatorState(id_actuador,10);



            String rawDataJson = gson.toJson(res);

            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"msg\": \"Últimos 10 valores del actuador "+id_actuador+".\", \"data\": "+rawDataJson+"}");

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

    private void handleGroupLatestActuatorState(RoutingContext routingContext){
        Integer id_group = Integer.parseInt(routingContext.pathParam("id_grupo"));

        try{
            Map<Integer, ActuatorState> sens_val =  SensorValueServicio.getLastestStateOfActuatorInGroup(id_group);

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
