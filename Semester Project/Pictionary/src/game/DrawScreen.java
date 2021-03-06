package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.StyleConstants;


//GUI that holds the actual pictionary game and draw screen.
public class DrawScreen implements ActionListener {

	private JFrame frame;
	private DrawPanel drawArea;
	private JLabel topic;
	private JPanel guessPanel;
	private JPanel guessPanelControls;
	private int numPoints = 0;
	private int currRound = 0;
	private int numRounds;

	private String playerName;
	private JTextPane guessLog;
	private JScrollPane pane;
	private JTextField guessText;
	private JButton guessBtn;
	private static final int WIDTH = 650;
	private static final int HEIGHT = 450;
	private MulticastSocket cSocket;
	private InetAddress group;
	private Thread listener;
	private int playerNum;
	private int numPlayers;
	private int currentDrawer = -1;

	private JPanel drawControlsPanel;
	private JButton clearBtn;
	private JLabel timerLabel;
	private static int time = 0;
	
	private File three = new File("3.txt");
	private File two = new File("2.txt");
	private File one = new File("1.txt");
	private File topicWordsFile;
	private String currentTopic;
	private List<String> topicWords;
	private boolean gameOver = false;
	
//	ArrayList<String> temp = new ArrayList<String>();

	public DrawScreen(String playerName, 
			MulticastSocket socket, 
			InetAddress group, 
			int playerNum, 
			Thread listener, 
			int numPlayers, 
			File topicWordsFile,
			int numRounds) {
		
		//All of these established prior.
		this.cSocket = socket;
		this.group = group;
		this.listener = listener;
		this.playerNum = playerNum;
		this.numPlayers = numPlayers;
		this.topicWordsFile = topicWordsFile;
		this.numRounds = numRounds;
		
		System.out.println("Player num is" + playerNum);
		drawArea = new DrawPanel(this);
		topic = new JLabel("HIDDEN");
		drawArea.add(topic);
		drawArea.setBorder(BorderFactory.createLineBorder(Color.black));
		guessPanel = new JPanel();
		guessPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		frame = new JFrame();

		frame.setSize(new Dimension(WIDTH + 50, HEIGHT + 75));
		frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.addWindowListener(new ExitListener(this));
		setLayout();

		guessPanelControls = new JPanel();
		guessText = new JTextField();
		guessText.setPreferredSize(new Dimension(90, 25));
		guessBtn = new JButton("GUESS");
		guessBtn.setPreferredSize(new Dimension(70, 25));
		guessBtn.setFont(new Font("Calibri", Font.PLAIN, 10));
		guessBtn.addActionListener(this);

		guessPanelControls.add(guessText);
		guessPanelControls.add(guessBtn);

		guessLog = new JTextPane();
		guessLog.setEditable(false);

		pane = new JScrollPane(guessLog);
		pane.setBackground(Color.white);
		pane.setBorder(BorderFactory.createLineBorder(Color.black));

		drawControlsPanel = new JPanel();
		clearBtn = new JButton("CLEAR");
		clearBtn.setFont(new Font("Calibri", Font.PLAIN, 10));
		clearBtn.setPreferredSize(new Dimension(70, 25));
		clearBtn.addActionListener(this);
		timerLabel = new JLabel("Time: ");
		timerLabel.setPreferredSize(new Dimension(90, 25));

		drawControlsPanel.add(timerLabel);
		drawControlsPanel.add(clearBtn);
//		guessLog.setContentType("text/html");
		guessPanel.setLayout(new GridBagLayout());
		this.playerName = playerName;
		setGuessPanelLayout();
		initializeFiles();
		initializetopicWordsFile();
		sendMessageToPlayers("nextDrawer" + playerNum);
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
		frame.add(drawArea, gbCons);

		gbCons.gridx = 2;
		gbCons.gridy = 0;
		gbCons.ipady = (int) (0.30 * HEIGHT);
		gbCons.weightx = 0.2;
		gbCons.weighty = 1;
		gbCons.ipady = (int) (HEIGHT);
		frame.add(guessPanel, gbCons);
	}

