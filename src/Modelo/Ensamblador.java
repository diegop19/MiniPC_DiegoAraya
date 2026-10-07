package Modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static final int PESO_BLOQUEO = -1; // usado para INT 09H
 
    // registros validos
    private static final List<String> REGISTROS = Arrays.asList("AX", "BX", "CX", "DX");
 
    // peso de cada opcode 
    private static final Map<String, Integer> PESOS = new HashMap<>();
    static {
        PESOS.put("LOAD", 2);
        PESOS.put("STORE", 2);
        PESOS.put("MOV", 1);
        PESOS.put("ADD", 3);
        PESOS.put("SUB", 3);
        PESOS.put("INC", 1);
        PESOS.put("DEC", 1);
        PESOS.put("SWAP", 1);
        PESOS.put("JMP", 2);
        PESOS.put("CMP", 2);
        PESOS.put("JE", 2);
        PESOS.put("JNE", 2);
        PESOS.put("PARAM", 3);
        PESOS.put("PUSH", 1);
        PESOS.put("POP", 1);
    }
 
    // peso de cada codigo de interrupcion
    private static final Map<String, Integer> PESOS_INT = new HashMap<>();
    static {
        PESOS_INT.put("20H", 2);
        PESOS_INT.put("10H", 2);
        PESOS_INT.put("09H", PESO_BLOQUEO);
        PESOS_INT.put("21H", 5);
    }
    

    public List<Instruccion> parsearArchivo(File archivo) throws IOException, FormatoInvalidoException {
       List<Instruccion> instrucciones = new ArrayList<>();
       List<String> errores = new ArrayList<>();

       try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
           String linea;
           int numeroLinea = 0;

           while ((linea = lector.readLine()) != null) {
               numeroLinea++;
               linea = linea.trim();

               if (linea.isEmpty()) {
                   continue;
               }

               // se guardan todos los errores en vez de detenerse en el primero
               try {
                   instrucciones.add(parsearLinea(linea, numeroLinea));
               } catch (FormatoInvalidoException e) {
                   errores.add(e.getMessage());
               }
           }
       }

    // si hubo errores, se lanza una sola excepcion con todos juntos
    if (!errores.isEmpty()) {
        throw new FormatoInvalidoException(String.join("\n", errores));
    }

    return instrucciones;
}
 
    /**
     * Parsea una sola linea y construye el objeto Instruccion
     */
    private Instruccion parsearLinea(String linea, int numeroLinea) throws FormatoInvalidoException {
        // el opcode va separado del resto solo por espacios
        String[] partes = linea.split("\\s+", 2);
        String opcode = partes[0].toUpperCase();

        // los operandos se separan por una sola coma, con espacios opcionales
        String[] operandos = new String[0];
        if (partes.length == 2) {
            operandos = partes[1].split("\\s*,\\s*", -1);
        }

        // un operando vacio significa coma de mas, al inicio o al final
        // un operando con espacios adentro significa que falto una coma
        for (String operando : operandos) {
            if (operando.isEmpty() || operando.matches(".*\\s.*")) {
                throw new FormatoInvalidoException(
                        "Linea " + numeroLinea + ": comas o espacios mal colocados -> \"" + linea + "\"");
            }
        }

        String[] tokens = new String[operandos.length + 1];
        tokens[0] = partes[0];
        for (int i = 0; i < operandos.length; i++) {
            tokens[i + 1] = operandos[i];
        }
        
        switch (opcode) {
            case "LOAD":
            case "STORE":
            case "PUSH":
            case "POP":
            case "ADD":
            case "SUB":
                return parsearUnRegistro(linea, tokens, numeroLinea, opcode);
 
            case "INC":
            case "DEC":
                return parsearIncDec(linea, tokens, numeroLinea, opcode);
 
            case "MOV":
                return parsearMov(linea, tokens, numeroLinea);
 
            case "SWAP":
            case "CMP":
                return parsearDosRegistros(linea, tokens, numeroLinea, opcode);
 
            case "INT":
                return parsearInt(linea, tokens, numeroLinea);
 
            case "JMP":
            case "JE":
            case "JNE":
                return parsearSalto(linea, tokens, numeroLinea, opcode);
 
            case "PARAM":
                return parsearParam(linea, tokens, numeroLinea);
 
            default:
                throw new FormatoInvalidoException(
                        "Linea " + numeroLinea + ": operador desconocido \"" + tokens[0] + "\"");
        }
    }
 
    // LOAD reg / STORE reg / PUSH reg / POP reg / ADD reg / SUB reg
    private Instruccion parsearUnRegistro(String linea, String[] tokens, int numeroLinea, String opcode)
            throws FormatoInvalidoException {
        if (tokens.length != 2) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": " + opcode + " requiere un registro -> \"" + linea + "\"");
        }
        String registro = validarRegistro(tokens[1], numeroLinea);
        return new Instruccion(linea, opcode, registro, null, null, null, null, PESOS.get(opcode));
    }
 
    // INC o INC reg / DEC o DEC reg
    private Instruccion parsearIncDec(String linea, String[] tokens, int numeroLinea, String opcode)
            throws FormatoInvalidoException {
        if (tokens.length == 1) {
            // actua sobre AC, sin registro destino
            return new Instruccion(linea, opcode, null, null, null, null, null, PESOS.get(opcode));
        }
        if (tokens.length == 2) {
            String registro = validarRegistro(tokens[1], numeroLinea);
            return new Instruccion(linea, opcode, registro, null, null, null, null, PESOS.get(opcode));
        }
        throw new FormatoInvalidoException(
                "Linea " + numeroLinea + ": formato invalido para " + opcode + " -> \"" + linea + "\"");
    }
 
    // MOV reg_destino, reg_origen   o   MOV reg_destino, valor
    private Instruccion parsearMov(String linea, String[] tokens, int numeroLinea) throws FormatoInvalidoException {
        if (tokens.length != 3) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": MOV requiere destino y origen -> \"" + linea + "\"");
        }
        String destino = validarRegistro(tokens[1], numeroLinea);
 
        if (REGISTROS.contains(tokens[2].toUpperCase())) {
            String origen = validarRegistro(tokens[2], numeroLinea);
            return new Instruccion(linea, "MOV", destino, origen, null, null, null, PESOS.get("MOV"));
        }
 
        Integer valor = parsearEntero(tokens[2], numeroLinea);
        return new Instruccion(linea, "MOV", destino, null, valor, null, null, PESOS.get("MOV"));
    }
 
    // SWAP reg1, reg2 / CMP reg1, reg2
    private Instruccion parsearDosRegistros(String linea, String[] tokens, int numeroLinea, String opcode)
            throws FormatoInvalidoException {
        if (tokens.length != 3) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": " + opcode + " requiere dos registros -> \"" + linea + "\"");
        }
        String reg1 = validarRegistro(tokens[1], numeroLinea);
        String reg2 = validarRegistro(tokens[2], numeroLinea);
        return new Instruccion(linea, opcode, reg1, reg2, null, null, null, PESOS.get(opcode));
    }
 
    // INT 20H / INT 10H / INT 09H / INT 21H
    private Instruccion parsearInt(String linea, String[] tokens, int numeroLinea) throws FormatoInvalidoException {
        if (tokens.length != 2) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": INT requiere un codigo -> \"" + linea + "\"");
        }
        String codigo = tokens[1].toUpperCase();
        if (!PESOS_INT.containsKey(codigo)) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": codigo de interrupcion invalido \"" + tokens[1] + "\"");
        }
        int peso = PESOS_INT.get(codigo);
        int valorCodigo = Integer.parseInt(codigo.replace("H", ""), 16);
        return new Instruccion(linea, "INT", null, null, valorCodigo, null, null, peso);
    }
 
