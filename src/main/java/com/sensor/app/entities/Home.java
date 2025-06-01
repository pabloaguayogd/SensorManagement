package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

public class Home {

    public static final String CREATE_HOME= "INSERT IGNORE INTO Home (user_id) VALUES (?)";
    public static final String GET_HOME_ID = "SELECT * FROM Home WHERE home_id = ?";
    public static final String UPDATE_HOME = "UPDATE Home SET user_id = ? WHERE home_id = ?";
    public static final String DELETE_HOME = "DELETE FROM Home WHERE home_id = ?";

    public Home(Row row){

        setHome_id(row.getInteger("home_id"));
        setUser_id(row.getInteger("user_id"));

    }
    public Home(){}


    private Integer home_id;
    private Integer user_id;


    public Integer getUser_id() {
        return user_id;
    }

    public Integer getHome_id() {
        return home_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public void setHome_id(Integer home_id) {
        this.home_id = home_id;
    }
}
