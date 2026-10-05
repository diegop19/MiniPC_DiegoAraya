package Modelo;

/**
 * Pila de tamano fijo, usada dentro del BCP de cada proceso para
 * las instrucciones PUSH, POP y PARAM Valida desbordamiento tanto
 * al insertar (pila llena) como al extraer (pila vacia)
 *
 * @author Diego Araya
 */
public class Pila {

    public static final int TAMANO = 5;

    private Integer[] valores;
    private int tope; // indica cuantos elementos hay

    public Pila() {
        valores = new Integer[TAMANO];
        tope = 0;
    }

    /**
     * Agrega un valor a la pila. Si ya esta llena, lanza excepcion
     */
    public void push(int valor) {
        if (tope >= TAMANO) {
            throw new IllegalStateException("Desbordamiento de pila: ya tiene " + TAMANO + " elementos");
        }
        valores[tope] = valor;
        tope++;
    }

    /**
     * Saca y devuelve el ultimo valor agregado. Si esta vacia, lanza excepcion.
     */
    public int pop() {
        if (tope <= 0) {
            throw new IllegalStateException("La pila esta vacia, no se puede sacar ningun valor");
        }
        tope--;
        int valor = valores[tope];
        valores[tope] = null;
        return valor;
    }

    public boolean estaVacia() {
        return tope == 0;
    }

    public boolean estaLlena() {
        return tope >= TAMANO;
    }

    public int getCantidadElementos() {
        return tope;
    }

    /**
     * Devuelve una copia de los valores actuales, en orden de insercion.
     * Las posiciones sin usar quedan en null. Se usa para escribir
     * cada elemento de la pila en su propia posicion de memoria del BCP.
     */
    public Integer[] getValores() {
        return valores.clone();
    }

    /**
     * Reemplaza todos los valores de la pila, usado al reconstruir
     * la pila desde las posiciones de memoria del BCP.
     */
    public void cargarValores(Integer[] nuevosValores, int nuevoTope) {
        this.valores = nuevosValores.clone();
        this.tope = nuevoTope;
    }
}