package Clases;

public class Proceso {
    // --- Atributos Básicos ---
    public int id;
    public String nombre;
    public String estado; // Nuevo, Listo, Ejecución, Bloqueado, Terminado, etc. 
    
    // --- Atributos de Ejecución ---
    public int instrucciones;    // Total de instrucciones (rafaga de CPU)
    public int pc;               // Program Counter (Instrucción actual) [cite: 22, 53, 67]
    public int mar;              // Memory Address Register [cite: 22, 53, 67]
    
    // --- Atributos de Planificación ---
    public int prioridad;        // Para algoritmo de Prioridad Estática 
    public int deadline;         // Ciclo máximo para finalizar (Para EDF) 
    public int tiempoRestante;   // Para SRT (instrucciones - pc) [cite: 23, 35]
    
    // --- Atributos de Memoria ---
    public int tamano;           // Tamaño en memoria para gestión de RAM/Swap [cite: 48]


    public Proceso(int id, String nombre, int instrucciones, int prioridad, int deadline, int tamano) {
        this.id = id;
        this.nombre = nombre;
        this.instrucciones = instrucciones;
        this.prioridad = prioridad;
        this.deadline = deadline;
        this.tamano = tamano;
        
        // Inicialización de estado y registros
        this.estado = "Nuevo"; 
        this.pc = 0;             // Inicia en la instrucción 0 
        this.mar = 0;            // Inicia en la dirección de memoria 0 
        this.tiempoRestante = instrucciones; // Al inicio, falta todo el proceso
    }

    /**
     * Simula el avance del proceso en un ciclo de reloj.
     * Incrementa PC y MAR según el requerimiento del proyecto. 
     */
    public void ejecutarCiclo() {
        if (this.pc < this.instrucciones) {
            this.pc++;           // Incrementa contador de programa 
            this.mar++;          // Incrementa registro de dirección de memoria 
            this.tiempoRestante = this.instrucciones - this.pc;
        }
    }
    
    // Método útil para mostrar info en la GUI
    @Override
    public String toString() {
        return nombre + " (ID: " + id + ") | PC: " + pc + " | Deadline: " + deadline;
    }
}