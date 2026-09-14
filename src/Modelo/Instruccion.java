package Modelo;

/**
 * Representa una instruccion ya interpretada del asm
 * Guarda tanto el original y su representación en binario de 8 bits (opcode + operando).
 *
 * @author Diego Araya
 */
public class Instruccion {

    private String textoOriginal;   
    private String opcode;          
    private String registro;        
    private Integer valor;          
    private String binario;         

    public Instruccion(String textoOriginal, String opcode, String registro, Integer valor, String binario) {
        this.textoOriginal = textoOriginal;
        this.opcode = opcode;
        this.registro = registro;
        this.valor = valor;
        this.binario = binario;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    public String getOpcode() {
        return opcode;
    }

    public String getRegistro() {
        return registro;
    }

    public Integer getValor() {
        return valor;
    }

    public String getBinario() {
        return binario;
    }

    @Override
    public String toString() {
        return textoOriginal + " -> " + binario;
    }
}