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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        @DisplayName("el MST tiene n-1 aristas para n vértices")
        void mstTieneNMenos1Aristas() {
            grafo4NodosConMSTPeso6();
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            int n = mst.getUsuarios().size();
            assertEquals(4, n);
            assertEquals(3, mst.getConexiones().size(), "Árbol con 4 nodos debe tener 3 aristas");
        }

        @Test
        @DisplayName("el peso total del MST es mínimo (conocido: 6)")
        void mstPesoTotalMinimo() {
            grafo4NodosConMSTPeso6();
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            int pesoTotal = mst.getConexiones().stream().mapToInt(Conexion::getPeso).sum();
            assertEquals(6, pesoTotal);
        }

        @Test
        @DisplayName("el MST contiene todos los vértices originales")
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
        @DisplayName("grafo de un solo nodo produce conjunto de aristas vacío")
        void grafoUnNodo_sinAristas() {
            grafo.agregarUsuario(a);
            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);
            assertEquals(1, mst.getUsuarios().size());
            assertTrue(mst.getConexiones().isEmpty());
        }

        @Test
        @DisplayName("dos nodos conectados producen una arista")
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
        @DisplayName("rechaza arista cuando origen y destino ya están en la misma componente (evita ciclo)")
        void rechazaAristaQueCreariaCiclo() {
            grafo.agregarUsuario(a);
            grafo.agregarUsuario(b);
            grafo.agregarUsuario(c);
            grafo.agregarConexion(a, b, 1);
            grafo.agregarConexion(b, a, 1);
            grafo.agregarConexion(b, c, 2);
            grafo.agregarConexion(c, b, 2);
            grafo.agregarConexion(a, c, 3);
            grafo.agregarConexion(c, a, 3);

            Grafo mst = KruskalMST.arbolDeRecubrimientoMinimo(grafo);

            assertEquals(2, mst.getConexiones().size(), "Solo 2 aristas: A-B y B-C");
            assertEquals(3, mst.getConexiones().stream().mapToInt(Conexion::getPeso).sum());
            boolean hayAconexionAC = mst.getConexiones().stream()
                    .anyMatch(con -> (con.getOrigen().equals(a) && con.getDestino().equals(c))
                            || (con.getOrigen().equals(c) && con.getDestino().equals(a)));
            assertFalse(hayAconexionAC, "A-C crearía ciclo, no debe estar en el MST");
        }

        @Test
        @DisplayName("elige la arista más liviana cuando varias conectan los mismos componentes")
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
