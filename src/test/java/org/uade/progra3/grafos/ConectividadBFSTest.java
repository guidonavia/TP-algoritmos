package org.uade.progra3.grafos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.List;
import java.util.stream.Collectors;

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

    private static String nombresUsuarios(List<Usuario> usuarios) {
        return usuarios.stream().map(Usuario::getNombre).collect(Collectors.joining(", "));
    }

    @Nested
    @DisplayName("simularBloqueo")
    class SimularBloqueo {

        @Test
        @DisplayName("bloqueo en grafo lineal desconecta el grafo en dos componentes")
        void bloqueoEnLinealDesconecta() {
            grafoLineal();
            System.out.println("=== Test: Bloqueo en grafo lineal ===");
            System.out.println("Grafo: Paula --1--> Silvia --2--> Oscar --3--> Eduardo --4--> Luis");
            System.out.println("Bloqueando arista: Silvia - Oscar");

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, silvia, oscar);

            System.out.println("Resultado: esConexo = " + resultado.esConexo());
            System.out.println("Componentes encontradas: " + resultado.getComponentes().size());
            for (int i = 0; i < resultado.getComponentes().size(); i++) {
                System.out.println("  Componente " + (i + 1) + ": [" + nombresUsuarios(resultado.getComponentes().get(i)) + "]");
            }
            System.out.println("PASSED: El grafo se desconectó correctamente en 2 componentes.\n");

            assertFalse(resultado.esConexo(), "El grafo lineal debe desconectarse al bloquear Silvia-Oscar");
            assertEquals(2, resultado.getComponentes().size(), "Debe haber 2 componentes");
        }

        @Test
        @DisplayName("bloqueo en grafo con ciclo mantiene el grafo conexo")
        void bloqueoEnCicloMantieneConexo() {
            grafoConCiclo();
            System.out.println("=== Test: Bloqueo en grafo con ciclo ===");
            System.out.println("Grafo: Paula ---> Silvia ---> Oscar ---> Paula, Eduardo ---> Oscar");
            System.out.println("Bloqueando arista: Paula - Silvia");

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, paula, silvia);

            System.out.println("Resultado: esConexo = " + resultado.esConexo());
            System.out.println("Componentes encontradas: " + resultado.getComponentes().size());
            System.out.println("Conexiones de restauración sugeridas: " + resultado.getConexionesRestauracion().size());
            System.out.println("PASSED: El grafo sigue conexo gracias al camino alternativo por el ciclo.\n");

            assertTrue(resultado.esConexo(), "El grafo con ciclo debe seguir conexo al bloquear Paula-Silvia");
            assertEquals(1, resultado.getComponentes().size());
            assertTrue(resultado.getConexionesRestauracion().isEmpty());
        }

        @Test
        @DisplayName("bloqueo de conexión inexistente no afecta la conectividad")
        void bloqueoConexionInexistenteNoAfecta() {
            grafoConCiclo();
            System.out.println("=== Test: Bloqueo de conexión inexistente ===");
            System.out.println("Grafo con ciclo (4 nodos). Bloqueando arista inexistente: Paula - Eduardo");

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, paula, eduardo);

            System.out.println("Resultado: esConexo = " + resultado.esConexo());
            System.out.println("PASSED: Bloquear una arista inexistente no afecta la conectividad.\n");

            assertTrue(resultado.esConexo(), "Bloquear una conexión inexistente no debe desconectar");
        }

        @Test
        @DisplayName("las componentes contienen todos los usuarios")
        void componentesContienenTodosLosUsuarios() {
            grafoLineal();
            System.out.println("=== Test: Componentes contienen todos los usuarios ===");
            System.out.println("Grafo lineal (5 nodos). Bloqueando Silvia - Oscar.");

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, silvia, oscar);

            int totalUsuarios = resultado.getComponentes().stream()
                    .mapToInt(List::size).sum();

            System.out.println("Total de usuarios en las componentes: " + totalUsuarios);
            System.out.println("Total de usuarios en el grafo: 5");
            System.out.println("PASSED: Ningún usuario se perdió tras el bloqueo.\n");

            assertEquals(5, totalUsuarios, "Todos los usuarios deben pertenecer a alguna componente");
        }

        @Test
        @DisplayName("se sugieren C-1 conexiones para restaurar C componentes")
        void restauracionSugiereConexionesCorrectas() {
            grafoLineal();
            System.out.println("=== Test: Cantidad de conexiones de restauración ===");
            System.out.println("Grafo lineal (5 nodos). Bloqueando Silvia - Oscar.");

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, silvia, oscar);

            int numComponentes = resultado.getComponentes().size();
            int conexionesRestauracion = resultado.getConexionesRestauracion().size();

            System.out.println("Componentes: " + numComponentes);
            System.out.println("Conexiones de restauración sugeridas: " + conexionesRestauracion);
            System.out.println("Esperado (C-1): " + (numComponentes - 1));
            for (Conexion c : resultado.getConexionesRestauracion()) {
                System.out.println("  Sugerencia: " + c.getOrigen().getNombre() + " <--> " + c.getDestino().getNombre());
            }
            System.out.println("PASSED: Se sugieren exactamente C-1 conexiones.\n");

            assertEquals(numComponentes - 1, resultado.getConexionesRestauracion().size(),
                    "Se necesitan C-1 conexiones para conectar C componentes");
        }

        @Test
        @DisplayName("grafo con dos nodos: bloquear la única conexión desconecta")
        void dosNodosBloquearUnicaConexion() {
            grafo.agregarUsuario(paula);
            grafo.agregarUsuario(silvia);
            grafo.agregarConexion(paula, silvia, 5);

            System.out.println("=== Test: Dos nodos, bloquear la única conexión ===");
            System.out.println("Grafo: Paula --5--> Silvia. Bloqueando Paula - Silvia.");

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, paula, silvia);

            System.out.println("Resultado: esConexo = " + resultado.esConexo());
            System.out.println("Componentes: " + resultado.getComponentes().size());
            System.out.println("Conexiones de restauración: " + resultado.getConexionesRestauracion().size());
            System.out.println("PASSED: Bloquear la única arista desconecta el grafo y sugiere 1 reconexión.\n");

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

            System.out.println("=== Test: Grafo completo sigue conexo ===");
            System.out.println("Grafo completo: Paula-Silvia, Silvia-Oscar, Paula-Oscar.");
            System.out.println("Bloqueando: Paula - Silvia.");

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, paula, silvia);

            System.out.println("Resultado: esConexo = " + resultado.esConexo());
            System.out.println("PASSED: El grafo completo sigue conexo por redundancia de caminos.\n");

            assertTrue(resultado.esConexo(), "Grafo completo debe seguir conexo al quitar una arista");
        }

        @Test
        @DisplayName("las conexiones de restauración conectan componentes distintas")
        void restauracionConectaComponentesDistintas() {
            grafoLineal();
            System.out.println("=== Test: Restauración conecta componentes distintas ===");
            System.out.println("Grafo lineal (5 nodos). Bloqueando Silvia - Oscar.");

            ConectividadBFS.ResultadoBloqueo resultado =
                    ConectividadBFS.simularBloqueo(grafo, silvia, oscar);

            System.out.println("Componentes:");
            for (int i = 0; i < resultado.getComponentes().size(); i++) {
                System.out.println("  Componente " + (i + 1) + ": [" + nombresUsuarios(resultado.getComponentes().get(i)) + "]");
            }

            for (Conexion c : resultado.getConexionesRestauracion()) {
                int compOrigen = -1, compDestino = -1;
                List<List<Usuario>> comps = resultado.getComponentes();
                for (int i = 0; i < comps.size(); i++) {
                    if (comps.get(i).contains(c.getOrigen())) compOrigen = i;
                    if (comps.get(i).contains(c.getDestino())) compDestino = i;
                }
                System.out.println("Conexión sugerida: " + c.getOrigen().getNombre() + " (comp " + (compOrigen + 1) + ") <--> " + c.getDestino().getNombre() + " (comp " + (compDestino + 1) + ")");
                assertTrue(compOrigen != compDestino,
                        "La conexión de restauración debe unir componentes distintas");
            }
            System.out.println("PASSED: Cada conexión de restauración une componentes distintas.\n");
        }
    }
}
