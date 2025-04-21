package com.sensor.app.mysql.rest;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import com.sensor.app.mysql.MySQLConnection;
import com.sensor.app.mysql.entities.Actuator;
import com.sensor.app.mysql.entities.ActuatorState;
import com.sensor.app.mysql.entities.Device;
import com.sensor.app.mysql.entities.Group;
import com.sensor.app.mysql.entities.Sensor;
import com.sensor.app.mysql.entities.SensorValue;

import com.sensor.app.util.LocalDateTimeAdapter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class RestAPIVerticle extends AbstractVerticle {

    private MySQLPool client;
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public void start(Promise<Void> startPromise) {
        // Configuración de conexión a la base de datos
        client = MySQLConnection.getClient();

        // Configuración del router HTTP
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        // ==== Endpoints sensors ====
        router.post("/api/sensors").handler(this::handleCreateSensor);
        router.get("/api/sensors/:id").handler(this::handleGetSensorById);
        router.get("/api/sensors").handler(this::handleGetAllSensors);
        router.put("/api/sensors/:id").handler(this::handleUpdateSensor);
        router.delete("/api/sensors/:id").handler(this::handleDeleteSensor);
        // ==== Endpoints actuators ====
        router.post("/api/actuators").handler(this::handleCreateActuator);
        router.get("/api/actuators/:id").handler(this::handleGetActuatorById);
        router.get("/api/actuators").handler(this::handleGetAllActuators);
        router.put("/api/actuators/:id").handler(this::handleUpdateActuator);
        router.delete("/api/actuators/:id").handler(this::handleDeleteActuator);
        // ====Endpoints devices ====
        router.post("/api/devices").handler(this::handleCreateDevice);
        router.get("/api/devices/:id").handler(this::handleGetDeviceById);
        router.get("/api/devices").handler(this::handleGetAllDevices);
        router.put("/api/devices/:id").handler(this::handleUpdateDevice);
        router.delete("/api/devices/:id").handler(this::handleDeleteDevice);
        // ====Endpoints groups ====
        router.post("/api/groups").handler(this::handleCreateGroup);
        router.get("/api/groups/:id").handler(this::handleGetGroupById);
        router.get("/api/groups").handler(this::handleGetAllGroups);
        router.put("/api/groups/:id").handler(this::handleUpdateGroup);
        router.delete("/api/groups/:id").handler(this::handleDeleteGroup);
        // ====Endpoints sensorValues ====
        router.post("/api/sensorValues").handler(this::handleCreateSensorValue);
        router.get("/api/sensorValues/:id_sensor").handler(this::handleGetSensorValuesBySensorId);
        // ====Endpoints actuatorStates ====
        router.post("/api/actuatorStates").handler(this::handleCreateActuatorState);
        router.get("/api/actuatorStates/:id_actuator").handler(this::handleGetActuatorStatesByActuatorId);

        // Iniciar servidor HTTP
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, http -> {
                if (http.succeeded()) {
                    System.out.println("Servidor REST iniciado en http://localhost:8080");
                    startPromise.complete();
                } else {
                    System.out.println("Error al iniciar servidor HTTP: " + http.cause().getMessage());
                    startPromise.fail(http.cause());
                }
            });
    }
    
  
    //=============SENSOR==============
    
    // Método que maneja POST /api/sensors
    private void handleCreateSensor(RoutingContext ctx) {
        try {
            Sensor sensor = gson.fromJson(ctx.getBodyAsString(), Sensor.class);

            String sql = "INSERT INTO sensor (name, type, identifier, device_id) VALUES (?, ?, ?, ?)";
            client.preparedQuery(sql)
                .execute(Tuple.of(sensor.getName(), sensor.getType(), sensor.getIdentifier(), sensor.getDeviceId()))
                .onSuccess(res -> {
                    ctx.response()
                        .setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(gson.toJson(sensor));
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500)
                        .end("Error al insertar sensor: " + err.getMessage());
                });

        } catch (Exception e) {
            ctx.response().setStatusCode(400)
                .end("Error en el formato del JSON recibido.");
        }
    }
    //Método que maneja GET /api/sensors{id}
    private void handleGetSensorById(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        String sql = "SELECT * FROM sensor WHERE id = ?";

        client.preparedQuery(sql)
            .execute(Tuple.of(id))
            .onSuccess(rows -> {
                if (rows != null && rows.size() > 0) {
                    Row row = rows.iterator().next();

                    Sensor sensor = new Sensor();
                    sensor.setId(row.getInteger("id"));
                    sensor.setName(row.getString("name"));
                    sensor.setType(row.getString("type"));
                    sensor.setIdentifier(row.getString("identifier"));
                    sensor.setDeviceId(row.getInteger("device_id"));

                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200)
                        .end(gson.toJson(sensor));
                } else {
                    ctx.response().setStatusCode(404).end("Sensor no encontrado.");
                }
            })
            .onFailure(err -> {
                ctx.response().setStatusCode(500)
                    .end("Error en la consulta: " + err.getMessage());
            });
    }
   //Método que maneja GET /api/sensors
    private void handleGetAllSensors(RoutingContext ctx) {
        String sql = "SELECT * FROM sensor";

        client.query(sql).execute()
            .onSuccess(rows -> {
                List<Sensor> sensores = new ArrayList<>();
                for (Row row : rows) {
                    Sensor sensor = new Sensor();
                    sensor.setId(row.getInteger("id"));
                    sensor.setName(row.getString("name"));
                    sensor.setType(row.getString("type"));
                    sensor.setIdentifier(row.getString("identifier"));
                    sensor.setDeviceId(row.getInteger("device_id"));
                    sensores.add(sensor);
                }

                String jsonResponse = gson.toJson(sensores);
                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(jsonResponse);
            })
            .onFailure(err -> {
                ctx.response().setStatusCode(500).end("Error al obtener sensores: " + err.getMessage());
            });
    }

    
  //Método que maneja PUT /api/sensors{id}
    private void handleUpdateSensor(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Sensor sensor = gson.fromJson(ctx.getBodyAsString(), Sensor.class);

            String sql = "UPDATE sensor SET name = ?, type = ?, identifier = ?, device_id = ? WHERE id = ?";

            client.preparedQuery(sql)
                .execute(Tuple.of(sensor.getName(), sensor.getType(), sensor.getIdentifier(), sensor.getDeviceId(), id))
                .onSuccess(res -> {
                    if (res.rowCount() > 0) {
                        sensor.setId(id);
                        ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(gson.toJson(sensor));
                    } else {
                        ctx.response().setStatusCode(404).end("Sensor no encontrado.");
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end("Error al actualizar: " + err.getMessage());
                });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Formato inválido.");
        }
    }

    
  //Método que maneja DELETE /api/sensors{id}
    private void handleDeleteSensor(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            String sql = "DELETE FROM sensor WHERE id = ?";

            client.preparedQuery(sql)
                .execute(Tuple.of(id))
                .onSuccess(res -> {
                    if (res.rowCount() > 0) {
                        ctx.response().setStatusCode(204).end(); // No Content
                    } else {
                        ctx.response().setStatusCode(404).end("Sensor no encontrado.");
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end("Error al eliminar: " + err.getMessage());
                });
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("ID inválido.");
        }
    }

    //=============ACTUATOR==============
    // Método que maneja POST /api/actuators
    private void handleCreateActuator(RoutingContext ctx) {
        try {
            Actuator actuator = gson.fromJson(ctx.getBodyAsString(), Actuator.class);

            String sql = "INSERT INTO actuator (name, type, identifier, device_id) VALUES (?, ?, ?, ?)";

            client.preparedQuery(sql)
                .execute(Tuple.of(
                    actuator.getName(),
                    actuator.getType(),
                    actuator.getIdentifier(),
                    actuator.getDeviceId()
                ))
                .onSuccess(res -> {
                    ctx.response()
                        .setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(gson.toJson(actuator));
                })
                .onFailure(err -> {
                    ctx.response()
                        .setStatusCode(500)
                        .end("Error al crear actuador: " + err.getMessage());
                });

        } catch (Exception e) {
            ctx.response()
                .setStatusCode(400)
                .end("JSON mal formado");
        }
    }

    // Método que maneja GET /api/actuators{id}
    private void handleGetActuatorById(RoutingContext ctx) {
        // Obtener el ID desde la ruta
        String idParam = ctx.pathParam("id");
        
        try {
            int id = Integer.parseInt(idParam);
            String sql = "SELECT * FROM actuator WHERE id = ?";

            client.preparedQuery(sql)
                .execute(Tuple.of(id))
                .onSuccess(rows -> {
                	if (rows != null && rows.iterator().hasNext()) {
                        Row row = rows.iterator().next();
                        Actuator actuator = new Actuator();
                        actuator.setId(row.getInteger("id"));
                        actuator.setName(row.getString("name"));
                        actuator.setType(row.getString("type"));
                        actuator.setIdentifier(row.getString("identifier"));
                        actuator.setDeviceId(row.getInteger("device_id"));
                        ctx.response()
                            .setStatusCode(200)
                            .putHeader("Content-Type", "application/json")
                            .end(gson.toJson(actuator));
                    } else {
                        ctx.response().setStatusCode(404).end("Actuador no encontrado.");
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end("Error al buscar actuador: " + err.getMessage());
                });

        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("ID inválido.");
        }
    }
    
    // Método que maneja GET /api/actuators
    private void handleGetAllActuators(RoutingContext ctx) {
        String sql = "SELECT * FROM actuator";

        client.query(sql).execute()
            .onSuccess(rows -> {
                List<Actuator> actuators = new ArrayList<>();
                for (Row row : rows) {
                    Actuator actuator = new Actuator();
                    actuator.setId(row.getInteger("id"));
                    actuator.setName(row.getString("name"));
                    actuator.setType(row.getString("type"));
                    actuator.setIdentifier(row.getString("identifier"));
                    actuator.setDeviceId(row.getInteger("device_id"));
                    actuators.add(actuator);
                }

                String jsonResponse = gson.toJson(actuators);
                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(jsonResponse);
            })
            .onFailure(err -> {
                ctx.response().setStatusCode(500).end(" Error al obtener actuadores: " + err.getMessage());
            });
    }
    //Método que maneja PUT /api/actuators{id}
    private void handleUpdateActuator(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Actuator actuator = gson.fromJson(ctx.getBodyAsString(), Actuator.class);

            String sql = "UPDATE actuator SET name = ?, type = ?, identifier = ?, device_id = ? WHERE id = ?";

            client.preparedQuery(sql)
                .execute(Tuple.of(actuator.getName(), actuator.getType(), actuator.getIdentifier(), actuator.getDeviceId(), id))
                .onSuccess(res -> {
                    if (res.rowCount() > 0) {
                        actuator.setId(id);
                        ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(gson.toJson(actuator));
                    } else {
                        ctx.response().setStatusCode(404).end("Actuador no encontrado.");
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end("Error al actualizar: " + err.getMessage());
                });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Formato inválido.");
        }
    }
  //Método que maneja DELETE /api/actuators{id}
    private void handleDeleteActuator(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            String sql = "DELETE FROM actuator WHERE id = ?";

            client.preparedQuery(sql)
                .execute(Tuple.of(id))
                .onSuccess(res -> {
                    if (res.rowCount() > 0) {
                        ctx.response().setStatusCode(204).end(); // No Content
                    } else {
                        ctx.response().setStatusCode(404).end("Actuador no encontrado.");
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end("Error al eliminar: " + err.getMessage());
                });
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("ID inválido.");
        }
    }
 // ============== DEVICE ==============

 // POST /api/devices
 private void handleCreateDevice(RoutingContext ctx) {
     try {
         Device device = gson.fromJson(ctx.getBodyAsString(), Device.class);
         String sql = "INSERT INTO device (name, group_id) VALUES (?, ?)";

         client.preparedQuery(sql)
             .execute(Tuple.of(device.getName(), device.getGroupId()))
             .onSuccess(res -> {
                 ctx.response().setStatusCode(201)
                     .putHeader("Content-Type", "application/json")
                     .end(gson.toJson(device));
             })
             .onFailure(err -> {
                 ctx.response().setStatusCode(500).end("Error al crear dispositivo: " + err.getMessage());
             });

     } catch (Exception e) {
         ctx.response().setStatusCode(400).end("JSON inválido.");
     }
 }

 // GET /api/devices/{id}
 private void handleGetDeviceById(RoutingContext ctx) {
     try {
         int id = Integer.parseInt(ctx.pathParam("id"));
         String sql = "SELECT * FROM device WHERE id = ?";

         client.preparedQuery(sql).execute(Tuple.of(id))
             .onSuccess(rows -> {
                 if (rows.iterator().hasNext()) {
                     Row row = rows.iterator().next();
                     Device device = new Device();
                     device.setId(row.getInteger("id"));
                     device.setName(row.getString("name"));
                     device.setGroupId(row.getInteger("group_id"));

                     ctx.response().setStatusCode(200)
                         .putHeader("Content-Type", "application/json")
                         .end(gson.toJson(device));
                 } else {
                     ctx.response().setStatusCode(404).end("Dispositivo no encontrado.");
                 }
             })
             .onFailure(err -> {
                 ctx.response().setStatusCode(500).end("Error al buscar: " + err.getMessage());
             });

     } catch (NumberFormatException e) {
         ctx.response().setStatusCode(400).end("ID inválido.");
     }
 }

 // GET /api/devices
 private void handleGetAllDevices(RoutingContext ctx) {
     String sql = "SELECT * FROM device";

     client.query(sql).execute()
         .onSuccess(rows -> {
             List<Device> devices = new ArrayList<>();
             for (Row row : rows) {
                 Device device = new Device();
                 device.setId(row.getInteger("id"));
                 device.setName(row.getString("name"));
                 device.setGroupId(row.getInteger("group_id"));
                 devices.add(device);
             }

             ctx.response().setStatusCode(200)
                 .putHeader("Content-Type", "application/json")
                 .end(gson.toJson(devices));
         })
         .onFailure(err -> {
             ctx.response().setStatusCode(500).end("Error al obtener dispositivos: " + err.getMessage());
         });
 }

 // PUT /api/devices/{id}
 private void handleUpdateDevice(RoutingContext ctx) {
     try {
         int id = Integer.parseInt(ctx.pathParam("id"));
         Device device = gson.fromJson(ctx.getBodyAsString(), Device.class);
         String sql = "UPDATE device SET name = ?, group_id = ? WHERE id = ?";

         client.preparedQuery(sql)
             .execute(Tuple.of(device.getName(), device.getGroupId(), id))
             .onSuccess(res -> {
                 if (res.rowCount() > 0) {
                     device.setId(id);
                     ctx.response()
                         .putHeader("Content-Type", "application/json")
                         .end(gson.toJson(device));
                 } else {
                     ctx.response().setStatusCode(404).end("Dispositivo no encontrado.");
                 }
             })
             .onFailure(err -> {
                 ctx.response().setStatusCode(500).end("Error al actualizar: " + err.getMessage());
             });

     } catch (Exception e) {
         ctx.response().setStatusCode(400).end("Error en el formato.");
     }
 }

 // DELETE /api/devices/{id}
 private void handleDeleteDevice(RoutingContext ctx) {
     try {
         int id = Integer.parseInt(ctx.pathParam("id"));
         String sql = "DELETE FROM device WHERE id = ?";

         client.preparedQuery(sql)
             .execute(Tuple.of(id))
             .onSuccess(res -> {
                 if (res.rowCount() > 0) {
                     ctx.response().setStatusCode(204).end();
                 } else {
                     ctx.response().setStatusCode(404).end("Dispositivo no encontrado.");
                 }
             })
             .onFailure(err -> {
                 ctx.response().setStatusCode(500).end("Error al eliminar: " + err.getMessage());
             });

     } catch (NumberFormatException e) {
         ctx.response().setStatusCode(400).end("ID inválido.");
     }
 }
 
 /*============= GROUP ==============*/

//POST /api/groups
private void handleCreateGroup(RoutingContext ctx) {
  try {
      Group group = gson.fromJson(ctx.getBodyAsString(), Group.class);
      String sql = "INSERT INTO grupo (name, mqtt_channel) VALUES (?, ?)";

      client.preparedQuery(sql)
          .execute(Tuple.of(group.getName(), group.getMqttChannel()))
          .onSuccess(res -> {
              ctx.response()
                  .setStatusCode(201)
                  .putHeader("Content-Type", "application/json")
                  .end(gson.toJson(group));
          })
          .onFailure(err -> {
              ctx.response().setStatusCode(500).end("Error al crear grupo: " + err.getMessage());
          });

  } catch (Exception e) {
      ctx.response().setStatusCode(400).end("JSON mal formado.");
  }
}

//GET /api/groups/:id
private void handleGetGroupById(RoutingContext ctx) {
  try {
      int id = Integer.parseInt(ctx.pathParam("id"));
      String sql = "SELECT * FROM grupo WHERE id = ?";

      client.preparedQuery(sql)
          .execute(Tuple.of(id))
          .onSuccess(rows -> {
              if (rows != null && rows.iterator().hasNext()) {
                  Row row = rows.iterator().next();
                  Group group = new Group();
                  group.setId(row.getInteger("id"));
                  group.setName(row.getString("name"));
                  group.setMqttChannel(row.getString("mqtt_channel"));

                  ctx.response()
                      .setStatusCode(200)
                      .putHeader("Content-Type", "application/json")
                      .end(gson.toJson(group));
              } else {
                  ctx.response().setStatusCode(404).end("Grupo no encontrado.");
              }
          })
          .onFailure(err -> {
              ctx.response().setStatusCode(500).end("Error al obtener grupo: " + err.getMessage());
          });
  } catch (NumberFormatException e) {
      ctx.response().setStatusCode(400).end("ID inválido.");
  }
}

//GET /api/groups
private void handleGetAllGroups(RoutingContext ctx) {
  String sql = "SELECT * FROM grupo";

  client.query(sql).execute()
      .onSuccess(rows -> {
          List<Group> groups = new ArrayList<>();
          for (Row row : rows) {
              Group group = new Group();
              group.setId(row.getInteger("id"));
              group.setName(row.getString("name"));
              group.setMqttChannel(row.getString("mqtt_channel"));
              groups.add(group);
          }

          ctx.response()
              .setStatusCode(200)
              .putHeader("Content-Type", "application/json")
              .end(gson.toJson(groups));
      })
      .onFailure(err -> {
          ctx.response().setStatusCode(500).end("Error al obtener grupos: " + err.getMessage());
      });
}

//PUT /api/groups/:id
private void handleUpdateGroup(RoutingContext ctx) {
  try {
      int id = Integer.parseInt(ctx.pathParam("id"));
      Group group = gson.fromJson(ctx.getBodyAsString(), Group.class);

      String sql = "UPDATE grupo SET name = ?, mqtt_channel = ? WHERE id = ?";

      client.preparedQuery(sql)
          .execute(Tuple.of(group.getName(), group.getMqttChannel(), id))
          .onSuccess(res -> {
              if (res.rowCount() > 0) {
                  group.setId(id);
                  ctx.response()
                      .putHeader("Content-Type", "application/json")
                      .end(gson.toJson(group));
              } else {
                  ctx.response().setStatusCode(404).end("Grupo no encontrado.");
              }
          })
          .onFailure(err -> {
              ctx.response().setStatusCode(500).end("Error al actualizar grupo: " + err.getMessage());
          });

  } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Formato inválido.");
  }
}

