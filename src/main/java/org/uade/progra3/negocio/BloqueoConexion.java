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
 *   2. BFS verifica si todos los nodos siguen siendo alcanzables entre sí.
 *   3. Si no: backtracking genera combinaciones de pares de usuarios no
 *      conectados, de tamaño creciente (1, 2, 3…), hasta hallar el subconjunto
 *      mínimo que restaura la conectividad. Se podan ramas en cuanto se
 *      confirma que ya se encontró una solución de ese tamaño.
 */
public class BloqueoConexion {

    /**
     * Simula el bloqueo de {@code conexionBloqueada} en {@code grafo}.
     *
     * @param grafo             grafo completo de la red social
     * @param conexionBloqueada conexión que se elimina (el usuario origen bloquea al destino)
     * @return resultado indicando si la red sigue conectada y, si no, qué
     *         conexiones mínimas hay que agregar para restablecerla
     */
    public ResultadoBloqueo simularBloqueo(Grafo grafo, Conexion conexionBloqueada) {
        Map<Usuario, Set<Usuario>> adj = construirAdjSinConexion(grafo, conexionBloqueada);

        if (estaConectado(adj, grafo.getUsuarios())) {
            return new ResultadoBloqueo(conexionBloqueada, true, Collections.emptyList());
        }

        List<Conexion> candidatas = generarCandidatas(grafo, conexionBloqueada);
        List<Conexion> minimas = buscarConexionesMinimas(adj, grafo.getUsuarios(), candidatas);

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
     * BFS sobre el grafo no dirigido representado por {@code adj}.
     * Devuelve {@code true} si todos los nodos son alcanzables desde uno cualquiera.
     */
    private boolean estaConectado(Map<Usuario, Set<Usuario>> adj, Set<Usuario> usuarios) {
        if (usuarios.isEmpty()) return true;
        Usuario inicio = usuarios.iterator().next();
        Set<Usuario> visitados = new HashSet<>();
        Queue<Usuario> cola = new LinkedList<>();
        cola.add(inicio);
        visitados.add(inicio);
        while (!cola.isEmpty()) {
            for (Usuario vecino : adj.get(cola.poll())) {
                if (visitados.add(vecino)) cola.add(vecino);
            }
        }
        return visitados.size() == usuarios.size();
    }

    /**
     * Genera todos los pares (u, v) con id(u) < id(v) que NO están conectados
     * en ninguna dirección en el grafo después del bloqueo. Estos son los
     * candidatos que el backtracking puede proponer como nuevas conexiones.
     */
    private List<Conexion> generarCandidatas(Grafo grafo, Conexion bloqueada) {
        List<Usuario> listaUsuarios = new ArrayList<>(grafo.getUsuarios());
        listaUsuarios.sort(Comparator.comparingLong(Usuario::getId));

        List<Conexion> candidatas = new ArrayList<>();
        for (int i = 0; i < listaUsuarios.size(); i++) {
            for (int j = i + 1; j < listaUsuarios.size(); j++) {
                Usuario u = listaUsuarios.get(i);
                Usuario v = listaUsuarios.get(j);

                boolean uToVExiste = grafo.existeConexion(u, v)
                        && !(u.equals(bloqueada.getOrigen()) && v.equals(bloqueada.getDestino()));
                boolean vToUExiste = grafo.existeConexion(v, u)
                        && !(v.equals(bloqueada.getOrigen()) && u.equals(bloqueada.getDestino()));

                if (!uToVExiste && !vToUExiste) {
                    candidatas.add(new Conexion(u, v, 1));
                }
            }
        }
        return candidatas;
    }

    /**
     * Backtracking: itera de tamaño 1 en adelante y devuelve el primer
     * subconjunto de candidatas que, sumado al grafo bloqueado, restaura
     * la conectividad.
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
     * Se poda en cuanto se completa una combinación exitosa.
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
     * conexiones adicionales propuestas. Crea una copia superficial del adj
     * para no modificar el original.
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
