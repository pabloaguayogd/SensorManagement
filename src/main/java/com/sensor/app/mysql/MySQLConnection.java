package com.sensor.app.mysql;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class MySQLConnection {
    private static MySQLPool client;

    public static void createConnection(){
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("sensor_management")
                .setUser("root")
                .setPassword("root"); // Cambia si tu contraseña es diferente

        PoolOptions poolOptions = new PoolOptions().setMaxSize(10);
        client = MySQLPool.pool(Vertx.currentContext().owner(), connectOptions, poolOptions);
    }


    public static MySQLPool getClient(){
        if(client == null)
            createConnection();

        return client;
    }



}
