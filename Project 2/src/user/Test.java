package user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import javax.swing.JFileChooser;

public class Test {
	public static void main(String[] args) throws IOException {
		CentralClient cc = new CentralClient(InetAddress.getLocalHost(), 11230);
		cc.Set("snsiox", "super");
		System.out.println("t");
		cc.Add("Yes", "No");
		System.out.println("a");
		//List<FileInfo> lr = cc.Search("o");
		//for (FileInfo f : lr) {
		//	System.out.println(f.fileName);
		//}
	}
}
