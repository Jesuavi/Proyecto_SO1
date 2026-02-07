package Clases;

public class Administrador {
    
    // --- CONSTANTES DE ALGORITMOS (Requeridos por el PDF) ---
    public static final int FCFS = 0;         // First-Come, First-Served [cite: 23]
    public static final int ROUND_ROBIN = 1;  // Round Robin [cite: 23]
    public static final int SRT = 2;          // Shortest Remaining Time [cite: 23]
    public static final int PRIORIDAD = 3;    // Prioridad Estática Preemptiva [cite: 23]
    public static final int EDF = 4;          // Earliest Deadline First [cite: 23]
    
    // Configuración actual del simulador
    public int algoritmoActual = ROUND_ROBIN; 
    
    // --- ESTRUCTURAS DE DATOS (Propias, sin ArrayList) ---
    public Cola colaListos;
    public Cola colaBloqueados;
    public Proceso cpu; // Proceso actualmente en ejecución
    
    // --- VARIABLES DE CONTROL ---
    private int quantum = 2;             // Tiempo asignado en Round Robin [cite: 23]
    private int contadorQuantum = 0;
    public static int cicloReloj = 0;    // Contador global del sistema [cite: 63]

    public Administrador() {
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.cpu = null;
    }
    
    /**
     * Lógica de Planificación con EXPROPIACIÓN (Preemption)
     * Decide si un proceso entra al CPU, expulsa al actual o espera en la cola[cite: 20].
     */
    public void planificarProceso(Proceso p) {
        p.estado = "Listo"; 
        
        // --- 1. VERIFICAR EXPROPIACIÓN (GOLPE DE ESTADO) ---
        if (cpu != null) {
            boolean debeExpulsar = false;
            
            // Caso Prioridad: Mayor número de prioridad gana
            if (algoritmoActual == PRIORIDAD && p.prioridad > cpu.prioridad) debeExpulsar = true;
            
            // Caso SRT: El nuevo es más corto que lo que le falta al actual
            if (algoritmoActual == SRT && p.tiempoRestante < (cpu.instrucciones - cpu.pc)) debeExpulsar = true;
            
            // Caso EDF: El nuevo tiene un deadline más cercano 
            if (algoritmoActual == EDF && p.deadline < cpu.deadline) debeExpulsar = true;

            if (debeExpulsar) {
                System.out.println("\n   [!!!] EXPROPIACIÓN: " + p.nombre + " expulsa a " + cpu.nombre);
                
                // El proceso expulsado vuelve a la cola según el algoritmo activo
                Proceso derrocado = cpu;
                derrocado.estado = "Listo";
                this.insertarEnColaSegunAlgoritmo(derrocado);
                
                // El nuevo toma el control inmediatamente
                cpu = p;
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
                return;
            }
        }

        // --- 2. ENCOLADO NORMAL  ---
        insertarEnColaSegunAlgoritmo(p);
    }

    /**
     * Método auxiliar para insertar procesos en la cola de listos
     * aplicando los criterios de ordenamiento de cada algoritmo[cite: 35].
     */
    private void insertarEnColaSegunAlgoritmo(Proceso p) {
        switch (algoritmoActual) {
            case FCFS:
            case ROUND_ROBIN:
                colaListos.encolar(p); // FIFO [cite: 23]
                break;
            case PRIORIDAD:
                colaListos.encolarPorPrioridad(p);
                break;
            case SRT:
                colaListos.encolarPorTiempoRestante(p);
                break;
            case EDF:
                colaListos.encolarPorDeadline(p);
                break;
        }
    }

    /**
     * Se ejecuta en cada "tic" del Reloj. 
     * Controla la ejecución, los registros y el fin de procesos.
     */
    public void ejecutarCiclo() {
        cicloReloj++; 
        
        // Fase 1: Asignar proceso al CPU si está vacío
        if (cpu == null) {
            if (!colaListos.esVacia()) {
                cpu = colaListos.desencolar();
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
                System.out.println("\n[Ciclo " + cicloReloj + "] NUEVO EN CPU: " + cpu.nombre);
            } else {
                return; 
            }
        }

        // Fase 2: Ejecución e incremento de registros (PC y MAR) 
        cpu.ejecutarCiclo(); 
        contadorQuantum++; 

        System.out.println("[Ciclo " + cicloReloj + "] Ejecutando: " + cpu.nombre 
                         + " | PC: " + cpu.pc + " | MAR: " + cpu.mar);

        // Fase 3: Verificar si el proceso terminó
        if (cpu.pc >= cpu.instrucciones) {
            System.out.println("   >>> PROCESO TERMINADO: " + cpu.nombre);
            cpu.estado = "Terminado";
            cpu = null; 
            return; 
        }

        // Fase 4: Fin de Quantum (Solo para Round Robin) 
        if (algoritmoActual == ROUND_ROBIN && contadorQuantum >= quantum) {
            System.out.println("   [!] Fin de Quantum para: " + cpu.nombre);
            planificarProceso(cpu);
            cpu = null;
        }
    }
}