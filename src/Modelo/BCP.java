package Modelo;

/*
 * Bloque de control de procesos. 
 * Guarda toda la info de un proceso y sabe convertirse a lineas de
 * texto para guardarse en la zona de kernel de la memoria, y
 * tambien reconstruirse leyendo esas lineas de vuelta. 
 *
 * Cada BCP ocupa 21 posiciones consecutivas en memoria 
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
     * Convierte este BCP en las 21 lineas de texto que se van a
     * escribir en la zona de kernel de la memoria, empezando en
     * la posicion indicada
     */
    public String[] serializar() {
        String[] lineas = new String[TAMANO_BLOQUE];

        lineas[0] = "PID:" + pid;
        lineas[1] = "ESTADO:" + estado;
        lineas[2] = "PC:" + pc;
        lineas[3] = "IR:" + ir;
        lineas[4] = "AC:" + ac;
        lineas[5] = "AX:" + ax;
        lineas[6] = "BX:" + bx;
        lineas[7] = "CX:" + cx;
        lineas[8] = "DX:" + dx;

        // cada elemento de la pila va en su propia posicion, del 9 al 13
        Integer[] valoresPila = pila.getValores();
        for (int i = 0; i < Pila.TAMANO; i++) {
            if (valoresPila[i] == null) {
                lineas[9 + i] = "PILA_" + i + ":VACIO";
            } else {
                lineas[9 + i] = "PILA_" + i + ":" + valoresPila[i];
            }
        }

        lineas[14] = "BASE:" + base;
        lineas[15] = "LIMITE:" + limite;
        lineas[16] = "PRIORIDAD:" + prioridad;
        lineas[17] = "TIEMPO_INICIO:" + tiempoInicio;
        lineas[18] = "TIEMPO_CPU:" + tiempoCpu;
        lineas[19] = "ARCHIVOS_ABIERTOS:" + archivosAbiertos;
        lineas[20] = "SIGUIENTE_BCP:" + siguienteBCP;

        return lineas;
    }

    /**
     * Reconstruye un BCP a partir de las 21 lineas leidas de memoria
     * Es nesesario para tener los valores legibles en memoria
     */
    public static BCP deserializar(String[] lineas) {
        // sacamos el valor despues de los dos puntos de cada linea
        int pid = Integer.parseInt(quitarEtiqueta(lineas[0]));
        Estado estado = Estado.valueOf(quitarEtiqueta(lineas[1]));
        int pc = Integer.parseInt(quitarEtiqueta(lineas[2]));
        String ir = quitarEtiqueta(lineas[3]);
        int ac = Integer.parseInt(quitarEtiqueta(lineas[4]));
        int ax = Integer.parseInt(quitarEtiqueta(lineas[5]));
        int bx = Integer.parseInt(quitarEtiqueta(lineas[6]));
        int cx = Integer.parseInt(quitarEtiqueta(lineas[7]));
        int dx = Integer.parseInt(quitarEtiqueta(lineas[8]));

        Integer[] valoresPila = new Integer[Pila.TAMANO];
        int cantidadPila = 0;
        for (int i = 0; i < Pila.TAMANO; i++) {
            String valorTexto = quitarEtiqueta(lineas[9 + i]);
            if (valorTexto.equals("VACIO")) {
                valoresPila[i] = null;
            } else {
                valoresPila[i] = Integer.valueOf(valorTexto);
                cantidadPila++;
            }
        }

        int base = Integer.parseInt(quitarEtiqueta(lineas[14]));
        int limite = Integer.parseInt(quitarEtiqueta(lineas[15]));
        int prioridad = Integer.parseInt(quitarEtiqueta(lineas[16]));
        String tiempoInicio = quitarEtiqueta(lineas[17]);
        int tiempoCpu = Integer.parseInt(quitarEtiqueta(lineas[18]));
        String archivosAbiertos = quitarEtiqueta(lineas[19]);
        int siguienteBCP = Integer.parseInt(quitarEtiqueta(lineas[20]));

        BCP bcp = new BCP(pid, base, limite);
        bcp.estado = estado;
        bcp.pc = pc;
        bcp.ir = ir;
        bcp.ac = ac;
        bcp.ax = ax;
        bcp.bx = bx;
        bcp.cx = cx;
        bcp.dx = dx;
        bcp.pila.cargarValores(valoresPila, cantidadPila);
        bcp.prioridad = prioridad;
        bcp.tiempoInicio = tiempoInicio;
        bcp.tiempoCpu = tiempoCpu;
        bcp.archivosAbiertos = archivosAbiertos;
        bcp.siguienteBCP = siguienteBCP;

        return bcp;
    }

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