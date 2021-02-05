package servidor;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import javax.swing.JOptionPane;


public class Servidor {  
  public static final int PORT = 4444;//se define el puerto por el cual se esuchará al cliente
  public static void main(String[] args) throws IOException {
    ServerSocket socketServidor = null; //inicializa el socket
    try {
      socketServidor = new ServerSocket(PORT);//intenta la escucha en el puerto establecido
      int idSession=0;
      while(true){
          Socket socket;
          socket = socketServidor.accept();
          System.out.println("nueva conexion entrante: "+socket);
          ((HiloServer)new HiloServer(socket, idSession)).start();
      }
    } catch (IOException e) {
      System.out.println("No puede escuchar en el puerto: " + PORT);//si ocurre un error avisa que no se puede escuchar en el puerto
      //posiblemente está siendo utilizado por otro programa
      System.exit(-1);//devuelve -1 si lo anterior ocurre
    }
  } 
}


