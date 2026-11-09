package rutasaereas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rutasaereas.algoritmos.Busquedas;
import rutasaereas.algoritmos.Criterio;
import rutasaereas.algoritmos.ResultadoRuta;
import rutasaereas.modelo.CargadorDatos;
import rutasaereas.modelo.Grafo;
import rutasaereas.modelo.Ruta;

/** Programa de consola que imprime los datos para las preguntas del informe. */
public class AnalisisInforme {

    public static void main(String[] args) throws Exception {
        String archAero = args.length >= 2 ? args[0] : "aeropuertos.txt";
        String archRutas = args.length >= 2 ? args[1] : "rutas.txt";
        List<String> advertencias = new ArrayList<>();
        Grafo g = CargadorDatos.cargar(new File(archAero), new File(archRutas), advertencias);
        advertencias.forEach(a -> System.out.println("Aviso: " + a));
        System.out.println("Grafo: n = " + g.cantidadAeropuertos() + ", m = " + g.cantidadRutas());

        // Pregunta 1
        System.out.println("\n=== Pregunta 1 ===");
        String[][] viajes = {{"DEN", "SJO"}, {"LIR", "SJU"}, {"SJO", "MVD"}};
        for (String[] v : viajes) {
            int o = g.buscarIndice(v[0]), d = g.buscarIndice(v[1]);
            if (o == -1 || d == -1) {
                System.out.println(v[0] + " -> " + v[1] + ": algun codigo no existe en estos archivos");
                continue;
            }
            System.out.println(v[0] + " -> " + v[1]);
            imprimir(g, "  Menos escalas (BFS)", Busquedas.bfsMenosEscalas(g, o, d));
            imprimir(g, "  Mas rapida (Dijkstra min)", Busquedas.dijkstra(g, o, d, Criterio.MINUTOS));
            imprimir(g, "  Mas barata (Dijkstra $)", Busquedas.dijkstra(g, o, d, Criterio.COSTO));
        }

        int sjo = g.buscarIndice("SJO");
        if (sjo == -1) {
            System.out.println("\nNo hay SJO en estos archivos; se omiten las preguntas 3 y 4.");
        } else {
            // Pregunta 3
            System.out.println("\n=== Pregunta 3 ===");
            boolean[] desdeSjo = Busquedas.alcanzablesDesde(g, sjo);
            boolean[] hastaSjo = Busquedas.quienesPuedenLlegarA(g, sjo);
            System.out.println("No alcanzables desde SJO (DFS desde SJO): " + noMarcados(g, desdeSjo));
            System.out.println("No pueden llegar a SJO (DFS en el grafo inverso): " + noMarcados(g, hastaSjo));

            // Pregunta 4
            // Cancela cada ruta, repite el DFS desde SJO y la restaura
            System.out.println("\n=== Pregunta 4 ===");
            int antes = contar(desdeSjo);
            List<Ruta> todas = new ArrayList<>();
            for (int u = 0; u < g.cantidadAeropuertos(); u++) todas.addAll(g.getRutasSalientes(u));
            int criticas = 0;
            for (Ruta r : todas) {
                g.cancelarRuta(r.getOrigen(), r.getDestino());
                boolean[] despues = Busquedas.alcanzablesDesde(g, sjo);
                if (contar(despues) < antes) {
                    criticas++;
                    System.out.println("Cancelar " + cod(g, r.getOrigen()) + " " + cod(g, r.getDestino())
                            + " deja inalcanzable(s): " + perdidos(g, desdeSjo, despues));
                }
                g.restaurarTodas();
            }
            System.out.println("Rutas criticas encontradas: " + criticas + " de " + todas.size());
        }

        // Pregunta 5
        long n = g.cantidadAeropuertos(), m = g.cantidadRutas();
        System.out.println("\n=== Pregunta 5 ===");
        System.out.println("Matriz: n^2 = " + (n * n) + " celdas | Lista: n + m = " + (n + m) + " entradas");
        System.out.println("Red de 4000 con 20 rutas c/u: matriz = " + (4000L * 4000)
                + " celdas | lista = " + (4000 + 4000 * 20) + " entradas");
    }

    private static void imprimir(Grafo g, String titulo, ResultadoRuta r) {
        if (!r.existe()) {
            System.out.println(titulo + ": no existe ruta");
            return;
        }
        System.out.println(titulo + ": " + r.getSecuencia(g, "-") + " | " + r.getVuelos() + " vuelos | "
                + r.getMinutosTotales() + " min | $" + r.getCostoTotal());
    }

    private static String cod(Grafo g, int i) { return g.getAeropuerto(i).getCodigo(); }

    private static int contar(boolean[] marcas) {
        int c = 0;
        for (boolean b : marcas) if (b) c++;
        return c;
    }

    private static List<String> noMarcados(Grafo g, boolean[] marcas) {
        List<String> lista = new ArrayList<>();
        for (int i = 0; i < marcas.length; i++) if (!marcas[i]) lista.add(cod(g, i));
        return lista;
    }

    private static List<String> perdidos(Grafo g, boolean[] antes, boolean[] despues) {
        List<String> lista = new ArrayList<>();
        for (int i = 0; i < antes.length; i++) if (antes[i] && !despues[i]) lista.add(cod(g, i));
        return lista;
    }
}
