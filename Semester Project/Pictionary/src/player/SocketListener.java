package player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import game.DrawScreen;
import game.MainGame;

public class SocketListener implements Runnable {
	
	private MulticastSocket cSocket;
	private InetAddress group;
	private boolean clientQuit = false;
	private int playerNum;
	private int numPlayers = 0;
	private final static String receivedCode = "updateTextField";
	private final static String disconnectCode = "disconnect";
	private final static String readyCode = "ready";
	private final static String unreadyCode = "unready";
	private final static String startCode = "start";
	private final static String numReadyCode = "numReady";
	private final static String newPlayerCode = "newPlayer";
	private final static String addPlayerCode = "addPlayer";
	private final static String difficultyCode = "difficulty";
	private final static String numRoundsCode = "rounds";
	private final static String newMessageCode = "newMessage";
	private final static String updateDrawingCode = "updateDrawing";
	private final static String clearScreenCode = "clearScreen";
	private final static String nextDrawerCode = "nextDrawer";
	private final static String updateTimeCode = "updateTime";
	private MainGame player;
	private DrawScreen screen;
	
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
			    	screen = player.startGame();
			    	if (screen == null)
			    		System.exit(1);
			    	
			    } else if (message.startsWith(difficultyCode)) {
			    	if (playerNum != 0) {
			    		int index = Character.getNumericValue(message.charAt(message.indexOf(difficultyCode) + difficultyCode.length()));
			    		player.updateDifficulty(index);
			    	}
			    	
			    } else if (message.startsWith(numRoundsCode)) {
			    	if (playerNum != 0) {
			    		int index = Character.getNumericValue(message.charAt(message.indexOf(numRoundsCode) + numRoundsCode.length()));
			    		player.updateNumRounds(index);
			    	}
			    	
			    } else if (message.startsWith(newPlayerCode)) {
			    	player.resetPlayerCount();
			    	player.resetReadyCount();
			    	sendMessageToPlayers(addPlayerCode + playerNum);
			    	if (playerNum == 0) {
			    		sendMessageToPlayers(difficultyCode + player.getDifficultyIndex());
			    		sendMessageToPlayers(numRoundsCode + player.getRoundsIndex());
			    	}			    	
			    	
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
			    		
		    	//screen section
			    } else if (message.startsWith(newMessageCode)) {
//			    	System.out.println(message.indexOf(newMessageCode) + " ::: " + message.indexOf);
			    	screen.updateLog(message.substring(message.indexOf(newMessageCode) + newMessageCode.length()));
			    	
			    } else if (message.startsWith(updateDrawingCode)) {
			    	int playerNumSent = Character.getNumericValue(message.charAt(message.indexOf(updateDrawingCode) + updateDrawingCode.length()));
			    	if (playerNumSent != playerNum) {
			    		String x1 = message.substring(message.indexOf("_") + 1, message.indexOf(","));
			    		String y1 = message.substring(message.indexOf(x1) + x1.length() + 1, 
			    				message.indexOf(",", message.indexOf(x1) + x1.length() + 1));
			    		
			    		String x2 = message.substring(message.indexOf(y1) + y1.length() + 1, 
			    				message.indexOf(",", message.indexOf(y1) + y1.length() + 1));
			    		//y1 is part of x1, blows up.
			    		String y2 = message.substring(message.indexOf(x2, message.indexOf(y1)) + x2.length() + 1,
			    				message.indexOf(",", message.indexOf(x2, message.indexOf(y1)) + x2.length() + 1));
//			    		System.out.println(x1 + ", " + y1 + ", " + x2 + ", " + y2);
			    		screen.addDrawingToPanel(Integer.parseInt(x1),
			    				Integer.parseInt(y1),
			    				Integer.parseInt(x2),
			    				Integer.parseInt(y2));
			    	}
			    	
			    } else if (message.startsWith(clearScreenCode)) {
			    	screen.clearScreen();
			    	
			    } else if (message.startsWith(nextDrawerCode)) {
			    	int playerNumSent = Character.getNumericValue(message.charAt(message.indexOf(updateDrawingCode) + updateDrawingCode.length()));
			    	if (playerNumSent == playerNum)
			    		screen.updateCurrentDrawer();
			    	
			    } else if (message.startsWith(updateTimeCode)) {
			    	String time = message.substring(message.indexOf(updateTimeCode) + updateTimeCode.length());
			    	screen.updateTimerLabel(time);
			    	
			    } else if (message.startsWith(disconnectCode)) {
			    	int playerNumToUpdate = Character.getNumericValue(message.charAt(message.indexOf(disconnectCode) + disconnectCode.length()));
			    	if (playerNumToUpdate != playerNum) {
			    		player.updateTextForPlayer(playerNumToUpdate, "");
			    	}
			    	numPlayers--;
			    	player.changePlayerCount(-1);
			    	player.updateGoBtn();
			    }
			} catch (SocketTimeoutException e) {
				try {
					cSocket.setSoTimeout(100000);
				} catch (SocketException e1) {
					continue;
				}
				
			} catch (SocketException e) {
				player.disconnect();
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


