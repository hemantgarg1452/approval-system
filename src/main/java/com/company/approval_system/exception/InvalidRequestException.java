package com.company.approval_system.exception;

//Thrown when business rules are violated (e.g., approving an already approved request)
public class InvalidRequestException extends RuntimeException{
    public InvalidRequestException(String message){
        super(message);
    }
}
