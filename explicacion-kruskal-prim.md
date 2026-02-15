# Algoritmos de Árbol de Recubrimiento Mínimo (MST)

## ¿Qué es un Árbol de Recubrimiento Mínimo (MST)?

Un **Árbol de Recubrimiento Mínimo** es un subgrafo que:
- ✅ Conecta **todos los vértices** del grafo original
- ✅ Es un **árbol** (sin ciclos)
- ✅ Tiene el **peso total mínimo** posible

**Ejemplo práctico en tu red social:**
- Tienes usuarios (vértices) y conexiones entre ellos (aristas con pesos)
- Quieres encontrar el conjunto mínimo de conexiones para que todos los usuarios estén conectados
- Sin conexiones redundantes (sin ciclos)
- Con el menor costo total posible

---

## Algoritmo de Kruskal

### ¿Qué hace Kruskal?

Kruskal construye el MST **agregando aristas de menor a mayor peso**, siempre que no formen un ciclo.

**¡Punto clave!** Kruskal **SIEMPRE elige la conexión más barata disponible** en cada paso. Primero forma "arbolitos" separados (componentes conexos) conectando las conexiones más baratas, y luego conecta esos arbolitos entre sí con las conexiones más baratas que los unan.

### ¿Cómo funciona?

**Paso a paso:**

1. **Ordenar TODAS las aristas por peso** (de menor a mayor) - esto es crucial
2. **Inicializar**: Cada vértice es su propio componente (están aislados, cada uno es un "arbolito")
3. **Iterar**: Para cada arista (de menor a mayor):
   - Si los dos vértices están en **componentes diferentes** → Agregar la arista al MST (conecta arbolitos)
   - Si están en el **mismo componente** → Ignorar (crearía un ciclo)
4. **Detener**: Cuando tengamos `n-1` aristas (donde `n` = número de vértices)

### Ejemplo detallado: Formando arbolitos

Imagina este grafo con estas conexiones:
- A ↔ B: costo **1** (la más barata)
- J ↔ Q: costo **3**
- Z ↔ K: costo **4**
- D ↔ E: costo **5**

**Proceso de Kruskal:**

**Estado inicial:** Cada vértice es su propio componente
- Arbolito 1: {A}
- Arbolito 2: {B}
- Arbolito 3: {J}
- Arbolito 4: {Q}
- Arbolito 5: {Z}
- Arbolito 6: {K}
- Arbolito 7: {D}
- Arbolito 8: {E}

**Paso 1:** Procesar A-B (costo 1) - **¡La más barata!**
- A y B están en componentes diferentes → ✅ **Agregar**
- Ahora tenemos: {A, B} (un arbolito más grande)
- Arbolitos restantes: {J}, {Q}, {Z}, {K}, {D}, {E}

**Paso 2:** Procesar J-Q (costo 3) - **La siguiente más barata**
- J y Q están en componentes diferentes → ✅ **Agregar**
- Ahora tenemos: {A, B} y {J, Q}
- Arbolitos restantes: {Z}, {K}, {D}, {E}

**Paso 3:** Procesar Z-K (costo 4)
- Z y K están en componentes diferentes → ✅ **Agregar**
- Ahora tenemos: {A, B}, {J, Q}, {Z, K}
- Arbolitos restantes: {D}, {E}

**Paso 4:** Procesar D-E (costo 5)
- D y E están en componentes diferentes → ✅ **Agregar**
- Ahora tenemos: {A, B}, {J, Q}, {Z, K}, {D, E}

**Si necesitamos conectar estos arbolitos**, Kruskal seguirá eligiendo las conexiones más baratas entre ellos. Por ejemplo:
- Si A-J cuesta 10 y B-Q cuesta 8, elegirá B-Q (8) porque es más barata
- Luego elegirá la siguiente más barata para conectar los arbolitos restantes

**Conclusión:** Kruskal **siempre** elige la conexión más barata disponible en cada momento, formando primero arbolitos pequeños y luego conectándolos con las conexiones más baratas posibles.

### Visualización con ejemplo:

```
Grafo inicial:
    A --3-- B
    |       |
    4       2
    |       |
    C --1-- D
```

**Paso 1:** Ordenar aristas: C-D (1), B-D (2), A-B (3), A-C (4)

