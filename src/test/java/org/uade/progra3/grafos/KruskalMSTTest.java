package org.uade.progra3.grafos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KruskalMST")
class KruskalMSTTest {

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
     * Grafo conexo no dirigido modelado con aristas en ambas direcciones.
     * A --1-- B --2-- C --3-- D
     *  \______4______/
     * MST: A-B(1), B-C(2), C-D(3) → peso total 6.
     */
    private void grafo4NodosConMSTPeso6() {
        grafo.agregarUsuario(a);
        grafo.agregarUsuario(b);
        grafo.agregarUsuario(c);
        grafo.agregarUsuario(d);
        grafo.agregarConexion(a, b, 1);
        grafo.agregarConexion(b, a, 1);
        grafo.agregarConexion(b, c, 2);
        grafo.agregarConexion(c, b, 2);
        grafo.agregarConexion(c, d, 3);
        grafo.agregarConexion(d, c, 3);
        grafo.agregarConexion(a, d, 4);
        grafo.agregarConexion(d, a, 4);
    }

    @Nested
    @DisplayName("arbolDeRecubrimientoMinimo")
    class ArbolRecubrimiento {

        @Test
        @DisplayName("MST has n-1 edges for n vertices")
        void mstTieneNMenos1Aristas() {
            grafo4NodosConMSTPeso6();
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            int n = mst.getUsuarios().size();
            assertEquals(4, n);
            assertEquals(3, mst.getConexiones().size(), "Árbol con 4 nodos debe tener 3 aristas");
        }

        @Test
        @DisplayName("MST total weight is minimal (known 6)")
        void mstPesoTotalMinimo() {
            grafo4NodosConMSTPeso6();
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            int pesoTotal = mst.getConexiones().stream().mapToInt(Conexion::getPeso).sum();
            assertEquals(6, pesoTotal);
        }

        @Test
        @DisplayName("MST contains all original vertices")
        void mstContieneTodosLosVertices() {
            grafo4NodosConMSTPeso6();
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            Set<Usuario> vertices = mst.getUsuarios();
            assertTrue(vertices.contains(a));
            assertTrue(vertices.contains(b));
            assertTrue(vertices.contains(c));
            assertTrue(vertices.contains(d));
            assertEquals(4, vertices.size());
        }

        @Test
        @DisplayName("single node graph yields empty edge set")
        void grafoUnNodo_sinAristas() {
            grafo.agregarUsuario(a);
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            assertEquals(1, mst.getUsuarios().size());
            assertTrue(mst.getConexiones().isEmpty());
        }

        @Test
        @DisplayName("two connected nodes yield one edge")
        void grafoDosNodos_unaArista() {
            grafo.agregarUsuario(a);
            grafo.agregarUsuario(b);
            grafo.agregarConexion(a, b, 10);
            grafo.agregarConexion(b, a, 10);
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            assertEquals(2, mst.getUsuarios().size());
            assertEquals(1, mst.getConexiones().size());
            assertEquals(10, mst.getConexiones().get(0).getPeso());
        }

        @Test
        @DisplayName("chooses lighter edge when multiple connect same components")
        void eligeAristaMasLiviana() {
            grafo.agregarUsuario(a);
            grafo.agregarUsuario(b);
            grafo.agregarUsuario(c);
            grafo.agregarConexion(a, b, 1);
            grafo.agregarConexion(b, a, 1);
            grafo.agregarConexion(b, c, 2);
            grafo.agregarConexion(c, b, 2);
            grafo.agregarConexion(a, c, 100); // arista pesada
            grafo.agregarConexion(c, a, 100);
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            int pesoTotal = mst.getConexiones().stream().mapToInt(Conexion::getPeso).sum();
            assertEquals(3, pesoTotal, "Debe elegir A-B(1) y B-C(2), no A-C(100)");
            assertEquals(2, mst.getConexiones().size());
        }
    }
}
