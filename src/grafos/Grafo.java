package grafos;

import java.util.*;

public class Grafo {
    private Set<Usuario> usuarios;
    private List<Conexion> conexiones;
    private Map<Usuario, List<Conexion>> adyacencias;

    public Grafo() {
        usuarios = new HashSet<>();
        conexiones = new ArrayList<>();
        adyacencias = new HashMap<>();
    }

    public void agregarUsuario(Usuario v) {
        usuarios.add(v);
        adyacencias.putIfAbsent(v, new ArrayList<>());
    }

    public void agregarConexion(Usuario origen, Usuario destino, int peso) {
        Conexion conexion = new Conexion(origen, destino, peso);

        adyacencias.putIfAbsent(origen, new ArrayList<>());
        adyacencias.putIfAbsent(destino, new ArrayList<>());

        adyacencias.get(origen).add(conexion);
    }

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Conexion> getAdyacentes(Usuario v) {
        return adyacencias.getOrDefault(v, new ArrayList<>());
    }

    public boolean existeConexion(Usuario origen, Usuario destino) {
        if (!adyacencias.containsKey(origen)) return false;

        // Solo busco en la lista pequeña de vecinos de ESE origen
        for (Conexion a : adyacencias.get(origen)) {
            if (a.getDestino().equals(destino)) {
                return true;
            }
        }
        return false;
    }

    public Integer getPesoConexion(Usuario origen, Usuario destino) {
        if (!adyacencias.containsKey(origen)) return null;

        for (Conexion a : adyacencias.get(origen)) {
            if (a.getDestino().equals(destino)) {
                return a.getPeso();
            }
        }
        return null;
    }
}
