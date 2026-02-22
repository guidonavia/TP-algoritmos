package org.uade.progra3.negocio;

import org.uade.progra3.modelo.Administrador;
import org.uade.progra3.modelo.Grupo;

import java.util.Arrays;
import java.util.List;

/**
 * Opcional 3 – Asignación de Administradores a Grupos.
 *
 * Asigna un administrador a cada grupo de manera que la suma total de
 * ineficiencias sea mínima (ineficiencia = 100 − eficiencia).
 *
 * Estrategia: Programación Dinámica con máscara de bits (bitmask DP).
 *   - Estado: dp[mask] = ineficiencia mínima acumulada cuando los administradores
 *     indicados por los bits activos de {@code mask} ya han sido asignados.
 *   - Transición: si en {@code mask} hay k bits activos, el siguiente grupo a
 *     cubrir es el grupo k. Para cada administrador i no usado, se calcula
 *     el nuevo costo y se actualiza dp[mask | (1 << i)].
 *   - La clave DP es que dp[mask] se construye siempre a partir de
 *     dp[mask sin el último admin], es decir, la solución óptima para los
 *     primeros k grupos se apoya en la solución óptima para los k-1 grupos
 *     anteriores, cumpliendo exactamente el principio de subproblemas óptimos.
 *   - Reconstrucción: se rastrea qué admin se eligió en cada transición para
 *     poder recuperar la asignación completa al final.
 *
 * Complejidad: O(n² · 2ⁿ) tiempo, O(2ⁿ) espacio (n = número de grupos/admins).
 */
public class AsignacionAdministradores {

    /**
     * Calcula la asignación óptima de administradores a grupos.
     *
     * Precondición: {@code administradores.size() == grupos.size()} y
     * cada admin tiene al menos tantas eficiencias como grupos hay.
     *
     * @param grupos          lista de grupos a administrar
     * @param administradores lista de administradores disponibles
     * @return resultado con la asignación óptima y la ineficiencia total mínima
     */
    public ResultadoAsignacion calcularAsignacionOptima(List<Grupo> grupos,
                                                        List<Administrador> administradores) {
        int n = grupos.size();
        int totalMascaras = 1 << n;

        // dp[mask] = ineficiencia mínima cuando los admins en 'mask' ya están asignados
        int[] dp = new int[totalMascaras];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;

        // adminAsignado[mask] = índice del admin elegido para llegar al estado 'mask'
        int[] adminAsignado = new int[totalMascaras];

        for (int mascara = 0; mascara < totalMascaras; mascara++) {
            if (dp[mascara] == Integer.MAX_VALUE) continue;

            // El siguiente grupo a asignar es el k-ésimo, donde k = cantidad de bits activos
            int grupoActual = Integer.bitCount(mascara);
            if (grupoActual == n) continue;

            for (int admin = 0; admin < n; admin++) {
                if ((mascara & (1 << admin)) != 0) continue; // admin ya asignado

                int ineficiencia = 100 - administradores.get(admin).getEficiencia(grupoActual);
                int nuevaMascara = mascara | (1 << admin);
                int nuevoCosto = dp[mascara] + ineficiencia;

                if (nuevoCosto < dp[nuevaMascara]) {
                    dp[nuevaMascara] = nuevoCosto;
                    adminAsignado[nuevaMascara] = admin;
                }
            }
        }

        // Reconstruir la asignación recorriendo las máscaras hacia atrás
        int[] asignacion = new int[n];
        int mascara = totalMascaras - 1;
        for (int grupo = n - 1; grupo >= 0; grupo--) {
            int admin = adminAsignado[mascara];
            asignacion[grupo] = admin;
            mascara ^= (1 << admin);
        }

        return new ResultadoAsignacion(grupos, administradores, asignacion, dp[totalMascaras - 1]);
    }
}
