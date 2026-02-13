package org.uade.progra3.negocio;

import org.uade.progra3.modelo.CandidatoPublicaciones;
import org.uade.progra3.modelo.Portada;
import org.uade.progra3.modelo.Publicacion;

import java.util.List;

public class PortadaDinamica {

    /**
     * Calcula la selección óptima y la deja en el listado de publicaciones de la portada.
     * Limpia la portada y agrega solo las publicaciones seleccionadas (orden original).
     */
    public void obtenerPublicaciones(CandidatoPublicaciones feed, Portada portada) {
        List<Publicacion> publicaciones = feed.getListadoPublicaciones();

        if (publicaciones == null || publicaciones.isEmpty()) {
            portada.getPublicaciones().clear();
            return;
        }

        int cantidadPublicaciones = publicaciones.size();
        int espacioMaximoPortada = Portada.getTamanioMaximo();

        // +1 filas: fila 0 = "0 publicaciones" (caso base, beneficio 0). Filas 1..n = "hasta la publicación i".
        // +1 columnas: índices 0 hasta espacioMaximoPortada (ej. capacidad 100 → columnas 0,1,...,100 = 101).
        int[][] beneficioMaximoHasta = new int[cantidadPublicaciones + 1][espacioMaximoPortada + 1];

        // Llenar la tabla (programación dinámica). En cada celda decidimos "incluir o no" solo
        // para ese subproblema (primeras indicePub publicaciones, capacidad espacio). Esa decisión
        // no es la solución final: qué ítems van en la portada se obtiene recorriendo la tabla
        // hacia atrás desde (n, capacidadMaxima), por eso hace falta volcarSolucionEnPortada.
        for (int indicePub = 1; indicePub <= cantidadPublicaciones; indicePub++) {
            Publicacion publicacion = publicaciones.get(indicePub - 1);
            int beneficio = publicacion.ponderar();
            int tamanio = publicacion.getTamanio();

            for (int espacio = 0; espacio <= espacioMaximoPortada; espacio++) {
                int beneficioSinIncluir = beneficioMaximoHasta[indicePub - 1][espacio];
                beneficioMaximoHasta[indicePub][espacio] = beneficioSinIncluir;

                boolean cabeEnElEspacio = tamanio <= espacio;
                if (cabeEnElEspacio) {
                    int espacioRestante = espacio - tamanio;
                    int beneficioIncluyendo = beneficioMaximoHasta[indicePub - 1][espacioRestante] + beneficio;
                    if (beneficioIncluyendo > beneficioSinIncluir) {
                        beneficioMaximoHasta[indicePub][espacio] = beneficioIncluyendo;
                    }
                }
            }
        }

        volcarSolucionEnPortada(publicaciones, 
            beneficioMaximoHasta, 
            cantidadPublicaciones, 
            espacioMaximoPortada, 
            portada
        );
    }

    /**
     * Recorre la tabla hacia atrás y agrega al listado de la portada solo las publicaciones
     * que forman la solución óptima (orden original).
     */
    private void volcarSolucionEnPortada(List<Publicacion> publicaciones,
                                         int[][] beneficioMaximoHasta,
                                         int cantidadPublicaciones,
                                         int espacioMaximoPortada,
                                         Portada portada) {
        portada.getPublicaciones().clear();
        int espacioRestante = espacioMaximoPortada;
        for (int indicePub = cantidadPublicaciones; indicePub >= 1; indicePub--) {
            if (beneficioMaximoHasta[indicePub][espacioRestante] != beneficioMaximoHasta[indicePub - 1][espacioRestante]) {
                Publicacion publicacion = publicaciones.get(indicePub - 1);
                portada.getPublicaciones().add(0, publicacion);
                espacioRestante -= publicacion.getTamanio();
            }
        }
    }
}
