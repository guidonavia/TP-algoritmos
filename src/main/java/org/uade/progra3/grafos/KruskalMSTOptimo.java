package org.uade.progra3.grafos;

import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.*;

/**
 * Kruskal optimizado:
 * - Union-Find con compresión de caminos y unión por rango → Find/Union casi O(1) amortizado.
 * - Lista de aristas del grafo ordenada una vez → O(E log E) en lugar de O(V²) para armar la cola.
 * Complejidad total: O(E log E) dominada por el ordenamiento.
 */
public class KruskalMSTOptimo {

    public static Grafo arbolDeRecubrimientoMinimo(Grafo grafo) {
        Grafo resultado = new Grafo();
        for (Usuario v : grafo.getUsuarios()) {
            resultado.agregarUsuario(v);
        }

        UnionFind uf = new UnionFind(grafo.getUsuarios());

        // Usar las aristas del grafo y ordenar por peso (una sola vez). O(E log E).
        List<Conexion> aristas = new ArrayList<>(grafo.getConexiones());
        aristas.sort(Comparator.comparingInt(Conexion::getPeso));

        int aristasAgregadas = 0;
        int n = grafo.getUsuarios().size();
        for (Conexion c : aristas) {
            if (aristasAgregadas == n - 1) break;
            Usuario u = c.getOrigen();
            Usuario v = c.getDestino();
            if (uf.find(u) != uf.find(v)) {
                uf.union(u, v);
                resultado.agregarConexion(u, v, c.getPeso());
                aristasAgregadas++;
            }
        }
        return resultado;
    }

    /**
     * Union-Find con compresión de caminos y unión por rango.
     * Find y Union son O(α(n)) ≈ O(1) amortizado.
     */
    private static class UnionFind {
        private final Map<Usuario, Usuario> parent;
        private final Map<Usuario, Integer> rank;

        UnionFind(Collection<Usuario> usuarios) {
            parent = new HashMap<>();
            rank = new HashMap<>();
            for (Usuario u : usuarios) {
                parent.put(u, u);
                rank.put(u, 0);
            }
        }

        Usuario find(Usuario x) {
            if (!parent.get(x).equals(x)) {
                parent.put(x, find(parent.get(x))); // compresión de camino
            }
            return parent.get(x);
        }

        void union(Usuario x, Usuario y) {
            Usuario rx = find(x);
            Usuario ry = find(y);
            if (rx.equals(ry)) return;
            int rkx = rank.get(rx);
            int rky = rank.get(ry);
            if (rkx < rky) {
                parent.put(rx, ry);
            } else if (rkx > rky) {
                parent.put(ry, rx);
            } else {
                parent.put(ry, rx);
                rank.put(rx, rkx + 1);
            }
        }
    }
}
