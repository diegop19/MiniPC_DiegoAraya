package Modelo;

import java.util.List;

/**
 * Representa el CPU del Mini PC: registros (AC, AX, BX, CX, DX),
 * PC e IR , junto con la logica de fetch-decode-execute de cada instruccion.
 * @author Diego Araya
 */
public class CPU {

    private int ac, ax, bx, cx, dx;
    private int pc;              // posicion de memoria de la instrucción actual
    private Instruccion ir;      // instruccion actualmente cargada

    private List<Instruccion> programa; // instrucciones cargadas en memoria de usuario
    private int posicionInicio;         // posicion de memoria donde inicia el programa
    private boolean terminado;

    /**
     * Carga un nuevo programa en el CPU y reinicia todos los registros
     *
     * @param programa       lista de instrucciones ya parseadas por el Ensamblador
     * @param posicionInicio posicion de memoria donde inicia el programa
     */
    public void cargarPrograma(List<Instruccion> programa, int posicionInicio) {
        this.programa = programa;
        this.posicionInicio = posicionInicio;
        this.pc = posicionInicio;
        this.ir = null;
        this.ac = this.ax = this.bx = this.cx = this.dx = 0;
        this.terminado = false;
    }

    /**
     * Ejecuta un solo ciclo fetch-decode-execute 
     *
     * @return true si se ejecutó una instrucción, false si el programa ya terminó
     */
    public boolean ejecutarPaso() {
        if (terminado || programa == null) {
            return false;
        }

        int indice = pc - posicionInicio;
        if (indice < 0 || indice >= programa.size()) {
            terminado = true;
            return false;
        }

        ir = fetch(indice);
        decodeYExecute(ir);
        pc++;

        if (pc - posicionInicio >= programa.size()) {
            terminado = true;
        }
        return true;
    }

    /**
     * Ejecuta todas las instrucciones restantes de una sola vez

     */
    public void ejecutarTodo() {
        while (ejecutarPaso()) {
        }
    }

    /**
     * FETCH: obtiene la instruccion correspondiente al índice actual
     */
    private Instruccion fetch(int indice) {
        return programa.get(indice);
    }

    /**
     * DECODE + EXECUTE: interpreta el opcode y aplica el efecto
     * correspondiente sobre los registros
     */
    private void decodeYExecute(Instruccion instr) {
        String opcode = instr.getOpcode();
        String registro = instr.getRegistro();

        switch (opcode) {
            case "MOV" -> asignarRegistro(registro, instr.getValor());
            case "LOAD" -> ac = obtenerRegistro(registro);
            case "STORE" -> asignarRegistro(registro, ac);
            case "ADD" -> ac = ac + obtenerRegistro(registro);
            case "SUB" -> ac = ac - obtenerRegistro(registro);
            default -> throw new IllegalStateException("Opcode no soportado: " + opcode);
        }
    }

    private int obtenerRegistro(String nombre) {
        switch (nombre) {
            case "AX" -> {
                return ax;
            }
            case "BX" -> {
                return bx;
            }
            case "CX" -> {
                return cx;
            }
            case "DX" -> {
                return dx;
            }
            default -> throw new IllegalArgumentException("Registro desconocido: " + nombre);
        }
    }

    private void asignarRegistro(String nombre, int valor) {
        switch (nombre) {
            case "AX" -> ax = valor;
            case "BX" -> bx = valor;
            case "CX" -> cx = valor;
            case "DX" -> dx = valor;
            default -> throw new IllegalArgumentException("Registro desconocido: " + nombre);
        }
    }

    // getters para que la GUI muestre el estado actual 
    public int getAc() { return ac; }
    public int getAx() { return ax; }
    public int getBx() { return bx; }
    public int getCx() { return cx; }
    public int getDx() { return dx; }
    public int getPc() { return pc; }
    public Instruccion getIr() { return ir; }
    public boolean isTerminado() { return terminado; }
}