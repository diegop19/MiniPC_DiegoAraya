package Modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Administra la lista enlazada de BCP que vive dentro de la zona
 * de kernel de la Memoria
 * Cada BCP ocupa 21 posiciones y el enlace entre uno y otro se guarda como una direccion
 * de memoria dentro del mismo bloque (campo SIGUIENTE_BCP)
 *
 * cabeza guarda la posicion donde empieza el primer BCP de la
 * lista
 * Si no hay ningun proceso todavia, vale -1
 *
 * @author Diego Araya
 */

public class TablaProcesos {

    private Memoria memoria;
    private int cabeza;

    public TablaProcesos(Memoria memoria) {
        this.memoria = memoria;
        this.cabeza = -1;
    }

    /**
     * Agrega un BCP nuevo a la lista, buscando un slot libre en el
     * kernel
     * Devuelve la posicion donde quedo guardado, o -1 si
     * no habia espacio disponible
     */
    public int agregar(BCP bcp) {
        int posicion = memoria.buscarSlotKernelLibre();
        if (posicion == -1) {
            return -1;
        }

        bcp.setSiguienteBCP(-1); 
        memoria.escribirBCP(posicion, bcp.serializar());

        if (cabeza == -1) {
            cabeza = posicion;
        } else {
            // hay que recorrer hasta el ultimo nodo para enlazarlo
            int actual = cabeza;
            BCP nodoActual = leer(actual);
            while (nodoActual.getSiguienteBCP() != -1) {
                actual = nodoActual.getSiguienteBCP();
                nodoActual = leer(actual);
            }
            nodoActual.setSiguienteBCP(posicion);
            memoria.escribirBCP(actual, nodoActual.serializar());
        }

        return posicion;
    }

    /**
     * Lee y reconstruye el BCP que esta guardado en una posicion
     * especifica de la memoria
     */
    public BCP leer(int posicion) {
        String[] lineas = memoria.leerBCP(posicion);
        return BCP.deserializar(lineas);
    }

    /**
     * Vuelve a guardar un BCP ya modificado, en la misma posicion
     * donde ya estaba 
     */
    public void actualizar(int posicion, BCP bcp) {
        memoria.escribirBCP(posicion, bcp.serializar());
    }

    /**
     * Quita un BCP de la lista, libera su espacio en memoria y
     * arregla el enlace del nodo anterior para que apunte al
     * siguiente del que se elimino
     */
    public void eliminar(int posicion) {
        if (cabeza == posicion) {
            BCP nodo = leer(posicion);
            cabeza = nodo.getSiguienteBCP();
            memoria.liberarBCP(posicion);
            return;
        }

        int actual = cabeza;
        BCP nodoActual = leer(actual);

        while (nodoActual.getSiguienteBCP() != posicion) {
            actual = nodoActual.getSiguienteBCP();
            if (actual == -1) {
                throw new IllegalArgumentException("No existe un BCP en la posicion " + posicion);
            }
            nodoActual = leer(actual);
        }

        BCP nodoAEliminar = leer(posicion);
        nodoActual.setSiguienteBCP(nodoAEliminar.getSiguienteBCP());
        memoria.escribirBCP(actual, nodoActual.serializar());
        memoria.liberarBCP(posicion);
    }
    
    /**
     * Recorre toda la lista enlazada y devuelve los BCP de todos
     * los procesos que estan actualmente en memoria principal
     */
    public List<BCP> listarTodos() {
        List<BCP> lista = new ArrayList<>();
        int actual = cabeza;
        while (actual != -1) {
            BCP nodo = leer(actual);
            lista.add(nodo);
            actual = nodo.getSiguienteBCP();
        }
        return lista;
    }
    
    /**
     * Busca en la lista la posicion donde esta guardado el BCP de
     * un proceso especifico, segun su pid
     * Devuelve -1 si no se encuentra (el proceso ya no existe o nunca existio)
     */
    public int buscarPosicionPorPid(int pid) {
        int actual = cabeza;
        while (actual != -1) {
            BCP nodo = leer(actual);
            if (nodo.getPid() == pid) {
                return actual;
            }
            actual = nodo.getSiguienteBCP();
        }
        return -1;
    }
    
    public int getCabeza() {
        return cabeza;
    }

    public boolean estaVacia() {
        return cabeza == -1;
    }
}