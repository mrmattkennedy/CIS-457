package user;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

class CentralClient {
  private Socket serverConnection;
  private DataOutputStream outToServer;
	private DataInputStream inFromServer;

  public CentralClient(InetAddress serverAddress, int port) throws IOException {
			serverConnection = new Socket(serverAddress, port);
			outToServer = new DataOutputStream(serverConnection.getOutputStream());
			inFromServer = new DataInputStream(new BufferedInputStream(serverConnection.getInputStream()));
  }
  
//  @Override
//  public void run() {
//	  while (true) {
//		  try {
//			  String message = inFromServer.readUTF();
//		  } catch (Exception e) {
//			  e.printStackTrace();
//		  }
//	  }
//  	
//  }

  public Boolean Add(String filename, String description) throws IOException {
    if (filename.contains("\t") || description.contains("\t")) {
      return false;
    }
    outToServer.writeUTF("ADD\t" + filename + "\t" + description + "\n");
    outToServer.flush();
    return true;
  }

  public Boolean Remove(String filename) throws IOException {
    if (filename.contains("\t")) {
      return false;
    }
    outToServer.writeUTF("REMOVE\t" + filename + "\n");
    return true;
  }

  public Boolean Set(String username, String connection) throws IOException {
    if (username.contains("\t") || connection.contains("\t")) {
      return false;
    }
    outToServer.writeUTF("SET\t" + username + "\t" + connection + "\n");
    return true;
  }

  public ArrayList<FileInfo> Search(String regex) throws IOException {
    if (regex.contains("\t")) {
      return null;
    }
    
    outToServer.writeUTF("SEARCH\t" + regex + "\n");
    ArrayList<FileInfo> results = new ArrayList<FileInfo>();
    @SuppressWarnings("deprecation")
	String lineIn = inFromServer.readLine().trim();
    System.out.println(lineIn + "\n");
    while (lineIn.length() > 0) {
      String[] splitLine = lineIn.split("\t");
      if (splitLine.length != 5) {
        System.out.println(lineIn.length());
      }
      results.add(new FileInfo(splitLine[0], splitLine[1], new ClientInfo(splitLine[2], splitLine[4], splitLine[3])));
      lineIn = inFromServer.readLine().trim();
    }
    return results;
  }


}
