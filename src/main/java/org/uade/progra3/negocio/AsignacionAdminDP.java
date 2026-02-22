package org.uade.progra3.negocio;

import java.util.Arrays;

/**
 * Problema Opcional 3: Asignación de Administradores a Grupos.
 *
 * Resuelve el problema de asignación (assignment problem) usando
 * Programación Dinámica con máscara de bits (bitmask DP).
 *
 * Cada administrador tiene un valor de ineficiencia diferente para cada grupo.
 * El objetivo es encontrar una asignación uno a uno que minimice la suma total
 * de ineficiencias.
 *
 * Estrategia (Programación Dinámica con bitmask):
 *   - Estado: dp[mask] = costo mínimo para asignar los administradores indicados
 *     por los bits de 'mask' a los primeros popcount(mask) grupos.
 *   - Transición: Para cada mask, sea k = popcount(mask) (el grupo k-1 ya fue
 *     asignado). Para cada admin j cuyo bit está en mask:
 *       dp[mask] = min(dp[mask], dp[mask ^ (1<<j)] + costos[k-1][j])
 *   - Caso base: dp[0] = 0 (ningún grupo asignado, costo cero).
 *   - Respuesta: dp[mask_final] donde mask_final tiene exactamente nGrupos bits.
 *
 * Complejidad temporal: O(n * 2^n) donde n = cantidad de administradores.
 * Complejidad espacial: O(2^n) para las tablas dp y parent.
 */
public class AsignacionAdminDP {

    /**
     * Resultado de la asignación óptima.
     */
    public static class ResultadoAsignacion {
        private final int costoTotal;
        private final int[] asignacion; // asignacion[i] = índice del admin asignado al grupo i
        private final String[] nombresGrupos;
        private final String[] nombresAdmins;
        private final int[][] costos;

        public ResultadoAsignacion(int costoTotal, int[] asignacion,
                                   String[] nombresGrupos, String[] nombresAdmins,
                                   int[][] costos) {
            this.costoTotal = costoTotal;
            this.asignacion = asignacion;
            this.nombresGrupos = nombresGrupos;
            this.nombresAdmins = nombresAdmins;
            this.costos = costos;
        }

        /** Costo total (suma de ineficiencias) de la asignación óptima. */
        public int getCostoTotal() { return costoTotal; }

        /** asignacion[i] = índice del administrador asignado al grupo i. */
        public int[] getAsignacion() { return asignacion; }

        public String[] getNombresGrupos() { return nombresGrupos; }

        public String[] getNombresAdmins() { return nombresAdmins; }

        /** Matriz original de costos. */
        public int[][] getCostos() { return costos; }
    }

    /**
     * Resuelve el problema de asignación mediante DP con bitmask.
     *
     * @param costos       costos[i][j] = ineficiencia del administrador j para el grupo i
     * @param nombresGrupos nombres de los grupos (longitud = cantidad de grupos)
     * @param nombresAdmins nombres de los administradores (longitud = cantidad de admins)
     * @return resultado con asignación óptima y costo mínimo
     * @throws IllegalArgumentException si hay menos admins que grupos
     */
    public static ResultadoAsignacion asignar(int[][] costos,
                                              String[] nombresGrupos,
                                              String[] nombresAdmins) {
        int nGrupos = costos.length;
        int nAdmins = costos[0].length;

        if (nAdmins < nGrupos) {
            throw new IllegalArgumentException(
                    "No hay suficientes administradores para cubrir todos los grupos");
        }

        int totalMasks = 1 << nAdmins;

        // dp[mask] = costo mínimo para asignar los admins del mask a los primeros popcount(mask) grupos
        int[] dp = new int[totalMasks];
        // parent[mask] = índice del admin que fue asignado al último grupo en este mask
        int[] parent = new int[totalMasks];
        Arrays.fill(dp, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dp[0] = 0; // caso base: sin grupos asignados, costo 0

        // Llenar la tabla DP
        for (int mask = 1; mask < totalMasks; mask++) {
            int k = Integer.bitCount(mask); // cantidad de admins usados = cantidad de grupos asignados
            if (k > nGrupos) continue; // no necesitamos más admins que grupos

            int grupo = k - 1; // índice del grupo que se está asignando (0-indexed)

            // Probar cada admin j que esté incluido en este mask
            for (int j = 0; j < nAdmins; j++) {
                if ((mask & (1 << j)) == 0) continue; // admin j no está en mask

                int maskPrevio = mask ^ (1 << j); // mask sin el admin j
                if (dp[maskPrevio] == Integer.MAX_VALUE) continue;

                int nuevoCosto = dp[maskPrevio] + costos[grupo][j];
                if (nuevoCosto < dp[mask]) {
                    dp[mask] = nuevoCosto;
                    parent[mask] = j; // recordar que el admin j fue asignado al grupo 'grupo'
                }
            }
        }

        // Encontrar el mask óptimo final (exactamente nGrupos bits encendidos)
        int mejorMask = -1;
        int mejorCosto = Integer.MAX_VALUE;
        for (int mask = 0; mask < totalMasks; mask++) {
            if (Integer.bitCount(mask) == nGrupos && dp[mask] < mejorCosto) {
                mejorCosto = dp[mask];
                mejorMask = mask;
            }
        }

        // Reconstruir la asignación mediante backtracking sobre la tabla parent
        int[] asignacion = new int[nGrupos];
        int mask = mejorMask;
        for (int g = nGrupos - 1; g >= 0; g--) {
            asignacion[g] = parent[mask];
            mask = mask ^ (1 << parent[mask]);
        }

        return new ResultadoAsignacion(mejorCosto, asignacion, nombresGrupos, nombresAdmins, costos);
    }
}
