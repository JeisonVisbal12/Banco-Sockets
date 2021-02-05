package servidor;
/**
 *
 * @author JEISON VISBAL,LUZ CENY SALAZAR, JUAN PABLO SALAZAR
 * codigo en proyecto BancoXYZ
 */
import java.sql.*;
import javax.swing.JOptionPane;

//Clase que conecta a la clase server con la base de datos bancoXYZ
public class Conexion {
    Connection cn = null;
    public Connection cargar(){
        //le da la direccion al programa, para conectarse al host especificado, junto con la contraseña y el usuario de la base de datos
        try {            
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            cn=DriverManager.getConnection("jdbc:sqlserver://localhost:1433;DatabaseName = bancoXYZ; user =sa"
                    + ";password=ANDREAyandres31");                       
            //JOptionPane.showMessageDialog(null,"Conectado.","conectado",JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"error.","error",JOptionPane.INFORMATION_MESSAGE);             
        } 
    return cn;
   }
}