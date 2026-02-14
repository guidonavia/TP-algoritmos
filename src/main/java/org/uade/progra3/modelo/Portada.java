package org.uade.progra3.modelo;

import java.util.ArrayList;
import java.util.List;

public class Portada {
    private static final int TAMANIO_MAXIMO = 100;
    private final List<Publicacion> publicaciones;

    public static int getTamanioMaximo() {
        return TAMANIO_MAXIMO;
    }

    public Portada() {
        this.publicaciones = new ArrayList<>();
    }

    public Portada(List<Publicacion> publicaciones) {
        this.publicaciones = publicaciones != null ? new ArrayList<>(publicaciones) : new ArrayList<>();
    }

    public List<Publicacion> getPublicaciones() {
        return publicaciones;
    }
}
