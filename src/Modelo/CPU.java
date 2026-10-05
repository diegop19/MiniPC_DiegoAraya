package Modelo;

import java.util.List;

/**
 * CPU del Mini PC
 * Tiene sus propios registros, separados del BCP
 *
 * Cada vez que se llama a ejecutarCiclo() se simula un segundo de
 * CPU 
 * Las instrucciones que tengan un peso mayor a 1 requieren varios ciclos
 * @author Diego Araya
 */
public class CPU {

    public enum ResultadoCiclo {
        CONTINUA, BLOQUEADO, TERMINADO
    }

    private int ac, ax, bx, cx, dx;
    private int pc;

    private List<Instruccion> programa;
    private int baseDireccion;
    private int indiceActual;

    private Instruccion ir;
    private int ciclosRestantes;

    private Pila pilaActual;

    private boolean banderaIgual; // usada por CMP y consultada por JE y JNE

    /**
     * El despachador llama a este metodo para montar un proceso en
     * el cpu, antes de empezar a llamar ejecutarCiclo().
     */
    public void cargarProceso(List<Instruccion> programa, int baseDireccion, int pcGuardado, Pila pila) {
        this.programa = programa;
        this.baseDireccion = baseDireccion;
        this.pc = pcGuardado;
        this.indiceActual = pcGuardado - baseDireccion;
        this.pilaActual = pila;
        this.ir = null;
        this.ciclosRestantes = 0;
        this.banderaIgual = false;
    }

    /**
     * Ejecuta un ciclo y devuelve el resultado
     * para que el controlador sepa que paso: si se bloqueo por un
     * INT 09H, si el programa ya termino, o si sigue normal
     */
    public ResultadoCiclo ejecutarCiclo() {
        if (programa == null) {
            return ResultadoCiclo.TERMINADO;
        }

        // si no hay instruccion cargada todavia, se hace el fetch
        if (ir == null) {
            ir = obtenerInstruccionActual();
            if (ir == null) {
                return ResultadoCiclo.TERMINADO;
            }
            ciclosRestantes = ir.getPeso();

            if (ciclosRestantes == Ensamblador.PESO_BLOQUEO) {
                // INT 09H, se bloquea de una vez, no cuenta como ciclo normal
                return ResultadoCiclo.BLOQUEADO;
            }
        }

        ciclosRestantes--;

        if (ciclosRestantes > 0) {
            return ResultadoCiclo.CONTINUA;
        }

        // se completaron los ciclos, ahora si se ejecuta 
        decodeYExecute(ir);

        // la siguiente instruccion a cargar
        ir = obtenerInstruccionActual();
        if (ir == null) {
            return ResultadoCiclo.TERMINADO;
        }
        ciclosRestantes = ir.getPeso();
        return ResultadoCiclo.CONTINUA;
    }

    /**
     * Se usa cuando el proceso estaba bloqueado por INT 09H y el
     * usuario ya entrego un valor desde el teclado
     * Completa la instruccion pendiente y avanza al siguiente
     */
    public void resolverEntradaTeclado(int valorIngresado) {
        this.dx = valorIngresado;
        avanzarIndice();
        ir = obtenerInstruccionActual();
        if (ir != null) {
            ciclosRestantes = ir.getPeso();
        }
    }

    private Instruccion obtenerInstruccionActual() {
        if (indiceActual < 0 || indiceActual >= programa.size()) {
            return null;
        }
        return programa.get(indiceActual);
    }

    private void avanzarIndice() {
        indiceActual++;
        pc = baseDireccion + indiceActual;
    }

