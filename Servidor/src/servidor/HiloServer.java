
package servidor;

import com.sun.istack.internal.logging.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import sun.util.logging.PlatformLogger;


public class HiloServer extends Thread {
    Conexion c = new Conexion();//crea el objeto c para la conexion
    Connection cin = c.cargar();
    private Socket socket;
    private int idSessio;
    PreparedStatement ps = null;// inicializa ps para la ejecucion de scripts en la base de datos
    ServerSocket socketServidor = null;
    BufferedReader entrada = null;//establece la entrada de mensaje del cliente
    PrintWriter salida = null, salida1=null;//establece la salida de respuesta del servidor
    String id_pais;//variable para almacenar el id de pais
    String id_ciudad;//variable para almacenar el id de la ciudad
    String documento;//variable para almacenar el documento
    String nombre;//variable para almacenar el nombre del cliente
    String apellido;
    String tip_doc;//variable para almacenar el tipo de documento
    public HiloServer(Socket socket, int id){
        this.socket = socket;
        this.idSessio=id;
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(new BufferedWriter(new 
	  OutputStreamWriter(socket.getOutputStream())),true);
            salida1 = new PrintWriter(new BufferedWriter(new 
	  OutputStreamWriter(socket.getOutputStream())),true);
            
   
        }catch (IOException ex){
            
        }
    }
    
    @Override
    public void run(){
        try{
          while (true) {  
        String str = entrada.readLine();//crea variable que almacenará el mensaje del cliente
        String opcion = str.substring(str.length()-1);//guarda en variable opcion la letra enviada 
        
	System.out.println("Cliente: " + str);
        System.out.println("Cliente: " + opcion);
       
	if(opcion.equals("A")){
            salida.println(str);
           
            int inicio = str.indexOf(",");
            int intermedio1 = str.indexOf(",",inicio+1);
            int intermedio = str.indexOf(",",intermedio1 +1);
            int fin = str.indexOf(",",intermedio +1);
            
            tip_doc = str.substring(0,inicio);
            id_pais = str.substring(str.length()-6,str.length()-4);
            id_ciudad = str.substring(str.length()-4,str.length()-1);
            documento = str.substring(inicio + 1,intermedio1);
            nombre = str.substring(intermedio1 + 1,intermedio);
            apellido=str.substring(intermedio+1,fin);
            
            try{
                
              String insertar = "insert into bancoXYZ.dbo.Cliente (tipo_documento,documento,nombre,apellido,id_pais,id_ciudad)values (?,?,?,?,?,?)";
            ps = cin.prepareCall(insertar);//llama el string que contiene el script a ejecutar para definir 
            //los datos posteriormente para agregarlos
            ps.setString(1, tip_doc);//guarda el tipo de documento en posicion 1
            ps.setString(2, documento);//guarda el documento en posicion 2
            ps.setString(3, nombre);//guarda el nombre en posicion 3
            ps.setString(4, apellido);//guarda el apellido en posicion 4
            ps.setString(5, id_pais);//guarda el id del pais en posicion 5
            ps.setString(6, id_ciudad);//guarda el id de la ciudad en la posicion 6
            int registro = ps.executeUpdate();//ejecuta el script
            /*
            *Valida si el registro ha sido realizado y envia un mensaje en pantalla de confirmación
            */
            if(registro > 0){
                JOptionPane.showMessageDialog(null, "Cliente agregado con exito","Accion exitosa",JOptionPane.INFORMATION_MESSAGE);
                }
            }
            /*
            *Si no puede realizar envia un mensaje de error
            */
            catch (Exception e){

                JOptionPane.showMessageDialog(null, "Datos erroneos, o Cliente ya registrado","escriba bien",JOptionPane.INFORMATION_MESSAGE);
            }
            /*
            Intenta realizar un insert en la tabla cuenta de la base de datos, obtiene la fecha y la hora para generar
            el id de la cuenta, con los ultimos tres digitos de la cedula
            */
            try{
            Calendar ca = Calendar.getInstance();
            String dia = Integer.toString(ca.get(Calendar.DATE));
            String mes = Integer.toString(ca.get(Calendar.MONTH)+1);
            String annio=Integer.toString(ca.get(Calendar.YEAR));
            String hora =Integer.toString(ca.get(Calendar.HOUR_OF_DAY));
            String minutos = Integer.toString(ca.get(Calendar.MINUTE));
            String segundos = Integer.toString(ca.get(Calendar.SECOND)); 
            String doc=documento.substring(documento.length()-3,documento.length());
            String insertar = "insert into bancoXYZ.dbo.cuenta (id_cuenta,fecha_creacion,saldo,tipo_documento,documento_cliente)values (?,?,?,?,?)";
          ps = cin.prepareCall(insertar);//llama el string que contiene 
          ps.setString(1, hora+minutos+segundos+doc);//agrega el id de la cuenta
          ps.setString(2, dia+"/"+mes+"/"+annio);//agrega la fecha
          ps.setString(3, "0");//guarda un saldo en 0
          ps.setString(4, tip_doc);//guarda el tipo de documento
          ps.setString(5, documento);//guarda el documento del cliente
          
          int registro = ps.executeUpdate();
          
          if(registro > 0){
              JOptionPane.showMessageDialog(null, "Cuenta creada con exito","Accion exitosa",JOptionPane.INFORMATION_MESSAGE);
          }
      }
      catch (Exception e){
          
          JOptionPane.showMessageDialog(null, "Datos erroneos, o Cuenta ya registrada","escriba bien",JOptionPane.INFORMATION_MESSAGE);
          
          
      }
        }
               /*Si la opcion es C obtiene el saldo, el tipo de documento y el id de la cuenta
        realiza la actualizacion del saldo de la cuenta asociada al documento recibido de cliente
        y registra el movimiento generando un codigo con la fecha, la hora y el monto
        */

        
        if(opcion.equals ("C")){
            int inicio = str.indexOf(",");
            String docum = str.substring(0,inicio);
            String monto = str.substring(inicio+1,str.length()-1);
            salida.println(str);
            int mont = Integer.parseInt(monto);
            int canti;
            String tipo_doc = null;
            String id_cuenta=null;
            /*
            Selecciona el saldo, tipo de documento y el id de la cuenta, de la tabla cuenta con el documento solicitado
            */
            try{
            String buscar ="select saldo,tipo_documento,id_cuenta from bancoXYZ.dbo.cuenta where documento_cliente = "+docum+" ;";
            Statement st = cin.createStatement();
            ResultSet rs = st.executeQuery(buscar);
            while(rs.next()){
                
               canti = Integer.parseInt(rs.getString("saldo"));
               mont = mont + canti;
               tipo_doc = rs.getString("tipo_documento");
               id_cuenta = rs.getString("id_cuenta");
            }
        }
        catch(Exception e){
            //JOptionPane.showMessageDialog(null,"Ha ocurrido un error", "Error",JOptionPane.INFORMATION_MESSAGE );
        } 
           
            Calendar ca = Calendar.getInstance();
            String dia = Integer.toString(ca.get(Calendar.DATE));
            String mes = Integer.toString(ca.get(Calendar.MONTH)+1);
            String annio=Integer.toString(ca.get(Calendar.YEAR));
            String hora =Integer.toString(ca.get(Calendar.HOUR_OF_DAY));
            String minutos = Integer.toString(ca.get(Calendar.MINUTE));
            String segundos = Integer.toString(ca.get(Calendar.SECOND));            
            String tipo_tr = "001";
            String saldo = String.valueOf(mont) ;
            
            
            /*
            *actualiza el saldo en la tabla cuenta con el documento solicitado
            */
            try{
             String modificar = "update bancoXYZ.dbo.cuenta set [saldo] ="+mont+ "where documento_cliente ="+docum+";";
             Statement st = cin.createStatement();
             ResultSet rs = st.executeQuery(modificar);
             
             int registro = ps.executeUpdate();

            }
        catch(Exception e){
            //JOptionPane.showMessageDialog(null,"Ha ocurrido un error", "Error",JOptionPane.INFORMATION_MESSAGE);    
        }
            /*
            *agrega en la tabla movimientos, el movimiento realizado con los datos especificados
            */
                   try {  
                       
                String insertar = "insert into bancoXYZ.dbo.movimientos (id_transaccion,monto,tipo_transaccion,saldo_cuenta,tipo_documento,documento_cliente,id_cuenta)values (?,?,?,?,?,?,?)";
                ps = cin.prepareCall(insertar);
                ps.setString(1, dia+mes+annio+hora+minutos+segundos);               
                ps.setString(2, monto);
                ps.setString(3, tipo_tr);
                ps.setString(4, saldo);
                ps.setString(5, tipo_doc);
                ps.setString(6, docum);
                ps.setString(7, id_cuenta);
          
                int registro = ps.executeUpdate();
          
                    if(registro > 0){
                    JOptionPane.showMessageDialog(null, "Consignacion realizada","Accion exitosa",JOptionPane.INFORMATION_MESSAGE);
                    }
                       
                 }
        
        catch (Exception e){
          
          JOptionPane.showMessageDialog(null, "No se pudo realizar la transaccion","Error",JOptionPane.INFORMATION_MESSAGE);
        }
            
                   
      }
        /*
        Si la opcion seleccionada es R realiza el retiro con el monto recibido, en la cuenta 
        registrada al documento solicitado
        */
         if(opcion.equals ("R")){
            int inicio = str.indexOf(",");
            String docum = str.substring(0,inicio);
            String monto = str.substring(inicio+1,str.length()-1);
            salida.println(str);
            int mont = Integer.parseInt(monto);
            int canti;
            String tipo_doc = null;
            String id_cuenta=null;
            /*
            selecciona el saldo, tipo de documento, y el id de la cuenta de la tabla cuenta con el documento solicitado
            */
            try{
            String buscar ="select saldo,tipo_documento,id_cuenta from bancoXYZ.dbo.cuenta where documento_cliente = "+docum+" ;";
            Statement st = cin.createStatement();
            ResultSet rs = st.executeQuery(buscar);
            while(rs.next()){
                
               canti = Integer.parseInt(rs.getString("saldo"));
               if(mont<=canti){
               mont = canti - mont;}
               
               tipo_doc = rs.getString("tipo_documento");
               id_cuenta = rs.getString("id_cuenta");
            }
        }
        catch(Exception e){
            //JOptionPane.showMessageDialog(null,"Ha ocurrido un error", "Error",JOptionPane.INFORMATION_MESSAGE );
        } 
           
            Calendar ca = Calendar.getInstance();
            String dia = Integer.toString(ca.get(Calendar.DATE));
            String mes = Integer.toString(ca.get(Calendar.MONTH)+1);
            String annio=Integer.toString(ca.get(Calendar.YEAR));
            String hora =Integer.toString(ca.get(Calendar.HOUR_OF_DAY));
            String minutos = Integer.toString(ca.get(Calendar.MINUTE));
            String segundos = Integer.toString(ca.get(Calendar.SECOND));            
            String tipo_tr = "002";
            String saldo = String.valueOf(mont) ;
            
             /*
            *actualiza el saldo en la tabla cuenta con el documento solicitado
            */
            
            try{
             String modificar = "update bancoXYZ.dbo.cuenta set [saldo] ="+mont+ "where documento_cliente ="+docum+";";
             Statement st = cin.createStatement();
             ResultSet rs = st.executeQuery(modificar);
             
             int registro = ps.executeUpdate();

            }
        catch(Exception e){
            //JOptionPane.showMessageDialog(null,"Ha ocurrido un error", "Error",JOptionPane.INFORMATION_MESSAGE);    
        }
            /*
            agrega en movimientos, el movimiento realizado, con los datos especificados a continuación
            */
                   try {  
                       
                String insertar = "insert into bancoXYZ.dbo.movimientos (id_transaccion,monto,tipo_transaccion,saldo_cuenta,tipo_documento,documento_cliente,id_cuenta)values (?,?,?,?,?,?,?)";
                ps = cin.prepareCall(insertar);
                ps.setString(1, dia+mes+annio+hora+minutos+segundos);               
                ps.setString(2, monto);
                ps.setString(3, tipo_tr);
                ps.setString(4, saldo);
                ps.setString(5, tipo_doc);
                ps.setString(6, docum);
                ps.setString(7, id_cuenta);
          
                int registro = ps.executeUpdate();
          
                    if(registro > 0){
                    JOptionPane.showMessageDialog(null, "Retiro realizado","Accion exitosa",JOptionPane.INFORMATION_MESSAGE);
                    }
                       
                 }
        
        catch (Exception e){
          
          JOptionPane.showMessageDialog(null, "No se pudo realizar el retiro","Error",JOptionPane.INFORMATION_MESSAGE);
        }
            
                   
      }
       /*
         Si la opcion es S realiza una consulta del saldo con el documento solicitado
         */
        if(opcion.equals ("S")){
            // int inicio = str.indexOf("'");
            String docum = str.substring(0,str.length()-1);
            //String monto = str.substring(inicio+1,str.length()-1);
            int canti = 0;
            /*
            Consulta el saldo de la tabla cuenta, con el documento solicitado
            */
            try{
            String buscar ="select saldo from bancoXYZ.dbo.cuenta where documento_cliente = "+docum+";";
            Statement st = cin.createStatement();
            ResultSet rs = st.executeQuery(buscar);
            while(rs.next()){
                
               canti = Integer.parseInt(rs.getString("saldo"));
                salida1.println(canti);
                }
            }
            catch(Exception e){
            //JOptionPane.showMessageDialog(null,"Ha ocurrido un error", "Error",JOptionPane.INFORMATION_MESSAGE );
            }
      }
         
        
        //break;
        if (str.equals("Adios")) break;
      }
    
    }catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
        
}
}
