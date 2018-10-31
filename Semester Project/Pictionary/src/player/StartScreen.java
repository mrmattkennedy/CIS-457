package player;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import game.MainGame;

public class StartScreen implements ActionListener {
	
	private JButton playGame;
	private JTextField addressText;
	private JTextField portText;
	private JFrame frame;
	private JPanel panel;
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	
	public StartScreen() {
		addressText = new JTextField("226.1.3.5");
		addressText.setPreferredSize(new Dimension(90, 30));
		portText = new JTextField("6789");
		portText .setPreferredSize(new Dimension(60, 30));
		playGame = new JButton("PLAY");
		playGame.addActionListener(this);
		panel = new JPanel();
		
		panel.add(addressText);
		panel.add(portText);
		panel.add(playGame);
		frame = new JFrame();
		frame.getContentPane().add(panel);
		frame.setSize(new Dimension(400, 205));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new StartScreen();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == playGame) {
			String address = addressText.getText();
			int port = Integer.parseInt(portText.getText());
			
			try {
				socket = new MulticastSocket(port);
		        group = InetAddress.getByName(address);
		        socket.joinGroup(group);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			frame.dispose();
			frame = new MainGame();
			
		}
		
	}
	
}
