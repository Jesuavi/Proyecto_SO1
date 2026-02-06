package clases;

// El Nodo es como un eslabón de la cadena
public class Nodo {
    public Proceso dato; // Aquí guardamos el Proceso (la carga)
    public Nodo siguiente; // Este es el gancho al siguiente vagón
    
    // Constructor
    public Nodo(Proceso dato) {
        this.dato = dato;
        this.siguiente = null; // Al principio no está enganchado a nada
    }
}