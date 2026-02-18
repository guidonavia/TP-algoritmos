# Documentacion de Algoritmos - TP Red Social

Este documento explica como funcionan los tres algoritmos implementados en el proyecto,
como esta estructurado el codigo y que casos de prueba cubren.

---

## Estructura del Proyecto

```
src/main/java/org/uade/progra3/
├── grafos/
│   ├── Grafo.java              Estructura de datos del grafo (lista de adyacencia)
│   ├── Djikstra.java           Algoritmo de Dijkstra
│   └── KruskalMST.java         Algoritmo de Kruskal
├── modelo/
│   ├── Usuario.java            Vertice del grafo (id, nombre)
│   ├── Conexion.java           Arista ponderada (origen, destino, peso)
│   ├── Publicacion.java        Item de la mochila (likes, comentarios, tamanio)
│   ├── Portada.java            Contenedor de publicaciones seleccionadas
│   ├── CandidatoPublicaciones.java  Wrapper de publicaciones candidatas
│   ├── Like.java               Modelo de like
│   └── Comentario.java         Modelo de comentario
├── negocio/
│   └── PortadaDinamica.java    Programacion Dinamica (Mochila 0/1)
├── servicio/
│   └── RedSocialServicio.java  Orquestador de los tres algoritmos
└── ui/
    ├── RedSocialFrame.java     Interfaz grafica (3 pestanias)
    └── GrafoDiagramPanel.java  Dibujado de grafos

src/test/java/org/uade/progra3/
├── grafos/
│   ├── DjikstraTest.java       8 tests
│   └── KruskalMSTTest.java     7 tests
└── negocio/
    └── PortadaDinamicaTest.java  12 tests
```

### Estructura de datos: Grafo

Archivo: `grafos/Grafo.java`

El grafo es **dirigido y ponderado**. Internamente usa tres colecciones:

| Estructura | Tipo | Proposito |
|---|---|---|
| `usuarios` | `Set<Usuario>` | Conjunto de vertices, lookup O(1) |
| `conexiones` | `List<Conexion>` | Lista global de aristas |
| `adyacencias` | `Map<Usuario, List<Conexion>>` | Lista de adyacencia por vertice |

Metodos principales:
- `agregarUsuario(Usuario v)` - agrega un vertice
- `agregarConexion(Usuario origen, Usuario destino, int peso)` - agrega una arista dirigida
- `getAdyacentes(Usuario v)` - devuelve las aristas salientes de un vertice
- `existeConexion(origen, destino)` - verifica si existe arista entre dos vertices
- `getPesoConexion(origen, destino)` - obtiene el peso de una arista

La clase `Conexion` implementa `Comparable<Conexion>` comparando por peso, lo que
permite usar `PriorityQueue` directamente en Kruskal.

---

## 1. Algoritmo de Dijkstra

**Archivo:** `grafos/Djikstra.java`
**Metodo principal:** `calcularCaminosMinimos(Grafo grafo, Usuario origen)`
**Retorna:** `Map<Usuario, Integer>` - distancia minima desde el origen a cada usuario

### Proposito en la red social

Calcular el grado de conexion entre usuarios. Permite saber a cuantos "saltos"
(ponderados) esta un usuario de otro, util para recomendar amistades.

### Como funciona paso a paso

```
1. INICIALIZACION
   - Para cada usuario en el grafo: distancia = INFINITO (MAX_VALUE)
   - distancia[origen] = 0
   - visitados = conjunto vacio

2. BUCLE PRINCIPAL (mientras haya no visitados)
   a. Seleccionar el usuario NO visitado con menor distancia
      -> metodo obtenerUsuarioConMenorDistancia()
      -> si es null, no hay mas alcanzables, terminamos

   b. Marcarlo como visitado

   c. Para cada conexion adyacente del usuario actual:
      - vecino = destino de la conexion
      - Si el vecino NO fue visitado:
        - nuevaDistancia = distancia[actual] + peso(conexion)
        - Si nuevaDistancia < distancia[vecino]:
          - distancia[vecino] = nuevaDistancia  (relajacion)

3. Devolver el mapa de distancias
```

