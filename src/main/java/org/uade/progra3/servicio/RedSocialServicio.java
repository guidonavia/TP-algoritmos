package org.uade.progra3.servicio;

import org.uade.progra3.grafos.ConectividadBFS;
import org.uade.progra3.grafos.Djikstra;
import org.uade.progra3.grafos.Grafo;
import org.uade.progra3.grafos.KruskalMST;
import org.uade.progra3.modelo.Administrador;
import org.uade.progra3.modelo.CandidatoPublicaciones;
import org.uade.progra3.modelo.Grupo;
import org.uade.progra3.modelo.Portada;
import org.uade.progra3.modelo.Publicacion;
import org.uade.progra3.modelo.Usuario;
import org.uade.progra3.negocio.AsignacionAdminDP;
import org.uade.progra3.negocio.PortadaDinamica;
import org.uade.progra3.utils.DataLoader;

import java.util.List;
import java.util.Map;

/**
 * Orquesta los algoritmos del prototipo: Kruskal (red mínima), Dijkstra (recomendación),
 * programación dinámica (portada óptima), BFS (simulación de bloqueo) y DP bitmask
 * (asignación de administradores).
 */
public class RedSocialServicio {

    private final Grafo grafoCompleto;
    private Grafo redMinima;
    private final DataLoader dataLoader;
    private final PortadaDinamica portadaDinamica;
    private final Portada portada;

    public RedSocialServicio() {
        this.grafoCompleto = new Grafo();
        this.dataLoader = new DataLoader(grafoCompleto);
        this.portadaDinamica = new PortadaDinamica();
        this.portada = new Portada();
    }

    /**
     * Carga datos desde un JSON en resources (debe tener "usuarios" y "conexiones";
     * opcionalmente "publicaciones", "grupos", "administradores", "costoAsignacion").
     */
    public void cargarDatos(String nombreRecurso) {
        dataLoader.cargarDesdeRecurso(nombreRecurso);
        redMinima = null;
    }

    public List<Usuario> getUsuarios() {
        return dataLoader.getUsuarios();
    }

    public Grafo getGrafoCompleto() {
        return grafoCompleto;
    }

    /** Ejecuta Kruskal y devuelve el grafo de la red mínima (MST). */
    public Grafo calcularRedMinima() {
        redMinima = KruskalMST.arbolDeRecubrimientoMinimo(grafoCompleto);
        return redMinima;
    }

    public Grafo getRedMinima() {
        return redMinima;
    }

    /** Ejecuta Dijkstra desde el usuario dado. Devuelve mapa usuario -> distancia mínima. */
    public Map<Usuario, Integer> calcularDistanciasDesde(Usuario origen) {
        return Djikstra.calcularCaminosMinimos(grafoCompleto, origen);
    }

    /** Ejecuta el algoritmo de portada óptima (DP) y deja el resultado en la portada. */
    public void calcularPortadaOptima() {
        CandidatoPublicaciones candidatos = new CandidatoPublicaciones(dataLoader.getPublicaciones());
        portadaDinamica.obtenerPublicaciones(candidatos, portada);
    }

    public List<Publicacion> getPublicacionesCandidatas() {
        return dataLoader.getPublicaciones();
    }

    public List<Publicacion> getPortadaOptima() {
        return portada.getPublicaciones();
    }

    public int getCapacidadPortada() {
        return Portada.getTamanioMaximo();
    }

    // --- Problema Opcional 1: Simulación de Bloqueos (BFS) ---

    /**
     * Simula el bloqueo entre dos usuarios y devuelve el resultado de conectividad.
     */
    public ConectividadBFS.ResultadoBloqueo simularBloqueo(Usuario bloqueador, Usuario bloqueado) {
        return ConectividadBFS.simularBloqueo(grafoCompleto, bloqueador, bloqueado);
    }

    // --- Problema Opcional 3: Asignación de Administradores (DP bitmask) ---

    public List<Grupo> getGrupos() {
        return dataLoader.getGrupos();
    }

    public List<Administrador> getAdministradores() {
        return dataLoader.getAdministradores();
    }

    public int[][] getCostoAsignacion() {
        return dataLoader.getCostoAsignacion();
    }

    /**
     * Calcula la asignación óptima de administradores a grupos usando DP bitmask.
     */
    public AsignacionAdminDP.ResultadoAsignacion calcularAsignacionOptima() {
        List<Grupo> grupos = dataLoader.getGrupos();
        List<Administrador> admins = dataLoader.getAdministradores();
        int[][] costos = dataLoader.getCostoAsignacion();

        if (grupos.isEmpty() || admins.isEmpty() || costos == null) {
            throw new IllegalStateException("No hay datos de grupos/administradores cargados");
        }

        String[] nombresGrupos = grupos.stream().map(Grupo::getNombre).toArray(String[]::new);
        String[] nombresAdmins = admins.stream().map(Administrador::getNombre).toArray(String[]::new);

        return AsignacionAdminDP.asignar(costos, nombresGrupos, nombresAdmins);
    }
}
