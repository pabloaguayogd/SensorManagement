package com.sensor.app.verticles;

import com.sensor.app.util.ControllerErrors;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class UserUIVerticle extends AbstractVerticle {

    private JWTAuth jwtAuth;

    public void start(Promise<Void> startPromise) {

        jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
                .setKeyStore(new io.vertx.ext.auth.KeyStoreOptions()
                        .setType("jceks")
                        .setPath("keystore.jceks")
                        .setPassword("secret")));


        Router router = Router.router(vertx);
        router.route("/main").handler(StaticHandler.create("static/main.html"));
        router.route("/login").handler(StaticHandler.create("static/login.html"));
        router.route("/register").handler(StaticHandler.create("static/register.html"));
        router.route("/panel").handler(this::authenticate).handler(StaticHandler.create("static/panel.html"));

        router.route().handler(StaticHandler.create()
                .setCachingEnabled(false)
                .setWebRoot("static"));


        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8083, http -> {
                    if (http.succeeded()) {
                        System.out.println("UI del usuario iniciada en http://localhost:8083");
                        startPromise.complete();
                    } else {
                        System.out.println(ControllerErrors.ERROR_INICIAR_SERVER + http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void authenticate(RoutingContext ctx) {
        // Obtener cookie
        Cookie cookie = ctx.request().getCookie("jwt");

        if (cookie == null) {
            ctx.response()
                    .setStatusCode(401)
                    .end("Acceso denegado: No se encontró token JWT en cookies");
            return;
        }

        String token = cookie.getValue();

        // Validar token JWT
        jwtAuth.authenticate(new JsonObject().put("token", token))
                .onSuccess(user -> {
                    // Guardar usuario en contexto para handlers posteriores
                    ctx.setUser(user);
                    ctx.next();
                })
                .onFailure(err -> {
                    ctx.response()
                            .setStatusCode(401)
                            .end("Acceso denegado: Token inválido o expirado");
                });
    }

}
