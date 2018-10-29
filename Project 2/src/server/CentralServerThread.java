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

	/** Stream to read commands and info from client. */
	private BufferedReader inFromClient;

	/**
	 * List to hold tokens from the client. The size varies, so a list is easier to
	 * use than a String object.
	 */
	private List<String> clientTokens;

	/** The command the client sent to the server. */
	private String clientCommand;

	/** Determines if the infinite loop should continue. */
	private boolean serverGo;
	
	private String xmlFile = "";

	private static Vector<CentralServerThread> clients = new Vector<CentralServerThread>();
	
	private static String allXMLFiles = "";
	

	/******************************************************************
	 * Constructor for the serverThread. Initializes the streams using the
	 * controlSocket sent in.
	 * 
	 * @param controlSocket Control Socket for the client/server connection.
	 * @param port          The port the server will run on.
	 *****************************************************************/
	public CentralServerThread(Socket controlSocket, int port) {
		serverGo = true;
		this.controlSocket = controlSocket;
		clientTokens = new ArrayList<String>();
		clients.add(this);
		try {
			outToClient = new DataOutputStream(controlSocket.getOutputStream());
			inFromClient = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
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
			xmlFile = inFromClient.readLine().substring(2);
			System.out.println("xml file is " + xmlFile);
			allXMLFiles += xmlFile + "|";
			broadcast(allXMLFiles);
			while (serverGo) {
				// Block until a command is read from the client.
				String fromClient = inFromClient.readLine();
				System.out.println(fromClient);
				// Clear the array list that held client commands.
//				clientTokens.clear();
//				StringTokenizer tokens = null;
//
//				// If the client quit, then fromClient is null sometimes.
//				// Helps prevent potential issues.
//				if (!(fromClient == null)) {
//					// Recreate tokenizer.
//					tokens = new StringTokenizer(fromClient);
//
//					while (tokens.hasMoreTokens())
//						clientTokens.add(tokens.nextToken());
//
//					System.out.println(clientTokens);
//
//					// If the size > 1, the command is 1. Ex quit command.
//					// Used for when a port is necessary for a data connection.
//					if (clientTokens.size() > 1)
//						clientCommand = clientTokens.get(1);
//					else
//						clientCommand = clientTokens.get(0);
//				} else
//					clientCommand = "quit";
//
//				// Quit command.
//				if (clientCommand.equals("quit")) {
//					System.out.println("Closing connection " + controlSocket.getInetAddress().getHostName() + ".");
//					outToClient.close();
//					inFromClient.close();
//					controlSocket.close();
//					serverGo = false;
//					return;
//				}
			}
		} catch (Exception e) {
			System.out.println("Error " + e.toString());
			e.printStackTrace();
		} finally {
			try {
				outToClient.close();
				inFromClient.close();
				controlSocket.close();
			} catch (Exception e) {
				return;
			}
		}
	}

	/******************************************************************
	 * Creates a data socket for the server and client to send data over.
	 * 
	 * @param port The port the server will run on.
	 * @return Socket The dataSocket object.
	 *****************************************************************/
	private Socket makeDataSocket(int port) {
		System.out.println("Making data socket...");
		try {
			return new Socket(controlSocket.getInetAddress(), port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static void broadcast(String message) {
		synchronized (clients) {
			Enumeration<CentralServerThread> e = clients.elements();
			
			while (e.hasMoreElements()) {
				CentralServerThread client = e.nextElement();
				try {
					client.outToClient.writeUTF("updateTable:");
					client.outToClient.writeUTF(message);
					client.outToClient.flush();
				} catch (IOException ex) {
					try {
						client.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
}
