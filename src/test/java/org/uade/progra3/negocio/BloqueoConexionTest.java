package org.uade.progra3.negocio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.uade.progra3.grafos.Grafo;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BloqueoConexion")
class BloqueoConexionTest {

    private BloqueoConexion bloqueo;
    private Usuario a, b, c, d;

    @BeforeEach
    void setUp() {
        bloqueo = new BloqueoConexion();
        a = new Usuario(1L, "A");
        b = new Usuario(2L, "B");
        c = new Usuario(3L, "C");
        d = new Usuario(4L, "D");
    }

    @Nested
    @DisplayName("simularBloqueo - la red sigue conexa")
    class SigueConexo {

        @Test
        @DisplayName("bloquear una arista no puente deja la red conectada")
        void bloqueoNoPuente_sigueConectado() {
            // A → B, A → C, B → C (triángulo: bloquear A→B no desconecta)
            Grafo grafo = new Grafo();
            grafo.agregarUsuario(a); grafo.agregarUsuario(b); grafo.agregarUsuario(c);
            grafo.agregarConexion(a, b, 1);
            grafo.agregarConexion(a, c, 1);
            grafo.agregarConexion(b, c, 1);

            Conexion bloqueada = new Conexion(a, b, 1);
            ResultadoBloqueo resultado = bloqueo.simularBloqueo(grafo, bloqueada);

            assertTrue(resultado.isSigueConectado());
            assertTrue(resultado.getConexionesNecesarias().isEmpty());
        }

        @Test
        @DisplayName("bloquear en un ciclo de 4 nodos deja la red conectada")
        void bloqueoCiclo4_sigueConectado() {
            // A→B→C→D→A (ciclo): bloquear A→B aún permite A←D←C←B (en no-dirigido)
            Grafo grafo = new Grafo();
            grafo.agregarUsuario(a); grafo.agregarUsuario(b);
            grafo.agregarUsuario(c); grafo.agregarUsuario(d);
            grafo.agregarConexion(a, b, 1);
            grafo.agregarConexion(b, c, 1);
            grafo.agregarConexion(c, d, 1);
            grafo.agregarConexion(d, a, 1);

            ResultadoBloqueo resultado = bloqueo.simularBloqueo(grafo, new Conexion(a, b, 1));

            assertTrue(resultado.isSigueConectado());
        }
    }

    @Nested
    @DisplayName("simularBloqueo - la red queda desconectada")
    class QuedesConectado {

        @Test
        @DisplayName("bloquear el único puente desconecta la red")
        void bloqueoPuente_desconecta() {
            // A → B → C (cadena sin retorno): bloquear A→B aísla A
            Grafo grafo = new Grafo();
            grafo.agregarUsuario(a); grafo.agregarUsuario(b); grafo.agregarUsuario(c);
            grafo.agregarConexion(a, b, 1);
            grafo.agregarConexion(b, c, 1);

            ResultadoBloqueo resultado = bloqueo.simularBloqueo(grafo, new Conexion(a, b, 1));

            assertFalse(resultado.isSigueConectado());
        }

        @Test
        @DisplayName("cuando se desconecta, se necesita exactamente 1 conexión mínima")
        void bloqueoPuente_necesitaUnaConexion() {
            // A → B → C: bloquear A→B. Candidatas: {A-B, A-C}. Con A-B ya se reconecta.
            Grafo grafo = new Grafo();
            grafo.agregarUsuario(a); grafo.agregarUsuario(b); grafo.agregarUsuario(c);
            grafo.agregarConexion(a, b, 1);
            grafo.agregarConexion(b, c, 1);

            ResultadoBloqueo resultado = bloqueo.simularBloqueo(grafo, new Conexion(a, b, 1));

            assertEquals(1, resultado.getConexionesNecesarias().size());
        }

        @Test
        @DisplayName("la conexión propuesta restaura la conectividad")
        void conexionesNecesarias_restauranConectividad() {
            // D → A → B → C (cadena): bloquear D→A aísla D del resto
            Grafo grafo = new Grafo();
            grafo.agregarUsuario(a); grafo.agregarUsuario(b);
            grafo.agregarUsuario(c); grafo.agregarUsuario(d);
            grafo.agregarConexion(d, a, 1);
            grafo.agregarConexion(a, b, 1);
            grafo.agregarConexion(b, c, 1);

            ResultadoBloqueo resultado = bloqueo.simularBloqueo(grafo, new Conexion(d, a, 1));

            assertFalse(resultado.isSigueConectado());
            assertFalse(resultado.getConexionesNecesarias().isEmpty());

            // Verificar que las conexiones propuestas involucran a D (el nodo aislado)
            List<Conexion> nuevas = resultado.getConexionesNecesarias();
            boolean involucraDRooted = nuevas.stream().anyMatch(c ->
                    c.getOrigen().equals(d) || c.getDestino().equals(d));
            assertTrue(involucraDRooted, "Al menos una nueva conexión debe involucrar al nodo aislado");
        }

        @Test
        @DisplayName("grafo de dos nodos: bloquear la única conexión desconecta")
        void grafoDosNodos_bloqueado_desconecta() {
            Grafo grafo = new Grafo();
            grafo.agregarUsuario(a); grafo.agregarUsuario(b);
            grafo.agregarConexion(a, b, 5);

            ResultadoBloqueo resultado = bloqueo.simularBloqueo(grafo, new Conexion(a, b, 5));

            assertFalse(resultado.isSigueConectado());
            assertEquals(1, resultado.getConexionesNecesarias().size());
        }
    }
}
