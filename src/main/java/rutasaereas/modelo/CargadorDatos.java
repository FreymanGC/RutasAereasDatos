package rutasaereas.modelo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Lee aeropuertos.txt y rutas.txt y construye el grafo. */
public final class CargadorDatos {

    private CargadorDatos() { }

    /** Crea el grafo desde los dos archivos; los problemas menores van a advertencias. */
    public static Grafo cargar(File archivoAeropuertos, File archivoRutas,
                               List<String> advertencias) throws IOException {
        Grafo grafo = new Grafo();
        // Primero aeropuertos, porque las rutas usan sus codigos
        leerAeropuertos(grafo, archivoAeropuertos, advertencias);
        leerRutas(grafo, archivoRutas, advertencias);
        return grafo;
    }

    private static void leerAeropuertos(Grafo grafo, File archivo,
                                        List<String> advertencias) throws IOException {
        try (Scanner sc = abrir(archivo)) {
            int cantidad = leerCantidad(sc, archivo);
            for (int i = 1; i <= cantidad; i++) {
                try {
                    String codigo = sc.next().toUpperCase();
                    int x = sc.nextInt();
                    int y = sc.nextInt();
                    String ciudad = sc.next();
                    String pais = sc.next();
                    if (!grafo.agregarAeropuerto(new Aeropuerto(codigo, x, y, ciudad, pais))) {
                        advertencias.add("Aeropuerto repetido ignorado: " + codigo);
                    }
                } catch (NoSuchElementException e) {
                    throw new IOException(archivo.getName() + ": el aeropuerto numero " + i
                            + " esta incompleto o tiene un dato mal escrito.");
                }
            }
        }
    }

    private static void leerRutas(Grafo grafo, File archivo,
                                  List<String> advertencias) throws IOException {
        try (Scanner sc = abrir(archivo)) {
            int cantidad = leerCantidad(sc, archivo);
            for (int i = 1; i <= cantidad; i++) {
                String codOrigen, codDestino;
                int minutos, costo;
                try {
                    codOrigen = sc.next();
                    codDestino = sc.next();
                    minutos = sc.nextInt();
                    costo = sc.nextInt();
                } catch (NoSuchElementException e) {
                    throw new IOException(archivo.getName() + ": la ruta numero " + i
                            + " esta incompleta o tiene un dato mal escrito.");
                }

                int origen = grafo.buscarIndice(codOrigen);
                int destino = grafo.buscarIndice(codDestino);
                if (origen == -1 || destino == -1) {
                    advertencias.add("Ruta ignorada (codigo inexistente): " + codOrigen + " " + codDestino);
                    continue;
                }
                if (minutos < 0 || costo < 0) {
                    // Dijkstra necesita pesos >= 0
                    advertencias.add("Ruta con peso negativo: " + codOrigen + " " + codDestino
                            + ". Dijkstra no garantiza el resultado con pesos negativos.");
                }
                grafo.agregarRuta(new Ruta(origen, destino, minutos, costo, i));
            }
        }
    }

    private static Scanner abrir(File archivo) throws IOException {
        if (!archivo.isFile()) {
            throw new IOException("No se encontro el archivo: " + archivo.getAbsolutePath());
        }
        return new Scanner(archivo, StandardCharsets.UTF_8);
    }

    /** Lee el primer numero del archivo: cuantos registros siguen. */
    private static int leerCantidad(Scanner sc, File archivo) throws IOException {
        if (!sc.hasNextInt()) {
            throw new IOException(archivo.getName() + ": la primera linea debe ser la cantidad de registros.");
        }
        int cantidad = sc.nextInt();
        if (cantidad < 0) {
            throw new IOException(archivo.getName() + ": la cantidad no puede ser negativa.");
        }
        return cantidad;
    }
}
