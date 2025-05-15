package com.sensor.app.servicios;

import com.google.gson.*;
import com.sensor.app.util.CRUDConnection;
import com.sensor.app.entities.*;
import com.sensor.app.util.LocalDateTimeAdapter;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class SensorValueServicio {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())

            .create();


    private static boolean checkIfSensorExists(Integer id_sensor) throws ExecutionException, InterruptedException {
        int r = CRUDConnection.getWebClient().get("/api/sensors/" + id_sensor)
                .host("127.0.0.1").port(8080)
                .send().result().body().getInt(10);

        return r != 404;
    }



    public static void addSensorValue(Integer id_sensor, Float valor) throws RuntimeException, ExecutionException, InterruptedException {
        if(!checkIfSensorExists(id_sensor))
            throw new RuntimeException("El sensor con id "+id_sensor+" no existe.");

        SensorValue newValue = new SensorValue(0,id_sensor,valor,LocalDateTime.now());

        CRUDConnection.getWebClient().post(8080,"127.0.0.1","/api/sensorValues")
                .putHeader("Content-Type", "application/json")
                .sendJson(gson.toJson(newValue))
                .onSuccess(respCRUD -> {
                    if(respCRUD.statusCode() != 201)
                        throw new RuntimeException("No se ha podido añadir el valor al sensor("+id_sensor+"), devuelve código "+respCRUD.statusCode()+".");
                })
                .onFailure(err -> {
                    System.err.println("Error en la solicitud: " + err.getMessage());
                });



    }

    public static List<SensorValue> getLatestSensorValue(Integer id_sensor, Integer num) throws ExecutionException, InterruptedException {
        List<SensorValue> res =  new ArrayList<SensorValue>();

        CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/sensorValues/"+id_sensor)
                .send()
                .onSuccess(respCRUD -> {

                    if(respCRUD.statusCode() != 200)
                        throw new RuntimeException("Error en la llamada CRUD");

                    respCRUD.bodyAsJsonArray().stream()
                            .map(rawJson ->  gson.fromJson(((JsonObject)rawJson).toString(), SensorValue.class))
                            .sorted(Comparator.comparing(SensorValue::getTimestamp).reversed())
                            .limit(num)
                            .forEach(res::add);

                })
                .onFailure(err -> {
                    System.err.println("Error en la solicitud: " + err.getMessage());
                });



        return res;
    }

    public static List<AlarmState> getLatestActuatorState(Integer id_actuador, Integer num) throws ExecutionException, InterruptedException {
        List<AlarmState> res =  new ArrayList<AlarmState>();



        CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/actuatorStates/"+id_actuador)
                .send()
                .onSuccess(respCRUD -> {

                    if(respCRUD.statusCode() != 200)
                        throw new RuntimeException("Error en la llamada CRUD");

                    respCRUD.bodyAsJsonArray().stream()
                            .map(rawJson ->  gson.fromJson(((JsonObject)rawJson).toString(), AlarmState.class))
                            .sorted(Comparator.comparing(AlarmState::getTimestamp).reversed())
                            .limit(num)
                            .forEach(res::add);

                })
                .onFailure(err -> {
                    System.err.println("Error en la solicitud: " + err.getMessage());
                });



        return res;
    }


    public static Integer getGroupOfDevice(Integer id_device) throws ExecutionException, InterruptedException {
        AtomicReference<Integer> res = new AtomicReference<>(0);

        CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/devices/"+id_device)
                .send()
                .onSuccess(respCRUD -> {
                    if(respCRUD.statusCode() != 200)
                        throw new RuntimeException("Error en la llamada CRUD");

                    res.set(gson.fromJson(respCRUD.bodyAsString(), Device.class).getGroupId());
                })
                .onFailure(err -> {
                    System.err.println("Error en la solicitud: " + err.getMessage());
                });;



        return res.get();
    }

    public static List<Sensor> getAllSensorInGroup(Integer id_group) throws ExecutionException, InterruptedException {
        List<Sensor> res = new ArrayList<Sensor>();

        CRUDConnection.getWebClient().get(8080,"127.0.0.1","/api/sensors")
                .send()
                .onSuccess(respCRUD -> {

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

                })
                .onFailure(err -> {
                    System.err.println("Error en la solicitud: " + err.getMessage());
                });



        return res;
    }

    public static List<Alarm> getAllActuatorInGroup(Integer id_group, /*TO DESTROY*/Vertx vertx) throws ExecutionException, InterruptedException {
        List<Alarm> res = new ArrayList<Alarm>();

        WebClient client = WebClient.create(vertx);
        client.get(8080, "localhost", "/api/alarms")
                .send()
                .onSuccess(respCRUD -> {

                    if(respCRUD.statusCode() != 200)
                        throw new RuntimeException("Error en la llamada CRUD");

                    respCRUD.bodyAsJsonArray().stream()
                            .map(rawJson ->  gson.fromJson(((JsonObject)rawJson).toString(), Alarm.class))
                            .map(alarm -> {
                                try {
                                    return Tuple.of(alarm, getGroupOfDevice(alarm.getDeviceId()));
                                } catch (ExecutionException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }).filter(t -> Objects.equals(t.get(Integer.class, 1), id_group))
                            .map(t -> t.get(Alarm.class, 0))
                            .forEach(res::add);

                })
                .onFailure(err -> {
                    System.err.println("Error en la solicitud: " + err.getMessage());
                });


        return res;
    }



    public static Map<Integer, SensorValue> getLastestValueOfSensorInGroup(Integer id_group) throws ExecutionException, InterruptedException {
            Map<Integer, SensorValue> res = new HashMap<Integer, SensorValue>();

            List<Sensor> sensorsInGroup = getAllSensorInGroup(id_group);

            sensorsInGroup.forEach(sensor -> {
                try {
                    SensorValue val = getLatestSensorValue(sensor.getSensor_id(), 1).iterator().next();
                    res.put(sensor.getSensor_id(), val);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            return res;
    }

    public static Map<Integer, AlarmState> getLastestStateOfActuatorInGroup(Integer id_group, /*TO DESTROY*/Vertx vertx) throws ExecutionException, InterruptedException {
        Map<Integer, AlarmState> res = new HashMap<Integer, AlarmState>();

        List<Alarm> actuatorsInGroup = getAllActuatorInGroup(id_group, vertx);

        actuatorsInGroup.forEach(alarm -> {
            try {
                getLatestActuatorState(alarm.getAlarm_id(), 1).iterator()
                        .forEachRemaining(state -> res.put(alarm.getAlarm_id(), state) );

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return res;
    }



}
