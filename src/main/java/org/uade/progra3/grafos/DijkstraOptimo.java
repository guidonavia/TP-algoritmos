package org.uade.progra3.grafos;

import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Dijkstra optimizado con cola de prioridad (min-heap).
 * Extraer el mínimo es O(log V) en lugar de O(V), y solo se relajan aristas salientes.
 * Complejidad: O((V + E) log V).
 */
public class DijkstraOptimo {

    public static Map<Usuario, Integer> calcularCaminosMinimos(Grafo grafo, Usuario origen) {
        Map<Usuario, Integer> distancia = new HashMap<>();
        for (Usuario v : grafo.getUsuarios()) {
            distancia.put(v, Integer.MAX_VALUE);
        }
        distancia.put(origen, 0);

        Set<Usuario> visitados = new HashSet<>();

        // Cola de prioridad: (distancia, usuario). Orden por distancia ascendente.
        PriorityQueue<ParDistanciaUsuario> pq = new PriorityQueue<>(
                (a, b) -> Integer.compare(a.distancia, b.distancia)
        );
        pq.offer(new ParDistanciaUsuario(0, origen));

        while (!pq.isEmpty()) {
            ParDistanciaUsuario par = pq.poll();
            Usuario actual = par.usuario;
            int distActual = par.distancia;

            if (visitados.contains(actual)) continue;
            visitados.add(actual);

            for (Conexion conexion : grafo.getAdyacentes(actual)) {
                Usuario vecino = conexion.getDestino();
                if (visitados.contains(vecino)) continue;

                int nuevaDist = distActual + conexion.getPeso();
                if (nuevaDist < distancia.get(vecino)) {
                    distancia.put(vecino, nuevaDist);
                    pq.offer(new ParDistanciaUsuario(nuevaDist, vecino));
                }
            }
        }

        return distancia;
    }

    private static class ParDistanciaUsuario {
        final int distancia;
        final Usuario usuario;

        ParDistanciaUsuario(int distancia, Usuario usuario) {
            this.distancia = distancia;
            this.usuario = usuario;
        }
    }
}
