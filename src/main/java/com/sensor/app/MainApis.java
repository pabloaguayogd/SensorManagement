package com.sensor.app;

import com.sensor.app.verticles.RestAPIVerticle;

import com.sensor.app.verticles.DevicesServiciosVerticle;
import com.sensor.app.verticles.UserServiceVerticle;
import com.sensor.app.verticles.UserUIVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class MainApis {
    public static void main(String[] args) {

        DeploymentOptions workerOpts = new DeploymentOptions()
                .setWorker(true)
                .setInstances(1)
                .setWorkerPoolSize(1);

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RestAPIVerticle()).onFailure(System.out::println);
        vertx.deployVerticle(new DevicesServiciosVerticle(), workerOpts).onFailure(System.out::println);
        vertx.deployVerticle(new UserServiceVerticle()).onFailure(System.out::println);
        vertx.deployVerticle(new UserUIVerticle()).onFailure(System.out::println);

    }
}