import java.util.ArrayList;
import java.util.List;

public class Registro {
    //un registro debe tener, dominio, tipo (A,MX,CNAME) , valor
    //se almacena en un array cada registro (array de registros)
    //se tendria que hacer un while en el array hasta encontrar el nombre mandado para responderle mandandole lo que tiene
    //Diccionario = es un pack clave (nuestra clave es el dominio) , valor
    //Representacion de la forma del diccionario:
    // dic = [
    // "test.com -> ("test,"A,"1.1.1.1")
    // etc ]
    //se haría un dic.get("test.com") y te devuelve los datos correspondientes
    //hay que agrupar todos los registros del mismo dominio en un arrraylist, quiero decir si en el diccionario hubieran tres registros del mismo dominio pero de diferente tipo
    //en el diccionario meterías el arrayList
    //En la fase 1 solo se considera tipo A pero hay que dejarlo preparado ya para las soguientes fases.
        private String dominio;
        private String  tipo;
        private String  nombre;

    public Registro(String dominio, String tipo, String nombre) {
        this.dominio = dominio;
        this.tipo = tipo;
        this.nombre = nombre;
    }

    public Registro() {
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Registro{" +
                "dominio='" + dominio + '\'' +
                ", tipo='" + tipo + '\'' +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}
