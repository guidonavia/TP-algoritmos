package org.uade.progra3.grafos;

import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.*;

public class ConectividadBFS {

    public static class ResultadoBloqueo {
        private final boolean conexo;
        private final List<List<Usuario>> componentes;
        private final List<Conexion> conexionesRestauracion;

        public ResultadoBloqueo(boolean conexo,
                                List<List<Usuario>> componentes,
                                List<Conexion> conexionesRestauracion) {
            this.conexo = conexo;
            this.componentes = componentes;
            this.conexionesRestauracion = conexionesRestauracion;
        }

        public boolean esConexo() {
            return conexo;
        }

        public List<List<Usuario>> getComponentes() {
            return componentes;
        }

        public List<Conexion> getConexionesRestauracion() {
            return conexionesRestauracion;
        }
    }

    public static ResultadoBloqueo simularBloqueo(Grafo grafo, Usuario bloqueador, Usuario bloqueado) {
        Map<Usuario, List<Usuario>> adyacencia =
                construirAdyacenciaNoDir(grafo, bloqueador, bloqueado);

        List<List<Usuario>> componentes =
                encontrarComponentesBFS(adyacencia, grafo.getUsuarios());

        boolean conexo = componentes.size() <= 1;
        List<Conexion> restauracion = new ArrayList<>();

        if (!conexo) {
            restauracion = calcularRestauracion(componentes, grafo, bloqueador, bloqueado);
        }

        return new ResultadoBloqueo(conexo, componentes, restauracion);
    }

    private static Map<Usuario, List<Usuario>> construirAdyacenciaNoDir(
            Grafo grafo, Usuario bloqueador, Usuario bloqueado) {
        Map<Usuario, List<Usuario>> adj = new HashMap<>();
        for (Usuario u : grafo.getUsuarios()) {
            adj.put(u, new ArrayList<>());
        }

        for (Conexion c : grafo.getConexiones()) {
            boolean esBloqueada =
                    (c.getOrigen().equals(bloqueador) && c.getDestino().equals(bloqueado)) ||
                    (c.getOrigen().equals(bloqueado) && c.getDestino().equals(bloqueador));
            if (esBloqueada) continue;

            adj.get(c.getOrigen()).add(c.getDestino());
            adj.get(c.getDestino()).add(c.getOrigen());
        }
        return adj;
    }

    private static List<List<Usuario>> encontrarComponentesBFS(
            Map<Usuario, List<Usuario>> adj, Set<Usuario> usuarios) {
        List<List<Usuario>> componentes = new ArrayList<>();
        Set<Usuario> visitados = new HashSet<>();

        for (Usuario u : usuarios) {
            if (!visitados.contains(u)) {
                List<Usuario> componente = bfs(u, adj, visitados);
                componentes.add(componente);
            }
        }
        return componentes;
    }

    private static List<Usuario> bfs(Usuario origen,
                                     Map<Usuario, List<Usuario>> adj,
                                     Set<Usuario> visitados) {
        List<Usuario> componente = new ArrayList<>();
        Queue<Usuario> cola = new LinkedList<>();
        cola.add(origen);
        visitados.add(origen);

        while (!cola.isEmpty()) {
            Usuario actual = cola.poll();
            componente.add(actual);

            for (Usuario vecino : adj.getOrDefault(actual, Collections.emptyList())) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        return componente;
    }

    private static List<Conexion> calcularRestauracion(
            List<List<Usuario>> componentes, Grafo grafo,
            Usuario bloqueador, Usuario bloqueado) {
        List<Conexion> nuevas = new ArrayList<>();

        Map<Usuario, Integer> componenteDeUsuario = new HashMap<>();
        for (int i = 0; i < componentes.size(); i++) {
            for (Usuario u : componentes.get(i)) {
                componenteDeUsuario.put(u, i);
            }
        }

        for (int i = 0; i < componentes.size() - 1; i++) {
            Conexion mejor = null;

            for (Conexion c : grafo.getConexiones()) {
                boolean esBloqueada =
                        (c.getOrigen().equals(bloqueador) && c.getDestino().equals(bloqueado)) ||
                        (c.getOrigen().equals(bloqueado) && c.getDestino().equals(bloqueador));
                if (esBloqueada) continue;

                int compOrigen = componenteDeUsuario.get(c.getOrigen());
                int compDestino = componenteDeUsuario.get(c.getDestino());

                if ((compOrigen == i && compDestino == i + 1) ||
                    (compOrigen == i + 1 && compDestino == i)) {
                    if (mejor == null || c.getPeso() < mejor.getPeso()) {
                        mejor = c;
                    }
                }
            }

            if (mejor != null) {
                nuevas.add(mejor);
            } else {
                nuevas.add(new Conexion(
                        componentes.get(i).get(0),
                        componentes.get(i + 1).get(0), 1));
            }
        }

        return nuevas;
    }
}
