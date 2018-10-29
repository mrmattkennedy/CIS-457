package server;

import java.io.*;
import java.net.*;


/**********************************************************************
 * FTP Server class for Project 1. Doesn't do a great deal. Creates a 
 * welcomeSocket that performs the handshake operation for incoming
 * connections, then creates a new thread for the controlSocket.
 * 
 * @author Matt Kennedy, Colton Bates, Parker Skarzynski, Noah Verdeyen
 *
 *********************************************************************/
public class CentralServer {
	private static int port = 11230;
	private static ServerSocket welcomeSocket;
	private static int currentSocket = 0;
	private static boolean serverGo = true;
	
	/******************************************************************
	 * Infinite loop here to constantly accept connections and make new
	 * server threads. 
	 * @param argv Args sent in from user from command line.
	 *****************************************************************/
	public static void main(String[] args) {
		//If the user input any commands, try to parse the first.
		if (args.length > 0)
			try {
				port = Integer.parseInt(args[0]);
				if (port < 1024)
					throw new IllegalArgumentException();
			//Failure to parse first arg as an int throws NumberFormatException
			//Per program requirements, if port < 1024, throw IllegalArgumentException.
			} catch (Exception e) {
				System.out.println("Failed to read port. Defaulting to 11230.");
				port = 11230;
			}
		//Declare here so finally block can try to close later.
		welcomeSocket = null;
		
		try {
			//Create this socket to perform handshake.
			welcomeSocket = new ServerSocket(port);
			/*
			 * Small issue not sure what to do; if a client quits, then
			 * the server shouldn't close (supports multiple clients).
			 * When to close the server?
			 */
			while (serverGo) {
				//Block until new connection accepted.
				Socket controlSocket = welcomeSocket.accept();
				
                System.out.println("Accepting connection from " + 
                		controlSocket.getInetAddress().getHostName() +  
                		", connection #" + ++currentSocket);
                //Create new server thread.
                Thread server = new CentralServerThread(controlSocket, port);
                server.start();
			}
		} catch (SocketException e) {
			System.out.println("Welcome socket closed.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				System.out.println("Closing welcome socket...");
				welcomeSocket.close();
			} catch (IOException e) {
				return;
			}
		}
	}
}
