package user;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketListener {
	
	private Socket controlSocket;
	private DataOutputStream outToServer;
	private DataInputStream inFromServer;
	private UserClient host;
	
	public SocketListener(String serverHost, 
			String serverPort,
			String connection, 
			String userHost, 
			String username,
			UserClient host) {
		String serverName = serverHost;
		String port = serverPort;
		this.host = host;
		
		try {
			controlSocket = new Socket(serverName, Integer.parseInt(port));
			outToServer = new DataOutputStream(controlSocket.getOutputStream());
			inFromServer = new DataInputStream(new BufferedInputStream(controlSocket.getInputStream()));
			System.out.println("You are connected to " + serverName);
			String str = getXMLFile();
			int nameIndex = 0;
			List<String> names = new ArrayList<String>();
			while (true) {
				names.add(str.substring(str.indexOf("<name>", nameIndex ) + 6, str.indexOf("</name>", nameIndex)));
				nameIndex = str.indexOf("</name>", nameIndex) + 1; //sets starting point to most recent name tag
				if (str.indexOf("<name>", nameIndex) == -1) //checks location of next name tag
					break;
			}
			String stringToServer = "";
			for (int i = 0; i < names.size(); i++)
				stringToServer += connection + " " + serverHost + " " + username + " " + names.get(i) + "|";
			stringToServer = stringToServer.substring(0, stringToServer.length() - 1) + "\n";
			outToServer.writeUTF(stringToServer); //can just send this, as is the first command in the connection.
			outToServer.flush();
			listenForCommands();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void listenForCommands() {
		while (true) {
            try {
				String message = inFromServer.readUTF();
				
				if (message.startsWith("updateTable:")) {
					host.updateTable(inFromServer.readUTF());
				}
					
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
         }
	}
	
	private String getXMLFile() throws Exception {
		File file = new File(System.getProperty("user.dir") + "/filelist.xml");
		if (file.exists()) {
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();

			String str = new String(data, "UTF-8");
			str = str.substring(str.indexOf("\n"));
			str = str.replaceAll("\n", "");
			return str;
		}
		return null;
	}
}
