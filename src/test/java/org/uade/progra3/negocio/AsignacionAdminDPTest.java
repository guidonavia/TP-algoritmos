package org.uade.progra3.negocio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AsignacionAdminDP - Asignación de Administradores con DP bitmask")
class AsignacionAdminDPTest {

    @Nested
    @DisplayName("asignar")
    class Asignar {

        @Test
        @DisplayName("caso 2x2: asignación óptima minimiza el costo total")
        void caso2x2Optimo() {
            int[][] costos = {
                    {5, 9},
                    {8, 2}
            };
            String[] grupos = {"G1", "G2"};
            String[] admins = {"A1", "A2"};

            System.out.println("=== Test: Caso 2x2 - Asignación óptima ===");
            System.out.println("Matriz de costos:");
            System.out.println("         A1  A2");
            System.out.println("  G1  [  5,  9 ]");
            System.out.println("  G2  [  8,  2 ]");

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            System.out.println("Asignación encontrada: " + Arrays.toString(resultado.getAsignacion()));
            System.out.println("  G1 <-- " + admins[resultado.getAsignacion()[0]] + " (costo " + costos[0][resultado.getAsignacion()[0]] + ")");
            System.out.println("  G2 <-- " + admins[resultado.getAsignacion()[1]] + " (costo " + costos[1][resultado.getAsignacion()[1]] + ")");
            System.out.println("Costo total: " + resultado.getCostoTotal());
            System.out.println("Esperado: 7 (A1→G1=5, A2→G2=2)");
            System.out.println("PASSED: Asignación óptima con costo mínimo.\n");

            assertEquals(7, resultado.getCostoTotal());
        }

        @Test
        @DisplayName("caso 4x4: encuentra asignación de costo mínimo")
        void caso4x4CostoMinimo() {
            int[][] costos = {
                    {9, 2, 7, 8},
                    {6, 4, 3, 7},
                    {5, 8, 1, 8},
                    {7, 6, 9, 4}
            };
            String[] grupos = {"Programación", "Arte Digital", "Ciencias", "Deportes"};
            String[] admins = {"Admin-Ana", "Admin-Pedro", "Admin-Maria", "Admin-Jorge"};

            System.out.println("=== Test: Caso 4x4 - Costo mínimo ===");
            System.out.println("Matriz de costos:");
            System.out.println("                   Ana  Pedro  Maria  Jorge");
            System.out.println("  Programación  [   9,     2,     7,     8 ]");
            System.out.println("  Arte Digital  [   6,     4,     3,     7 ]");
            System.out.println("  Ciencias      [   5,     8,     1,     8 ]");
            System.out.println("  Deportes      [   7,     6,     9,     4 ]");

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            int[] asig = resultado.getAsignacion();
            System.out.println("Asignación encontrada:");
            for (int i = 0; i < asig.length; i++) {
                System.out.println("  " + grupos[i] + " <-- " + admins[asig[i]] + " (costo " + costos[i][asig[i]] + ")");
            }
            System.out.println("Costo total: " + resultado.getCostoTotal());

            assertEquals(4, asig.length);
            Set<Integer> adminsUsados = new HashSet<>();
            for (int a : asig) {
                assertTrue(a >= 0 && a < 4, "Índice de admin válido");
                adminsUsados.add(a);
            }
            assertEquals(4, adminsUsados.size(), "Cada admin debe ser asignado exactamente una vez");

            int costoVerificado = 0;
            for (int i = 0; i < asig.length; i++) {
                costoVerificado += costos[i][asig[i]];
            }
            assertEquals(resultado.getCostoTotal(), costoVerificado);

            System.out.println("Admins distintos usados: " + adminsUsados.size() + " (todos)");
            System.out.println("Costo verificado (suma real): " + costoVerificado);
            System.out.println("Cota superior conocida: 13");
            assertTrue(resultado.getCostoTotal() <= 13,
                    "El costo óptimo debe ser <= 13 (una cota superior conocida)");
            System.out.println("PASSED: Costo óptimo <= 13, asignación válida.\n");
        }

        @Test
        @DisplayName("caso 1x1: un grupo y un admin")
        void caso1x1() {
            int[][] costos = {{42}};
            String[] grupos = {"Único"};
            String[] admins = {"Solo"};

            System.out.println("=== Test: Caso 1x1 - Un grupo, un admin ===");
            System.out.println("Matriz de costos: [[ 42 ]]");
            System.out.println("Grupo: \"Único\", Admin: \"Solo\"");

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            System.out.println("Costo total: " + resultado.getCostoTotal() + " (esperado: 42)");
            System.out.println("Asignación: " + Arrays.toString(resultado.getAsignacion()) + " (esperado: [0])");
            System.out.println("PASSED: Caso trivial resuelto correctamente.\n");

            assertEquals(42, resultado.getCostoTotal());
            assertArrayEquals(new int[]{0}, resultado.getAsignacion());
        }

