package org.uade.progra3.grafos;

import java.util.*;

public class KruskalMST {


    public static Grafo arbolDeRecubrimientoMinimo(Grafo grafo) {
        Grafo resultado = new Grafo();
        int qtyUsuarios = 0;
        Map<Usuario, Usuario> indice = new HashMap<>();

        // Agregamos los Usuarios al resultado final y a cada indice lo marcamos como su propio parent
        // Con esto aislamos cada Usuario.
        for(Usuario v: grafo.getUsuarios()) {
            resultado.agregarUsuario(v);
            indice.put(v, v);
            qtyUsuarios ++;
        }

        // Obtenemos todos los pesos y los ponemos en la PQ.
        PriorityQueue<Conexion> pq = new PriorityQueue<>();
        for(Usuario v1 : grafo.getUsuarios()) {
            for(Usuario v2 : grafo.getUsuarios()) {
                if(grafo.existeConexion(v1, v2) && !resultado.existeConexion(v1, v2)) {
                    int peso = grafo.getPesoConexion(v1, v2);
                    pq.add(new Conexion(v1, v2, peso));
                }
            }
        }

        // Para cada Usuario, hacemos un recorrido
        while(qtyUsuarios > 1) {
            //Aca el algoritmo de java agarra la menor y la saca de la Queue
            Conexion ConexionMasOptima = pq.poll();
            if(ConexionMasOptima == null) {
                continue;
            }
            if(origenYDestinoNotEquals(indice, ConexionMasOptima)) {
                // Aca unificamos componentes, sin agregar las Conexions aun
                qtyUsuarios --;
                Usuario origen = indice.get(ConexionMasOptima.getOrigen());
                Usuario destino = indice.get(ConexionMasOptima.getDestino());
                for(Usuario v : indice.keySet()) {
                    if (indice.get(v).equals(origen)) {
                        indice.put(v, destino);
                    }
                }
                // Finalmente agregamos el Conexion al grafo saliente
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