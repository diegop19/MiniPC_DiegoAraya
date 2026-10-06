package Controlador;

import Modelo.BCP;
import Modelo.CPU;
import Modelo.ColaTrabajos;
import Modelo.Despachador;
import Modelo.Disco;
import Modelo.Ensamblador;
import Modelo.FormatoInvalidoException;
import Modelo.Instruccion;
import Modelo.Memoria;
import Modelo.Planificador;
import Modelo.TablaProcesos;
import Modelo.Trabajo;
import Vista.MainFrame;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Conecta la ventana con el modelo. Aqui viven los listeners de los
 * botones y el avance del reloj simulado: cada vez que se presiona
 * Siguiente pasa un segundo de cpu.
 *
 * @author Diego Araya
 */
public class MiniPC {

    private static final int LIMITE_TICKS_EJECUTAR = 10000; // por si un programa nunca termina

    private final MainFrame vista;
    private final Ensamblador ensamblador;
    private final Despachador despachador;

    private CPU cpu;
    private Memoria memoria;
    private Disco disco;
    private TablaProcesos tablaProcesos;
    private ColaTrabajos colaTrabajos;
    private Planificador planificador;

    private int tamanoMemoria;
    private int tamanoDisco;

    // el proceso que esta montado en el cpu en este momento, null si el cpu esta libre
    private BCP procesoActual;
    private int posicionKernelActual;

    private int contadorPid;
    private int archivosCargados;
    private Map<Integer, String> nombresArchivoPorPid;
    private LinkedList<Integer> colaEsperaTeclado; // pids esperando un valor, en orden
    private List<String> procesosTerminados;
    private List<String> historialEstadisticas;

    public MiniPC(MainFrame vista, int tamanoMemoria, int tamanoDisco) {
        this.vista = vista;
        this.ensamblador = new Ensamblador();
        this.despachador = new Despachador();
        this.tamanoMemoria = tamanoMemoria;
        this.tamanoDisco = tamanoDisco;

        crearModelo();
        registrarListeners();
        refrescarTodo();
    }

    /**
     * Crea todos los objetos del modelo desde cero. Se usa al
     * iniciar, al limpiar y al cambiar la configuracion.
     */
    private void crearModelo() {
        memoria = new Memoria(tamanoMemoria);
        disco = new Disco(tamanoDisco);
        tablaProcesos = new TablaProcesos(memoria);
        colaTrabajos = new ColaTrabajos();
        planificador = new Planificador(colaTrabajos, tablaProcesos, memoria, disco);

        cpu = new CPU();
        cpu.setSalida(texto -> imprimirDeProcesoActual(texto));

        procesoActual = null;
        posicionKernelActual = -1;
        contadorPid = 1;
        archivosCargados = 0;
        nombresArchivoPorPid = new HashMap<>();
        colaEsperaTeclado = new LinkedList<>();
        procesosTerminados = new ArrayList<>();
        historialEstadisticas = new ArrayList<>();
    }

    private void registrarListeners() {
        vista.getBtnCargar().addActionListener(e -> cargarArchivos());
        vista.getBtnSiguiente().addActionListener(e -> siguiente());
        vista.getBtnEjecutar().addActionListener(e -> ejecutarTodo());
        vista.getBtnLimpiar().addActionListener(e -> limpiar());
        vista.getBtnEstadisticas().addActionListener(e -> mostrarEstadisticas());
        vista.getBtnConfigurar().addActionListener(e -> configurar());
        vista.getBtnEnviarTeclado().addActionListener(e -> resolverTeclado());
        vista.getTxtTeclado().addActionListener(e -> resolverTeclado()); // tecla enter
    }

    // =========================================================
    // cargar archivos
    // =========================================================

