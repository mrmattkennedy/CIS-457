package user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.InetAddress;
import java.util.List;

import javax.swing.JFileChooser;

public class Test {
	public static void main(String[] args) throws IOException {
		FileServer pot = new FileServer(9874);
		pot.Add("q2.pdf", Paths.get("/home/batescol/q2.pdf"));
		Thread thread = new Thread(pot);
		thread.start();
		
	}
}
