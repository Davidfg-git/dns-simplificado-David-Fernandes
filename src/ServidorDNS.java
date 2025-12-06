import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServidorDNS {
    public static void main(String[] args) throws IOException {

        HashMap<String, List<Registro>> diccionario = new HashMap<>();
        File archivoDirecciones = new File("src/direcciones.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(archivoDirecciones));
            String line;
            while ((line = br.readLine()) != null) {
                String[] partes = line.split(" ");
                if (partes.length <= 3) {
                    Registro registro = new Registro(partes[0], partes[1], partes[2]);
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
        try (ServerSocket socket = new ServerSocket(puerto);) {
            while (true) {
                Socket socketCliente = socket.accept();

                System.out.println("Cliente conectado: " + socketCliente.getInetAddress().getHostAddress());

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


                            if (!diccionario.containsKey(dominio)){
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

                        } else if (peticion.matches(regexAnadir)){
                            String[] partes = peticion.split("\\s+",2);

                           try(BufferedWriter bf = new BufferedWriter(new FileWriter(archivoDirecciones,true))){
                               bf.write(System.lineSeparator() + partes[1]);
                           }

                           String[] solicitud = partes[1].split("\\s+");
                           Registro registro = new Registro(solicitud[0],solicitud[1],solicitud[2]);

                            if (!diccionario.containsKey(partes[0])) {
                                diccionario.put(partes[0], new ArrayList<>());
                                diccionario.get(partes[0]).add(registro);
                                salida.println("200 record added");
                            }else {
                                diccionario.get(partes[0]).add(registro);
                                salida.println("200 record added");
                            }

                        }else{
                            salida.println("400 Bad Request");
                        }

                    } catch (Exception e) {
                        salida.println("500 Server Error");
                    }
                }
            }
        }
    }
}
