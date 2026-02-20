package proyecto_so1;

import Clases.Administrador;
import Clases.GeneradorEmergencias;
import Clases.HiloPeriodico;
import Clases.Reloj;

public class Proyecto_SO1 {

    public static void main(String[] args) {
        
        
        // 1. Creamos el "cerebro" del Sistema Operativo
        Administrador admin = new Administrador();
        
        // 2. Encendemos los componentes de hardware (Los Hilos)
        // El Reloj marca el paso del tiempo (los ciclos)
        new Reloj(admin).start();
        
        // El Generador de Emergencias vigila cada 10 ciclos para lanzar el meteorito
        new GeneradorEmergencias(admin).start();
        
        // El Hilo Periódico vigila si hay tareas de mantenimiento
        new HiloPeriodico(admin).start();
        
        // 3. Encendemos la pantalla (La Interfaz Visual)
        java.awt.EventQueue.invokeLater(() -> {
            new NewJFrame(admin).setVisible(true);
        });
    }
}