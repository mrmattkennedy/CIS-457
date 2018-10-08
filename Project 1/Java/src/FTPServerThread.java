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

public class FTPServerThread extends Thread {
	private Socket controlSocket;
	private DataOutputStream outToClient;
	private BufferedReader inFromClient;
	private List<String> clientTokens;
	private String clientCommand;
	private boolean clientGo;
	
	public FTPServerThread(Socket controlSocket, int port)
    {
		clientGo = true;
		this.controlSocket = controlSocket;
        clientTokens = new ArrayList<String>();
		try {
			outToClient = new DataOutputStream(controlSocket.getOutputStream());
			inFromClient = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Error: " + e.getLocalizedMessage());
		}
    }
	
	//Might be deprecated? Not sure, it automatically overrides without this,
	//but helps readability to show this overrides the super run() method.
	@Override
	public void run() {
		try {
			while (clientGo) {
				String fromClient = inFromClient.readLine();
				clientTokens.clear();
				StringTokenizer tokens = null;
				if (!(fromClient == null)) {
					tokens = new StringTokenizer(fromClient);
					
					while (tokens.hasMoreTokens())
						clientTokens.add(tokens.nextToken());
					
					System.out.println(clientTokens);
					
					if (clientTokens.size() > 1)
						clientCommand = clientTokens.get(1);
					else 
						clientCommand = clientTokens.get(0);
				} else
					clientCommand = "quit:";
				
				if (clientCommand.equals("list:")) {
					Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
					DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
					String files = getAllFiles();
					dataOutToClient.writeUTF(files);
					dataOutToClient.flush();
					dataOutToClient.close();
					System.out.println("List sent successfully");
					dataSocket.close();
					System.out.println("Data Socket closed.\n");
	
				} else if (clientCommand.equals("retr:")) {
					if (clientTokens.size() < 3) {
						System.out.println("No file supplied.");
						outToClient.writeInt(550);
						continue;
					}
					String filePath = System.getProperty("user.dir") + "/";
					File fileToSend = new File(filePath + clientTokens.get(2));
					if (!fileToSend.exists()) {
						System.out.println("File does not exist.");
						outToClient.writeInt(550);
						continue;
					} else {
						outToClient.writeInt(200);
						Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
						DataOutputStream outData = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
						FileInputStream fis = new FileInputStream(fileToSend);
						BufferedInputStream bis = new BufferedInputStream(fis);
						
						long length = fileToSend.length();
						byte[] bytesToSend = new byte[(int) length];
						bis.read(bytesToSend, 0, (int)length);
						outData.writeInt((int)length);
						outData.write(bytesToSend);
						outData.flush();
						outData.close();
						bis.close();
						fis.close();
						dataSocket.close();
					}
					
				} else if (clientCommand.equals("stor:")) {
					//Skip status code, no need to check if file exists if user is forced to choose.
					Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
					DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
					byte[] dataIn = new byte[inData.readInt()];
					while (inData.available() == 0)
						Thread.sleep(20);
					
					String filePath = System.getProperty("user.dir") + "/";
					filePath += inData.readUTF();
					
					inData.readFully(dataIn);
					try (FileOutputStream fos = new FileOutputStream(filePath)) {
						   fos.write(dataIn);
					}
					inData.close();
					dataSocket.close();
				} else if (clientCommand.equals("quit:")) {
					outToClient.writeUTF("quit:");
					outToClient.close();
					inFromClient.close();
					clientGo = false;
				}
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			e.printStackTrace();
		}
	}
	
	private Socket makeDataSocket(int port) {
		System.out.println("Making data socket...");
		try {
			return new Socket(controlSocket.getInetAddress(), port);
		} catch (IOException e) {
			System.out.println("Error: " + e.toString());
		}
		return null;
	}
	
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
