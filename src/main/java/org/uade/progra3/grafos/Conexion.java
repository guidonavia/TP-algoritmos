package org.uade.progra3.grafos;

public class Conexion implements Comparable<Conexion>{
    private Usuario origen;
    private Usuario destino;
    private int peso;

    public Conexion(Usuario origen, Usuario destino, int peso) {
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    public Usuario getOrigen() {
        return origen;
    }

    public Usuario getDestino() {
        return destino;
    }

    public int getPeso() {
        return peso;
    }

    @Override
    public String toString() {
        return origen + " --(" + peso + ")--> " + destino;
    }

    @Override
    public int compareTo(Conexion otra) {
        return Integer.compare(this.peso, otra.peso);  // ← Ordena por peso
    }
}