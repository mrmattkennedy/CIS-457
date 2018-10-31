package user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;

public class Test {
	public static void main(String[] args) throws IOException {
		String test = "abc123";
		test = test.substring(0, test.length() - 1);
		System.out.println(test);
	}
}
