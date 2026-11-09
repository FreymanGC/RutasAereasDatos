package rutasaereas.vista;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;
import rutasaereas.algoritmos.ResultadoRuta;
import rutasaereas.modelo.Aeropuerto;
import rutasaereas.modelo.Grafo;
import rutasaereas.modelo.Ruta;

/** Mueve el avion tramo por tramo usando un Timer (unas 60 veces por segundo) y genera su estela de humo. */
public class AnimadorVuelo {

    private static final int ESPERA_ESCALA_MS = 900;
    private static final double VIDA_HUMO_MS = 1400;     // cuanto dura cada bocanada de humo

    private final Timer timer;
    private final Runnable alCambiar;     // avisa a la ventana para repintar

    private Grafo grafo;
    private ResultadoRuta ruta;           // null = no hay vuelo
    private int tramoActual;
    private double progreso;              // 0 = en el origen del tramo, 1 = en el destino
    private double esperaRestanteMs;
    private boolean pausado;
    private boolean terminado;
    private long ultimoTick;
    private int minutosAcumulados;
    private int costoAcumulado;

    // Particulas de humo: {x, y, edadMs, vidaMs} en coordenadas del mapa
    private final List<double[]> humo = new ArrayList<>();
    private final Random azar = new Random();

    public AnimadorVuelo(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        this.timer = new Timer(16, e -> avanzar());
    }

    /** Empieza a animar la ruta desde el primer tramo. */
    public void iniciar(ResultadoRuta r, Grafo g) {
        timer.stop();
        if (r == null || !r.existe() || r.getVuelos() == 0) {
            ruta = null;
            alCambiar.run();
            return;
        }
        ruta = r;
        grafo = g;
        tramoActual = 0;
        progreso = 0;
        esperaRestanteMs = 0;
        pausado = false;
        terminado = false;
        minutosAcumulados = 0;
        costoAcumulado = 0;
        humo.clear();
        ultimoTick = System.nanoTime();
        timer.start();
        alCambiar.run();
    }

    public void detener() {
        timer.stop();
        ruta = null;
        humo.clear();
        alCambiar.run();
    }

    public void reiniciar() {
        if (ruta != null) {
            iniciar(ruta, grafo);
        }
    }

    public void alternarPausa() {
        if (ruta == null || terminado) {
            return;
        }
        pausado = !pausado;
        alCambiar.run();
    }

    public boolean hayVuelo() { return ruta != null; }
    public boolean isPausado() { return pausado; }

    /** Se ejecuta en cada tick del Timer: avanza el avion o la espera en la escala. */
    private void avanzar() {
        long ahora = System.nanoTime();
        double dt = (ahora - ultimoTick) / 1_000_000.0;   // ms desde el tick anterior
        ultimoTick = ahora;
        if (ruta == null || pausado) {
            return;
        }
        envejecerHumo(dt);

        // Ya llego: el Timer sigue solo hasta que el humo se desvanece
        if (terminado) {
            if (humo.isEmpty()) {
                timer.stop();
            }
            alCambiar.run();
            return;
        }

        // Detenido en una escala
        if (esperaRestanteMs > 0) {
            esperaRestanteMs -= dt;
            if (esperaRestanteMs <= 0) {
                esperaRestanteMs = 0;
                tramoActual++;
                progreso = 0;
            }
            alCambiar.run();
            return;
        }

        // Volando
        Ruta tramo = ruta.getTramos().get(tramoActual);
        progreso += dt / duracionMs(tramo);
        soltarHumo();
        if (progreso >= 1) {
            progreso = 1;
            minutosAcumulados += tramo.getMinutos();
            costoAcumulado += tramo.getCosto();
            if (tramoActual == ruta.getVuelos() - 1) {
                terminado = true;
            } else {
                esperaRestanteMs = ESPERA_ESCALA_MS;
            }
        }
        alCambiar.run();
    }

    /** Duracion del tramo en pantalla, segun sus minutos (0,9 a 3,5 s). */
    private double duracionMs(Ruta r) {
        return Math.max(900, Math.min(3500, r.getMinutos() * 10.0));
    }

    /** Agrega una bocanada de humo donde esta el avion, con un pequeno desorden. */
    private void soltarHumo() {
        double[] p = getPosicionAvion();
        double x = p[0] + (azar.nextDouble() - 0.5) * 3;
        double y = p[1] + (azar.nextDouble() - 0.5) * 3;
        humo.add(new double[]{x, y, 0, VIDA_HUMO_MS * (0.8 + azar.nextDouble() * 0.4)});
    }

    /** Envejece el humo y quita las particulas que ya cumplieron su vida. */
    private void envejecerHumo(double dt) {
        for (int i = humo.size() - 1; i >= 0; i--) {
            double[] p = humo.get(i);
            p[2] += dt;
            if (p[2] >= p[3]) {
                humo.remove(i);
            }
        }
    }

    public List<double[]> getHumo() {
        return Collections.unmodifiableList(humo);
    }

    /** Posicion {x, y, angulo} del avion: punto del arco del tramo segun 'progreso'. */
    public double[] getPosicionAvion() {
        if (ruta == null) {
            return null;
        }
        Ruta tramo = ruta.getTramos().get(tramoActual);
        Aeropuerto a = grafo.getAeropuerto(tramo.getOrigen());
        Aeropuerto b = grafo.getAeropuerto(tramo.getDestino());
        double[] p = Curva.punto(a.getX(), a.getY(), b.getX(), b.getY(), progreso);
        double angulo = Curva.angulo(a.getX(), a.getY(), b.getX(), b.getY(), progreso);
        return new double[]{p[0], p[1], angulo};
    }

    /** Texto del panel: tramo actual, estado y acumulados. */
    public String getEstadoHtml() {
        if (ruta == null) {
            return "<html>Sin vuelo en curso.</html>";
        }
        Ruta tramo = ruta.getTramos().get(tramoActual);
        String de = grafo.getAeropuerto(tramo.getOrigen()).getCodigo();
        String a = grafo.getAeropuerto(tramo.getDestino()).getCodigo();

        String estado;
        if (terminado) {
            estado = "Llegó a " + a + ".";
        } else if (esperaRestanteMs > 0) {
            estado = "Escala en " + a + ".";
        } else {
            estado = "En vuelo.";
        }
        if (pausado) {
            estado += " (PAUSADO)";
        }
        return "<html><b>Tramo " + (tramoActual + 1) + " de " + ruta.getVuelos() + ":</b> "
                + de + " → " + a + " (" + tramo.getMinutos() + " min, $" + tramo.getCosto() + ")"
                + "<br>Estado: " + estado
                + "<br><b>Acumulado:</b> " + minutosAcumulados + " min, $" + costoAcumulado
                + "</html>";
    }
}
