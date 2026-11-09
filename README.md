# Proyecto II — Rutas aéreas (EIF207 Estructuras de Datos)

Planificador de vuelos entre aeropuertos de América usando un grafo dirigido y ponderado.

- **Lenguaje:** Java 17 o posterior
- **Entorno:** Apache NetBeans con maven
- **Biblioteca gráfica:** Swing (incluida en el JDK, sin dependencias externas)
- **Colecciones usadas:** `ArrayList`, `HashMap`, `ArrayDeque`, `PriorityQueue` (java.util).
  BFS, DFS y Dijkstra están implementados a mano.

## Estructura

```
src/main/java/rutasaereas/
├── Main.java                 Abre la ventana
├── AnalisisInforme.java      Imprime en consola los datos de la sección 8 (informe)
├── modelo/                   GRAFO Y CARGA
│   ├── Aeropuerto.java       Vértice
│   ├── Ruta.java             Arista dirigida (minutos y costo)
│   ├── Grafo.java            Lista de adyacencia, cancelar/restaurar rutas
│   └── CargadorDatos.java    Lee aeropuertos.txt y rutas.txt
├── algoritmos/               ALGORITMOS
│   ├── Busquedas.java        BFS, Dijkstra, DFS (alcanzables y grafo inverso)
│   ├── Criterio.java         Peso que usa Dijkstra: MINUTOS o COSTO
│   └── ResultadoRuta.java    Tramos de una ruta y sus totales
└── vista/                    INTERFAZ
    ├── VentanaPrincipal.java Controles, panel de resultados
    ├── PanelMapa.java        Dibujo del mapa, clics
    ├── FondoMapa.java        Fondo decorativo (continentes)
    ├── Curva.java            Arco (Bézier) con el que se dibuja cada ruta
    ├── Tema.java             Colores del mapa: modo día y modo noche
    └── AnimadorVuelo.java    Animación del avión y su estela de humo (Timer)

src/main/resources/rutasaereas/vista/continentes.txt   Contorno de los continentes
```

### Fondo del mapa
El fondo es solo decorativo y no forma parte de los datos del grafo. Las costas vienen de
Natural Earth (dominio público, a través del paquete `world-atlas`) y se proyectaron al mismo
sistema 0..1000 de `aeropuertos.txt` (x = 11.2193·lon + 1408.77 ; y = −10.6963·lat + 553.52).
Si se cargan archivos con otras coordenadas, el fondo se puede apagar con la casilla
**"Mostrar mapa de fondo"**. La casilla **"Modo noche"** cambia entre los colores de día y de noche.

## Cómo compilar y ejecutar

**En NetBeans:** `File → Open Project`, elegir la carpeta `RutasAereas` (la que tiene `pom.xml`),
luego `Clean and Build` (Shift+F11) y `Run` (F6).

**En consola** (con Maven instalado):
```
mvn package
java -jar target/RutasAereas-1.0.jar
```

Los archivos `aeropuertos.txt` y `rutas.txt` se buscan en la carpeta desde donde se ejecuta
(la raíz del proyecto). Si no están, el programa pide elegirlos. El botón
**"Cargar otros archivos..."** permite probar con cualquier par de archivos del mismo formato.

## Uso

1. Elegir origen y destino: escribiendo el código, con la lista o con clic en el mapa
   (primer clic = origen, segundo = destino).
2. Elegir el criterio (menos escalas, más rápida o más barata) y presionar **Buscar ruta**.
3. El avión recorre la ruta; **Pausar/Continuar** y **Reiniciar** controlan la animación.
4. **Alcanzables** colorea los aeropuertos alcanzables (verde) y no alcanzables (rojo) desde el origen.
5. **Cancelar ruta** quita la ruta directa origen → destino (se dibuja roja punteada);
   **Restaurar todas** las devuelve.
