package org.uade.progra3.negocio;

import org.uade.progra3.grafos.Grafo;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.*;

/**
 * Opcional 1 – Simulación de Bloqueos y Conexiones Alternativas.
 *
 * Simula el bloqueo de una conexión entre dos usuarios y determina si la red
 * sigue completamente conexa. Si quedó desconectada, usa backtracking para
 * encontrar el conjunto mínimo de nuevas conexiones que restablecen la
 * conectividad.
 *
 * Estrategia:
 *   1. Se elimina la conexión bloqueada del grafo (tratado como no dirigido).
 *   2. BFS identifica la componente conexa alcanzable desde cualquier nodo.
 *      Si todos los nodos están en esa componente, la red sigue conectada.
 *   3. Si no: se derivan las dos componentes A y B directamente del BFS.
 *      Las candidatas se generan SOLO entre nodos de A y nodos de B
 *      (cualquier par inter-componente garantiza reconectar la red).
 *   4. Backtracking sobre ese conjunto reducido de candidatas para encontrar
 *      el subconjunto mínimo — que siempre es de tamaño 1, ya que eliminar
 *      una sola arista de un grafo conexo produce a lo sumo 2 componentes.
 *
 * Complejidad mejorada respecto a la versión anterior:
 *   - Anterior: O(C(C,k) · (V+E)), con C = O(V²) pares candidatos totales.
 *   - Mejorada: O(V + E) — el BFS domina; el backtracking es O(|A|·|B|) en
 *     el peor caso pero siempre termina en la primera candidata de tamaño 1.
 *   - Espacio: O(V + E).
 */
public class BloqueoConexion {

    /**
     * Simula el bloqueo de {@code conexionBloqueada} en {@code grafo}.
     *
     * @param grafo             grafo completo de la red social
     * @param conexionBloqueada conexión que se elimina
     * @return resultado indicando si la red sigue conectada y, si no, qué
     *         conexiones mínimas hay que agregar para restablecerla
     */
    public ResultadoBloqueo simularBloqueo(Grafo grafo, Conexion conexionBloqueada) {
        Map<Usuario, Set<Usuario>> adj = construirAdjSinConexion(grafo, conexionBloqueada);

        Set<Usuario> todos = grafo.getUsuarios();
        Set<Usuario> componenteA = encontrarComponente(adj, todos.iterator().next());

        if (componenteA.size() == todos.size()) {
            return new ResultadoBloqueo(conexionBloqueada, true, Collections.emptyList());
        }

        // ComponenteB = todos los nodos que BFS no alcanzó desde componenteA.
        // Cualquier arista entre A y B reconecta la red, por lo que el
        // backtracking siempre termina en la primera candidata de tamaño 1.
        Set<Usuario> componenteB = new HashSet<>(todos);
        componenteB.removeAll(componenteA);

        List<Conexion> candidatas = generarCandidatas(componenteA, componenteB);
        List<Conexion> minimas = buscarConexionesMinimas(adj, todos, candidatas);

        return new ResultadoBloqueo(conexionBloqueada, false, minimas);
    }

    /**
     * Construye un mapa de adyacencias no dirigido a partir del grafo,
     * excluyendo la conexión bloqueada.
     */
    private Map<Usuario, Set<Usuario>> construirAdjSinConexion(Grafo grafo, Conexion bloqueada) {
        Map<Usuario, Set<Usuario>> adj = new HashMap<>();
        for (Usuario u : grafo.getUsuarios()) {
            adj.put(u, new HashSet<>());
        }
        for (Conexion c : grafo.getConexiones()) {
            boolean esBloqueada = c.getOrigen().equals(bloqueada.getOrigen())
                    && c.getDestino().equals(bloqueada.getDestino());
            if (!esBloqueada) {
                adj.get(c.getOrigen()).add(c.getDestino());
                adj.get(c.getDestino()).add(c.getOrigen());
            }
        }
        return adj;
    }

