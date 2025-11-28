import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServidorDNS {
    public static void main(String[] args) throws IOException {
        HashMap<String,List<Registro>> diccionario = new HashMap<>();

        try {
            File archivoDirecciones = new File("src/direcciones.txt");
            List<String> registros = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(archivoDirecciones));
            String line;
            while ((line = br.readLine()) != null) {
                registros.add(line);
            }
        }catch (FileNotFoundException e) {
            System.out.println("Error: Archivo no encontrado");
        }


        int puerto = 5000;
        try(ServerSocket socket = new ServerSocket(puerto);){
            Socket socketCliente = socket.accept();

        }


    }
}
