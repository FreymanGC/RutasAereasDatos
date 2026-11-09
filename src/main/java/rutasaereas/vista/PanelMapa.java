package rutasaereas.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.util.function.IntConsumer;
import javax.swing.JPanel;
import rutasaereas.algoritmos.ResultadoRuta;
import rutasaereas.modelo.Aeropuerto;
import rutasaereas.modelo.Grafo;
import rutasaereas.modelo.Ruta;

/** Dibuja el mapa: fondo, rutas, ruta elegida, canceladas, aeropuertos, humo y avion. */
public class PanelMapa extends JPanel {

    private static final int TAM_MAPA = 1000;
    private static final int MARGEN = 25;
    private static final int RADIO = 6;


    private final AnimadorVuelo animador;
    private final IntConsumer alHacerClic;   // recibe el indice del aeropuerto clicado
    private final FondoMapa fondo = new FondoMapa();

    private Grafo grafo;
    private ResultadoRuta rutaElegida;
    private boolean[] alcanzables;           // null = no se colorean
    private int origenAlcance = -1;
    private int seleccionOrigen = -1;
    private int seleccionDestino = -1;
    private boolean mostrarFondo = true;
    private Tema tema = Tema.DIA;            // colores actuales (dia o noche)

    private double escala = 1, despX, despY;

