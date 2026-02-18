package org.uade.progra3.grafos;

import org.uade.progra3.modelo.Usuario;

public class NodoDistancia implements Comparable<NodoDistancia> {
    private final Usuario usuario;
    private final int distancia;

    public NodoDistancia(Usuario usuario, int distancia) {
        this.usuario = usuario;
        this.distancia = distancia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public int getDistancia() {
        return distancia;
    }

    @Override
    public int compareTo(NodoDistancia o) {
        return Integer.compare(this.distancia, o.distancia);
    }
}
