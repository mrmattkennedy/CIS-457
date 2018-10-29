import java.net.*; 
import java.io.*; 
import java.util.*;

public class ChatServer { 

   public ChatServer (int port) throws IOException { 
      ServerSocket server = new ServerSocket (port); 
      while (true) { 
         Socket client = server.accept(); 
         DataInputStream in = new DataInputStream(client.getInputStream());
         String name = in.readUTF();

         System.out.println ("New client "+name+" from " + client.getInetAddress().getHostAddress());
         ChatHandler c = new ChatHandler (name, client); 
         c.start (); 
      } 
   }
  
   public static void main (String args[]) throws IOException { 
      if (args.length != 1) 
         throw new RuntimeException ("Syntax: java ChatServer <port>"); 
      new ChatServer (Integer.parseInt (args[0])); 
   }
}
