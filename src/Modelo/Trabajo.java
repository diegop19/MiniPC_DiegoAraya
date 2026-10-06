package Modelo;

import java.util.List;

/**
 * Representa un programa que todavia no tiene BCP ni espacio en
 * memoria asignado, esta esperando su turno en la ColaTrabajos
 *
 * @author Diego Araya
 */
public class Trabajo {

    private int pid;
    private String nombreArchivo;
    private List<Instruccion> programa;

    public Trabajo(int pid, String nombreArchivo, List<Instruccion> programa) {
        this.pid = pid;
        this.nombreArchivo = nombreArchivo;
        this.programa = programa;
    }

    public int getPid() {
        return pid;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public List<Instruccion> getPrograma() {
        return programa;
    }
}