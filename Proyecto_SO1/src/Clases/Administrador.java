package Clases;

public class Administrador {
    
    // --- CONSTANTES PARA LOS ALGORITMOS 
    public static final int FCFS = 0; //First Come, First Served
    public static final int ROUND_ROBIN = 1; //cada proceso tiene su tiempo si no termina cambia
    public static final int SRT = 2; //El procesador siempre elige al proceso al que le falte menos tiempo para terminar.
    public static final int PRIORIDAD = 3; // Prioridad Estática Preemptiva
    public static final int EDF = 4; //Earliest Deadline First
    
    // Configuración actual del sistema
    public int algoritmoActual = ROUND_ROBIN; 
    
    // --- ESTRUCTURAS DE DATOS ---
    public Cola colaListos;
    public Cola colaBloqueados;
    public Proceso cpu; // Proceso que tiene el control actualmente
    
    // --- VARIABLES DE CONTROL ---
    private int quantum = 2;        // Duración del turno en RR
    private int contadorQuantum = 0;
    public static int cicloReloj = 0; // Tiempo global del sistema

    public Administrador() {
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.cpu = null;
    }
    
    /**
     * Lógica de Planificación con EXPROPIACIÓN (Preemption)
     * Decide si el proceso entra directo al CPU, si expulsa al actual o si va a la cola.
     */
    public void planificarProceso(Proceso p) {
        p.estado = "Listo"; 
        
        // 1. VERIFICAR GOLPE DE ESTADO (Preemption)
        // Si hay alguien en CPU y el nuevo es más importante, lo sacamos.
        if (cpu != null && (algoritmoActual == PRIORIDAD || algoritmoActual == EDF)) {
            
            // En el caso de PRIORIDAD: Mayor número gana (ej: 99 > 1)
            if (p.prioridad > cpu.prioridad) {
                System.out.println("\n   [!!!] INTERRUPCIÓN: " + p.nombre + " (Prio:" + p.prioridad 
                                 + ") expulsó a " + cpu.nombre + " (Prio:" + cpu.prioridad + ")");
                
                // El proceso expulsado vuelve a la cola de listos respetando su prioridad
                Proceso derrocado = cpu;
                derrocado.estado = "Listo";
                colaListos.encolarPorPrioridad(derrocado);
                
                // El nuevo toma el control inmediatamente
                cpu = p;
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
                return; // Finaliza la planificación porque ya entró al CPU
            }
        }

        // 2. ENCOLADO NORMAL (Si no hubo expulsión o el CPU está vacío)
        switch (algoritmoActual) {
            case FCFS:
            case ROUND_ROBIN:
                colaListos.encolar(p); 
                break;
                
            case PRIORIDAD:
                colaListos.encolarPorPrioridad(p);
                break;
                
            case SRT:
            case EDF:
                // Por ahora usamos encolar normal, se ajustará cuando definamos Deadlines
                colaListos.encolar(p); 
                break;
        }
    }

    /**
     * Se ejecuta en cada ciclo del Reloj. 
     * Controla la ejecución, el fin de procesos y el tiempo de Quantum.
     */
    public void ejecutarCiclo() {
        cicloReloj++; 
        
        // --- FASE 1: ASIGNACIÓN DE CPU ---
        if (cpu == null) {
            if (!colaListos.esVacia()) {
                cpu = colaListos.desencolar();
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
                System.out.println("\n[Ciclo " + cicloReloj + "] NUEVO PROCESO EN CPU: " + cpu.nombre);
            } else {
                System.out.println("[Ciclo " + cicloReloj + "] Sistema en espera (IDLE)...");
                return; 
            }
        }

        // --- FASE 2: TRABAJO DEL CPU ---
        cpu.pc++;        // El proceso avanza una instrucción
        contadorQuantum++; 

        System.out.println("[Ciclo " + cicloReloj + "] Ejecutando: " + cpu.nombre 
                         + " (" + cpu.pc + "/" + cpu.instrucciones + ")");

        // --- FASE 3: ¿TERMINÓ EL PROCESO? ---
        if (cpu.pc >= cpu.instrucciones) {
            System.out.println("   >>> OK: " + cpu.nombre + " ha finalizado.");
            cpu.estado = "Terminado";
            cpu = null; 
            return; 
        }

        // --- FASE 4: ¿FIN DE QUANTUM? (Solo para Round Robin) ---
        if (algoritmoActual == ROUND_ROBIN) {
            if (contadorQuantum >= quantum) {
                System.out.println("   [!] Tiempo agotado (Quantum). Reubicando: " + cpu.nombre);
                planificarProceso(cpu); // Vuelve a la cola
                cpu = null; // Se libera el CPU para el siguiente
            }
        }
    }
}