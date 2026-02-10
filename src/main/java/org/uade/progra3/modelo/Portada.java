package org.uade.progra3.modelo;

import java.util.List;

public class Portada {
    private static final int TAMANIO_MAXIMO = 100;
    private List<Publicacion> publicaciones;


    public Portada(List<Publicacion> publicaciones) {
        this.publicaciones = publicaciones;
    }
}
