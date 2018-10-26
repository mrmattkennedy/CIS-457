package user;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

//client only connects to centralized server
//server will have a listening socket, then open 1 socket, send the file, and then close.
//clients directly connect to user servers

public class UserClient {
	
	private JPanel connectPanel;
	private JPanel connectSubPanel1;
	private JPanel connectSubPanel2;
	private JTextField serverHost;
	private JTextField serverPort;
	private JTextField username;
	private JTextField userHost;
	private JButton connectBtn;
	private JPanel panel2;
	private JPanel panel3;
	private JFrame frame;
	private static final int WIDTH = 650;
	private static final int HEIGHT = 400;
	
	public UserClient() {
		connectPanel = new JPanel();
		connectSubPanel1 = new JPanel();
		connectSubPanel2 = new JPanel();
		serverHost = new JTextField();
		serverPort = new JTextField();
		username = new JTextField();
		userHost= new JTextField();
		connectBtn = new JButton("Connect");
		
		serverHost.setPreferredSize(new Dimension(220, 30));
		serverPort.setPreferredSize(new Dimension(90, 30));
		username.setPreferredSize(new Dimension(110, 30));
		userHost.setPreferredSize(new Dimension(150, 30));
		connectBtn.setPreferredSize(new Dimension(100, 30));
		
		connectSubPanel1.add(new JLabel("Server Hostname"));
		connectSubPanel1.add(serverHost);
		connectSubPanel1.add(new JLabel("Server port"));
		connectSubPanel1.add(serverPort);
		connectSubPanel1.add(connectBtn);
		connectSubPanel2.add(new JLabel("Username"));
		connectSubPanel2.add(username);
		connectSubPanel2.add(new JLabel("Hostname"));
		connectSubPanel2.add(userHost);
		
		connectPanel.setLayout(new GridLayout(2, 1));
		connectPanel.add(connectSubPanel1);
		connectPanel.add(connectSubPanel2);
		
		panel2 = new JPanel();
		panel3 = new JPanel();
		connectPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel2.setBorder(BorderFactory.createLineBorder(Color.black));
		panel3.setBorder(BorderFactory.createLineBorder(Color.black));
		
		
		frame = new JFrame();
		frame.setLayout(new GridBagLayout());
		frame.setSize(new Dimension(WIDTH, HEIGHT + 50));
		setLayout();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
	public void setLayout() {
		GridBagConstraints gbCons = new GridBagConstraints();
		gbCons.fill = GridBagConstraints.HORIZONTAL;

//		gbCons.anchor = GridBagConstraints.FIRST_LINE_START;
		gbCons.insets = new Insets(10, 5, 10, 5);
		gbCons.gridwidth = GridBagConstraints.REMAINDER;
		gbCons.gridx = 0;
		gbCons.gridy = 0; 
//		gbCons.ipadx = 800;
		gbCons.ipady = (int) (0.1 * HEIGHT);
		frame.add(connectPanel, gbCons);

//		gbCons.anchor = GridBagConstraints.CENTER;
		gbCons.insets = new Insets(10,5,10,5);
		gbCons.gridwidth = GridBagConstraints.REMAINDER;
		gbCons.gridx = 0;
		gbCons.gridy = 1; 
//		gbCons.ipadx = 800;
		gbCons.ipady = (int) ((250.0 / 650.0) * HEIGHT);
		frame.add(panel2, gbCons);

//		gbCons.anchor = GridBagConstraints.SOUTH;
		gbCons.insets = new Insets(10,5,10,5);
		gbCons.gridwidth = GridBagConstraints.REMAINDER;
		gbCons.gridx = 0;
		gbCons.gridy = 2; 
		gbCons.ipadx = WIDTH - 50;
		gbCons.ipady = (int) ((100.0 / 650.0) * HEIGHT);
		frame.add(panel3, gbCons);
	}
	
	public static void main(String[] args) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to load Look and Feel, using default.", "Error", JOptionPane.INFORMATION_MESSAGE);     		        			
		}
			
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createFrame();
			}
		});
	}
	
	public static void createFrame() {
		new UserClient();
	}
}
