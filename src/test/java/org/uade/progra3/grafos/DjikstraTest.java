package org.uade.progra3.grafos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.uade.progra3.modelo.Usuario;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Djikstra")
class DjikstraTest {

    private Grafo grafo;
    private Usuario a, b, c, d;

    @BeforeEach
    void setUp() {
        grafo = new Grafo();
        a = new Usuario(1L, "A");
        b = new Usuario(2L, "B");
        c = new Usuario(3L, "C");
        d = new Usuario(4L, "D");
    }

    /**
     * Grafo: A --1--> B --2--> C --3--> D
     *            \_____4________/
     * Desde A: A=0, B=1, C=3, D=min(6, 4)=4.
     */
    private void grafoLinealConAtajo() {
        grafo.agregarUsuario(a);
        grafo.agregarUsuario(b);
        grafo.agregarUsuario(c);
        grafo.agregarUsuario(d);
        grafo.agregarConexion(a, b, 1);
        grafo.agregarConexion(b, c, 2);
        grafo.agregarConexion(c, d, 3);
        grafo.agregarConexion(a, d, 4); // atajo A->D peso 4 (peor que 1+2+3=6)
    }

    @Nested
    @DisplayName("calcularCaminosMinimos")
    class CaminosMinimos {

        @Test
        @DisplayName("el origen tiene distancia 0")
        void origenDistanciaCero() {
            grafoLinealConAtajo();
            Map<Usuario, Integer> dist = Djikstra.calcularCaminosMinimos(grafo, a);
            assertEquals(0, dist.get(a));
        }

        @Test
        @DisplayName("el vecino directo tiene el peso de la arista")
        void vecinoDirectoPesoArista() {
            grafoLinealConAtajo();
            Map<Usuario, Integer> dist = Djikstra.calcularCaminosMinimos(grafo, a);
            assertEquals(1, dist.get(b));
        }

        @Test
        @DisplayName("camino mínimo: C=3 (A->B->C), D=4 (atajo A->D)")
        void caminoMinimoCadena() {
            grafoLinealConAtajo();
            Map<Usuario, Integer> dist = Djikstra.calcularCaminosMinimos(grafo, a);
            assertEquals(3, dist.get(c)); // 1+2
            assertEquals(4, dist.get(d)); // atajo A->D=4 (menor que 1+2+3=6)
        }

        @Test
        @DisplayName("desde B: B=0, C=2, D=5, A inalcanzable (dirigido)")
        void desdeB() {
            grafoLinealConAtajo();
            Map<Usuario, Integer> dist = Djikstra.calcularCaminosMinimos(grafo, b);
            assertEquals(0, dist.get(b));
            assertEquals(2, dist.get(c));
            assertEquals(5, dist.get(d));
            assertEquals(Integer.MAX_VALUE, dist.get(a));
        }

        @Test
        @DisplayName("un solo nodo: distancia a sí mismo es 0")
        void unSoloNodo() {
            grafo.agregarUsuario(a);
            Map<Usuario, Integer> dist = Djikstra.calcularCaminosMinimos(grafo, a);
            assertEquals(1, dist.size());
            assertEquals(0, dist.get(a));
        }

        @Test
        @DisplayName("dos nodos conectados: distancia correcta")
        void dosNodosConectados() {
            grafo.agregarUsuario(a);
            grafo.agregarUsuario(b);
            grafo.agregarConexion(a, b, 7);
            Map<Usuario, Integer> dist = Djikstra.calcularCaminosMinimos(grafo, a);
            assertEquals(0, dist.get(a));
            assertEquals(7, dist.get(b));
        }

        @Test
        @DisplayName("nodo inalcanzable tiene MAX_VALUE")
        void nodoInalcanzable() {
            grafo.agregarUsuario(a);
            grafo.agregarUsuario(b);
            grafo.agregarConexion(a, b, 1);
            Map<Usuario, Integer> dist = Djikstra.calcularCaminosMinimos(grafo, b);
            assertEquals(0, dist.get(b));
            assertEquals(Integer.MAX_VALUE, dist.get(a));
        }

        @Test
        @DisplayName("devuelve una entrada por vértice del grafo")
        void unaEntradaPorVertice() {
            grafoLinealConAtajo();
            Map<Usuario, Integer> dist = Djikstra.calcularCaminosMinimos(grafo, a);
            assertEquals(4, dist.size());
        }
    }
}
