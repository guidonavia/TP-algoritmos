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
    private GrafoDiagramPanel diagramaGrafoOriginal;
    private GrafoDiagramPanel diagramaMST;
    private JTextArea areaKruskal;
    private JComboBox<Usuario> comboUsuarios;
    private JTextArea areaDijkstra;
    private GrafoDiagramPanel diagramaDijkstra;
    private JTextArea areaPortadaCandidatas;
    private JTextArea areaPortadaOptima;

    public RedSocialFrame() {
        servicio = new RedSocialServicio();
        setTitle("Red Social Universitaria - Prototipo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
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
            actualizarDiagramaGrafoOriginal();
            actualizarDiagramaDijkstra();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarDiagramaDijkstra() {
        if (diagramaDijkstra != null) {
            diagramaDijkstra.setGrafo(servicio.getGrafoCompleto(), servicio.getUsuarios());
        }
    }

    private void actualizarDiagramaGrafoOriginal() {
        if (diagramaGrafoOriginal != null) {
            diagramaGrafoOriginal.setGrafo(servicio.getGrafoCompleto(), servicio.getUsuarios());
        }
    }

    private JPanel panelRedMinima() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnCalcular = new JButton("Calcular red mínima (Kruskal)");
        btnCalcular.addActionListener(e -> ejecutarKruskal());
        p.add(btnCalcular, BorderLayout.NORTH);

        // Grafo original: aristas curvas por el centro y flechas. MST: layout en árbol.
        diagramaGrafoOriginal = new GrafoDiagramPanel("Grafo original (nodos y aristas)");
        diagramaMST = new GrafoDiagramPanel("Red mínima - MST (Kruskal)");
        diagramaMST.setModoArbol(true);
        diagramaMST.setGrafo(null, List.of());

        JPanel diagramas = new JPanel(new GridLayout(1, 2, 10, 0));
        diagramas.add(diagramaGrafoOriginal);
        diagramas.add(diagramaMST);
        diagramaGrafoOriginal.setPreferredSize(new Dimension(420, 350));
        diagramaMST.setPreferredSize(new Dimension(420, 350));
        p.add(diagramas, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout(0, 4));
        areaKruskal = new JTextArea(8, 50);
        areaKruskal.setEditable(false);
        areaKruskal.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        sur.add(new JScrollPane(areaKruskal), BorderLayout.CENTER);
        JLabel info = new JLabel("Lista de conexiones de la red mínima y peso total:");
        sur.add(info, BorderLayout.SOUTH);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private void ejecutarKruskal() {
        try {
            servicio.calcularRedMinima();
            // Actualizar diagrama del MST (mismos nodos, solo aristas del árbol)
            diagramaMST.setGrafo(servicio.getRedMinima(), servicio.getUsuarios());
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

        // Diagrama del grafo con distancias + lista de texto
        diagramaDijkstra = new GrafoDiagramPanel("Distancias mínimas (Dijkstra)");
        diagramaDijkstra.setPreferredSize(new Dimension(420, 350));

        areaDijkstra = new JTextArea(12, 30);
        areaDijkstra.setEditable(false);
        areaDijkstra.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        content.add(diagramaDijkstra);
        content.add(new JScrollPane(areaDijkstra));
        p.add(content, BorderLayout.CENTER);

        JLabel info = new JLabel("Verde=origen, azul→naranja=cerca→lejos, rojo=inalcanzable (∞ = sin camino posible).");
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

            // Actualizar diagrama con distancias coloreadas
            diagramaDijkstra.setDistancias(distancias, origen);

            // Lista de texto
            StringBuilder sb = new StringBuilder();
            sb.append("Distancias desde: ").append(origen.getNombre()).append("\n\n");
            distancias.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .forEach(entry -> {
                        int d = entry.getValue();
                        String distStr = d == Integer.MAX_VALUE ? "∞ (inalcanzable)" : String.valueOf(d);
                        String tag = entry.getKey().equals(origen) ? " ← origen" : "";
                        sb.append("  ").append(entry.getKey().getNombre()).append("  →  ").append(distStr).append(tag).append("\n");
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
