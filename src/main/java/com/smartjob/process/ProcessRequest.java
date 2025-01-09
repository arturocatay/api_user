package com.smartjob.process;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProcessRequest {

    public ResponseEntity<?> process(RequestAction action) {
        try {
            return action.execute();
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("registrado") ? HttpStatus.IM_USED : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(e.getMessage(), status);
        }
    }
}
