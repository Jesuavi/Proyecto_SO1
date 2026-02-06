package clases;

// Esta es TU versión de una lista. 
// Usaremos esto para la cola de "Listos", "Bloqueados", etc.
public class Cola {
    private Nodo inicio; // El primer vagón (Cabeza)
    private Nodo fin;    // El último vagón (Cola)
    private int tamano;  // Para saber cuántos procesos hay

    public Cola() {
        this.inicio = null;
        this.fin = null;
        this.tamano = 0;
    }

    // Método para saber si está vacía
    public boolean esVacia() {
        return inicio == null;
    }

    // Método ENCOLAR: Agrega un proceso al final
    public void encolar(Proceso p) {
        Nodo nuevoNodo = new Nodo(p);
        
        if (esVacia()) {
            inicio = nuevoNodo;
            fin = nuevoNodo;
        } else {
            fin.siguiente = nuevoNodo; // El último se engancha al nuevo
            fin = nuevoNodo;           // El nuevo pasa a ser el último
        }
        tamano++;
    }

    // Método DESENCOLAR: Saca el proceso que está al frente (para CPU)
    public Proceso desencolar() {
        if (esVacia()) {
            return null; // No hay nada que sacar
        }
        
        Proceso procesoSalida = inicio.dato;
        inicio = inicio.siguiente; // Movemos el inicio al segundo vagón
        tamano--;
        
        if (inicio == null) { // Si sacamos el último, el fin también es null
            fin = null;
        }
        
        return procesoSalida;
    }
    
    // Para ver el tamaño
    public int getTamano() {
        return tamano;
    }
}