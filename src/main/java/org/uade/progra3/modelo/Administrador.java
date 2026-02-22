package org.uade.progra3.modelo;

public class Administrador {
    private final int id;
    private final String nombre;
    // eficiencias[grupoIndex] = valor de eficiencia 0..100 para ese grupo
    private final int[] eficiencias;

    public Administrador(int id, String nombre, int[] eficiencias) {
        this.id = id;
        this.nombre = nombre;
        this.eficiencias = eficiencias;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEficiencia(int grupoIndex) {
        return eficiencias[grupoIndex];
    }

    public int[] getEficiencias() {
        return eficiencias;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
