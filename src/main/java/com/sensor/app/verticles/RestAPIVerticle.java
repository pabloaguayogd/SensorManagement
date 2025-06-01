package com.sensor.app.verticles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import com.sensor.app.entities.*;
import com.sensor.app.util.ControllerErrors;
import com.sensor.app.util.MySQLConnection;

import com.sensor.app.util.LocalDateTimeAdapter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class RestAPIVerticle extends AbstractVerticle {

    private MySQLPool client;
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private static final Logger logger = LoggerFactory.getLogger(RestAPIVerticle.class);

    public void start(Promise<Void> startPromise) {
        // Configuración de conexión a la base de datos
        client = MySQLConnection.getClient(getVertx());

        // Configuración del router HTTP
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Middleware de logging
        router.route().handler(ctx -> {
            long start = System.currentTimeMillis();

            ctx.addBodyEndHandler(v -> {
                long duration = System.currentTimeMillis() - start;
                logger.info(String.join(" ", ctx.request().method().toString(), ctx.request().path(), String.valueOf(ctx.response().getStatusCode()), String.valueOf(duration)+"ms" ));
            });

            ctx.next();
        });

        router.route().handler(CorsHandler.create(".*."));
        // ==== Endpoints sensors ====
        router.post("/api/sensors").handler(this::handleCreateSensor);
        router.get("/api/sensors/:sensor_id").handler(this::handleGetSensorById);
        router.get("/api/sensors").handler(this::handleGetAllSensors);
        router.put("/api/sensors/:sensor_id").handler(this::handleUpdateSensor);
        router.delete("/api/sensors/:sensor_id").handler(this::handleDeleteSensor);
        // ==== Endpoints actuators ====
        router.post("/api/alarms").handler(this::handleCreateAlarms);
        router.get("/api/alarms/:alarm_id").handler(this::handleGetAlarmsById);
        router.get("/api/alarms").handler(this::handleGetAllAlarms);
        router.put("/api/alarms/:alarm_id").handler(this::handleUpdateAlarms);
        router.delete("/api/alarms/:alarm_id").handler(this::handleDeleteAlarm);
        // ====Endpoints devices ====
        router.post("/api/devices").handler(this::handleCreateDevice);
        router.get("/api/devices/:device_id").handler(this::handleGetDeviceById);
        router.get("/api/devices").handler(this::handleGetAllDevices);
        router.put("/api/devices/:device_id").handler(this::handleUpdateDevice);
        router.delete("/api/devices/:device_id").handler(this::handleDeleteDevice);
        // ====Endpoints groups ====
        router.post("/api/groups").handler(this::handleCreateGroup);
        router.get("/api/groups/:group_id").handler(this::handleGetGroupById);
        router.get("/api/groups").handler(this::handleGetAllGroups);
        router.get("/api/groups/:group_id/sensors").handler(this::handleGetSensorsByGroupId);
        router.get("/api/groups/:group_id/alarms").handler(this::handleGetAlarmsByGroupId);
        router.put("/api/groups/:group_id").handler(this::handleUpdateGroup);
        router.delete("/api/groups/:group_id").handler(this::handleDeleteGroup);
        // ====Endpoints sensorValues ====
        router.post("/api/sensorValues").handler(this::handleCreateSensorValue);
        router.get("/api/sensorValues/:sensor_id").handler(this::handleGetSensorValuesBySensorId);
        router.get("/api/sensorValues/group/:group_id").handler(this::handleGetSensorValuesByGroupId);
        // ====Endpoints actuatorStates ====
        router.post("/api/alarmStates").handler(this::handleCreateAlarmState);
        router.get("/api/alarmStates/:alarm_id").handler(this::handleGetAlarmStatesByAlarmId);
        router.get("/api/alarmStates/group/:group_id").handler(this::handleGetAlarmStatesByGroupId);
        // ==== Endpoints users ====
        router.post("/api/users").handler(this::handleCreateUser);
        router.get("/api/users/:user_id").handler(this::handleGetUserById);
        router.get("/api/users").handler(this::handleGetAllUsers);
        router.get("/api/users/groups/:user_id").handler(this::handleGetAllGroupsFromUser);
        router.put("/api/users/:user_id").handler(this::handleUpdateUser);
        router.delete("/api/users/:user_id").handler(this::handleDeleteUser);
        // ==== Endpoints homes ====
        router.post("/api/homes").handler(this::handleCreateHome);
        router.get("/api/homes/:home_id").handler(this::handleGetHomeById);
        router.put("/api/homes/:home_id").handler(this::handleUpdateHome);
        router.delete("/api/homes/:home_id").handler(this::handleDeleteHome);




        // Iniciar servidor HTTP
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, http -> {
                if (http.succeeded()) {
                    System.out.println("Servidor REST iniciado en http://localhost:8080");
                    startPromise.complete();
                } else {
                    System.out.println(ControllerErrors.ERROR_INICIAR_SERVER + http.cause().getMessage());
                    startPromise.fail(http.cause());
                }
            });
    }

    private void handleGetSensorValuesByGroupId(RoutingContext ctx) {

        String idParam = ctx.pathParam("group_id");

        try {
            int id = Integer.parseInt(idParam);

            client.preparedQuery(SensorValue.GET_SENSOR_VALUE_IN_GROUP)
                    .execute(Tuple.of(id))
                    .onSuccess(rows -> {
                        List<JsonObject> sensorValues = new ArrayList<>();
                        if (rows != null && rows.iterator().hasNext()) {

                            for(Row row : rows){
                                sensorValues.add(gson.fromJson(row.toJson().toString(), JsonObject.class));
                            }

                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(sensorValues));

                        } else {
                            ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }

    }

    private void handleGetAlarmStatesByGroupId(RoutingContext ctx) {

        String idParam = ctx.pathParam("group_id");

        try {
            int id = Integer.parseInt(idParam);

            client.preparedQuery(AlarmState.GET_ALARM_STATE_IN_GROUP)
                    .execute(Tuple.of(id))
                    .onSuccess(rows -> {
                        List<JsonObject> alarmStates = new ArrayList<>();
                        if (rows != null && rows.iterator().hasNext()) {

                            for(Row row : rows){
                                alarmStates.add(gson.fromJson(row.toJson().toString(), JsonObject.class));
                            }

                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(alarmStates));

                        } else {
                            ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }

    }

    private void handleGetSensorsByGroupId(RoutingContext ctx) {

        String idParam = ctx.pathParam("group_id");

        try {
            int id = Integer.parseInt(idParam);

            client.preparedQuery(Sensor.GET_BY_GROUP)
                    .execute(Tuple.of(id))
                    .onSuccess(rows -> {
                        List<Sensor> sensors = new ArrayList<>();
                        if (rows != null && rows.iterator().hasNext()) {

                            for(Row row : rows){
                                sensors.add(new Sensor(row));
                            }

                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(sensors));

                        } else {
                            ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }

    }

    private void handleGetAllGroupsFromUser(RoutingContext ctx) {

        String idParam = ctx.pathParam("user_id");

        try {
            int id = Integer.parseInt(idParam);

            client.preparedQuery(Group.GET_BY_USER)
                    .execute(Tuple.of(id))
                    .onSuccess(rows -> {
                        List<Group> groups = new ArrayList<>();
                        if (rows != null && rows.iterator().hasNext()) {

                            for(Row row : rows){
                                groups.add(new Group(row));
                            }

                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(groups));

                        } else {
                            ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }

    }

    //Para Capa de Servicios
    private void handleGetAlarmsByGroupId(RoutingContext ctx) {

        String idParam = ctx.pathParam("group_id");

        try {
            int id = Integer.parseInt(idParam);

            client.preparedQuery(Alarm.GET_BY_GROUP)
                    .execute(Tuple.of(id))
                    .onSuccess(rows -> {
                        List<Alarm> alarms = new ArrayList<>();
                        if (rows != null && rows.iterator().hasNext()) {

                            for(Row row : rows){
                                alarms.add(new Alarm(row));
                            }

                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(alarms));

                        } else {
                            ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }

    }


    //=============SENSOR==============
    
    // Método que maneja POST /api/sensors
    private void handleCreateSensor(RoutingContext ctx) {
        try {
            Sensor sensor = gson.fromJson(ctx.getBodyAsString(), Sensor.class);

            client.preparedQuery(Sensor.CREATE_SENSOR)
                .execute(Tuple.of(sensor.getName(), sensor.getType(), sensor.getDeviceId()))
                .onSuccess(res -> {

                    Integer id =  res.property(MySQLClient.LAST_INSERTED_ID).intValue();
                    sensor.setSensor_id(id);


                    ctx.response()
                        .setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(gson.toJson(sensor));
                })
                .onFailure(err -> {
                    System.out.println("Error: " + err.getMessage());
                    ctx.response().setStatusCode(500)
                        .end("Error al insertar sensor: " + err.getMessage());
                });

        } catch (Exception e) {
            ctx.response().setStatusCode(400)
                .end(ControllerErrors.ERROR_IN_JSON);
        }
    }
    //Método que maneja GET /api/sensors{sensor_id}
    private void handleGetSensorById(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("sensor_id"));

        client.preparedQuery(Sensor.GET_SENSOR_ID)
            .execute(Tuple.of(id))
            .onSuccess(rows -> {
                if (rows != null && rows.size() > 0) {

                    Row row = rows.iterator().next();
                    Sensor sensor = new Sensor(row);

                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200)
                        .end(gson.toJson(sensor));
                } else {
                    ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                }
            })
            .onFailure(err -> {
                ctx.response().setStatusCode(500)
                    .end(ControllerErrors.BAD_CONSULT + err.getMessage());
            });
    }
   //Método que maneja GET /api/sensors
    private void handleGetAllSensors(RoutingContext ctx) {

        client.query(Sensor.GET_ALL_SENSOR).execute()
            .onSuccess(rows -> {
                List<Sensor> sensores = new ArrayList<>();
                for (Row row : rows) {
                    Sensor sensor = new Sensor(row);
                    sensores.add(sensor);
                }

                String jsonResponse = gson.toJson(sensores);
                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(jsonResponse);
            })
            .onFailure(err -> {
                ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
            });
    }

    
  //Método que maneja PUT /api/sensors{sensor_id}
    private void handleUpdateSensor(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("sensor_id"));
            Sensor sensor = gson.fromJson(ctx.getBodyAsString(), Sensor.class);

            client.preparedQuery(Sensor.UPDATE_SENSOR)
                .execute(Tuple.of(sensor.getName(), sensor.getType(), sensor.getDeviceId(), id))
                .onSuccess(res -> {
                    if (res.rowCount() > 0) {
                        sensor.setSensor_id(id);
                        ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(gson.toJson(sensor));
                    } else {
                        ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }
    }

    
  //Método que maneja DELETE /api/sensors{sensor_id}
    private void handleDeleteSensor(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("sensor_id"));

            client.preparedQuery(Sensor.DELETE_SENSOR)
                .execute(Tuple.of(id))
                .onSuccess(res -> {
                    if (res.rowCount() > 0) {
                        ctx.response().setStatusCode(204).end(); // No Content
                    } else {
                        ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                });
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }
    }

    //=============Alarm==============
    // Método que maneja POST /api/alarms
    private void handleCreateAlarms(RoutingContext ctx) {
        try {
            Alarm alarm = gson.fromJson(ctx.getBodyAsString(), Alarm.class);

            client.preparedQuery(Alarm.CREATE_ALARM)
                .execute(Tuple.of(
                    alarm.getName(),
                    alarm.getType(),
                    alarm.getDeviceId()
                ))
                .onSuccess(res -> {

                    Integer id =  res.property(MySQLClient.LAST_INSERTED_ID).intValue();
                    alarm.setAlarm_id(id);

                    ctx.response()
                        .setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(gson.toJson(alarm));
                })
                .onFailure(err -> {
                    ctx.response()
                        .setStatusCode(500)
                        .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                });

        } catch (Exception e) {
            ctx.response()
                .setStatusCode(400)
                .end(ControllerErrors.ERROR_IN_JSON);
        }
    }

    // Método que maneja GET /api/alarms{alarm_id}
    private void handleGetAlarmsById(RoutingContext ctx) {
        // Obtener el ID desde la ruta
        String idParam = ctx.pathParam("alarm_id");
        
        try {
            int id = Integer.parseInt(idParam);

            client.preparedQuery(Alarm.GET_ALARM_ID)
                .execute(Tuple.of(id))
                .onSuccess(rows -> {
                	if (rows != null && rows.iterator().hasNext()) {
                        Row row = rows.iterator().next();

                        Alarm alarm = new Alarm(row);
                        ctx.response()
                            .setStatusCode(200)
                            .putHeader("Content-Type", "application/json")
                            .end(gson.toJson(alarm));
                    } else {
                        ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }
    }
    
    // Método que maneja GET /api/alarms
    private void handleGetAllAlarms(RoutingContext ctx) {

        client.query(Alarm.GET_ALL_ALARM).execute()
            .onSuccess(rows -> {
                List<Alarm> alarms = new ArrayList<>();
                for (Row row : rows) {
                    Alarm alarm = new Alarm(row);
                    alarms.add(alarm);
                }

                String jsonResponse = gson.toJson(alarms);
                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(jsonResponse);
            })
            .onFailure(err -> {
                ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
            });
    }
    //Método que maneja PUT /api/alarms{alarm_id}
    private void handleUpdateAlarms(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("alarm_id"));
            Alarm alarm = gson.fromJson(ctx.getBodyAsString(), Alarm.class);

            client.preparedQuery(Alarm.UPDATE_ALARM)
                .execute(Tuple.of(alarm.getName(), alarm.getType(), alarm.getDeviceId(), id))
                .onSuccess(res -> {
                    if (res.rowCount() > 0) {
                        alarm.setAlarm_id(id);
                        ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(gson.toJson(alarm));
                    } else {
                        ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }
    }
  //Método que maneja DELETE /api/alarms{alarm_id}
    private void handleDeleteAlarm(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("alarm_id"));

            client.preparedQuery(Alarm.DELETE_ALARM)
                .execute(Tuple.of(id))
                .onSuccess(res -> {
                    if (res.rowCount() > 0) {
                        ctx.response().setStatusCode(204).end(); // No Content
                    } else {
                        ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                });
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
        }
    }
 // ============== DEVICE ==============

 // POST /api/devices
 private void handleCreateDevice(RoutingContext ctx) {
     try {
         Device device = gson.fromJson(ctx.getBodyAsString(), Device.class);

         client.preparedQuery(Device.CREATE_DEVICE)
             .execute(Tuple.of(device.getName(), device.getGroupId()))
             .onSuccess(res -> {
                 Integer id = res.property(MySQLClient.LAST_INSERTED_ID).intValue();
                 device.setDevice_id(id);

                 ctx.response().setStatusCode(201)
                     .putHeader("Content-Type", "application/json")
                     .end(gson.toJson(device));
             })
             .onFailure(err -> {
                 ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
             });

     } catch (Exception e) {
         ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
     }
 }

 // GET /api/devices/{device_id}
 private void handleGetDeviceById(RoutingContext ctx) {
     try {
         int id = Integer.parseInt(ctx.pathParam("device_id"));

         client.preparedQuery(Device.GET_DEVICE_ID).execute(Tuple.of(id))
             .onSuccess(rows -> {
                 if (rows.iterator().hasNext()) {
                     Row row = rows.iterator().next();
                     Device device = new Device(row);

                     ctx.response().setStatusCode(200)
                         .putHeader("Content-Type", "application/json")
                         .end(gson.toJson(device));
                 } else {
                     ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                 }
             })
             .onFailure(err -> {
                 ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
             });

     } catch (NumberFormatException e) {
         ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
     }
 }

 // GET /api/devices
 private void handleGetAllDevices(RoutingContext ctx) {

     client.query(Device.GET_ALL_DEVICE).execute()
         .onSuccess(rows -> {
             List<Device> devices = new ArrayList<>();
             for (Row row : rows) {
                 Device device = new Device(row);
                 devices.add(device);
             }

             ctx.response().setStatusCode(200)
                 .putHeader("Content-Type", "application/json")
                 .end(gson.toJson(devices));
         })
         .onFailure(err -> {
             ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
         });
 }

 // PUT /api/devices/{device_id}
 private void handleUpdateDevice(RoutingContext ctx) {
     try {
         int id = Integer.parseInt(ctx.pathParam("device_id"));
         Device device = gson.fromJson(ctx.getBodyAsString(), Device.class);

         client.preparedQuery(Device.UPDATE_DEVICE)
             .execute(Tuple.of(device.getName(), device.getGroupId(), id))
             .onSuccess(res -> {
                 if (res.rowCount() > 0) {
                     device.setDevice_id(id);
                     ctx.response()
                         .putHeader("Content-Type", "application/json")
                         .end(gson.toJson(device));
                 } else {
                     ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                 }
             })
             .onFailure(err -> {
                 ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
             });

     } catch (Exception e) {
         ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
     }
 }

 // DELETE /api/devices/{device_id}
 private void handleDeleteDevice(RoutingContext ctx) {
     try {
         int id = Integer.parseInt(ctx.pathParam("device_id"));

         client.preparedQuery(Device.DELETE_DEVICE)
             .execute(Tuple.of(id))
             .onSuccess(res -> {
                 if (res.rowCount() > 0) {
                     ctx.response().setStatusCode(204).end();
                 } else {
                     ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
                 }
             })
             .onFailure(err -> {
                 ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
             });

     } catch (NumberFormatException e) {
         ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
     }
 }
 
 /*============= GROUP ==============*/

//POST /api/groups
private void handleCreateGroup(RoutingContext ctx) {
  try {
      Group group = gson.fromJson(ctx.getBodyAsString(), Group.class);

      client.preparedQuery(Group.CREATE_GROUP)
          .execute(Tuple.of(group.getName(), group.getMqttChannel(), group.getHome_id(), group.getSuppressed()))
          .onSuccess(res -> {

              Integer id = res.property(MySQLClient.LAST_INSERTED_ID).intValue();
              group.setGroup_id(id);

              ctx.response()
                  .setStatusCode(201)
                  .putHeader("Content-Type", "application/json")
                  .end(gson.toJson(group));
          })
          .onFailure(err -> {
              System.out.println(err.getMessage());
              ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
          });

  } catch (Exception e) {
      ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
  }
}

//GET /api/groups/:group_id
private void handleGetGroupById(RoutingContext ctx) {
  try {
      int id = Integer.parseInt(ctx.pathParam("group_id"));

      client.preparedQuery(Group.GET_GROUP_ID)
          .execute(Tuple.of(id))
          .onSuccess(rows -> {
              if (rows != null && rows.iterator().hasNext()) {
                  Row row = rows.iterator().next();
                  Group group = new Group(row);

                  ctx.response()
                      .setStatusCode(200)
                      .putHeader("Content-Type", "application/json")
                      .end(gson.toJson(group));
              } else {
                  ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
              }
          })
          .onFailure(err -> {
              ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
          });
  } catch (NumberFormatException e) {
      ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
  }
}

//GET /api/groups
private void handleGetAllGroups(RoutingContext ctx) {

  client.query(Group.GET_ALL_GROUP).execute()
      .onSuccess(rows -> {
          List<Group> groups = new ArrayList<>();
          for (Row row : rows) {
              Group group = new Group(row);
              groups.add(group);
          }

          ctx.response()
              .setStatusCode(200)
              .putHeader("Content-Type", "application/json")
              .end(gson.toJson(groups));
      })
      .onFailure(err -> {
          ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
      });
}

//PUT /api/groups/:group_id
private void handleUpdateGroup(RoutingContext ctx) {
  try {
      int id = Integer.parseInt(ctx.pathParam("group_id"));
      Group group = gson.fromJson(ctx.getBodyAsString(), Group.class);

      client.preparedQuery(Group.UPDATE_GROUP)
          .execute(Tuple.of(group.getName(), group.getMqttChannel(), id))
          .onSuccess(res -> {
              if (res.rowCount() > 0) {
                  group.setGroup_id(id);
                  ctx.response()
                      .putHeader("Content-Type", "application/json")
                      .end(gson.toJson(group));
              } else {
                  ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
              }
          })
          .onFailure(err -> {
              ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
          });

  } catch (Exception e) {
      ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
  }
}

//DELETE /api/groups/:group_id
private void handleDeleteGroup(RoutingContext ctx) {
  try {
      int id = Integer.parseInt(ctx.pathParam("group_id"));

      client.preparedQuery(Group.DELETE_GROUP)
          .execute(Tuple.of(id))
          .onSuccess(res -> {
              if (res.rowCount() > 0) {
                  ctx.response().setStatusCode(204).end(); // No Content
              } else {
                  ctx.response().setStatusCode(404).end(ControllerErrors.NOT_FOUND);
              }
          })
          .onFailure(err -> {
              ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
          });

  } catch (NumberFormatException e) {
      ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
  }
}
/*============= SENSOR VALUE =============*/

//POST /api/sensorValues
private void handleCreateSensorValue(RoutingContext ctx) {
    try {
        SensorValue sensorValue = gson.fromJson(JsonParser.parseString(ctx.body().asString()).getAsString(), SensorValue.class);

        client.preparedQuery(SensorValue.CREATE_SENSOR_VALUE)
            .execute(Tuple.of(sensorValue.getSensorId(), sensorValue.getValue(), sensorValue.getTimestamp()))
            .onSuccess(res -> {

                Integer id = res.property(MySQLClient.LAST_INSERTED_ID).intValue();
                sensorValue.setSensor_value_id(id);

                ctx.response()
                    .setStatusCode(201)
                    .putHeader("Content-Type", "application/json")
                    .end(gson.toJson(sensorValue));
            })
            .onFailure(err -> {
                ctx.response()
                    .setStatusCode(500)
                    .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
            });

    } catch (Exception e) {
        System.err.println("Error general: " + e.getMessage());
        ctx.response()
            .setStatusCode(400)
            .end(ControllerErrors.ERROR_IN_JSON + e.getMessage());
    }
}



//GET /api/sensorValues/:sensor_id
private void handleGetSensorValuesBySensorId(RoutingContext ctx) {
 try {
     int idSensor = Integer.parseInt(ctx.pathParam("sensor_id"));

     client.preparedQuery(SensorValue.GET_SENSOR_VALUE_ID)
         .execute(Tuple.of(idSensor))
         .onSuccess(rows -> {
             List<SensorValue> values = new ArrayList<>();
             for (Row row : rows) {
                 SensorValue value = new SensorValue(row);
                 values.add(value);
             }

             ctx.response()
                 .setStatusCode(200)
                 .putHeader("Content-Type", "application/json")
                 .end(gson.toJson(values));
         })
         .onFailure(err -> {
             ctx.response().setStatusCode(500).end(ControllerErrors.NOT_FOUND + err.getMessage());
         });

 } catch (NumberFormatException e) {
     ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
 }
}


/*============= ACTUATOR STATE =============*/

//POST /api/alarmStates
private void handleCreateAlarmState(RoutingContext ctx) {
 try {
     AlarmState state = gson.fromJson(ctx.getBodyAsString(), AlarmState.class);

     client.preparedQuery(AlarmState.CREATE_ALARM_STATE)
         .execute(Tuple.of(state.getActuatorId(), state.isState(), state.getTimestamp().toString()))
         .onSuccess(res -> {

             Integer id = res.property(MySQLClient.LAST_INSERTED_ID).intValue();
             state.setAlarm_state_id(id);

             ctx.response()
                 .setStatusCode(201)
                 .putHeader("Content-Type", "application/json")
                 .end(gson.toJson(state));
         })
         .onFailure(err -> {
             ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
         });

 } catch (Exception e) {
     ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
 }
}

//GET /api/actuatorStates/:alarm_id
private void handleGetAlarmStatesByAlarmId(RoutingContext ctx) {
 try {
     int idActuator = Integer.parseInt(ctx.pathParam("alarm_id"));

     client.preparedQuery(AlarmState.GET_ALARM_STATE_ID)
         .execute(Tuple.of(idActuator))
         .onSuccess(rows -> {
             List<AlarmState> states = new ArrayList<>();
             for (Row row : rows) {
                 AlarmState state = new AlarmState(row);
                 states.add(state);
             }

             ctx.response()
                 .setStatusCode(200)
                 .putHeader("Content-Type", "application/json")
                 .end(gson.toJson(states));
         })
         .onFailure(err -> {
             ctx.response().setStatusCode(500).end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
         });

 } catch (NumberFormatException e) {
     ctx.response().setStatusCode(400).end(ControllerErrors.ERROR_IN_JSON);
 }
}



    // POST /api/users
    private void handleCreateUser(RoutingContext ctx) {
        try {
            String nickname = ctx.getBodyAsJson().getString("nickname");
            String password =ctx.getBodyAsJson().getString("password");


            client.preparedQuery(User.CREATE_USER)
                    .execute(Tuple.of(nickname, password))
                    .onSuccess(res -> {



                        Integer id = res.property(MySQLClient.LAST_INSERTED_ID).intValue();
                        User user = new User(id, nickname, password);

                        ctx.response()
                                .setStatusCode(201)
                                .putHeader("Content-Type", "application/json")
                                .end(gson.toJson(user));
                    })
                    .onFailure(err -> {


                        ctx.response().setStatusCode(500)
                                .end(ControllerErrors.ERROR_ON_SERVER);
                    });

        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());

            ctx.response().setStatusCode(400)
                    .end(ControllerErrors.ERROR_IN_JSON);
        }
    }

    // GET /api/users/:user_id
    private void handleGetUserById(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("user_id"));

            client.preparedQuery(User.GET_USER_ID)
                    .execute(Tuple.of(id))
                    .onSuccess(rows -> {
                        if (rows != null && rows.iterator().hasNext()) {
                            Row row = rows.iterator().next();
                            User user = new User(row);

                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(user));
                        } else {
                            ctx.response().setStatusCode(404)
                                    .end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500)
                                .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
                    .end(ControllerErrors.ERROR_IN_JSON);
        }
    }

    // GET /api/users
    private void handleGetAllUsers(RoutingContext ctx) {
        client.query(User.GET_ALL_USER)
                .execute()
                .onSuccess(rows -> {
                    List<User> users = new ArrayList<>();
                    for (Row row : rows) {
                        User user = new User(row);
                        users.add(user);
                    }

                    ctx.response()
                            .setStatusCode(200)
                            .putHeader("Content-Type", "application/json")
                            .end(gson.toJson(users));
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500)
                            .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                });
    }

    // PUT /api/users/:user_id
    private void handleUpdateUser(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("user_id"));
            User user = gson.fromJson(ctx.getBodyAsString(), User.class);

            client.preparedQuery(User.UPDATE_USER)
                    .execute(Tuple.of(user.getNickname(), user.getPassword(), id))
                    .onSuccess(res -> {
                        if (res.rowCount() > 0) {
                            user.setUser_id(id);
                            ctx.response()
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(user));
                        } else {
                            ctx.response().setStatusCode(404)
                                    .end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500)
                                .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (Exception e) {
            ctx.response().setStatusCode(400)
                    .end(ControllerErrors.ERROR_IN_JSON);
        }
    }

    // DELETE /api/users/:user_id
    private void handleDeleteUser(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("user_id"));

            client.preparedQuery(User.DELETE_USER)
                    .execute(Tuple.of(id))
                    .onSuccess(res -> {
                        if (res.rowCount() > 0) {
                            ctx.response().setStatusCode(204).end();
                        } else {
                            ctx.response().setStatusCode(404)
                                    .end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500)
                                .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
                    .end(ControllerErrors.ERROR_IN_JSON);
        }
    }


    // POST /api/homes
    private void handleCreateHome(RoutingContext ctx) {
        try {
            Home home = gson.fromJson(ctx.getBodyAsString(), Home.class);

            client.preparedQuery(Home.CREATE_HOME)
                    .execute(Tuple.of(home.getUser_id()))
                    .onSuccess(res -> {

                        Integer id = res.property(MySQLClient.LAST_INSERTED_ID).intValue();
                        home.setHome_id(id);

                        ctx.response()
                                .setStatusCode(201)
                                .putHeader("Content-Type", "application/json")
                                .end(gson.toJson(home));
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500)
                                .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (Exception e) {
            ctx.response().setStatusCode(400)
                    .end(ControllerErrors.ERROR_IN_JSON);
        }
    }

    // GET /api/homes/:home_id
    private void handleGetHomeById(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("home_id"));

            client.preparedQuery(Home.GET_HOME_ID)
                    .execute(Tuple.of(id))
                    .onSuccess(rows -> {
                        if (rows != null && rows.iterator().hasNext()) {
                            Row row = rows.iterator().next();
                            Home home = new Home(row);

                            ctx.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(home));
                        } else {
                            ctx.response().setStatusCode(404)
                                    .end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500)
                                .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
                    .end(ControllerErrors.ERROR_IN_JSON);
        }
    }


    // PUT /api/homes/:home_id
    private void handleUpdateHome(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("home_id"));
            Home home = gson.fromJson(ctx.getBodyAsString(), Home.class);

            client.preparedQuery(Home.UPDATE_HOME)
                    .execute(Tuple.of(home.getUser_id(), id))
                    .onSuccess(res -> {
                        if (res.rowCount() > 0) {
                            home.setHome_id(id);
                            ctx.response()
                                    .putHeader("Content-Type", "application/json")
                                    .end(gson.toJson(home));
                        } else {
                            ctx.response().setStatusCode(404)
                                    .end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500)
                                .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (Exception e) {
            ctx.response().setStatusCode(400)
                    .end(ControllerErrors.ERROR_IN_JSON);
        }
    }

    // DELETE /api/homes/:home_id
    private void handleDeleteHome(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("home_id"));

            client.preparedQuery(Home.DELETE_HOME)
                    .execute(Tuple.of(id))
                    .onSuccess(res -> {
                        if (res.rowCount() > 0) {
                            ctx.response().setStatusCode(204).end();
                        } else {
                            ctx.response().setStatusCode(404)
                                    .end(ControllerErrors.NOT_FOUND);
                        }
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500)
                                .end(ControllerErrors.ERROR_ON_SERVER + err.getMessage());
                    });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
                    .end(ControllerErrors.ERROR_IN_JSON);
        }
    }
}
