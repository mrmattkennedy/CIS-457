package player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class SocketListener implements Runnable {
	
	private MulticastSocket cSocket;
	private InetAddress group;
	private boolean clientQuit = false;
	private int playerNum;
	private final String receivedCode = "updateTextField";
	
	public SocketListener(MulticastSocket socket, InetAddress group, int playerNum) {
		cSocket = socket;
		this.group = group;
		this.playerNum = playerNum;
	}

	@Override
	public void run() {
		while (!clientQuit) {
			try {
			    byte[] buffer = new byte[1000];
			    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
			    cSocket.receive(datagram);
			    String message = new String(datagram.getData());
			    System.out.println(message);
			    
			    if (message.contains("count")) {
			    	sendMessageToPlayers("received");
			    } else if (message.startsWith(receivedCode)) {
			    	int playerNumToUpdate = Character.getNumericValue(message.charAt(message.indexOf(receivedCode) + receivedCode.length()));
			    	if (playerNumToUpdate != playerNum) {
			    		
			    	}
			    }
			} catch (SocketTimeoutException e) {
				continue;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
	public void sendMessageToPlayers(String message) {
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
