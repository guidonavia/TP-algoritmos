package org.uade.progra3.grafos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Djikstra {

    public static Map<Usuario, Integer> calcularCaminosMinimos(Grafo grafo, Usuario origen) {

        // distancia minima desde el usuario a cada posible amistad
        Map<Usuario, Integer> distancia = new HashMap<>();

        // usuarios ya visitados
        Set<Usuario> visitados = new HashSet<>();

        // inicializacion
        for (Usuario v : grafo.getUsuarios()) {
            distancia.put(v, Integer.MAX_VALUE);
        }
        distancia.put(origen, 0);

        // mientras haya usuarios sin visitar
        while (visitados.size() < grafo.getUsuarios().size()) {

            Usuario actual = obtenerUsuarioConMenorDistancia(distancia, visitados);

            if (actual == null) {
                break; // no hay mas alcanzables
            }

            visitados.add(actual);

            // recorrer conexiones adyacentes
            for (Conexion conexion : grafo.getAdyacentes(actual)) {
                Usuario vecino = conexion.getDestino();

                if (!visitados.contains(vecino)) {
                    int nuevaDistancia =
                            distancia.get(actual) + conexion.getPeso();

                    if (nuevaDistancia < distancia.get(vecino)) {
                        distancia.put(vecino, nuevaDistancia);
                    }
                }
            }
        }

        return distancia;
    }

    // devuelve el usuario no visitado con menor distancia
    private static Usuario obtenerUsuarioConMenorDistancia(
            Map<Usuario, Integer> distancia,
            Set<Usuario> visitados) {

        Usuario minimo = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (Usuario v : distancia.keySet()) {
            if (!visitados.contains(v) && distancia.get(v) < menorDistancia) {
                menorDistancia = distancia.get(v);
                minimo = v;
            }
        }

        return minimo;
    }
}
