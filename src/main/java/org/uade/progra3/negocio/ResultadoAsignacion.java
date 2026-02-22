package org.uade.progra3.negocio;

import org.uade.progra3.modelo.Administrador;
import org.uade.progra3.modelo.Grupo;

import java.util.List;

public class ResultadoAsignacion {
    private final List<Grupo> grupos;
    private final List<Administrador> administradores;
    // asignacion[grupoIndex] = índice del administrador asignado a ese grupo
    private final int[] asignacion;
    private final int ineficienciaTotal;

    public ResultadoAsignacion(List<Grupo> grupos, List<Administrador> administradores,
                               int[] asignacion, int ineficienciaTotal) {
        this.grupos = grupos;
        this.administradores = administradores;
        this.asignacion = asignacion;
        this.ineficienciaTotal = ineficienciaTotal;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public List<Administrador> getAdministradores() {
        return administradores;
    }

    public int[] getAsignacion() {
        return asignacion;
    }

    public int getIneficienciaTotal() {
        return ineficienciaTotal;
    }

    public Administrador getAdminParaGrupo(int grupoIndex) {
        return administradores.get(asignacion[grupoIndex]);
    }

    public int getEficienciaParaGrupo(int grupoIndex) {
        return administradores.get(asignacion[grupoIndex]).getEficiencia(grupoIndex);
    }
}
