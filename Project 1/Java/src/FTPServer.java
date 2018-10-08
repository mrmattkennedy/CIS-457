import java.io.*;
import java.net.*;


public class FTPServer {
	static String fromClient;
	static String clientCommand;
	static String frstln;
	static int port = 12000;
	static ServerSocket welcomeSocket;
	static byte[] data;
	static int currentSocket = 1;
	static boolean clientGo = true;
	public static void main(String[] args) throws IOException {
		welcomeSocket = new ServerSocket(port);
		
		try {
			while (clientGo) {
				Socket controlSocket = welcomeSocket.accept();
				
                System.out.println("Accepting connection from " + 
                		controlSocket.getInetAddress().getHostName() +  
                		", connection #" + currentSocket);
                Thread server = new FTPServerThread(controlSocket, port);
                currentSocket++;
                server.start();
			}
		} catch (SocketException e) {
			System.out.println("Welcome socket closed.");
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
		}
	}
	
	public static void quit() {
		clientGo = false;
		try {
			welcomeSocket.close();
		} catch (IOException e) {
			System.out.println("Error: " + e.toString());
		}
	}
}
