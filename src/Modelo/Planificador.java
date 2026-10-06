package Modelo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Planificador FCFS
 * Decide que trabajo entra a memoria principal,
 * maneja hasta 5 procesos residentes a la vez, y cuando no hay
 * espacio intenta liberar sacando a disco (swap out) un proceso
 * que no este corriendo en ese momento
 * Tambien se encarga de traer de vuelta procesos que estaban en disco cuando se libera
 * espacio (swap in)
 *
 * @author Diego Araya
 */
public class Planificador {

    public static final int MAX_PROCESOS_RESIDENTES = 5;

    private ColaTrabajos colaTrabajos;
    private TablaProcesos tablaProcesos;
    private Memoria memoria;
    private Disco disco;

    // el programa real (la lista de instrucciones) de cada proceso
    // se guarda aca mientras el proceso existe, independiente de si
    // esta en memoria o en disco en este momento. En memoria/disco
    // solo se guarda el texto, como espejo para poder mostrarlo
    private Map<Integer, List<Instruccion>> programasPorPid;

    public Planificador(ColaTrabajos colaTrabajos, TablaProcesos tablaProcesos, Memoria memoria, Disco disco) {
        this.colaTrabajos = colaTrabajos;
        this.tablaProcesos = tablaProcesos;
        this.memoria = memoria;
        this.disco = disco;
        this.programasPorPid = new HashMap<>();
    }

    public void agregarTrabajo(Trabajo trabajo) {
        colaTrabajos.agregar(trabajo);
    }

    /**
     * Intenta meter el siguiente trabajo de la cola a memoria
     * principal. Si no hay espacio, primero intenta sacar a disco
     * algun proceso que no este corriendo. Si de todas formas no
     * se puede, el trabajo se queda esperando y se devuelve null.
     */
    public BCP intentarIngresarSiguiente() {
        Trabajo trabajo = colaTrabajos.verSiguiente();
        if (trabajo == null) {
            return null;
        }

        List<Instruccion> programa = trabajo.getPrograma();

        if (contarResidentes() >= MAX_PROCESOS_RESIDENTES) {
            if (!intentarLiberarEspacio()) {
                return null; // ya hay 5 y no se pudo sacar ninguno, toca esperar
            }
        }

        int posicionPrograma = memoria.buscarEspacioUsuario(programa.size());
        if (posicionPrograma == -1) {
            if (!intentarLiberarEspacio()) {
                return null;
            }
            posicionPrograma = memoria.buscarEspacioUsuario(programa.size());
            if (posicionPrograma == -1) {
                return null; // de plano no cabe ni liberando
            }
        }

        // ahora si hay espacio, se saca de la cola de verdad
        colaTrabajos.siguiente();

        for (int i = 0; i < programa.size(); i++) {
            memoria.escribirUsuario(posicionPrograma + i, programa.get(i).getTextoOriginal());
        }

        BCP bcp = new BCP(trabajo.getPid(), posicionPrograma, posicionPrograma + programa.size() - 1);
        bcp.setEstado(BCP.Estado.LISTO);

        int posicionKernel = tablaProcesos.agregar(bcp);
        if (posicionKernel == -1) {
            // raro, pero si no hay espacio ni para el bcp, se deshace todo
            memoria.liberarUsuario(posicionPrograma, programa.size());
            colaTrabajos.agregar(trabajo);
            return null;
        }

        programasPorPid.put(trabajo.getPid(), programa);
        return bcp;
    }

    /**
     * Busca entre los procesos residentes uno que este LISTO (no
     * corriendo ni bloqueado) y lo manda a disco para liberar su
     * espacio. Devuelve true si logro sacar alguno.
     */
    private boolean intentarLiberarEspacio() {
        for (BCP candidato : tablaProcesos.listarTodos()) {
            if (candidato.getEstado() == BCP.Estado.LISTO) {
                hacerSwapOut(candidato);
                return true;
            }
        }
        return false;
    }

