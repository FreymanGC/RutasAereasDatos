package rutasaereas.vista;

/**
 * Arco (curva de Bezier cuadratica) con el que se dibuja cada ruta.
 * Une P0 con P1 pasando cerca de un punto de control C corrido hacia un lado,
 * asi A->B y B->A quedan en arcos separados.
 */
final class Curva {

    private static final double CURVATURA = 0.15;   // cuanto se abre el arco

    private Curva() { }

    /** Punto de control: el centro del segmento, corrido de forma perpendicular. */
    static double[] control(double x1, double y1, double x2, double y2) {
        double mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
        double dx = x2 - x1, dy = y2 - y1;
        return new double[]{mx + dy * CURVATURA, my - dx * CURVATURA};
    }

    /** Punto del arco en t (0 = inicio, 1 = fin): B(t) = (1-t)^2*P0 + 2(1-t)t*C + t^2*P1 */
    static double[] punto(double x1, double y1, double x2, double y2, double t) {
        double[] c = control(x1, y1, x2, y2);
        double u = 1 - t;
        return new double[]{
            u * u * x1 + 2 * u * t * c[0] + t * t * x2,
            u * u * y1 + 2 * u * t * c[1] + t * t * y2};
    }

    /** Direccion del arco en t (derivada), en radianes. */
    static double angulo(double x1, double y1, double x2, double y2, double t) {
        double[] c = control(x1, y1, x2, y2);
        double dx = 2 * (1 - t) * (c[0] - x1) + 2 * t * (x2 - c[0]);
        double dy = 2 * (1 - t) * (c[1] - y1) + 2 * t * (y2 - c[1]);
        return Math.atan2(dy, dx);
    }
}
