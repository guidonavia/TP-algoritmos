
import java.util.Map;

import org.uade.progra3.grafos.Djikstra;
import org.uade.progra3.grafos.Grafo;
import org.uade.progra3.grafos.Usuario;
import org.uade.progra3.utils.DataLoader;


public class Main {
    public static void main(String[] args) {

        // Ejercicio 3:
        Grafo grafo = new Grafo();

        DataLoader dataLoader = new DataLoader(grafo);

        dataLoader.cargarDesdeRecurso("conexiones.JSON");

        // System.out.println(KruskalMST.arbolDeRecubrimientoMinimo(grafo));

        // Ejercicio 4:
        Usuario origen = grafo.getUsuarios().iterator().next();
        Map<Usuario, Integer> map = Djikstra.calcularCaminosMinimos(grafo, origen);
        
        for (Map.Entry<Usuario, Integer> entry : map.entrySet()) {
            Usuario usuario = entry.getKey();
            Integer distancia = entry.getValue();
            System.out.println(usuario.getNombre() + " -> " + distancia);
        }
    }
}