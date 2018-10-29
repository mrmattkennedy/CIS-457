import java.net.*;
import java.io.*;

public class ChatClient {
	
   ChatFrame gui;
   String name;
  
   InetAddress group;
   MulticastSocket socket;
   int port = 6789;
  
   public ChatClient(String name) {
      this.name = name;

      // GUI Create GUI and handle events:
      // After text input, sendTextToChat() is called,
      // When closing the window, disconnect() is called. 

      gui = new ChatFrame("Chat with IP-Multicast");
      gui.input.addKeyListener (new EnterListener(this,gui));
      gui.addWindowListener(new ExitListener(this));
     
// In order for a host to receive a multicast data, 
// the receiver must register with a group, by creating a MulticastSocket on a port and call the joinGroup() method.

      try {
         socket = new MulticastSocket(port);
         group = InetAddress.getByName("226.1.3.5");
         socket.joinGroup(group);
         gui.output.append("Connected...\n");

 // waiting for and receiving messages 

         while (true) {
	    byte[] buffer = new byte[1000];
	    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
	    socket.receive(datagram);
	    String message = new String(datagram.getData());
	    gui.output.append(message);
         }
      } catch (IOException e) { 
         e.printStackTrace(); 
      }
   }
  
   public void sendTextToChat(String message) {
      message = name+": "+message+"\n";
      byte[] buf = (message).getBytes();
      DatagramPacket dg = new DatagramPacket(buf, buf.length, group, 6789);
      try { 
         socket.send(dg);
      }
      catch (IOException ex) { 
         System.out.println(ex);
      }
   }

   public void disconnect() {}

   public static void main(String args[]) {
      if (args.length!=1) 
         throw new RuntimeException ("Syntax: java ChatClient <name>"); 
      ChatClient client = new ChatClient(args[0]);
   }
}

