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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;

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

import player.Player;

public class DrawScreen implements ActionListener {
	
	private JFrame frame;
	private DrawPanel drawArea;
	private JPanel guessPanel;
	private JPanel guessPanelControls;
	
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
	
	public DrawScreen(String playerName, 
			MulticastSocket socket, 
			InetAddress group, 
			int playerNum, 
			Thread listener) {
		this.cSocket = socket;
		this.group = group;
		this.listener = listener;
		this.playerNum = playerNum;
		
		drawArea = new DrawPanel(this);
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
		
//		guessLog.setContentType("text/html");
		guessPanel.setLayout(new GridBagLayout());
		this.playerName = playerName;
		setGuessPanelLayout();
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
		gbCons.gridwidth = 1;
		gbCons.gridwidth = 2;
		gbCons.weightx = 1;
		gbCons.weighty = 0.15;
		gbCons.ipady = (int) (HEIGHT);
		guessPanel.add(pane, gbCons);
		
		gbCons.gridx = 0;
		gbCons.gridy = 2; 
		gbCons.weightx = 1;
		gbCons.weighty = 0.8;
		gbCons.gridwidth = 1;
		gbCons.gridwidth = 1;
		gbCons.ipady = (int) (HEIGHT);
		guessPanel.add(guessPanelControls, gbCons);
	}
	
	public void updatePanel(int x1, int y1, int x2, int y2) {
		sendMessageToPlayers("updateDrawing" + playerNum + "_" + x1 + "," + y1 + "," + x2 + "," + y2 + ",");
	}
	
	public void addDrawingToPanel(int x1, int y1, int x2, int y2) {
		drawArea.updateDrawing(x1, y1, x2, y2);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == guessBtn) {
			sendMessageToPlayers("newMessage" + playerName + ": " + guessText.getText());
			guessText.setText("");
		}	
	}
	
	public void disconnect() {
		try {
			cSocket.leaveGroup(group);
			listener.stop();
		} catch (IOException e) {
			System.exit(0);
		}
		cSocket.close();
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
