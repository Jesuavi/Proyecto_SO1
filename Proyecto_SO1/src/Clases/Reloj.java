package Clases;

// Sin imports raros. Thread es nativo de Java.

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
                admin.ejecutarCiclo();
                
            } catch (InterruptedException ex) {
                // Si algo interrumpe el reloj, solo imprimimos el error en consola simple
                System.out.println("Error en el Reloj: " + ex.getMessage());
            }
        }
    }
}