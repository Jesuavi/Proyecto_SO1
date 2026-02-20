package Clases;

import java.util.Random;

public class GeneradorEmergencias extends Thread {
    
    private Administrador admin;
    private Random random = new Random();
    private boolean activo = true;
    
    // Ahora guardamos el "bloque" (decena) en lugar del ciclo exacto
    private int ultimoBloqueEvaluado = 0; 

    public GeneradorEmergencias(Administrador admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        while (activo) {
            try {
                // Duerme un poquito para no saturar el procesador
                Thread.sleep(50); 
                
                // Dividimos entre 10. 
                // Ej: Ciclos 10 al 19 darán bloque 1. Ciclos 20 al 29 darán bloque 2.
                int bloqueActual = Administrador.cicloReloj / 10;
                
                // Si entramos a un nuevo bloque de 10 y no es el inicio (bloque 0)
                if (bloqueActual > 0 && bloqueActual > ultimoBloqueEvaluado) {
                    
                    ultimoBloqueEvaluado = bloqueActual; // Marcamos el bloque como evaluado
                    
                    // 🎲 Probabilidad de emergencia (lo dejé en 99% para tu prueba)
                    if (random.nextInt(100) < 99) {
                        
                        String[] emergencias = {
                            "Impacto Meteorito", 
                            "Falla de Oxigeno",  
                            "Tormenta Solar"     
                        };
                        
                        String evento = emergencias[random.nextInt(emergencias.length)];
                        admin.generarInterrupcion(evento);
                        
                    } else {
                        System.out.println("⏳ [Info] Ciclo " + Administrador.cicloReloj + " alcanzado. Todo en orden.");
                    }
                }
                
            } catch (InterruptedException e) {
                return;
            }
        }
    }
    
    public void detener() { 
        this.activo = false; 
    }
}