//DELETE /api/groups/:id
private void handleDeleteGroup(RoutingContext ctx) {
  try {
      int id = Integer.parseInt(ctx.pathParam("id"));
      String sql = "DELETE FROM grupo WHERE id = ?";

      client.preparedQuery(sql)
          .execute(Tuple.of(id))
          .onSuccess(res -> {
              if (res.rowCount() > 0) {
                  ctx.response().setStatusCode(204).end(); // No Content
              } else {
                  ctx.response().setStatusCode(404).end("Grupo no encontrado.");
              }
          })
          .onFailure(err -> {
              ctx.response().setStatusCode(500).end("Error al eliminar grupo: " + err.getMessage());
          });

  } catch (NumberFormatException e) {
      ctx.response().setStatusCode(400).end("ID inválido.");
  }
}
/*============= SENSOR VALUE =============*/

//POST /api/sensorValues
private void handleCreateSensorValue(RoutingContext ctx) {
    try {


        System.out.println("JSON recibido: " + JsonParser.parseString(ctx.body().asString()).getAsString());

        SensorValue sensorValue = gson.fromJson(JsonParser.parseString(ctx.body().asString()).getAsString(), SensorValue.class);
        System.out.println("SensorValue parseado: " + sensorValue);

        String sql = "INSERT INTO sensorvalue (sensor_id, value, timestamp) VALUES (?, ?, ?)";

        client.preparedQuery(sql)
            .execute(Tuple.of(sensorValue.getSensorId(), sensorValue.getValue(), sensorValue.getTimestamp()))
            .onSuccess(res -> {
                System.out.println("Valor insertado en DB.");
                ctx.response()
                    .setStatusCode(201)
                    .putHeader("Content-Type", "application/json")
                    .end(gson.toJson(sensorValue));
            })
            .onFailure(err -> {
                System.err.println("Error al insertar en DB: " + err.getMessage());
                ctx.response()
                    .setStatusCode(500)
                    .end("Error al insertar valor del sensor: " + err.getMessage());
            });

    } catch (Exception e) {
        System.err.println("Error general: " + e.getMessage());
        ctx.response()
            .setStatusCode(400)
            .end("JSON mal formado o error en conversión: " + e.getMessage());
    }
}



