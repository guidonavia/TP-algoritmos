package org.uade.progra3.negocio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

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

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            int[] asig = resultado.getAsignacion();
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

            assertTrue(resultado.getCostoTotal() <= 13,
                    "El costo óptimo debe ser <= 13 (una cota superior conocida)");
        }

        @Test
        @DisplayName("caso 1x1: un grupo y un admin")
        void caso1x1() {
            int[][] costos = {{42}};
            String[] grupos = {"Único"};
            String[] admins = {"Solo"};

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

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

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

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

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            assertEquals(6, resultado.getCostoTotal());

            Set<Integer> usados = new HashSet<>();
            for (int a : resultado.getAsignacion()) usados.add(a);
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

            assertThrows(IllegalArgumentException.class,
                    () -> AsignacionAdminDP.asignar(costos, grupos, admins));
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

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            assertEquals(15, resultado.getCostoTotal(), "Costo uniforme: 5*3=15");
        }

        @Test
        @DisplayName("la asignación devuelta tiene nombres correctos")
        void asignacionTieneNombresCorrectos() {
            int[][] costos = {{3, 7}, {8, 2}};
            String[] grupos = {"Prog", "Arte"};
            String[] admins = {"Ana", "Pedro"};

            AsignacionAdminDP.ResultadoAsignacion resultado =
                    AsignacionAdminDP.asignar(costos, grupos, admins);

            assertArrayEquals(grupos, resultado.getNombresGrupos());
            assertArrayEquals(admins, resultado.getNombresAdmins());
        }
    }
}
