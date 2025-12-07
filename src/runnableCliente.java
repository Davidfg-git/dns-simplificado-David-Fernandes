import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class runnableCliente implements Runnable {

    private Socket socketCliente;
    private HashMap<String, List<Registro>> diccionario;
    private File archivoDirecciones;

    // Recibimos los datos necesarios para manejar correctamente.
    public runnableCliente(Socket socketCliente, HashMap<String, List<Registro>> diccionario, File archivoDirecciones) {
        this.socketCliente = socketCliente;
        this.diccionario = diccionario;
        this.archivoDirecciones = archivoDirecciones;
    }


    @Override
    public void run() {
        System.out.println("Cliente conectado: " + socketCliente.getInetAddress().getHostAddress());

        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true);

            while (true) {
                try {
                    salida.println("Formato: LOOKUP <tipo> <dominio>   |  exit para salir");
                    String peticion = entrada.readLine();

                    if (peticion == null) break;

                    if (peticion.equalsIgnoreCase("exit")) {
                        salida.println("Cerrando conexión...");
                        socketCliente.close();
                        break;
                    }

                    if (peticion.equalsIgnoreCase("LIST")) {
                        salida.println("150 Inicio de listado");
                        diccionario.forEach((s, registros) -> {
                            registros.forEach(registro -> salida.println(registro));
                            salida.println("--------------------------------------------");
                        });
                        salida.println("226 Fin listado");

                        continue;
                    }

                    // Expresion regular para validar el formato del comando LOOKUP
                    String regex = "^LOOKUP\\s+(A|CNAME|MX)\\s+([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$";

                    // Expresion regular para validar el formato del comando REGISTER
                    String regexAnadir = "^REGISTER\\s+([a-zA-Z0-9.-]+)\\s+(A|CNAME|MX)\\s+(\\S+)$";

                    if (peticion.matches(regex)) {
                        String[] partes = peticion.split("\\s+");
                        String tipo = partes[1];
                        String dominio = partes[2];


                        if (!diccionario.containsKey(dominio)) {
                            salida.println("404 Not Found");
                            continue;
                        }

                        // Recorre el hashmap para buscar registros del dominio con el tipo solicitado.
                        boolean encontrado = false;
                        for (Registro r : diccionario.get(dominio)) {
                            if (r.getTipo().equalsIgnoreCase(tipo)) {
                                salida.println("200 " + r.getIp());
                                encontrado = true;
                            }
                        }

                        if (!encontrado) {
                            salida.println("404 Not Found");
                        }

                    } else if (peticion.matches(regexAnadir)) {
                        String[] partes = peticion.split("\\s+", 2);

                        try (BufferedWriter bf = new BufferedWriter(new FileWriter(archivoDirecciones, true))) {
                            bf.write(System.lineSeparator() + partes[1]);
                        }

                        String[] solicitud = partes[1].split("\\s+");
                        Registro registro = new Registro(solicitud[0], solicitud[1], solicitud[2]);

                        // Igual que en la clase ServidorDNS: si no existe el dominio se
                        // crea la lista, si existe se añade el registro
                        if (!diccionario.containsKey(solicitud[0])) {
                            diccionario.put(solicitud[0], new ArrayList<>());
                            diccionario.get(solicitud[0]).add(registro);
                            salida.println("200 record added");
                        } else {
                            diccionario.get(solicitud[0]).add(registro);
                            salida.println("200 record added");
                        }

                        // Error para cuando se mande un comando inválido.
                    } else {
                        salida.println("400 Bad Request");
                    }

                    // Error del servidor al procesar una petición
                } catch (Exception e) {
                    salida.println("500 Server Error");
                }
            }
        } catch (IOException e) {
            System.out.println("Error IO" + e.getMessage());
        } finally {
            //Cuando el cliente cierra la conexión, se decrementa el número de clientes
            // activos y se cierra el socket por seguridad.
            ServidorDNS.clientesActivos.decrementAndGet();
            System.out.println("Cliente desconectado. Activos: " + ServidorDNS.clientesActivos.get());

            try {
                socketCliente.close();
            } catch (Exception ignored) {
            }
        }
    }
}

