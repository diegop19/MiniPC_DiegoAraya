package Modelo;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Cola de trabajos esperando a que el planificador los deje entrar
 * a memoria principal 
 *
 * @author Diego Araya
 */
public class ColaTrabajos {

    private Queue<Trabajo> trabajos;

    public ColaTrabajos() {
        trabajos = new LinkedList<>();
    }

    public void agregar(Trabajo trabajo) {
        trabajos.add(trabajo);
    }

    /**
     * Saca y devuelve el trabajo que lleva mas tiempo esperando
     * Si la cola esta vacia devuelve null
     */
    public Trabajo siguiente() {
        return trabajos.poll();
    }

    /**
     * Mira cual es el siguiente trabajo sin sacarlo de la cola,
     * sirve para revisar si hay espacio en memoria antes de sacarlo
     */
    public Trabajo verSiguiente() {
        return trabajos.peek();
    }

    public boolean estaVacia() {
        return trabajos.isEmpty();
    }

    public int getCantidad() {
        return trabajos.size();
    }
}