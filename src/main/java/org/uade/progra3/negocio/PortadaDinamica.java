package org.uade.progra3.negocio;

import org.uade.progra3.modelo.Feed;
import org.uade.progra3.modelo.Publicacion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortadaDinamica {


    public void obtenerPublicaciones(Feed feed) {
        if (feed.getListadoPublicaciones().isEmpty() || publicacionesTotal(feed) == 0) {
            System.out.println("Sin publicaciones disponibles");
            return;
        }
        /*
        int montoMinimo = stock.keySet().stream().min(Integer::compareTo).get();

        if (monto < montoMinimo) {
            System.out.println("Monto mínimo a extraer: $" + montoMinimo);
            return;
        }
        */

        List<Publicacion> publicaciones = feed.obtenerPublicaciones();
        feed.
        int max = publicaciones.size() + 1;
        int[] dp = new int[publicaciones.size() + 1];
        int[] ultimaPublicacion = new int[publicaciones.size()  + 1];

        for (int i = 1; i <= publicaciones.size(); i++) {
            dp[i] = max;
        }
        dp[0] = 0;

        // Programación Dinámica
        for (Publicacion p : publicaciones) {
            for (int i = b; i <= monto; i++) {
                if (dp[i - b] + 1 < dp[i]) {
                    dp[i] = dp[i - b] + 1;
                    ultimoBillete[i] = b;
                }
            }
        }

        if (dp[monto] == max) {
            System.out.println("Cajero fuera de servicio o fondos insuficientes para este monto");
            return;
        }

        Map<Integer, Integer> resultado = reconstruirSolucion(monto, ultimoBillete);

        if (!hayStockSuficiente(resultado)) {
            System.out.println("Cajero fuera de servicio o fondos insuficientes para este monto");
            return;
        }

        mostrarResultadoUsuario(resultado);
        actualizarStock(resultado);
        verificarAlertas();
    }

    // ---------------- Métodos auxiliares ----------------

    private Map<Integer, Integer> reconstruirSolucion(int monto, int[] ultimoBillete) {
        Map<Integer, Integer> resultado = new HashMap<>();
        int restante = monto;

        while (restante > 0) {
            int b = ultimoBillete[restante];
            resultado.put(b, resultado.getOrDefault(b, 0) + 1);
            restante -= b;
        }
        return resultado;
    }

    private boolean hayStockSuficiente(Map<Integer, Integer> resultado) {
        for (int b : resultado.keySet()) {
            if (resultado.get(b) > stock.getOrDefault(b, 0)) {
                return false;
            }
        }
        return true;
    }

    private void mostrarResultadoUsuario(Map<Integer, Integer> resultado) {
        System.out.println("Operación exitosa. Se entregará:");
        for (int b : resultado.keySet()) {
            System.out.println("$" + b + " x " + resultado.get(b));
        }
    }

    private void actualizarStock(Map<Integer, Integer> resultado) {
        for (int b : resultado.keySet()) {
            stock.put(b, stock.get(b) - resultado.get(b));
        }
    }

    private void verificarAlertas() {
        for (int b : stock.keySet()) {
            if (stock.get(b) < UMBRAL_ALERTA) {
                System.out.println("ALERTA MANTENIMIENTO: Bajo stock de billetes de $" + b);
            }
        }
    }

    private int publicacionesTotal(Feed feed) {
        return feed.getListadoPublicaciones().size();
    }
}