    private void cargarArchivos() {
        JFileChooser selector = new JFileChooser();
        selector.setMultiSelectionEnabled(true);
        selector.setFileFilter(new FileNameExtensionFilter("Archivos ASM (*.asm)", "asm"));

        if (selector.showOpenDialog(vista) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] archivos = selector.getSelectedFiles();

        // primero se validan todos, si alguno falla no se carga ninguno
        List<Trabajo> nuevos = new ArrayList<>();
        int pidTemporal = contadorPid;
        int espacioUsuario = memoria.getTamanoTotal() - memoria.getInicioUsuario();

        for (File archivo : archivos) {
            if (!archivo.getName().toLowerCase().endsWith(".asm")) {
                mostrarError("El archivo " + archivo.getName() + " no tiene extensión .asm", "Formato inválido");
                return;
            }

            List<Instruccion> programa;
            try {
                programa = ensamblador.parsearArchivo(archivo);
            } catch (IOException e) {
                mostrarError("No se pudo leer " + archivo.getName() + ": " + e.getMessage(), "Error de lectura");
                return;
            } catch (FormatoInvalidoException e) {
                mostrarError(archivo.getName() + ": " + e.getMessage(), "Formato inválido");
                return;
            }

            if (programa.isEmpty()) {
                mostrarError("El archivo " + archivo.getName() + " está vacío", "Formato inválido");
                return;
            }
            if (programa.size() > espacioUsuario) {
                mostrarError("El programa " + archivo.getName() + " (" + programa.size()
                        + " instrucciones) no cabe en la memoria de usuario (" + espacioUsuario + " posiciones)",
                        "Programa muy grande");
                return;
            }

            nuevos.add(new Trabajo(pidTemporal, archivo.getName(), programa));
            pidTemporal++;
        }

        // si llego hasta aca, todos los archivos son validos
        for (Trabajo trabajo : nuevos) {
            nombresArchivoPorPid.put(trabajo.getPid(), trabajo.getNombreArchivo());
            planificador.agregarTrabajo(trabajo);
            vista.imprimirEnPantalla("[Sistema] Trabajo cargado: PID " + trabajo.getPid()
                    + " (" + trabajo.getNombreArchivo() + ")");
        }
        contadorPid = pidTemporal;
        archivosCargados = archivosCargados + nuevos.size();
        vista.setArchivosCargados(archivosCargados);

        admitirTrabajosPendientes();
        refrescarTodo();
    }

    /**
     * Mete a memoria principal todos los trabajos de la cola que
     * quepan. Los que no quepan se quedan esperando.
     */
    private void admitirTrabajosPendientes() {
        BCP admitido = planificador.intentarIngresarSiguiente();
        while (admitido != null) {
            vista.imprimirEnPantalla("[Planificador] PID " + admitido.getPid() + " entra a memoria principal");
            admitido = planificador.intentarIngresarSiguiente();
        }
    }

    // =========================================================
    // ejecucion
    // =========================================================

    private void siguiente() {
        boolean huboAvance = avanzarUnSegundo();
        if (!huboAvance) {
            mostrarMensajeSinAvance();
        }
        refrescarTodo();
    }

    private void ejecutarTodo() {
        int ticks = 0;
        while (ticks < LIMITE_TICKS_EJECUTAR && avanzarUnSegundo()) {
            ticks++;
        }

        if (ticks == 0) {
            mostrarMensajeSinAvance();
        } else if (ticks >= LIMITE_TICKS_EJECUTAR) {
            vista.imprimirEnPantalla("[Sistema] Se detuvo la ejecución automática, se alcanzó el límite de "
                    + LIMITE_TICKS_EJECUTAR + " segundos");
        } else if (!colaEsperaTeclado.isEmpty()) {
            vista.imprimirEnPantalla("[Sistema] La ejecución se detuvo, hay procesos esperando entrada de teclado");
        } else {
            vista.imprimirEnPantalla("[Sistema] Todos los procesos terminaron");
        }
        refrescarTodo();
    }

