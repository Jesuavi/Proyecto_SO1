package Clases;

public class Cola {
    
    Nodo inicio;
    Nodo fin;

    public Cola() {
        this.inicio = null;
        this.fin = null;
    }

    // Método estándar: Inserta al final
    public void encolar(Proceso dato) {
        Nodo nuevo = new Nodo(dato);
        if (esVacia()) {
            inicio = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
    }

    public Proceso desencolar() {
        if (esVacia()) return null;
        
        Proceso dato = inicio.dato;
        inicio = inicio.siguiente;
        if (inicio == null) {
            fin = null;
        }
        return dato;
    }

    public boolean esVacia() {
        return inicio == null;
    }

    // --- NUEVO: INSERTAR POR PRIORIDAD (VIP) ---
    // (Mayor número = Mayor prioridad, pasa primero)
    public void encolarPorPrioridad(Proceso nuevoProceso) {
        Nodo nuevo = new Nodo(nuevoProceso);

        // 1. Si está vacía
        if (esVacia()) {
            inicio = nuevo;
            fin = nuevo;
            return;
        }

        // 2. Si es más importante que el primero (Cabecera)
        if (nuevoProceso.prioridad > inicio.dato.prioridad) {
            nuevo.siguiente = inicio;
            inicio = nuevo;
            return;
        }

        // 3. Buscar su lugar en el medio
        Nodo actual = inicio;
        while (actual.siguiente != null && actual.siguiente.dato.prioridad >= nuevoProceso.prioridad) {
            actual = actual.siguiente;
        }
        
        // Insertar
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;

        // 4. Actualizar fin si quedó de último
        if (nuevo.siguiente == null) {
            fin = nuevo;
        }
    }
}