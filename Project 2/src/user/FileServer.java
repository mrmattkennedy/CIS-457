package user;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.Hashtable;


public class FileServer implements Runnable {
	private ServerSocket welcomeSocket;
	private int port = 12000;
	private String fileName;
	
	private Hashtable<String, Path> fileTable;

	public FileServer() throws IOException {
		fileTable = new Hashtable<String, Path>();
	}
	
	public Boolean Add(String file, Path path) {
        synchronized (fileTable) {
            if (fileTable.containsKey(file)) {
                return false;
            }
            fileTable.put(file, path);
        }
        return true;
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
				Thread server = new Thread(new FileServerThread(controlSocket, fileTable));
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