    public PanelMapa(AnimadorVuelo animador, IntConsumer alHacerClic) {
        this.animador = animador;
        this.alHacerClic = alHacerClic;
        setPreferredSize(new Dimension(820, 820));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int i = aeropuertoCercano(e.getX(), e.getY());
                if (i != -1) {
                    PanelMapa.this.alHacerClic.accept(i);
                }
            }
        });
    }

    public void setGrafo(Grafo g) {
        grafo = g;
        rutaElegida = null;
        alcanzables = null;
        seleccionOrigen = seleccionDestino = -1;
        repaint();
    }

    public void setRutaElegida(ResultadoRuta r) { rutaElegida = r; repaint(); }

    public void setAlcanzables(boolean[] marcas, int origen) {
        alcanzables = marcas;
        origenAlcance = origen;
        repaint();
    }

    public void setSeleccion(int origen, int destino) {
        seleccionOrigen = origen;
        seleccionDestino = destino;
        repaint();
    }

    public void setMostrarFondo(boolean mostrar) { mostrarFondo = mostrar; repaint(); }

    public void setModoNoche(boolean noche) { tema = noche ? Tema.NOCHE : Tema.DIA; repaint(); }

    /** Ajusta el mapa de 1000 x 1000 al tamano actual del panel. */
    private void calcularEscala() {
        double lado = Math.min(getWidth(), getHeight()) - 2.0 * MARGEN;
        escala = Math.max(lado, 100) / TAM_MAPA;
        despX = (getWidth() - TAM_MAPA * escala) / 2;
        despY = (getHeight() - TAM_MAPA * escala) / 2;
    }

    // Convierten coordenadas del archivo a pixeles de pantalla
    private double px(double x) { return despX + x * escala; }
    private double py(double y) { return despY + y * escala; }

    /** Aeropuerto a menos de 10 px del clic, o -1 si no hay. */
    private int aeropuertoCercano(int sx, int sy) {
        if (grafo == null) return -1;
        calcularEscala();
        int mejor = -1;
        double mejorDist = 10;
        for (int i = 0; i < grafo.cantidadAeropuertos(); i++) {
            Aeropuerto a = grafo.getAeropuerto(i);
            double d = Math.hypot(px(a.getX()) - sx, py(a.getY()) - sy);
            if (d <= mejorDist) {
                mejorDist = d;
                mejor = i;
            }
        }
        return mejor;
    }

    /** Arco de la ruta en pantalla (ver Curva). */
    private QuadCurve2D arco(Ruta r) {
        Aeropuerto a = grafo.getAeropuerto(r.getOrigen());
        Aeropuerto b = grafo.getAeropuerto(r.getDestino());
        double x1 = px(a.getX()), y1 = py(a.getY()), x2 = px(b.getX()), y2 = py(b.getY());
        double[] c = Curva.control(x1, y1, x2, y2);
        return new QuadCurve2D.Double(x1, y1, c[0], c[1], x2, y2);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        calcularEscala();

        // Oceano con degradado
        g.setPaint(new GradientPaint(0, 0, tema.oceanoArriba, 0, getHeight(), tema.oceanoAbajo));
        g.fillRect(0, 0, getWidth(), getHeight());

        if (mostrarFondo) {
            AffineTransform t = new AffineTransform(escala, 0, 0, escala, despX, despY);
            fondo.dibujar(g, t, tema);
        } else {
            g.setColor(new Color(255, 255, 255, 40));
            g.drawRect((int) px(0), (int) py(0), (int) (TAM_MAPA * escala), (int) (TAM_MAPA * escala));
        }

        if (grafo != null) {
            dibujarRutas(g);
            dibujarCanceladas(g);
            dibujarRutaElegida(g);
            dibujarAeropuertos(g);
            dibujarHumo(g);
            dibujarAvion(g);
        }
        dibujarLeyenda(g);
        g.dispose();
    }

    private void dibujarRutas(Graphics2D g) {
        g.setColor(tema.ruta);
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < grafo.cantidadAeropuertos(); i++) {
            for (Ruta r : grafo.getRutasSalientes(i)) {
                g.draw(arco(r));
            }
        }
    }

    private void dibujarCanceladas(Graphics2D g) {
        g.setColor(tema.cancelada);
        g.setStroke(punteada(2f));
        for (Ruta r : grafo.getCanceladas()) {
            QuadCurve2D c = arco(r);
            g.draw(c);
            flecha(g, c);
        }
    }

    private void dibujarRutaElegida(Graphics2D g) {
        if (rutaElegida == null || !rutaElegida.existe()) return;
        for (Ruta r : rutaElegida.getTramos()) {
            QuadCurve2D c = arco(r);
            // Brillo: una linea ancha y transparente debajo de la linea principal
            g.setColor(Tema.conAlfa(tema.elegida, 70));
            g.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(c);
            g.setColor(tema.elegida);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(c);
            flecha(g, c);
        }
    }

    private void dibujarAeropuertos(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        for (int i = 0; i < grafo.cantidadAeropuertos(); i++) {
            Aeropuerto a = grafo.getAeropuerto(i);
            double x = px(a.getX()), y = py(a.getY());

            Color relleno = tema.aeropuerto;
            if (alcanzables != null) {
                if (i == origenAlcance) relleno = tema.origen;
                else relleno = alcanzables[i] ? tema.alcanzable : tema.noAlcanzable;
            }
            // Halo suave alrededor del punto
            g.setColor(Tema.conAlfa(relleno, 70));
            g.fill(new Ellipse2D.Double(x - RADIO - 4, y - RADIO - 4, 2 * RADIO + 8, 2 * RADIO + 8));
            g.setColor(relleno);
            g.fill(new Ellipse2D.Double(x - RADIO, y - RADIO, 2 * RADIO, 2 * RADIO));
            g.setColor(tema.borde);
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new Ellipse2D.Double(x - RADIO, y - RADIO, 2 * RADIO, 2 * RADIO));

            // Anillo de seleccion
            if (i == seleccionOrigen || i == seleccionDestino) {
                g.setColor(i == seleccionOrigen ? tema.origen : tema.destino);
                g.setStroke(new BasicStroke(3f));
                g.draw(new Ellipse2D.Double(x - RADIO - 5, y - RADIO - 5, 2 * RADIO + 10, 2 * RADIO + 10));
            }

            // Codigo con sombra para que se lea sobre cualquier fondo
            g.setColor(new Color(0, 0, 0, 170));
            g.drawString(a.getCodigo(), (float) (x + 9), (float) (y - 6));
            g.setColor(Color.WHITE);
            g.drawString(a.getCodigo(), (float) (x + 8), (float) (y - 7));
        }
    }

    /** Estela de humo: circulos que crecen y se desvanecen con la edad. */
    private void dibujarHumo(Graphics2D g) {
        for (double[] p : animador.getHumo()) {
            double vida = p[2] / p[3];                          // 0 = recien creado, 1 = por desaparecer
            double r = (2.5 + 8 * vida) * Math.max(escala, 0.6);
            int alfa = (int) (140 * (1 - vida));
            g.setColor(new Color(235, 238, 245, Math.max(0, alfa)));
            g.fill(new Ellipse2D.Double(px(p[0]) - r, py(p[1]) - r, 2 * r, 2 * r));
        }
    }

    private void dibujarAvion(Graphics2D g) {
        double[] p = animador.getPosicionAvion();
        if (p == null) return;
        Graphics2D ga = (Graphics2D) g.create();
        ga.translate(px(p[0]), py(p[1]));
        ga.rotate(p[2]);                      // apunta hacia donde va
        Path2D avion = new Path2D.Double();
        double[][] puntos = {{14, 0}, {3, -3}, {-2, -13}, {-6, -13}, {-3, -3}, {-10, -3}, {-13, -8},
            {-16, -8}, {-13, 0}, {-16, 8}, {-13, 8}, {-10, 3}, {-3, 3}, {-6, 13}, {-2, 13}, {3, 3}};
        avion.moveTo(puntos[0][0], puntos[0][1]);
        for (int i = 1; i < puntos.length; i++) avion.lineTo(puntos[i][0], puntos[i][1]);
        avion.closePath();
        // Sombra
        ga.setColor(new Color(0, 0, 0, 90));
        ga.translate(3, 4);
        ga.fill(avion);
        ga.translate(-3, -4);
        ga.setColor(tema.avion);
        ga.fill(avion);
        ga.setColor(tema.bordeAvion);
        ga.setStroke(new BasicStroke(1.2f));
        ga.draw(avion);
        ga.dispose();
    }

    private void dibujarLeyenda(Graphics2D g) {
        int x = 12, y = getHeight() - 112;
        g.setColor(new Color(10, 20, 36, 215));
        g.fillRoundRect(x, y, 196, 100, 12, 12);
        g.setColor(new Color(255, 255, 255, 50));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x, y, 196, 100, 12, 12);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));

        itemLinea(g, x + 10, y + 18, Tema.conAlfa(tema.ruta, 200), new BasicStroke(1.5f), "Ruta activa");
        itemLinea(g, x + 10, y + 36, tema.elegida, new BasicStroke(3f), "Ruta elegida");
        itemLinea(g, x + 10, y + 54, tema.cancelada, punteada(2f), "Ruta cancelada");
        itemPunto(g, x + 10, y + 72, tema.alcanzable, "Alcanzable");
        itemPunto(g, x + 100, y + 72, tema.noAlcanzable, "No alcanzable");
        itemPunto(g, x + 10, y + 90, tema.origen, "Origen");
        itemPunto(g, x + 100, y + 90, tema.destino, "Destino");
    }

    private void itemLinea(Graphics2D g, int x, int y, Color c, Stroke s, String texto) {
        g.setColor(c);
        g.setStroke(s);
        g.drawLine(x, y - 4, x + 30, y - 4);
        g.setColor(new Color(226, 232, 240));
        g.drawString(texto, x + 40, y);
    }

    private void itemPunto(Graphics2D g, int x, int y, Color c, String texto) {
        g.setColor(c);
        g.fillOval(x + 10, y - 9, 10, 10);
        g.setColor(new Color(226, 232, 240));
        g.drawString(texto, x + 26, y);
    }

    private static Stroke punteada(float grosor) {
        return new BasicStroke(grosor, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{8f, 6f}, 0f);
    }

    /** Punta de flecha al final del arco, para ver la direccion de la ruta. */
    private void flecha(Graphics2D g, QuadCurve2D c) {
        double largo = Math.hypot(c.getX2() - c.getX1(), c.getY2() - c.getY1());
        if (largo < 1) return;
        double t = Math.max(0.5, 1 - (RADIO + 4) / largo);      // un poco antes del aeropuerto
        double[] p = Curva.punto(c.getX1(), c.getY1(), c.getX2(), c.getY2(), t);
        double ang = Curva.angulo(c.getX1(), c.getY1(), c.getX2(), c.getY2(), t);
        Path2D punta = new Path2D.Double();
        punta.moveTo(p[0], p[1]);
        punta.lineTo(p[0] - 10 * Math.cos(ang - 0.4), p[1] - 10 * Math.sin(ang - 0.4));
        punta.lineTo(p[0] - 10 * Math.cos(ang + 0.4), p[1] - 10 * Math.sin(ang + 0.4));
        punta.closePath();
        Stroke anterior = g.getStroke();
        g.setStroke(new BasicStroke(1f));
        g.fill(punta);
        g.setStroke(anterior);
    }
}
