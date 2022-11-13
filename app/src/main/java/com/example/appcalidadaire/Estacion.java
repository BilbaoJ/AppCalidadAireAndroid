package com.example.appcalidadaire;

public class Estacion {
    Double longitud;
    Double latitud;
    Double pm;

    public Estacion(Double longitud, Double latitud, Double pm) {
        this.longitud = longitud;
        this.latitud = latitud;
        this.pm = pm;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getPm() {
        return pm;
    }

    public void setPm(Double pm) {
        this.pm = pm;
    }
}
