package com.sensor.app.mysql.servicios;

import com.google.gson.*;
import com.mysql.cj.conf.ConnectionUrlParser;
import com.sensor.app.mysql.CRUDConnection;
import com.sensor.app.mysql.entities.*;
import com.sensor.app.util.LocalDateTimeAdapter;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.sqlclient.Tuple;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class SensorValueServicio {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())

            .create();


    private static boolean checkIfSensorExists(Integer id_sensor) throws ExecutionException, InterruptedException {
        int r = CRUDConnection.getWebClient().get("/api/sensors/" + id_sensor)
                .host("127.0.0.1").port(8080)
                .send()
                .toCompletionStage().toCompletableFuture().get().statusCode();

        return r != 404;
    }



    public static void addSensorValue(Integer id_sensor, Float valor) throws RuntimeException, ExecutionException, InterruptedException {
        if(!checkIfSensorExists(id_sensor))
            throw new RuntimeException("El sensor con id "+id_sensor+" no existe.");

        SensorValue newValue = new SensorValue(0,id_sensor,valor,LocalDateTime.now());

        int r = CRUDConnection.getWebClient().post(8080,"127.0.0.1","/api/sensorValues")
                .putHeader("Content-Type", "application/json")
                .sendJson(gson.toJson(newValue))
                .toCompletionStage().toCompletableFuture().get().statusCode();

        if(r != 201)
            throw new RuntimeException("No se ha podido añadir el valor al sensor("+id_sensor+"), devuelve código "+r+".");

    }

    public static List<SensorValue> getLatestSensorValue(Integer id_sensor, Integer num) throws ExecutionException, InterruptedException {
        List<SensorValue> res =  new ArrayList<SensorValue>();

        HttpResponse<Buffer> respCRUD = CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/sensorValues/"+id_sensor)
                .send()
                .toCompletionStage().toCompletableFuture().get();

        if(respCRUD.statusCode() != 200)
            throw new RuntimeException("Error en la llamada CRUD");

        respCRUD.bodyAsJsonArray().stream()
                .map(rawJson ->  gson.fromJson(((JsonObject)rawJson).toString(), SensorValue.class))
                .sorted(Comparator.comparing(SensorValue::getTimestamp).reversed())
                .limit(num)
                .forEach(res::add);

        return res;
    }

    public static List<ActuatorState> getLatestActuatorState(Integer id_actuador, Integer num) throws ExecutionException, InterruptedException {
        List<ActuatorState> res =  new ArrayList<ActuatorState>();



        HttpResponse<Buffer> respCRUD = CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/actuatorStates/"+id_actuador)
                .send()
                .toCompletionStage().toCompletableFuture().get();

        if(respCRUD.statusCode() != 200)
            throw new RuntimeException("Error en la llamada CRUD");

        respCRUD.bodyAsJsonArray().stream()
                .map(rawJson ->  gson.fromJson(((JsonObject)rawJson).toString(), ActuatorState.class))
                .sorted(Comparator.comparing(ActuatorState::getTimestamp).reversed())
                .limit(num)
                .forEach(res::add);

        return res;
    }


    public static Integer getGroupOfDevice(Integer id_device) throws ExecutionException, InterruptedException {
        HttpResponse<Buffer> respCRUD = CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/devices/"+id_device)
                .send()
                .toCompletionStage().toCompletableFuture().get();

        if(respCRUD.statusCode() != 200)
            throw new RuntimeException("Error en la llamada CRUD");

        return gson.fromJson(respCRUD.bodyAsString(),Device.class).getGroupId();
    }

    public static List<Sensor> getAllSensorInGroup(Integer id_group) throws ExecutionException, InterruptedException {
        List<Sensor> res = new ArrayList<Sensor>();

        HttpResponse<Buffer> respCRUD = CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/sensors")
                .send()
                .toCompletionStage().toCompletableFuture().get();

        if(respCRUD.statusCode() != 200)
            throw new RuntimeException("Error en la llamada CRUD");

        respCRUD.bodyAsJsonArray().stream()
                .map(rawJson ->  gson.fromJson(((JsonObject)rawJson).toString(), Sensor.class))
                .map(sensor -> {
                    try {
                        return Tuple.of(sensor, getGroupOfDevice(sensor.getDeviceId()));
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(t -> Objects.equals(t.get(Integer.class, 1), id_group))
                .map(t -> t.get(Sensor.class, 0))
                .forEach(res::add);

        return res;
    }

    public static List<Actuator> getAllActuatorInGroup(Integer id_group) throws ExecutionException, InterruptedException {
        List<Actuator> res = new ArrayList<Actuator>();

        HttpResponse<Buffer> respCRUD = CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/actuators")
                .send()
                .toCompletionStage().toCompletableFuture().get();

        if(respCRUD.statusCode() != 200)
            throw new RuntimeException("Error en la llamada CRUD");

        respCRUD.bodyAsJsonArray().stream()
                .map(rawJson ->  gson.fromJson(((JsonObject)rawJson).toString(), Actuator.class))
                .map(actuator -> {
                    try {
                        return Tuple.of(actuator, getGroupOfDevice(actuator.getDeviceId()));
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(t -> Objects.equals(t.get(Integer.class, 1), id_group))
                .map(t -> t.get(Actuator.class, 0))
                .forEach(res::add);

        return res;
    }



    public static Map<Integer, SensorValue> getLastestValueOfSensorInGroup(Integer id_group) throws ExecutionException, InterruptedException {
            Map<Integer, SensorValue> res = new HashMap<Integer, SensorValue>();

            List<Sensor> sensorsInGroup = getAllSensorInGroup(id_group);

            sensorsInGroup.forEach(sensor -> {
                try {
                    SensorValue val = getLatestSensorValue(sensor.getId(), 1).iterator().next();
                    res.put(sensor.getId(), val);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            return res;
    }

    public static Map<Integer, ActuatorState> getLastestStateOfActuatorInGroup(Integer id_group) throws ExecutionException, InterruptedException {
        Map<Integer, ActuatorState> res = new HashMap<Integer, ActuatorState>();

        List<Actuator> actuatorsInGroup = getAllActuatorInGroup(id_group);

        actuatorsInGroup.forEach(actuator -> {
            try {
                getLatestActuatorState(actuator.getId(), 1).iterator()
                        .forEachRemaining(state -> res.put(actuator.getId(), state) );

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return res;
    }



}
