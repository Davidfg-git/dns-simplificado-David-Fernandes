import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServidorDNS {

    private static final int MAX_CLIENTES = 5;
    public static final AtomicInteger clientesActivos = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {

        HashMap<String, List<Registro>> diccionario = new HashMap<>();
        File archivoDirecciones = new File("src/direcciones.txt");

        // Al iniciar el servidor lee el archivo de registros y se añaden al hashmap
        try {
            BufferedReader br = new BufferedReader(new FileReader(archivoDirecciones));
            String line;
            while ((line = br.readLine()) != null) {
                String[] partes = line.split(" ");
                if (partes.length <= 3) {
                    Registro registro = new Registro(partes[0], partes[1], partes[2]);

                    // Si el dominio del registro a meter no existe en el hashmap se crea
                    // una lista para guardar registros futuros con ese dominio.
                    // Si ya existía, simplemente añade el registro
                    if (!diccionario.containsKey(partes[0])) {
                        diccionario.put(partes[0], new ArrayList<>());
                        diccionario.get(partes[0]).add(registro);
                    } else {
                        diccionario.get(partes[0]).add(registro);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error: Archivo no encontrado");
        }


        int puerto = 5000;
        // Se abre el servidor y maneja las conexiones.
        try (ServerSocket socket = new ServerSocket(puerto);) {
            while (true) {
                Socket socketCliente = socket.accept();

                // Cuando se conecta un cliente incrementa el número de clientes activos
                int actuales = clientesActivos.incrementAndGet();

                // Si se supera el maximo de clientes conectados, decrementa el contador de
                // clientes y cierra esa conexión que había entrado.
                if (actuales > MAX_CLIENTES) {
                    clientesActivos.decrementAndGet();
                    System.out.println("Máximo de clientes alcanzado.");
                    socketCliente.close();
                    continue;
                }

                // Se crea el hilo que maneja el cliente y se inicia.
                runnableCliente runnableCliente = new runnableCliente(socketCliente, diccionario, archivoDirecciones);
                Thread threadCliente = new Thread(runnableCliente);
                threadCliente.start();
            }
        }
    }
}