	private void setGuessPanelLayout() {
		GridBagConstraints gbCons = new GridBagConstraints();
		gbCons.fill = GridBagConstraints.VERTICAL;

		gbCons.insets = new Insets(10, 5, 10, 5);
		gbCons.fill = GridBagConstraints.BOTH;
		gbCons.gridx = 0;
		gbCons.gridy = 0;
		gbCons.weightx = 1;
		gbCons.weighty = 0.76;
		gbCons.gridwidth = 1;
		gbCons.ipady = (int) (HEIGHT);
		guessPanel.add(drawControlsPanel, gbCons);

		gbCons.gridx = 0;
		gbCons.gridy = 1;
		gbCons.gridwidth = 2;
		gbCons.weightx = 1;
		gbCons.weighty = 0.2;
		gbCons.ipady = (int) (HEIGHT);
		guessPanel.add(pane, gbCons);

		gbCons.gridx = 0;
		gbCons.gridy = 2;
		gbCons.weightx = 1;
		gbCons.weighty = 0.76;
		gbCons.gridwidth = 1;
		gbCons.ipady = (int) (HEIGHT);
		guessPanel.add(guessPanelControls, gbCons);
	}

	//Updating a drawing point for a panel.
	public void updatePanel(int x1, int y1, int x2, int y2) {
		sendMessageToPlayers("updateDrawing" + playerNum + "_" + x1 + "," + y1 + "," + x2 + "," + y2 + ",");
	}

	public void addDrawingToPanel(int x1, int y1, int x2, int y2) {
		drawArea.updateDrawing(x1, y1, x2, y2);
	}
	
	public void updateTimerLabel(String time) {
		timerLabel.setText("Time: " + time);
	}
	
	//Timer updated.
	public void updateTime() {
		guessBtn.setEnabled(false);
		guessText.setEnabled(false);
		DrawScreen.time = 0;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		//Player made a guess, check if right, end round if so.
		if (source == guessBtn) {
			sendMessageToPlayers("newMessage" + playerName + ": " + guessText.getText());
			if (checkIfGuessTrue(guessText.getText())) {
				sendMessageToPlayers("guessCorrect");
				numPoints++;
			}
			guessText.setText("");
		} else if (source == clearBtn) {
			sendMessageToPlayers("clearScreen");
		}
	}
	
	//Load in the 3-2-1 at the beginning of each round.
	private void initializeFiles() {

		three = new File("3.txt");
		two = new File("2.txt");
		one = new File("1.txt");
	}

	private void initializetopicWordsFile() {
		try {
			topicWords = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(topicWordsFile));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	topicWords.add(line);
		    }
		    br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void updateCurrentTopic(String topic) {
		currentTopic = topic;
	}
	
	public int getNumPoints() {
		gameOver = true;
		return numPoints;
	}
	
	public void updateCurrentDrawer() {
		//Checks if all rounds played. Ends game if so.
		if (++currRound > numRounds) {
			guessLog.setText("");
			guessBtn.setEnabled(false);
			guessText.setEnabled(false);
			sendMessageToPlayers("gameOver" + playerNum);
			return;
		}
		
		//Increment the current drawer.
		currentDrawer = ++currentDrawer % numPlayers;
		System.out.println(playerNum + "..." + currentDrawer);
		guessLog.setText("");
		//only updates for drawer
		if (playerNum == currentDrawer) {
			drawArea.removeListener();
			guessBtn.setEnabled(false);
			guessText.setEnabled(false);
			drawStart();
			
			//Start timer thread.
			Thread timer = new Thread() {

				public void run() {
					DrawScreen.time = 34;
					while (DrawScreen.time > 0) {
						try {
							sendMessageToPlayers("updateTime" + String.format("%02d", DrawScreen.time));
							DrawScreen.time--;
							System.out.println("Drawscreen time in thread is " + DrawScreen.time);
							Thread.sleep(1000L);
						} catch (InterruptedException e) { ; }
					}
					
					DrawScreen.time = 3;
					drawArea.removeListener();
					clearBtn.setEnabled(false);
					sendMessageToPlayers("showTopic");
					while (DrawScreen.time > 0) {
						try {
							DrawScreen.time--;
							Thread.sleep(1000L);
						} catch (InterruptedException e) {
							;
						}
					}
					sendMessageToPlayers("clearScreen");
					System.out.println("Updating drawer" + playerNum);
					sendMessageToPlayers("nextDrawer5");
				}
			};
			timer.start();
		} else {
			drawArea.removeListener();
			clearBtn.setEnabled(false);
			guessBtn.setEnabled(true);
			guessText.setEnabled(true);
			topic.setText("HIDDEN");
		}
	}
	