    /**
     * Interpreta el opcode de la instruccion actual y aplica su
     * efecto
     * Para los saltos, el indice se mueve directo adentro
     * del case correspondiente, por eso al final se revisa si ya
     * se movio para no sumarle uno de mas
     */
    private void decodeYExecute(Instruccion instr) {
        String opcode = instr.getOpcode();
        int indiceAntesDeEjecutar = indiceActual;

        switch (opcode) {
            case "LOAD":
                ac = obtenerRegistro(instr.getRegistroDestino());
                break;

            case "STORE":
                asignarRegistro(instr.getRegistroDestino(), ac);
                break;

            case "MOV":
                if (instr.getRegistroOrigen() != null) {
                    asignarRegistro(instr.getRegistroDestino(), obtenerRegistro(instr.getRegistroOrigen()));
                } else {
                    asignarRegistro(instr.getRegistroDestino(), instr.getValor());
                }
                break;

            case "ADD":
                ac = ac + obtenerRegistro(instr.getRegistroDestino());
                break;

            case "SUB":
                ac = ac - obtenerRegistro(instr.getRegistroDestino());
                break;

            case "INC":
                if (instr.getRegistroDestino() == null) {
                    ac = ac + 1;
                } else {
                    asignarRegistro(instr.getRegistroDestino(), obtenerRegistro(instr.getRegistroDestino()) + 1);
                }
                break;

            case "DEC":
                if (instr.getRegistroDestino() == null) {
                    ac = ac - 1;
                } else {
                    asignarRegistro(instr.getRegistroDestino(), obtenerRegistro(instr.getRegistroDestino()) - 1);
                }
                break;

            case "SWAP": {
                int temporal = obtenerRegistro(instr.getRegistroDestino());
                asignarRegistro(instr.getRegistroDestino(), obtenerRegistro(instr.getRegistroOrigen()));
                asignarRegistro(instr.getRegistroOrigen(), temporal);
                break;
            }

            case "CMP":
                banderaIgual = obtenerRegistro(instr.getRegistroDestino()) == obtenerRegistro(instr.getRegistroOrigen());
                break;

            case "JMP":
                indiceActual = indiceActual + instr.getDesplazamiento();
                break;

            case "JE":
                if (banderaIgual) {
                    indiceActual = indiceActual + instr.getDesplazamiento();
                }
                break;

            case "JNE":
                if (!banderaIgual) {
                    indiceActual = indiceActual + instr.getDesplazamiento();
                }
                break;

            case "PUSH":
                pilaActual.push(obtenerRegistro(instr.getRegistroDestino()));
                break;

            case "POP":
                asignarRegistro(instr.getRegistroDestino(), pilaActual.pop());
                break;

            case "PARAM":
                for (Integer valor : instr.getParametros()) {
                    pilaActual.push(valor);
                }
                break;

            case "INT":
                ejecutarInterrupcion(instr.getValor());
                break;

            default:
                throw new IllegalStateException("Opcode no soportado: " + opcode);
        }

        // si el case de arriba no toco el indice (no fue un salto),
        // entonces avanzamos normal a la siguiente instruccion
        if (indiceActual == indiceAntesDeEjecutar) {
            avanzarIndice();
        } else {
            pc = baseDireccion + indiceActual;
        }
    }

    /**
     * Maneja las interrupciones 
     */
    private void ejecutarInterrupcion(int codigo) {
        switch (codigo) {
            case 32: // 20H finaliza el programa
                indiceActual = programa.size(); // asi obtenerInstruccionActual() devuelve null
                break;
            case 16: // 10H imprime el valor de dx en pantalla
                break;
            case 9: // 09H entrada de teclado se maneja aparte como bloqueo
                break;
            case 33: 
                break;
            default:
                throw new IllegalStateException("Codigo de interrupcion no soportado: " + codigo);
        }
    }

    private int obtenerRegistro(String nombre) {
        switch (nombre) {
            case "AX": return ax;
            case "BX": return bx;
            case "CX": return cx;
            case "DX": return dx;
            default: throw new IllegalArgumentException("Registro desconocido: " + nombre);
        }
    }

    private void asignarRegistro(String nombre, int valor) {
        switch (nombre) {
            case "AX": ax = valor; break;
            case "BX": bx = valor; break;
            case "CX": cx = valor; break;
            case "DX": dx = valor; break;
            default: throw new IllegalArgumentException("Registro desconocido: " + nombre);
        }
    }

    // ---- getters y setters, usados por el Despachador para copiar valores 

    public int getAc() { return ac; }
    public void setAc(int ac) { this.ac = ac; }

    public int getAx() { return ax; }
    public void setAx(int ax) { this.ax = ax; }

    public int getBx() { return bx; }
    public void setBx(int bx) { this.bx = bx; }

    public int getCx() { return cx; }
    public void setCx(int cx) { this.cx = cx; }

    public int getDx() { return dx; }
    public void setDx(int dx) { this.dx = dx; }

    public int getPc() { return pc; }

    public Instruccion getIr() { return ir; }

    public int getCiclosRestantes() { return ciclosRestantes; }
}