// JMP +3 / JMP -2 / JE +3 / JNE -1
    private Instruccion parsearSalto(String linea, String[] tokens, int numeroLinea, String opcode)
            throws FormatoInvalidoException {
        if (tokens.length != 2) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": " + opcode + " requiere un desplazamiento -> \"" + linea + "\"");
        }
        Integer desplazamiento = parsearEntero(tokens[1], numeroLinea);
        return new Instruccion(linea, opcode, null, null, null, desplazamiento, null, PESOS.get(opcode));
    }
 
    // PARAM v1, v2, v3 (maximo 3 valores)
    private Instruccion parsearParam(String linea, String[] tokens, int numeroLinea) throws FormatoInvalidoException {
        int cantidadValores = tokens.length - 1;
        if (cantidadValores < 1 || cantidadValores > 3) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": PARAM admite de 1 a 3 valores -> \"" + linea + "\"");
        }
        List<Integer> parametros = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            parametros.add(parsearEntero(tokens[i], numeroLinea));
        }
        return new Instruccion(linea, "PARAM", null, null, null, null, parametros, PESOS.get("PARAM"));
    }
 
    // valida que el texto sea uno de los 4 registros permitidos
    private String validarRegistro(String texto, int numeroLinea) throws FormatoInvalidoException {
        String registro = texto.toUpperCase();
        if (!REGISTROS.contains(registro)) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": registro desconocido \"" + texto + "\"");
        }
        return registro;
    }
 
    // convierte texto a entero, validando signo y rango de 8 bits (-127 a 127)
    private Integer parsearEntero(String texto, int numeroLinea) throws FormatoInvalidoException {
        try {
            int valor = Integer.parseInt(texto);
            if (valor < -127 || valor > 127) {
                throw new FormatoInvalidoException(
                        "Linea " + numeroLinea + ": valor fuera de rango (-127 a 127) \"" + valor + "\"");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new FormatoInvalidoException(
                    "Linea " + numeroLinea + ": valor numerico invalido \"" + texto + "\"");
        }
    }
}
 