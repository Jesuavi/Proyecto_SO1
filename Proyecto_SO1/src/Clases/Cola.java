package Clases;

public class Cola {
    public Nodo inicio, fin;

    public Cola() {
        this.inicio = null;
        this.fin = null;
    }

    public boolean esVacia() {
        return inicio == null;
    }

    // Método estándar (al final)
    public void encolar(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (esVacia()) {
            inicio = fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
    }

    // Método para ISR (al principio)
    public void encolarAlInicio(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (esVacia()) {
            inicio = fin = nuevo;
        } else {
            nuevo.siguiente = inicio;
            inicio = nuevo;
        }
    }

    public Proceso desencolar() {
        if (esVacia()) return null;
        Proceso p = inicio.dato;
        inicio = inicio.siguiente;
        if (inicio == null) fin = null;
        return p;
    }

    // --- MÉTODOS DE ORDENAMIENTO (PARA QUE NO DE ERROR EL ADMIN) ---

    // 1. SRT: Menor tiempo restante primero
    public void encolarPorSRT(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (esVacia() || p.instrucciones < inicio.dato.instrucciones) {
            nuevo.siguiente = inicio;
            inicio = nuevo;
            if (fin == null) fin = nuevo;
        } else {
            Nodo actual = inicio;
            while (actual.siguiente != null && actual.siguiente.dato.instrucciones <= p.instrucciones) {
                actual = actual.siguiente;
            }
            nuevo.siguiente = actual.siguiente;
            actual.siguiente = nuevo;
            if (nuevo.siguiente == null) fin = nuevo;
        }
    }

    // 2. PRIORIDAD: Menor número = Mayor prioridad
    public void encolarPorPrioridad(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (esVacia() || p.prioridad < inicio.dato.prioridad) {
            nuevo.siguiente = inicio;
            inicio = nuevo;
            if (fin == null) fin = nuevo;
        } else {
            Nodo actual = inicio;
            while (actual.siguiente != null && actual.siguiente.dato.prioridad <= p.prioridad) {
                actual = actual.siguiente;
            }
            nuevo.siguiente = actual.siguiente;
            actual.siguiente = nuevo;
            if (nuevo.siguiente == null) fin = nuevo;
        }
    }

    // 3. EDF: Menor Deadline primero
    public void encolarPorEDF(Proceso p) {
        Nodo nuevo = new Nodo(p);
        if (esVacia() || p.deadline < inicio.dato.deadline) {
            nuevo.siguiente = inicio;
            inicio = nuevo;
            if (fin == null) fin = nuevo;
        } else {
            Nodo actual = inicio;
            while (actual.siguiente != null && actual.siguiente.dato.deadline <= p.deadline) {
                actual = actual.siguiente;
            }
            nuevo.siguiente = actual.siguiente;
            actual.siguiente = nuevo;
            if (nuevo.siguiente == null) fin = nuevo;
        }
    }

    // --- MÉTODOS AUXILIARES PARA SWAP ---
    public Proceso obtenerMayorDeadline() {
        if (esVacia()) return null;
        Nodo actual = inicio;
        Proceso candidato = inicio.dato;
        while (actual != null) {
            if (actual.dato.deadline > candidato.deadline) {
                candidato = actual.dato;
            }
            actual = actual.siguiente;
        }
        return candidato;
    }

    public void eliminarPorId(int id) {
        if (esVacia()) return;
        if (inicio.dato.id == id) {
            inicio = inicio.siguiente;
            if (inicio == null) fin = null;
            return;
        }
        Nodo actual = inicio;
        while (actual.siguiente != null) {
            if (actual.siguiente.dato.id == id) {
                actual.siguiente = actual.siguiente.siguiente;
                if (actual.siguiente == null) fin = actual;
                return;
            }
            actual = actual.siguiente;
        }
    }
}