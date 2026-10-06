package Modelo;

import java.util.List;

/**
 * Hace el cambio de contexto entre procesos
 * Cuando un proceso entra al cpu, copia sus valores 
 * guardados del BCP hacia losregistros del CPU
 * Cuando un proceso sale, hace lo contrario,
 * copia los registros del CPU de vuelta a su BCP para no perder
 * el progreso
 *
 * @author Diego Araya
 */
public class Despachador {

    /**
     * Monta un proceso en el cpu, restaurando sus registros
     * guardados y dejandolo en estado EJECUTANDO
     */
    public void despachar(CPU cpu, BCP bcp, List<Instruccion> programa) {
        cpu.setAc(bcp.getAc());
        cpu.setAx(bcp.getAx());
        cpu.setBx(bcp.getBx());
        cpu.setCx(bcp.getCx());
        cpu.setDx(bcp.getDx());

        cpu.cargarProceso(programa, bcp.getBase(), bcp.getPc(), bcp.getPila());

        bcp.setEstado(BCP.Estado.EJECUTANDO);
    }

    /**
     * Saca el proceso actual del cpu, guardando sus registros en
     * el BCP, y le deja el estado que corresponda (bloqueado,
     * terminado, listo, etc, eso lo decide quien llama este metodo).
     */
    public void sacarProceso(CPU cpu, BCP bcp, BCP.Estado nuevoEstado) {
        bcp.setAc(cpu.getAc());
        bcp.setAx(cpu.getAx());
        bcp.setBx(cpu.getBx());
        bcp.setCx(cpu.getCx());
        bcp.setDx(cpu.getDx());
        bcp.setPc(cpu.getPc());

        if (cpu.getIr() != null) {
            bcp.setIr(cpu.getIr().getTextoOriginal());
        }

        bcp.setEstado(nuevoEstado);
    }
}