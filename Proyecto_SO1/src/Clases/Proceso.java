package Clases;

// Esta clase es el PCB (Process Control Block)
// Es la "carpeta" con toda la info de una tarea del satélite.
public class Proceso {
    public int id;
    public String nombre;
    public String estado; // "Nuevo", "Listo", "Ejecución", etc.
    
    // Contadores del CPU
    public int pc;  // Program Counter (dónde va la lectura)
    public int mar; // Memory Address Register (dirección de memoria)
    
    // Datos para el Planificador
    public int prioridad;       // 1 (Alta) a 3 (Baja)
    public int instrucciones;   // Cuánto dura en total
    public int deadline;        // Ciclo límite para terminar
    
    // Constructor: Para crear un proceso nuevo
    public Proceso(int id, String nombre, int instrucciones, int prioridad, int deadline) {
        this.id = id;
        this.nombre = nombre;
        this.instrucciones = instrucciones;
        this.prioridad = prioridad;
        this.deadline = deadline;
        this.estado = "Nuevo";
        this.pc = 0;
        this.mar = 0;
    }
    
    // Método para imprimir los datos (nos sirve para probar)
    @Override
    public String toString() {
        return "ID:" + id + " | " + nombre + " | Estado:" + estado;
    }
}