package rutasaereas.vista;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Fondo decorativo: continentes y lineas de latitud/longitud.
 * Se lee de continentes.txt (recurso del proyecto), que ya viene en el mismo
 * sistema 0..1000 que los aeropuertos. No forma parte de los datos del grafo.
 */
final class FondoMapa {

    private final List<Path2D> tierras = new ArrayList<>();
    private final List<Path2D> lineas = new ArrayList<>();

    // Figuras ya escaladas a la pantalla (se recalculan solo si cambia el tamano)
    private List<Shape> tierrasPantalla = new ArrayList<>();
    private List<Shape> lineasPantalla = new ArrayList<>();
    private AffineTransform ultimaTransformacion;

    FondoMapa() {
        try (InputStream in = FondoMapa.class.getResourceAsStream("continentes.txt")) {
            if (in == null) {
                return;                   // sin archivo: el mapa se dibuja sin fondo
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("P ")) {
                    tierras.add(leerTrazo(linea, true));
                } else if (linea.startsWith("L ")) {
                    lineas.add(leerTrazo(linea, false));
                }
            }
        } catch (Exception e) {
            tierras.clear();
            lineas.clear();
        }
    }

    /** Convierte "x,y x,y ..." en un trazo; si es poligono, lo cierra. */
    private static Path2D leerTrazo(String linea, boolean cerrar) {
        Path2D p = new Path2D.Double();
        String[] puntos = linea.substring(2).trim().split(" ");
        for (int i = 0; i < puntos.length; i++) {
            String[] xy = puntos[i].split(",");
            double x = Double.parseDouble(xy[0]), y = Double.parseDouble(xy[1]);
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        if (cerrar) p.closePath();
        return p;
    }

    /** Dibuja el fondo usando la transformacion mapa -> pantalla del panel. */
    void dibujar(Graphics2D g, AffineTransform mapaAPantalla, Tema tema) {
        if (!mapaAPantalla.equals(ultimaTransformacion)) {
            tierrasPantalla = transformar(tierras, mapaAPantalla);
            lineasPantalla = transformar(lineas, mapaAPantalla);
            ultimaTransformacion = new AffineTransform(mapaAPantalla);
        }
        g.setStroke(new BasicStroke(1f));
        g.setColor(tema.lineasGeo);
        for (Shape s : lineasPantalla) g.draw(s);
        for (Shape s : tierrasPantalla) {
            g.setColor(tema.tierra);
            g.fill(s);
            g.setColor(tema.costa);
            g.draw(s);
        }
    }

    private static List<Shape> transformar(List<Path2D> figuras, AffineTransform t) {
        List<Shape> resultado = new ArrayList<>();
        for (Path2D p : figuras) resultado.add(t.createTransformedShape(p));
        return resultado;
    }
}
