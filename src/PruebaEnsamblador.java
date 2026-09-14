import Modelo.Ensamblador;
import Modelo.FormatoInvalidoException;
import Modelo.Instruccion;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Clase temporal solo para probar que el Ensamblador parsea
 * correctamente el archivo .asm de ejemplo. Se puede borrar
 * una vez confirmado que funciona.
 *
 * Instrucciones de uso:
 * 1. Crea un archivo "file.asm" con el contenido de la imagen 3.
 * 2. Ajusta la ruta en RUTA_ARCHIVO según donde lo guardaste.
 * 3. Ejecuta esta clase (botón derecho -> Run File).
 */
public class PruebaEnsamblador {

    private static final String RUTA_ARCHIVO = "file.asm";

    public static void main(String[] args) {
        System.out.println("Prueba ensamblador");
        Ensamblador ensamblador = new Ensamblador();

        try {
            List<Instruccion> instrucciones = ensamblador.parsearArchivo(new File(RUTA_ARCHIVO));

            System.out.println("Instrucciones parseadas: " + instrucciones.size());
            System.out.println("------------------------------------------------");
            for (Instruccion instr : instrucciones) {
                System.out.println(instr);
            }

        } catch (IOException e) {
            System.out.println("Error leyendo el archivo: " + e.getMessage());
        } catch (FormatoInvalidoException e) {
            System.out.println("Error de formato: " + e.getMessage());
        }
    }
}