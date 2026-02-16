package org.uade.progra3.negocio;

import org.uade.progra3.modelo.CandidatoPublicaciones;
import org.uade.progra3.modelo.Portada;
import org.uade.progra3.modelo.Publicacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Portada (0/1 Knapsack) con espacio optimizado: solo dos filas para la DP.
 * Espacio para la recurrencia: O(W) en lugar de O(n×W).
 * Reconstrucción: se guarda una fila de "¿tomé este ítem?" por cada publicación (O(n×W) bits),
 * así podemos hacer backtrack sin tener la tabla completa.
 */
public class PortadaDinamicaOptimo {

    public void obtenerPublicaciones(CandidatoPublicaciones feed, Portada portada) {
        List<Publicacion> publicaciones = feed.getListadoPublicaciones();

        if (publicaciones == null || publicaciones.isEmpty()) {
            portada.getPublicaciones().clear();
            return;
        }

        int n = publicaciones.size();
        int W = Portada.getTamanioMaximo();

        // Solo dos filas: anterior y actual. Fila 0 implícita (todos 0).
        int[] dpAnterior = new int[W + 1];
        int[] dpActual = new int[W + 1];
        boolean[] tomadoActual = new boolean[W + 1];

        // Guardamos por cada fila i si en capacidad w tomamos la publicación i (para reconstruir).
        List<boolean[]> tomadoPorFila = new ArrayList<>(n);

        for (int i = 1; i <= n; i++) {
            Publicacion pub = publicaciones.get(i - 1);
            int beneficio = pub.ponderar();
            int tamanio = pub.getTamanio();

            for (int w = 0; w <= W; w++) {
                int sinIncluir = dpAnterior[w];
                dpActual[w] = sinIncluir;
                tomadoActual[w] = false;

                if (tamanio <= w) {
                    int incluyendo = dpAnterior[w - tamanio] + beneficio;
                    if (incluyendo > sinIncluir) {
                        dpActual[w] = incluyendo;
                        tomadoActual[w] = true;
                    }
                }
            }

            tomadoPorFila.add(tomadoActual.clone());

            // Rotar: la actual pasa a ser la anterior para la próxima iteración.
            int[] tmp = dpAnterior;
            dpAnterior = dpActual;
            dpActual = tmp;
        }

        // Al final la fila "última" está en dpAnterior (por el swap).
        reconstruirSolucion(publicaciones, tomadoPorFila, n, W, portada);
    }

    /**
     * Reconstrucción: desde (n, W) hacia atrás usando las decisiones guardadas por fila.
     * Si tomadoPorFila.get(i-1)[espacioRestante] == true, la publicación i fue incluida.
     */
    private void reconstruirSolucion(List<Publicacion> publicaciones,
                                     List<boolean[]> tomadoPorFila,
                                     int cantidadPublicaciones,
                                     int espacioMaximo,
                                     Portada portada) {
        portada.getPublicaciones().clear();
        int w = espacioMaximo;

        for (int i = cantidadPublicaciones; i >= 1; i--) {
            boolean[] tomado = tomadoPorFila.get(i - 1);
            if (w >= 0 && tomado[w]) {
                Publicacion pub = publicaciones.get(i - 1);
                portada.getPublicaciones().add(0, pub);
                w -= pub.getTamanio();
            }
        }
    }
}
