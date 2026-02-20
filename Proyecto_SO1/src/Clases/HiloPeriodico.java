package Clases;

public class HiloPeriodico extends Thread {
    private Administrador admin;

    public HiloPeriodico(Administrador admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(15000); // Cada 15 segundos
                Proceso p = GeneradorProcesos.crearProcesoAleatorio("SENSOR");
                System.out.println("📡 [TAREA PERIÓDICA]: Nuevo reporte de sensores recibido.");
                admin.admitirProceso(p);
            } catch (InterruptedException e) { return; }
        }
    }
}