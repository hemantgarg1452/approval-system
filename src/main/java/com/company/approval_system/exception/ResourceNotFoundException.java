package com.company.approval_system.exception;

//Thrown when a requested resource (user, request, etc.) is not found in the database
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue){
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
