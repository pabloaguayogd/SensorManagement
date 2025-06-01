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

import static com.sensor.app.servicios.AlarmStateService.getLatestAlarmState;
import static com.sensor.app.servicios.SensorServicio.checkIfSensorExists;

public class SensorValueServicio {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();



    public static void addSensorValue(Integer id_sensor, Float valor) throws RuntimeException, ExecutionException, InterruptedException {

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








}
