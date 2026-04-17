package com.buchi.petfinder.exception;

public class PetFinderApiException extends RuntimeException {

    public PetFinderApiException(String message) {
        super(message);
    }

    public PetFinderApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
