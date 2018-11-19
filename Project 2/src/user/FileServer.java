package user;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class FileServer implements Runnable {
	private ServerSocket welcomeSocket;
	private int port;
	private String fileName;

	public FileServer(int port, String fileName) throws IOException {
		this.port = port;
		this.fileName = fileName;
	}

	@Override
	public void run() {

		try {
			// Create this socket to perform handshake.
			welcomeSocket = new ServerSocket(port);
			/*
			 * Small issue not sure what to do; if a client quits, then the server shouldn't
			 * close (supports multiple clients). When to close the server?
			 */
			while (true) {
				// Block until new connection accepted.
				Socket controlSocket = welcomeSocket.accept();

				System.out.println("Accepting connection from " + controlSocket.getInetAddress().getHostName());
				// Create new server thread.
				Thread server = new Thread(new FileServerThread(contolSocket, fileName))
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
