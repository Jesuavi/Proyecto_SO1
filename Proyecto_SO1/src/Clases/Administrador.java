package Clases;

public class Administrador {
    
    // Algoritmos (Punto 7) - Dejamos esto listo
    public static final int ROUND_ROBIN = 1;
    public int algoritmoActual = ROUND_ROBIN; 
    
    // --- COLAS DE ESTADOS ---
    public Cola colaListos = new Cola();
    public Cola colaBloqueados = new Cola(); // Estado: Bloqueado (RAM)
    public Proceso cpu = null; 
    
    // --- MEMORIA SECUNDARIA (SWAP) ---
    public Cola colaListoSuspendido = new Cola();      
    public Cola colaBloqueadoSuspendido = new Cola();  // Estado: Bloqueado-Suspendido
    
    private final int MAX_RAM = 5;       
    public int procesosEnRAM = 0;        
    
    // --- CONTROL ---
    private int quantum = 2;             
    private int contadorQuantum = 0;
    public static int cicloReloj = 0;    

    public Administrador() {}
    
    // ================================================================
    //  1. GESTIÓN DE MEMORIA (Del Punto 2)
    // ================================================================
    public void admitirProceso(Proceso p) {
        if (procesosEnRAM < MAX_RAM) {
            p.estado = "Listo";
            colaListos.encolar(p);
            procesosEnRAM++;
            System.out.println(" ✅ RAM [" + procesosEnRAM + "/" + MAX_RAM + "]: Ingresa '" + p.nombre + "'");
        } else {
            // Lógica de Swap: Buscar al de mayor Deadline (menos urgente)
            Proceso candidato = colaListos.obtenerMayorDeadline();
            
            // Si el nuevo es más urgente (menor deadline) que el candidato...
            if (candidato != null && candidato.deadline > p.deadline) {
                // SACAMOS AL VIEJO
                colaListos.eliminarPorId(candidato.id);
                candidato.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(candidato);
                System.out.println(" ⚠️ SWAP OUT: Sale '" + candidato.nombre + "' (Deadl: " + candidato.deadline + ") -> Disco.");

                // METEMOS AL NUEVO
                p.estado = "Listo";
                colaListos.encolar(p);
                System.out.println(" 📥 SWAP IN: Entra '" + p.nombre + "' (Deadl: " + p.deadline + ") a RAM.");
            } else {
                p.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(p);
                System.out.println(" ⛔ RAM LLENA: '" + p.nombre + "' va directo a Disco.");
            }
        }
    }

    // ================================================================
    //  2. CICLO PRINCIPAL (CON E/S DEL PUNTO 3)
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

    // 1. PRIMERO EJECUTAMOS LA INSTRUCCIÓN
    cpu.ejecutarCiclo(); 
    contadorQuantum++; 

    System.out.println("⏱️ Ciclo " + cicloReloj + " | CPU: " + cpu.nombre + " (PC: " + cpu.pc + ")");

    // 2. DESPUÉS CHEQUEAMOS SI ESA INSTRUCCIÓN DETONÓ UNA E/S
    // Al hacerlo después de ejecutarCiclo(), el PC ya subió, 
    // por lo que la próxima vez que vuelva al CPU empezará en PC+1.
    if (cpu.pc == cpu.cicloDetonadorES && cpu.duracionES > 0) {
        System.out.println(" 🛑 BLOQUEO E/S: '" + cpu.nombre + "' inicia espera de " + cpu.duracionES + " ciclos.");
        cpu.estado = "Bloqueado";
        cpu.contadorES = 0; 
        colaBloqueados.encolar(cpu);
        cpu = null; 
        return; 
    }

    // 3. Verificar Terminación
    if (cpu.pc >= cpu.instrucciones) {
        System.out.println("   🎉 FIN: " + cpu.nombre + " terminó.");
        cpu.estado = "Terminado";
        cpu = null; 
        procesosEnRAM--; 
        realizarSwapIn(); 
        return; 
    }

    // 4. Round Robin
    if (algoritmoActual == ROUND_ROBIN && contadorQuantum >= quantum) {
        System.out.println("   🔄 Quantum RR: Sale " + cpu.nombre);
        cpu.estado = "Listo";
        colaListos.encolar(cpu);
        cpu = null;
    }
}

    // ================================================================
    //  3. GESTIÓN DE COLAS BLOQUEADAS (PUNTO 3)
    // ================================================================
    private void gestionarBloqueados() {
        // --- A) BLOQUEADOS EN RAM ---
        // Usamos una cola temporal para reconstruir la lista (evita errores de índice)
        Cola colaTemp = new Cola();
        
        while (!colaBloqueados.esVacia()) {
            Proceso p = colaBloqueados.desencolar();
            p.contadorES++; // Un ciclo más esperando...
            
            if (p.contadorES >= p.duracionES) {
                // ¡Terminó la espera! Vuelve a Listo
                p.contadorES = 0; 
                p.estado = "Listo";
                colaListos.encolar(p);
                System.out.println(" ⚡ FIN E/S (RAM): '" + p.nombre + "' vuelve a Listos.");
            } else {
                // Sigue esperando
                colaTemp.encolar(p);
            }
        }
        colaBloqueados = colaTemp; // Restauramos la cola

        // --- B) BLOQUEADOS EN DISCO (Suspendidos) ---
        // ¡El disco también avanza el tiempo!
        Cola colaTempSusp = new Cola();
        while (!colaBloqueadoSuspendido.esVacia()) {
            Proceso p = colaBloqueadoSuspendido.desencolar();
            p.contadorES++;
            
            if (p.contadorES >= p.duracionES) {
                // Terminó E/S, pasa a Listo-Suspendido (esperando RAM)
                p.contadorES = 0;
                p.estado = "Listo-Suspendido";
                colaListoSuspendido.encolar(p);
                System.out.println(" ⚡ FIN E/S (DISCO): '" + p.nombre + "' pasa a cola de entrada (Swap In pendiente).");
                realizarSwapIn(); 
            } else {
                colaTempSusp.encolar(p);
            }
        }
        colaBloqueadoSuspendido = colaTempSusp;
    }

    // ================================================================
    //  4. SWAP IN (RECUPERAR DEL DISCO)
    // ================================================================
    public void realizarSwapIn() {
        if (!colaListoSuspendido.esVacia() && procesosEnRAM < MAX_RAM) {
            Proceso p = colaListoSuspendido.desencolar();
            admitirProceso(p); // Reutilizamos admitir para gestionar la entrada
        }
    }
}