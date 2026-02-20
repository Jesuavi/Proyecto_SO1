package Clases;

public class Administrador {
    
    // --- CONSTANTES DE ALGORITMOS ---
    public static final int FCFS = 0;
    public static final int ROUND_ROBIN = 1;
    public static final int SRT = 2;
    public static final int PRIORIDAD = 3;
    public static final int EDF = 4;
    
    public int algoritmoActual = ROUND_ROBIN; 
    private int quantum = 2; 
    private int contadorQuantum = 0;
    
    // --- COLAS DE ESTADOS ---
    public Cola colaListos = new Cola();
    public Cola colaBloqueados = new Cola();        
    public Cola colaListoSuspendido = new Cola();   
    public Cola colaBloqueadoSuspendido = new Cola(); 
    
    // --- RECURSOS Y MEMORIA ---
    public Proceso cpu = null;
    public final int MAX_RAM_MB = 128; 
    public static int cicloReloj = 0;
    
    private boolean emergenciaActiva = false;

    public Administrador() {}

    public int obtenerRamEnUso() {
        int uso = 0;
        if (cpu != null) uso += cpu.tamano;
        
        Nodo actualListo = colaListos.inicio;
        while (actualListo != null) {
            uso += actualListo.dato.tamano;
            actualListo = actualListo.siguiente;
        }
        
        Nodo actualBloq = colaBloqueados.inicio;
        while (actualBloq != null) {
            uso += actualBloq.dato.tamano;
            actualBloq = actualBloq.siguiente;
        }
        return uso;
    }

    // ================================================================
    //  1. GESTIÓN DE MEMORIA (ADMISIÓN Y SWAPPING)
    // ================================================================
    public void admitirProceso(Proceso p) {
        if (obtenerRamEnUso() + p.tamano <= MAX_RAM_MB) {
            encolarSegunAlgoritmo(p);
        } else {
            // RAM LLENA: 1. Sacamos un bloqueado si hay (es lo mejor para liberar RAM)
            if (!colaBloqueados.esVacia()) {
                Proceso victima = colaBloqueados.desencolar();
                victima.estado = "Bloq-Suspendido";
                colaBloqueadoSuspendido.encolar(victima);
                encolarSegunAlgoritmo(p); // Ahora sí entra el nuevo a RAM
            } 
            // 2. Si no hay bloqueados, aplicamos Swapping a los Listos
            else {
                Proceso candidato = colaListos.obtenerMayorDeadline();
                if (candidato != null && candidato.deadline > p.deadline) {
                    colaListos.eliminarPorId(candidato.id);
                    candidato.estado = "Listo-Suspendido";
                    colaListoSuspendido.encolar(candidato);
                    encolarSegunAlgoritmo(p);
                } else {
                    p.estado = "Listo-Suspendido";
                    colaListoSuspendido.encolar(p);
                }
            }
        }
    }
    
    private void encolarSegunAlgoritmo(Proceso p) {
        p.estado = "Listo";
        switch (algoritmoActual) {
            case SRT:       colaListos.encolarPorSRT(p); break;
            case PRIORIDAD: colaListos.encolarPorPrioridad(p); break;
            case EDF:       colaListos.encolarPorEDF(p); break;
            default:        colaListos.encolar(p); break;
        }
    }

    // ================================================================
    //  2. CICLO DE EJECUCIÓN (CPU)
    // ================================================================
    public void ejecutarCiclo() {
        cicloReloj++; 
        gestionarBloqueados(); 

        if (cpu == null) {
            if (!colaListos.esVacia()) {
                cpu = colaListos.desencolar();
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
            } else {
                return; 
            }
        }
        
        cpu.ejecutarCiclo(); 
        contadorQuantum++; 

        // 🛑 Verificación de Bloqueo por E/S (Planificado)
        if (cpu.pc == cpu.cicloDetonadorES && cpu.duracionES > 0) {
            bloquearProcesoActual();
            return; 
        }

        // 🎲 NUEVO: Bloqueo Aleatorio Espontáneo (10% de probabilidad en cada ciclo)
        if (!cpu.nombre.startsWith("PROTOCOLO_") && Math.random() < 0.10) {
            cpu.duracionES = 10 + (int)(Math.random() * 10); // Tarda de 10 a 20 ciclos
            bloquearProcesoActual();
            return;
        }

        // Finalización
        if (cpu.pc >= cpu.instrucciones || cpu.tiempoRestante <= 0) {
            if (cpu.nombre.startsWith("PROTOCOLO_")) emergenciaActiva = false; 
            cpu.estado = "Terminado";
            cpu = null; 
            realizarSwapIn(); 
            return; 
        }

        // Round Robin
        if (algoritmoActual == ROUND_ROBIN && contadorQuantum >= quantum) {
            if (!cpu.nombre.startsWith("PROTOCOLO_")) {
                cpu.estado = "Listo";
                encolarSegunAlgoritmo(cpu);
                cpu = null;
            }
        }
    }

    private void bloquearProcesoActual() {
        cpu.estado = "Bloqueado";
        cpu.contadorES = 0; 
        colaBloqueados.encolar(cpu);
        cpu = null; 
    }

    public void generarInterrupcion(String tipoEmergencia) {
        if (emergenciaActiva) return;
        emergenciaActiva = true;

        if (tipoEmergencia.equals("Impacto Meteorito")) {
            if (!colaListos.esVacia()) {
                colaListos.desencolar(); 
                realizarSwapIn(); 
            }
        }

        String nombreEmergencia = "PROTOCOLO_" + tipoEmergencia.replace(" ", "_").toUpperCase();
        Proceso isr = new Proceso(999, nombreEmergencia, 7, 100, 1, 15, 99, 0);
        
        if (this.cpu != null) {
            this.cpu.estado = "Listo";
            this.colaListos.encolarAlInicio(this.cpu); 
        }
        
        this.cpu = isr;
        this.cpu.estado = "Ejecución";
        this.contadorQuantum = 0;
    }

    // ================================================================
    //  4. MÉTODOS AUXILIARES
    // ================================================================
    private void gestionarBloqueados() {
        // 1. Avanzar Bloqueados Normales (En RAM)
        Cola temp = new Cola();
        while (!colaBloqueados.esVacia()) {
            Proceso p = colaBloqueados.desencolar();
            p.contadorES++;
            if (p.contadorES >= p.duracionES) {
                encolarSegunAlgoritmo(p); // Vuelve a Listos
            } else {
                temp.encolar(p);
            }
        }
        colaBloqueados = temp; 

        // 2. NUEVO: Avanzar Bloqueados Suspendidos (En Disco)
        Cola tempSusp = new Cola();
        while (!colaBloqueadoSuspendido.esVacia()) {
            Proceso p = colaBloqueadoSuspendido.desencolar();
            p.contadorES++;
            if (p.contadorES >= p.duracionES) {
                // Terminó la E/S, pero como está en Disco, pasa a "Listo-Suspendido"
                p.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(p);
            } else {
                tempSusp.encolar(p);
            }
        }
        colaBloqueadoSuspendido = tempSusp;
    }

    public void realizarSwapIn() {
        boolean huboEspacio = true;
        while (huboEspacio && !colaListoSuspendido.esVacia()) {
            Proceso candidato = colaListoSuspendido.inicio.dato;
            if (obtenerRamEnUso() + candidato.tamano <= MAX_RAM_MB) {
                colaListoSuspendido.desencolar();
                encolarSegunAlgoritmo(candidato);
            } else {
                huboEspacio = false; 
            }
        }
    }
}