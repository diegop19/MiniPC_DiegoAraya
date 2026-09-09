
package Vista;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/**
 *
 * @author Diego Araya 
 */
public class MainFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());
 
    //  colores y fuentes 
    private static final Color COLOR_PRIMARIO = new Color(41, 98, 168);
    private static final Color COLOR_PRIMARIO_HOVER = new Color(30, 80, 145);
    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_PANEL = Color.WHITE;
    private static final Color COLOR_TEXTO = new Color(40, 40, 40);
    private static final Color COLOR_BORDE = new Color(210, 214, 220);
    private static final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_BOTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);
 
    
    private JTable tablaInstrucciones;
    private JTable tablaMemoria;
    private DefaultTableModel modeloInstrucciones;
    private DefaultTableModel modeloMemoria;
    private JLabel lblPC, lblIR, lblAC, lblAX, lblBX, lblCX, lblDX, lblEstado;
    private JButton btnEjecutar, btnPasoAPaso, btnLimpiar, btnEstadisticas, btnCargar;
    private JLabel lblArchivo;
 
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
     *Construye la interfaz, Se llama desde el
     * constructor, después de initComponents()
     */
    private void construirInterfaz() {
        setTitle("Mini PC");
        setSize(900, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        getContentPane().setLayout(new BorderLayout(10, 10));
 
        getContentPane().add(crearPanelSuperior(), BorderLayout.NORTH);
        getContentPane().add(crearPanelCentral(), BorderLayout.CENTER);
 
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
    }
 
    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBackground(COLOR_FONDO);
 
        JLabel titulo = new JLabel("Mini PC - Simulador");
        titulo.setFont(FONT_TITULO);
        titulo.setForeground(COLOR_TEXTO);
        panelSuperior.add(titulo, BorderLayout.NORTH);
 
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotones.setBackground(COLOR_FONDO);
 
        btnEjecutar = crearBoton("Ejecutar");
        btnPasoAPaso = crearBoton("Paso a paso");
        btnLimpiar = crearBoton("Limpiar");
        btnEstadisticas = crearBoton("Estadísticas");
        btnCargar = crearBoton("Cargar archivo");
 
        panelBotones.add(btnEjecutar);
        panelBotones.add(btnPasoAPaso);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnEstadisticas);
        panelBotones.add(Box.createHorizontalStrut(20));
        panelBotones.add(btnCargar);
 
        lblArchivo = new JLabel("Ningún archivo cargado");
        lblArchivo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblArchivo.setForeground(Color.GRAY);
        panelBotones.add(lblArchivo);
 
        panelSuperior.add(panelBotones, BorderLayout.CENTER);
        return panelSuperior;
    }
 
    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_BOTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_PRIMARIO);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_PRIMARIO_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_PRIMARIO);
            }
        });
        return btn;
    }
 
    private JPanel crearPanelCentral() {
        JPanel panelCentral = new JPanel(new GridLayout(1, 3, 10, 10));
        panelCentral.setBackground(COLOR_FONDO);
 
        panelCentral.add(crearPanelTablaInstrucciones());
        panelCentral.add(crearPanelTablaMemoria());
        panelCentral.add(crearPanelBCP());
 
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
        panel.add(lbl, BorderLayout.NORTH);
        return panel;
    }
 
    private JPanel crearPanelTablaInstrucciones() {
        JPanel panel = crearPanelConTitulo("Instrucciones");
        modeloInstrucciones = new DefaultTableModel(new Object[]{"Instrucción", "Binario"}, 0);
        tablaInstrucciones = estilizarTabla(new JTable(modeloInstrucciones));
        panel.add(new JScrollPane(tablaInstrucciones), BorderLayout.CENTER);
        return panel;
    }
 
    private JPanel crearPanelTablaMemoria() {
        JPanel panel = crearPanelConTitulo("Memoria");
        modeloMemoria = new DefaultTableModel(new Object[]{"Pos", "Valor en memoria"}, 0);
        tablaMemoria = estilizarTabla(new JTable(modeloMemoria));
        panel.add(new JScrollPane(tablaMemoria), BorderLayout.CENTER);
        return panel;
    }
 
    private JTable estilizarTabla(JTable tabla) {
        tabla.setFont(FONT_MONO);
        tabla.setRowHeight(24);
        tabla.setGridColor(COLOR_BORDE);
        tabla.setSelectionBackground(new Color(200, 220, 245));
        tabla.setSelectionForeground(COLOR_TEXTO);
        tabla.getTableHeader().setFont(FONT_LABEL);
        tabla.getTableHeader().setBackground(COLOR_PRIMARIO);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }
 
    private JPanel crearPanelBCP() {
        JPanel panel = crearPanelConTitulo("BPC actual - CPU");
 
        JPanel contenido = new JPanel();
        contenido.setBackground(COLOR_PANEL);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
 
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
        fila.setBorder(new EmptyBorder(4, 0, 4, 0));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
 
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
 
    // Getters para el controlador
    public JButton getBtnEjecutar() { return btnEjecutar; }
    public JButton getBtnPasoAPaso() { return btnPasoAPaso; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JButton getBtnEstadisticas() { return btnEstadisticas; }
    public JButton getBtnCargar() { return btnCargar; }
    public DefaultTableModel getModeloInstrucciones() { return modeloInstrucciones; }
    public DefaultTableModel getModeloMemoria() { return modeloMemoria; }
    public JLabel getLblPC() { return lblPC; }
    public JLabel getLblIR() { return lblIR; }
    public JLabel getLblAC() { return lblAC; }
    public JLabel getLblAX() { return lblAX; }
    public JLabel getLblBX() { return lblBX; }
    public JLabel getLblCX() { return lblCX; }
    public JLabel getLblDX() { return lblDX; }
    public JLabel getLblEstado() { return lblEstado; }
    public void setArchivoCargado(File archivo) {
        lblArchivo.setText(archivo.getName());
    }
 
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
 
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables



}
