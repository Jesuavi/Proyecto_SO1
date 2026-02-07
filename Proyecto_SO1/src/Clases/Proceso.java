package Clases;

public class Proceso {
    // --- Atributos Básicos ---
    public int id;
    public String nombre;
    public String estado; 
    
    // --- Ejecución ---
    public int instrucciones;    
    public int pc;               // Program Counter
    public int mar;              // Memory Address Register
    
    // --- Planificación ---
    public int prioridad;       
    public int deadline;        
    public int tiempoRestante;   
    
    // --- Memoria ---
    public int tamano;           

    // --- NUEVO: E/S DETERMINISTA (PUNTO 3) ---
    public int cicloDetonadorES; // ¿En qué instrucción ocurre el bloqueo? (Ej: en la 3)
    public int duracionES;       // ¿Cuántos ciclos tarda el dispositivo? (Ej: 5 ciclos)
    public int contadorES;       // Contador interno para saber cuánto lleva esperando

    // Constructor Actualizado
    public Proceso(int id, String nombre, int instrucciones, int prioridad, int deadline, int tamano, int cicloDetonadorES, int duracionES) {
        this.id = id;
        this.nombre = nombre;
        this.instrucciones = instrucciones;
        this.prioridad = prioridad;
        this.deadline = deadline;
        this.tamano = tamano;
        
        // Configuración E/S
        this.cicloDetonadorES = cicloDetonadorES;
        this.duracionES = duracionES;
        this.contadorES = 0; // Inicia en 0

        // Inicialización
        this.estado = "Nuevo"; 
        this.pc = 0;             
        this.mar = 0;            
        this.tiempoRestante = instrucciones; 
    }

    public void ejecutarCiclo() {
        if (this.pc < this.instrucciones) {
            this.pc++;           
            this.mar++;          
            this.tiempoRestante = this.instrucciones - this.pc;
        }
    }
}