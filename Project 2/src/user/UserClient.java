package user;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

//client only connects to centralized server
//server will have a listening socket, then open 1 socket, send the file, and then close.
//clients directly connect to user servers

public class UserClient implements ActionListener {
	
	private JPanel connectPanel;
	private JPanel connectSubPanel1;
	private JPanel connectSubPanel2;
	private JTextField serverHost;
	private JTextField serverPort;
	private JTextField username;
	private JTextField userHost;
	private JButton connectBtn;
	private String[] connectionTypes;
	private JComboBox<String> connection;
	
	private JPanel searchPanel;
	private JTextField searchField;
	private JButton searchBtn;
	private FileTableModel tableModel;
	private JTable fileTable;
	public JScrollPane pane;	
	
	private JPanel commandPanel;
	private JTextField commandText;
	private JButton commandBtn;
	private JTextPane commandLog;
	
	private Socket controlSocket;
	private DataOutputStream outToServer;
	private DataInputStream inFromServer;
	private JFrame frame;
	private static final int WIDTH = 650;
	private static final int HEIGHT = 450;
	
	
	public UserClient() {
		connectPanel = new JPanel();
		connectSubPanel1 = new JPanel();
		connectSubPanel2 = new JPanel();
		serverHost = new JTextField("localhost");
		serverPort = new JTextField("11230");
		username = new JTextField();
		userHost= new JTextField();
		connectBtn = new JButton("Connect");
		connectionTypes = new String[] {"ISDN", "DSL", "Cable", "Wireless", "T1", "T3", "OC3", "Satellite"};
		connection = new JComboBox<String>(connectionTypes);
		
		serverHost.setPreferredSize(new Dimension(220, 30));
		serverPort.setPreferredSize(new Dimension(90, 30));
		username.setPreferredSize(new Dimension(110, 30));
		userHost.setPreferredSize(new Dimension(150, 30));
		connectBtn.setPreferredSize(new Dimension(100, 30));
		connection.setPreferredSize(new Dimension(120, 30));
		connectBtn.addActionListener(this);
		
		connectSubPanel1.add(new JLabel("Server Hostname"));
		connectSubPanel1.add(serverHost);
		connectSubPanel1.add(new JLabel("Server port"));
		connectSubPanel1.add(serverPort);
		connectSubPanel1.add(connectBtn);
		connectSubPanel2.add(new JLabel("Username"));
		connectSubPanel2.add(username);
		connectSubPanel2.add(new JLabel("Hostname"));
		connectSubPanel2.add(userHost);
		connectSubPanel2.add(connection);
		
		connectPanel.setLayout(new GridLayout(2, 1));
		connectPanel.add(connectSubPanel1);
		connectPanel.add(connectSubPanel2);
		
		
		searchPanel = new JPanel();
		searchField = new JTextField();
		searchBtn = new JButton("Search");
		tableModel = new FileTableModel();
		fileTable = new JTable(tableModel);
		pane = new JScrollPane(fileTable);
		
		searchField.setPreferredSize(new Dimension(200, 30));
		searchBtn.setPreferredSize(new Dimension(100, 30));
		pane.setPreferredSize(new Dimension(WIDTH - 100, HEIGHT / 4));
		fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		searchPanel.add(new JLabel("Keyword"));
		searchPanel.add(searchField);
		searchPanel.add(searchBtn);
		searchPanel.add(pane);
		
		
		commandPanel = new JPanel();
		commandText = new JTextField();
		commandBtn = new JButton("Send");
		commandLog = new JTextPane();
		
		commandText.setPreferredSize(new Dimension(300, 30));
		commandBtn.setPreferredSize(new Dimension(100, 30));
		commandLog.setPreferredSize(new Dimension(WIDTH - 100, HEIGHT / 8));
		commandLog.setContentType("text/html");
		
		commandPanel.add(new JLabel("Enter command: "));
		commandPanel.add(commandText);
		commandPanel.add(commandBtn);
		commandPanel.add(commandLog);
		
		
		
		connectPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		searchPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		commandPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		
		frame = new JFrame();
		frame.setLayout(new GridBagLayout());
		setLayout();
		frame.setSize(new Dimension(WIDTH + 50, HEIGHT + 75));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void setLayout() {
		GridBagConstraints gbCons = new GridBagConstraints();
		gbCons.fill = GridBagConstraints.HORIZONTAL;

		gbCons.insets = new Insets(10, 5, 10, 5);
		gbCons.gridwidth = GridBagConstraints.REMAINDER;
		gbCons.gridx = 0;
		gbCons.gridy = 0; 
		gbCons.ipady = (int) (0.1 * HEIGHT);
		frame.add(connectPanel, gbCons);
		
		gbCons.gridx = 0;
		gbCons.gridy = 1; 
		gbCons.ipady = (int) (0.30 * HEIGHT);
		frame.add(searchPanel, gbCons);

		gbCons.insets = new Insets(10,5,10,5);
		gbCons.gridwidth = GridBagConstraints.REMAINDER;
		gbCons.gridx = 0;
		gbCons.gridy = 2; 
		gbCons.ipadx = WIDTH - 200;
		gbCons.ipady = (int) (0.15 * HEIGHT);
		frame.add(commandPanel, gbCons);
		
		gbCons.gridx = 0;
		gbCons.gridy = 0;
		gbCons.ipadx = 10;
		gbCons.ipady = 10;
		gbCons.anchor = GridBagConstraints.NORTHWEST;
		gbCons.insets = new Insets(-10, 5, 0, 0);
		frame.add(new JLabel("Connection"), gbCons);
		gbCons.gridy = 1;
		frame.add(new JLabel("Search"), gbCons);
		gbCons.gridy = 2;
		frame.add(new JLabel("Command"), gbCons);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		
		if (source == connectBtn) {
//			String str = "";
//			try {
//				str = getXMLFile();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			int nameIndex = 0;
//			List<String> names = new ArrayList<String>();
//			while (true) {
//				names.add(str.substring(str.indexOf("<name>", nameIndex ) + 6, str.indexOf("</name>", nameIndex)));
//				nameIndex = str.indexOf("</name>", nameIndex) + 1; //sets starting point to most recent name tag
//				if (str.indexOf("<name>", nameIndex) == -1) //checks location of next name tag
//					break;
//			}
//				
//			String speed = (String)(connection.getSelectedItem());
//			String host = userHost.getText();
//			String user = username.getText();
//			for (int i = 0; i < names.size(); i++)
//				tableModel.addVariable(speed, host, names.get(i), user);
//			tableModel.repaintTable();
			SocketListener sListen = new SocketListener(serverHost.getText(), 
					serverPort.getText(),
					(String)(connection.getSelectedItem()),
					userHost.getText(),
					username.getText(),
					this);
		}
	}
	
	public void updateTable(String message) {
		String[] newRows = message.split("[|]");
		String[] temp;
		for (int i = 0; i < newRows.length; i++) {
			temp = newRows[i].split(" ");
			for (int j = 0; j < temp.length; j++)
				System.out.println(temp[j]);
		}
		//TODO: update the table.
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
