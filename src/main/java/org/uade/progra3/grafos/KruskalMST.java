package org.uade.progra3.grafos;

import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.*;

public class KruskalMST {

    /** Complejidad temporal: E = aristas, V = vértices. */
    public static final String COMPLEJIDAD_TEMPORAL = "O(E log E)";

    public static Grafo arbolDeRecubrimientoMinimo(Grafo grafo) {
        Grafo resultado = new Grafo();
        int qtyUsuarios = 0;
        Map<Usuario, Usuario> indice = new HashMap<>();

        // Agregamos los usuarios al resultado final y a cada índice lo marcamos como su propio padre.
        // Con esto aislamos cada usuario.
        for(Usuario v: grafo.getUsuarios()) {
            resultado.agregarUsuario(v);
            indice.put(v, v);
            qtyUsuarios ++;
        }

        // Obtenemos todos los pesos y los ponemos en la cola de prioridad.
        PriorityQueue<Conexion> pq = new PriorityQueue<>();
        for (Usuario v1 : grafo.getUsuarios()) {
            for (Usuario v2 : grafo.getUsuarios()) {
                if (grafo.existeConexion(v1, v2) && !pq.contains(new Conexion(v1, v2, grafo.getPesoConexion(v1, v2)))) {
                    int peso = grafo.getPesoConexion(v1, v2);
                    pq.add(new Conexion(v1, v2, peso));
                }
            }
        }

        // Para cada Usuario, hacemos un recorrido
        while(qtyUsuarios > 1) {
            // Acá el algoritmo de Java agarra la menor y la saca de la cola de prioridad
            Conexion ConexionMasOptima = pq.poll();
            if(ConexionMasOptima == null) {
                continue;
            }
            if(origenYDestinoNotEquals(indice, ConexionMasOptima)) {
                // Acá unificamos componentes, sin agregar las conexiones aún
                qtyUsuarios --;
                Usuario origen = indice.get(ConexionMasOptima.getOrigen());
                Usuario destino = indice.get(ConexionMasOptima.getDestino());
                for(Usuario v : indice.keySet()) {
                    if (indice.get(v).equals(origen)) {
                        indice.put(v, destino);
                    }
                }
                // Finalmente agregamos la conexión al grafo saliente
                resultado.agregarConexion(ConexionMasOptima.getOrigen(),
                        ConexionMasOptima.getDestino(),
                        ConexionMasOptima.getPeso()
                );
            }
        }
        return resultado;
    }

    private static boolean origenYDestinoNotEquals( Map<Usuario, Usuario> indice, Conexion Conexion) {
        Usuario v1 = indice.get(Conexion.getOrigen());
        Usuario v2 = indice.get(Conexion.getDestino());

        return !v1.equals(v2);
    }
}