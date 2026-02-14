package org.uade.progra3.ui;

import org.uade.progra3.grafos.Grafo;
import org.uade.progra3.modelo.Conexion;
import org.uade.progra3.modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Panel que dibuja un grafo: nodos y aristas con flechas.
 * Modo circular: aristas curvas por el centro (más visibles). Modo árbol: layout jerárquico para MST.
 */
public class GrafoDiagramPanel extends JPanel {

    private static final int NODE_RADIUS = 22;
    private static final int MARGIN = 44;
    private static final float EDGE_STROKE = 2.5f;
    private static final Color EDGE_COLOR = new Color(45, 55, 72);
    private static final Color WEIGHT_BG = new Color(248, 250, 252);
    private static final Color WEIGHT_BORDER = new Color(203, 213, 225);

    private Grafo grafo;
    private List<Usuario> ordenUsuarios;
    private Map<Usuario, Point> posiciones;
    private String titulo;
    private boolean modoArbol;
    private Map<Usuario, Integer> distancias;  // Dijkstra: distancia mínima por nodo
    private Usuario nodoOrigen;                // Dijkstra: nodo origen resaltado

    public GrafoDiagramPanel(String titulo) {
        this.titulo = titulo;
        this.grafo = null;
        this.ordenUsuarios = new ArrayList<>();
        this.modoArbol = false;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    }

    /** true = layout en árbol (para MST); false = circular con aristas por el centro. */
    public void setModoArbol(boolean modoArbol) {
        if (this.modoArbol != modoArbol) {
            this.modoArbol = modoArbol;
            posiciones = null;
            repaint();
        }
    }

    /** Muestra distancias de Dijkstra sobre los nodos: origen en verde, alcanzables coloreados, ∞ en rojo. */
    public void setDistancias(Map<Usuario, Integer> distancias, Usuario origen) {
        this.distancias = distancias;
        this.nodoOrigen = origen;
        repaint();
    }

    /** Limpia las distancias de Dijkstra. */
    public void clearDistancias() {
        this.distancias = null;
        this.nodoOrigen = null;
        repaint();
    }

    public void setGrafo(Grafo grafo, List<Usuario> ordenUsuarios) {
        this.grafo = grafo;
        this.ordenUsuarios = ordenUsuarios != null ? new ArrayList<>(ordenUsuarios) : new ArrayList<>();
        posiciones = null;
        this.distancias = null;
        this.nodoOrigen = null;
        repaint();
    }

    public void setGrafo(Grafo grafo, Set<Usuario> usuarios) {
        setGrafo(grafo, usuarios == null ? null : new ArrayList<>(usuarios));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titulo, (w - fm.stringWidth(titulo)) / 2, fm.getAscent() + 4);

        if (grafo == null || grafo.getUsuarios().isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("Sin datos", w / 2 - 25, h / 2 - 6);
            return;
        }

        if (ordenUsuarios.isEmpty()) {
            ordenUsuarios = new ArrayList<>(grafo.getUsuarios());
        }

        calcularPosiciones(w, h);

        // Aristas primero (más gruesas, visibles, con flecha)
        g2.setStroke(new BasicStroke(EDGE_STROKE));
        g2.setColor(EDGE_COLOR);
        for (Conexion c : grafo.getConexiones()) {
            Point p1 = posiciones.get(c.getOrigen());
            Point p2 = posiciones.get(c.getDestino());
            if (p1 == null || p2 == null) continue;
            dibujarAristaRecta(g2, p1, p2, c.getPeso());
        }

