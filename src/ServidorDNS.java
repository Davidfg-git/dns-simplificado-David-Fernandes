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
            BufferedReader br = new BufferedReader(new FileReader(archivoDirecciones));
            String line;
            while ((line = br.readLine()) != null) {
                String[] partes = line.split(" ");
                if(partes.length <=3) {
                    Registro registro = new Registro(partes[0],partes[1],partes[2]);
                    if (!diccionario.containsKey(partes[0])){
                        diccionario.put(partes[0],new ArrayList<>());
                        diccionario.get(partes[0]).add(registro);
                    }else{
                        diccionario.get(partes[0]).add(registro);
                    }
                }
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
