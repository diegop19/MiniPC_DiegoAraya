package Controlador;

import Modelo.BCP;
import Modelo.CPU;
import Modelo.Ensamblador;
import Modelo.FormatoInvalidoException;
import Modelo.Instruccion;
import Modelo.Memoria;
import Vista.MainFrame;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Diego Araya
 */
public class MiniPC {

    private final MainFrame vista;
    private final Ensamblador ensamblador;
    private Memoria memoria;
    private final CPU cpu;
    private BCP bcp;

    private List<Instruccion> programaActual;
    private final int posicionInicioPrograma;

    public MiniPC(MainFrame vista, int tamanoMemoria) {
        this.vista = vista;
        this.ensamblador = new Ensamblador();
        this.memoria = new Memoria(tamanoMemoria);
        this.cpu = new CPU();
        this.posicionInicioPrograma = memoria.getInicioUsuario();
        this.bcp = new BCP(1, memoria.getInicioUsuario(), memoria.getTamanoTotal() - 1);

        registrarListeners();
    }

    /**
     * Conecta cada boton de la vista con su accion correspondiente.
     */
    private void registrarListeners() {
        vista.getBtnCargar().addActionListener(e -> cargarArchivo());
        vista.getBtnEjecutar().addActionListener(e -> ejecutarTodo());
        vista.getBtnPasoAPaso().addActionListener(e -> ejecutarPaso());
        vista.getBtnLimpiar().addActionListener(e -> limpiar());
        vista.getBtnEstadisticas().addActionListener(e -> mostrarEstadisticas());
        vista.getBtnConfigurarMemoria().addActionListener(e -> configurarMemoria());
    }

    /**
     * Abre un JFileChooser, parsea el .asm seleccionado y carga
     * el programa resultante en memoria 
     */
    private void cargarArchivo() {
        JFileChooser selector = new JFileChooser();
        int resultado = selector.showOpenDialog(vista);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        
        if (!archivo.getName().toLowerCase().endsWith(".asm")) {
            JOptionPane.showMessageDialog(vista,
                    "El archivo debe tener extensión .asm",
                    "Formato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            programaActual = ensamblador.parsearArchivo(archivo);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(vista,
                    "No se pudo leer el archivo: " + e.getMessage(),
                    "Error de lectura", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (FormatoInvalidoException e) {
            JOptionPane.showMessageDialog(vista,
                    e.getMessage(),
                    "Formato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cargarEnMemoria();
        cpu.cargarPrograma(programaActual, posicionInicioPrograma);
        bcp.setEstado(BCP.Estado.NUEVO);

        vista.setArchivoCargado(archivo);
        actualizarTablaInstrucciones();
        actualizarTablaMemoria();
        actualizarPanelBCP();
    }

    /**
     * Escribe cada instruccion del programa en el espacio de
     * usuario de la memoria, una por posicion consecutiva.
     */
    private void cargarEnMemoria() {
        int posicion = posicionInicioPrograma;
        for (Instruccion instr : programaActual) {
            memoria.escribir(posicion, instr.getTextoOriginal());
            posicion++;
        }
    }

    private void ejecutarTodo() {
        if (programaActual == null) {
            JOptionPane.showMessageDialog(vista, "Primero carga un archivo .asm.");
            return;
        }
        cpu.ejecutarTodo();
        actualizarPanelBCP();
    }

    private void ejecutarPaso() {
        if (programaActual == null) {
            JOptionPane.showMessageDialog(vista, "Primero carga un archivo .asm.");
            return;
        }
        boolean ejecuto = cpu.ejecutarPaso();
        actualizarPanelBCP();

        if (!ejecuto) {
            JOptionPane.showMessageDialog(vista, "El programa ya terminó.");
        }
    }

    /**
     * Reinicia memoria, CPU y las tablas de la vista a su estado inicial.
     */
    private void limpiar() {
        memoria.limpiar();
        programaActual = null;
        vista.getModeloInstrucciones().setRowCount(0);
        vista.getModeloMemoria().setRowCount(0);
        cpu.cargarPrograma(null, posicionInicioPrograma);
        bcp.setEstado(BCP.Estado.NUEVO);
        actualizarPanelBCP();
    }

    /**
     * Muestra estadisticas simples del programa cargado.
     */
    private void mostrarEstadisticas() {
        if (programaActual == null) {
            JOptionPane.showMessageDialog(vista, "No hay programa cargado.");
            return;
        }
        String mensaje = "Total de instrucciones: " + programaActual.size() + "\n"
                + "Espacio de memoria usado: " + programaActual.size() + " posiciones\n"
                + "Estado actual: " + bcp.getEstado();
        JOptionPane.showMessageDialog(vista, mensaje, "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarTablaInstrucciones() {
        vista.getModeloInstrucciones().setRowCount(0);
        for (Instruccion instr : programaActual) {
            vista.getModeloInstrucciones().addRow(new Object[]{instr.getTextoOriginal(), instr.getBinario()});
        }
    }

    private void actualizarTablaMemoria() {
        vista.getModeloMemoria().setRowCount(0);
        String[] datos = memoria.getDatos();
        for (int i = 0; i < datos.length; i++) {
            if (datos[i] != null) {
                vista.getModeloMemoria().addRow(new Object[]{i, datos[i]});
            }
        }
    }

    /**
     * Sincroniza el BCP con el CPU y refresca las etiquetas de la vista.
     */
    private void actualizarPanelBCP() {
        bcp.actualizarDesdeCPU(cpu);

        vista.getLblEstado().setText(bcp.getEstado().toString());
        vista.getLblPC().setText(String.valueOf(bcp.getPc()));
        vista.getLblIR().setText(bcp.getIr() != null ? bcp.getIr().getTextoOriginal() : "-");
        vista.getLblAC().setText(String.valueOf(bcp.getAc()));
        vista.getLblAX().setText(String.valueOf(bcp.getAx()));
        vista.getLblBX().setText(String.valueOf(bcp.getBx()));
        vista.getLblCX().setText(String.valueOf(bcp.getCx()));
        vista.getLblDX().setText(String.valueOf(bcp.getDx()));
    }
    
    private void configurarMemoria() {
        if (programaActual != null) {
            JOptionPane.showMessageDialog(vista,
                    "No se puede cambiar la memoria con un programa cargado. Usa \"Limpiar\" primero.",
                    "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String texto = vista.getTxtTamanoMemoria().getText().trim();
        int tamano;
        try {
            tamano = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vista, "Ingresa un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tamano < Memoria.TAMANO_MINIMO) {
            JOptionPane.showMessageDialog(vista,
                    "El tamaño mínimo de memoria es " + Memoria.TAMANO_MINIMO + ".",
                    "Valor inválido", JOptionPane.ERROR_MESSAGE);
            vista.getTxtTamanoMemoria().setText(String.valueOf(Memoria.TAMANO_MINIMO));
            return;
        }

        memoria = new Memoria(tamano);
        bcp = new BCP(1, memoria.getInicioUsuario(), memoria.getTamanoTotal() - 1);
        programaActual = null;
        cpu.cargarPrograma(null, posicionInicioPrograma);

        vista.getModeloInstrucciones().setRowCount(0);
        vista.getModeloMemoria().setRowCount(0);
        actualizarPanelBCP();

        JOptionPane.showMessageDialog(vista, "Memoria configurada a " + tamano + " posiciones.");
    }
}