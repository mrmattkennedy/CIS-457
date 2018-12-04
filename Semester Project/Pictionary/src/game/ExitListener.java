package game;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//Checks if player closed window.
public class ExitListener extends WindowAdapter {

	   MainGame client;
	   DrawScreen screen;

	   public ExitListener(MainGame client) {
	      this.client = client;
	   }
	   
	   public ExitListener(DrawScreen client) {
		      this.screen = client;
		   }
	      
	   //Checks if maingame active or screen active.
	   public void windowClosing(WindowEvent e) {
		  if (client != null)
			  client.disconnect();
		  else if (screen != null)
			  screen.disconnect();
	      System.exit(0);
	   }
	}
