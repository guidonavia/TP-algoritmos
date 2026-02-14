import org.uade.progra3.ui.RedSocialFrame;

import javax.swing.*;

/**
 * Punto de entrada: lanza la UI del prototipo de red social universitaria.
 * Usa demo-red-social.json (usuarios, conexiones, publicaciones) para las tres pestañas:
 * Kruskal (red mínima), Dijkstra (recomendación), Programación dinámica (portada óptima).
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            RedSocialFrame frame = new RedSocialFrame();
            frame.setVisible(true);
        });
    }
}