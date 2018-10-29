import java.awt.event.*;

public class ExitListener extends WindowAdapter {

   ChatClient client;

   public ExitListener(ChatClient client) {
      this.client = client;
   }
      
   public void windowClosing(WindowEvent e) {
      client.disconnect();
      System.exit(0);
   }
}
