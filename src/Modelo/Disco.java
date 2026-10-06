package Modelo;

/**
 * Representa el almacenamiento secundario (disco) del Mini PC.
 * Se divide en 3 zonas consecutivas:
 *   1. Programas: copia de los archivos asm cargados
 *   2. Memoria virtual: zona de swap para procesos que no caben en RAM
 *   3. Archivos virtuales: usados por la instruccion INT 21H
 *
 * Los porcentajes estan fijos por ahora como constantes, pero se va a cambiar
 * para que se lean desde un archivo json
 *
 * @author Diego Araya
 */
public class Disco {

    public static final int TAMANO_MINIMO = 64;

    private String[] datos;
    private int tamanoTotal;

    // limites de cada zona (inicio inclusive, fin exclusivo)
    private int inicioProgramas;
    private int finProgramas;
    private int inicioMemoriaVirtual;
    private int finMemoriaVirtual;
    private int inicioArchivos;
    private int finArchivos;

    public Disco(int tamanoTotal, double porcentajeProgramas, double porcentajeMemoriaVirtual) {
        if (tamanoTotal < TAMANO_MINIMO) {
            tamanoTotal = TAMANO_MINIMO;
        }
        this.tamanoTotal = tamanoTotal;
        this.datos = new String[tamanoTotal];

        inicioProgramas = 0;
        finProgramas = (int) (tamanoTotal * porcentajeProgramas);

        inicioMemoriaVirtual = finProgramas;
        finMemoriaVirtual = inicioMemoriaVirtual + (int) (tamanoTotal * porcentajeMemoriaVirtual);

        // los archivos virtuales se quedan con todo lo que sobre
        inicioArchivos = finMemoriaVirtual;
        finArchivos = tamanoTotal;
    }

    // ---- zona de programas ----

    public void escribirPrograma(int posicion, String valor) {
        validarZona(posicion, inicioProgramas, finProgramas, "programas");
        datos[posicion] = valor;
    }

    public int getInicioProgramas() {
        return inicioProgramas;
    }

    public int getFinProgramas() {
        return finProgramas;
    }

    // ---- zona de memoria virtual (swap) ----

    public void escribirMemoriaVirtual(int posicion, String valor) {
        validarZona(posicion, inicioMemoriaVirtual, finMemoriaVirtual, "memoria virtual");
        datos[posicion] = valor;
    }

    public int getInicioMemoriaVirtual() {
        return inicioMemoriaVirtual;
    }

    public int getFinMemoriaVirtual() {
        return finMemoriaVirtual;
    }
    
     /**
     * Busca un espacio libre y contiguo dentro de la zona de
     * memoria virtual, con al menos la cantidad de posiciones
     * pedida
     * Devuelve donde empieza, o -1 si no cabe
     */
    public int buscarEspacioMemoriaVirtual(int cantidadPosiciones) {
        int libresSeguidas = 0;
        int inicioCandidato = -1;
 
        for (int i = inicioMemoriaVirtual; i < finMemoriaVirtual; i++) {
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
     * Libera un rango de posiciones dentro de la memoria virtual,
     * usado cuando un proceso vuelve a memoria principal (swap in)
     */
    public void liberarMemoriaVirtual(int posicionInicio, int cantidadPosiciones) {
        for (int i = 0; i < cantidadPosiciones; i++) {
            datos[posicionInicio + i] = null;
        }
    }

    // ---- zona de archivos virtuales (INT 21H) ----

    public void escribirArchivo(int posicion, String valor) {
        validarZona(posicion, inicioArchivos, finArchivos, "archivos virtuales");
        datos[posicion] = valor;
    }

    public int getInicioArchivos() {
        return inicioArchivos;
    }

    public int getFinArchivos() {
        return finArchivos;
    }

    // ---- lectura general, sirve para cualquier zona ----

    public String leer(int posicion) {
        if (posicion < 0 || posicion >= tamanoTotal) {
            throw new IndexOutOfBoundsException("Posicion fuera de rango: " + posicion);
        }
        return datos[posicion];
    }

    private void validarZona(int posicion, int inicio, int fin, String nombreZona) {
        if (posicion < inicio || posicion >= fin) {
            throw new IllegalArgumentException(
                    "Posicion " + posicion + " no pertenece a la zona de " + nombreZona
                    + " [" + inicio + " - " + (fin - 1) + "]");
        }
    }

    public void limpiar() {
        datos = new String[tamanoTotal];
    }

    public int getTamanoTotal() {
        return tamanoTotal;
    }

    public String[] getDatos() {
        return datos;
    }
}