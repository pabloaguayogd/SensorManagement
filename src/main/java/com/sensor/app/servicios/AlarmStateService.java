package com.sensor.app.servicios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sensor.app.entities.AlarmState;
import com.sensor.app.util.CRUDConnection;
import com.sensor.app.util.LocalDateTimeAdapter;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AlarmStateService {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();


    public static List<AlarmState> getLatestAlarmState(Integer id_actuador, Integer num) throws ExecutionException, InterruptedException {
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

}
