package org.uade.progra3.modelo;

import java.util.List;
import java.util.stream.Collectors;

public class Feed {
    private List<Publicacion> listadoPublicaciones;

    public Feed(List<Publicacion> listadoPublicaciones) {
        this.listadoPublicaciones = listadoPublicaciones;
    }

    public List<Publicacion> getListadoPublicaciones() {
        return listadoPublicaciones;
    }

    public void setListadoPublicaciones(List<Publicacion> listadoPublicaciones) {
        this.listadoPublicaciones = listadoPublicaciones;
    }

    public List<Publicacion> obtenerPublicaciones() {
        return this.listadoPublicaciones;
    }

    public List<Publicacion> publicacionesPonderadas() {
        return this.listadoPublicaciones
                .stream()
                .sorted(Publicacion::compareTo)
                .collect(Collectors.toList());
    }
}
