package player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import game.MainGame;

public class SocketListener implements Runnable {
	
	private MulticastSocket cSocket;
	private InetAddress group;
	private boolean clientQuit = false;
	private int playerNum;
	private final static String receivedCode = "updateTextField";
	private final static String disconnectCode = "disconnect";
	private final static String readyCode = "ready";
	private final static String unreadyCode = "unready";
	private final static String startCode = "start";
	private final static String numReadyCode = "numReady";
	private final static String newPlayerCode = "newPlayer";
	private final static String addPlayerCode = "addPlayer";
	private MainGame player;
	
	public SocketListener(MulticastSocket socket, InetAddress group, int playerNum, MainGame player) {
		cSocket = socket;
		this.group = group;
		this.playerNum = playerNum;
		this.player = player;
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
			    	sendMessageToPlayers(playerNum + "received " + player.getUsername());
			    	
			    } else if (message.startsWith(receivedCode)) {
			    	int playerNumToUpdate = Character.getNumericValue(message.charAt(message.indexOf(receivedCode) + receivedCode.length()));
			    	if (playerNumToUpdate != playerNum) {
			    		String text = message.substring(message.indexOf(receivedCode) + receivedCode.length() + 2);
			    		player.updateTextForPlayer(playerNumToUpdate, text);
			    	}
			    } else if (message.startsWith(readyCode)) {
			    	player.changeNumReady(1);
			    	player.updateGoBtn();
			    	
		
			    } else if (message.startsWith(unreadyCode)) {
			    	player.changeNumReady(-1);
		    		player.updateGoBtn();
		    		
			    } else if (message.startsWith(startCode)) {
			    	player.startGame();
			    	
			    } else if (message.startsWith(newPlayerCode)) {
			    	player.resetPlayerCount();
			    	player.resetReadyCount();
			    	System.out.println(playerNum + ", 0:0");;
			    	sendMessageToPlayers(addPlayerCode + playerNum);
			    	
			    	
			    } else if (message.startsWith(addPlayerCode)) {
			    	/*check player num necessary. Consider the following:
			    	 * player 0 is ready, then player 1 joins.
			    	 * player 1 sends newPlayerCode, which is sent to group.
			    	 * That in turn sends out addPlayerCode (this) to everyone in group.
			    	 * player 0 and 1 both get addPlayerCode, twice per, so 4 times.
			    	 * Have to check if the player code is not the same, 
			    	 * so each player is only added once per.
			    	 */
			    	player.changePlayerCount(1);
			    	int playerNumSent = Character.getNumericValue(message.charAt(message.indexOf(addPlayerCode) + addPlayerCode.length()));
			    	if (player.getPlayerReady() && playerNumSent != playerNum)
			    		sendMessageToPlayers(readyCode);			    	
			    	player.updateGoBtn();
			    	
			    	
			    } else if (message.startsWith(numReadyCode)) {
			    	player.changeNumReady(-1);
		    		player.updateGoBtn();
			    		
			    	
			    } else if (message.startsWith(disconnectCode)) {
			    	int playerNumToUpdate = Character.getNumericValue(message.charAt(message.indexOf(disconnectCode) + disconnectCode.length()));
			    	if (playerNumToUpdate != playerNum) {
			    		player.updateTextForPlayer(playerNumToUpdate, "");
			    	}
			    	player.changePlayerCount(-1);
			    	player.updateGoBtn();
			    }
			} catch (SocketTimeoutException e) {
				player.disconnect();
			} catch (SocketException e) {
				try {
					cSocket.setSoTimeout(100000);
				} catch (SocketException e1) {
					continue;
				}
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
