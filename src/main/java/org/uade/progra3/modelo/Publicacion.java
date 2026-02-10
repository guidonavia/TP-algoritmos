package org.uade.progra3.modelo;

import java.util.List;

public class Publicacion {
    private List<Like> likes;
    private List<Comentario> comentarios;
    private int tamanio;

    public Publicacion(List<Like> likes, List<Comentario> comentarios, int tamanio) {
        this.likes = likes;
        this.comentarios = comentarios;
        this.tamanio = tamanio;
    }

    public double ponderar() {
        return comentarios.size() * 10 + likes.size() * 2;
    }

    public int getTamanio() {
        return tamanio;
    }
}
