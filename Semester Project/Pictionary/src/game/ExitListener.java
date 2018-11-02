package game;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ExitListener extends WindowAdapter {

	   MainGame client;

	   public ExitListener(MainGame client) {
	      this.client = client;
	   }
	      
	   public void windowClosing(WindowEvent e) {
	      client.disconnect();
	      System.exit(0);
	   }
	}