**Paso 2:** Inicializar componentes:
- Componente 1: {A}
- Componente 2: {B}
- Componente 3: {C}
- Componente 4: {D}

**Paso 3:** Procesar aristas:

1. **C-D (peso 1)**: C y D están en componentes diferentes → ✅ Agregar
   - Componentes: {A}, {B}, {C, D}

2. **B-D (peso 2)**: B y D están en componentes diferentes → ✅ Agregar
   - Componentes: {A}, {B, C, D}

3. **A-B (peso 3)**: A y B están en componentes diferentes → ✅ Agregar
   - Componentes: {A, B, C, D} → ¡Todos conectados!

4. **A-C (peso 4)**: A y C están en el mismo componente → ❌ Ignorar (crearía ciclo)

**Resultado MST:**
```
    A --3-- B
            |
            2
            |
    C --1-- D
```

**Peso total: 1 + 2 + 3 = 6**

### Tu implementación de Kruskal

Mirando tu código en `KruskalMST.java`:

```java
// 1. Inicialización: Cada usuario es su propio componente
Map<Usuario, Usuario> indice = new HashMap<>();
for(Usuario v: grafo.getUsuarios()) {
    indice.put(v, v);  // Cada usuario apunta a sí mismo
}

// 2. Ordenar aristas por peso (PriorityQueue lo hace automáticamente)
PriorityQueue<Conexion> pq = new PriorityQueue<>();
// ... agregar todas las conexiones

// 3. Procesar aristas de menor a mayor
while(qtyUsuarios > 1) {
    Conexion conexionMasOptima = pq.poll();  // Obtiene la de menor peso
    
    // 4. Verificar si están en componentes diferentes
    if(origenYDestinoNotEquals(indice, conexionMasOptima)) {
        // Unificar componentes
        // Agregar conexión al resultado
    }
}
```

**Nota importante:** Tu implementación usa un enfoque de "unión" simple. Una versión más eficiente usaría **Union-Find (Disjoint Set Union)** para detectar componentes.

### Complejidad de Kruskal

- **Tiempo**: O(E log E) donde E = número de aristas
  - Ordenar aristas: O(E log E)
  - Procesar cada arista: O(E)
  - Verificar componentes: O(V) por arista en el peor caso
- **Espacio**: O(V + E)

---

## Algoritmo de Prim

### ¿Qué hace Prim?

Prim construye el MST **creciendo desde un vértice inicial**, agregando siempre la arista de menor peso que conecte un vértice ya incluido con uno que no esté incluido.

### ¿Cómo funciona?

**Paso a paso:**

1. **Elegir un vértice inicial** (cualquiera)
2. **Inicializar**: 
   - Conjunto de vértices incluidos: {vértice inicial}
   - Cola de prioridad con aristas desde el vértice inicial
3. **Iterar**: Mientras no todos los vértices estén incluidos:
   - Tomar la arista de **menor peso** que conecte un vértice incluido con uno no incluido
   - Agregar el nuevo vértice al conjunto
   - Agregar sus aristas a la cola de prioridad
4. **Detener**: Cuando todos los vértices estén incluidos

### Visualización con el mismo ejemplo:

```
Grafo inicial:
    A --3-- B
    |       |
    4       2
    |       |
    C --1-- D
```

**Paso 1:** Empezar desde A
- Vértices incluidos: {A}
- Aristas disponibles: A-B (3), A-C (4)

**Paso 2:** Elegir arista mínima: A-C (4) → Agregar C
- Vértices incluidos: {A, C}
- Aristas disponibles: A-B (3), C-D (1)

**Paso 3:** Elegir arista mínima: C-D (1) → Agregar D
- Vértices incluidos: {A, C, D}
- Aristas disponibles: A-B (3), B-D (2)

**Paso 4:** Elegir arista mínima: B-D (2) → Agregar B
- Vértices incluidos: {A, B, C, D} → ¡Completo!

**Resultado MST:**
```
    A        B
    |        |
    4        2
    |        |
    C --1-- D
```

**Peso total: 4 + 1 + 2 = 7**

*(Nota: Este resultado es diferente al de Kruskal porque empezamos desde A. Si empezáramos desde otro vértice, podríamos obtener el mismo resultado que Kruskal)*

### Pseudocódigo de Prim:

