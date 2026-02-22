package org.uade.progra3.ui;

import org.uade.progra3.modelo.Administrador;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Grupo;
import org.uade.progra3.modelo.Publicacion;
import org.uade.progra3.modelo.Usuario;
import org.uade.progra3.negocio.ResultadoAsignacion;
import org.uade.progra3.negocio.ResultadoBloqueo;
import org.uade.progra3.servicio.RedSocialServicio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal del prototipo: red social universitaria.
 * Cinco pestañas: Red mínima (Kruskal), Recomendación (Dijkstra), Portada óptima (DP Knapsack),
 * Bloqueo de conexiones (Backtracking) y Asignación de administradores (DP Bitmask).
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
    private JComboBox<Conexion> comboConexiones;
    private JTextArea areaBloqueo;
    private JTable tablaEficiencias;
    private JTextArea areaAsignacion;

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
        tabs.addTab("4. Bloqueo de conexión (Backtracking)", panelBloqueo());
        tabs.addTab("5. Asignación de admins (DP)", panelAsignacion());
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
            actualizarComboConexiones();
            actualizarTablaEficiencias();
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

    // -------------------------------------------------------------------------
    // Pestaña 4: Bloqueo de conexión (Backtracking – Opcional 1)
    // -------------------------------------------------------------------------

    private JPanel panelBloqueo() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Conexión a bloquear:"));
        comboConexiones = new JComboBox<>();
        comboConexiones.setPreferredSize(new Dimension(280, 28));
        top.add(comboConexiones);
        JButton btnBloquear = new JButton("Simular bloqueo (Backtracking)");
        btnBloquear.addActionListener(e -> ejecutarBloqueo());
        top.add(btnBloquear);
        p.add(top, BorderLayout.NORTH);

        areaBloqueo = new JTextArea();
        areaBloqueo.setEditable(false);
        areaBloqueo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        p.add(new JScrollPane(areaBloqueo), BorderLayout.CENTER);

        JLabel info = new JLabel("Determina si la red sigue conexa tras el bloqueo y, si no, halla el mínimo de nuevas conexiones para restaurarla.");
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private void actualizarComboConexiones() {
        if (comboConexiones == null) return;
        comboConexiones.removeAllItems();
        for (Conexion c : servicio.getConexiones()) {
            comboConexiones.addItem(c);
        }
    }

    private void ejecutarBloqueo() {
        Conexion seleccionada = (Conexion) comboConexiones.getSelectedItem();
        if (seleccionada == null) return;
        try {
            ResultadoBloqueo resultado = servicio.simularBloqueo(seleccionada);
            StringBuilder sb = new StringBuilder();
            sb.append("Conexión bloqueada: ").append(resultado.getConexionBloqueada()).append("\n\n");
            if (resultado.isSigueConectado()) {
                sb.append("Estado: la red sigue completamente conexa.\n");
                sb.append("No es necesario agregar nuevas conexiones.");
            } else {
                sb.append("Estado: la red quedó DESCONECTADA.\n\n");
                sb.append("Conexiones mínimas para restaurar la conectividad (").append(resultado.getConexionesNecesarias().size()).append("):\n");
                for (Conexion c : resultado.getConexionesNecesarias()) {
                    sb.append("  + ").append(c.getOrigen().getNombre()).append(" ↔ ").append(c.getDestino().getNombre()).append("\n");
                }
            }
            areaBloqueo.setText(sb.toString());
        } catch (Exception ex) {
            areaBloqueo.setText("Error: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Pestaña 5: Asignación de administradores (DP Bitmask – Opcional 3)
    // -------------------------------------------------------------------------

    private JPanel panelAsignacion() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnAsignar = new JButton("Calcular asignación óptima (Programación dinámica)");
        btnAsignar.addActionListener(e -> ejecutarAsignacion());
        p.add(btnAsignar, BorderLayout.NORTH);

        tablaEficiencias = new JTable();
        tablaEficiencias.setEnabled(false);
        tablaEficiencias.setRowHeight(22);

        areaAsignacion = new JTextArea();
        areaAsignacion.setEditable(false);
        areaAsignacion.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        JPanel izq = new JPanel(new BorderLayout(0, 4));
        izq.add(new JLabel("Eficiencias (admin × grupo):"), BorderLayout.NORTH);
        izq.add(new JScrollPane(tablaEficiencias), BorderLayout.CENTER);
        content.add(izq);
        content.add(new JScrollPane(areaAsignacion));
        p.add(content, BorderLayout.CENTER);

        JLabel info = new JLabel("Ineficiencia = 100 − eficiencia. La DP minimiza la suma total de ineficiencias asignando un admin a cada grupo.");
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private void actualizarTablaEficiencias() {
        if (tablaEficiencias == null) return;
        List<Grupo> grupos = servicio.getGrupos();
        List<Administrador> admins = servicio.getAdministradores();
        if (grupos.isEmpty() || admins.isEmpty()) return;

        String[] columnas = new String[grupos.size() + 1];
        columnas[0] = "Administrador";
        for (int g = 0; g < grupos.size(); g++) columnas[g + 1] = grupos.get(g).getNombre();

        Object[][] datos = new Object[admins.size()][columnas.length];
        for (int a = 0; a < admins.size(); a++) {
            datos[a][0] = admins.get(a).getNombre();
            for (int g = 0; g < grupos.size(); g++) {
                datos[a][g + 1] = admins.get(a).getEficiencia(g);
            }
        }
        tablaEficiencias.setModel(new DefaultTableModel(datos, columnas));
    }

    private void ejecutarAsignacion() {
        try {
            ResultadoAsignacion resultado = servicio.calcularAsignacionAdministradores();
            List<Grupo> grupos = resultado.getGrupos();
            StringBuilder sb = new StringBuilder("Asignación óptima:\n\n");
            for (int g = 0; g < grupos.size(); g++) {
                Administrador admin = resultado.getAdminParaGrupo(g);
                int ef = resultado.getEficienciaParaGrupo(g);
                sb.append(String.format("  %-14s → %-16s  (eficiencia=%d, ineficiencia=%d)%n",
                        grupos.get(g).getNombre(), admin.getNombre(), ef, 100 - ef));
            }
            sb.append("\nIneficiencia total mínima: ").append(resultado.getIneficienciaTotal());
            areaAsignacion.setText(sb.toString());
        } catch (Exception ex) {
            areaAsignacion.setText("Error: " + ex.getMessage());
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
