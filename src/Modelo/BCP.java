package Modelo;

/**
 * Bloque de Control de Procesos
 * Guarda toda la informacion necesaria para identificar y describir
 * el estado de un proceso  
 *
 * @author Diego Araya
 */
public class BCP {

    public enum Estado {
        NUEVO, LISTO, EJECUTANDO, TERMINADO
    }

    private final int pid;
    private Estado estado;

    // Copia del estado del CPU en el momento consultado
    private int pc;
    private Instruccion ir;
    private int ac, ax, bx, cx, dx;

    // Espacio de memoria asignado al proceso
    private int baseMemoria;
    private int limiteMemoria;

    public BCP(int pid, int baseMemoria, int limiteMemoria) {
        this.pid = pid;
        this.estado = Estado.NUEVO;
        this.baseMemoria = baseMemoria;
        this.limiteMemoria = limiteMemoria;
    }

    /**
     * Actualiza el BCP con el estado actual del CPU.
     * Se llama después de cada paso de ejecucion para mantener
     * el BCP sincronizado 
     * @param cpu
     */
    public void actualizarDesdeCPU(CPU cpu) {
        this.pc = cpu.getPc();
        this.ir = cpu.getIr();
        this.ac = cpu.getAc();
        this.ax = cpu.getAx();
        this.bx = cpu.getBx();
        this.cx = cpu.getCx();
        this.dx = cpu.getDx();
        this.estado = cpu.isTerminado() ? Estado.TERMINADO : Estado.EJECUTANDO;
    }

    public int getPid() { return pid; }
    public Estado getEstado() { return estado; }
    public int getPc() { return pc; }
    public Instruccion getIr() { return ir; }
    public int getAc() { return ac; }
    public int getAx() { return ax; }
    public int getBx() { return bx; }
    public int getCx() { return cx; }
    public int getDx() { return dx; }
    public int getBaseMemoria() { return baseMemoria; }
    public int getLimiteMemoria() { return limiteMemoria; }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}