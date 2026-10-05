package Modelo;

/**
 * Bloque de Control de Procesos.
 * Guarda toda la info de un proceso y sabe convertirse a lineas de
 * texto para guardarse en la zona de kernel de la memoria, y
 * tambien reconstruirse leyendo esas lineas de vuelta
 *
 * Cada BCP ocupa 21 posiciones consecutivas en memoria (con cada elemento de la pila en su propia posicion).
 *
 * @author Diego Araya
 */
public class BCP {

    public static final int TAMANO_BLOQUE = 21;

    public enum Estado {
        NUEVO, LISTO, EJECUTANDO, BLOQUEADO, LISTO_SUSPENDIDO, BLOQUEADO_SUSPENDIDO, TERMINADO
    }

    private int pid;
    private Estado estado;

    private int pc;
    private String ir; // la instruccion actual como texto, ej "MOV AX, 5"

    private int ac, ax, bx, cx, dx;

    private Pila pila;

    private int base;
    private int limite;
    private int prioridad;

    private String tiempoInicio; // guardado como texto tipo "14:32:05"
    private int tiempoCpu; // segundos acumulados de cpu usados

    private String archivosAbiertos; // por ahora un solo texto, separado por ; si hay varios

    private int siguienteBCP; // direccion en memoria del siguiente BCP, -1 si no hay

    public BCP(int pid, int base, int limite) {
        this.pid = pid;
        this.estado = Estado.NUEVO;
        this.base = base;
        this.limite = limite;
        this.pc = base;
        this.ir = "-";
        this.ac = 0;
        this.ax = 0;
        this.bx = 0;
        this.cx = 0;
        this.dx = 0;
        this.pila = new Pila();
        this.prioridad = 1;
        this.tiempoInicio = "-";
        this.tiempoCpu = 0;
        this.archivosAbiertos = "NINGUNO";
        this.siguienteBCP = -1;
    }

    /**
    public String[] serializar() {
        
        return "";
    }**/

   /**
    public static BCP deserializar(String[] lineas) {
        return;
    }**/

    // una linea viene como "ETIQUETA:valor", esto se queda solo con el valor
    private static String quitarEtiqueta(String linea) {
        int posDosPuntos = linea.indexOf(":");
        return linea.substring(posDosPuntos + 1);
    }

    // getters y setters 

    public int getPid() {
        return pid;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public String getIr() {
        return ir;
    }

    public void setIr(String ir) {
        this.ir = ir;
    }

    public int getAc() {
        return ac;
    }

    public void setAc(int ac) {
        this.ac = ac;
    }

    public int getAx() {
        return ax;
    }

    public void setAx(int ax) {
        this.ax = ax;
    }

    public int getBx() {
        return bx;
    }

    public void setBx(int bx) {
        this.bx = bx;
    }

    public int getCx() {
        return cx;
    }

    public void setCx(int cx) {
        this.cx = cx;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public Pila getPila() {
        return pila;
    }

    public int getBase() {
        return base;
    }

    public int getLimite() {
        return limite;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public String getTiempoInicio() {
        return tiempoInicio;
    }

    public void setTiempoInicio(String tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }

    public int getTiempoCpu() {
        return tiempoCpu;
    }

    public void sumarTiempoCpu(int segundos) {
        this.tiempoCpu = this.tiempoCpu + segundos;
    }

    public String getArchivosAbiertos() {
        return archivosAbiertos;
    }

    public void setArchivosAbiertos(String archivosAbiertos) {
        this.archivosAbiertos = archivosAbiertos;
    }

    public int getSiguienteBCP() {
        return siguienteBCP;
    }

    public void setSiguienteBCP(int siguienteBCP) {
        this.siguienteBCP = siguienteBCP;
    }
}