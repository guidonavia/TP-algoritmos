package org.uade.progra3.ui;

import org.uade.progra3.grafos.ConectividadBFS;
import org.uade.progra3.modelo.Administrador;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Grupo;
import org.uade.progra3.modelo.Publicacion;
import org.uade.progra3.modelo.Usuario;
import org.uade.progra3.negocio.AsignacionAdminDP;
import org.uade.progra3.servicio.RedSocialServicio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal del prototipo: red social universitaria.
 * Cinco pestañas: Red mínima (Kruskal), Recomendación (Dijkstra), Portada óptima (DP),
 * Simulación de bloqueo (BFS), Asignación de admins (DP bitmask).
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
    // Pestaña 4: Simulación de bloqueo
    private JComboBox<Usuario> comboBloqueador;
    private JComboBox<Usuario> comboBloqueado;
    private JTextArea areaBloqueo;
    // Pestaña 5: Asignación de administradores
    private JTextArea areaMatrizCostos;
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
        tabs.addTab("4. Bloqueo (BFS)", panelBloqueo());
        tabs.addTab("5. Asignación admins (DP)", panelAsignacion());
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
            actualizarCombosBloqueo();
            actualizarMatrizCostos();
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

    // --- Pestaña 4: Simulación de Bloqueo (BFS) ---

    private JPanel panelBloqueo() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Bloqueador:"));
        comboBloqueador = new JComboBox<>();
        comboBloqueador.setPreferredSize(new Dimension(160, 28));
        top.add(comboBloqueador);
        top.add(new JLabel("Bloqueado:"));
        comboBloqueado = new JComboBox<>();
        comboBloqueado.setPreferredSize(new Dimension(160, 28));
        top.add(comboBloqueado);
        JButton btnBloqueo = new JButton("Simular bloqueo (BFS)");
        btnBloqueo.addActionListener(e -> ejecutarBloqueo());
        top.add(btnBloqueo);
        p.add(top, BorderLayout.NORTH);

        areaBloqueo = new JTextArea(20, 60);
        areaBloqueo.setEditable(false);
        areaBloqueo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        p.add(new JScrollPane(areaBloqueo), BorderLayout.CENTER);

        JLabel info = new JLabel("Simula un bloqueo entre dos usuarios y verifica si el grafo sigue siendo conexo (BFS).");
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private void actualizarCombosBloqueo() {
        if (comboBloqueador == null || comboBloqueado == null) return;
        comboBloqueador.removeAllItems();
        comboBloqueado.removeAllItems();
        for (Usuario u : servicio.getUsuarios()) {
            comboBloqueador.addItem(u);
            comboBloqueado.addItem(u);
        }
        if (comboBloqueado.getItemCount() > 1) {
            comboBloqueado.setSelectedIndex(1);
        }
    }

    private void ejecutarBloqueo() {
        Usuario bloqueador = (Usuario) comboBloqueador.getSelectedItem();
        Usuario bloqueado = (Usuario) comboBloqueado.getSelectedItem();
        if (bloqueador == null || bloqueado == null) return;
        if (bloqueador.equals(bloqueado)) {
            areaBloqueo.setText("Error: el bloqueador y el bloqueado deben ser usuarios distintos.");
            return;
        }

        try {
            ConectividadBFS.ResultadoBloqueo resultado = servicio.simularBloqueo(bloqueador, bloqueado);

            StringBuilder sb = new StringBuilder();
            sb.append("=== Simulación de Bloqueo (BFS) ===\n\n");
            sb.append("Bloqueador: ").append(bloqueador.getNombre()).append("\n");
            sb.append("Bloqueado:  ").append(bloqueado.getNombre()).append("\n\n");

            if (resultado.esConexo()) {
                sb.append("RESULTADO: El grafo SIGUE SIENDO CONEXO.\n");
                sb.append("Existen caminos alternativos que mantienen la conectividad.\n");
            } else {
                sb.append("RESULTADO: El grafo QUEDÓ DESCONECTADO.\n");
                sb.append("Se detectaron ").append(resultado.getComponentes().size())
                  .append(" componentes conexas:\n\n");

                for (int i = 0; i < resultado.getComponentes().size(); i++) {
                    sb.append("  Componente ").append(i + 1).append(": ");
                    List<Usuario> comp = resultado.getComponentes().get(i);
                    for (int j = 0; j < comp.size(); j++) {
                        if (j > 0) sb.append(", ");
                        sb.append(comp.get(j).getNombre());
                    }
                    sb.append("\n");
                }

                sb.append("\nConexiones sugeridas para restaurar la conectividad:\n\n");
                for (Conexion c : resultado.getConexionesRestauracion()) {
                    sb.append("  ").append(c.getOrigen().getNombre())
                      .append(" --(").append(c.getPeso()).append(")--> ")
                      .append(c.getDestino().getNombre()).append("\n");
                }
            }

            sb.append("\n--- Análisis de complejidad ---\n");
            sb.append("BFS para detección de componentes: O(V + E)\n");
            sb.append("V = ").append(servicio.getGrafoCompleto().getUsuarios().size())
              .append(" usuarios, E = ").append(servicio.getGrafoCompleto().getConexiones().size())
              .append(" conexiones\n");

            areaBloqueo.setText(sb.toString());
        } catch (Exception ex) {
            areaBloqueo.setText("Error: " + ex.getMessage());
        }
    }

    // --- Pestaña 5: Asignación de Administradores (DP bitmask) ---

    private JPanel panelAsignacion() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnAsignar = new JButton("Calcular asignación óptima (DP bitmask)");
        btnAsignar.addActionListener(e -> ejecutarAsignacion());
        p.add(btnAsignar, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        areaMatrizCostos = new JTextArea(18, 25);
        areaMatrizCostos.setEditable(false);
        areaMatrizCostos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        content.add(new JScrollPane(areaMatrizCostos));

        areaAsignacion = new JTextArea(18, 25);
        areaAsignacion.setEditable(false);
        areaAsignacion.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        content.add(new JScrollPane(areaAsignacion));

        p.add(content, BorderLayout.CENTER);

        JLabel info = new JLabel("Izq: matriz de ineficiencias (grupo x admin). Der: asignación óptima que minimiza la ineficiencia total.");
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private void actualizarMatrizCostos() {
        if (areaMatrizCostos == null) return;
        List<Grupo> grupos = servicio.getGrupos();
        List<Administrador> admins = servicio.getAdministradores();
        int[][] costos = servicio.getCostoAsignacion();

        if (grupos.isEmpty() || admins.isEmpty() || costos == null) {
            areaMatrizCostos.setText("No hay datos de asignación cargados.");
            return;
        }

        StringBuilder sb = new StringBuilder("Matriz de ineficiencias:\n\n");

        // Header con nombres de admins
        sb.append(String.format("%-14s", ""));
        for (Administrador a : admins) {
            sb.append(String.format("%-14s", a.getNombre()));
        }
        sb.append("\n");

        // Filas: cada grupo con sus costos
        for (int i = 0; i < grupos.size(); i++) {
            sb.append(String.format("%-14s", grupos.get(i).getNombre()));
            for (int j = 0; j < admins.size(); j++) {
                sb.append(String.format("%-14d", costos[i][j]));
            }
            sb.append("\n");
        }
        areaMatrizCostos.setText(sb.toString());
    }

    private void ejecutarAsignacion() {
        try {
            AsignacionAdminDP.ResultadoAsignacion resultado = servicio.calcularAsignacionOptima();

            StringBuilder sb = new StringBuilder("=== Asignación Óptima (DP bitmask) ===\n\n");
            int[] asig = resultado.getAsignacion();
            String[] nombresGrupos = resultado.getNombresGrupos();
            String[] nombresAdmins = resultado.getNombresAdmins();
            int[][] costos = resultado.getCostos();

            for (int i = 0; i < asig.length; i++) {
                sb.append("  ").append(nombresGrupos[i])
                  .append(" ← ").append(nombresAdmins[asig[i]])
                  .append(" (ineficiencia: ").append(costos[i][asig[i]]).append(")\n");
            }

            sb.append("\nIneficiencia total mínima: ").append(resultado.getCostoTotal()).append("\n");

            sb.append("\n--- Análisis de complejidad ---\n");
            int n = nombresAdmins.length;
            sb.append("DP bitmask: O(n * 2^n) donde n = ").append(n).append("\n");
            sb.append("Estados evaluados: ").append(n).append(" * 2^").append(n)
              .append(" = ").append(n * (1 << n)).append("\n");
            sb.append("Espacio: O(2^").append(n).append(") = ").append(1 << n).append(" estados\n");

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