//GET /api/sensorValues/:id_sensor
private void handleGetSensorValuesBySensorId(RoutingContext ctx) {
 try {
     int idSensor = Integer.parseInt(ctx.pathParam("id_sensor"));
     String sql = "SELECT * FROM sensorvalue WHERE sensor_id = ?";

     client.preparedQuery(sql)
         .execute(Tuple.of(idSensor))
         .onSuccess(rows -> {
             List<SensorValue> values = new ArrayList<>();
             for (Row row : rows) {
                 SensorValue value = new SensorValue();
                 value.setId(row.getInteger("id"));
                 value.setSensorId(row.getInteger("sensor_id"));
                 value.setValue(row.getFloat("value"));


                 LocalDateTime timestamp = row.get(LocalDateTime.class,"timestamp");

                 value.setTimestamp(timestamp);

                 values.add(value);
             }

             ctx.response()
                 .setStatusCode(200)
                 .putHeader("Content-Type", "application/json")
                 .end(gson.toJson(values));
         })
         .onFailure(err -> {
             ctx.response().setStatusCode(500).end("Error al obtener valores: " + err.getMessage());
         });

 } catch (NumberFormatException e) {
     ctx.response().setStatusCode(400).end("ID inválido.");
 }
}


/*============= ACTUATOR STATE =============*/