    /**
     * Simula un segundo de cpu. Si el cpu esta libre, primero
     * despacha al siguiente proceso listo. Devuelve false si no habia
     * nada que ejecutar.
     */
    private boolean avanzarUnSegundo() {
        if (procesoActual == null) {
            if (!despacharSiguiente()) {
                return false;
            }
        }

        CPU.ResultadoCiclo resultado = cpu.ejecutarCiclo();
        procesoActual.sumarTiempoCpu(1);

        switch (resultado) {
            case BLOQUEADO:
                bloquearProcesoActual();
                break;
            case TERMINADO:
                terminarProcesoActual();
                break;
            default:
                guardarProcesoActual();
                break;
        }
        return true;
    }

    /**
     * Elige al siguiente proceso listo (FCFS) y lo monta en el cpu.
     * Devuelve false si no hay ninguno listo.
     */
    private boolean despacharSiguiente() {
        BCP candidato = planificador.elegirSiguienteListo();

        if (candidato == null) {
            // a lo mejor hay alguno en disco que ya pueda volver
            if (planificador.intentarSwapIn() != null) {
                candidato = planificador.elegirSiguienteListo();
            }
        }
        if (candidato == null) {
            return false;
        }

        posicionKernelActual = tablaProcesos.buscarPosicionPorPid(candidato.getPid());
        List<Instruccion> programa = planificador.getProgramaDe(candidato.getPid());

        despachador.despachar(cpu, candidato, programa);
        if (candidato.getTiempoInicio().equals("-")) {
            candidato.setTiempoInicio(horaActual());
        }

        procesoActual = candidato;
        guardarProcesoActual();
        vista.imprimirEnPantalla("[Despachador] PID " + candidato.getPid() + " entra al CPU");
        return true;
    }

    /**
     * El proceso actual llego a un INT 09H. Se saca del cpu y queda
     * esperando el valor del teclado, el cpu queda libre para otro.
     */
    private void bloquearProcesoActual() {
        despachador.sacarProceso(cpu, procesoActual, BCP.Estado.BLOQUEADO);
        guardarProcesoActual();

        int pid = procesoActual.getPid();
        colaEsperaTeclado.add(pid);
        vista.imprimirEnPantalla("[CPU] PID " + pid + " se bloquea esperando teclado");
        if (colaEsperaTeclado.size() == 1) {
            vista.imprimirEnPantalla(">> Ingresar valor (0 a 255) para PID " + pid + ":");
        }

        procesoActual = null;
    }

    /**
     * El proceso actual termino. Se anota en el historial para las
     * estadisticas, se libera todo lo que tenia y se intenta que
     * entren los que estaban esperando.
     */
    private void terminarProcesoActual() {
        despachador.sacarProceso(cpu, procesoActual, BCP.Estado.TERMINADO);

        int pid = procesoActual.getPid();
        String nombre = nombresArchivoPorPid.get(pid);
        String horaFin = horaActual();

        historialEstadisticas.add("PID " + pid + " (" + nombre + ")   inicio: " + procesoActual.getTiempoInicio()
                + "   fin: " + horaFin + "   duración: " + procesoActual.getTiempoCpu() + " s");
        procesosTerminados.add("PID " + pid + " (" + nombre + ")");
        vista.imprimirEnPantalla("[CPU] PID " + pid + " terminó");

        planificador.finalizarProceso(procesoActual);
        procesoActual = null;
        posicionKernelActual = -1;

        // primero los que ya estaban en disco, porque llegaron antes
        BCP regreso = planificador.intentarSwapIn();
        while (regreso != null) {
            vista.imprimirEnPantalla("[Planificador] PID " + regreso.getPid() + " regresa de disco a memoria");
            regreso = planificador.intentarSwapIn();
        }
        admitirTrabajosPendientes();
    }

    /**
     * Guarda el BCP del proceso actual en la memoria del kernel.
     * Antes se refresca el enlace al siguiente BCP, porque otro
     * proceso pudo haberse agregado a la lista mientras este corria
     * y la copia que tenemos aqui ya estaria desactualizada.
     */
    private void guardarProcesoActual() {
        BCP enMemoria = tablaProcesos.leer(posicionKernelActual);
        procesoActual.setSiguienteBCP(enMemoria.getSiguienteBCP());
        tablaProcesos.actualizar(posicionKernelActual, procesoActual);
    }

