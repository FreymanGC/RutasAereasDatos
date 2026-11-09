package rutasaereas.algoritmos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rutasaereas.modelo.Grafo;
import rutasaereas.modelo.Ruta;

/** Resultado de BFS o Dijkstra: los tramos del viaje y sus totales. */
public class ResultadoRuta {

    private final int origen;
    private final int destino;
    private final boolean existe;
    private final List<Ruta> tramos;

    private ResultadoRuta(int origen, int destino, boolean existe, List<Ruta> tramos) {
        this.origen = origen;
        this.destino = destino;
        this.existe = existe;
        this.tramos = Collections.unmodifiableList(tramos);
    }

    public static ResultadoRuta conTramos(int origen, int destino, List<Ruta> tramos) {
        return new ResultadoRuta(origen, destino, true, tramos);
    }

    public static ResultadoRuta sinRuta(int origen, int destino) {
        return new ResultadoRuta(origen, destino, false, new ArrayList<>());
    }

    public int getOrigen() { return origen; }
    public int getDestino() { return destino; }
    public boolean existe() { return existe; }
    public List<Ruta> getTramos() { return tramos; }

    public int getVuelos() {
        return tramos.size();
    }

    public int getMinutosTotales() {
        int total = 0;
        for (Ruta r : tramos) total += r.getMinutos();
        return total;
    }

    public int getCostoTotal() {
        int total = 0;
        for (Ruta r : tramos) total += r.getCosto();
        return total;
    }

    /** Codigos del viaje, ej. "SJO -> PTY -> LIM". */
    public String getSecuencia(Grafo grafo, String separador) {
        StringBuilder sb = new StringBuilder(grafo.getAeropuerto(origen).getCodigo());
        for (Ruta r : tramos) {
            sb.append(separador).append(grafo.getAeropuerto(r.getDestino()).getCodigo());
        }
        return sb.toString();
    }
}