### Ejemplo visual

```
Grafo:  A --1--> B --2--> C --3--> D
        \________4_________↗

Desde A:
  - A = 0
  - B = 1  (directo)
  - C = 3  (A->B->C = 1+2)
  - D = 4  (A->D = 4, que es menor que A->B->C->D = 1+2+3 = 6)
```

### Complejidad

| | Complejidad |
|---|---|
| Tiempo | O(V^2) donde V = cantidad de vertices |
| Espacio | O(V) |

La implementacion usa busqueda lineal para encontrar el minimo (`obtenerUsuarioConMenorDistancia`
recorre todo el mapa). Una optimizacion posible seria usar `PriorityQueue` para reducir
a O((V + E) log V).

### Codigo clave (lineas 13-54)

```java
// Inicializacion: todos a infinito, origen a 0
for (Usuario v : grafo.getUsuarios()) {
    distancia.put(v, Integer.MAX_VALUE);
}
distancia.put(origen, 0);

// Bucle: seleccionar minimo, relajar vecinos
while (visitados.size() < grafo.getUsuarios().size()) {
    Usuario actual = obtenerUsuarioConMenorDistancia(distancia, visitados);
    if (actual == null) break;  // no hay mas alcanzables
    visitados.add(actual);

    for (Conexion conexion : grafo.getAdyacentes(actual)) {
        Usuario vecino = conexion.getDestino();
        if (!visitados.contains(vecino)) {
            int nuevaDistancia = distancia.get(actual) + conexion.getPeso();
            if (nuevaDistancia < distancia.get(vecino)) {
                distancia.put(vecino, nuevaDistancia);  // relajacion
            }
        }
    }
}
```

### Casos de prueba (DjikstraTest.java - 8 tests)

| Test | Que verifica |
|---|---|
| `origenDistanciaCero` | El nodo origen siempre tiene distancia 0 |
| `vecinoDirectoPesoArista` | Un vecino directo tiene como distancia el peso de la arista |
| `caminoMinimoCadena` | Calcula correctamente caminos multi-salto y elige el atajo cuando es mejor (C=3, D=4) |
| `desdeB` | Desde otro origen: B=0, C=2, D=5, A=inalcanzable (grafo dirigido) |
| `unSoloNodo` | Grafo con un solo nodo devuelve distancia 0 a si mismo |
| `dosNodosConectados` | Dos nodos con arista peso 7 devuelve distancia correcta |
| `nodoInalcanzable` | Un nodo sin camino desde el origen tiene distancia MAX_VALUE |
| `unaEntradaPorVertice` | El mapa resultado contiene exactamente una entrada por cada vertice del grafo |

---

## 2. Algoritmo de Kruskal (MST)

**Archivo:** `grafos/KruskalMST.java`
**Metodo principal:** `arbolDeRecubrimientoMinimo(Grafo grafo)`
**Retorna:** `Grafo` - nuevo grafo que contiene solo las aristas del arbol de recubrimiento minimo

### Proposito en la red social

Encontrar la forma mas economica (menor peso total) de conectar a todos los usuarios.
Si las aristas representan costo de comunicacion, el MST da la red de costo minimo que
mantiene a todos conectados.

### Como funciona paso a paso

```
1. INICIALIZACION
   - Crear grafo resultado (vacio)
   - Para cada usuario: agregarlo al resultado
   - Inicializar indice de componentes: cada usuario es su propia componente
     indice[usuario] = usuario  (se apunta a si mismo)
   - contadorComponentes = cantidad de usuarios

2. COLA DE PRIORIDAD
   - Recorrer todos los pares de usuarios
   - Si existe conexion entre ellos, agregarla a la PriorityQueue
   - La PQ ordena automaticamente por peso (Conexion implementa Comparable)

3. BUCLE PRINCIPAL (mientras haya mas de 1 componente)
   a. Extraer la arista de menor peso de la PQ (poll)
   b. Verificar si origen y destino estan en componentes distintas:
      -> origenYDestinoNotEquals(indice, conexion)
      -> Compara indice[origen] vs indice[destino]

   c. Si estan en DISTINTA componente:
      - Decrementar contadorComponentes
      - Obtener la raiz de cada componente
      - Unificar: todos los vertices de la componente del origen
        pasan a apuntar a la componente del destino
      - Agregar la arista al grafo resultado

   d. Si estan en la MISMA componente:
      - Descartar la arista (agregarla crearia un ciclo)

4. Devolver el grafo resultado
```

