package player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SocketListener implements Runnable {
	
	private MulticastSocket cSocket;
	private InetAddress group;
	private boolean clientQuit = false;
	
	public SocketListener(MulticastSocket socket, InetAddress group) {
		cSocket = socket;
		this.group = group;
	}

	@Override
	public void run() {
		sendTextToChat("test1");
		while (!clientQuit) {
			try {
			    byte[] buffer = new byte[1000];
			    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
			    cSocket.receive(datagram);
			    String message = new String(datagram.getData());
			    System.out.println(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void sendTextToChat(String message) {
	      message = message+"\n";
	      byte[] buf = (message).getBytes();
	      DatagramPacket dg = new DatagramPacket(buf, buf.length, group, 6789);
	      try { 
	    	  cSocket.send(dg);
	      }
	      catch (IOException ex) { 
	         System.out.println(ex);
	      }
	   }
	
}
