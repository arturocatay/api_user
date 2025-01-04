package com.smartjob.dtos;

import java.io.Serializable;


public class AuthenticationResponse implements Serializable {

    private String jwtToken;
    private String mensaje;

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
