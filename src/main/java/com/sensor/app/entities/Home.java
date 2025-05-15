package com.sensor.app.entities;

public class Home {

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
