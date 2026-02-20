package Clases;

import java.util.Random;

public class GeneradorProcesos {
    private static Random random = new Random();
    private static int contadorId = 10; // Empezamos después de los iniciales

    public static Proceso crearProcesoAleatorio(String tipo) {
        contadorId++;
        String nombre = tipo + "_" + contadorId;
        
        // Datos aleatorios balanceados
        int instrucciones = 10 + random.nextInt(40); // 10 a 50
        int prioridad = 1 + random.nextInt(5);      // 1 a 5
        int deadline = 100 + random.nextInt(900);   // 100 a 1000
        int tamano = 5 + random.nextInt(15);        // 5 a 20 MB
        
        // 50% de probabilidad de que tenga E/S 
        int detonador = 99; 
        int duracion = 0;
        if (random.nextInt(100) < 50) {
            detonador = 2 + random.nextInt(5);  // Se bloquea rápido
            duracion = 10 + random.nextInt(15); // Tarda entre 10 y 25 ciclos en volver
        }

        return new Proceso(contadorId, nombre, instrucciones, prioridad, deadline, tamano, detonador, duracion);
    }
}
