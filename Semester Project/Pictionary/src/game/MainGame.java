package game;
//start screen where players join, main screen

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import player.SocketListener;

//MainGame screen where the game is setup.
public class MainGame extends JFrame implements ActionListener, DocumentListener {

	private JPanel playerPanel;
	private JPanel controlPanel;
	private JPanel[] players;
	private JTextField[] playerNames;
	private int[] playerCount;
	private Font panelFont;
	private String[] difficulty;
	private String[] numRounds;
	private JComboBox<String> difficultyChooser;
	private JComboBox<String> numRoundsChooser;
	private JButton goBtn;
	private JButton cancelBtn;
	private MulticastSocket cSocket;
	private InetAddress group;
	private int playerNum = -1;
	private int numPlayers = 0;
	private int numReadyPlayers = 0;
	private Thread listener;
	private boolean playerReady = false;
	private String goBtnStatus = "READY";
	private DrawScreen screen;
	private String difficultySelected = "Easy";
	private int numRoundsSelected = 1; 
	
	private static final int WIDTH = 650;
	private static final int HEIGHT = 450;

	public MainGame(MulticastSocket socket, InetAddress group) {
		//Socket and group already established.
		cSocket = socket;
		this.group = group;
		screen = null;
		String[] temp = getPlayerInfo();
		listener = new Thread(new SocketListener(socket, group, playerNum, this));
		listener.start();

		panelFont = new Font("Calibri", Font.PLAIN, 18);
		playerPanel = new JPanel();

		playerPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		playerPanel.setLayout(new GridLayout(4, 1, 5, 5));
		playerCount = new int[] { 1, 2, 3, 4 };

		playerNames = new JTextField[playerCount.length];
		players = new JPanel[playerCount.length];
		//Create the common details between each "player" section.
		for (int i = 0; i < playerCount.length; i++) {
			playerNames[i] = new JTextField();
			playerNames[i].setPreferredSize(new Dimension(300, 30));
			players[i] = new JPanel();
			players[i].setLayout(new GridLayout(2, 1, 2, 2));
			players[i].setBorder(BorderFactory.createLineBorder(Color.black));

			JPanel tempPanel = new JPanel();
			JLabel label = new JLabel("Player" + (playerCount[i] - 1));

			// Add player label to top
			label.setFont(panelFont);
			tempPanel.add(label);
			players[i].add(tempPanel);

			// Add username label and text field to bottom
			tempPanel = new JPanel();
			label = new JLabel("Username");
			label.setFont(panelFont);
			tempPanel.add(label);
			tempPanel.add(playerNames[i]);
			
			if (i != playerNum)
				playerNames[i].setEnabled(false);				

			players[i].add(tempPanel);
			playerPanel.add(players[i]);
		}
		playerNames[playerNum].getDocument().addDocumentListener(this);
		for (int i = 0; i < playerNames.length; i++)
			if (temp[i] != null)
				playerNames[i].setText(temp[i]);

		controlPanel = new JPanel();

		controlPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		//Available difficulties.
		difficulty = new String[] { "Easy", "Medium"};//, "Hard", "Really Hard", "Movies", "People" };
		numRounds = new String[10];
		for (int i = 0; i < numRounds.length; i++)
			numRounds[i] = Integer.toString(i + 1);

		difficultyChooser = new JComboBox<String>(difficulty);
		numRoundsChooser = new JComboBox<String>(numRounds);

		goBtn = new JButton("GO");
		cancelBtn = new JButton("CANCEL");

		goBtn.addActionListener(this);
		cancelBtn.addActionListener(this);

		JPanel tempPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(3, 1, 15, 15));
		tempPanel = new JPanel();
		tempPanel.setLayout(new GridLayout(2, 1, 25, 25));
		tempPanel.add(new JLabel("Difficulty: "));
		tempPanel.add(difficultyChooser);
		controlPanel.add(tempPanel);

		tempPanel = new JPanel();
		tempPanel.setLayout(new GridLayout(2, 1, 25, 25));
		tempPanel.add(new JLabel("Number of rounds: "));
		tempPanel.add(numRoundsChooser);
		controlPanel.add(tempPanel);

		tempPanel = new JPanel();
		tempPanel.setLayout(new GridLayout(2, 1, 25, 25));
		tempPanel.add(goBtn);
		tempPanel.add(cancelBtn);
		controlPanel.add(tempPanel);
	
		if (playerNum != 0) {
			difficultyChooser.setEnabled(false);
			numRoundsChooser.setEnabled(false);
		} else {
			difficultyChooser.addActionListener(this);
			numRoundsChooser.addActionListener(this);
		}
		
		sendMessageToPlayers("newPlayer" + playerNum, 100000);

