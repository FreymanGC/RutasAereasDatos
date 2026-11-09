package rutasaereas.vista;

import java.awt.Color;

/** Colores del mapa. Hay dos temas: DIA (tierra verde, mar azul) y NOCHE (oscuro). */
final class Tema {

    final Color oceanoArriba, oceanoAbajo, tierra, costa, lineasGeo;
    final Color ruta, elegida, cancelada;
    final Color aeropuerto, borde, alcanzable, noAlcanzable, origen, destino;
    final Color avion, bordeAvion;

    private Tema(Color[] c) {
        oceanoArriba = c[0]; oceanoAbajo = c[1]; tierra = c[2]; costa = c[3]; lineasGeo = c[4];
        ruta = c[5]; elegida = c[6]; cancelada = c[7];
        aeropuerto = c[8]; borde = c[9]; alcanzable = c[10]; noAlcanzable = c[11]; origen = c[12]; destino = c[13];
        avion = c[14]; bordeAvion = c[15];
    }

    static final Tema DIA = new Tema(new Color[]{
        new Color(46, 134, 222), new Color(18, 92, 184),        // oceano
        new Color(112, 184, 96), new Color(58, 128, 62),        // tierra y costa
        new Color(255, 255, 255, 40),                            // lineas de latitud/longitud
        new Color(255, 255, 255, 95),                            // rutas
        new Color(255, 214, 10), new Color(220, 20, 60),         // elegida, cancelada
        Color.WHITE, new Color(20, 40, 60),                      // aeropuerto y su borde
        new Color(0, 230, 118), new Color(235, 40, 40),          // alcanzable / no alcanzable
        new Color(255, 140, 0), new Color(170, 60, 230),         // origen / destino
        Color.WHITE, new Color(200, 30, 30)});                   // avion

    static final Tema NOCHE = new Tema(new Color[]{
        new Color(10, 24, 44), new Color(18, 42, 74),
        new Color(38, 56, 78), new Color(78, 104, 135),
        new Color(255, 255, 255, 18),
        new Color(125, 175, 255, 70),
        new Color(56, 189, 248), new Color(255, 85, 85),
        new Color(226, 232, 240), new Color(10, 24, 44),
        new Color(74, 222, 128), new Color(248, 113, 113),
        new Color(251, 146, 60), new Color(192, 132, 252),
        new Color(255, 184, 0), Color.WHITE});

    /** Mismo color con otra transparencia (0 = invisible, 255 = solido). */
    static Color conAlfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alfa)));
    }
}
