package com.sensor.app;

import com.sensor.app.mysql.rest.RestAPIVerticle;

import com.sensor.app.mysql.rest.ServiciosVerticle;
import io.vertx.core.Vertx;

public class MainApis {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RestAPIVerticle());
        vertx.deployVerticle(new ServiciosVerticle());
    }
}