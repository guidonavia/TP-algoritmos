package org.uade.progra3.negocio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.uade.progra3.modelo.Comentario;
import org.uade.progra3.modelo.CandidatoPublicaciones;
import org.uade.progra3.modelo.Like;
import org.uade.progra3.modelo.Portada;
import org.uade.progra3.modelo.Publicacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PortadaDinamica")
class PortadaDinamicaTest {

    private PortadaDinamica portadaDinamica;
    private Portada portada;

    @BeforeEach
    void setUp() {
        portadaDinamica = new PortadaDinamica();
        portada = new Portada();
    }

    /**
     * Helper: creates a publication with the given benefit (ponderar) and size.
     * Benefit = comentarios.size()*10 + likes.size()*2.
     */
    private static Publicacion publicacion(int beneficio, int tamanio) {
        int c = beneficio / 10;
        int l = (beneficio % 10) / 2;
        List<Comentario> comentarios = new ArrayList<>();
        List<Like> likes = new ArrayList<>();
        for (int i = 0; i < c; i++) comentarios.add(new Comentario());
        for (int i = 0; i < l; i++) likes.add(new Like());
        return new Publicacion(likes, comentarios, tamanio);
    }

    @Nested
    @DisplayName("obtenerPublicaciones - early return")
    class EarlyReturn {

        @Test
        @DisplayName("when feed has null listado then portada is cleared")
        void feedListadoNull_clearsPortada() {
            CandidatoPublicaciones feed = new CandidatoPublicaciones(null);
            portada.getPublicaciones().add(publicacion(10, 5)); // pre-fill

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertTrue(portada.getPublicaciones().isEmpty());
        }

        @Test
        @DisplayName("when feed has empty list then portada is cleared")
        void feedListadoEmpty_clearsPortada() {
            CandidatoPublicaciones feed = new CandidatoPublicaciones(Collections.emptyList());
            portada.getPublicaciones().add(publicacion(10, 5));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertTrue(portada.getPublicaciones().isEmpty());
        }
    }

    @Nested
    @DisplayName("obtenerPublicaciones - DP and volcarSolucionEnPortada")
    class DpAndReconstruction {

        @Test
        @DisplayName("single publication that fits is selected")
        void singlePublicationThatFits_selected() {
            Publicacion pub = publicacion(20, 30);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(pub));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(pub, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("single publication that does not fit (size > capacity) leaves portada empty")
        void singlePublicationTooLarge_portadaEmpty() {
            Publicacion pub = publicacion(100, Portada.getTamanioMaximo() + 1);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(pub));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertTrue(portada.getPublicaciones().isEmpty());
        }

        @Test
        @DisplayName("two publications both fit and both selected when capacity allows")
        void twoPublicationsBothFit_bothSelected() {
            Publicacion a = publicacion(10, 40);
            Publicacion b = publicacion(10, 40);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(a, b));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(2, portada.getPublicaciones().size());
            assertEquals(a, portada.getPublicaciones().get(0));
            assertEquals(b, portada.getPublicaciones().get(1));
        }

        @Test
        @DisplayName("two publications only one fits by size then one selected")
        void twoPublicationsOnlyOneFits_oneSelected() {
            Publicacion fits = publicacion(50, 50);
            Publicacion tooBig = publicacion(100, 60);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(fits, tooBig));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(fits, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("knapsack choice: higher benefit subset selected over lower")
        void knapsackOptimalSubsetSelected() {
            // Capacity 100. A=benefit 60 size 50, B=benefit 50 size 50 -> both fit, total 110. A+B=100 size.
            // Or A=60/50, C=40/50 -> 100 benefit, 100 size. So A+C is better than A+B if we had something else.
            // Simpler: A=60 size 50, B=50 size 50 -> both fit, benefit 110.
            // Now: A=60 size 60, B=50 size 50, C=50 size 50. Only one of (B,C) fits with A (60+50=110>100). So A+B or A+C = 110, or B+C = 100. So we want A + one of B,C -> selected 2 items. Covers "include" in volcar.
            Publicacion a = publicacion(60, 60);
            Publicacion b = publicacion(50, 50);
            Publicacion c = publicacion(50, 50);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(a, b, c));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            // Optimal: A + B or A + C (benefit 110), not B+C (100). So 2 items.
            assertEquals(2, portada.getPublicaciones().size());
            int totalBenefit = portada.getPublicaciones().stream().mapToInt(Publicacion::ponderar).sum();
            assertEquals(110, totalBenefit);
        }

        @Test
        @DisplayName("cabeEnElEspacio false path: publication larger than current space")
        void publicationLargerThanSpace_notIncluded() {
            // One pub size 50 benefit 10, one pub size 60 benefit 100. For space 50 we can't take the second.
            Publicacion small = publicacion(10, 50);
            Publicacion big = publicacion(100, 60);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(small, big));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            // Only "small" fits in 100? No: 50+60=110>100. So we take either small (10) or big (100). Optimal = big.
            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(big, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("beneficioIncluyendo <= beneficioSinIncluir: don't update cell")
        void includeNotBetter_keepsPreviousValue() {
            // Low benefit item that fits: including it gives less than not including (e.g. we already have better).
            // First pub 100 benefit 50 size, second 4 benefit 50 size. With capacity 100: take first only (100). Exercises "include" losing to "don't include".
            Publicacion high = publicacion(100, 50);
            Publicacion low = publicacion(4, 50);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(high, low));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(high, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("backtracking skips non-selected items")
        void backtrackingSkipsNonSelected() {
            // Multiple items where some are not in optimal solution (covers volcarSolucionEnPortada branch when equal).
            Publicacion a = publicacion(30, 40);
            Publicacion b = publicacion(30, 40);
            Publicacion c = publicacion(30, 40);
            // Capacity 100. A+B+C = 120 > 100. So best is any two: 60 benefit. One item is left out.
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(a, b, c));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(2, portada.getPublicaciones().size());
            assertEquals(60, portada.getPublicaciones().stream().mapToInt(Publicacion::ponderar).sum());
        }

        @Test
        @DisplayName("portada is cleared before filling with solution")
        void portadaClearedBeforeFill() {
            portada.getPublicaciones().add(publicacion(1, 1));
            Publicacion pub = publicacion(20, 20);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(pub));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(pub, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("order of selected publications is preserved (first to last)")
        void orderPreserved() {
            Publicacion first = publicacion(20, 30);
            Publicacion second = publicacion(20, 30);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(first, second));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(2, portada.getPublicaciones().size());
            assertEquals(first, portada.getPublicaciones().get(0));
            assertEquals(second, portada.getPublicaciones().get(1));
        }
    }
}
