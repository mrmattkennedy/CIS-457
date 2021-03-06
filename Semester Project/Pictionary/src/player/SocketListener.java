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

//Class is used on a separate thread to listen for a player and interact with a group.
public class SocketListener implements Runnable {
	
	private MulticastSocket cSocket;
	private InetAddress group;
	private boolean clientQuit = false;
	private int playerNum;
	private int numPlayers = 0;
	
	//All codes the Socket will receive.
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
	private final static String updateTopicCode = "updateTopic";
	private final static String guessCorrectCode = "guessCorrect";
	private final static String showTopicCode = "showTopic";
	private final static String gameOverCode = "gameOver";
	private final static String getPointsCode = "getPoints";
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
				//Get the message.
			    byte[] buffer = new byte[1000];
			    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
			    cSocket.receive(datagram);
			    String message = new String(datagram.getData());
			    System.out.println(message);
			    
			    //Used with new players.
			    if (message.contains("count")) {
			    	sendMessageToPlayers(playerNum + "received " + player.getUsername());
			    	
			    	//Updating text field.
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
			    		int difficulty = Character.getNumericValue(message.charAt(message.indexOf(difficultyCode) + difficultyCode.length()));
			    		player.updateDifficulty(difficulty);
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

			    		String y1 = message.substring(message.indexOf(",") + 1, message.indexOf(",",
			    				message.indexOf(",") + 1));         

			    		String x2 = message.substring(message.indexOf(",", message.indexOf(",") + 1) + 1,
			    				message.indexOf(",", message.indexOf(",", message.indexOf(",") + 1) + 1));        

			    		String y2 = message.substring(message.indexOf(",", message.indexOf(",", message.indexOf(",") + 1) + 1) + 1, 
			    				message.indexOf(",", message.indexOf(",", message.indexOf(",", message.indexOf(",") + 1) + 1) + 1));
			    		
//			    		System.out.println(x1 + ", " + y1 + ", " + x2 + ", " + y2);
			    		screen.addDrawingToPanel(Integer.parseInt(x1),
			    				Integer.parseInt(y1),
			    				Integer.parseInt(x2),
			    				Integer.parseInt(y2));
			    	}
			    	
			    } else if (message.startsWith(clearScreenCode)) {
			    	System.out.println("clearing screen");
			    	screen.clearScreen();
			    	
			    } else if (message.startsWith(nextDrawerCode)) {
			    	int playerNumSent = Character.getNumericValue(message.charAt(message.indexOf(nextDrawerCode) + nextDrawerCode.length()));
			    	if (playerNumSent == playerNum || playerNumSent == 5)
			    		screen.updateCurrentDrawer();
			    	
			    } else if (message.startsWith(updateTimeCode)) {
			    	String time = message.substring(message.indexOf(updateTimeCode) + updateTimeCode.length());
			    	screen.updateTimerLabel(time);
			    	
			    } else if (message.startsWith(updateTopicCode)) {
			    	String topic = message.substring(message.indexOf(updateTopicCode) + updateTopicCode.length()).trim();
			    	screen.updateCurrentTopic(topic);
			    	
			    } else if (message.startsWith(guessCorrectCode)) {
			    	screen.updateTime();
			    	
			    } else if (message.startsWith(showTopicCode)) {
			    	screen.showCorrectAnswer();
			    	
			    } else if (message.startsWith(gameOverCode)) {
			    	int playerNumSent = Character.getNumericValue(message.charAt(message.indexOf(gameOverCode) + gameOverCode.length()));
			    	if (playerNumSent == playerNum)
			    		sendMessageToPlayers(getPointsCode + playerNum + screen.getNumPoints());
			    	
			    } else if (message.startsWith(getPointsCode)) {
			    	int playerNumSent = Character.getNumericValue(message.charAt(message.indexOf(getPointsCode) + getPointsCode.length()));
			    	int points = Character.getNumericValue(message.charAt(message.indexOf(getPointsCode) + getPointsCode.length() + 1));
			    	screen.updateLog("Player " + playerNumSent + ": " + points);
			    	
			    } else if (message.startsWith(disconnectCode)) {
			    	int playerNumToUpdate = Character.getNumericValue(message.charAt(message.indexOf(disconnectCode) + disconnectCode.length()));
			    	numPlayers--;
			    	if (screen == null) {
			    		player.changePlayerCount(-1);
				    	player.updateGoBtn();
				    	if (playerNumToUpdate != playerNum)
				    		player.updateTextForPlayer(playerNumToUpdate, "");
			    	} else {
			    		screen.playerLeft(playerNumToUpdate);
			    	}
			    }
			    //Sockets timeout fairly frequently, if no message is given.
			    //Resets timeout so exception is handled.
			} catch (SocketTimeoutException e) {
				try {
					cSocket.setSoTimeout(100000);
				} catch (SocketException e1) {
					continue;
				}
				
				//Issue with socket = disconnect.
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


