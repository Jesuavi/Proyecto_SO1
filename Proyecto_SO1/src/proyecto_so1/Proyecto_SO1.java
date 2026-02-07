package proyecto_so1; // Asegúrate de que esto coincida con tu paquete

import Clases.Administrador;
import Clases.Proceso;
import Clases.Reloj;

public class Proyecto_SO1 {

    public static void main(String[] args) {
        System.out.println("=== 🛰️ INICIO DE SIMULACIÓN (PRUEBA DE PRIORIDADES) 🛰️ ===");
        
        // 1. Instanciamos el Sistema Operativo
        Administrador admin = new Administrador();
        
        // ----------------------------------------------------
        // ⚙️ CONFIGURACIÓN DEL ALGORITMO
        // Aquí decidimos qué lógica usar. 
        // Opciones: Administrador.ROUND_ROBIN, Administrador.PRIORIDAD, etc.
        // ----------------------------------------------------
        admin.algoritmoActual = Administrador.PRIORIDAD; 
        
        System.out.println("--> Algoritmo activo: " + "PRIORIDAD (El VIP pasa primero)");

        // 2. Creación de Procesos
        // P1 y P2 son usuarios normales (Prioridad 1)
        Proceso p1 = new Proceso(1, "Descarga_Datos_1", 5, 1, 20);
        Proceso p2 = new Proceso(2, "Descarga_Datos_2", 5, 1, 20);
        
        // P3 es CRÍTICO (Prioridad 99)
        Proceso pVIP = new Proceso(3, "CORRECCION_ORBITA", 3, 99, 50);

        // 3. Planificación (Simulamos que van llegando)
        
        System.out.println("... Llegó " + p1.nombre + " (Prioridad " + p1.prioridad + ")");
        admin.planificarProceso(p1);
        
        System.out.println("... Llegó " + p2.nombre + " (Prioridad " + p2.prioridad + ")");
        admin.planificarProceso(p2);
        
        System.out.println("... Llegó " + pVIP.nombre + " (Prioridad " + pVIP.prioridad + ") -> ¡SE DEBE COLAR!");
        admin.planificarProceso(pVIP);

        // 4. Arrancar la simulación
        System.out.println("\n--- ⏱️ INICIANDO RELOJ ---");
        Reloj hilo = new Reloj(admin);
        hilo.start();
    }
}