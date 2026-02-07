package Clases;

public class Cola {
    Nodo inicio;
    Nodo fin;

    public Cola() {
        this.inicio = null;
        this.fin = null;
    }

    // --- MÉTODOS BÁSICOS ---
    public boolean esVacia() {
        return inicio == null;
    }

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
        if (inicio == null) fin = null;
        return dato;
    }

    // =======================================================
    //   PARTE 1: ALGORITMOS DE PLANIFICACIÓN (Lo que te daba error)
    // =======================================================

    // PRIORIDAD: Mayor número va primero
    public void encolarPorPrioridad(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (esVacia() || p.prioridad > inicio.dato.prioridad) {
            nuevo.siguiente = inicio;
            inicio = nuevo;
            if (fin == null) fin = nuevo;
            return;
        }
        Nodo actual = inicio;
        while (actual.siguiente != null && actual.siguiente.dato.prioridad >= p.prioridad) {
            actual = actual.siguiente;
        }
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
        if (nuevo.siguiente == null) fin = nuevo;
    }

    // SRT: Menor tiempo restante va primero
    public void encolarPorTiempoRestante(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (esVacia() || p.tiempoRestante < inicio.dato.tiempoRestante) {
            nuevo.siguiente = inicio;
            inicio = nuevo;
            if (fin == null) fin = nuevo;
            return;
        }
        Nodo actual = inicio;
        while (actual.siguiente != null && actual.siguiente.dato.tiempoRestante <= p.tiempoRestante) {
            actual = actual.siguiente;
        }
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
        if (nuevo.siguiente == null) fin = nuevo;
    }

    // EDF: Menor deadline (más urgente) va primero
    public void encolarPorDeadline(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (esVacia() || p.deadline < inicio.dato.deadline) {
            nuevo.siguiente = inicio;
            inicio = nuevo;
            if (fin == null) fin = nuevo;
            return;
        }
        Nodo actual = inicio;
        while (actual.siguiente != null && actual.siguiente.dato.deadline <= p.deadline) {
            actual = actual.siguiente;
        }
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
        if (nuevo.siguiente == null) fin = nuevo;
    }

    // =======================================================
    //   PARTE 2: GESTIÓN DE MEMORIA (SWAP)
    // =======================================================

    // Busca al candidato para expulsar de la RAM (El deadline más lejano)
    public Proceso obtenerMayorDeadline() {
        if (esVacia()) return null;
        Nodo temp = inicio;
        Proceso mayor = temp.dato;
        while (temp != null) {
            // Buscamos el mayor valor (el menos urgente)
            if (temp.dato.deadline > mayor.deadline) {
                mayor = temp.dato;
            }
            temp = temp.siguiente;
        }
        return mayor;
    }

    // Saca un proceso específico de la fila (para moverlo a disco)
    public void eliminarPorId(int id) {
        if (esVacia()) return;
        
        // Si es el primero
        if (inicio.dato.id == id) {
            desencolar();
            return;
        }

        Nodo anterior = inicio;
        Nodo actual = inicio.siguiente;

        while (actual != null) {
            if (actual.dato.id == id) {
                anterior.siguiente = actual.siguiente;
                if (actual == fin) {
                    fin = anterior;
                }
                return;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
    }
}