package org.uade.progra3.negocio;

import java.util.Arrays;

public class AsignacionAdminDP {

    public static class ResultadoAsignacion {
        private final int costoTotal;
        private final int[] asignacion;
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

        public int getCostoTotal() { return costoTotal; }

        public int[] getAsignacion() { return asignacion; }

        public String[] getNombresGrupos() { return nombresGrupos; }

        public String[] getNombresAdmins() { return nombresAdmins; }

        public int[][] getCostos() { return costos; }
    }

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

        int[] dp = new int[totalMasks];
        int[] parent = new int[totalMasks];
        Arrays.fill(dp, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dp[0] = 0;

        for (int mask = 1; mask < totalMasks; mask++) {
            int k = Integer.bitCount(mask);
            if (k > nGrupos) continue;

            int grupo = k - 1;

            for (int j = 0; j < nAdmins; j++) {
                if ((mask & (1 << j)) == 0) continue;

                int maskPrevio = mask ^ (1 << j);
                if (dp[maskPrevio] == Integer.MAX_VALUE) continue;

                int nuevoCosto = dp[maskPrevio] + costos[grupo][j];
                if (nuevoCosto < dp[mask]) {
                    dp[mask] = nuevoCosto;
                    parent[mask] = j;
                }
            }
        }

        int mejorMask = -1;
        int mejorCosto = Integer.MAX_VALUE;
        for (int mask = 0; mask < totalMasks; mask++) {
            if (Integer.bitCount(mask) == nGrupos && dp[mask] < mejorCosto) {
                mejorCosto = dp[mask];
                mejorMask = mask;
            }
        }

        int[] asignacion = new int[nGrupos];
        int mask = mejorMask;
        for (int g = nGrupos - 1; g >= 0; g--) {
            asignacion[g] = parent[mask];
            mask = mask ^ (1 << parent[mask]);
        }

        return new ResultadoAsignacion(mejorCosto, asignacion, nombresGrupos, nombresAdmins, costos);
    }
}
