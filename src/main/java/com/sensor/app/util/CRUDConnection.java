package com.sensor.app.util;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

public class CRUDConnection {

    private static WebClient client;

    public static void createWebClient(Vertx vertx){
        client = WebClient.create(vertx);
    }

    public static WebClient getWebClient(Vertx vertx){
        if(client == null)
            createWebClient(vertx);



        return client;
    }

    public static WebClient getWebClient(){
        return client;
    }


}