    private void imprimirDeProcesoActual(String texto) {
        if (procesoActual != null) {
            vista.imprimirEnPantalla("[PID " + procesoActual.getPid() + "] DX = " + texto);
        } else {
            vista.imprimirEnPantalla("DX = " + texto);
        }
    }

    private void mostrarMensajeSinAvance() {
        if (!colaEsperaTeclado.isEmpty()) {
            vista.imprimirEnPantalla("[Sistema] No hay procesos listos, hay procesos esperando entrada de teclado");
        } else if (tablaProcesos.estaVacia() && colaTrabajos.estaVacia()) {
            vista.imprimirEnPantalla("[Sistema] No hay procesos para ejecutar, carga archivos primero");
        } else {
            vista.imprimirEnPantalla("[Sistema] No hay procesos listos para ejecutar");
        }
    }

    // =========================================================
    // teclado (INT 09H)
    // =========================================================

    /**
     * El usuario escribio un valor para el proceso que lleva mas
     * tiempo esperando. Como ese proceso ya no esta en el cpu, se
     * modifica directo su BCP: se guarda el valor en DX y se avanza
     * el pc para que no vuelva a ejecutar el mismo INT 09H.
     */
    private void resolverTeclado() {
        if (colaEsperaTeclado.isEmpty()) {
            vista.imprimirEnPantalla("[Sistema] Ningún proceso está esperando entrada");
            vista.getTxtTeclado().setText("");
            return;
        }

        int valor;
        try {
            valor = Integer.parseInt(vista.getTxtTeclado().getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Solo se permiten valores numéricos entre 0 y 255", "Entrada inválida");
            return;
        }
        if (valor < 0 || valor > 255) {
            mostrarError("El valor debe estar entre 0 y 255", "Entrada inválida");
            return;
        }

        int pid = colaEsperaTeclado.poll();
        int posicion = tablaProcesos.buscarPosicionPorPid(pid);
        BCP bcp = tablaProcesos.leer(posicion);

        bcp.setDx(valor);
        bcp.setPc(bcp.getPc() + 1);
        if (bcp.getEstado() == BCP.Estado.BLOQUEADO_SUSPENDIDO) {
            bcp.setEstado(BCP.Estado.LISTO_SUSPENDIDO);
        } else {
            bcp.setEstado(BCP.Estado.LISTO);
        }
        tablaProcesos.actualizar(posicion, bcp);

        vista.getTxtTeclado().setText("");
        vista.imprimirEnPantalla("<< PID " + pid + " recibió el valor " + valor);

        if (!colaEsperaTeclado.isEmpty()) {
            vista.imprimirEnPantalla(">> Ingresar valor (0 a 255) para PID " + colaEsperaTeclado.peek() + ":");
        }
        refrescarTodo();
    }

    // =========================================================
    // limpiar, configurar y estadisticas
    // =========================================================

    private void limpiar() {
        crearModelo();
        vista.getAreaPantalla().setText("");
        vista.getTxtTeclado().setText("");
        vista.setArchivosCargados(0);
        refrescarTodo();
    }

    /**
     * Cambia el tamano de memoria y disco. Solo se permite si no hay
     * ningun proceso ni trabajo en el sistema, porque cambiar los
     * tamanos en media ejecucion dejaria todo inconsistente.
     */
    private void configurar() {
        if (!tablaProcesos.estaVacia() || !colaTrabajos.estaVacia()) {
            JOptionPane.showMessageDialog(vista,
                    "No se puede cambiar la configuración con procesos cargados. Usa \"Limpiar\" primero.",
                    "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int nuevaMemoria;
        int nuevoDisco;
        try {
            nuevaMemoria = Integer.parseInt(vista.getTxtTamanoMemoria().getText().trim());
            nuevoDisco = Integer.parseInt(vista.getTxtTamanoDisco().getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Ingresa números válidos en memoria y disco", "Error");
            return;
        }

        if (nuevaMemoria < Memoria.TAMANO_MINIMO) {
            mostrarError("El tamaño mínimo de memoria es " + Memoria.TAMANO_MINIMO, "Valor inválido");
            vista.getTxtTamanoMemoria().setText(String.valueOf(Memoria.TAMANO_MINIMO));
            return;
        }
        if (nuevoDisco < Disco.TAMANO_MINIMO) {
            mostrarError("El tamaño mínimo del disco es " + Disco.TAMANO_MINIMO, "Valor inválido");
            vista.getTxtTamanoDisco().setText(String.valueOf(Disco.TAMANO_MINIMO));
            return;
        }

        tamanoMemoria = nuevaMemoria;
        tamanoDisco = nuevoDisco;
        crearModelo();
        vista.setArchivosCargados(0);
        refrescarTodo();
        JOptionPane.showMessageDialog(vista, "Configuración aplicada: memoria " + nuevaMemoria
                + ", disco " + nuevoDisco);
    }

    private void mostrarEstadisticas() {
        if (historialEstadisticas.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Todavía no ha terminado ningún proceso.",
                    "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder texto = new StringBuilder();
        for (String linea : historialEstadisticas) {
            texto.append(linea).append("\n");
        }
        JOptionPane.showMessageDialog(vista, texto.toString(), "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================
    // refrescar la interfaz
    // =========================================================

    private void refrescarTodo() {
        actualizarTablaProcesos();
        actualizarTablaMemoria();
        actualizarTablaDisco();
        actualizarPanelCPU();
    }

    private void actualizarTablaProcesos() {
        vista.getModeloProcesos().setRowCount(0);

        for (String terminado : procesosTerminados) {
            vista.getModeloProcesos().addRow(new Object[]{terminado, "TERMINADO"});
        }
        for (BCP bcp : tablaProcesos.listarTodos()) {
            String nombre = nombresArchivoPorPid.get(bcp.getPid());
            vista.getModeloProcesos().addRow(new Object[]{
                "PID " + bcp.getPid() + " (" + nombre + ")", bcp.getEstado().toString()});
        }
        for (Trabajo trabajo : colaTrabajos.listar()) {
            vista.getModeloProcesos().addRow(new Object[]{
                "PID " + trabajo.getPid() + " (" + trabajo.getNombreArchivo() + ")", "EN COLA"});
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

    private void actualizarTablaDisco() {
        vista.getModeloDisco().setRowCount(0);
        String[] datos = disco.getDatos();
        for (int i = 0; i < datos.length; i++) {
            if (datos[i] != null) {
                vista.getModeloDisco().addRow(new Object[]{i, datos[i]});
            }
        }
    }

    /**
     * El panel muestra lo que hay en el cpu, que son los registros
     * reales. Si el cpu esta libre se siguen mostrando los ultimos
     * valores, igual que un cpu de verdad que conserva lo que tenia
     * hasta que entra otro proceso
     */
    private void actualizarPanelCPU() {
        if (procesoActual != null) {
            vista.getLblPid().setText(String.valueOf(procesoActual.getPid()));
            vista.getLblEstado().setText("EJECUTANDO");
        } else {
            vista.getLblPid().setText("-");
            vista.getLblEstado().setText("CPU LIBRE");
        }

        vista.getLblPC().setText(String.valueOf(cpu.getPc()));
        if (cpu.getIr() != null) {
            vista.getLblIR().setText(cpu.getIr().getTextoOriginal());
        } else {
            vista.getLblIR().setText("-");
        }
        vista.getLblAC().setText(String.valueOf(cpu.getAc()));
        vista.getLblAX().setText(String.valueOf(cpu.getAx()));
        vista.getLblBX().setText(String.valueOf(cpu.getBx()));
        vista.getLblCX().setText(String.valueOf(cpu.getCx()));
        vista.getLblDX().setText(String.valueOf(cpu.getDx()));
    }

    // =========================================================
    // utilidades
    // =========================================================

    private String horaActual() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private void mostrarError(String mensaje, String titulo) {
        JOptionPane.showMessageDialog(vista, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}