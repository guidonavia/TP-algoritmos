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
     * Helper: crea una publicación con el beneficio (ponderar) y tamaño dados.
     * Beneficio = comentarios.size()*10 + likes.size()*2.
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
    @DisplayName("obtenerPublicaciones - retorno temprano")
    class EarlyReturn {

        @Test
        @DisplayName("cuando el feed tiene listado null, la portada se limpia")
        void feedListadoNull_clearsPortada() {
            CandidatoPublicaciones feed = new CandidatoPublicaciones(null);
            portada.getPublicaciones().add(publicacion(10, 5)); // precarga

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertTrue(portada.getPublicaciones().isEmpty());
        }

        @Test
        @DisplayName("cuando el feed tiene lista vacía, la portada se limpia")
        void feedListadoEmpty_clearsPortada() {
            CandidatoPublicaciones feed = new CandidatoPublicaciones(Collections.emptyList());
            portada.getPublicaciones().add(publicacion(10, 5));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertTrue(portada.getPublicaciones().isEmpty());
        }
    }

    @Nested
    @DisplayName("obtenerPublicaciones - PD y volcarSolucionEnPortada")
    class DpAndReconstruction {

        @Test
        @DisplayName("una sola publicación que cabe es seleccionada")
        void singlePublicationThatFits_selected() {
            Publicacion pub = publicacion(20, 30);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(pub));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(pub, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("una sola publicación que no cabe (tamaño > capacidad) deja la portada vacía")
        void singlePublicationTooLarge_portadaEmpty() {
            Publicacion pub = publicacion(100, Portada.getTamanioMaximo() + 1);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(pub));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertTrue(portada.getPublicaciones().isEmpty());
        }

        @Test
        @DisplayName("dos publicaciones que caben, ambas seleccionadas si la capacidad lo permite")
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
        @DisplayName("dos publicaciones, solo una cabe por tamaño, se selecciona una")
        void twoPublicationsOnlyOneFits_oneSelected() {
            Publicacion fits = publicacion(50, 50);
            Publicacion tooBig = publicacion(100, 60);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(fits, tooBig));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(fits, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("mochila: se selecciona el subconjunto de mayor beneficio")
        void knapsackOptimalSubsetSelected() {
            Publicacion a = publicacion(60, 50);
            Publicacion b = publicacion(50, 50);
            Publicacion c = publicacion(50, 50);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(a, b, c));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(2, portada.getPublicaciones().size());
            int totalBenefit = portada.getPublicaciones().stream().mapToInt(Publicacion::ponderar).sum();
            assertEquals(110, totalBenefit);
        }

        @Test
        @DisplayName("camino cabeEnElEspacio falso: publicación más grande que el espacio actual")
        void publicationLargerThanSpace_notIncluded() {
            // Una pub tamaño 50 beneficio 10, otra pub tamaño 60 beneficio 100. Con espacio 50 no podemos tomar la segunda.
            Publicacion small = publicacion(10, 50);
            Publicacion big = publicacion(100, 60);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(small, big));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            // ¿Solo "small" cabe en 100? No: 50+60=110>100. Tomamos small (10) o big (100). Óptimo = big.
            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(big, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("beneficioIncluyendo <= beneficioSinIncluir: no se actualiza la celda")
        void includeNotBetter_keepsPreviousValue() {
            // Ítem de bajo beneficio que cabe: incluirlo da menos que no incluirlo (ya tenemos algo mejor).
            // Primera pub beneficio 100 tamaño 50, segunda beneficio 4 tamaño 50. Con capacidad 100: tomar solo la primera (100). Ejercita "incluir" perdiendo contra "no incluir".
            Publicacion high = publicacion(100, 50);
            Publicacion low = publicacion(4, 50);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(high, low));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(high, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("el backtracking omite ítems no seleccionados")
        void backtrackingSkipsNonSelected() {
            // Múltiples ítems donde algunos no están en la solución óptima (cubre la rama de volcarSolucionEnPortada cuando son iguales).
            Publicacion a = publicacion(30, 40);
            Publicacion b = publicacion(30, 40);
            Publicacion c = publicacion(30, 40);
            // Capacidad 100. A+B+C = 120 > 100. Lo mejor es cualquier par: beneficio 60. Un ítem queda afuera.
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(a, b, c));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(2, portada.getPublicaciones().size());
            assertEquals(60, portada.getPublicaciones().stream().mapToInt(Publicacion::ponderar).sum());
        }

        @Test
        @DisplayName("la portada se limpia antes de llenarla con la solución")
        void portadaClearedBeforeFill() {
            portada.getPublicaciones().add(publicacion(1, 1));
            Publicacion pub = publicacion(20, 20);
            CandidatoPublicaciones feed = new CandidatoPublicaciones(List.of(pub));

            portadaDinamica.obtenerPublicaciones(feed, portada);

            assertEquals(1, portada.getPublicaciones().size());
            assertEquals(pub, portada.getPublicaciones().get(0));
        }

        @Test
        @DisplayName("el orden de las publicaciones seleccionadas se preserva (primera a última)")
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