    private void hacerSwapOut(BCP bcp) {
        List<Instruccion> programa = programasPorPid.get(bcp.getPid());
        int cantidadLineas = programa.size();

        int posicionDisco = disco.buscarEspacioMemoriaVirtual(cantidadLineas);
        if (posicionDisco == -1) {
            throw new IllegalStateException("No hay espacio en la memoria virtual para hacer swap");
        }

        for (int i = 0; i < cantidadLineas; i++) {
            disco.escribirMemoriaVirtual(posicionDisco + i, programa.get(i).getTextoOriginal());
        }

        memoria.liberarUsuario(bcp.getBase(), cantidadLineas);

        int posicionKernel = tablaProcesos.buscarPosicionPorPid(bcp.getPid());

        // reutilizamos base y limite para que ahora apunten a la
        // memoria virtual del disco, en vez de agregar un campo nuevo
        bcp.setEstado(BCP.Estado.LISTO_SUSPENDIDO);
        bcp.setBase(posicionDisco);
        bcp.setLimite(posicionDisco + cantidadLineas - 1);

        tablaProcesos.actualizar(posicionKernel, bcp);
    }

    /**
     * Revisa si hay algun proceso suspendido en disco que ya pueda
     * volver a memoria principal, y lo trae de vuelta si hay campo.
     * Devuelve el BCP que se trajo, o null si no habia ninguno o no
     * cupo.
     */
    public BCP intentarSwapIn() {
        for (BCP candidato : tablaProcesos.listarTodos()) {
            boolean estaSuspendido = candidato.getEstado() == BCP.Estado.LISTO_SUSPENDIDO
                    || candidato.getEstado() == BCP.Estado.BLOQUEADO_SUSPENDIDO;

            if (estaSuspendido) {
                boolean exito = hacerSwapIn(candidato);
                if (exito) {
                    return candidato;
                }
            }
        }
        return null;
    }

    private boolean hacerSwapIn(BCP bcp) {
        List<Instruccion> programa = programasPorPid.get(bcp.getPid());
        int cantidadLineas = programa.size();

        int posicionMemoria = memoria.buscarEspacioUsuario(cantidadLineas);
        if (posicionMemoria == -1) {
            return false; // todavia no cabe en memoria principal
        }

        for (int i = 0; i < cantidadLineas; i++) {
            memoria.escribirUsuario(posicionMemoria + i, programa.get(i).getTextoOriginal());
        }

        disco.liberarMemoriaVirtual(bcp.getBase(), cantidadLineas);

        int posicionKernel = tablaProcesos.buscarPosicionPorPid(bcp.getPid());

        if (bcp.getEstado() == BCP.Estado.BLOQUEADO_SUSPENDIDO) {
            bcp.setEstado(BCP.Estado.BLOQUEADO);
        } else {
            bcp.setEstado(BCP.Estado.LISTO);
        }
        bcp.setBase(posicionMemoria);
        bcp.setLimite(posicionMemoria + cantidadLineas - 1);

        tablaProcesos.actualizar(posicionKernel, bcp);
        return true;
    }

    /**
     * Devuelve el primer proceso en estado LISTO que encuentre en
     * la tabla, siguiendo el orden en el que fueron insertados
     * (asi se respeta FCFS).
     */
    public BCP elegirSiguienteListo() {
        for (BCP bcp : tablaProcesos.listarTodos()) {
            if (bcp.getEstado() == BCP.Estado.LISTO) {
                return bcp;
            }
        }
        return null;
    }

    /**
     * Libera todo lo que tenia asignado un proceso que ya termino,
     * y lo saca por completo de la tabla de procesos.
     */
    public void finalizarProceso(BCP bcp) {
        List<Instruccion> programa = programasPorPid.get(bcp.getPid());
        int cantidadLineas = programa.size();

        boolean estabaEnDisco = bcp.getEstado() == BCP.Estado.LISTO_SUSPENDIDO
                || bcp.getEstado() == BCP.Estado.BLOQUEADO_SUSPENDIDO;

        if (estabaEnDisco) {
            disco.liberarMemoriaVirtual(bcp.getBase(), cantidadLineas);
        } else {
            memoria.liberarUsuario(bcp.getBase(), cantidadLineas);
        }

        int posicionKernel = tablaProcesos.buscarPosicionPorPid(bcp.getPid());
        tablaProcesos.eliminar(posicionKernel);
        programasPorPid.remove(bcp.getPid());
    }

    private int contarResidentes() {
        int contador = 0;
        for (BCP bcp : tablaProcesos.listarTodos()) {
            BCP.Estado estado = bcp.getEstado();
            boolean esResidente = estado == BCP.Estado.NUEVO
                    || estado == BCP.Estado.LISTO
                    || estado == BCP.Estado.EJECUTANDO
                    || estado == BCP.Estado.BLOQUEADO;
            if (esResidente) {
                contador++;
            }
        }
        return contador;
    }

    public List<Instruccion> getProgramaDe(int pid) {
        return programasPorPid.get(pid);
    }
}