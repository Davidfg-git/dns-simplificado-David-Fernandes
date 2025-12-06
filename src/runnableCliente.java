import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class runnableCliente implements Runnable{

    private Socket socketCliente;
    private HashMap<String, List<Registro>> diccionario;
    private File archivoDirecciones;

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

                    // Expresion regular para validar el formato
                    String regex = "^LOOKUP\\s+(A|CNAME|MX)\\s+([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$";

                    String regexAnadir = "^REGISTER\\s+([a-zA-Z0-9.-]+)\\s+(A|CNAME|MX)\\s+(\\S+)$";

                    if (peticion.matches(regex)) {
                        String[] partes = peticion.split("\\s+");
                        String tipo = partes[1];
                        String dominio = partes[2];


                        if (!diccionario.containsKey(dominio)) {
                            salida.println("404 Not Found");
                            continue;
                        }

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

                        if (!diccionario.containsKey(partes[0])) {
                            diccionario.put(partes[0], new ArrayList<>());
                            diccionario.get(partes[0]).add(registro);
                            salida.println("200 record added");
                        } else {
                            diccionario.get(partes[0]).add(registro);
                            salida.println("200 record added");
                        }

                    } else {
                        salida.println("400 Bad Request");
                    }

                } catch (Exception e) {
                    salida.println("500 Server Error");
                }
            }
        }catch (IOException e){
            System.out.println("Error IO" + e.getMessage());
        }
    }
}

