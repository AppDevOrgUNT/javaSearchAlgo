import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class NodePlotter 
{
	public static void main(String[] args) 
	{
		new NFrame(); 
	}
}

class NFrame extends JFrame 
{
	public NFrame()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		
		add(new NPanel()); 
		pack(); 
		
		setVisible(true); 
	}
}

class NPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener
{
	private final int SIZE_MULTIPLIER = 5;
	
	private final int MIN_LINE_FRAMES = 50;
	
	BufferedImage outline; 
	
	Node graph; 
	
	LinkedList<Node> nodes; 
	
	Node closest; 
	
	boolean textKeyPressed, lineKeyPressed; 
	
	Node textNode; 
	
	Node lastDraggedNode; 
	
	int frameCounter; 
	
	public NPanel()
	{
		try 
		{
			outline = ImageIO.read(new File("outline.png")); 
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}
		 
		setPreferredSize(new Dimension(outline.getWidth() * SIZE_MULTIPLIER, outline.getHeight() * SIZE_MULTIPLIER));
		addMouseListener(this); 
		addMouseMotionListener(this); 
		addKeyListener(this); 
		setFocusable(true); 
		
		nodes = new LinkedList<>(); 
		
		textKeyPressed = false;
		lineKeyPressed = false;
		 
		lastDraggedNode = null; 
		frameCounter = 0; 
	}
	
	public void paint(Graphics g)
	{
		Graphics2D gr = (Graphics2D)g;
		
		gr.setColor(Color.white);
		gr.fillRect(0, 0, outline.getWidth() * SIZE_MULTIPLIER, outline.getHeight() * SIZE_MULTIPLIER);
		
		gr.drawImage(outline, 0, 0, outline.getWidth() * SIZE_MULTIPLIER, outline.getHeight() * SIZE_MULTIPLIER, this); 
		
		gr.setStroke(new BasicStroke(3));
		for (Node node : nodes)
		{
			gr.setColor(Color.RED);
			gr.fillRect((int)(node.x-1) * SIZE_MULTIPLIER, (int)(node.y-1) * SIZE_MULTIPLIER, 10, 10);
			gr.drawString(node.text, (int)(node.x-5)*SIZE_MULTIPLIER, (int)(node.y-5)*SIZE_MULTIPLIER);
			
			gr.setColor(Color.BLUE);
			for (Node connection : node.connectors)
			{
				gr.drawLine((int)node.x * SIZE_MULTIPLIER, (int)node.y * SIZE_MULTIPLIER, (int)connection.x * SIZE_MULTIPLIER, (int)connection.y * SIZE_MULTIPLIER);
			}
		}
	}
	
	Node addNode(int screenX, int screenY)
	{
		Node node = new Node(nodes.size(), screenX / SIZE_MULTIPLIER, screenY / SIZE_MULTIPLIER); 
		nodes.add(node);
		return node; 
	}
	
	void save()
	{
		try 
		{
			FileWriter file = new FileWriter(new File("nodes.txt")); 
			
			file.write(nodes.size() + "\n");
			
			for (Node node : nodes)
			{
				file.write(node.key + " " + node.text + " " + node.x + " " + node.y);
				file.write('\n');
			}
			for (Node node : nodes)
			{
				for (Node connection : node.connectors)
				{
					file.write(node.key + " " + connection.key + "\n");
				}
			}
			file.write("$");
			
			file.close(); 
		}
		catch (IOException ex) 
		{ 
			ex.printStackTrace();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3)
		{
			save(); 
		}
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			int mouseX = e.getX() / SIZE_MULTIPLIER, mouseY = e.getY() / SIZE_MULTIPLIER; 
			double shortestDist = Double.MAX_VALUE; 
			
			// Get closest node 
			for (Node node : nodes)
			{
				double dist = Node.distance(mouseX, mouseY, node.x, node.y);
				if (dist < shortestDist)
				{
					shortestDist = dist; 
					closest = node; 
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		if (e.getButton() == MouseEvent.BUTTON1 && !lineKeyPressed)
		{
			int mouseX = e.getX() / SIZE_MULTIPLIER, mouseY = e.getY() / SIZE_MULTIPLIER; 
			double shortestDist = Double.MAX_VALUE; 
			
			Node releasedClosest = null; 
			
			// Get closest node 
			for (Node node : nodes)
			{
				double dist = Node.distance(mouseX, mouseY, node.x, node.y);
				if (dist < shortestDist)
				{
					shortestDist = dist; 
					releasedClosest = node; 
				}
			}
			
			if (closest == null || closest == releasedClosest)
			{
				if (textKeyPressed)
				{
					textNode = closest; 
				}
				else 
				{
					addNode(e.getX(), e.getY());
				}
			}
			else 
			{
				Node a = null, b = null; 
				for (Node node : nodes)
				{
					if (node.key == closest.key) a = closest; 
					else if (node.key == releasedClosest.key) b = releasedClosest; 
				}
				
				a.connectors.add(b);
				b.connectors.add(a); 
			} 
			
			repaint();
		} 
		
		lastDraggedNode = null; 
		
		lineKeyPressed = false; 
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (textNode != null)
		{
			if (e.getKeyChar() == '/')
			{
				// Enter
				textNode = null; 
			}
			else if (e.getKeyChar() == '\\' && textNode.text.length() > 0)
			{
				// Backspace
				textNode.text = textNode.text.substring(0, textNode.text.length()-1); 
			}
			else if (e.getKeyChar() != '1')
			{
				textNode.text += e.getKeyChar();
			}
			
			repaint(); 
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_1)
		{
			textKeyPressed = true; 
		}
		else if (!lineKeyPressed && e.getKeyCode() == KeyEvent.VK_2)
		{
			lineKeyPressed = true; 
			frameCounter = 0; 
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_1)
		{
			textKeyPressed = false; 
		}
		else if (e.getKeyCode() == KeyEvent.VK_2)
		{
			lineKeyPressed = false; 
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) 
	{
		frameCounter++; 
		if (lineKeyPressed == true)
		{
			if (frameCounter > MIN_LINE_FRAMES)
			{
				Node toAdd = addNode(e.getX(), e.getY());
				if (lastDraggedNode != null)
				{
					lastDraggedNode.connectors.add(toAdd); 
					toAdd.connectors.add(lastDraggedNode); 
				}
				
				lastDraggedNode = toAdd; 
				
				repaint(); 
				
				frameCounter = 0; 
				closest = null; 
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		
	}
}