package com.example.offermanagementsystem.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // špatné ID v URL (např. /offers/new vs /offers/{id})
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch() {
        return "redirect:/offers?error=bad-request";
    }

    // fallback – cokoliv neočekávaného
    @ExceptionHandler(Exception.class)
    public String handleGeneralError(Exception e) {
        e.printStackTrace(); // jen pro DEV
        return "error/general";
    }
}