        // Nodos encima (con color según distancias si están seteadas)
        int maxDistFinita = 1;
        if (distancias != null) {
            maxDistFinita = distancias.values().stream()
                    .filter(d -> d != Integer.MAX_VALUE && d > 0)
                    .max(Integer::compareTo).orElse(1);
        }
        for (Usuario u : ordenUsuarios) {
            Point p = posiciones.get(u);
            if (p == null) continue;

            Color fillColor = new Color(70, 130, 180); // default azul
            Color borderColor = new Color(30, 64, 120);
            if (distancias != null) {
                if (u.equals(nodoOrigen)) {
                    fillColor = new Color(34, 139, 34);   // verde: origen
                    borderColor = new Color(20, 90, 20);
                } else {
                    int dist = distancias.getOrDefault(u, Integer.MAX_VALUE);
                    if (dist == Integer.MAX_VALUE) {
                        fillColor = new Color(180, 50, 50);  // rojo: inalcanzable
                        borderColor = new Color(120, 30, 30);
                    } else {
                        // Degradado azul → naranja según distancia relativa
                        float t = Math.min((float) dist / maxDistFinita, 1f);
                        int r = (int) (70 + t * 185);   // 70→255
                        int gr = (int) (130 - t * 30);   // 130→100
                        int b = (int) (180 - t * 130);   // 180→50
                        fillColor = new Color(r, gr, b);
                        borderColor = fillColor.darker();
                    }
                }
            }
            g2.setColor(fillColor);
            g2.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            // Nombre dentro del nodo
            String label = abreviar(u.getNombre(), 8);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 10f));
            FontMetrics nodeFm = g2.getFontMetrics();
            g2.drawString(label, p.x - nodeFm.stringWidth(label) / 2, p.y - 2);

            // Distancia debajo del nombre (dentro del nodo)
            if (distancias != null) {
                int dist = distancias.getOrDefault(u, Integer.MAX_VALUE);
                String distLabel = u.equals(nodoOrigen) ? "0 (origen)" : (dist == Integer.MAX_VALUE ? "∞" : String.valueOf(dist));
                g2.setFont(getFont().deriveFont(Font.BOLD, 9f));
                FontMetrics distFm = g2.getFontMetrics();
                g2.setColor(new Color(255, 255, 255, 200));
                g2.drawString(distLabel, p.x - distFm.stringWidth(distLabel) / 2, p.y + distFm.getAscent() - 1);
            }
        }
    }

    private void dibujarAristaRecta(Graphics2D g2, Point p1, Point p2, int peso) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double len = Math.hypot(dx, dy);
        if (len < 1e-6) return;
        double ux = dx / len;
        double uy = dy / len;
        int inset = NODE_RADIUS + 4;
        int x1 = (int) (p1.x + ux * inset);
        int y1 = (int) (p1.y + uy * inset);
        int x2 = (int) (p2.x - ux * inset);
        int y2 = (int) (p2.y - uy * inset);
        g2.drawLine(x1, y1, x2, y2);
        dibujarFlecha(g2, x1, y1, x2, y2);
        dibujarPeso(g2, (x1 + x2) / 2, (y1 + y2) / 2, peso);
    }

    private void dibujarFlecha(Graphics2D g2, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len < 1e-6) return;
        double ux = dx / len;
        double uy = dy / len;
        int arrowLen = 10;
        int ax1 = (int) (x2 - ux * arrowLen + uy * 4);
        int ay1 = (int) (y2 - uy * arrowLen - ux * 4);
        int ax2 = (int) (x2 - ux * arrowLen - uy * 4);
        int ay2 = (int) (y2 - uy * arrowLen + ux * 4);
        g2.fill(new Polygon(new int[]{x2, ax1, ax2}, new int[]{y2, ay1, ay2}, 3));
    }

    private void dibujarPeso(Graphics2D g2, int x, int y, int peso) {
        String s = String.valueOf(peso);
        g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(s);
        int th = fm.getAscent();
        int pad = 4;
        int rx = x - tw / 2 - pad;
        int ry = y - th / 2 - pad;
        g2.setColor(WEIGHT_BG);
        g2.fillRoundRect(rx, ry, tw + pad * 2, th + pad * 2, 6, 6);
        g2.setColor(WEIGHT_BORDER);
        g2.drawRoundRect(rx, ry, tw + pad * 2, th + pad * 2, 6, 6);
        g2.setColor(EDGE_COLOR);
        g2.drawString(s, x - tw / 2, y + fm.getAscent() / 2 - 2);
    }

    private void calcularPosiciones(int w, int h) {
        if (posiciones != null) return;
        posiciones = new HashMap<>();
        int cx = w / 2;
        int cy = h / 2;
        if (modoArbol && grafo != null) {
            calcularPosicionesArbol(w, h, cx, cy);
        } else {
            calcularPosicionesFuerzas(w, h);
        }
    }

    /**
     * Force-directed layout: simula fuerzas de repulsión entre todos los nodos
     * y atracción entre nodos conectados para distribuirlos con distancia clara.
     */
    private void calcularPosicionesFuerzas(int w, int h) {
        int n = ordenUsuarios.size();
        if (n == 0) return;
        int titleOffset = 24;
        int usableW = w - 2 * MARGIN;
        int usableH = h - 2 * MARGIN - titleOffset;
        double[] px = new double[n];
        double[] py = new double[n];

        // Posiciones iniciales en círculo amplio para arrancar la simulación
        double cx = usableW / 2.0;
        double cy = usableH / 2.0;
        double r = Math.min(usableW, usableH) / 2.5;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            px[i] = cx + r * Math.cos(angle);
            py[i] = cy + r * Math.sin(angle);
        }

        // Mapa de índices
        Map<Usuario, Integer> idx = new HashMap<>();
        for (int i = 0; i < n; i++) idx.put(ordenUsuarios.get(i), i);

        // Pares conectados
        Set<Long> connected = new HashSet<>();
        for (Conexion c : grafo.getConexiones()) {
            Integer a = idx.get(c.getOrigen());
            Integer b = idx.get(c.getDestino());
            if (a != null && b != null) {
                connected.add((long) Math.min(a, b) * n + Math.max(a, b));
            }
        }

        // Simulación (100 iteraciones)
        double repulsion = 8000.0;
        double attraction = 0.02;
        double idealDist = Math.min(usableW, usableH) / (Math.sqrt(n) + 1);
        for (int iter = 0; iter < 100; iter++) {
            double[] fx = new double[n];
            double[] fy = new double[n];
            double temp = 0.3 * (1.0 - iter / 100.0); // enfriamiento

            // Repulsión entre todos los pares
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double dx = px[i] - px[j];
                    double dy = py[i] - py[j];
                    double dist = Math.max(Math.hypot(dx, dy), 1.0);
                    double force = repulsion / (dist * dist);
                    double fdx = force * dx / dist;
                    double fdy = force * dy / dist;
                    fx[i] += fdx;
                    fy[i] += fdy;
                    fx[j] -= fdx;
                    fy[j] -= fdy;
                }
            }

            // Atracción entre conectados
            for (Conexion c : grafo.getConexiones()) {
                Integer ai = idx.get(c.getOrigen());
                Integer bi = idx.get(c.getDestino());
                if (ai == null || bi == null || ai.equals(bi)) continue;
                double dx = px[ai] - px[bi];
                double dy = py[ai] - py[bi];
                double dist = Math.max(Math.hypot(dx, dy), 1.0);
                double force = attraction * (dist - idealDist);
                double fdx = force * dx / dist;
                double fdy = force * dy / dist;
                fx[ai] -= fdx;
                fy[ai] -= fdy;
                fx[bi] += fdx;
                fy[bi] += fdy;
            }

            // Aplicar
            for (int i = 0; i < n; i++) {
                double fLen = Math.hypot(fx[i], fy[i]);
                double maxMove = Math.max(20 * temp, 2.0);
                if (fLen > maxMove) {
                    fx[i] = fx[i] / fLen * maxMove;
                    fy[i] = fy[i] / fLen * maxMove;
                }
                px[i] += fx[i];
                py[i] += fy[i];
                px[i] = Math.max(NODE_RADIUS, Math.min(usableW - NODE_RADIUS, px[i]));
                py[i] = Math.max(NODE_RADIUS, Math.min(usableH - NODE_RADIUS, py[i]));
            }
        }

        // Asignar posiciones finales (trasladadas al área del panel)
        for (int i = 0; i < n; i++) {
            int x = MARGIN + (int) px[i];
            int y = MARGIN + titleOffset + (int) py[i];
            posiciones.put(ordenUsuarios.get(i), new Point(x, y));
        }
    }

    /** Layout en árbol: BFS desde el primer nodo, niveles horizontales. */
    private void calcularPosicionesArbol(int w, int h, int cx, int cy) {
        Map<Usuario, List<Usuario>> vecinos = new HashMap<>();
        for (Usuario u : ordenUsuarios) vecinos.put(u, new ArrayList<>());
        for (Conexion c : grafo.getConexiones()) {
            vecinos.get(c.getOrigen()).add(c.getDestino());
            vecinos.get(c.getDestino()).add(c.getOrigen());
        }
        Usuario raiz = ordenUsuarios.isEmpty() ? null : ordenUsuarios.get(0);
        if (raiz == null) return;
        Map<Usuario, Integer> nivel = new HashMap<>();
        nivel.put(raiz, 0);
        Queue<Usuario> q = new LinkedList<>();
        q.add(raiz);
        while (!q.isEmpty()) {
            Usuario u = q.poll();
            for (Usuario v : vecinos.get(u)) {
                if (!nivel.containsKey(v)) {
                    nivel.put(v, nivel.get(u) + 1);
                    q.add(v);
                }
            }
        }
        int maxNivel = nivel.values().stream().max(Integer::compareTo).orElse(0);
        int nivelHeight = maxNivel <= 0 ? h - 2 * MARGIN : (h - 2 * MARGIN) / (maxNivel + 1);
        int yBase = MARGIN + 20;
        Map<Integer, List<Usuario>> porNivel = new HashMap<>();
        for (Usuario u : ordenUsuarios) {
            int n = nivel.getOrDefault(u, 0);
            porNivel.computeIfAbsent(n, k -> new ArrayList<>()).add(u);
        }
        for (Map.Entry<Integer, List<Usuario>> e : porNivel.entrySet()) {
            int n = e.getKey();
            List<Usuario> lista = e.getValue();
            int y = yBase + n * nivelHeight;
            int ancho = w - 2 * MARGIN;
            for (int i = 0; i < lista.size(); i++) {
                int x = MARGIN + (lista.size() == 1 ? ancho / 2 : (ancho * (i + 1)) / (lista.size() + 1));
                posiciones.put(lista.get(i), new Point(x, y));
            }
        }
    }

    private static String abreviar(String nombre, int maxLen) {
        if (nombre == null) return "";
        if (nombre.length() <= maxLen) return nombre;
        int idx = nombre.indexOf('-');
        if (idx > 0) return nombre.substring(0, Math.min(idx, maxLen));
        return nombre.substring(0, maxLen);
    }
}