### Ejemplo visual

```
Grafo original:
  A --1-- B --2-- C --3-- D
   \______4______/

Proceso de Kruskal (aristas ordenadas por peso):
  1. A-B (peso 1): A y B en componentes distintas -> AGREGAR. Unir {A,B}
  2. B-C (peso 2): {A,B} y {C} distintas -> AGREGAR. Unir {A,B,C}
  3. C-D (peso 3): {A,B,C} y {D} distintas -> AGREGAR. Unir {A,B,C,D}
  4. A-C (peso 4): {A,B,C,D} misma componente -> RECHAZAR (crearia ciclo)

MST resultado: A-B(1), B-C(2), C-D(3) -> Peso total = 6
```

### Manejo de componentes (Union-Find simplificado)

El algoritmo usa un `Map<Usuario, Usuario> indice` como estructura Union-Find:

```java
// Inicializacion: cada uno es su propia componente
for (Usuario v : grafo.getUsuarios()) {
    indice.put(v, v);
}

// Verificar si estan en la misma componente
private static boolean origenYDestinoNotEquals(Map<Usuario, Usuario> indice, Conexion c) {
    Usuario v1 = indice.get(c.getOrigen());
    Usuario v2 = indice.get(c.getDestino());
    return !v1.equals(v2);
}

// Unificar componentes: todos los del origen pasan al destino
Usuario origen = indice.get(conexion.getOrigen());
Usuario destino = indice.get(conexion.getDestino());
for (Usuario v : indice.keySet()) {
    if (indice.get(v).equals(origen)) {
        indice.put(v, destino);
    }
}
```

### Complejidad

| | Complejidad |
|---|---|
| Tiempo | O(E log E + V^2) - E log E por la PQ, V^2 por la unificacion de componentes |
| Espacio | O(V + E) |

La unificacion de componentes recorre todos los vertices en cada union (O(V) por union).
Una optimizacion posible seria usar Union-Find con compresion de caminos y union por rango
para reducir a O(E log E).

### Casos de prueba (KruskalMSTTest.java - 7 tests)

| Test | Que verifica |
|---|---|
| `mstTieneNMenos1Aristas` | Un MST con n vertices tiene exactamente n-1 aristas (4 nodos = 3 aristas) |
| `mstPesoTotalMinimo` | El peso total del MST es el minimo conocido (6) |
| `mstContieneTodosLosVertices` | El MST incluye todos los vertices del grafo original |
| `grafoUnNodo_sinAristas` | Un grafo de 1 nodo produce MST sin aristas |
| `grafoDosNodos_unaArista` | Dos nodos conectados producen MST con 1 arista |
| `rechazaAristaQueCreariaCiclo` | En un triangulo A-B-C, rechaza la arista A-C porque crearia un ciclo |
| `eligeAristaMasLiviana` | Cuando hay multiples opciones, siempre elige la de menor peso (A-B(1)+B-C(2) en vez de A-C(100)) |

---

## 3. Programacion Dinamica - Portada Optima (Mochila 0/1)

**Archivo:** `negocio/PortadaDinamica.java`
**Metodo principal:** `obtenerPublicaciones(CandidatoPublicaciones feed, Portada portada)`
**Efecto:** Llena la portada con las publicaciones que maximizan el beneficio sin exceder la capacidad

### Proposito en la red social

Seleccionar las publicaciones para la portada (feed) del usuario que maximicen el
engagement (likes + comentarios) sin exceder el espacio disponible. Es una variante
clasica del **Problema de la Mochila 0/1** (Knapsack Problem).

