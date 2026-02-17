# Explicación Detallada del Algoritmo Dijkstra

Este documento detalla el comportamiento línea por línea de la implementación del algoritmo Dijkstra (`Djikstra.java`), utilizando como ejemplo el caso de prueba `grafoLinealConAtajo` definido en `DjikstraTest.java`.

## Escenario de Prueba

Utilizaremos el siguiente grafo definido en los tests:

*   **Usuarios (Nodos):** A, B, C, D
*   **Conexiones (Aristas con peso):**
    *   A -> B (peso 1)
    *   B -> C (peso 2)
    *   C -> D (peso 3)
    *   A -> D (peso 4)
*   **Objetivo:** Calcular caminos mínimos desde el usuario **A**.

Visualmente:
```
      1       2       3
  A ----> B ----> C ----> D
   \                     ^
    \________ 4 ________/
```

## Ejecución Línea por Línea

Analizamos la ejecución del método `calcularCaminosMinimos(grafo, A)`.

### 1. Inicialización (Líneas 16-25)

```java
16: Map<Usuario, Integer> distancia = new HashMap<>();
19: Set<Usuario> visitados = new HashSet<>();
```
*   Se crean las estructuras de datos:
    *   `distancia`: Guardará la distancia mínima conocida desde A.
    *   `visitados`: Guardará los usuarios cuyo camino mínimo definitivo ya fue calculado.

```java
22: for (Usuario v : grafo.getUsuarios()) {
23:     distancia.put(v, Integer.MAX_VALUE);
24: }
25: distancia.put(origen, 0);
```
*   Se inicializan las distancias.
*   **Estado:**
    *   `distancia`: { A: 0, B: ∞, C: ∞, D: ∞ }
    *   `visitados`: { }

---

### 2. Primera Iteración del `while` (Líneas 28-51)

**Condición `while`:** `visitados.size()` (0) < `grafo.size()` (4). **Verdadero**.

```java
30: Usuario actual = obtenerUsuarioConMenorDistancia(distancia, visitados);
```
*   Busca el nodo no visitado con menor distancia.
*   Entre A(0), B(∞), C(∞), D(∞), el menor es **A**.
*   `actual` = A.

```java
36: visitados.add(actual);
```
*   Marcamos A como visitado.
*   `visitados`: { A }

**Expansión de adyacentes de A:**
Recorremos las conexiones de A: (A->B, peso 1) y (A->D, peso 4).

*   **Conexión A->B (peso 1):**
    *   `vecino` = B.
    *   `!visitados.contains(B)` es verdadero.
    *   `nuevaDistancia` = `distancia.get(A)` (0) + 1 = 1.
    *   ¿`1 < distancia.get(B)` (∞)? **Sí**.
    *   **Actualización:** `distancia.put(B, 1)`.

*   **Conexión A->D (peso 4):**
    *   `vecino` = D.
    *   `!visitados.contains(D)` es verdadero.
    *   `nuevaDistancia` = `distancia.get(A)` (0) + 4 = 4.
    *   ¿`4 < distancia.get(D)` (∞)? **Sí**.
    *   **Actualización:** `distancia.put(D, 4)`.

*   **Estado al final de la Iteración 1:**
    *   `distancia`: { A: 0, B: 1, C: ∞, D: 4 }
    *   `visitados`: { A }

---

### 3. Segunda Iteración del `while`

**Condición `while`:** `visitados.size()` (1) < 4. **Verdadero**.

```java
30: Usuario actual = obtenerUsuarioConMenorDistancia(...);
```
*   Busca entre los no visitados: B(1), C(∞), D(4).
*   El menor es **B** (1).
*   `actual` = B.

```java
36: visitados.add(B);
```
*   `visitados`: { A, B }

**Expansión de adyacentes de B:**
Conexión de B: (B->C, peso 2).

*   **Conexión B->C (peso 2):**
    *   `vecino` = C.
    *   `nuevaDistancia` = `distancia.get(B)` (1) + 2 = 3.
    *   ¿`3 < distancia.get(C)` (∞)? **Sí**.
    *   **Actualización:** `distancia.put(C, 3)`.

*   **Estado al final de la Iteración 2:**
    *   `distancia`: { A: 0, B: 1, C: 3, D: 4 }
    *   `visitados`: { A, B }

---

### 4. Tercera Iteración del `while`

**Condición `while`:** `visitados.size()` (2) < 4. **Verdadero**.

```java
30: Usuario actual = obtenerUsuarioConMenorDistancia(...);
```
*   Busca entre los no visitados: C(3), D(4).
*   El menor es **C** (3).
*   `actual` = C.

```java
36: visitados.add(C);
```
*   `visitados`: { A, B, C }

**Expansión de adyacentes de C:**
Conexión de C: (C->D, peso 3).

*   **Conexión C->D (peso 3):**
    *   `vecino` = D.
    *   `nuevaDistancia` = `distancia.get(C)` (3) + 3 = 6.
    *   ¿`6 < distancia.get(D)` (4)? **No**.
    *   La distancia actual a D (4, por el atajo directo A->D) es mejor que el camino por C (6). **No se actualiza**.

*   **Estado al final de la Iteración 3:**
    *   `distancia`: { A: 0, B: 1, C: 3, D: 4 }
    *   `visitados`: { A, B, C }

---

### 5. Cuarta Iteración del `while`

**Condición `while`:** `visitados.size()` (3) < 4. **Verdadero**.

```java
30: Usuario actual = obtenerUsuarioConMenorDistancia(...);
```
*   Busca entre los no visitados: D(4).
*   El único y menor es **D** (4).
*   `actual` = D.

```java
36: visitados.add(D);
```
*   `visitados`: { A, B, C, D }

**Expansión de adyacentes de D:**
D no tiene conexiones salientes en este grafo. El bucle `for` no ejecuta iteraciones.

*   **Estado al final de la Iteración 4:**
    *   `distancia`: { A: 0, B: 1, C: 3, D: 4 }
    *   `visitados`: { A, B, C, D }

---

### 6. Finalización

**Condición `while`:** `visitados.size()` (4) < 4. **Falso**.
El bucle termina.

```java
53: return distancia;
```
Se devuelve el mapa final:
*   **A**: 0
*   **B**: 1
*   **C**: 3
*   **D**: 4

El algoritmo ha determinado correctamente que el camino más corto a D es el directo (costo 4) y no el camino más largo a través de B y C (costo 6), cumpliendo con la lógica del algoritmo de Dijkstra.
