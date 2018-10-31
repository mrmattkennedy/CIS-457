package game;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StartScreen extends JPanel implements ActionListener {
	
	private JButton playGame;
	private JFrame frame;
	
	public StartScreen() {
		playGame = new JButton("PLAY");
		playGame.addActionListener(this);
		add(playGame);
		frame = new JFrame();
		frame.getContentPane().add(this);
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
			new MainGame();
			frame.dispose();
		}
		
	}
	
}