### Modelo del problema

| Concepto de Mochila | Equivalente en la Red Social |
|---|---|
| Mochila | Portada (capacidad maxima = 100 unidades) |
| Objeto | Publicacion |
| Peso del objeto | `publicacion.getTamanio()` |
| Valor del objeto | `publicacion.ponderar()` = comentarios * 10 + likes * 2 |
| Objetivo | Maximizar beneficio total sin exceder capacidad |

### Como funciona paso a paso

```
1. VALIDACION
   - Si el feed es null o vacio: limpiar la portada y retornar

2. CREAR TABLA DP
   - Dimensiones: (n+1) filas x (capacidad+1) columnas
   - beneficioMaximoHasta[i][w] = maximo beneficio alcanzable
     usando las primeras i publicaciones con capacidad w
   - Fila 0 = caso base (0 publicaciones = beneficio 0)

3. LLENAR LA TABLA (dos bucles anidados)
   Para cada publicacion i (1 a n):
     Para cada espacio w (0 a capacidad):
       a. Opcion 1 - NO incluir publicacion i:
          beneficioSinIncluir = tabla[i-1][w]
          tabla[i][w] = beneficioSinIncluir  (por defecto)

       b. Opcion 2 - INCLUIR publicacion i (si cabe):
          Si tamanio <= w:
            beneficioIncluyendo = tabla[i-1][w - tamanio] + beneficio
            Si beneficioIncluyendo > beneficioSinIncluir:
              tabla[i][w] = beneficioIncluyendo

4. RECONSTRUIR SOLUCION (volcarSolucionEnPortada)
   - Limpiar la portada
   - Partir desde tabla[n][capacidadMaxima]
   - Para cada publicacion i (de n a 1):
     Si tabla[i][espacioRestante] != tabla[i-1][espacioRestante]:
       -> La publicacion i fue seleccionada
       -> Agregarla a la portada (en posicion 0 para mantener orden)
       -> Restar su tamanio del espacioRestante
```

### Ejemplo visual

```
Publicaciones candidatas:
  A: beneficio=60, tamanio=50
  B: beneficio=50, tamanio=50
  C: beneficio=50, tamanio=50
Capacidad: 100

Opciones posibles:
  Solo A:   beneficio=60,  tamanio=50   ✗ no es optimo
  Solo B:   beneficio=50,  tamanio=50   ✗
  Solo C:   beneficio=50,  tamanio=50   ✗
  A + B:    beneficio=110, tamanio=100  ✓ OPTIMO (cabe justo)
  A + C:    beneficio=110, tamanio=100  ✓ tambien optimo
  B + C:    beneficio=100, tamanio=100  ✗ no es optimo
  A + B + C: tamanio=150 > 100         ✗ no cabe

Resultado: se seleccionan A + B (o A + C) con beneficio total = 110
```

### Tabla DP para el ejemplo

```
         Espacio: 0   10  20  30  40  50  60  70  80  90  100
Pub 0 (base):    0    0   0   0   0   0   0   0   0   0    0
Pub A (60,50):   0    0   0   0   0  60  60  60  60  60   60
Pub B (50,50):   0    0   0   0   0  60  60  60  60  60  110
Pub C (50,50):   0    0   0   0   0  60  60  60  60  60  110
```

Reconstruccion desde [3][100]:
- tabla[3][100]=110 vs tabla[2][100]=110 -> iguales -> C NO seleccionada
- tabla[2][100]=110 vs tabla[1][100]=60  -> diferentes -> B SELECCIONADA, espacio=100-50=50
- tabla[1][50]=60   vs tabla[0][50]=0    -> diferentes -> A SELECCIONADA, espacio=50-50=0

Resultado: {A, B} con beneficio 110.

### Complejidad

| | Complejidad |
|---|---|
| Tiempo | O(n * W) donde n = publicaciones, W = capacidad (100) |
| Espacio | O(n * W) por la tabla bidimensional |

