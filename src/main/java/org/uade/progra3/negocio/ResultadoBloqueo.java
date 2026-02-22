package org.uade.progra3.negocio;

import org.uade.progra3.modelo.Conexion;

import java.util.List;

public class ResultadoBloqueo {
    private final Conexion conexionBloqueada;
    private final boolean sigueConectado;
    // Vacío si sigueConectado = true; de lo contrario, el conjunto mínimo a agregar.
    private final List<Conexion> conexionesNecesarias;

    public ResultadoBloqueo(Conexion conexionBloqueada, boolean sigueConectado, List<Conexion> conexionesNecesarias) {
        this.conexionBloqueada = conexionBloqueada;
        this.sigueConectado = sigueConectado;
        this.conexionesNecesarias = conexionesNecesarias;
    }

    public Conexion getConexionBloqueada() {
        return conexionBloqueada;
    }

    public boolean isSigueConectado() {
        return sigueConectado;
    }

    public List<Conexion> getConexionesNecesarias() {
        return conexionesNecesarias;
    }
}
