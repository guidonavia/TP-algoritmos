package org.uade.progra3.modelo;

import java.util.List;

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
}
