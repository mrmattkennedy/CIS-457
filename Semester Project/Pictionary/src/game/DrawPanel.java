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

public class DrawPanel extends JPanel implements MouseListener {
	
	private int x1, x2, y1, y2;
	private JFrame frame;
	private Graphics2D g2;
	private boolean onPanel;
	private JPanel thisPanel;
	private DrawScreen paFrame;
	private boolean removeGraphics = true;
	
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
	
	@Override
	protected void paintComponent(Graphics g) {
		if (removeGraphics) {
			System.out.println("here");
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

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getButton() == MouseEvent.BUTTON1) {
			x2 = arg0.getX();
			y2 = arg0.getY();
	        isRunning = true;
	        initThread();
	    }
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (arg0.getButton() == MouseEvent.BUTTON1) {
	        isRunning = false;
	    }
		
	}
	
	
	private void initThread() {
	    if (isRunning) {
	        new Thread() {
	            public void run() {
	                do {
	                    if (onPanel) {
	                    	x1 = x2;
	                    	y1 = y2;
	                    	Point p = MouseInfo.getPointerInfo().getLocation();
	                    	SwingUtilities.convertPointFromScreen(p, thisPanel);
	                    	x2 = (int) p.getX();
	                    	y2 = (int) p.getY();
	                    	repaint();
	                    	updateAllPanels();
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
	
	private void updateAllPanels() {
		paFrame.updatePanel(x1, y1, x2, y2);
	}
	
	public void clearScreen() {
		removeGraphics = true;
		repaint();
	}
	
	public void updateDrawing(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		repaint();
	}
}
