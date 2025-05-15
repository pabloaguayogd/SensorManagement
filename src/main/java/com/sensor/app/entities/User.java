package com.sensor.app.entities;

public class User {


    private Integer user_id;
    private String nickname;
    private String password;

    public Integer getUser_id() {
        return user_id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
