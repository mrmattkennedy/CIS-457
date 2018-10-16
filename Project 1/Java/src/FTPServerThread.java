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
import java.util.List;
import java.util.StringTokenizer;

/**********************************************************************
 * FTP ServerThread class for Project 1. This is where the server
 * processes commands for the client. The controlSocket is already
 * created and sent in to the constructor of the class. No static
 * variables because everything is out of the run() method.
 * 
 * @author Matt Kennedy, Colton Bates, Parker Skarzynski, Noah Verdeyen
 *
 *********************************************************************/
public class FTPServerThread extends Thread {
	
	/** controlSocket that reads and sends commands to client. */
	private Socket controlSocket;
	
	/** Stream to write commands and info to client. */
	private DataOutputStream outToClient;
	
	/** Stream to read commands and info from client. */
	private BufferedReader inFromClient;
	
	/** List to hold tokens from the client.
	 * The size varies, so a list is easier to use than a String object. */
	private List<String> clientTokens;
	
	/** The command the client sent to the server. */
	private String clientCommand;
	
	/** Determines if the infinite loop should continue. */
	private boolean serverGo;
	
	
	/******************************************************************
	 * Constructor for the serverThread. Initializes the streams using
	 * the controlSocket sent in.
	 * 
	 * @param controlSocket Control Socket for the client/server 
	 * connection.
	 * @param port The port the server will run on.
	 *****************************************************************/
	public FTPServerThread(Socket controlSocket, int port)
    {
		serverGo = true;
		this.controlSocket = controlSocket;
        clientTokens = new ArrayList<String>();
		try {
			outToClient = new DataOutputStream(controlSocket.getOutputStream());
			inFromClient = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Error: " + e.getLocalizedMessage());
		}
    }
	
	//Override might be deprecated? Not sure, it automatically overrides without this,
	//but helps readability to show this overrides the super run() method.
	@Override
	public void run() {
		try {
			while (serverGo) {
				//Block until a command is read from the client.
				String fromClient = inFromClient.readLine();
				//Clear the array list that held client commands. 
				clientTokens.clear();
				StringTokenizer tokens = null;
				
				//If the client quit, then fromClient is null sometimes.
				//Helps prevent potential issues.
				if (!(fromClient == null)) {
					//Recreate tokenizer.
					tokens = new StringTokenizer(fromClient);
					
					while (tokens.hasMoreTokens())
						clientTokens.add(tokens.nextToken());
					
					System.out.println(clientTokens);
					
					//If the size > 1, the command is 1. Ex: quit: command.
					//Used for when a port is necessary for a data connection.
					if (clientTokens.size() > 1)
						clientCommand = clientTokens.get(1);
					else 
						clientCommand = clientTokens.get(0);
				} else
					clientCommand = "quit:";
				
				//List command.
				if (clientCommand.equals("list:")) {
					//Create a data socket on the given port.
					Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
					DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
					
					//Get all the files as a String.
					String files = getAllFiles();
					
					//Use writeUTF(String) to send the file String to client.
					dataOutToClient.writeUTF(files);
					//Helps clear the stream before closing.
					dataOutToClient.flush();
					dataOutToClient.close();
					System.out.println("List sent successfully");
					dataSocket.close();
					System.out.println("Data Socket closed.\n");
				
				//Retrieve command.
				} else if (clientCommand.equals("retr:")) {
					//If not exactly 3 tokens, status code is 550 (error).
					if (clientTokens.size() < 3) {
						System.out.println("No file supplied.");
						outToClient.writeInt(550);
						continue;
					}
					String filePath = System.getProperty("user.dir") + "/";
					File fileToSend = new File(filePath + clientTokens.get(2));
					
					//If the file doesn't exist, status code 550 (error).
					if (!fileToSend.exists()) {
						System.out.println("File does not exist.");
						outToClient.writeInt(550);
						continue;
					} else {
						//If all good, open dataSocket on given port.
						outToClient.writeInt(200);
						Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
						DataOutputStream outData = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
						//Use BufferedInputStream to read file in as bytes.
						FileInputStream fis = new FileInputStream(fileToSend);
						BufferedInputStream bis = new BufferedInputStream(fis);
						
						//Get size of file.
						long length = fileToSend.length();
						//Create byte array that is the size of the file.
						byte[] bytesToSend = new byte[(int) length];
						//Read bytes into byte array bytesToSend.
						bis.read(bytesToSend, 0, (int)length);
						//Send the size of the file to the client.
						outData.writeInt((int)length);
						//Send the byte array.
						outData.write(bytesToSend);
						//Helps clear stream before closing.
						outData.flush();
						outData.close();
						bis.close();
						fis.close();
						dataSocket.close();
					}
				
				//Store command.
				} else if (clientCommand.equals("stor:")) {
					//Skip status code, no need to check if file exists if user is forced to choose.
					Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
					DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
					//readInt() from client to get byte array size.
					byte[] dataIn = new byte[inData.readInt()];
					while (inData.available() == 0)
						Thread.sleep(20);
					
					//Get the server working directory.
					String filePath = System.getProperty("user.dir") + "/";
					//Get fhe file name from client.
					filePath += inData.readUTF();
					
					//Read the bytes for the file in.
					inData.readFully(dataIn);
					
					//Write bytes to file.
					try (FileOutputStream fos = new FileOutputStream(filePath)) {
						   fos.write(dataIn);
					}
					inData.close();
					dataSocket.close();
					
				//Quit command.
				} else if (clientCommand.equals("quit:")) {
					System.out.println("Closing connection " + controlSocket.getInetAddress().getHostName() + ".");
					outToClient.close();
					inFromClient.close();
					controlSocket.close();
					serverGo = false;
					return;
				}
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			e.printStackTrace();
		} finally {
			try {
				outToClient.writeUTF("quit:");
				outToClient.close();
				inFromClient.close();
				controlSocket.close();
			} catch (Exception e) {
				return;
			}
		}
	}
	
	/******************************************************************
	 * Creates a data socket for the server and client to send data
	 * over.
	 * 
	 * @param port The port the server will run on.
	 * @return Socket The dataSocket object.
	 *****************************************************************/
	private Socket makeDataSocket(int port) {
		System.out.println("Making data socket...");
		try {
			return new Socket(controlSocket.getInetAddress(), port);
		} catch (IOException e) {
			System.out.println("Error: " + e.toString());
		}
		return null;
	}
	
	/******************************************************************
	 * Gets a list of all files in the working directory.
	 * 
	 * @return The String of all files.
	 *****************************************************************/
	private static String getAllFiles() {
		File curDir = new File(".");
		File[] files = curDir.listFiles();
		String retStr = "";
		
		for (int i = 0; i < files.length; i++)
			if (files[i].isFile())
				retStr += files[i].getName() + "\n";
		return retStr;
	}
}