        @Test
        @DisplayName("caso 3x3: asignación identidad es óptima cuando diagonal tiene costos mínimos")
        void casoDiagonalOptima() {
            int[][] costos = {
                    {1, 10, 10},
                    {10, 1, 10},
                    {10, 10, 1}
            };
            String[] grupos = {"G1", "G2", "G3"};
            String[] admins = {"A1", "A2", "A3"};

            System.out.println("=== Test: Caso 3x3 - Diagonal óptima ===");
            System.out.println("Matriz de costos:");
            System.out.println("       A1  A2  A3");
            System.out.println("  G1 [  1, 10, 10 ]");
            System.out.println("  G2 [ 10,  1, 10 ]");
            System.out.println("  G3 [ 10, 10,  1 ]");

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            System.out.println("Asignación: " + Arrays.toString(resultado.getAsignacion()) + " (esperado: [0, 1, 2])");
            System.out.println("Costo total: " + resultado.getCostoTotal() + " (esperado: 3 = 1+1+1)");
            System.out.println("PASSED: Se eligió la diagonal, asignación identidad (admin i al grupo i).\n");

            assertEquals(3, resultado.getCostoTotal(), "Diagonal óptima: 1+1+1=3");
            assertArrayEquals(new int[]{0, 1, 2}, resultado.getAsignacion(),
                    "Asignación identidad: admin i al grupo i");
        }

        @Test
        @DisplayName("más admins que grupos: selecciona el subconjunto óptimo")
        void masAdminsQueGrupos() {
            int[][] costos = {
                    {10, 5, 8},
                    {6, 3, 1}
            };
            String[] grupos = {"G1", "G2"};
            String[] admins = {"A1", "A2", "A3"};

            System.out.println("=== Test: Más admins que grupos ===");
            System.out.println("Matriz de costos (2 grupos, 3 admins):");
            System.out.println("       A1  A2  A3");
            System.out.println("  G1 [ 10,  5,  8 ]");
            System.out.println("  G2 [  6,  3,  1 ]");

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            int[] asig = resultado.getAsignacion();
            System.out.println("Asignación encontrada:");
            for (int i = 0; i < asig.length; i++) {
                System.out.println("  " + grupos[i] + " <-- " + admins[asig[i]] + " (costo " + costos[i][asig[i]] + ")");
            }
            System.out.println("Costo total: " + resultado.getCostoTotal() + " (esperado: 6 = A2→G1=5, A3→G2=1)");

            Set<Integer> usados = new HashSet<>();
            for (int a : resultado.getAsignacion()) usados.add(a);
            System.out.println("Admins distintos usados: " + usados.size() + " de 3 disponibles");
            System.out.println("PASSED: Se seleccionó el subconjunto óptimo de admins.\n");

            assertEquals(6, resultado.getCostoTotal());
            assertEquals(2, usados.size());
        }

        @Test
        @DisplayName("lanza excepción si hay menos admins que grupos")
        void menosAdminsQueGruposLanzaExcepcion() {
            int[][] costos = {
                    {1, 2},
                    {3, 4},
                    {5, 6}
            };
            String[] grupos = {"G1", "G2", "G3"};
            String[] admins = {"A1", "A2"};

            System.out.println("=== Test: Menos admins que grupos (excepción) ===");
            System.out.println("3 grupos pero solo 2 admins: imposible asignar.");

            assertThrows(IllegalArgumentException.class,
                    () -> AsignacionAdminDP.asignar(costos, grupos, admins));

            System.out.println("Resultado: IllegalArgumentException lanzada correctamente.");
            System.out.println("PASSED: Se rechaza el caso inválido con la excepción esperada.\n");
        }

        @Test
        @DisplayName("costos uniformes: cualquier asignación es óptima")
        void costosUniformes() {
            int[][] costos = {
                    {5, 5, 5},
                    {5, 5, 5},
                    {5, 5, 5}
            };
            String[] grupos = {"G1", "G2", "G3"};
            String[] admins = {"A1", "A2", "A3"};

            System.out.println("=== Test: Costos uniformes ===");
            System.out.println("Todos los costos son 5. Cualquier asignación vale lo mismo.");

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            System.out.println("Asignación: " + Arrays.toString(resultado.getAsignacion()));
            System.out.println("Costo total: " + resultado.getCostoTotal() + " (esperado: 15 = 5*3)");
            System.out.println("PASSED: Costo uniforme calculado correctamente.\n");

            assertEquals(15, resultado.getCostoTotal(), "Costo uniforme: 5*3=15");
        }

        @Test
        @DisplayName("la asignación devuelta tiene nombres correctos")
        void asignacionTieneNombresCorrectos() {
            int[][] costos = {{3, 7}, {8, 2}};
            String[] grupos = {"Prog", "Arte"};
            String[] admins = {"Ana", "Pedro"};

            System.out.println("=== Test: Nombres correctos en el resultado ===");

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            System.out.println("Nombres de grupos devueltos: " + Arrays.toString(resultado.getNombresGrupos()));
            System.out.println("Nombres de admins devueltos: " + Arrays.toString(resultado.getNombresAdmins()));
            System.out.println("Esperados grupos: [Prog, Arte]");
            System.out.println("Esperados admins: [Ana, Pedro]");
            System.out.println("PASSED: Los nombres se preservan correctamente en el resultado.\n");

            assertArrayEquals(grupos, resultado.getNombresGrupos());
            assertArrayEquals(admins, resultado.getNombresAdmins());
        }
    }
}
