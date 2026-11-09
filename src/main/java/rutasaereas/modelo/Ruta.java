package rutasaereas.modelo;

/** Arista dirigida del grafo: un vuelo de origen a destino con minutos y costo. */
public class Ruta {

    private final int origen;    // indice del aeropuerto de salida
    private final int destino;   // indice del aeropuerto de llegada
    private final int minutos;
    private final int costo;
    private final int orden;     // posicion en rutas.txt (para restaurar en el mismo orden)

    public Ruta(int origen, int destino, int minutos, int costo, int orden) {
        this.origen = origen;
        this.destino = destino;
        this.minutos = minutos;
        this.costo = costo;
        this.orden = orden;
    }

    public int getOrigen() { return origen; }
    public int getDestino() { return destino; }
    public int getMinutos() { return minutos; }
    public int getCosto() { return costo; }
    public int getOrden() { return orden; }
}
