# Plan de Implementación: Optimización de Dijkstra con PriorityQueue

## Goal Description
Optimizar el algoritmo de Dijkstra actual (`O(V^2)`) reemplazando la búsqueda lineal del nodo con menor distancia por una `PriorityQueue` (Min-Heap), reduciendo la complejidad a `O(E log V)`. Esto permitirá que el algoritmo escale eficientemente para grafos con miles o millones de usuarios.

## User Review Required
> [!IMPORTANT]
> Esta optimización requiere crear una clase auxiliar (`NodoDistancia`) para almacenar pares `(Usuario, Distancia)` en la cola de prioridad, ya que Java `PriorityQueue` necesita objetos comparables.

## Proposed Changes

### org.uade.progra3.grafos

#### [MODIFY] [Djikstra.java](file:///c:/Users/Guido/Documents/Repositorios/TP-algoritmos/src/main/java/org/uade/progra3/grafos/Djikstra.java)
- Eliminar el método `obtenerUsuarioConMenorDistancia` (Búsqueda lineal).
- Modificar `calcularCaminosMinimos`:
    - Instanciar `PriorityQueue<NodoDistancia>`.
    - En lugar de iterar `while (visitados.size() < n)`, usar `while (!pq.isEmpty())`.
    - Al actualizar una distancia, insertar el nuevo par en la cola.

#### [NEW] [NodoDistancia.java](file:///c:/Users/Guido/Documents/Repositorios/TP-algoritmos/src/main/java/org/uade/progra3/grafos/NodoDistancia.java)
- Clase simple (o `record` si la versión de Java lo permite, asumo Java 8+ por seguridad haré clase) que implemente `Comparable<NodoDistancia>`.
- Campos: `Usuario usuario`, `int distancia`.
- `compareTo`: Compara por distancia ascendente.

## Verification Plan

### Automated Tests
- Ejecutar `DjikstraTest.java` existente.
- La lógica de los tests no cambia (entradas y salidas son las mismas), solo cambia la eficiencia interna.
- Verificar que todos los tests pasen (GREEN).
