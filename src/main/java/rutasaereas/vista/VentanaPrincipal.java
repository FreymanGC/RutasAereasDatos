package rutasaereas.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import rutasaereas.algoritmos.Busquedas;
import rutasaereas.algoritmos.Criterio;
import rutasaereas.algoritmos.ResultadoRuta;
import rutasaereas.modelo.Aeropuerto;
import rutasaereas.modelo.CargadorDatos;
import rutasaereas.modelo.Grafo;
import rutasaereas.modelo.Ruta;

/** Ventana principal: mapa a la izquierda, controles y resultados a la derecha. */
public class VentanaPrincipal extends JFrame {

    private Grafo grafo = new Grafo();
    private final AnimadorVuelo animador;
    private final PanelMapa mapa;

    private final JComboBox<String> cbOrigen = new JComboBox<>();
    private final JComboBox<String> cbDestino = new JComboBox<>();
    private final JRadioButton rbEscalas = new JRadioButton("Menos escalas (BFS)", true);
    private final JRadioButton rbRapida = new JRadioButton("Más rápida (Dijkstra, minutos)");
    private final JRadioButton rbBarata = new JRadioButton("Más barata (Dijkstra, costo)");
    private final JTextArea txtResultado = new JTextArea();
    private final JLabel lblVuelo = new JLabel();
    private final JButton btnPausa = new JButton("Pausar");

    private boolean clicEsOrigen = true;   // el clic en el mapa alterna origen / destino

    public VentanaPrincipal() {
        super("Planificador de rutas aéreas — EIF207 Proyecto II");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        animador = new AnimadorVuelo(this::alCambiarAnimacion);
        mapa = new PanelMapa(animador, this::alHacerClicEnAeropuerto);

        add(mapa, BorderLayout.CENTER);
        add(crearPanelDerecho(), BorderLayout.EAST);
        alCambiarAnimacion();

        setSize(1280, 860);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelDerecho() {
        JPanel controles = new JPanel();
        controles.setLayout(new BoxLayout(controles, BoxLayout.Y_AXIS));

        // Origen y destino: se pueden escribir o elegir
        cbOrigen.setEditable(true);
        cbDestino.setEditable(true);
        cbOrigen.addActionListener(e -> actualizarSeleccionEnMapa());
        cbDestino.addActionListener(e -> actualizarSeleccionEnMapa());
        JPanel pSel = new JPanel(new GridLayout(0, 2, 6, 6));
        pSel.add(new JLabel("Origen:"));
        pSel.add(cbOrigen);
        pSel.add(new JLabel("Destino:"));
        pSel.add(cbDestino);
        JPanel secSel = seccion("Origen y destino");
        secSel.add(pSel);
        JLabel ayuda = new JLabel("<html><i>Clic en el mapa: 1.º origen, 2.º destino.</i></html>");
        ayuda.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        ayuda.setAlignmentX(Component.CENTER_ALIGNMENT);
        secSel.add(ayuda);
        controles.add(secSel);

        // Criterio
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbEscalas);
        grupo.add(rbRapida);
        grupo.add(rbBarata);
        JPanel secCrit = seccion("Criterio");
        JPanel pCrit = new JPanel(new GridLayout(0, 1));
        pCrit.add(rbEscalas);
        pCrit.add(rbRapida);
        pCrit.add(rbBarata);
        secCrit.add(pCrit);
        controles.add(secCrit);

        // Acciones
        JPanel secAcc = seccion("Consultas");
        JPanel pAcc = new JPanel(new GridLayout(0, 2, 6, 6));
        pAcc.add(boton("Buscar ruta", this::buscarRuta));
        pAcc.add(boton("Ver aeropuerto", this::verAeropuerto));
        pAcc.add(boton("Alcanzables", this::mostrarAlcanzables));
        pAcc.add(boton("Limpiar mapa", this::limpiarMapa));
        secAcc.add(pAcc);
        controles.add(secAcc);

        JPanel secCan = seccion("Rutas canceladas");
        JPanel pCan = new JPanel(new GridLayout(0, 2, 6, 6));
        pCan.add(boton("Cancelar ruta", this::cancelarRuta));
        pCan.add(boton("Restaurar todas", this::restaurarRutas));
        secCan.add(pCan);
        controles.add(secCan);

        // Animacion
        JPanel secVuelo = seccion("Vuelo");
        lblVuelo.setPreferredSize(new Dimension(300, 60));
        lblVuelo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        lblVuelo.setAlignmentX(Component.CENTER_ALIGNMENT);
        secVuelo.add(lblVuelo);
        JPanel pVuelo = new JPanel(new GridLayout(1, 2, 6, 6));
        btnPausa.addActionListener(e -> animador.alternarPausa());
        pVuelo.add(btnPausa);
        pVuelo.add(boton("Reiniciar", animador::reiniciar));
        secVuelo.add(pVuelo);
        controles.add(secVuelo);

        JPanel secArch = seccion("Datos");
        JPanel pArch = new JPanel(new GridLayout(0, 1, 6, 6));
        pArch.add(boton("Cargar otros archivos...", this::elegirArchivos));
        // El fondo es solo decorativo; se puede apagar si otros archivos usan otras coordenadas
        JCheckBox cbFondo = new JCheckBox("Mostrar mapa de fondo", true);
        cbFondo.addActionListener(e -> mapa.setMostrarFondo(cbFondo.isSelected()));
        pArch.add(cbFondo);
        JCheckBox cbNoche = new JCheckBox("Modo noche", false);
        cbNoche.addActionListener(e -> mapa.setModoNoche(cbNoche.isSelected()));
        pArch.add(cbNoche);
        secArch.add(pArch);
        controles.add(secArch);

        // Area de resultados
        txtResultado.setEditable(false);
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);
        txtResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(txtResultado);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultado"));

