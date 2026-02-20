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
                Thread.sleep(10000); // Cada 10 segundos
                Proceso p = GeneradorProcesos.crearProcesoAleatorio("SENSOR");
                admin.admitirProceso(p);
            } catch (InterruptedException e) { return; }
        }
    }
}