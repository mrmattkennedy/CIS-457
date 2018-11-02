package game;
//start screen where players join, main screen

//game starts,
//datagram packet to request player nums, get highest response.
//reuse components by replacing panel, then replacing. not necessary

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import player.Player;
import player.SocketListener;

public class MainGame extends JFrame implements ActionListener, DocumentListener {

	private JButton playGame;
	private JFrame frame;
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
	private int playerNum = 0;

	private static final int WIDTH = 650;
	private static final int HEIGHT = 450;

	public MainGame(MulticastSocket socket, InetAddress group) {
		cSocket = socket;
		this.group = group;
		getNumPlayers();
		Thread listener = new Thread(new SocketListener(socket, group, playerNum));
		listener.start();

		panelFont = new Font("Calibri", Font.PLAIN, 18);
		playerPanel = new JPanel();

		playerPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		playerPanel.setLayout(new GridLayout(4, 1, 5, 5));
		playerCount = new int[] { 1, 2, 3, 4 };

		playerNames = new JTextField[4];
		players = new JPanel[4];
		for (int i = 0; i < playerCount.length; i++) {
			playerNames[i] = new JTextField();
			playerNames[i].setPreferredSize(new Dimension(300, 30));
			players[i] = new JPanel();
			players[i].setLayout(new GridLayout(2, 1, 2, 2));
			players[i].setBorder(BorderFactory.createLineBorder(Color.black));

			JPanel tempPanel = new JPanel();
			JLabel label = new JLabel("Player" + playerCount[i]);

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

		controlPanel = new JPanel();

		controlPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		difficulty = new String[] { "Easy", "Medium", "Hard", "Really Hard", "Movies", "People" };
		numRounds = new String[10];
		for (int i = 0; i < numRounds.length; i++)
			numRounds[i] = Integer.toString(i + 1);

		difficultyChooser = new JComboBox<String>(difficulty);
		numRoundsChooser = new JComboBox<String>(numRounds);

		goBtn = new JButton("GO");
		cancelBtn = new JButton("CANCEL");

		goBtn.addActionListener(this);

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

		this.setSize(new Dimension(WIDTH + 50, HEIGHT + 75));
		this.setLayout(new GridBagLayout());
		setLayout();
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
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
	
	private void getNumPlayers() {
		sendMessageToPlayers("count", 500);
		while (true) {
			try {
			    byte[] buffer = new byte[1000];
			    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
			    cSocket.receive(datagram);
			    String message = new String(datagram.getData());
			    System.out.println(message);
			    if (message.contains("received"))
			    	playerNum++;
			} catch (IOException e) {
				try {
					cSocket.setSoTimeout(100000);
				} catch (SocketException e1) {
					break;
				}
				break;
			}
		}
		System.out.println(playerNum);
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

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();

		if (source == goBtn) {
			Player[] playersInfo = new Player[4];
			for (int i = 0; i < playerNames.length; i++) {
				String playerName = playerNames[i].getText();
				if (!playerName.isEmpty())
					playersInfo[i] = new Player(i, playerName);
			}

			new DrawScreen(playersInfo);
			this.dispose();
		}

	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		sendMessageToPlayers("updateTextField" + playerNum + " " + playerNames[playerNum].getText(), 1000);
	}

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