	//Starts each round with 3-2-1 drawings.
	private void drawStart() {
		drawArea.removeListener();
		if (three.exists() && two.exists() && one.exists()) {
			Thread timer = new Thread() {
				public void run() {
					try {
						clearBtn.setEnabled(false);
						guessText.setEnabled(false);
						BufferedReader br = new BufferedReader(new FileReader(three));
					    String line;
					    while ((line = br.readLine()) != null) {
					       sendMessageToPlayers(line);
					       Thread.sleep(5);
					    }
					    Thread.sleep(500);
					    sendMessageToPlayers("clearScreen");
					    
					    br = new BufferedReader(new FileReader(two));
					    while ((line = br.readLine()) != null) {
					       sendMessageToPlayers(line);
					       Thread.sleep(5);
					    }
					    Thread.sleep(500);
					    sendMessageToPlayers("clearScreen");
					    
					    br = new BufferedReader(new FileReader(one));
					    while ((line = br.readLine()) != null) {
					       sendMessageToPlayers(line);
					       Thread.sleep(5);
					    }
					    Thread.sleep(500);
					    sendMessageToPlayers("clearScreen");
					    
					    String temp = topicWords.get(ThreadLocalRandom.current().nextInt(topicWords.size()));
					    sendMessageToPlayers("updateTopic" + temp);
					    topic.setText(temp);
						drawArea.addListener();
					    clearBtn.setEnabled(true);
					    guessText.setEnabled(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			timer.start();
		}
	}
	
	private boolean checkIfGuessTrue(String message) {
		System.out.println("Current topic is " + currentTopic + ", guess is " + message.toLowerCase() + ", " + currentTopic.toLowerCase().equals(message.toLowerCase()));
		return currentTopic.toLowerCase().equals(message.toLowerCase());
	}

	
	//Clears all drawing off screen.
	public void clearScreen() {
		drawArea.clearScreen();
	}
	
	//Player disconnected.
	public void playerLeft(int playerNumLeft) {
		if (gameOver)
			return;
		numPlayers--;
		System.out.println("numPlayers is" + numPlayers);
		if (playerNumLeft == currentDrawer) {
			topic.setText("Drawer left!");
			System.out.println("Num players: " + numPlayers);
			guessBtn.setEnabled(false);
			guessText.setEnabled(false);
			DrawScreen.time = 3;
			updateCurrentDrawer();
		}
		
		if (numPlayers == 1)
			currRound = numRounds + 1;
	}

	public void disconnect() {
		try {
			sendMessageToPlayers("disconnect" + playerNum);
			cSocket.leaveGroup(group);
			listener.stop();
		} catch (IOException e) {
			System.exit(0);
		}
		cSocket.close();
	}
	
	public void showCorrectAnswer() {
		topic.setText("Topic was " + currentTopic);
	}

	public void updateLog(String message) {
		if (guessLog.getText().equals(""))
			guessLog.setText(guessLog.getText() + message);
		else
			guessLog.setText(guessLog.getText() + "\n" + message);
	}

	public void sendMessageToPlayers(String message) {
		byte[] buf = (message).getBytes();
		DatagramPacket dg = new DatagramPacket(buf, buf.length, group, 6789);
		try {
			cSocket.send(dg);
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}
}