		//Add window listener to check if player disconnects.
		this.addWindowListener(new ExitListener(this));
		this.setSize(new Dimension(WIDTH + 50, HEIGHT + 75));
		this.setLayout(new GridBagLayout());
		setLayout();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setVisible(true);
	}

	private void setLayout() {
		GridBagConstraints gbCons = new GridBagConstraints();
		gbCons.fill = GridBagConstraints.HORIZONTAL;

		gbCons.insets = new Insets(10, 5, 10, 5);
		gbCons.gridx = 0;
		gbCons.gridy = 0;
		gbCons.weightx = 0.7;
		gbCons.weighty = 1;
		gbCons.ipady = (int) (HEIGHT);
		this.add(playerPanel, gbCons);

		gbCons.gridx = 2;
		gbCons.gridy = 0;
		gbCons.ipady = (int) (0.30 * HEIGHT);
		gbCons.weightx = 0.2;
		gbCons.weighty = 1;
		gbCons.ipady = (int) (HEIGHT);
		this.add(controlPanel, gbCons);

	}
	
	//Retrieves info of players already in session.
	private String[] getPlayerInfo() {
		sendMessageToPlayers("count", 500);
		String[] temp = new String[4];
		while (true) {
			try {
			    byte[] buffer = new byte[1000];
			    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
			    cSocket.receive(datagram);
			    String message = new String(datagram.getData());
			    if (message.contains("received")) {
			    	int playerNum = Integer.parseInt(message.substring(message.indexOf("received") - 1, 1));
			    	temp[playerNum] = message.substring(message.indexOf("received") + "received ".length());
			    }
			} catch (IOException e) {
				try {
					cSocket.setSoTimeout(100000);
				} catch (SocketException e1) {
					break;
				}
				break;
			}
		}
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] == null) {
				playerNum = i;
				break;
			}
		}
		if (playerNum == -1) {
			System.out.println("All spots full.");
			System.exit(1);
		}
		System.out.println(playerNum);
		return temp;
	}

	public void sendMessageToPlayers(String message, int timeout) {
		byte[] buf = (message).getBytes();
		DatagramPacket dg = new DatagramPacket(buf, buf.length, group, 6789);
		try {
			cSocket.send(dg);
			cSocket.setSoTimeout(timeout);
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}
	
	public String getUsername() {
		return playerNames[playerNum].getText();
	}
	
	
	public void changeNumReady(int numReady) {
		numReadyPlayers += numReady;
	}
	
	public void resetReadyCount() {
		System.out.println("reset ready " + playerNum);
		numReadyPlayers = 0;
	}
	
	
	public void changePlayerCount(int playerCount) {
		numPlayers += playerCount;
	}
	
	public void resetPlayerCount() {
		System.out.println("reset players " + playerNum);
		numPlayers = 0;
	}
	
	public boolean getPlayerReady() {
		return playerReady;
	}
	
	public void updateTextForPlayer(int playerToChange, String text) {
		playerNames[playerToChange].setText(text);
	}
	
	public void updateGoBtn() {
		goBtn.setText(goBtnStatus + " (" + numReadyPlayers + "/" + numPlayers + ")");
	}
	
	public void updateDifficulty(int index) {
		difficultySelected = difficultyChooser.getItemAt(index);
		System.out.println("Difficulty is " + difficultySelected);
		difficultyChooser.setSelectedIndex(index);
	}
	
	public int getDifficultyIndex() {
		return difficultyChooser.getSelectedIndex();
	}
	
	public void updateNumRounds(int index) {
		System.out.println("Index is " + index);
		numRoundsSelected = index + 1;
		numRoundsChooser.setSelectedIndex(index);
	}
	
	public int getRoundsIndex() {
		return numRoundsChooser.getSelectedIndex();
	}
	
	//Player has closed window, disconnect.
	public void disconnect() {
		if (playerReady) 
			sendMessageToPlayers("unready", 100000);
		
		sendMessageToPlayers("disconnect" + playerNum, 1000);
		try {
			cSocket.leaveGroup(group);
			listener.stop();
		} catch (IOException e) {
			System.exit(0);
		}
		cSocket.close();
	}
	
	//All players are ready.
	public DrawScreen startGame() {
		//Invoke and wait necessary for concurrency in EDT thread for Swing utilities.
		//Can't use invokeLater due to screen being assigned, so the run block will be skipped
		//and screen will be null.
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					System.out.println("Starting");
					dispose();
					String topic = difficultyChooser.getSelectedItem().toString();
					File temp = null;
					switch (topic) {
						case "Easy":
							temp = new File("easyWords");
							break;
						case "Medium":
							temp = new File("medWords");
							break;
					}
					
					int numRounds = numRoundsChooser.getSelectedIndex() + 1;
					if (temp != null)
						screen = new DrawScreen(playerNames[playerNum].getText(), cSocket, group, playerNum, listener, numPlayers, temp, numRounds);
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return screen;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		
		if (source == goBtn) {
			if (playerReady) {
				goBtnStatus = "READY";
				goBtn.setBackground(Color.GRAY);
				playerReady = !playerReady;
				sendMessageToPlayers("unready" + playerNum, 100000);
			} else if (!playerReady) {
				goBtnStatus = "UNREADY";
				goBtn.setBackground(Color.GREEN);
				playerReady = !playerReady;
				sendMessageToPlayers("ready" + playerNum, 100000);
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				;
			}
			System.out.println(numReadyPlayers + " : " +  numPlayers);
			if (numReadyPlayers == numPlayers && numPlayers > 1)
				sendMessageToPlayers("start", 100000);
				
		} else if (source == cancelBtn) {
			disconnect();
			this.dispose();
		} else if (source == difficultyChooser) {
			System.out.println("selected is " + difficultyChooser.getSelectedIndex());
			sendMessageToPlayers("difficulty" + difficultyChooser.getSelectedIndex(), 100000);
		} else if (source == numRoundsChooser) {
			for (int i = 0; i < 5; i++)
				sendMessageToPlayers("rounds" + numRoundsChooser.getSelectedIndex(), 100000);
		}

	}

	//These listen for changes in the text field.
	@Override
	public void changedUpdate(DocumentEvent arg0) { ; }

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		System.out.println("insert");
		sendMessageToPlayers("updateTextField" + playerNum + " " + playerNames[playerNum].getText(), 1000);	
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		sendMessageToPlayers("updateTextField" + playerNum + " " + playerNames[playerNum].getText(), 1000);
	}
}
