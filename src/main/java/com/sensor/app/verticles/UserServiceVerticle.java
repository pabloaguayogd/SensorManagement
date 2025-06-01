package com.sensor.app.verticles;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sensor.app.entities.User;
import com.sensor.app.util.CRUDConnection;
import com.sensor.app.util.ControllerErrors;
import com.sensor.app.util.LocalDateTimeAdapter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserServiceVerticle extends AbstractVerticle {


    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private static final Logger logger = LoggerFactory.getLogger(UserServiceVerticle.class);

    private JWTAuth jwtAuth;

    public void start(Promise<Void> startPromise) {
        CRUDConnection.getWebClient(getVertx());

        jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
                .setKeyStore(new io.vertx.ext.auth.KeyStoreOptions()
                        .setType("jceks")
                        .setPath("keystore.jceks")
                        .setPassword("secret")));


        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.route().handler(CorsHandler.create(".*."));
        router.route("/api/user/login").handler(this::handlerLogin);
        router.route("/api/user/register").handler(this::handlerRegisterUser);



        // Middleware de logging
        router.route().handler(ctx -> {
            long start = System.currentTimeMillis();

            ctx.addBodyEndHandler(v -> {
                long duration = System.currentTimeMillis() - start;
                logger.info(String.join(" ", ctx.request().method().toString(), ctx.request().path(), String.valueOf(ctx.response().getStatusCode()), String.valueOf(duration)+"ms" ));
            });

            ctx.next();
        });


        router.post("/api/register").handler(this::handlerRegisterUser);
        router.post("/api/login").handler(this::handlerLogin);





        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8082, http -> {
                    if (http.succeeded()) {
                        System.out.println("Capa de servicios del usuario iniciado en http://localhost:8082");
                        startPromise.complete();
                    } else {
                        System.out.println(ControllerErrors.ERROR_INICIAR_SERVER + http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });


    }

    private void handlerRegisterUser(RoutingContext ctx) {

        JsonObject user = ctx.getBodyAsJson();

        System.out.println(ctx.getBodyAsString());

        String username = user.getString("username");
        String password = user.getString("password");

        if (username == null || password == null) {
            System.out.println(username);
            System.out.println(password);
            ctx.response().setStatusCode(400).end("Datos incompletos");
            return;
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        User hashed_user = new User(0, username, hashedPassword);


        CRUDConnection.getWebClient().post(8080,"localhost", "/api/users/")
                .putHeader("Content-Type", "application/json")
                .sendJson(hashed_user)
                .onSuccess(res -> {
                    JsonObject resJSON = new JsonObject();
                    resJSON.put("msg", "Usuario registrado de forma exitosa");

                    String jwt_token =generarJWT(res.bodyAsJsonObject().getString("id"), username);
                    resJSON.put("token", jwt_token);
                    resJSON.put("user_id", res.bodyAsJsonObject().getInteger("user_id") );


                    ctx.response()
                            .setStatusCode(201)
                            .putHeader("Access-Control-Allow-Origin", "*")
                            .putHeader("Content-Type", "application/json")
                            .end(gson.toJson(resJSON));
                })
                .onFailure(error -> {
                    ctx.response()
                            .setStatusCode(500)
                            .putHeader("Access-Control-Allow-Origin", "*")
                            .putHeader("Content-Type", "application/json")
                            .end("{\"error\": Error al registrar el usuario(\"" + error.getMessage() + ")\"}");
                });
    }

    private void handlerLogin(RoutingContext ctx) {

        JsonObject creds = ctx.getBodyAsJson();
        String username = creds.getString("username");
        String password = creds.getString("password");

        if (username == null || password == null) {
            ctx.response().setStatusCode(400).end("Faltan credenciales");
            return;
        }

        CRUDConnection.getWebClient().get(8080,"localhost", "/api/users")
                .send()
        .onSuccess(res -> {



            res.bodyAsJsonArray().stream().map(obj -> gson.fromJson(obj.toString(), User.class))
                    .filter(user -> user.getNickname().equals(username))
                    .forEach(user -> {
                        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                        if (result.verified) {
                            JsonObject resJSON = new JsonObject();
                            resJSON.put("msg", "Inicio de sesión exitoso");
                            resJSON.put("token", generarJWT(user.getUser_id().toString(), username));
                            resJSON.put("user_id", user.getUser_id() );

                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Access-Control-Allow-Origin", "*")
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(resJSON));
                        } else {
                            ctx.response()
                                    .setStatusCode(400)
                                    .putHeader("Access-Control-Allow-Origin", "*")
                                    .putHeader("Content-Type", "application/json")
                                    .end("{\"error\": \" Error en la contraseña \"}");
                        }
                    });

        })
                .onFailure(error -> {
                    ctx.response()
                            .setStatusCode(500)
                            .putHeader("Access-Control-Allow-Origin", "*")
                            .putHeader("Content-Type", "application/json")
                            .end("{\"error\": Error al iniciar sesión(\"" + error.getMessage() + ")\"}");
                });
    }


    private String generarJWT(String userId, String username) {
        return jwtAuth.generateToken(
                new JsonObject()
                        .put("sub", userId)
                        .put("username", username)
                        .put("iat", Instant.now().getEpochSecond())
                        .put("jti", UUID.randomUUID().toString()),
                new JWTOptions()
                        .setAlgorithm("RS256")
                        .setExpiresInMinutes(60) // 1h de TTL
        );
    }


}
