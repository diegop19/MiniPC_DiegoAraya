package Modelo;

/**
 * Representa la memoria principal del PC
 * Se divide en dos zonas: espacio del Sistema Operativo  y
 * espacio de Usuario, donde se cargan las instrucciones del programa.
 *
 * @author Diego Araya
 */

public class Memoria {

    private String[] datos;      // contenido de cada posicion de memoria
    private int tamanoTotal;     // tamaño total de la memoria
    private int limiteSO;        // ultima posición reservada para el S.O. 
    private int inicioUsuario;   // primera posicion disponible para el usuario

    public static final int TAMANO_MINIMO = 128;
    public static final int ESPACIO_SO = 64; // posiciones fijas reservadas al SO

    /**
     * Crea la memoria con un tamaño dado, reservando siempre las
     * primeras posiciones para el Sistema Operativo.
     *
     * @param tamanoTotal tamaño total deseado 
     */
    public Memoria(int tamanoTotal) {
        if (tamanoTotal < TAMANO_MINIMO) {
            tamanoTotal = TAMANO_MINIMO;
        }
        this.tamanoTotal = tamanoTotal;
        this.datos = new String[tamanoTotal];
        this.limiteSO = ESPACIO_SO;      // siempre 64, sin importar el tamaño total
        this.inicioUsuario = ESPACIO_SO; // el usuario empieza en la posición 64
    }

    /**
     * Guarda un valor en una posicion de memoria, validando que
     * este dentro del espacio de usuario.
     */
    public void escribir(int posicion, String valor) {
        validarPosicionUsuario(posicion);
        datos[posicion] = valor;
    }

    /**
     * Lee el valor almacenado en una posicion de memoria.
     */
    public String leer(int posicion) {
        if (posicion < 0 || posicion >= tamanoTotal) {
            throw new IndexOutOfBoundsException("Posición fuera de rango: " + posicion);
        }
        return datos[posicion];
    }

    /**
     * Valida que una posicion pertenezca al espacio de usuario
     */
    private void validarPosicionUsuario(int posicion) {
        if (posicion < inicioUsuario || posicion >= tamanoTotal) {
            throw new IllegalArgumentException(
                    "Posición " + posicion + " fuera del espacio de usuario [" + inicioUsuario + " - " + (tamanoTotal - 1) + "]");
        }
    }

    /**
     * Limpia todo el contenido de la memoria 
     */
    public void limpiar() {
        datos = new String[tamanoTotal];
    }

    public int getTamanoTotal() {
        return tamanoTotal;
    }

    public int getLimiteSO() {
        return limiteSO;
    }

    public int getInicioUsuario() {
        return inicioUsuario;
    }

    public String[] getDatos() {
        return datos;
    }
}