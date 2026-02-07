package Clases;

public class Administrador {
    
    // --- CONSTANTES ---
    public static final int FCFS = 0;
    public static final int ROUND_ROBIN = 1;
    public static final int SRT = 2;
    public static final int PRIORIDAD = 3;
    public static final int EDF = 4;
    
    public int algoritmoActual = ROUND_ROBIN; 
    
    // --- ESTRUCTURAS ---
    public Cola colaListos;
    public Cola colaBloqueados;
    public Proceso cpu; 
    
    // --- MEMORIA (7 ESTADOS) ---
    public Cola colaListoSuspendido = new Cola();      
    public Cola colaBloqueadoSuspendido = new Cola();  
    
    private final int MAX_RAM = 5;       
    public int procesosEnRAM = 0;        
    
    // --- CONTROL ---
    private int quantum = 2;             
    private int contadorQuantum = 0;
    public static int cicloReloj = 0;    

    public Administrador() {
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.cpu = null;
    }
    
    // ---------------------------------------------------------
    //  MÉTODO 1: ADMISIÓN 
    // ---------------------------------------------------------
    // MÉTODOS DE MEMORIA (Versión Inteligente con Prioridad de Deadline)
    public void admitirProceso(Proceso p) {
        if (procesosEnRAM < MAX_RAM) {
            // A) Hay espacio: Entra directo
            p.estado = "Listo";
            insertarEnColaSegunAlgoritmo(p);
            procesosEnRAM++;
            System.out.println(" ✅ RAM [" + procesosEnRAM + "/" + MAX_RAM + "]: Ingresa '" + p.nombre + "'");
        } else {
            // B) No hay espacio: Verificamos si vale la pena hacer un SWAP OUT
            // Buscamos en la RAM al proceso MENOS urgente (mayor deadline)
            Proceso candidatoSacar = colaListos.obtenerMayorDeadline();
            
            // Si encontramos a alguien y ese alguien es MENOS urgente que el proceso nuevo...
            if (candidatoSacar != null && candidatoSacar.deadline > p.deadline) {
                // 1. Sacamos al viejo (Swap Out)
                colaListos.eliminarPorId(candidatoSacar.id);
                candidatoSacar.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(candidatoSacar);
                System.out.println(" ⚠️ SWAP OUT: Se expulsa a '" + candidatoSacar.nombre + "' (Deadline lejana) para meter a '" + p.nombre + "'");
                
                // 2. Metemos al nuevo (Swap In inmediato)
                p.estado = "Listo";
                insertarEnColaSegunAlgoritmo(p);
                // La cantidad de procesos en RAM se mantiene igual (sale 1, entra 1)
                
            } else {
                // C) El nuevo no es tan urgente, se va a la cola de espera
                p.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(p);
                System.out.println(" ⛔ RAM LLENA: '" + p.nombre + "' enviado al DISCO (Deadline no suficiente).");
            }
        }
    }

    // ---------------------------------------------------------
    //  MÉTODO 2: PLANIFICACIÓN (SIN CAMBIOS, SOLO LÓGICA)
    // ---------------------------------------------------------
    public void planificarProceso(Proceso p) {
        p.estado = "Listo"; 
        
        if (cpu != null) {
            boolean debeExpulsar = false;
            if (algoritmoActual == PRIORIDAD && p.prioridad > cpu.prioridad) debeExpulsar = true;
            if (algoritmoActual == SRT && p.tiempoRestante < (cpu.instrucciones - cpu.pc)) debeExpulsar = true;
            if (algoritmoActual == EDF && p.deadline < cpu.deadline) debeExpulsar = true;

            if (debeExpulsar) {
                System.out.println(" ⚠️ EXPROPIACIÓN: '" + p.nombre + "' saca a '" + cpu.nombre + "'");
                Proceso derrocado = cpu;
                derrocado.estado = "Listo";
                this.insertarEnColaSegunAlgoritmo(derrocado);
                cpu = p;
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
                return;
            }
        }
        insertarEnColaSegunAlgoritmo(p);
    }

    private void insertarEnColaSegunAlgoritmo(Proceso p) {
        switch (algoritmoActual) {
            case FCFS:
            case ROUND_ROBIN: colaListos.encolar(p); break;
            case PRIORIDAD: colaListos.encolarPorPrioridad(p); break;
            case SRT: colaListos.encolarPorTiempoRestante(p); break;
            case EDF: colaListos.encolarPorDeadline(p); break;
        }
    }

    // ---------------------------------------------------------
    //  MÉTODO 3: EJECUTAR CICLO (VISUALMENTE MEJORADO)
    // ---------------------------------------------------------
    public void ejecutarCiclo() {
        cicloReloj++; 
        
        // Fase 1: Asignar CPU
        if (cpu == null) {
            if (!colaListos.esVacia()) {
                cpu = colaListos.desencolar();
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
            } else {
                // CPU Ocioso, no imprimimos nada para no ensuciar
                return; 
            }
        }

        // Fase 2: Ejecución
        cpu.ejecutarCiclo(); 
        contadorQuantum++; 

        // --- VISUALIZACIÓN DE BARRA DE PROGRESO ---
        String barra = "[";
        int completado = (cpu.pc * 10) / cpu.instrucciones; // Escala de 10
        for (int i = 0; i < 10; i++) {
            if (i < completado) barra += "#"; else barra += "-";
        }
        barra += "]";

        System.out.println("⏱️ Ciclo " + cicloReloj + " | Ejecutando: " + cpu.nombre + " " + barra + " (" + cpu.pc + "/" + cpu.instrucciones + ")");

        // Fase 3: Verificar Terminación
        if (cpu.pc >= cpu.instrucciones) {
            System.out.println("   🎉 FIN PROCESO: " + cpu.nombre + " terminó y libera RAM.");
            cpu.estado = "Terminado";
            cpu = null; 
            
            procesosEnRAM--; 
            realizarSwapIn(); 
            return; 
        }

        // Fase 4: Round Robin
        if (algoritmoActual == ROUND_ROBIN && contadorQuantum >= quantum) {
            System.out.println("   🔄 Cambio de Turno (Quantum): Sale " + cpu.nombre);
            planificarProceso(cpu);
            cpu = null;
        }
    }

    // ---------------------------------------------------------
    //  MÉTODO 4: SWAP IN (VISUALMENTE MEJORADO)
    // ---------------------------------------------------------
    public void realizarSwapIn() {
        if (!colaListoSuspendido.esVacia() && procesosEnRAM < MAX_RAM) {
            Proceso p = colaListoSuspendido.desencolar();
            p.estado = "Listo";
            insertarEnColaSegunAlgoritmo(p);
            procesosEnRAM++;
            System.out.println("   📥 SWAP IN: '" + p.nombre + "' sube del DISCO a la RAM.");
        }
    }
}