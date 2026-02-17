package org.uade.progra3.modelo;

public class Conexion implements Comparable<Conexion> {
    private final Usuario origen;
    private final Usuario destino;
    private final int peso;

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
        return Integer.compare(this.peso, otra.peso);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Conexion conexion = (Conexion) o;
        return peso == conexion.peso && java.util.Objects.equals(origen, conexion.origen)
                && java.util.Objects.equals(destino, conexion.destino);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(origen, destino, peso);
    }
}