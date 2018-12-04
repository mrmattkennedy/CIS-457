package game;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

//Panel where drawer can draw, and updates sent to other players.
public class DrawPanel extends JPanel implements MouseListener {
	
	private int x1, x2, y1, y2;
	private boolean onPanel;
	private JPanel thisPanel;
	private DrawScreen paFrame;
	private boolean removeGraphics = true;
	
	//Used to determine if mouse is pressed. Volatile for thread safety.
	volatile private boolean isRunning = false;
	
	
	public DrawPanel(DrawScreen paFrame) {
		thisPanel = this;
		this.paFrame = paFrame;
		addMouseListener(this);
		repaint();
	}
	
	public void addListener() {
		addMouseListener(this);
	}
	
	public void removeListener() {
		isRunning = false;
		removeMouseListener(this);
	}
	
	//Checks if clear was pressed first, then draws.
	@Override
	protected void paintComponent(Graphics g) {
		if (removeGraphics) {
			super.paintComponent(g);
			removeGraphics = false;
		} else
			g.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) { ; }

	@Override
	public void mouseEntered(MouseEvent arg0) {
		onPanel = true;
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		onPanel = false;
		
	}

	//Gets new coordinates, and initializes thread when mouse pressed.
	@Override
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getButton() == MouseEvent.BUTTON1) {
			x2 = arg0.getX();
			y2 = arg0.getY();
	        isRunning = true;
	        initThread();
	    }
		
	}

	//Sets is running to false, kills drawing thread.
	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (arg0.getButton() == MouseEvent.BUTTON1) {
	        isRunning = false;
	    }
		
	}
	
	//Uses volatile isRunning to run thread to draw, else EDT will freeze for this and lock,
	//because mouseReleased can never be called.
	private void initThread() {
	    if (isRunning) {
	        new Thread() {
	            public void run() {
	                do {
	                	//If mouse on panel, draw.
	                    if (onPanel) {
	                    	//Set old x1 and y1, then get new coords relative to panel.
	                    	x1 = x2;
	                    	y1 = y2;
	                    	Point p = MouseInfo.getPointerInfo().getLocation();
	                    	SwingUtilities.convertPointFromScreen(p, thisPanel);
	                    	x2 = (int) p.getX();
	                    	y2 = (int) p.getY();
	                    	repaint();
	                    	updateAllPanels();
	                    	//Sleep so thread doesn't do crazy amounts of drawing/sending.
	                    	try {
								Thread.sleep(20);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
	                    }
	                } while (isRunning);
	                isRunning = false;
	            }
	        }.start();
	    }
	}
	
	//Update all panels of drawing.
	private void updateAllPanels() {
		paFrame.updatePanel(x1, y1, x2, y2);
	}
	
	public void clearScreen() {
		removeGraphics = true;
		repaint();
	}
	
	//Got a drawing, update panel.
	public void updateDrawing(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		repaint();
	}
}
