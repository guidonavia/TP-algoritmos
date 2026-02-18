# Explicación Paso a Paso: Algoritmo de Portada (Mochila 0/1)

Este documento detalla el funcionamiento del algoritmo utilizado en `PortadaDinamica.java` para seleccionar las mejores publicaciones que mostrar en la portada sin exceder el espacio disponible.

Este problema es una variante clásica de **Programación Dinámica** conocida como **Problema de la Mochila (Knapsack Problem 0/1)**: tenemos una "mochila" (portada) con capacidad limitada (espacio) y "ítems" (publicaciones) con peso (tamaño) y valor (beneficio). Queremos maximizar el valor total sin romper la mochila.

## 1. Escenario Simplificado (Mini-Caso)

Para explicar la traza, usaremos un caso más pequeño que el real para que sea fácil de seguir.

*   **Capacidad de la Portada (Espacio Máximo):** `W = 10`
*   **Publicaciones Disponibles:**
    1.  **Pub A**: Beneficio `5`, Tamaño `4`
    2.  **Pub B**: Beneficio `8`, Tamaño `5`
    3.  **Pub C**: Beneficio `9`, Tamaño `6`

El objetivo es elegir un subconjunto de estas publicaciones tal que la suma de sus tamaños sea `<=` 10 y la suma de sus beneficios sea máxima.

## 2. Estructura de Datos (Tabla DP)

El algoritmo construye una matriz `beneficioMaximoHasta[i][w]`, donde:
*   `i` (filas) representa "considerando hasta la publicación `i`".
*   `w` (columnas) representa "con una capacidad disponible de `w`".
*   El valor de la celda es el **beneficio máximo posible** bajo esas condiciones.

Dimensiones de la tabla:
*   Filas: 0 a 3 (0=ninguna, 1=solo A, 2=A y B, 3=A, B y C).
*   Columnas: 0 a 10 (capacidades de 0 a 10).

---

## 3. Ejecución Paso a Paso: Llenado de la Tabla

### Inicialización (Fila 0)
La fila 0 representa "considerando 0 ítems". El beneficio es 0 para cualquier capacidad.
`[0, 0, 0, ..., 0]` (Esta fila ya está inicializada en 0 por defecto en Java).

### Paso 1: Procesando Pub A (Beneficio 5, Tamaño 4)
Estamos en la **Fila 1**.
Para cada capacidad `w` (columna) desde 0 hasta 10:
*   Si `w < 4` (no cabe): Copiamos el valor de arriba (0).
*   Si `w >= 4` (cabe): Elegimos el máximo entre:
    *   No incluirla: valor de arriba (0).
    *   Incluirla: `Beneficio(A) + Valor(Fila 0, Capacidad w-4)` = `5 + 0 = 5`.
    *   Max(0, 5) = 5.

**Resultado Fila 1 (Solo Pub A):**
| Capacidad | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|:---------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:--:|
| **Fila 0**| 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **Fila 1**| 0 | 0 | 0 | 0 | **5**| **5**| **5**| **5**| **5**| **5**| **5**|

### Paso 2: Procesando Pub B (Beneficio 8, Tamaño 5)
Estamos en la **Fila 2**. Item actual: B (Ben=8, Tam=5).
Para cada capacidad `w`:
*   Si `w < 5` (no cabe B): Copiamos el valor de arriba.
    *   Ejs: `dp[2][4]` = `dp[1][4]` = 5.
*   Si `w >= 5` (cabe B):
    *   **Opción 1 (No poner B):** Valor de arriba (`dp[1][w]`).
    *   **Opción 2 (Poner B):** `8 + dp[1][w-5]`.
    *   Tomamos el máximo.

Ejemplos clave en Fila 2:
*   `w=5`: Max( `dp[1][5]`=5, `8 + dp[1][0]`=8+0=8 ) -> **8**. (Mejor poner B que A).
*   `w=8`: Max( `dp[1][8]`=5, `8 + dp[1][3]`=8+0=8 ) -> **8**.
*   `w=9`: Max( `dp[1][9]`=5, `8 + dp[1][4]`=8+5=13 ) -> **13**. (¡Poner B deja espacio 4, donde cabe A! Total 13).
*   `w=10`: Max( `dp[1][10]`=5, `8 + dp[1][5]`=8+5=13 ) -> **13**.

**Resultado Fila 2 (Pubs A y B):**
| Capacidad | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|:---------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:--:|
| **Fila 2**| 0 | 0 | 0 | 0 | 5 | **8**| **8**| **8**| **8**| **13**| **13**|

### Paso 3: Procesando Pub C (Beneficio 9, Tamaño 6)
Estamos en la **Fila 3**. Item actual: C (Ben=9, Tam=6).
Análisis de capacidades:
*   `w < 6`: Copiar de arriba. `dp[3][5]` = `dp[2][5]` = 8.
*   `w = 6`: Max(`dp[2][6]`=8, `9 + dp[2][0]`=9) -> **9**. (Mejor C sola que B sola).
*   `w = 9`: Max(`dp[2][9]`=13, `9 + dp[2][3]`=9+0=9) -> **13**. (A+B=13 gana a C sola).
*   `w = 10`: Max(`dp[2][10]`=13, `9 + dp[2][4]`=9+5=14) -> **14**. (C deja espacio 4, donde cabe A. Total 9+5=14. A+B era 13).

**Resultado Fila 3 (Final):**
| Capacidad | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|:---------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:--:|
| **Fila 3**| 0 | 0 | 0 | 0 | 5 | 8 | **9**| 9 | 9 | 13 | **14**|

**Beneficio Máximo Obtenido:** 14.

---

## 4. Reconstrucción de la Solución (Backtracking)

Una vez llena la tabla, ¿qué publicaciones logran ese 14? Recorremos desde el final (`i=3, w=10`) hacia el inicio.

1.  **Estado actual:** Fila 3 (C), Capacidad 10. Valor = 14.
    *   Comparamos con el valor de justo arriba (`dp[2][10]` = 13).
    *   ¿Son diferentes? **Sí** (14 != 13).
    *   **Significado:** Incluir C mejoró el resultado.
    *   **Acción:** **Seleccionamos C**.
    *   Nuevo estado: `i=2`, Capacidad = `10 - Tamaño(C)` = `10 - 6 = 4`.

2.  **Estado actual:** Fila 2 (B), Capacidad 4. Valor = 5 (leemos `dp[2][4]`).
    *   Comparamos con valor de arriba (`dp[1][4]` = 5).
    *   ¿Son diferentes? **No** (5 == 5).
    *   **Significado:** Incluir B no mejoró (o no cabía) para capacidad 4. El valor viene de antes.
    *   **Acción:** **No seleccionamos B**.
    *   Nuevo estado: `i=1`, Capacidad = `4` (mantiene igual).

3.  **Estado actual:** Fila 1 (A), Capacidad 4. Valor = 5 (leemos `dp[1][4]`).
    *   Comparamos con valor de arriba (`dp[0][4]` = 0).
    *   ¿Son diferentes? **Sí** (5 != 0).
    *   **Acción:** **Seleccionamos A**.
    *   Nuevo estado: `i=0`, Capacidad = `4 - Tamaño(A)` = `4 - 4 = 0`.

4.  **Estado actual:** Fila 0. Fin del proceso.

### Resultado Final
Publicaciones Seleccionadas: **C y A**.
*   Suma de Beneficios: 9 + 5 = **14**.
*   Suma de Tamaños: 6 + 4 = **10** (<= 10).

El algoritmo de `PortadaDinamica.java` sigue exactamente esta lógica, escalada a las publicaciones y capacidad reales del sistema.
