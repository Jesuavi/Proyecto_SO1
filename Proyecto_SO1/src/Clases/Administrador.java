package Clases;

public class Administrador {
    
    // Algoritmos
    public static final int FCFS = 0;
    public static final int ROUND_ROBIN = 1;
    public static final int SRT = 2;
    public static final int PRIORIDAD = 3;
    public static final int EDF = 4;
    
    // Configuración
    public int algoritmoActual = ROUND_ROBIN; 
    private int quantum = 2; 
    private int contadorQuantum = 0;
    
    // Colas y Recursos
    public Cola colaListos = new Cola();
    public Cola colaBloqueados = new Cola();
    public Cola colaListoSuspendido = new Cola();
    public Cola colaBloqueadoSuspendido = new Cola();
    
    public Proceso cpu = null;
    private final int MAX_RAM = 5;       
    public int procesosEnRAM = 0;
    public static int cicloReloj = 0;
    
    // Control de Emergencias
    private boolean emergenciaActiva = false;

    public Administrador() {}

    // GESTIÓN DE MEMORIA
    public void admitirProceso(Proceso p) {
        if (procesosEnRAM < MAX_RAM) {
            encolarSegunAlgoritmo(p);
            procesosEnRAM++;
            System.out.println(" ✅ RAM [" + procesosEnRAM + "/" + MAX_RAM + "]: Ingresa '" + p.nombre + "'");
        } else {
            // Swap logic
            Proceso candidato = colaListos.obtenerMayorDeadline();
            if (candidato != null && candidato.deadline > p.deadline) {
                colaListos.eliminarPorId(candidato.id);
                candidato.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(candidato);
                System.out.println(" ⚠️ SWAP OUT: Sale '" + candidato.nombre + "' -> Disco.");

                encolarSegunAlgoritmo(p);
                System.out.println(" 📥 SWAP IN: Entra '" + p.nombre + "' a RAM.");
            } else {
                p.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(p);
                System.out.println(" ⛔ RAM LLENA: '" + p.nombre + "' va directo a Disco.");
            }
        }
    }
    
    // Este método ya no dará error porque actualizamos Cola.java
    private void encolarSegunAlgoritmo(Proceso p) {
        p.estado = "Listo";
        switch (algoritmoActual) {
            case SRT:       colaListos.encolarPorSRT(p); break;
            case PRIORIDAD: colaListos.encolarPorPrioridad(p); break;
            case EDF:       colaListos.encolarPorEDF(p); break;
            default:        colaListos.encolar(p); break; // FCFS y RR
        }
    }

    // CICLO PRINCIPAL
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

        int faltan = cpu.instrucciones - cpu.pc;
        System.out.println("⏱️ Ciclo " + cicloReloj + " | CPU: " + cpu.nombre + 
                           " (PC: " + cpu.pc + "/" + cpu.instrucciones + ") | Faltan: " + faltan);

        // Bloqueo E/S
        if (cpu.pc == cpu.cicloDetonadorES && cpu.duracionES > 0) {
            System.out.println(" 🛑 BLOQUEO E/S: '" + cpu.nombre + "' espera " + cpu.duracionES + " ciclos.");
            cpu.estado = "Bloqueado";
            cpu.contadorES = 0; 
            colaBloqueados.encolar(cpu);
            cpu = null; 
            return; 
        }

        // Terminación
        if (cpu.pc >= cpu.instrucciones) {
            System.out.println("   🎉 FIN: " + cpu.nombre + " terminó.");
            if (cpu.nombre.startsWith("PROTOCOLO_")) {
                emergenciaActiva = false;
            }
            cpu.estado = "Terminado";
            cpu = null; 
            procesosEnRAM--; 
            realizarSwapIn(); 
            return; 
        }

        // Round Robin
        if (algoritmoActual == ROUND_ROBIN && contadorQuantum >= quantum) {
            if (!cpu.nombre.startsWith("PROTOCOLO_")) {
                System.out.println("   🔄 Quantum RR: Sale " + cpu.nombre);
                cpu.estado = "Listo";
                encolarSegunAlgoritmo(cpu);
                cpu = null;
            }
        }
    }

    // INTERRUPCIONES
    public void generarInterrupcion(String tipoEmergencia) {
        if (emergenciaActiva) return;

        emergenciaActiva = true;
        System.out.println("\n🚨 [ALERTA DE SISTEMA]: " + tipoEmergencia.toUpperCase());

        if (tipoEmergencia.equals("Impacto Meteorito")) {
            if (!colaListos.esVacia()) {
                Proceso victima = colaListos.desencolar(); 
                System.out.println("☄️ ¡COLISIÓN! El proceso '" + victima.nombre + "' fue DESTRUIDO.");
                procesosEnRAM--; 
                realizarSwapIn(); 
            }
        }

        Proceso isr = new Proceso(999, "PROTOCOLO_REPARACION", 3, 100, 1, 0, 99, 0);
        
        if (this.cpu != null) {
            System.out.println("⚠️ Pausando '" + cpu.nombre + "' para emergencia.");
            this.cpu.estado = "Listo";
            this.colaListos.encolarAlInicio(this.cpu); 
        }
        
        this.cpu = isr;
        this.cpu.estado = "Ejecución";
        this.contadorQuantum = 0;
        System.out.println("🛠️ Ejecutando protocolo de reparación...");
    }

    private void gestionarBloqueados() {
        Cola colaTemp = new Cola();
        while (!colaBloqueados.esVacia()) {
            Proceso p = colaBloqueados.desencolar();
            p.contadorES++;
            if (p.contadorES >= p.duracionES) {
                p.contadorES = 0; 
                encolarSegunAlgoritmo(p);
                System.out.println(" ⚡ FIN E/S (RAM): '" + p.nombre + "' vuelve a Listos.");
            } else {
                colaTemp.encolar(p);
            }
        }
        colaBloqueados = colaTemp;
        
        // Gestión simple de suspendidos bloqueados
        Cola colaTempSusp = new Cola();
         while (!colaBloqueadoSuspendido.esVacia()) {
            Proceso p = colaBloqueadoSuspendido.desencolar();
            p.contadorES++;
            if (p.contadorES >= p.duracionES) {
                p.contadorES = 0;
                p.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(p);
                realizarSwapIn(); 
            } else {
                colaTempSusp.encolar(p);
            }
        }
        colaBloqueadoSuspendido = colaTempSusp;
    }

    public void realizarSwapIn() {
        if (!colaListoSuspendido.esVacia() && procesosEnRAM < MAX_RAM) {
            Proceso p = colaListoSuspendido.desencolar();
            admitirProceso(p); 
        }
    }
}