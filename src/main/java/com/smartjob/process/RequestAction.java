package com.smartjob.process;

import org.springframework.http.ResponseEntity;

@FunctionalInterface
public interface RequestAction {
    ResponseEntity<?> execute();
}