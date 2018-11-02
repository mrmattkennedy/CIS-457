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
	
	volatile private boolean isRunning = false;
	
	public DrawPanel() {
		thisPanel = this;
		
		frame = new JFrame();
		addMouseListener(this);
		frame.getContentPane().add(this);
		frame.setSize(new Dimension(400, 205));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g2 = (Graphics2D)g;
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
	
	public static void main(String[] args) {
		new DrawPanel();
	}
}
