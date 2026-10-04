package Modelo;
import java.util.List;

/**
 * Representa una instruccion ya interpretada del asm
 * Guarda tanto el original y su representación en binario de 8 bits (opcode + operando).
 *
 * @author Diego Araya
 */
public class Instruccion {

    private String textoOriginal;   
    private String opcode;          
    private String registroDestino;
    private String registroOrigen;
    private Integer valor;  
    private Integer desplazamiento;
    private List<Integer> parametros;
    private int peso;
    
    //private String binario;         

    public Instruccion(String textoOriginal, String opcode, String registroDestino,String registroOrigen, Integer valor, Integer desplazamiento,List<Integer> parametros, int peso) {
        this.textoOriginal = textoOriginal;
        this.opcode = opcode;
        this.registroDestino = registroDestino;
        this.registroOrigen = registroOrigen;
        this.desplazamiento = desplazamiento;
        this.parametros = parametros;
        this.peso = peso;
        this.valor = valor;
        //this.binario = binario;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    public String getOpcode() {
        return opcode;
    }

    public String getRegistroDestino() {
        return registroDestino;
    }
    
    public String getRegistroOrigen() {
        return registroOrigen;
    }

    public Integer getValor() {
        return valor;
    }
    
    public Integer getDesplazamiento(){
        return desplazamiento;
    }
    
    public List<Integer> getParametros(){
        return parametros;
    }
    
    public int getPeso(){
        return peso;
    }

    /**public String getBinario() {
        return binario;
    }
    **/
    
    @Override
    public String toString() {
        return textoOriginal;
    }
}