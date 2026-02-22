package org.uade.progra3.grafos;

import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.*;

/**
 * Problema Opcional 1: Simulación de Bloqueos y Conexiones Alternativas.
 *
 * Usa BFS (Breadth-First Search) para verificar la conectividad del grafo
 * después de eliminar una conexión (bloqueo) y calcular las conexiones mínimas
 * necesarias para restaurar la conectividad total.
 *
 * Estrategia:
 * 1. Se construye una representación no dirigida del grafo, excluyendo la
 *    conexión bloqueada (en ambas direcciones).
 * 2. Se ejecuta BFS repetidamente para descubrir todas las componentes conexas.
 * 3. Si hay más de una componente, se calculan C-1 conexiones nuevas (donde C
 *    es el número de componentes) para reconectar el grafo.
 *
 * Complejidad temporal: O(V + E) para la detección de componentes mediante BFS.
 * Complejidad espacial: O(V + E) para la lista de adyacencia no dirigida.
 */
public class ConectividadBFS {

    /**
     * Resultado de la simulación de bloqueo.
     */
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

        /** Indica si el grafo sigue siendo conexo después del bloqueo. */
        public boolean esConexo() {
            return conexo;
        }

        /** Lista de componentes conexas encontradas. */
        public List<List<Usuario>> getComponentes() {
            return componentes;
        }

        /** Conexiones mínimas sugeridas para restaurar la conectividad (vacía si ya es conexo). */
        public List<Conexion> getConexionesRestauracion() {
            return conexionesRestauracion;
        }
    }

    /**
     * Simula el bloqueo entre dos usuarios y analiza la conectividad resultante.
     *
     * Pasos del algoritmo:
     * 1. Construir lista de adyacencia no dirigida sin la arista bloqueada.
     * 2. BFS iterativo para encontrar componentes conexas.
     * 3. Si hay más de una componente, calcular conexiones de restauración.
     *
     * @param grafo      el grafo completo de la red social
     * @param bloqueador el usuario que inicia el bloqueo
     * @param bloqueado  el usuario bloqueado
     * @return resultado con estado de conectividad, componentes y restauración
     */
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

    /**
     * Construye una lista de adyacencia no dirigida a partir del grafo dirigido,
     * excluyendo las conexiones entre bloqueador y bloqueado en ambas direcciones.
     *
     * El grafo original es dirigido, pero para evaluar la conectividad de una red
     * social (donde la amistad es bidireccional) se trata como no dirigido.
     */
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

    /**
     * Encuentra todas las componentes conexas mediante BFS.
     *
     * Recorre todos los nodos del grafo. Para cada nodo no visitado,
     * inicia un BFS que descubre toda su componente conexa.
     *
     * Complejidad: O(V + E) — cada nodo y arista se visita exactamente una vez.
     */
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

    /**
     * BFS (Breadth-First Search) desde un nodo origen.
     *
     * Utiliza una cola (FIFO) para explorar el grafo nivel por nivel:
     * 1. Encola el nodo origen y lo marca como visitado.
     * 2. Mientras la cola no esté vacía:
     *    a. Desencola un nodo.
     *    b. Para cada vecino no visitado, lo marca y lo encola.
     * 3. Todos los nodos desencolados forman una componente conexa.
     *
     * @return lista de usuarios alcanzables desde el origen (su componente conexa)
     */
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

    /**
     * Calcula el conjunto mínimo de conexiones para restaurar la conectividad.
     *
     * Para C componentes se necesitan exactamente C-1 conexiones nuevas.
     * Se conectan componentes adyacentes (i con i+1), buscando la arista de menor
     * peso disponible en el grafo original. Si no existe ninguna, se crea una
     * con peso 1 entre los primeros nodos de cada componente.
     *
     * @return lista de conexiones sugeridas para restaurar la conectividad
     */
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

        // Conectar componente i con componente i+1 (C-1 conexiones)
        for (int i = 0; i < componentes.size() - 1; i++) {
            Conexion mejor = null;

            for (Conexion c : grafo.getConexiones()) {
                // Excluir la conexión bloqueada
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
