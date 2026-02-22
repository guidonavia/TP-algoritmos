package org.uade.progra3.negocio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.uade.progra3.modelo.Administrador;
import org.uade.progra3.modelo.Grupo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsignacionAdministradores")
class AsignacionAdministradoresTest {

    private AsignacionAdministradores asignacion;

    @BeforeEach
    void setUp() {
        asignacion = new AsignacionAdministradores();
    }

    /** Helper: crea un Grupo con id y nombre. */
    private static Grupo grupo(int id, String nombre) {
        return new Grupo(id, nombre);
    }

    /** Helper: crea un Administrador con id, nombre y sus eficiencias por grupo. */
    private static Administrador admin(int id, String nombre, int... eficiencias) {
        return new Administrador(id, nombre, eficiencias);
    }

    @Nested
    @DisplayName("caso 2x2")
    class Caso2x2 {

        @Test
        @DisplayName("asigna el admin de mayor eficiencia a cada grupo")
        void dosGrupos_asignacionOptima() {
            // Torres: Prog=90, Arte=60 → inef: 10, 40
            // Vera:   Prog=50, Arte=85 → inef: 50, 15
            // Óptimo: Torres→Prog(10) + Vera→Arte(15) = 25
            List<Grupo> grupos = List.of(grupo(1, "Prog"), grupo(2, "Arte"));
            List<Administrador> admins = List.of(
                    admin(1, "Torres", 90, 60),
                    admin(2, "Vera",   50, 85));

            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);

            assertEquals(25, resultado.getIneficienciaTotal());
            assertEquals("Torres", resultado.getAdminParaGrupo(0).getNombre());
            assertEquals("Vera",   resultado.getAdminParaGrupo(1).getNombre());
        }

        @Test
        @DisplayName("cuando los admins están cruzados, elige el cruce óptimo")
        void dosGrupos_cruceOptimo() {
            // A: Prog=40, Arte=80 → inef: 60, 20
            // B: Prog=70, Arte=30 → inef: 30, 70
            // Óptimo: B→Prog(30) + A→Arte(20) = 50
            List<Grupo> grupos = List.of(grupo(1, "Prog"), grupo(2, "Arte"));
            List<Administrador> admins = List.of(
                    admin(1, "A", 40, 80),
                    admin(2, "B", 70, 30));

            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);

            assertEquals(50, resultado.getIneficienciaTotal());
            assertEquals("B", resultado.getAdminParaGrupo(0).getNombre());
            assertEquals("A", resultado.getAdminParaGrupo(1).getNombre());
        }
    }

    @Nested
    @DisplayName("caso 3x3 (datos del demo)")
    class Caso3x3 {

        // Torres: Prog=90, Arte=60, Ciencias=70 → inef: 10, 40, 30
        // Vera:   Prog=50, Arte=85, Ciencias=65 → inef: 50, 15, 35
        // Cruz:   Prog=70, Arte=55, Ciencias=95 → inef: 30, 45,  5
        // Óptimo: Torres→Prog(10) + Vera→Arte(15) + Cruz→Ciencias(5) = 30

        private List<Grupo> grupos;
        private List<Administrador> admins;

        @BeforeEach
        void setUp3x3() {
            grupos = List.of(grupo(1, "Programacion"), grupo(2, "Arte"), grupo(3, "Ciencias"));
            admins = List.of(
                    admin(1, "Torres", 90, 60, 70),
                    admin(2, "Vera",   50, 85, 65),
                    admin(3, "Cruz",   70, 55, 95));
        }

        @Test
        @DisplayName("la ineficiencia total mínima es 30")
        void ineficienciaTotalMinima() {
            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);
            assertEquals(30, resultado.getIneficienciaTotal());
        }

        @Test
        @DisplayName("Torres se asigna a Programacion")
        void torresAPrograma() {
            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);
            assertEquals("Torres", resultado.getAdminParaGrupo(0).getNombre());
        }

        @Test
        @DisplayName("Vera se asigna a Arte")
        void veraAAnte() {
            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);
            assertEquals("Vera", resultado.getAdminParaGrupo(1).getNombre());
        }

        @Test
        @DisplayName("Cruz se asigna a Ciencias")
        void cruzACiencias() {
            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);
            assertEquals("Cruz", resultado.getAdminParaGrupo(2).getNombre());
        }

        @Test
        @DisplayName("cada admin aparece asignado exactamente una vez")
        void cadaAdminUnaVez() {
            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);
            int[] asig = resultado.getAsignacion();
            // Los índices 0,1,2 deben aparecer cada uno exactamente una vez
            boolean[] usado = new boolean[3];
            for (int idx : asig) {
                assertFalse(usado[idx], "Admin " + idx + " asignado más de una vez");
                usado[idx] = true;
            }
        }
    }

    @Nested
    @DisplayName("casos límite")
    class CasosLimite {

        @Test
        @DisplayName("un solo grupo y un admin: ineficiencia = 100 - eficiencia")
        void unGrupoUnAdmin() {
            List<Grupo> grupos = List.of(grupo(1, "G1"));
            List<Administrador> admins = List.of(admin(1, "Admin", 75));

            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);

            assertEquals(25, resultado.getIneficienciaTotal());
            assertEquals("Admin", resultado.getAdminParaGrupo(0).getNombre());
        }

        @Test
        @DisplayName("eficiencias iguales: ineficiencia total es n * (100 - eficiencia)")
        void eficienciasIguales() {
            // Todos con eficiencia 80: ineficiencia = 20 por grupo × 2 grupos = 40
            List<Grupo> grupos = List.of(grupo(1, "G1"), grupo(2, "G2"));
            List<Administrador> admins = List.of(
                    admin(1, "X", 80, 80),
                    admin(2, "Y", 80, 80));

            ResultadoAsignacion resultado = asignacion.calcularAsignacionOptima(grupos, admins);

            assertEquals(40, resultado.getIneficienciaTotal());
        }
    }
}
