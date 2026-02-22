package org.uade.progra3.servicio;

import org.uade.progra3.grafos.Djikstra;
import org.uade.progra3.grafos.Grafo;
import org.uade.progra3.grafos.KruskalMST;
import org.uade.progra3.modelo.Administrador;
import org.uade.progra3.modelo.CandidatoPublicaciones;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Grupo;
import org.uade.progra3.modelo.Portada;
import org.uade.progra3.modelo.Publicacion;
import org.uade.progra3.modelo.Usuario;
import org.uade.progra3.negocio.AsignacionAdministradores;
import org.uade.progra3.negocio.BloqueoConexion;
import org.uade.progra3.negocio.PortadaDinamica;
import org.uade.progra3.negocio.ResultadoAsignacion;
import org.uade.progra3.negocio.ResultadoBloqueo;
import org.uade.progra3.utils.DataLoader;

import java.util.List;
import java.util.Map;

/**
 * Orquesta los cinco algoritmos del prototipo:
 * Kruskal (red mínima), Dijkstra (recomendación), DP Knapsack (portada óptima),
 * Backtracking (bloqueo de conexiones) y DP Bitmask (asignación de administradores).
 */
public class RedSocialServicio {

    private final Grafo grafoCompleto;
    private Grafo redMinima;
    private final DataLoader dataLoader;
    private final PortadaDinamica portadaDinamica;
    private final Portada portada;
    private final BloqueoConexion bloqueoConexion;
    private final AsignacionAdministradores asignacionAdministradores;

    public RedSocialServicio() {
        this.grafoCompleto = new Grafo();
        this.dataLoader = new DataLoader(grafoCompleto);
        this.portadaDinamica = new PortadaDinamica();
        this.portada = new Portada();
        this.bloqueoConexion = new BloqueoConexion();
        this.asignacionAdministradores = new AsignacionAdministradores();
    }

    /**
     * Carga datos desde un JSON en resources (debe tener "usuarios" y "conexiones";
     * opcionalmente "publicaciones" con likes, comentarios, tamanio).
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

    /** Simula el bloqueo de una conexión y devuelve el resultado del análisis de conectividad. */
    public ResultadoBloqueo simularBloqueo(Conexion conexionBloqueada) {
        return bloqueoConexion.simularBloqueo(grafoCompleto, conexionBloqueada);
    }

    public List<Conexion> getConexiones() {
        return dataLoader.getConexiones();
    }

    /** Ejecuta el algoritmo de asignación óptima de administradores a grupos (DP bitmask). */
    public ResultadoAsignacion calcularAsignacionAdministradores() {
        return asignacionAdministradores.calcularAsignacionOptima(
                dataLoader.getGrupos(), dataLoader.getAdministradores());
    }

    public List<Grupo> getGrupos() {
        return dataLoader.getGrupos();
    }

    public List<Administrador> getAdministradores() {
        return dataLoader.getAdministradores();
    }
}
