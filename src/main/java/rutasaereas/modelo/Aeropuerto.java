package rutasaereas.modelo;

/** Vertice del grafo: un aeropuerto leido de aeropuertos.txt. */
public class Aeropuerto {

    private final String codigo;  // codigo IATA, ej. "SJO"
    private final int x;          // posicion en el mapa de 1000 x 1000
    private final int y;
    private final String ciudad;
    private final String pais;

    public Aeropuerto(String codigo, int x, int y, String ciudad, String pais) {
        this.codigo = codigo;
        this.x = x;
        this.y = y;
        this.ciudad = ciudad;
        this.pais = pais;
    }

    public String getCodigo() { return codigo; }
    public int getX() { return x; }
    public int getY() { return y; }

    // En el archivo los espacios vienen como '_'
    public String getCiudad() { return ciudad.replace('_', ' '); }
    public String getPais() { return pais.replace('_', ' '); }

    @Override
    public String toString() { return codigo; }
}
