package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**********************************************************************
* FTP ServerThread class for Project 1. This is where the server processes
* commands for the client. The controlSocket is already created and sent in to
* the constructor of the class. No static variables because everything is out
* of the run() method.
*
* @author Matt Kennedy, Colton Bates, Parker Skarzynski, Noah Verdeyen
*
*********************************************************************/
public class CentralServerThread extends Thread {

	/** controlSocket that reads and sends commands to client. */
	private Socket controlSocket;

	/** Stream to write commands and info to client. */
	protected DataOutputStream outToClient;

	protected DataInputStream inFromClient;

	private String hostName;

	Vector<ClientInfo> clients;
	Vector<FileInfo> files;
	Vector<FileInfo> myFiles = new Vector<FileInfo>();

	ClientInfo self;

	/******************************************************************
	* Constructor for the serverThread. Initializes the streams using the
	* controlSocket sent in.
	*
	* @param controlSocket Control Socket for the client/server connection.
	* @param port          The port the server will run on.
	*****************************************************************/
	public CentralServerThread(Socket controlSocket, int port, Vector<ClientInfo> clients, Vector<FileInfo> files) {
		this.clients = clients;
		this.files = files;
		this.controlSocket = controlSocket;
		hostName = controlSocket.getInetAddress().getHostName();

		System.out.println(files);

		self = new ClientInfo("unnamed", "Dial-Up", hostName);
		clients.add(self);

		try {
			outToClient = new DataOutputStream(controlSocket.getOutputStream());
			inFromClient = new DataInputStream(controlSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Error " + e.getLocalizedMessage());
		}
	}

	// Override might be deprecated? Not sure, it automatically overrides without
	// this,
	// but helps readability to show this overrides the super run() method.
	@Override
	public void run() {
		try {
			while (true) {
				String message = inFromClient.readUTF();
				String[] tokens = message.split("\t");
				switch (tokens[0].toUpperCase()) {
					case "ADD":
						FileInfo newFile = new FileInfo(tokens[1], tokens.length > 2 ? tokens[2] : "", self);
						synchronized(files) {
							files.add(newFile);
						}
						myFiles.add(newFile);
						System.out.println(self.username + " added " + tokens[1]);
						break;
					case "REMOVE":
						synchronized(files) {
							for (FileInfo file : myFiles) {
								if (file.fileName.equals(tokens[1])) {
									files.remove(file);
									System.out.println(self.username + " removed " + tokens[1]);
								} else {
									System.out.println(self.username + " attempted to remove " + tokens[1]);
								}
							}
						}
					break;
					case "SET":
						if (tokens[1].length() > 0) {
							System.out.println(self.username + " is now " + tokens[1]);
							self.username = tokens[1];
						}
						if (tokens.length > 2 && tokens[2].length() > 0) {
							System.out.println(self.username + " now has speed " + tokens[2]);
							self.connectionSpeed = tokens[2];
						}
						break;
					case "SEARCH":
						synchronized(files) {
							System.out.println("searching");
							for (FileInfo file : files) {
								if (file.description.matches(tokens[1])) {
									System.out.println("Found " + file.fileName);
									outToClient.writeUTF(file.fileName + "\t" + file.description + "\t" + file.host.username + "\t" + file.host.hostName + "\t" + file.host.connectionSpeed);
								}
							}
							outToClient.writeUTF("END");
						}
						break;
					default:
						System.out.println("Bad cmd: " + message + ":\t\"" + (tokens[0].length()) + "\"");
						for (char c : tokens[0].toCharArray()) {
							System.out.print((int)c + " ");
						}
						System.out.println("");
				}
			}
		} catch (EOFException e) {		// Connection closed
		} catch (Exception e) {
			System.out.println("Error " + e.toString());
			e.printStackTrace();
		} finally {
			try {
				System.out.println(self.username + " left.");
				synchronized(files) {
					files.removeAll(myFiles);
				}
				clients.remove(self);
				outToClient.close();
				inFromClient.close();
				controlSocket.close();
			} catch (Exception e) {
				return;
			}
		}

	}
}
