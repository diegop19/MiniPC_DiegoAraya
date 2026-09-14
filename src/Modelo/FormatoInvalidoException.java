package Modelo;

/**
 * Excepción lanzada cuando una línea del archivo .asm no cumple
 * con el formato esperado
 * @author Diego Araya
 */
public class FormatoInvalidoException extends Exception {

    public FormatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}