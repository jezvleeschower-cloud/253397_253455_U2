import java.util.Scanner;

/**
 * Compilar: javac AbordajeAvion.java
 * Ejecutar: java AbordajeAvion
 */
public class AbordajeAvion {


    static class Pasajero {
        String nombre;
        String idDocumento;   // numero de identificacion oficial
        String asiento;
        int grupo;            // grupo indicado en el boleto (1, 2 o 3)

        Pasajero(String nombre, String idDocumento, String asiento, int grupo) {
            this.nombre = nombre;
            this.idDocumento = idDocumento;
            this.asiento = asiento;
            this.grupo = grupo;
        }

        @Override
        public String toString() {
            return nombre + " | Asiento " + asiento + " | Grupo " + grupo;
        }
    }

    static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
        }
    }

    static class Cola<T> {
        private Nodo<T> frente;
        private Nodo<T> fin;
        private int tamano;

        // Agrega al final de la cola
        void encolar(T dato) {
            Nodo<T> nuevo = new Nodo<>(dato);
            if (estaVacia()) {
                frente = nuevo;
            } else {
                fin.siguiente = nuevo;
            }
            fin = nuevo;
            tamano++;
        }

        // Saca al elemento del frente
        T desencolar() {
            if (estaVacia()) return null;
            T dato = frente.dato;
            frente = frente.siguiente;
            if (frente == null) fin = null;
            tamano--;
            return dato;
        }

        // Consulta el frente sin sacarlo
        T verFrente() {
            return estaVacia() ? null : frente.dato;
        }

        boolean estaVacia() {
            return frente == null;
        }

        int tamano() {
            return tamano;
        }

        void mostrar() {
            if (estaVacia()) {
                System.out.println("   (vacia)");
                return;
            }
            Nodo<T> actual = frente;
            int pos = 1;
            while (actual != null) {
                System.out.println("   " + pos + ". " + actual.dato);
                actual = actual.siguiente;
                pos++;
            }
        }
    }

    static final int TOTAL_GRUPOS = 3;
    static Cola<Pasajero>[] colasPorGrupo;      // espera de cada grupo
    static Cola<Pasajero> colaEscaner = new Cola<>(); // fila frente al escaner
    static Cola<Pasajero> abordados = new Cola<>();   // ya dentro del avion
    static Cola<Pasajero> rechazados = new Cola<>();  // enviados a mostrador
    static int grupoLlamado = 0;                // ultimo grupo llamado

    static Scanner sc = new Scanner(System.in);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        colasPorGrupo = new Cola[TOTAL_GRUPOS + 1]; // indices 1..3
        for (int i = 1; i <= TOTAL_GRUPOS; i++) {
            colasPorGrupo[i] = new Cola<>();
        }

        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Elige una opcion: ");
            System.out.println();
            switch (opcion) {
                case 1: registrarPasajero(); break;
                case 2: llamarSiguienteGrupo(); break;
                case 3: escanearPasajero(); break;
                case 4: mostrarEstado(); break;
                case 0: System.out.println("Fin del programa. Buen vuelo!"); break;
                default: System.out.println("Opcion invalida.");
            }
        } while (opcion != 0);
    }

    static void mostrarMenu() {
        System.out.println("\n========== PUERTA DE EMBARQUE ==========");
        System.out.println("1. Registrar pasajero");
        System.out.println("2. Llamar al siguiente grupo");
        System.out.println("3. Escanear pasajero (pase + identificacion)");
        System.out.println("4. Ver estado de las colas");
        System.out.println("0. Salir");
        System.out.println("========================================");
    }

    // 1. El pasajero queda en la cola de su grupo
    static void registrarPasajero() {
        System.out.print("Nombre: ");
        String nombre = sc.nextLine().trim();
        System.out.print("Numero de identificacion: ");
        String id = sc.nextLine().trim();
        System.out.print("Asiento (ej. 12A): ");
        String asiento = sc.nextLine().trim();
        int grupo = leerEntero("Grupo del boleto (1-" + TOTAL_GRUPOS + "): ");

        if (grupo < 1 || grupo > TOTAL_GRUPOS) {
            System.out.println("Grupo invalido. Registro cancelado.");
            return;
        }
        if (grupo <= grupoLlamado) {
            System.out.println("Aviso: el grupo " + grupo + " ya fue llamado; "
                    + "pasa directo a la fila del escaner.");
            colaEscaner.encolar(new Pasajero(nombre, id, asiento, grupo));
            return;
        }
        colasPorGrupo[grupo].encolar(new Pasajero(nombre, id, asiento, grupo));
        System.out.println("Pasajero registrado en la cola del grupo " + grupo + ".");
    }

    // 2. Se llama al grupo: todos pasan en orden a la fila del escaner
    static void llamarSiguienteGrupo() {
        if (grupoLlamado >= TOTAL_GRUPOS) {
            System.out.println("Ya se llamaron todos los grupos.");
            return;
        }
        grupoLlamado++;
        System.out.println(">>> Se llama al GRUPO " + grupoLlamado + " <<<");
        Cola<Pasajero> cola = colasPorGrupo[grupoLlamado];
        if (cola.estaVacia()) {
            System.out.println("No hay pasajeros en este grupo.");
            return;
        }
        while (!cola.estaVacia()) {
            colaEscaner.encolar(cola.desencolar());
        }
        System.out.println("Pasajeros del grupo " + grupoLlamado
                + " pasan a la fila del escaner.");
    }

    // 3. Se atiende al primero de la fila: pase + identificacion
    static void escanearPasajero() {
        if (colaEscaner.estaVacia()) {
            System.out.println("No hay pasajeros frente al escaner.");
            return;
        }
        Pasajero p = colaEscaner.verFrente();
        System.out.println("Pasajero en el escaner: " + p);

        System.out.print("Presenta pase de abordar (s/n): ");
        boolean tienePase = sc.nextLine().trim().equalsIgnoreCase("s");
        System.out.print("Numero de identificacion presentado: ");
        String idPresentado = sc.nextLine().trim();

        colaEscaner.desencolar();
        if (tienePase && idPresentado.equals(p.idDocumento)) {
            abordados.encolar(p);
            System.out.println("VALIDO. " + p.nombre + " aborda el avion.");
        } else {
            rechazados.encolar(p);
            System.out.println("RECHAZADO. " + p.nombre
                    + " debe pasar al mostrador (pase o ID no coinciden).");
        }
    }

    // 4. Estado general
    static void mostrarEstado() {
        for (int g = 1; g <= TOTAL_GRUPOS; g++) {
            System.out.println("Cola de espera - Grupo " + g
                    + " (" + colasPorGrupo[g].tamano() + "):");
            colasPorGrupo[g].mostrar();
        }
        System.out.println("Fila del escaner (" + colaEscaner.tamano() + "):");
        colaEscaner.mostrar();
        System.out.println("Abordados (" + abordados.tamano() + "):");
        abordados.mostrar();
        System.out.println("Enviados a mostrador (" + rechazados.tamano() + "):");
        rechazados.mostrar();
    }

    static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Escribe un numero valido.");
            }
        }
    }
}