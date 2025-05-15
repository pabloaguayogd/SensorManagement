package com.sensor.app;

import com.sensor.app.verticles.RestAPIVerticle;

import com.sensor.app.verticles.ServiciosVerticle;
import io.vertx.core.Vertx;

public class MainApis {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RestAPIVerticle()).result();
        vertx.deployVerticle(new ServiciosVerticle()).result();
    }
}