//POST /api/actuatorStates
private void handleCreateActuatorState(RoutingContext ctx) {
 try {
     ActuatorState state = gson.fromJson(ctx.getBodyAsString(), ActuatorState.class);
     String sql = "INSERT INTO actuator_state (actuator_id, state, timestamp) VALUES (?, ?, ?)";

     client.preparedQuery(sql)
         .execute(Tuple.of(state.getActuatorId(), state.isState(), state.getTimestamp().toString()))
         .onSuccess(res -> {
             ctx.response()
                 .setStatusCode(201)
                 .putHeader("Content-Type", "application/json")
                 .end(gson.toJson(state));
         })
         .onFailure(err -> {
             ctx.response().setStatusCode(500).end("Error al crear actuator state: " + err.getMessage());
         });

 } catch (Exception e) {
     ctx.response().setStatusCode(400).end("JSON mal formado.");
 }
}

//GET /api/actuatorStates/:id_actuator
private void handleGetActuatorStatesByActuatorId(RoutingContext ctx) {
 try {
     int idActuator = Integer.parseInt(ctx.pathParam("id_actuator"));
     String sql = "SELECT * FROM actuator_state WHERE actuator_id = ?";

     client.preparedQuery(sql)
         .execute(Tuple.of(idActuator))
         .onSuccess(rows -> {
             List<ActuatorState> states = new ArrayList<>();
             for (Row row : rows) {
                 ActuatorState state = new ActuatorState();
                 state.setId(row.getInteger("id"));
                 state.setActuatorId(row.getInteger("actuator_id"));
                 state.setState(row.getBoolean("state"));
                 state.setTimestamp(row.getLocalDateTime("timestamp"));
                 states.add(state);
             }

             ctx.response()
                 .setStatusCode(200)
                 .putHeader("Content-Type", "application/json")
                 .end(gson.toJson(states));
         })
         .onFailure(err -> {
             ctx.response().setStatusCode(500).end("Error al obtener estados: " + err.getMessage());
         });

 } catch (NumberFormatException e) {
     ctx.response().setStatusCode(400).end("ID inválido.");
 }
}





    

    

    
    


   



    
    
    
}
