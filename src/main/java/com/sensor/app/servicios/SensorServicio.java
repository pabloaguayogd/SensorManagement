package com.sensor.app.servicios;

import com.sensor.app.util.CRUDConnection;

import java.util.concurrent.ExecutionException;

public class SensorServicio {

    public static boolean checkIfSensorExists(Integer id_sensor) throws ExecutionException, InterruptedException {
        int r = CRUDConnection.getWebClient().get("/api/sensors/" + id_sensor)
                .host("127.0.0.1").port(8080)
                .send().result().body().getInt(10);

        return r != 404;
    }
}
