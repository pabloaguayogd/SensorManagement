package com.sensor.app.mysql;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

public class CRUDConnection {

    private static WebClient client;

    public static void createWebClient(){
        client = WebClient.create(Vertx.vertx());
    }

    public static WebClient getWebClient(){
        if(client == null){
            createWebClient();
        }


        return client;
    }


}
