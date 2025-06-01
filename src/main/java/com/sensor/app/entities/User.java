package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

public class User {

    public static final String CREATE_USER= "INSERT IGNORE INTO User (nickname, password) VALUES (?, ?)";
    public static final String GET_USER_ID = "SELECT * FROM User WHERE user_id = ?";
    public static final String GET_ALL_USER = "SELECT * FROM User";
    public static final String UPDATE_USER = "UPDATE User SET nickname = ?, password = ? WHERE user_id = ?";
    public static final String DELETE_USER = "DELETE FROM User WHERE user_id = ?";

    public User(Row row){

        setUser_id(row.getInteger("user_id"));
        setNickname(row.getString("nickname"));
        setPassword(row.getString("password"));

    }

    public User(Integer user_id, String nickname, String password ){

        setUser_id(user_id);
        setNickname(nickname);
        setPassword(password);

    }

    public User(){}


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
