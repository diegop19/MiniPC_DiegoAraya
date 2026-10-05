package Modelo;

/**
 * Memoria principal del Mini PC 
 * Se divide en dos zonas:
 *   - Kernel: aqui se guardan los BCP de los procesos (serializados
 *     como texto, 21 posiciones por proceso)
 *   - Usuario: aqui se cargan los programas de los procesos
 *
 * @author Diego Araya
 */
public class Memoria {

    public static final int TAMANO_MINIMO = 128;

    // porcentaje por defecto para la zona de kernel
    public static final double PORCENTAJE_KERNEL_DEFAULT = 0.40;

    private String[] datos;
    private int tamanoTotal;
    private int finKernel;      // posicion donde termina el kernel (exclusiva)
    private int inicioUsuario;  // igual a finKernel

    public Memoria(int tamanoTotal) {
        this(tamanoTotal, PORCENTAJE_KERNEL_DEFAULT);
    }

    public Memoria(int tamanoTotal, double porcentajeKernel) {
        if (tamanoTotal < TAMANO_MINIMO) {
            tamanoTotal = TAMANO_MINIMO;
        }
        this.tamanoTotal = tamanoTotal;
        this.datos = new String[tamanoTotal];
        this.finKernel = (int) (tamanoTotal * porcentajeKernel);
        this.inicioUsuario = finKernel;
    }

    // zona de kernel (BCP de los procesos)

    /**
     * Busca la primera posicion libre dentro del kernel donde
     * quepa un bloque completo de BCP (21 posiciones seguidas
     * todas en null). Si no hay espacio, devuelve -1.
     */
    public int buscarSlotKernelLibre() {
        int tamanoBloque = BCP.TAMANO_BLOQUE;

        for (int inicio = 0; inicio + tamanoBloque <= finKernel; inicio += tamanoBloque) {
            boolean libre = true;
            for (int j = 0; j < tamanoBloque; j++) {
                if (datos[inicio + j] != null) {
                    libre = false;
                    break;
                }
            }
            if (libre) {
                return inicio;
            }
        }
        return -1;
    }

    /**
     * Escribe las 21 lineas de un BCP ya serializado, empezando
     * en la posicion indicada.
     */
    public void escribirBCP(int posicionInicio, String[] lineasBCP) {
        if (posicionInicio < 0 || posicionInicio + lineasBCP.length > finKernel) {
            throw new IllegalArgumentException(
                    "El bloque de BCP no cabe en la zona de kernel a partir de " + posicionInicio);
        }
        for (int i = 0; i < lineasBCP.length; i++) {
            datos[posicionInicio + i] = lineasBCP[i];
        }
    }

    /**
     * Lee las 21 lineas de un BCP a partir de la posicion indicada.
     */
    public String[] leerBCP(int posicionInicio) {
        String[] lineas = new String[BCP.TAMANO_BLOQUE];
        for (int i = 0; i < BCP.TAMANO_BLOQUE; i++) {
            lineas[i] = datos[posicionInicio + i];
        }
        return lineas;
    }

    /**
     * Deja en null las 21 posiciones de un BCP, para liberar ese
     * espacio cuando el proceso termina o se saca de memoria.
     */
    public void liberarBCP(int posicionInicio) {
        for (int i = 0; i < BCP.TAMANO_BLOQUE; i++) {
            datos[posicionInicio + i] = null;
        }
    }

    // zona de usuario (programas de los procesos)

    public void escribirUsuario(int posicion, String valor) {
        validarPosicionUsuario(posicion);
        datos[posicion] = valor;
    }

    /**
     * Busca un espacio libre y contiguo en la zona de usuario con
     * al menos la cantidad de posiciones pedida Devuelve la
     * posicion donde empieza, o -1 si no encontro espacio suficiente
     */
    public int buscarEspacioUsuario(int cantidadPosiciones) {
        int libresSeguidas = 0;
        int inicioCandidato = -1;

        for (int i = inicioUsuario; i < tamanoTotal; i++) {
            if (datos[i] == null) {
                if (libresSeguidas == 0) {
                    inicioCandidato = i;
                }
                libresSeguidas++;
                if (libresSeguidas == cantidadPosiciones) {
                    return inicioCandidato;
                }
            } else {
                libresSeguidas = 0;
            }
        }
        return -1;
    }

    /**
     * Libera un rango de posiciones de la zona de usuario, por
     * ejemplo cuando un proceso termina o se manda a swap.
     */
    public void liberarUsuario(int posicionInicio, int cantidadPosiciones) {
        for (int i = 0; i < cantidadPosiciones; i++) {
            datos[posicionInicio + i] = null;
        }
    }

    private void validarPosicionUsuario(int posicion) {
        if (posicion < inicioUsuario || posicion >= tamanoTotal) {
            throw new IllegalArgumentException(
                    "Posicion " + posicion + " fuera del espacio de usuario [" + inicioUsuario + " - " + (tamanoTotal - 1) + "]");
        }
    }

    
    // lectura general y utilidades
    public String leer(int posicion) {
        if (posicion < 0 || posicion >= tamanoTotal) {
            throw new IndexOutOfBoundsException("Posicion fuera de rango: " + posicion);
        }
        return datos[posicion];
    }

    public void limpiar() {
        datos = new String[tamanoTotal];
    }

    public int getTamanoTotal() {
        return tamanoTotal;
    }

    public int getFinKernel() {
        return finKernel;
    }

    public int getInicioUsuario() {
        return inicioUsuario;
    }

    public String[] getDatos() {
        return datos;
    }
}