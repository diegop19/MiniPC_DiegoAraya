package Vista;
 
import Controlador.MiniPC;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
 
/**
 * Ventana principal del Gestor de Procesos (Proyecto #1).
 *
 * @author Diego Araya
 */
public class MainFrame extends javax.swing.JFrame {
 
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());
 
    // ---- Colores y fuentes del tema ----
    private static final Color COLOR_PRIMARIO = new Color(41, 98, 168);
    private static final Color COLOR_PRIMARIO_HOVER = new Color(30, 80, 145);
    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_PANEL = Color.WHITE;
    private static final Color COLOR_TEXTO = new Color(40, 40, 40);
    private static final Color COLOR_BORDE = new Color(210, 214, 220);
    private static final Color COLOR_PANTALLA_FONDO = new Color(25, 28, 33);
    private static final Color COLOR_PANTALLA_TEXTO = new Color(100, 230, 120);
    private static final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_BOTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);
 
    // ---- Tablas ----
    private JTable tablaProcesos;
    private JTable tablaMemoria;
    private JTable tablaDisco;
    private DefaultTableModel modeloProcesos;
    private DefaultTableModel modeloMemoria;
    private DefaultTableModel modeloDisco;
 
    // ---- Panel BPC actual ----
    private JLabel lblPid, lblEstado, lblPC, lblIR, lblAC, lblAX, lblBX, lblCX, lblDX;
 
    // ---- Pantalla y teclado ----
    private JTextArea areaPantalla;
    private JTextField txtTeclado;
    private JButton btnEnviarTeclado;
 
    // ---- Botones de control ----
    private JButton btnEjecutar, btnSiguiente, btnLimpiar, btnEstadisticas, btnCargar;
    private JLabel lblArchivos;
 
    // ---- Configuración de memoria/disco ----
    private JTextField txtTamanoMemoria;
    private JTextField txtTamanoDisco;
    private JButton btnConfigurar;
 
    public MainFrame() {
        initComponents();
        construirInterfaz();
    }
 
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 900, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    /**
     * Arma la interfaz completa por código, después de initComponents().
     */
    private void construirInterfaz() {
        setTitle("Proyecto 1 - Gestor de Procesos");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        getContentPane().setLayout(new BorderLayout(10, 10));
 
        getContentPane().add(crearPanelSuperior(), BorderLayout.NORTH);
        getContentPane().add(crearPanelCentral(), BorderLayout.CENTER);
 
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
    }
 
    // =========================================================
    // PANEL SUPERIOR: título + botones + configuración
    // =========================================================
    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(COLOR_FONDO);
 
        JLabel titulo = new JLabel("Gestor de Procesos - Mini PC");
        titulo.setFont(FONT_TITULO);
        titulo.setForeground(COLOR_TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
 
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBotones.setBackground(COLOR_FONDO);
        panelBotones.setAlignmentX(Component.LEFT_ALIGNMENT);
 
        btnEjecutar = crearBoton("Ejecutar");
        btnSiguiente = crearBoton("Siguiente");
        btnLimpiar = crearBoton("Limpiar");
        btnEstadisticas = crearBoton("Estadísticas");
        btnCargar = crearBoton("Cargar archivos");
 
        panelBotones.add(btnEjecutar);
        panelBotones.add(btnSiguiente);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnEstadisticas);
        panelBotones.add(Box.createHorizontalStrut(10));
        panelBotones.add(btnCargar);
 
        lblArchivos = new JLabel("Ningún archivo cargado");
        lblArchivos.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblArchivos.setForeground(Color.GRAY);
        panelBotones.add(lblArchivos);
 
        JPanel panelConfig = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelConfig.setBackground(COLOR_FONDO);
        panelConfig.setAlignmentX(Component.LEFT_ALIGNMENT);
 
        JLabel lblMem = new JLabel("Memoria principal:");
        lblMem.setFont(FONT_LABEL);
        txtTamanoMemoria = new JTextField("256", 5);
 
        JLabel lblDisco = new JLabel("Disco:");
        lblDisco.setFont(FONT_LABEL);
        txtTamanoDisco = new JTextField("512", 5);
 
        btnConfigurar = crearBoton("Configurar");
 
        panelConfig.add(lblMem);
        panelConfig.add(txtTamanoMemoria);
        panelConfig.add(lblDisco);
        panelConfig.add(txtTamanoDisco);
        panelConfig.add(btnConfigurar);
 
        panelSuperior.add(titulo);
        panelSuperior.add(Box.createVerticalStrut(8));
        panelSuperior.add(panelBotones);
        panelSuperior.add(panelConfig);
 
        return panelSuperior;
    }
 
    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_BOTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_PRIMARIO);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_PRIMARIO_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_PRIMARIO);
            }
        });
        return btn;
    }
 
    // =========================================================
    // PANEL CENTRAL: Procesos | BPC | Memoria/Disco + Pantalla
    // =========================================================
    private JPanel crearPanelCentral() {
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBackground(COLOR_FONDO);
 
        // Izquierda: Procesos (arriba) + BPC actual (abajo)
        JPanel panelIzquierdo = new JPanel(new GridLayout(2, 1, 10, 10));
        panelIzquierdo.setBackground(COLOR_FONDO);
        panelIzquierdo.setPreferredSize(new Dimension(260, 0));
        panelIzquierdo.add(crearPanelTablaProcesos());
        panelIzquierdo.add(crearPanelBCP());
 
        // Derecha: Memoria/Disco (arriba) + Pantalla (abajo)
        JPanel panelDerecho = new JPanel(new BorderLayout(10, 10));
        panelDerecho.setBackground(COLOR_FONDO);
 
        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 10, 10));
        panelTablas.setBackground(COLOR_FONDO);
        panelTablas.add(crearPanelTablaMemoria());
        panelTablas.add(crearPanelTablaDisco());
 
        panelDerecho.add(panelTablas, BorderLayout.CENTER);
        panelDerecho.add(crearPanelPantalla(), BorderLayout.SOUTH);
 
        panelCentral.add(panelIzquierdo, BorderLayout.WEST);
        panelCentral.add(panelDerecho, BorderLayout.CENTER);
 
        return panelCentral;
    }
 
    private JPanel crearPanelConTitulo(String titulo) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(COLOR_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COLOR_TEXTO);
        lbl.setOpaque(true);
        lbl.setBackground(COLOR_PANEL);
        panel.add(lbl, BorderLayout.NORTH);
        return panel;
    }
 
    // ---- Tabla Procesos ----
    private JPanel crearPanelTablaProcesos() {
        JPanel panel = crearPanelConTitulo("Procesos");
        modeloProcesos = new DefaultTableModel(new Object[]{"Proceso", "Estado"}, 0);
        tablaProcesos = estilizarTabla(new JTable(modeloProcesos));
        panel.add(new JScrollPane(tablaProcesos), BorderLayout.CENTER);
        return panel;
    }
 
    // ---- Tabla Memoria ----
    private JPanel crearPanelTablaMemoria() {
        JPanel panel = crearPanelConTitulo("Memoria");
        modeloMemoria = new DefaultTableModel(new Object[]{"Pos", "Valor en memoria"}, 0);
        tablaMemoria = estilizarTabla(new JTable(modeloMemoria));
        panel.add(new JScrollPane(tablaMemoria), BorderLayout.CENTER);
        return panel;
    }
 
    // ---- Tabla Disco ----
    private JPanel crearPanelTablaDisco() {
        JPanel panel = crearPanelConTitulo("Disco");
        modeloDisco = new DefaultTableModel(new Object[]{"Pos", "Valor en disco"}, 0);
        tablaDisco = estilizarTabla(new JTable(modeloDisco));
        panel.add(new JScrollPane(tablaDisco), BorderLayout.CENTER);
        return panel;
    }
 
    private JTable estilizarTabla(JTable tabla) {
        tabla.setFont(FONT_MONO);
        tabla.setRowHeight(22);
        tabla.setGridColor(COLOR_BORDE);
        tabla.setSelectionBackground(new Color(200, 220, 245));
        tabla.setSelectionForeground(COLOR_TEXTO);
        tabla.setBackground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
 
        DefaultTableCellRenderer renderizadorCeldas = new DefaultTableCellRenderer();
        renderizadorCeldas.setOpaque(true);
        renderizadorCeldas.setBackground(Color.WHITE);
        renderizadorCeldas.setForeground(COLOR_TEXTO);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderizadorCeldas);
        }
 
        DefaultTableCellRenderer renderizadorHeader = new DefaultTableCellRenderer();
        renderizadorHeader.setOpaque(true);
        renderizadorHeader.setBackground(COLOR_PRIMARIO);
        renderizadorHeader.setForeground(Color.WHITE);
        renderizadorHeader.setFont(FONT_LABEL);
        renderizadorHeader.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getTableHeader().setDefaultRenderer(renderizadorHeader);
 
        return tabla;
    }
 
    // ---- Panel BPC actual ----
    private JPanel crearPanelBCP() {
        JPanel panel = crearPanelConTitulo("BPC actual - CPU1");
 
        JPanel contenido = new JPanel();
        contenido.setBackground(COLOR_PANEL);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
 
        lblPid = crearFilaBCP(contenido, "PID:", "-");
        lblEstado = crearFilaBCP(contenido, "Estado:", "-");
        lblPC = crearFilaBCP(contenido, "PC:", "0");
        lblIR = crearFilaBCP(contenido, "IR:", "-");
        lblAC = crearFilaBCP(contenido, "AC:", "0");
        lblAX = crearFilaBCP(contenido, "AX:", "0");
        lblBX = crearFilaBCP(contenido, "BX:", "0");
        lblCX = crearFilaBCP(contenido, "CX:", "0");
        lblDX = crearFilaBCP(contenido, "DX:", "0");
 
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }
 
    private JLabel crearFilaBCP(JPanel contenedor, String etiqueta, String valorInicial) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(COLOR_PANEL);
        fila.setBorder(new EmptyBorder(3, 0, 3, 0));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
 
        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(FONT_LABEL);
        lblEtiqueta.setForeground(new Color(90, 90, 90));
 
        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(FONT_MONO);
        lblValor.setForeground(COLOR_PRIMARIO);
        lblValor.setHorizontalAlignment(SwingConstants.RIGHT);
 
        fila.add(lblEtiqueta, BorderLayout.WEST);
        fila.add(lblValor, BorderLayout.EAST);
        contenedor.add(fila);
        return lblValor;
    }
 
    // ---- Panel Pantalla + entrada de teclado ----
    private JPanel crearPanelPantalla() {
        JPanel panel = crearPanelConTitulo("Pantalla");
        panel.setPreferredSize(new Dimension(0, 200));
 
        areaPantalla = new JTextArea();
        areaPantalla.setEditable(false);
        areaPantalla.setFont(FONT_MONO);
        areaPantalla.setBackground(COLOR_PANTALLA_FONDO);
        areaPantalla.setForeground(COLOR_PANTALLA_TEXTO);
        areaPantalla.setCaretColor(COLOR_PANTALLA_TEXTO);
        areaPantalla.setBorder(new EmptyBorder(8, 8, 8, 8));
 
        JScrollPane scroll = new JScrollPane(areaPantalla);
 
        JPanel panelTeclado = new JPanel(new BorderLayout(5, 5));
        panelTeclado.setBackground(COLOR_PANEL);
        panelTeclado.setBorder(new EmptyBorder(8, 0, 0, 0));
 
        JLabel lblTeclado = new JLabel("Teclado (INT 09H):");
        lblTeclado.setFont(FONT_LABEL);
 
        txtTeclado = new JTextField();
        btnEnviarTeclado = crearBoton("Enter");
 
        panelTeclado.add(lblTeclado, BorderLayout.WEST);
        panelTeclado.add(txtTeclado, BorderLayout.CENTER);
        panelTeclado.add(btnEnviarTeclado, BorderLayout.EAST);
 
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelTeclado, BorderLayout.SOUTH);
 
        return panel;
    }
 
    // =========================================================
    // Getters para el Controlador
    // =========================================================
    public JButton getBtnEjecutar() { return btnEjecutar; }
    public JButton getBtnSiguiente() { return btnSiguiente; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JButton getBtnEstadisticas() { return btnEstadisticas; }
    public JButton getBtnCargar() { return btnCargar; }
    public JButton getBtnConfigurar() { return btnConfigurar; }
    public JButton getBtnEnviarTeclado() { return btnEnviarTeclado; }
 
    public JTextField getTxtTamanoMemoria() { return txtTamanoMemoria; }
    public JTextField getTxtTamanoDisco() { return txtTamanoDisco; }
    public JTextField getTxtTeclado() { return txtTeclado; }
    public JTextArea getAreaPantalla() { return areaPantalla; }
 
    public DefaultTableModel getModeloProcesos() { return modeloProcesos; }
    public DefaultTableModel getModeloMemoria() { return modeloMemoria; }
    public DefaultTableModel getModeloDisco() { return modeloDisco; }
 
    public JLabel getLblPid() { return lblPid; }
    public JLabel getLblEstado() { return lblEstado; }
    public JLabel getLblPC() { return lblPC; }
    public JLabel getLblIR() { return lblIR; }
    public JLabel getLblAC() { return lblAC; }
    public JLabel getLblAX() { return lblAX; }
    public JLabel getLblBX() { return lblBX; }
    public JLabel getLblCX() { return lblCX; }
    public JLabel getLblDX() { return lblDX; }
 
    public void setArchivosCargados(int cantidad) {
        lblArchivos.setText(cantidad + " archivo(s) cargado(s)");
    }
 
    public void imprimirEnPantalla(String texto) {
        areaPantalla.append(texto + "\n");
        areaPantalla.setCaretPosition(areaPantalla.getDocument().getLength());
    }
 
    public static void main(String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
 
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame ventana = new MainFrame();
            new MiniPC(ventana, 256, 512); // memoria principal y disco por defecto
            ventana.setVisible(true);
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables



}
