import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;

/**********************************************************************
 * FTP Client class for Project 1. Waits for input from user, then sends makes a
 * data connection. After that, command is sent to server.
 * 
 * @author Matt Kennedy, Colton Bates, Parker Skarzynski, Noah Verdeyen
 *
 *********************************************************************/
public class FTPClient {

	/******************************************************************
	 * Everything happens in the main method in this class. Just takes place in an
	 * infinite loop.
	 * 
	 * @param argv Args sent in from user from command line.
	 * @throws Exception Anything that may go wrong here is caught in a try-catch,
	 *                   but declaring streams requires a throw declaration.
	 *****************************************************************/
	public static void main(String argv[]) throws Exception {

		/** This is the command that is sent to the server. */
		String sentence;

		/** Used to get a list from the server. */
		String modifiedSentence = "";

		/** Determines if the program should continue or not at points. */
		int statusCode;

		/** Port to open the control connection on. */
		int port = 11230;

		/** Reader to get input from the user. */
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		/** Get the first input from user. Blocks until something happens. */
		sentence = inFromUser.readLine();

		/** Splits the sentence according to spaces. */
		StringTokenizer tokens = new StringTokenizer(sentence);

		/** Used to verify input has 3 arguments. */
		String[] tempVerify = sentence.split(" ");

		/**
		 * Use while loop to verify input. If doesn't start with connect or invalid
		 * args, continue loop.
		 */
		while (!tempVerify[0].equals("connect") || !checkValidArgs(tempVerify)) {
			System.out.println(
				"Incorrect arguments, please try again.\n" + 
				"Format: connect <IP Address> <Port>\n");
			sentence = inFromUser.readLine();
			tempVerify = sentence.split(" ");
		}

		// Create the variables here due to scope.
		// This way finally block can close later if exception caught.
		Socket ControlSocket = null;
		DataOutputStream outToServer = null;
		DataInputStream inFromServer = null;

		// Put everything in try block to catch exceptions.
		try {
			String serverName = tokens.nextToken(); // pass the connect command
			serverName = tokens.nextToken();
			int port1 = Integer.parseInt(tokens.nextToken());
			if (port1 < 1024) {
				System.out.println("Port < 1024, defaulting to 11230.");
				port1 = 11230;
			}

			// Create the Control Connection and the input/output streams for commands.
			ControlSocket = new Socket(serverName, port1);
			outToServer = new DataOutputStream(ControlSocket.getOutputStream());
			inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
			System.out.println("You are connected to " + serverName);

			// Infinite loop to work in.
			while (true) {
				System.out
						.println("\nWhat would you like to do next " + 
								"\n list || retr file.txt || stor || quit\n\n");
				// Get the first input from user. Blocks until something happens.
				sentence = inFromUser.readLine();
				modifiedSentence = "";

				// List command
				if (sentence.equals("list")) {
					// Set port equal to port + 2, as per project guidelines.
					port = port1 + 2;

					// Send the command to the server.
					outToServer.writeBytes(port + " " + sentence + " " + '\n');

					// Create a ServerSocket that blocks until a connection is made with the server.
					ServerSocket welcomeData = new ServerSocket(port);
					Socket dataSocket = welcomeData.accept();
					// Input stream to receive data from server on data connection.
					DataInputStream inData = new DataInputStream(
							new BufferedInputStream(dataSocket.getInputStream()));

					// Wait while data becomes available.
					// This command will execute too quickly without waiting.
					while (inData.available() == 0)
						Thread.sleep(20);

					// Read data while data is available.
					while (inData.available() > 0)
						modifiedSentence += inData.readUTF();

					// Print list.
					System.out.println("\nList is \n" + modifiedSentence);

					// Close server socket and the data socket.
					welcomeData.close();
					dataSocket.close();

					// Retrieve command
				} else if (sentence.startsWith("retr")) {
					// Set port equal to port + 2, as per project guidelines.
					port = port1 + 2;
					// Send the command to the server.
					outToServer.writeBytes(port + " " + sentence + " " + '\n');
					// Read status code with readInt(), which blocks until all 4 bytes read.
					statusCode = inFromServer.readInt();

					// If status code is 550 (error).
					if (statusCode == 550) {
						System.out.println("Did not work.");
						continue;

						// If status code is 200 (no error).
					} else if (statusCode == 200) {
						ServerSocket welcomeData = new ServerSocket(port);
						Socket dataSocket = welcomeData.accept();
						DataInputStream inData = new DataInputStream(
								new BufferedInputStream(dataSocket.getInputStream()));

						// readUTF was giving errors, so just parse the sentence to get the file name.
						String[] temp = sentence.split(" ");

						// Not necessary but helpful - use JFileChooser to select directory.
						// If none is chosen, default to working directory.
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						chooser.showSaveDialog(null);
						String filePath = chooser.getSelectedFile() + "/" + temp[temp.length - 1];
						if (chooser.getSelectedFile() == null)
							filePath = System.getProperty("user.dir") + "/" + temp[temp.length - 1];

						System.out.println("Saving file at " + filePath);
						// Create a byte array of a size given by the inData stream to get exact file
						// size.
						byte[] dataIn = new byte[inData.readInt()];

						// Reads bytes from the inData stream and places them in dataIn byte array.
						inData.readFully(dataIn);

						// Use FileOutputStream to write byes to new file.
						try (FileOutputStream fos = new FileOutputStream(filePath)) {
							fos.write(dataIn);
						}

						// Close everything.
						inData.close();
						welcomeData.close();
						dataSocket.close();
					}

					// Store command.
				} else if (sentence.startsWith("stor")) {
					// Use JFileChooser to select file.
					// Helpful because if no file selected, 
					// no command is sent, and no connection is opened.
					JFileChooser chooser = new JFileChooser();
					chooser.showSaveDialog(null);
					File fileToSend = chooser.getSelectedFile();
					if (chooser.getSelectedFile() == null) {
						System.out.println("No file selected.");
						continue;
					}

					// Create the FileInputStream to get bytes.
					FileInputStream fis = new FileInputStream(fileToSend);
					BufferedInputStream bis = new BufferedInputStream(fis);

					// Get port and send command to server.
					port = port1 + 2;
					outToServer.writeBytes(port + " " + sentence + " " + '\n');

					// Create dataSocket connection.
					ServerSocket welcomeData = new ServerSocket(port);
					Socket dataSocket = welcomeData.accept();
					DataOutputStream outData = new DataOutputStream(
							new BufferedOutputStream(dataSocket.getOutputStream()));

					System.out.println("Storing " + fileToSend.getAbsolutePath());

					// Get size of file, create byte array of that size.
					long length = fileToSend.length();
					byte[] bytesToSend = new byte[(int) length];

					// Reads bytes in to the bytesToSend array.
					bis.read(bytesToSend, 0, (int) length);

					// Sends the size of the array followed by the name of the file.
					outData.writeInt((int) length);
					outData.writeUTF(fileToSend.getName());

					// Writes byte array
					outData.write(bytesToSend);

					// Close everything.
					bis.close();
					fis.close();
					outData.close();

					welcomeData.close();
					dataSocket.close();

				} else if (sentence.equals("quit")) {
					outToServer.writeBytes(sentence + " " + '\n');
					break;

				} else {
					System.out.println("Command not recognized.");
					port = port1 + 2;

					// Tests if the connection is still open.
					try {
						outToServer.writeBytes("ping " + '\n');
					} catch (Exception e) {
						throw new SocketException();
					}
				}
			}
		} catch (SocketException e) {
			System.out.println("Connection closed.");
		} catch (Exception e) {
			System.out.println("Quitting application.");
		} finally {
			try {
				ControlSocket.close();
				outToServer.close();
				inFromServer.close();
			} catch (Exception e) {
				return;
			}
		}
	}

	/******************************************************************
	 * Verifies that the number of arguments is right and that the 3rd 
	 * argument is an integer (port).
	 * 
	 * @param tempVerify The inputs given by the user split into a 
	 * String array.
	 * @return True if valid, false if not.
	 *****************************************************************/
	private static boolean checkValidArgs(String[] tempVerify) {
		int portIndex = 2;
		// If not 3 args, then force user input again.
		if (tempVerify.length != 3) {
			return false;
		}

		// If the 3rd argument (port) isn't integer, try again.
		try {
			Integer.parseInt(tempVerify[portIndex]);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}