package user;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class FileServerThread implements Runnable {
	
	public FileServerThread(int )

	@Override
	public void run() {
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
			DataOutputStream outData = 
					new DataOutputStream(
					new BufferedOutputStream(dataSocket.getOutputStream()));
			//Use BufferedInputStream to read file in as bytes.
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
		
	}
	
}
