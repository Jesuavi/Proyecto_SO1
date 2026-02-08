package Clases;

import java.util.Random;

public class GeneradorEmergencias extends Thread {
    private Administrador admin;
    private Random random = new Random();
    private boolean activo = true;

    public GeneradorEmergencias(Administrador admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        while (activo) {
            try {
                Thread.sleep(10000); // 10 segundos
                
                // 20% de probabilidad
                if (random.nextInt(100) < 20) {
                    int tipo = random.nextInt(3);
                    String evento;
                    
                    switch(tipo) {
                        case 0: evento = "Impacto Meteorito"; break;
                        case 1: evento = "Falla de Oxigeno"; break;
                        default: evento = "Tormenta Solar"; break;
                    }
                    admin.generarInterrupcion(evento);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
    
    public void detener() { this.activo = false; }
}