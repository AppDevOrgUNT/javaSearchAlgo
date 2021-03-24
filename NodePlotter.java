import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

class NPanel extends JPanel implements MouseListener
{
	private final int SIZE_MULTIPLIER = 5;
	
	BufferedImage outline; 
	
	Node graph; 
	
	LinkedList<Node> nodes; 
	
	Node closest; 
	
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
		
		nodes = new LinkedList<>(); 
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
			
			gr.setColor(Color.BLUE);
			for (Node connection : node.connectors)
			{
				gr.drawLine((int)node.x * SIZE_MULTIPLIER, (int)node.y * SIZE_MULTIPLIER, (int)connection.x * SIZE_MULTIPLIER, (int)connection.y * SIZE_MULTIPLIER);
			}
		}
	}
	
	void save()
	{
		try 
		{
			FileWriter file = new FileWriter(new File("nodes.txt")); 
			
			file.write(nodes.size() + "\n");
			
			int index = 0; 
			for (Node node : nodes)
			{
				file.write(node.key + " " + node.x + " " + node.y + " ");
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
		if (e.getButton() == MouseEvent.BUTTON1)
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
				nodes.add(new Node(nodes.size(), e.getX() / SIZE_MULTIPLIER, e.getY() / SIZE_MULTIPLIER)); 
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
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
}