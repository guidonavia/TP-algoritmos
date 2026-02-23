package org.uade.progra3.negocio;

import org.uade.progra3.modelo.CandidatoPublicaciones;
import org.uade.progra3.modelo.Portada;
import org.uade.progra3.modelo.Publicacion;

import java.util.List;

public class PortadaDinamica {

    public void obtenerPublicaciones(CandidatoPublicaciones feed, Portada portada) {
        List<Publicacion> publicaciones = feed.getListadoPublicaciones();

        if (publicaciones == null || publicaciones.isEmpty()) {
            portada.getPublicaciones().clear();
            return;
        }

        int cantidadPublicaciones = publicaciones.size();
        int espacioMaximoPortada = Portada.getTamanioMaximo();

        int[][] beneficioMaximoHasta = new int[cantidadPublicaciones + 1][espacioMaximoPortada + 1];

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
