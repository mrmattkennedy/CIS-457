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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import player.Player;

public class DrawScreen implements ActionListener {
	
	private JFrame frame;
	private JPanel drawArea;
	private JPanel guessPanel;
	private JPanel guessPanelControls;
	
	private String playerName;
	private JTextPane guessLog;
	private JScrollPane pane;
	private JTextField guessText;
	private JButton guessBtn;
	private int port;
	private String address = "226.1.3.5";
	private static final int WIDTH = 650;
	private static final int HEIGHT = 450;
	
	public DrawScreen(String playerName) {
		drawArea = new DrawPanel();
		drawArea.setBorder(BorderFactory.createLineBorder(Color.black));
		guessPanel = new JPanel();
		guessPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		frame = new JFrame();
		
		frame.setSize(new Dimension(WIDTH + 50, HEIGHT + 75));
		frame.setLayout(new GridBagLayout());
		setLayout();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		
		guessPanelControls = new JPanel();
		guessText = new JTextField();
		guessText.setPreferredSize(new Dimension(90, 25));
		guessBtn = new JButton("GUESS");
		guessBtn.setPreferredSize(new Dimension(70, 25));
		guessBtn.setFont(new Font("Calibri", Font.PLAIN, 10));
		guessBtn.addActionListener(this);
		
		guessPanelControls.add(guessText);
		guessPanelControls.add(guessBtn);
		guessPanelControls.setBorder(BorderFactory.createLineBorder(Color.black));
		
		guessLog = new JTextPane();
		guessLog.setEditable(false);
//		guessLog.setBorder(BorderFactory.createLineBorder(Color.red));
		pane = new JScrollPane(guessLog);
		
//		guessLog.setContentType("text/html");
		guessPanel.setLayout(new GridBagLayout());
		this.playerName = playerName;
		setGuessPanelLayout();
		
		
//		guessPanel.add(guessLog);
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
		gbCons.weighty = 0.1;
		gbCons.ipady = (int) (HEIGHT);
		guessPanel.add(pane, gbCons);
		
		gbCons.gridx = 0;
		gbCons.gridy = 2; 
		gbCons.weightx = 1;
		gbCons.weighty = 0.6;
		gbCons.gridwidth = 1;
		gbCons.gridwidth = 1;
		gbCons.ipady = (int) (HEIGHT);
		guessPanel.add(guessPanelControls, gbCons);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == guessBtn) {
			if (guessLog.getText().equals(""))
				guessLog.setText(guessLog.getText() + playerName + ": " + guessText.getText());
			else
				guessLog.setText(guessLog.getText() + "\n" + playerName + ": " + guessText.getText());
			guessText.setText("");
			
		}
		
	}
}
