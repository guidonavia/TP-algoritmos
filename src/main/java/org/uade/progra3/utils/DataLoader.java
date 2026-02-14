package org.uade.progra3.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Publicacion;
import org.uade.progra3.grafos.Grafo;
import org.uade.progra3.modelo.Usuario;
import org.uade.progra3.modelo.Like;
import org.uade.progra3.modelo.Comentario;

public class DataLoader {

    private List<Usuario> usuarios;
    private List<Conexion> conexiones;
    private List<Publicacion> publicaciones;
    private Grafo grafo;

    public DataLoader(Grafo grafo) {
        this.usuarios = new ArrayList<>();
        this.conexiones = new ArrayList<>();
        this.publicaciones = new ArrayList<>();
        this.grafo = grafo;
    }

    /**
     * Carga los datos desde un archivo JSON ubicado en el classpath.
     * Ejemplo: si el archivo está en src/main/resources/usuarios-grafo.json,
     * se invoca con cargarDesdeRecurso("usuarios-grafo.json").
     */
    public void cargarDesdeRecurso(String nombreArchivo) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(nombreArchivo);
        if (is == null) {
            throw new RuntimeException("No se encontró el archivo: " + nombreArchivo);
        }
        try (Reader reader = new InputStreamReader(is)) {
            cargar(reader);
        } catch (Exception e) {
            throw new RuntimeException("Error al leer el recurso: " + nombreArchivo, e);
        }
    }

    private void cargar(Reader reader) {
        JSONObject root = new JSONObject(new JSONTokener(reader));

        // Parsear usuarios
        Map<Long, Usuario> mapaUsuarios = new HashMap<>();
        JSONArray jsonUsuarios = root.getJSONArray("usuarios");

        for (int i = 0; i < jsonUsuarios.length(); i++) {
            JSONObject obj = jsonUsuarios.getJSONObject(i);
            Long id = obj.getLong("id");
            String nombre = obj.getString("nombre");
            Usuario usuario = new Usuario(id, nombre);
            usuarios.add(usuario);
            grafo.agregarUsuario(usuario);
            mapaUsuarios.put(id, usuario);
        }

        // Parsear conexiones
        JSONArray jsonConexiones = root.getJSONArray("conexiones");

        for (int i = 0; i < jsonConexiones.length(); i++) {
            JSONObject obj = jsonConexiones.getJSONObject(i);
            Long origenId = obj.getLong("origen");
            Long destinoId = obj.getLong("destino");
            int peso = obj.getInt("peso");

            Usuario origen = mapaUsuarios.get(origenId);
            Usuario destino = mapaUsuarios.get(destinoId);

            if (origen == null || destino == null) {
                System.err.println("Conexión ignorada: origen=" + origenId + ", destino=" + destinoId);
                continue;
            }

            conexiones.add(new Conexion(origen, destino, peso));
            grafo.agregarConexion(origen, destino, peso);
        }

        // Parsear publicaciones (opcional: beneficio = comentarios*10 + likes*2, tamanio)
        if (root.has("publicaciones")) {
            JSONArray jsonPublicaciones = root.getJSONArray("publicaciones");
            for (int i = 0; i < jsonPublicaciones.length(); i++) {
                JSONObject obj = jsonPublicaciones.getJSONObject(i);
                int likes = obj.optInt("likes", 0);
                int comentarios = obj.optInt("comentarios", 0);
                int tamanio = obj.getInt("tamanio");
                publicaciones.add(crearPublicacion(likes, comentarios, tamanio));
            }
        }

        System.out.println("Datos cargados: " + usuarios.size() + " usuarios, " + conexiones.size() + " conexiones"
                + (publicaciones.isEmpty() ? "" : ", " + publicaciones.size() + " publicaciones") + ". \n");
    }

    private static Publicacion crearPublicacion(int cantidadLikes, int cantidadComentarios, int tamanio) {
        List<Like> likes = new ArrayList<>();
        for (int i = 0; i < cantidadLikes; i++) likes.add(new Like());
        List<Comentario> comentarios = new ArrayList<>();
        for (int i = 0; i < cantidadComentarios; i++) comentarios.add(new Comentario());
        return new Publicacion(likes, comentarios, tamanio);
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Conexion> getConexiones() {
        return conexiones;
    }

    public List<Publicacion> getPublicaciones() {
        return publicaciones;
    }
}