```
Prim(Grafo G, Vértice inicio):
    MST = nuevo grafo
    incluidos = {inicio}
    colaPrioridad = cola con aristas desde inicio
    
    mientras incluidos.size() < G.vertices.size():
        arista = colaPrioridad.extraerMinimo()
        si arista.destino no está en incluidos:
            agregar arista a MST
            agregar arista.destino a incluidos
            agregar aristas de arista.destino a colaPrioridad
    
    retornar MST
```

### Complejidad de Prim

- **Tiempo**: O(E log V) con heap binario, O(V²) con matriz de adyacencia
- **Espacio**: O(V + E)

---

## Diferencias Clave: Kruskal vs Prim

| Aspecto | **Kruskal** | **Prim** |
|---------|-------------|----------|
| **Enfoque** | Ordena todas las aristas y las agrega globalmente | Construye el árbol creciendo desde un vértice |
| **Estructura de datos** | PriorityQueue de aristas | PriorityQueue de aristas desde vértices incluidos |
| **Detección de ciclos** | Necesita verificar componentes (Union-Find) | No necesita (solo agrega vértices no incluidos) |
| **Vértice inicial** | No requiere vértice inicial | Requiere elegir un vértice inicial |
| **Mejor para** | Grafos dispersos (pocas aristas) | Grafos densos (muchas aristas) |
| **Complejidad típica** | O(E log E) | O(E log V) o O(V²) |
| **Resultado** | Siempre el mismo MST | Puede variar según el vértice inicial (pero mismo peso total) |

### Analogía simple:

- **Kruskal**: "Toma todas las conexiones, ordénalas por costo, y agrega las más baratas que no formen ciclos"
- **Prim**: "Empieza desde un usuario, y siempre agrega la conexión más barata que conecte a alguien nuevo"

---

## ¿Cuándo usar cada uno?

### Usa **Kruskal** cuando:
- ✅ El grafo es **disperso** (pocas aristas comparado con vértices)
- ✅ Ya tienes todas las aristas ordenadas
- ✅ No te importa desde dónde empezar
- ✅ Quieres una implementación más simple conceptualmente

### Usa **Prim** cuando:
- ✅ El grafo es **denso** (muchas aristas)
- ✅ Tienes un vértice inicial específico
- ✅ Quieres construir el árbol de forma incremental
- ✅ Prefieres trabajar con vértices en lugar de aristas globalmente

---

## En tu proyecto

Tu implementación actual usa **Kruskal** para encontrar la "red mínima" de conexiones entre usuarios. Esto tiene sentido porque:

1. Quieres el conjunto mínimo de conexiones para que todos estén conectados
2. No importa desde qué usuario empieces
3. Es útil para optimizar la red social eliminando conexiones redundantes

**Ejemplo de uso en tu código:**
```java
// En RedSocialServicio.java
public Grafo calcularRedMinima() {
    redMinima = KruskalMST.arbolDeRecubrimientoMinimo(grafoCompleto);
    return redMinima;
}
```

Esto calcula el conjunto mínimo de conexiones necesarias para que todos los usuarios estén conectados en la red social.

---

## Ejercicio para entender mejor

Dado este grafo:
```
    A --5-- B
    | \     |
    3  4    2
    |   \   |
    C --1-- D
```

**Con Kruskal:**
1. Ordenar: C-D(1), B-D(2), A-C(3), A-D(4), A-B(5)
2. Agregar: C-D, B-D, A-C → MST completo
3. Peso total: 1 + 2 + 3 = 6

**Con Prim (empezando desde A):**
1. Incluidos: {A}
2. Agregar: A-C(3) → {A, C}
3. Agregar: C-D(1) → {A, C, D}
4. Agregar: B-D(2) → {A, B, C, D}
5. Peso total: 3 + 1 + 2 = 6

¡Ambos dan el mismo peso total! (aunque pueden tener aristas diferentes)

---

## Resumen

- **Kruskal**: Ordena aristas globalmente, agrega las más baratas que no formen ciclos
- **Prim**: Construye el árbol creciendo desde un vértice, agregando siempre la conexión más barata a un vértice nuevo
- **Ambos** encuentran el mismo peso total del MST, pero pueden elegir aristas diferentes
- **Kruskal** es mejor para grafos dispersos, **Prim** para grafos densos
- Tu proyecto usa **Kruskal** para optimizar la red de conexiones entre usuarios

