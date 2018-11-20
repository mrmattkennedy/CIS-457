package user;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;

public class FileServerThread implements Runnable {

    private Hashtable<String, Path> fileTable;
    private Socket controlSocket;
    
    private DataInputStream inFromClient;
    private DataOutputStream outToClient;
   
	
	public FileServerThread(Socket socket, Hashtable<String, Path> table) throws IOException {
        controlSocket = socket;
        fileTable = table;
        inFromClient = new DataInputStream(controlSocket.getInputStream());
        outToClient = new DataOutputStream(controlSocket.getOutputStream());
    }

	@Override
	public void run(){
	try {
        String filename = inFromClient.readUTF();
        Path filePath;
        
        synchronized (fileTable) {
        	 Enumeration e = fileTable.elements();

        	  while (e.hasMoreElements())
        	  {
        		  System.out.println(Arrays.toString((String[]) e.nextElement()));
        	  }
            if (fileTable.containsKey(filename)) {
                filePath = fileTable.get(filename);
            } else {
                outToClient.writeInt(9001);
                outToClient.flush();
                controlSocket.close();
                return;
            }
        }
		File fileToSend = filePath.toFile();
		
		//If the file doesn't exist, status code 550 (error).
		if (!fileToSend.exists()) {
			System.out.println("File does not exist.");
			outToClient.writeInt(550);
			outToClient.flush();
			controlSocket.close();
		} else {
			//If all good, open dataSocket on given port.
			outToClient.writeInt(200);
			FileInputStream fis = new FileInputStream(fileToSend);
			BufferedInputStream bis = new BufferedInputStream(fis);
			
			System.out.println("Sending requested file " + fileToSend.getName());
			//Get size of file.
			long length = fileToSend.length();
			//Create byte array that is the size of the file.
			byte[] bytesToSend = new byte[(int) length];
			//Read bytes into byte array bytesToSend.
			bis.read(bytesToSend, 0, (int)length);
			//Send the size of the file to the client.
			outToClient.writeInt((int)length);
			//Send the byte array.
			outToClient.write(bytesToSend);
			//Helps clear stream before closing.
			outToClient.flush();
			outToClient.close();
			bis.close();
			fis.close();
			controlSocket.close();
		}
	} catch (Exception e) {}
	}
}
