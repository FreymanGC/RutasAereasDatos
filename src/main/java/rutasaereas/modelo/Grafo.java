package rutasaereas.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/** Grafo dirigido y ponderado guardado con lista de adyacencia. */
public class Grafo {

    private final ArrayList<Aeropuerto> aeropuertos = new ArrayList<>();      // vertices
    private final ArrayList<ArrayList<Ruta>> adyacencia = new ArrayList<>();  // rutas que salen de cada vertice
    private final HashMap<String, Integer> indicePorCodigo = new HashMap<>(); // codigo -> indice
    private final ArrayList<Ruta> canceladas = new ArrayList<>();             // rutas quitadas del grafo

    /** Agrega un aeropuerto; devuelve false si el codigo ya existia. */
    public boolean agregarAeropuerto(Aeropuerto a) {
        String clave = a.getCodigo().toUpperCase();
        if (indicePorCodigo.containsKey(clave)) {
            return false;
        }
        indicePorCodigo.put(clave, aeropuertos.size());
        aeropuertos.add(a);
        adyacencia.add(new ArrayList<>());
        return true;
    }

    /** Agrega la ruta solo en la lista del origen (grafo dirigido). */
    public void agregarRuta(Ruta r) {
        adyacencia.get(r.getOrigen()).add(r);
    }

    /** Devuelve el indice del codigo, o -1 si no existe. */
    public int buscarIndice(String codigo) {
        if (codigo == null) {
            return -1;
        }
        Integer i = indicePorCodigo.get(codigo.trim().toUpperCase());
        return (i == null) ? -1 : i;
    }

    public int cantidadAeropuertos() {
        return aeropuertos.size();
    }

    /** Cuenta solo las rutas activas. */
    public int cantidadRutas() {
        int total = 0;
        for (ArrayList<Ruta> lista : adyacencia) {
            total += lista.size();
        }
        return total;
    }

    public Aeropuerto getAeropuerto(int indice) {
        return aeropuertos.get(indice);
    }

    public List<Ruta> getRutasSalientes(int indice) {
        return Collections.unmodifiableList(adyacencia.get(indice));
    }

    public List<Ruta> getCanceladas() {
        return Collections.unmodifiableList(canceladas);
    }

    /** Quita la ruta origen -> destino del grafo y la guarda en canceladas. */
    public boolean cancelarRuta(int origen, int destino) {
        boolean seCancelo = false;
        Iterator<Ruta> it = adyacencia.get(origen).iterator();
        while (it.hasNext()) {
            Ruta r = it.next();
            if (r.getDestino() == destino) {
                it.remove();
                canceladas.add(r);
                seCancelo = true;
            }
        }
        return seCancelo;
    }

    /** Devuelve todas las rutas canceladas al grafo. */
    public int restaurarTodas() {
        int cantidad = canceladas.size();
        for (Ruta r : canceladas) {
            adyacencia.get(r.getOrigen()).add(r);
        }
        // Deja cada lista en el orden original del archivo
        for (Ruta r : canceladas) {
            adyacencia.get(r.getOrigen()).sort(Comparator.comparingInt(Ruta::getOrden));
        }
        canceladas.clear();
        return cantidad;
    }
}
