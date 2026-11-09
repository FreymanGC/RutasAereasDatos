package rutasaereas.algoritmos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;
import rutasaereas.modelo.Grafo;
import rutasaereas.modelo.Ruta;

/** BFS, Dijkstra y DFS implementados a mano. */
public final class Busquedas {

    private Busquedas() { }

    /** BFS: ruta con menos vuelos. O(n + m). */
    public static ResultadoRuta bfsMenosEscalas(Grafo grafo, int origen, int destino) {
        if (origen == destino) {
            return ResultadoRuta.conTramos(origen, destino, new ArrayList<>());
        }
        int n = grafo.cantidadAeropuertos();
        boolean[] visitado = new boolean[n];
        Ruta[] llegoPor = new Ruta[n];          // ruta con la que se llego a cada aeropuerto
        ArrayDeque<Integer> cola = new ArrayDeque<>();

        visitado[origen] = true;
        cola.add(origen);

        while (!cola.isEmpty() && !visitado[destino]) {
            int actual = cola.poll();
            for (Ruta r : grafo.getRutasSalientes(actual)) {
                int vecino = r.getDestino();
                if (!visitado[vecino]) {
                    visitado[vecino] = true;
                    llegoPor[vecino] = r;
                    cola.add(vecino);
                }
            }
        }

        if (!visitado[destino]) {
            return ResultadoRuta.sinRuta(origen, destino);
        }
        return ResultadoRuta.conTramos(origen, destino, reconstruir(origen, destino, llegoPor));
    }

    /** Dijkstra: ruta de menor peso segun el criterio (minutos o costo). O((n + m) log n). */
    public static ResultadoRuta dijkstra(Grafo grafo, int origen, int destino, Criterio criterio) {
        if (origen == destino) {
            return ResultadoRuta.conTramos(origen, destino, new ArrayList<>());
        }
        int n = grafo.cantidadAeropuertos();
        long[] distancia = new long[n];
        Arrays.fill(distancia, Long.MAX_VALUE);   // infinito
        boolean[] fijado = new boolean[n];
        Ruta[] llegoPor = new Ruta[n];

        // Elementos {distancia, aeropuerto}; sale primero la menor distancia
        PriorityQueue<long[]> cola = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        distancia[origen] = 0;
        cola.add(new long[]{0, origen});

        while (!cola.isEmpty()) {
            int actual = (int) cola.poll()[1];
            if (fijado[actual]) {
                continue;            // copia vieja en la cola
            }
            fijado[actual] = true;
            if (actual == destino) {
                break;
            }
            for (Ruta r : grafo.getRutasSalientes(actual)) {
                int vecino = r.getDestino();
                long nueva = distancia[actual] + criterio.peso(r);
                if (nueva < distancia[vecino]) {          // se encontro un camino mejor
                    distancia[vecino] = nueva;
                    llegoPor[vecino] = r;
                    cola.add(new long[]{nueva, vecino});
                }
            }
        }

        if (distancia[destino] == Long.MAX_VALUE) {
            return ResultadoRuta.sinRuta(origen, destino);
        }
        return ResultadoRuta.conTramos(origen, destino, reconstruir(origen, destino, llegoPor));
    }

    /** Arma el camino yendo del destino hacia el origen. */
    private static LinkedList<Ruta> reconstruir(int origen, int destino, Ruta[] llegoPor) {
        LinkedList<Ruta> tramos = new LinkedList<>();
        int actual = destino;
        while (actual != origen) {
            Ruta r = llegoPor[actual];
            tramos.addFirst(r);
            actual = r.getOrigen();
        }
        return tramos;
    }

    /** DFS: marca los aeropuertos a los que se puede llegar desde origen. O(n + m). */
    public static boolean[] alcanzablesDesde(Grafo grafo, int origen) {
        boolean[] visitado = new boolean[grafo.cantidadAeropuertos()];
        dfs(grafo, origen, visitado);
        return visitado;
    }

    private static void dfs(Grafo grafo, int actual, boolean[] visitado) {
        visitado[actual] = true;
        for (Ruta r : grafo.getRutasSalientes(actual)) {
            if (!visitado[r.getDestino()]) {
                dfs(grafo, r.getDestino(), visitado);
            }
        }
    }

    /** Marca los aeropuertos que pueden llegar a destino (DFS sobre el grafo inverso). */
    public static boolean[] quienesPuedenLlegarA(Grafo grafo, int destino) {
        int n = grafo.cantidadAeropuertos();
        // Grafo inverso: cada A->B se guarda como B->A
        ArrayList<ArrayList<Integer>> inverso = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            inverso.add(new ArrayList<>());
        }
        for (int u = 0; u < n; u++) {
            for (Ruta r : grafo.getRutasSalientes(u)) {
                inverso.get(r.getDestino()).add(u);
            }
        }
        boolean[] visitado = new boolean[n];
        dfsInverso(inverso, destino, visitado);
        return visitado;
    }

    private static void dfsInverso(ArrayList<ArrayList<Integer>> inverso, int actual, boolean[] visitado) {
        visitado[actual] = true;
        for (int vecino : inverso.get(actual)) {
            if (!visitado[vecino]) {
                dfsInverso(inverso, vecino, visitado);
            }
        }
    }
}
