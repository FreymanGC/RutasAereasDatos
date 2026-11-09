package rutasaereas.algoritmos;

import rutasaereas.modelo.Ruta;

/** Peso que usa Dijkstra: minutos (mas rapida) o costo (mas barata). */
public enum Criterio {
    MINUTOS,
    COSTO;

    public int peso(Ruta r) {
        return (this == MINUTOS) ? r.getMinutos() : r.getCosto();
    }
}
