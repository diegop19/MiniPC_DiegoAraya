package Modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee un archivo .asm, valida el formato de cada línea y la convierte
 * en un objeto Instruccion con su representación binaria de 16 bits
 * (8 bits de opcode+registro, 8 bits de valor con signo).
 *
 * @author Diego Araya
 */
public class Ensamblador {

    // Tabla de opcodes (4 bits)
    private static final Map<String, String> OPCODES = new HashMap<>();
    static {
        OPCODES.put("LOAD", "0001");
        OPCODES.put("STORE", "0010");
        OPCODES.put("MOV", "0011");
        OPCODES.put("SUB", "0100");
        OPCODES.put("ADD", "0101");
    }

    // Tabla de registros (4 bits)
    private static final Map<String, String> REGISTROS = new HashMap<>();
    static {
        REGISTROS.put("AX", "0001");
        REGISTROS.put("BX", "0010");
        REGISTROS.put("CX", "0011");
        REGISTROS.put("DX", "0100");
    }

    /**
     * Lee el archivo completo y devuelve la lista de instrucciones parseadas.
     *
     * @param archivo archivo .asm a leer
     * @return lista de instrucciones validas
     * @throws IOException si no se puede leer el archivo
     * @throws FormatoInvalidoException si alguna línea no cumple el formato
     */
    public List<Instruccion> parsearArchivo(File archivo) throws IOException, FormatoInvalidoException {
        List<Instruccion> instrucciones = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();

                if (linea.isEmpty()) {
                    continue; // se ignoran líneas en blanco
                }

                instrucciones.add(parsearLinea(linea, numeroLinea));
            }
        }

        return instrucciones;
    }

    /**
     * Parsea una sola línea de texto y construye
     * el objeto Instruccion correspondiente, validando el formato.
     */
    private Instruccion parsearLinea(String linea, int numeroLinea) throws FormatoInvalidoException {
        // separa por espacios y  comas
        String[] tokens = linea.split("[,\\s]+");

        if (tokens.length < 2 || tokens.length > 3) { 
            throw new FormatoInvalidoException(
                    "Línea " + numeroLinea + ": formato inválido -> \"" + linea + "\"");
        }

        String opcodeTexto = tokens[0].toUpperCase();
        String registroTexto = tokens[1].toUpperCase();

        if (!OPCODES.containsKey(opcodeTexto)) {
            throw new FormatoInvalidoException(
                    "Línea " + numeroLinea + ": operador desconocido \"" + tokens[0] + "\"");
        }
        if (!REGISTROS.containsKey(registroTexto)) {
            throw new FormatoInvalidoException(
                    "Línea " + numeroLinea + ": registro desconocido \"" + tokens[1] + "\"");
        }

        Integer valor = null;
        if (tokens.length == 3) {
            try {
                valor = Integer.parseInt(tokens[2]);
            } catch (NumberFormatException e) {
                throw new FormatoInvalidoException(
                        "Línea " + numeroLinea + ": valor numérico inválido \"" + tokens[2] + "\"");
            }
            if (valor < -127 || valor > 127) {
                throw new FormatoInvalidoException(
                        "Línea " + numeroLinea + ": valor fuera de rango (-127 a 127) \"" + valor + "\"");
            }
        }

        String binario = construirBinario(opcodeTexto, registroTexto, valor);

        return new Instruccion(linea, opcodeTexto, registroTexto, valor, binario);
    }

    /**
     * Arma la cadena binaria de 16 bits
     *  4 bits opcode + 4 bits registro 
     *  1 bit signo + 7 bits magnitud 
     */
    private String construirBinario(String opcode, String registro, Integer valor) {
        String byteInstruccion = OPCODES.get(opcode) + REGISTROS.get(registro);

        int magnitud = 0;
        String bitSigno = "0";

        if (valor != null) {
            magnitud = Math.abs(valor);
            if (valor < 0) {
                bitSigno = "1";
            }
        }
        
        String magnitudBinaria = String.format("%7s", Integer.toBinaryString(magnitud)).replace(' ', '0');
        String byteValor = bitSigno + magnitudBinaria;

        return byteInstruccion + byteValor;
    }
}