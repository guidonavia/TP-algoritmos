package org.uade.progra3.grafos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ConectividadBFS - Simulación de Bloqueos")
class ConectividadBFSTest {

    private Grafo grafo;
    private Usuario paula, silvia, oscar, eduardo, luis;

    @BeforeEach
    void setUp() {
        grafo = new Grafo();
        paula = new Usuario(1L, "Paula");
        silvia = new Usuario(2L, "Silvia");
        oscar = new Usuario(3L, "Oscar");
        eduardo = new Usuario(4L, "Eduardo");
        luis = new Usuario(5L, "Luis");
    }

    /**
     * Grafo lineal: Paula -> Silvia -> Oscar -> Eduardo -> Luis
     * Bloquear Silvia-Oscar divide el grafo en dos componentes.
     */
    private void grafoLineal() {
        grafo.agregarUsuario(paula);
        grafo.agregarUsuario(silvia);
        grafo.agregarUsuario(oscar);
        grafo.agregarUsuario(eduardo);
        grafo.agregarUsuario(luis);
        grafo.agregarConexion(paula, silvia, 1);
        grafo.agregarConexion(silvia, oscar, 2);
        grafo.agregarConexion(oscar, eduardo, 3);
        grafo.agregarConexion(eduardo, luis, 4);
    }

    /**
     * Grafo con ciclo: Paula -> Silvia -> Oscar -> Paula, Eduardo -> Oscar
     * Tiene caminos alternativos, más robusto ante bloqueos.
     */
    private void grafoConCiclo() {
        grafo.agregarUsuario(paula);
        grafo.agregarUsuario(silvia);
        grafo.agregarUsuario(oscar);
        grafo.agregarUsuario(eduardo);
        grafo.agregarConexion(paula, silvia, 1);
        grafo.agregarConexion(silvia, oscar, 2);
        grafo.agregarConexion(oscar, paula, 3);
        grafo.agregarConexion(eduardo, oscar, 4);
    }

    @Nested
    @DisplayName("simularBloqueo")
    class SimularBloqueo {

        @Test
        @DisplayName("bloqueo en grafo lineal desconecta el grafo en dos componentes")
        void bloqueoEnLinealDesconecta() {
            grafoLineal();
            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, silvia, oscar);

            assertFalse(resultado.esConexo(), "El grafo lineal debe desconectarse al bloquear Silvia-Oscar");
            assertEquals(2, resultado.getComponentes().size(), "Debe haber 2 componentes");
        }

        @Test
        @DisplayName("bloqueo en grafo con ciclo mantiene el grafo conexo")
        void bloqueoEnCicloMantieneConexo() {
            grafoConCiclo();
            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, paula, silvia);

            assertTrue(resultado.esConexo(), "El grafo con ciclo debe seguir conexo al bloquear Paula-Silvia");
            assertEquals(1, resultado.getComponentes().size());
            assertTrue(resultado.getConexionesRestauracion().isEmpty());
        }

        @Test
        @DisplayName("bloqueo de conexión inexistente no afecta la conectividad")
        void bloqueoConexionInexistenteNoAfecta() {
            grafoConCiclo();
            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, paula, eduardo);

            assertTrue(resultado.esConexo(), "Bloquear una conexión inexistente no debe desconectar");
        }

        @Test
        @DisplayName("las componentes contienen todos los usuarios")
        void componentesContienenTodosLosUsuarios() {
            grafoLineal();
            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, silvia, oscar);

            int totalUsuarios = resultado.getComponentes().stream()
                    .mapToInt(List::size).sum();
            assertEquals(5, totalUsuarios, "Todos los usuarios deben pertenecer a alguna componente");
        }

        @Test
        @DisplayName("se sugieren C-1 conexiones para restaurar C componentes")
        void restauracionSugiereConexionesCorrectas() {
            grafoLineal();
            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, silvia, oscar);

            int numComponentes = resultado.getComponentes().size();
            assertEquals(numComponentes - 1, resultado.getConexionesRestauracion().size(),
                    "Se necesitan C-1 conexiones para conectar C componentes");
        }

        @Test
        @DisplayName("grafo con dos nodos: bloquear la única conexión desconecta")
        void dosNodosBloquearUnicaConexion() {
            grafo.agregarUsuario(paula);
            grafo.agregarUsuario(silvia);
            grafo.agregarConexion(paula, silvia, 5);

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, paula, silvia);

            assertFalse(resultado.esConexo());
            assertEquals(2, resultado.getComponentes().size());
            assertEquals(1, resultado.getConexionesRestauracion().size());
        }

        @Test
        @DisplayName("grafo completamente conectado sigue conexo tras un bloqueo")
        void grafoCompletoSigueConexo() {
            grafo.agregarUsuario(paula);
            grafo.agregarUsuario(silvia);
            grafo.agregarUsuario(oscar);
            grafo.agregarConexion(paula, silvia, 1);
            grafo.agregarConexion(silvia, oscar, 2);
            grafo.agregarConexion(paula, oscar, 3);

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, paula, silvia);

            assertTrue(resultado.esConexo(), "Grafo completo debe seguir conexo al quitar una arista");
        }

        @Test
        @DisplayName("las conexiones de restauración conectan componentes distintas")
        void restauracionConectaComponentesDistintas() {
            grafoLineal();
            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, silvia, oscar);

            for (Conexion c : resultado.getConexionesRestauracion()) {
                // Verificar que origen y destino están en componentes diferentes
                int compOrigen = -1, compDestino = -1;
                List<List<Usuario>> comps = resultado.getComponentes();
                for (int i = 0; i < comps.size(); i++) {
                    if (comps.get(i).contains(c.getOrigen())) compOrigen = i;
                    if (comps.get(i).contains(c.getDestino())) compDestino = i;
                }
                assertTrue(compOrigen != compDestino,
                        "La conexión de restauración debe unir componentes distintas");
            }
        }
    }
}
