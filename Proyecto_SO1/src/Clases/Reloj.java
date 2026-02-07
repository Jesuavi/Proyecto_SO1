package Clases;

// El Reloj es un hilo (Thread) que corre en paralelo al resto del programa
public class Reloj extends Thread {
    
    Administrador admin;

    public Reloj(Administrador admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 1. Esperamos 1 segundo (1000 milisegundos)
                Thread.sleep(1000); 
                
                // 2. Ejecutamos el ciclo del sistema operativo
                // Esto actualiza el proceso actual, verifica si terminó y gestiona la memoria
                admin.ejecutarCiclo();
                
            } catch (InterruptedException ex) {
                // Si algo interrumpe el reloj, mostramos el error
                System.out.println("Error en el Reloj: " + ex.getMessage());
            }
        }
    }
}