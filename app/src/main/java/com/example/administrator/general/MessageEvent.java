package com.example.administrator.general;

/**
 * Created by Administrator on 2018\11\6 0006.
 */

public class MessageEvent {
    private String message;
    public MessageEvent(String message){
        this.message=message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
