package org.uade.progra3.servicio;

import org.uade.progra3.grafos.Djikstra;
import org.uade.progra3.grafos.Grafo;
import org.uade.progra3.grafos.KruskalMST;
import org.uade.progra3.modelo.CandidatoPublicaciones;
import org.uade.progra3.modelo.Portada;
import org.uade.progra3.modelo.Publicacion;
import org.uade.progra3.modelo.Usuario;
import org.uade.progra3.negocio.PortadaDinamica;
import org.uade.progra3.utils.DataLoader;

import java.util.List;
import java.util.Map;

/**
 * Orquesta los tres algoritmos del prototipo: Kruskal (red mínima), Dijkstra (recomendación)
 * y programación dinámica (portada óptima).
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
     * opcionalmente "publicaciones" con likes, comentarios, tamanio).
     */
    public void cargarDatos(String nombreRecurso) {
        dataLoader.cargarDesdeRecurso(nombreRecurso);
        redMinima = null;
    }

    public List<Usuario> getUsuarios() {
        return dataLoader.getUsuarios();
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
}
