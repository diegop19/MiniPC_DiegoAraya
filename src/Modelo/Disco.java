package Modelo;

import java.util.List;

/**
 * Representa el almacenamiento secundario (disco) del Mini PC.
 * Se divide en 2 zonas
 *   1. Programas: copia de los archivos asm cargados
 *   2. Memoria virtual: zona de swap para procesos que no caben en RAM

 * @author Diego Araya
 */
public class Disco {

    public static final int TAMANO_MINIMO = 64;
    public static final int ENTRADAS_INDICE = 10;

    private String[] datos;
    private int tamanoTotal;

    // limites de cada zona 
    private int inicioProgramas;
    private int finProgramas;
    private int inicioMemoriaVirtual;
    private int finMemoriaVirtual;

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

    }

    // ---- zona de programas ----

    public void escribirPrograma(int posicion, String valor) {
        validarZona(posicion, inicioProgramas, finProgramas, "programas");
        datos[posicion] = valor;
    }
    
    /**
    * Revisa si todavia caben cantidadArchivos entradas en el indice y
    * totalInstrucciones lineas en la zona de programas. Se usa antes de
    * guardar, para no cargar solo una parte de los archivos.
    */
   public boolean hayEspacioParaProgramas(int cantidadArchivos, int totalInstrucciones) {
       int entradasLibres = 0;
       for (int i = inicioProgramas; i < inicioProgramas + ENTRADAS_INDICE; i++) {
           if (datos[i] == null) {
               entradasLibres++;
           }
       }

       int posicionesLibres = 0;
       for (int i = inicioProgramas + ENTRADAS_INDICE; i < finProgramas; i++) {
           if (datos[i] == null) {
               posicionesLibres++;
           }
       }

       return cantidadArchivos <= entradasLibres && totalInstrucciones <= posicionesLibres;
   }

   /**
    * Guarda un programa completo en la zona de programas y agrega su
    * entrada al indice con el formato nombre:direccion. Devuelve la
    * direccion donde quedo guardado.
    */
   public int guardarPrograma(String nombreArchivo, List<Instruccion> programa) {
       int entrada = buscarEntradaIndiceLibre();
       int direccion = buscarEspacioProgramas(programa.size());

       if (entrada == -1 || direccion == -1) {
           throw new IllegalStateException("No hay espacio en el disco para guardar " + nombreArchivo);
       }

       for (int i = 0; i < programa.size(); i++) {
           datos[direccion + i] = programa.get(i).getTextoOriginal();
       }
       datos[entrada] = nombreArchivo + ":" + direccion;

       return direccion;
   }

   private int buscarEntradaIndiceLibre() {
       for (int i = inicioProgramas; i < inicioProgramas + ENTRADAS_INDICE; i++) {
           if (datos[i] == null) {
               return i;
           }
       }
       return -1;
   }

   // busca espacio contiguo despues del indice, nunca dentro de el
   private int buscarEspacioProgramas(int cantidadPosiciones) {
       int libresSeguidas = 0;
       int inicioCandidato = -1;

       for (int i = inicioProgramas + ENTRADAS_INDICE; i < finProgramas; i++) {
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