Como W es constante (100), la complejidad efectiva es O(n).

### Codigo clave (lineas 34-51)

```java
for (int indicePub = 1; indicePub <= cantidadPublicaciones; indicePub++) {
    Publicacion publicacion = publicaciones.get(indicePub - 1);
    int beneficio = publicacion.ponderar();
    int tamanio = publicacion.getTamanio();

    for (int espacio = 0; espacio <= espacioMaximoPortada; espacio++) {
        int beneficioSinIncluir = beneficioMaximoHasta[indicePub - 1][espacio];
        beneficioMaximoHasta[indicePub][espacio] = beneficioSinIncluir;

        boolean cabeEnElEspacio = tamanio <= espacio;
        if (cabeEnElEspacio) {
            int espacioRestante = espacio - tamanio;
            int beneficioIncluyendo = beneficioMaximoHasta[indicePub - 1][espacioRestante] + beneficio;
            if (beneficioIncluyendo > beneficioSinIncluir) {
                beneficioMaximoHasta[indicePub][espacio] = beneficioIncluyendo;
            }
        }
    }
}
```

### Reconstruccion (lineas 66-80)

```java
portada.getPublicaciones().clear();
int espacioRestante = espacioMaximoPortada;
for (int indicePub = cantidadPublicaciones; indicePub >= 1; indicePub--) {
    if (beneficioMaximoHasta[indicePub][espacioRestante]
            != beneficioMaximoHasta[indicePub - 1][espacioRestante]) {
        Publicacion publicacion = publicaciones.get(indicePub - 1);
        portada.getPublicaciones().add(0, publicacion);  // add(0,...) preserva orden original
        espacioRestante -= publicacion.getTamanio();
    }
}
```

### Casos de prueba (PortadaDinamicaTest.java - 12 tests)

| Test | Que verifica |
|---|---|
| `feedListadoNull_clearsPortada` | Feed null limpia la portada |
| `feedListadoEmpty_clearsPortada` | Feed vacio limpia la portada |
| `singlePublicationThatFits_selected` | Una publicacion que cabe es seleccionada |
| `singlePublicationTooLarge_portadaEmpty` | Una publicacion que no cabe deja la portada vacia |
| `twoPublicationsBothFit_bothSelected` | Dos publicaciones que caben juntas son seleccionadas |
| `twoPublicationsOnlyOneFits_oneSelected` | De dos publicaciones, solo la que cabe es seleccionada |
| `knapsackOptimalSubsetSelected` | Selecciona el subconjunto de mayor beneficio (A=60+B=50 > A=60 sola) |
| `publicationLargerThanSpace_notIncluded` | Elige la de mayor beneficio aunque la otra sea mas chica |
| `includeNotBetter_keepsPreviousValue` | No reemplaza una buena solucion por una peor |
| `backtrackingSkipsNonSelected` | El backtracking omite items que no forman parte de la solucion |
| `portadaClearedBeforeFill` | La portada se limpia antes de llenarse con la nueva solucion |
| `orderPreserved` | El orden original de las publicaciones se preserva en el resultado |

---

## Resumen comparativo

| Aspecto | Dijkstra | Kruskal | Prog. Dinamica |
|---|---|---|---|
| **Tipo** | Caminos minimos | Arbol recubrimiento minimo | Mochila 0/1 |
| **Estructura** | Grafo dirigido ponderado | Grafo no dirigido ponderado | Lista de items |
| **Tecnica** | Greedy (relajacion) | Greedy (arista minima) | Tabla DP bottom-up |
| **Complejidad** | O(V^2) | O(E log E + V^2) | O(n * W) |
| **Uso en red social** | Grado de conexion | Red de costo minimo | Feed optimo |
| **Tests** | 8 | 7 | 12 |
| **Total tests** | **27** | | |

---

## Como ejecutar los tests

```bash
mvn test
```

Los tests usan JUnit 5 con anotaciones `@DisplayName` para nombres descriptivos
y `@Nested` para agrupar tests por funcionalidad.
