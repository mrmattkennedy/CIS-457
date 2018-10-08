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
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

public class FTPClient {

	public static void main(String argv[]) throws Exception {
		String sentence;
		String modifiedSentence = "";
		boolean isOpen = true;
		int number = 1;
		boolean notEnd = true;
		int statusCode;
		boolean clientgo = true;
		int port;

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		sentence = inFromUser.readLine();
		StringTokenizer tokens = new StringTokenizer(sentence);

		if (sentence.startsWith("connect")) {
			String serverName = tokens.nextToken(); // pass the connect command
			serverName = tokens.nextToken();
			int port1 = Integer.parseInt(tokens.nextToken());
			System.out.println("You are connected to " + serverName);

			Socket ControlSocket = new Socket(serverName, port1);
			DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
			DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

			try {
				while (isOpen && clientgo) {
					sentence = inFromUser.readLine();
					modifiedSentence = "";
	
					if (sentence.equals("list:")) {
	
						port = port1 + 2;
						outToServer.writeBytes(port + " " + sentence + " " + '\n');
						
						ServerSocket welcomeData = new ServerSocket(port);
						Socket dataSocket = welcomeData.accept();
						DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
						while (inData.available() == 0)
							Thread.sleep(20);
	
						
						while (inData.available() > 0) 
							modifiedSentence += inData.readUTF();
						
						System.out.println("List is \n" + modifiedSentence);
	
						welcomeData.close();
						dataSocket.close();
					} else if (sentence.startsWith("retr:")) {
						port = port1 + 2;
						outToServer.writeBytes(port + " " + sentence + " " + '\n');
						statusCode = inFromServer.readInt();
						if (statusCode == 550) {
							System.out.println("Did not work.");
							System.out.println("\nWhat would you like to do next: \n list: || retr: file.txt ||stor: || quit:\n\n");
							continue;
						} else if (statusCode == 200) {
							ServerSocket welcomeData = new ServerSocket(port);
							Socket dataSocket = welcomeData.accept();
							DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
							
							//readUTF was giving errors, so just parse the sentence
							String[] temp = sentence.split(" ");
							JFileChooser chooser=new JFileChooser();
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							chooser.showSaveDialog(null);
							String filePath = chooser.getSelectedFile() + "/" + temp[temp.length - 1];
							System.out.println(filePath);
							byte[] dataIn = new byte[inData.readInt()];
							inData.readFully(dataIn);
							try (FileOutputStream fos = new FileOutputStream(filePath)) {
								   fos.write(dataIn);
							}
							inData.close();
							welcomeData.close();
							dataSocket.close();
						}
						
					} else if (sentence.startsWith("stor:")) {
						JFileChooser chooser=new JFileChooser();
						chooser.showSaveDialog(null);
						File fileToSend = chooser.getSelectedFile();
						if (chooser.getSelectedFile() == null) {
							System.out.println("No file selected.");
							System.out.println("\nWhat would you like to do next: \n list: || retr: file.txt ||stor: || quit:\n\n");
							continue;
						}
						FileInputStream fis = new FileInputStream(fileToSend);
						BufferedInputStream bis = new BufferedInputStream(fis);
						System.out.println(fileToSend.getAbsolutePath());
						port = port1 + 2;
						outToServer.writeBytes(port + " " + sentence + " " + '\n');
						
						ServerSocket welcomeData = new ServerSocket(port);
						Socket dataSocket = welcomeData.accept();
						DataOutputStream outData = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
						
						long length = fileToSend.length();
						byte[] bytesToSend = new byte[(int) length];
						bis.read(bytesToSend, 0, (int)length);
						outData.writeInt((int)length);
						outData.writeUTF(fileToSend.getName());
						outData.write(bytesToSend);
						bis.close();
						fis.close();
						outData.close();
						
						welcomeData.close();
						dataSocket.close();
						
					} else if (sentence.equals("quit:")) {
						outToServer.writeBytes(sentence + " " + '\n');
						clientgo = false;
					} else {
						System.out.println("Command not recognized.");
					}
					
					if (!sentence.equals("quit:"))
							System.out.println("\nWhat would you like to do next: \n list: || retr: file.txt ||stor: || quit:\n\n");
				}
			} catch (Exception e) {
				System.out.println("Error: " + e.toString());
			} finally {
				ControlSocket.close();	
			}
			ControlSocket.close();	
		}
	}
}