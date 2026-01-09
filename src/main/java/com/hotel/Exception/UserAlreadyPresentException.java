package com.hotel.Exception;

public class UserAlreadyPresentException extends Exception{

    public UserAlreadyPresentException(String msg){
        super(msg);
    }
}
