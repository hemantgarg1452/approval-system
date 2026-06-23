package com.company.approval_system.exception;

//Thrown when a user attempts an action they don't have permission for
public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String message){
        super(message);
    }
}