    /**
     * BFS que devuelve el conjunto de nodos alcanzables desde {@code inicio}.
     * Reemplaza al antiguo estaConectado: en vez de un booleano, devuelve
     * la componente, de modo que se pueden derivar ambas componentes en O(V+E)
     * sin un segundo recorrido.
     */
    private Set<Usuario> encontrarComponente(Map<Usuario, Set<Usuario>> adj, Usuario inicio) {
        Set<Usuario> visitados = new HashSet<>();
        Queue<Usuario> cola = new LinkedList<>();
        cola.add(inicio);
        visitados.add(inicio);
        while (!cola.isEmpty()) {
            for (Usuario vecino : adj.get(cola.poll())) {
                if (visitados.add(vecino)) cola.add(vecino);
            }
        }
        return visitados;
    }

    /**
     * BFS de conectividad (usado por estaConectadoConAdicionales).
     */
    private boolean estaConectado(Map<Usuario, Set<Usuario>> adj, Set<Usuario> usuarios) {
        if (usuarios.isEmpty()) return true;
        return encontrarComponente(adj, usuarios.iterator().next()).size() == usuarios.size();
    }

    /**
     * Genera candidatas SOLO entre las dos componentes desconectadas.
     * Cualquier par (u ∈ componenteA, v ∈ componenteB) es una arista válida
     * que reconecta la red, por lo que el espacio de búsqueda pasa de O(V²)
     * a O(|A| · |B|) y el backtracking termina en el primer intento.
     */
    private List<Conexion> generarCandidatas(Set<Usuario> componenteA, Set<Usuario> componenteB) {
        List<Conexion> candidatas = new ArrayList<>();
        for (Usuario u : componenteA) {
            for (Usuario v : componenteB) {
                candidatas.add(new Conexion(u, v, 1));
            }
        }
        return candidatas;
    }

    /**
     * Backtracking: itera de tamaño 1 en adelante y devuelve el primer
     * subconjunto de candidatas que, sumado al grafo bloqueado, restaura
     * la conectividad. Con candidatas inter-componente siempre termina en
     * tamaño 1.
     */
    private List<Conexion> buscarConexionesMinimas(Map<Usuario, Set<Usuario>> adjBase,
                                                   Set<Usuario> usuarios,
                                                   List<Conexion> candidatas) {
        for (int tamanio = 1; tamanio <= candidatas.size(); tamanio++) {
            List<Conexion> seleccion = new ArrayList<>();
            if (backtrack(adjBase, usuarios, candidatas, seleccion, 0, tamanio)) {
                return new ArrayList<>(seleccion);
            }
        }
        return candidatas;
    }

    /**
     * Backtracking recursivo: construye combinaciones de exactamente {@code objetivo}
     * elementos y verifica si cada una restaura la conectividad.
     */
    private boolean backtrack(Map<Usuario, Set<Usuario>> adjBase,
                              Set<Usuario> usuarios,
                              List<Conexion> candidatas,
                              List<Conexion> seleccion,
                              int inicio,
                              int objetivo) {
        if (seleccion.size() == objetivo) {
            return estaConectadoConAdicionales(adjBase, usuarios, seleccion);
        }
        for (int i = inicio; i < candidatas.size(); i++) {
            seleccion.add(candidatas.get(i));
            if (backtrack(adjBase, usuarios, candidatas, seleccion, i + 1, objetivo)) return true;
            seleccion.remove(seleccion.size() - 1);
        }
        return false;
    }

    /**
     * Verifica la conectividad del grafo base (post-bloqueo) sumado a las
     * conexiones adicionales propuestas.
     */
    private boolean estaConectadoConAdicionales(Map<Usuario, Set<Usuario>> adjBase,
                                                Set<Usuario> usuarios,
                                                List<Conexion> adicionales) {
        Map<Usuario, Set<Usuario>> adj = new HashMap<>();
        for (Map.Entry<Usuario, Set<Usuario>> entry : adjBase.entrySet()) {
            adj.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        for (Conexion c : adicionales) {
            adj.get(c.getOrigen()).add(c.getDestino());
            adj.get(c.getDestino()).add(c.getOrigen());
        }
        return estaConectado(adj, usuarios);
    }
}
