package com.sensor.app;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;

public class TestDBConnection {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(new VertxOptions());

        // Configuración de conexión MySQL (ajusta si tienes otro puerto o contraseña)
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
            .setPort(3307)
            .setHost("localhost")
            .setDatabase("sensor_management")
            .setUser("root")
            .setPassword("RB22raba.");

        // Pool de conexiones
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        // Crear pool
        MySQLPool client = MySQLPool.pool(vertx, connectOptions, poolOptions);

        // Intentar conectar
        client.getConnection(ar -> {
            if (ar.succeeded()) {
                System.out.println("✅ ¡Conexión exitosa a la base de datos MySQL!");
                SqlConnection conn = ar.result();
                conn.close(); // cerramos la conexión
                vertx.close();
            } else {
                System.out.println("❌ Error al conectar: " + ar.cause().getMessage());
                vertx.close();
            }
        });
    }
}
