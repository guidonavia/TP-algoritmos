package org.uade.progra3.modelo;

import java.util.List;

public class Publicacion implements Comparable<Publicacion> {
    private final List<Like> likes;
    private final List<Comentario> comentarios;
    private final int tamanio;

    public Publicacion(List<Like> likes, List<Comentario> comentarios, int tamanio) {
        this.likes = likes;
        this.comentarios = comentarios;
        this.tamanio = tamanio;
    }

    public int ponderar() {
        return comentarios.size() * 10 + likes.size() * 2;
    }

    public int getTamanio() {
        return tamanio;
    }

    @Override
    public int compareTo(Publicacion p) {
        int valor = this.ponderar() / tamanio;
        int valorComparado = p.ponderar() / tamanio;
        return Integer.compare(valor, valorComparado);
    }
}
