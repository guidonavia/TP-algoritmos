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

    public Set<Usuario> getusuarios() {
        return usuarios;
    }

    public List<Conexion> getAdyacentes(Usuario v) {
        return adyacencias.getOrDefault(v, new ArrayList<>());
    }
}
