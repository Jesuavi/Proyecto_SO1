package Clases;

public class Administrador {
    
    // --- CONSTANTES DE ALGORITMOS (Para el Switch) ---
    public static final int FCFS = 0;
    public static final int ROUND_ROBIN = 1;
    public static final int SRT = 2;
    public static final int PRIORIDAD = 3;
    public static final int EDF = 4;
    
    // --- CONFIGURACIÓN ACTUAL ---
    // Cambia este valor manual o dinámicamente para probar otros modos
    public int algoritmoActual = ROUND_ROBIN; 
    
    // --- ESTRUCTURAS DE DATOS ---
    public Cola colaListos;
    public Cola colaBloqueados;
    public Proceso cpu; 
    
    // --- VARIABLES ROUND ROBIN ---
    private int quantum;        
    private int contadorQuantum;
    
    // --- RELOJ GLOBAL ---
    public static int cicloReloj = 0;

    public Administrador() {
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.cpu = null;
        this.quantum = 2; // Configurable: 2 ciclos por turno
        this.contadorQuantum = 0;
    }
    
    // MÉTODO CENTRAL: Decide cómo formar procesos según el algoritmo activo
    public void planificarProceso(Proceso p) {
        p.estado = "Listo"; // Siempre que vuelve a la cola, está "Listo"
        
        switch (algoritmoActual) {
            case FCFS:
            case ROUND_ROBIN:
                // En estos, la democracia manda: A la cola, atrás de todos.
                colaListos.encolar(p); 
                break;
                
            case PRIORIDAD:
                // Aquí manda la jerarquía: Usamos tu nuevo método VIP
                colaListos.encolarPorPrioridad(p);
                break;
                
            case SRT:
            case EDF:
                // (Futuro) Aquí implementaremos la lógica de tiempo restante/deadline
                colaListos.encolar(p); 
                break;
        }
    }

    // Esta función se ejecuta en cada "Tic-Tac" del Reloj
    public void ejecutarCiclo() {
        cicloReloj++; 
        
        // ---------------------------------------------------------
        // FASE 1: PLANIFICADOR (Meter procesos al CPU si está vacío)
        // ---------------------------------------------------------
        if (cpu == null) {
            if (!colaListos.esVacia()) {
                // Sacamos al siguiente (sea quien sea que esté primero)
                cpu = colaListos.desencolar();
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
                System.out.println("[Reloj " + cicloReloj + "] Entra a CPU: " + cpu.nombre);
            } else {
                System.out.println("[Reloj " + cicloReloj + "] CPU Ocioso...");
                return; 
            }
        }

        // ---------------------------------------------------------
        // FASE 2: EJECUCIÓN
        // ---------------------------------------------------------
        cpu.pc++;        
        contadorQuantum++; 

        System.out.println("   -> Procesando: " + cpu.nombre + " (PC: " + cpu.pc + "/" + cpu.instrucciones + ")");

        // ---------------------------------------------------------
        // FASE 3: VERIFICAR SI TERMINÓ
        // ---------------------------------------------------------
        if (cpu.pc >= cpu.instrucciones) {
            cpu.estado = "Terminado";
            System.out.println("   >>> ¡TERMINADO! " + cpu.nombre + " salió del sistema.");
            cpu = null; 
            return; 
        }

        // ---------------------------------------------------------
        // FASE 4: ALGORITMO ROUND ROBIN (Expulsión por Tiempo)
        // ---------------------------------------------------------
        // Solo aplicamos esto si estamos en modo ROUND_ROBIN
        if (algoritmoActual == ROUND_ROBIN) {
            if (contadorQuantum >= quantum) {
                System.out.println("   [!] Fin de Quantum: " + cpu.nombre + " vuelve a la cola.");
                
                // Usamos el planificador para devolverlo a la cola
                planificarProceso(cpu);
                
                cpu = null; // Liberamos CPU
            }
        }
    }
}