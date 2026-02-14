package org.uade.progra3.modelo;

import java.util.List;

public class CandidatoPublicaciones {
    private final List<Publicacion> listadoPublicaciones;

    public CandidatoPublicaciones(List<Publicacion> listadoPublicaciones) {
        this.listadoPublicaciones = listadoPublicaciones;
    }

    public List<Publicacion> getListadoPublicaciones() {
        return listadoPublicaciones;
    }
}
