package org.uade.progra3.ui;

import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Publicacion;
import org.uade.progra3.modelo.Usuario;
import org.uade.progra3.servicio.RedSocialServicio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal del prototipo: red social universitaria.
 * Tres pestañas: Red mínima (Kruskal), Recomendación de amigos (Dijkstra), Portada óptima (DP).
 */
public class RedSocialFrame extends JFrame {

    private final RedSocialServicio servicio;
    private JTextArea areaKruskal;
    private JComboBox<Usuario> comboUsuarios;
    private JTextArea areaDijkstra;
    private JTextArea areaPortadaCandidatas;
    private JTextArea areaPortadaOptima;

    public RedSocialFrame() {
        servicio = new RedSocialServicio();
        setTitle("Red Social Universitaria - Prototipo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Red mínima (Kruskal)", panelRedMinima());
        tabs.addTab("2. Recomendación (Dijkstra)", panelRecomendacion());
        tabs.addTab("3. Portada óptima (DP)", panelPortada());
        add(tabs);

        cargarDatosInicial();
    }

    private void cargarDatosInicial() {
        try {
            servicio.cargarDatos("demo-red-social.json");
            actualizarComboUsuarios();
            actualizarCandidatasPortada();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel panelRedMinima() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnCalcular = new JButton("Calcular red mínima (Kruskal)");
        btnCalcular.addActionListener(e -> ejecutarKruskal());
        p.add(btnCalcular, BorderLayout.NORTH);

        areaKruskal = new JTextArea(20, 50);
        areaKruskal.setEditable(false);
        areaKruskal.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        p.add(new JScrollPane(areaKruskal), BorderLayout.CENTER);

        JLabel info = new JLabel("Conjunto mínimo de conexiones para que todos los usuarios estén conectados (sin ciclos).");
        info.setBorder(new EmptyBorder(0, 0, 5, 0));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private void ejecutarKruskal() {
        try {
            servicio.calcularRedMinima();
            StringBuilder sb = new StringBuilder();
            int total = 0;
            for (Conexion c : servicio.getRedMinima().getConexiones()) {
                sb.append(c.getOrigen().getNombre()).append(" --(").append(c.getPeso()).append(")--> ")
                        .append(c.getDestino().getNombre()).append("\n");
                total += c.getPeso();
            }
            sb.append("\nPeso total de la red mínima: ").append(total);
            areaKruskal.setText(sb.toString());
        } catch (Exception ex) {
            areaKruskal.setText("Error: " + ex.getMessage());
        }
    }

    private JPanel panelRecomendacion() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Usuario origen:"));
        comboUsuarios = new JComboBox<>();
        comboUsuarios.setPreferredSize(new Dimension(200, 28));
        top.add(comboUsuarios);
        JButton btnDijkstra = new JButton("Calcular distancias (Dijkstra)");
        btnDijkstra.addActionListener(e -> ejecutarDijkstra());
        top.add(btnDijkstra);
        p.add(top, BorderLayout.NORTH);

        areaDijkstra = new JTextArea(18, 50);
        areaDijkstra.setEditable(false);
        areaDijkstra.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        p.add(new JScrollPane(areaDijkstra), BorderLayout.CENTER);

        JLabel info = new JLabel("Distancias mínimas (peso de conexiones). Recomendación: usuarios a menor distancia = amigos potenciales.");
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private void actualizarComboUsuarios() {
        comboUsuarios.removeAllItems();
        for (Usuario u : servicio.getUsuarios()) {
            comboUsuarios.addItem(u);
        }
    }

    private void ejecutarDijkstra() {
        Usuario origen = (Usuario) comboUsuarios.getSelectedItem();
        if (origen == null) return;
        try {
            Map<Usuario, Integer> distancias = servicio.calcularDistanciasDesde(origen);
            StringBuilder sb = new StringBuilder();
            sb.append("Distancias desde: ").append(origen.getNombre()).append("\n\n");
            distancias.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .forEach(entry -> {
                        int d = entry.getValue();
                        String distStr = d == Integer.MAX_VALUE ? "∞" : String.valueOf(d);
                        sb.append("  ").append(entry.getKey().getNombre()).append("  →  ").append(distStr).append("\n");
                    });
            areaDijkstra.setText(sb.toString());
        } catch (Exception ex) {
            areaDijkstra.setText("Error: " + ex.getMessage());
        }
    }

    private JPanel panelPortada() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnOptima = new JButton("Calcular portada óptima (Programación dinámica)");
        btnOptima.addActionListener(e -> ejecutarPortadaOptima());
        p.add(btnOptima, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        areaPortadaCandidatas = new JTextArea(18, 25);
        areaPortadaCandidatas.setEditable(false);
        areaPortadaCandidatas.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        content.add(new JScrollPane(areaPortadaCandidatas));

        areaPortadaOptima = new JTextArea(18, 25);
        areaPortadaOptima.setEditable(false);
        areaPortadaOptima.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        content.add(new JScrollPane(areaPortadaOptima));

        p.add(content, BorderLayout.CENTER);

        JLabel info = new JLabel("Izq: candidatas (beneficio = likes*2 + comentarios*10, tamaño). Der: selección óptima sin exceder capacidad " + servicio.getCapacidadPortada() + ".");
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private void actualizarCandidatasPortada() {
        List<Publicacion> list = servicio.getPublicacionesCandidatas();
        StringBuilder sb = new StringBuilder("Publicaciones candidatas:\n\n");
        for (int i = 0; i < list.size(); i++) {
            Publicacion pub = list.get(i);
            sb.append(i + 1).append(". beneficio=").append(pub.ponderar()).append(", tamaño=").append(pub.getTamanio()).append("\n");
        }
        areaPortadaCandidatas.setText(sb.toString());
    }

    private void ejecutarPortadaOptima() {
        try {
            servicio.calcularPortadaOptima();
            List<Publicacion> seleccionadas = servicio.getPortadaOptima();
            StringBuilder sb = new StringBuilder("Portada óptima:\n\n");
            int beneficioTotal = 0;
            int tamanioTotal = 0;
            for (int i = 0; i < seleccionadas.size(); i++) {
                Publicacion pub = seleccionadas.get(i);
                beneficioTotal += pub.ponderar();
                tamanioTotal += pub.getTamanio();
                sb.append(i + 1).append(". beneficio=").append(pub.ponderar()).append(", tamaño=").append(pub.getTamanio()).append("\n");
            }
            sb.append("\nBeneficio total: ").append(beneficioTotal).append(", tamaño usado: ").append(tamanioTotal).append("/").append(servicio.getCapacidadPortada());
            areaPortadaOptima.setText(sb.toString());
        } catch (Exception ex) {
            areaPortadaOptima.setText("Error: " + ex.getMessage());
        }
    }

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