        JPanel derecho = new JPanel(new BorderLayout(0, 6));
        derecho.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));
        derecho.setPreferredSize(new Dimension(360, 100));
        derecho.add(controles, BorderLayout.NORTH);
        derecho.add(scroll, BorderLayout.CENTER);
        return derecho;
    }

    private JPanel seccion(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(2, 6, 6, 6)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> accion.run());
        return b;
    }

    /** Carga aeropuertos.txt y rutas.txt de la carpeta del proyecto, o pide elegirlos. */
    public void cargarInicial() {
        File aero = new File("aeropuertos.txt");
        File rutas = new File("rutas.txt");
        if (aero.isFile() && rutas.isFile()) {
            cargarArchivos(aero, rutas);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron aeropuertos.txt y rutas.txt en:\n"
                    + new File(".").getAbsolutePath() + "\nElíjalos a continuación.");
            elegirArchivos();
        }
    }

    /** Pide los dos archivos con un JFileChooser. */
    private void elegirArchivos() {
        JFileChooser fc = new JFileChooser(new File("."));
        fc.setDialogTitle("1/2: elija el archivo de AEROPUERTOS");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File aero = fc.getSelectedFile();
        fc.setDialogTitle("2/2: elija el archivo de RUTAS");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        cargarArchivos(aero, fc.getSelectedFile());
    }

    private void cargarArchivos(File aero, File rutas) {
        List<String> advertencias = new ArrayList<>();
        Grafo nuevo;
        try {
            nuevo = CargadorDatos.cargar(aero, rutas, advertencias);
        } catch (IOException ex) {
            // Si falla se conserva el grafo anterior
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al cargar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        grafo = nuevo;
        animador.detener();
        mapa.setGrafo(grafo);

        // Llena los combos con los codigos ordenados
        List<String> codigos = new ArrayList<>();
        for (int i = 0; i < grafo.cantidadAeropuertos(); i++) codigos.add(grafo.getAeropuerto(i).getCodigo());
        codigos.sort(null);
        cbOrigen.removeAllItems();
        cbDestino.removeAllItems();
        for (String c : codigos) {
            cbOrigen.addItem(c);
            cbDestino.addItem(c);
        }
        cbOrigen.setSelectedItem("");
        cbDestino.setSelectedItem("");
        clicEsOrigen = true;

        StringBuilder sb = new StringBuilder();
        sb.append("Datos cargados: ").append(grafo.cantidadAeropuertos()).append(" aeropuertos, ")
          .append(grafo.cantidadRutas()).append(" rutas.\n");
        sb.append("Archivos: ").append(aero.getName()).append(", ").append(rutas.getName()).append("\n");
        for (String a : advertencias) sb.append("Aviso: ").append(a).append("\n");
        mostrar(sb.toString());
    }

    /** Calcula la ruta con el criterio elegido, la muestra y anima el vuelo. */
    private void buscarRuta() {
        int o = leerAeropuerto(cbOrigen, "origen");
        int d = leerAeropuerto(cbDestino, "destino");
        if (o == -1 || d == -1) return;
        mapa.setAlcanzables(null, -1);

        if (o == d) {
            mapa.setRutaElegida(null);
            animador.detener();
            mostrar("El origen y el destino son el mismo aeropuerto (" + codigo(o) + ").\n"
                    + "No hace falta ningún vuelo: 0 vuelos, 0 min, $0.");
            return;
        }

        String nombre;
        ResultadoRuta r;
        if (rbEscalas.isSelected()) {
            nombre = "Ruta con menos escalas (BFS)";
            r = Busquedas.bfsMenosEscalas(grafo, o, d);
        } else if (rbRapida.isSelected()) {
            nombre = "Ruta más rápida (Dijkstra por minutos)";
            r = Busquedas.dijkstra(grafo, o, d, Criterio.MINUTOS);
        } else {
            nombre = "Ruta más barata (Dijkstra por costo)";
            r = Busquedas.dijkstra(grafo, o, d, Criterio.COSTO);
        }

        if (!r.existe()) {
            mapa.setRutaElegida(null);
            animador.detener();
            mostrar(nombre + "\n\nNo existe ruta de " + codigo(o) + " a " + codigo(d) + ".");
            return;
        }

        StringBuilder sb = new StringBuilder(nombre).append("\n\n");
        sb.append("Secuencia: ").append(r.getSecuencia(grafo, " → ")).append("\n");
        sb.append("Vuelos:    ").append(r.getVuelos()).append("\n");
        sb.append("Minutos:   ").append(r.getMinutosTotales()).append("\n");
        sb.append("Costo:     $").append(r.getCostoTotal()).append("\n\nTramos:\n");
        for (Ruta t : r.getTramos()) {
            sb.append(String.format("  %s → %s  %4d min  $%d%n",
                    codigo(t.getOrigen()), codigo(t.getDestino()), t.getMinutos(), t.getCosto()));
        }
        mostrar(sb.toString());
        mapa.setRutaElegida(r);
        animador.iniciar(r, grafo);
    }

    /** Muestra los datos del aeropuerto origen y sus rutas salientes. */
    private void verAeropuerto() {
        int o = leerAeropuerto(cbOrigen, "origen");
        if (o == -1) return;
        Aeropuerto a = grafo.getAeropuerto(o);
        StringBuilder sb = new StringBuilder();
        sb.append(a.getCodigo()).append(" — ").append(a.getCiudad()).append(", ").append(a.getPais()).append("\n");
        sb.append("Posición: x=").append(a.getX()).append(", y=").append(a.getY()).append("\n\n");
        List<Ruta> salientes = grafo.getRutasSalientes(o);
        sb.append("Rutas salientes activas: ").append(salientes.size()).append("\n");
        for (Ruta r : salientes) {
            sb.append(String.format("  %s → %s  %4d min  $%d%n",
                    a.getCodigo(), codigo(r.getDestino()), r.getMinutos(), r.getCosto()));
        }
        for (Ruta r : grafo.getCanceladas()) {
            if (r.getOrigen() == o) {
                sb.append("  (cancelada) ").append(a.getCodigo()).append(" → ").append(codigo(r.getDestino())).append("\n");
            }
        }
        mostrar(sb.toString());
    }

    /** Colorea los aeropuertos alcanzables y no alcanzables desde el origen (DFS). */
    private void mostrarAlcanzables() {
        int o = leerAeropuerto(cbOrigen, "origen");
        if (o == -1) return;
        boolean[] marcas = Busquedas.alcanzablesDesde(grafo, o);
        List<String> si = new ArrayList<>(), no = new ArrayList<>();
        for (int i = 0; i < marcas.length; i++) {
            if (i == o) continue;
            (marcas[i] ? si : no).add(codigo(i));
        }
        animador.detener();
        mapa.setRutaElegida(null);
        mapa.setAlcanzables(marcas, o);
        mostrar("Destinos desde " + codigo(o) + " (DFS)\n\n"
                + "Alcanzables (" + si.size() + "):\n" + String.join(", ", si) + "\n\n"
                + "NO alcanzables (" + no.size() + "):\n" + (no.isEmpty() ? "(ninguno)" : String.join(", ", no)));
    }

    /** Cancela la ruta directa origen -> destino. */
    private void cancelarRuta() {
        int o = leerAeropuerto(cbOrigen, "origen");
        int d = leerAeropuerto(cbDestino, "destino");
        if (o == -1 || d == -1) return;
        if (grafo.cancelarRuta(o, d)) {
            limpiarResultados();
            mostrar("Ruta " + codigo(o) + " → " + codigo(d) + " cancelada.\n"
                    + "Las consultas siguientes usan el grafo modificado.\n"
                    + "Rutas activas: " + grafo.cantidadRutas() + ", canceladas: " + grafo.getCanceladas().size());
        } else {
            mostrar("No hay una ruta directa activa " + codigo(o) + " → " + codigo(d)
                    + ".\n(No existe en el archivo o ya está cancelada.)");
        }
    }

    /** Restaura todas las rutas canceladas. */
    private void restaurarRutas() {
        int n = grafo.restaurarTodas();
        limpiarResultados();
        mostrar(n == 0 ? "No había rutas canceladas." : "Se restauraron " + n + " ruta(s).");
    }

    private void limpiarMapa() {
        limpiarResultados();
        mostrar("");
    }

    /** Borra la ruta y los colores del mapa (cuando cambia el grafo). */
    private void limpiarResultados() {
        animador.detener();
        mapa.setRutaElegida(null);
        mapa.setAlcanzables(null, -1);
    }

    /** Devuelve el indice del codigo escrito, o -1 si esta vacio o no existe. */
    private int leerAeropuerto(JComboBox<String> combo, String rol) {
        Object item = combo.getEditor().getItem();
        String cod = (item == null) ? "" : item.toString().trim().toUpperCase();
        if (cod.isEmpty()) {
            mostrar("Escriba o elija el aeropuerto de " + rol + ".");
            return -1;
        }
        int i = grafo.buscarIndice(cod);
        if (i == -1) {
            mostrar("El aeropuerto \"" + cod + "\" (" + rol + ") no existe.");
        }
        return i;
    }

    private String codigo(int i) {
        return grafo.getAeropuerto(i).getCodigo();
    }

    private void mostrar(String texto) {
        txtResultado.setText(texto);
        txtResultado.setCaretPosition(0);
    }

    /** Clic en el mapa: alterna entre llenar origen y destino. */
    private void alHacerClicEnAeropuerto(int indice) {
        if (clicEsOrigen) {
            cbOrigen.setSelectedItem(codigo(indice));
        } else {
            cbDestino.setSelectedItem(codigo(indice));
        }
        clicEsOrigen = !clicEsOrigen;
    }

    private void actualizarSeleccionEnMapa() {
        mapa.setSeleccion(grafo.buscarIndice(String.valueOf(cbOrigen.getSelectedItem())),
                          grafo.buscarIndice(String.valueOf(cbDestino.getSelectedItem())));
    }

    private void alCambiarAnimacion() {
        lblVuelo.setText(animador.getEstadoHtml());
        btnPausa.setText(animador.isPausado() ? "Continuar" : "Pausar");
        mapa.repaint();
    }
}
