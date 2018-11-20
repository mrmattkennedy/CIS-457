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
  
  public Boolean Add(String filename, String description) throws IOException {
    if (filename.contains("\t") || description.contains("\t")) {
      return false;
    }
    outToServer.writeUTF("ADD\t" + filename + "\t" + description);
    outToServer.flush();
    return true;
  }

  public Boolean Remove(String filename) throws IOException {
    if (filename.contains("\t")) {
      return false;
    }
    outToServer.writeUTF("REMOVE\t" + filename);
    return true;
  }

  public Boolean Set(String username, String connection, String hostname) throws IOException {
    if (username.contains("\t") || connection.contains("\t") || hostname.contains("\t")) {
      return false;
    }
    outToServer.writeUTF("SET\t" + username + "\t" + connection +  "\t" + hostname);
    return true;
  }

  public ArrayList<FileInfo> Search(String regex) throws IOException {
    if (regex.contains("\t")) {
      return null;
    }

    outToServer.writeUTF("SEARCH\t" + regex);
    ArrayList<FileInfo> results = new ArrayList<FileInfo>();
	String lineIn = inFromServer.readUTF();
    while (!lineIn.equals("END")) {
      String[] splitLine = lineIn.split("\t");
      if (splitLine.length != 5) {
        System.out.println(lineIn.length());
      }
      results.add(new FileInfo(splitLine[0], splitLine[1], new ClientInfo(splitLine[2], splitLine[4], splitLine[3])));
      lineIn = inFromServer.readUTF();
    }
    return results;
  }


}
