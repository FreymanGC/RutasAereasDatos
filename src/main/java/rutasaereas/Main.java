package rutasaereas;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import rutasaereas.vista.VentanaPrincipal;

/** Abre la ventana del programa. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // si falla se usa el aspecto por defecto
            }
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
            ventana.cargarInicial();
        });
    }
}
