package com.javamentor.qa.platform.webapp.controllers.advice;

import com.javamentor.qa.platform.exception.ConstrainException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return  ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ConstrainException.class)
    public ResponseEntity<String> handleConstrainException(ConstrainException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<String> handleNoResultException(NoResultException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
