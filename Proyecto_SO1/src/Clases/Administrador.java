package Clases;

public class Administrador {
    
    // --- CONSTANTES DE ALGORITMOS ---
    public static final int FCFS = 0;
    public static final int ROUND_ROBIN = 1;
    public static final int SRT = 2;
    public static final int PRIORIDAD = 3;
    public static final int EDF = 4;
    
    // --- CONFIGURACIÓN ACTUAL ---
    public int algoritmoActual = ROUND_ROBIN; 
    private int quantum = 2; 
    private int contadorQuantum = 0;
    
    // --- COLAS DE ESTADOS ---
    public Cola colaListos = new Cola();
    public Cola colaBloqueados = new Cola();        
    public Cola colaListoSuspendido = new Cola();   
    public Cola colaBloqueadoSuspendido = new Cola(); 
    
    // --- RECURSOS ---
    public Proceso cpu = null;
    private final int MAX_RAM = 5;       
    public int procesosEnRAM = 0;
    public static int cicloReloj = 0;
    
    // --- CONTROL DE EMERGENCIAS ---
    private boolean emergenciaActiva = false;

    public Administrador() {}

    // ================================================================
    //  1. GESTIÓN DE MEMORIA (ADMISIÓN Y SWAPPING)
    // ================================================================
    public void admitirProceso(Proceso p) {
        if (procesosEnRAM < MAX_RAM) {
            encolarSegunAlgoritmo(p);
            procesosEnRAM++;
            System.out.println(" ✅ RAM [" + procesosEnRAM + "/" + MAX_RAM + "]: Ingresa '" + p.nombre + "'");
        } else {
            // RAM llena: Aplicar Swapping por Deadline
            Proceso candidato = colaListos.obtenerMayorDeadline();
            
            if (candidato != null && candidato.deadline > p.deadline) {
                // El nuevo es más urgente, sacamos al que tiene mayor deadline
                colaListos.eliminarPorId(candidato.id);
                candidato.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(candidato);
                System.out.println(" ⚠️ SWAP OUT: Sale '" + candidato.nombre + "' -> Disco.");

                encolarSegunAlgoritmo(p);
                System.out.println(" 📥 SWAP IN: Entra '" + p.nombre + "' a RAM.");
            } else {
                // El nuevo no es urgente, va directo al disco
                p.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(p);
                System.out.println(" ⛔ RAM LLENA: '" + p.nombre + "' va directo a Disco.");
            }
        }
    }
    
    private void encolarSegunAlgoritmo(Proceso p) {
        p.estado = "Listo";
        switch (algoritmoActual) {
            case SRT:       colaListos.encolarPorSRT(p); break;
            case PRIORIDAD: colaListos.encolarPorPrioridad(p); break;
            case EDF:       colaListos.encolarPorEDF(p); break;
            default:        colaListos.encolar(p); break; // FCFS y RR
        }
    }

    // ================================================================
    //  2. CICLO DE EJECUCIÓN (CPU)
    // ================================================================
    public void ejecutarCiclo() {
        cicloReloj++; 
        gestionarBloqueados(); 

        // Si el CPU está libre, tomamos un proceso de la cola de listos
        if (cpu == null) {
            if (!colaListos.esVacia()) {
                cpu = colaListos.desencolar();
                cpu.estado = "Ejecución";
                contadorQuantum = 0; 
            } else {
                return; // CPU Ocioso
            }
        }
        
        // Ejecutamos un ciclo de instrucción
        cpu.ejecutarCiclo(); 
        contadorQuantum++; 

        // Verificación de Bloqueo por E/S
        if (cpu.pc == cpu.cicloDetonadorES && cpu.duracionES > 0) {
            cpu.estado = "Bloqueado";
            cpu.contadorES = 0; 
            colaBloqueados.encolar(cpu);
            cpu = null; 
            return; 
        }

        // Verificación de Finalización del proceso
        if (cpu.pc >= cpu.instrucciones) {
            if (cpu.nombre.startsWith("PROTOCOLO_")) {
                emergenciaActiva = false; // Apagamos la alarma al terminar la ISR
            }
            cpu.estado = "Terminado";
            cpu = null; 
            procesosEnRAM--; 
            realizarSwapIn(); // Traemos alguien del disco si hay espacio
            return; 
        }

        // Quantum para Round Robin (No aplica a la ISR de emergencia)
        if (algoritmoActual == ROUND_ROBIN && contadorQuantum >= quantum) {
            if (!cpu.nombre.startsWith("PROTOCOLO_")) {
                cpu.estado = "Listo";
                encolarSegunAlgoritmo(cpu);
                cpu = null;
            }
        }
    }

    // ================================================================
    //  3. MANEJO DE INTERRUPCIONES (EMERGENCIAS)
    // ================================================================
    public void generarInterrupcion(String tipoEmergencia) {
        // Evitar que se solapen múltiples emergencias
        if (emergenciaActiva) return;

        emergenciaActiva = true;
        System.out.println("\n🚨 [ALERTA DE SISTEMA]: " + tipoEmergencia.toUpperCase());

        // Lógica de Meteorito: Elimina un proceso de Listos si existe
        if (tipoEmergencia.equals("Impacto Meteorito")) {
            if (!colaListos.esVacia()) {
                Proceso victima = colaListos.desencolar(); 
                System.out.println("☄️ Proceso '" + victima.nombre + "' DESTRUIDO por meteorito.");
                procesosEnRAM--; 
                realizarSwapIn(); 
            }
        }

        // Transformamos el nombre (Ej: "Falla de Oxigeno" -> "PROTOCOLO_FALLA_DE_OXIGENO")
        String nombreEmergencia = "PROTOCOLO_" + tipoEmergencia.replace(" ", "_").toUpperCase();

        // Creamos el proceso con ese nuevo nombre
        Proceso isr = new Proceso(999, nombreEmergencia, 7, 100, 1, 0, 99, 0);
        
        // Expropiación de CPU: Sacamos al proceso actual y lo guardamos al inicio
        if (this.cpu != null) {
            this.cpu.estado = "Listo";
            this.colaListos.encolarAlInicio(this.cpu); 
        }
        
        // Asignamos el CPU de inmediato a la emergencia
        this.cpu = isr;
        this.cpu.estado = "Ejecución";
        this.contadorQuantum = 0;
    }

    // ================================================================
    //  4. MÉTODOS AUXILIARES
    // ================================================================
    private void gestionarBloqueados() {
        // Revisar y avanzar el tiempo de los procesos Bloqueados en RAM
        Cola temp = new Cola();
        while (!colaBloqueados.esVacia()) {
            Proceso p = colaBloqueados.desencolar();
            p.contadorES++;
            if (p.contadorES >= p.duracionES) {
                // Terminó su E/S, vuelve a listos
                encolarSegunAlgoritmo(p);
            } else {
                temp.encolar(p);
            }
        }
        colaBloqueados = temp;
    }

    public void realizarSwapIn() {
        // Traer un proceso del Disco a la RAM si hay espacio
        if (!colaListoSuspendido.esVacia() && procesosEnRAM < MAX_RAM) {
            Proceso p = colaListoSuspendido.desencolar();
            p.estado = "Listo";
            encolarSegunAlgoritmo(p);
            procesosEnRAM++;
        }
    }
}