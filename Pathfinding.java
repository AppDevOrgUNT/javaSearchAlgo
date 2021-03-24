import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Pathfinding 
{
	public static void main(String[] args) 
	{
		new Frame(); 
	}
}

class Frame extends JFrame 
{
	public Frame()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		
		add(new Panel()); 
		pack(); 
		
		setVisible(true); 
	}
}

class Panel extends JPanel implements MouseMotionListener
{
	private final int SIZE_MULTIPLIER = 5;
	
	BufferedImage outline; 
	
	Node graph; 
	
	Node[] nodes; 
	
	LinkedList<Node> bestPath; 
	
	int mouseX, mouseY; 
	
	int hallwayX, hallwayY; 
	
	public Panel()
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
		addMouseMotionListener(this); 
		
		graph = createGraph(); 
		// pathfind(STARTING_POS, ENDING_POS); 
	}
	
	public void paint(Graphics g)
	{
		Graphics2D gr = (Graphics2D)g; 
		
		gr.setColor(Color.white);
		gr.fillRect(0, 0, outline.getWidth() * SIZE_MULTIPLIER, outline.getHeight() * SIZE_MULTIPLIER);
		gr.drawImage(outline, 0, 0, outline.getWidth() * SIZE_MULTIPLIER, outline.getHeight() * SIZE_MULTIPLIER, this); 
		
		gr.setColor(Color.black);	
		gr.setStroke(new BasicStroke(4));
		Node current = null; 
		if (bestPath != null)
		{
			for (Node node : bestPath)
			{
				if (current == null) current = node; 
				
				gr.drawLine((int)current.x * SIZE_MULTIPLIER, (int)current.y * SIZE_MULTIPLIER, (int)node.x * SIZE_MULTIPLIER, (int)node.y * SIZE_MULTIPLIER);
				current = node; 
			}
			
			gr.drawLine((int)hallwayX * SIZE_MULTIPLIER, (int)hallwayY * SIZE_MULTIPLIER, (int)current.x * SIZE_MULTIPLIER, (int)current.y * SIZE_MULTIPLIER); 
			gr.drawLine(mouseX, mouseY, hallwayX * SIZE_MULTIPLIER, hallwayY * SIZE_MULTIPLIER); 
		}
	}
	
	private Node createGraph()
	{	
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader("nodes.txt")); 
			
			Scanner sizeScanner = new Scanner(reader.readLine()); 
			nodes = new Node[sizeScanner.nextInt()]; 
			sizeScanner.close(); 
			
			String line = reader.readLine(); 
			for (int index = 0; index < nodes.length; index++)
			{
				Scanner scanner = new Scanner(line); 
				nodes[index] = new Node(scanner.nextInt(), scanner.nextDouble(), scanner.nextDouble());
				scanner.close();
				
				line = reader.readLine(); 
			}
			
			while (line.charAt(0) != '$')
			{
				Scanner scanner = new Scanner(line); 
				int a = scanner.nextInt(), 
					b = scanner.nextInt(); 
				
				nodes[a].connectors.add(nodes[b]);
				nodes[b].connectors.add(nodes[a]);
				
				scanner.close(); 
				
				line = reader.readLine(); 
			}
			
			reader.close(); 
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
		return nodes[0]; 
	}
	
	private LinkedList<Node> pathfind(int destinationKey)
	{	
		HashMap<Integer, Double> visitedDistances = new HashMap<>(); 
		visitedDistances.put(graph.key, 0.0); 
		
		LinkedList<Node> path = new LinkedList<>();
		path.add(graph);
		
		bestPath = new LinkedList<>(); 		
		pathfind(destinationKey, graph, visitedDistances, path, Double.MAX_VALUE); 
		
		return bestPath; 
	}
	
	@SuppressWarnings("unchecked")
	private void pathfind(int destinationKey, Node current, HashMap<Integer, Double> visited, LinkedList<Node> path, double bestDist)
	{
		if (current.key == destinationKey) return; 
		
		for (Node node : current.connectors)
		{
			double newDist = current.distance(node) + visited.get(current.key); 
			if (!visited.containsKey(node.key) || visited.get(node.key) > newDist)
			{
				visited.put(node.key, newDist); 
				path.add(node); 
				pathfind(destinationKey, node, visited, path, bestDist); 
				if (path.getLast().key == destinationKey && visited.get(destinationKey) < bestDist)
				{
					bestDist = visited.get(destinationKey); 
					bestPath = (LinkedList<Node>)path.clone();
				}
				path.remove(node);
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		//What two nodes are we closest to? 
		Node c1 = null, c2 = null; 
		double dist1 = Double.MAX_VALUE, dist2 = Double.MAX_VALUE; 
		for (Node node : nodes)
		{
			double dist = Node.distance(e.getX() / SIZE_MULTIPLIER, e.getY() / SIZE_MULTIPLIER, node.x, node.y); 
			
			if (dist < dist1)
			{
				if (dist1 < dist2)
				{
					c2 = c1; 
					dist2 = dist1; 
				}
				
				c1 = node; 
				dist1 = dist; 
			}
			else if (dist < dist2)
			{
				c2 = node; 
				dist2 = dist; 
			}
		}
		
		pathfind(c1.key); 
		
		// Line between these two points. 
		double dx = c2.x - c1.x; 
		double slope = (c2.y - c1.y) / (dx == 0 ? 0.001 : dx), 
			   b = c1.y - slope * c1.x;
		
		// Perpendicular line
		double perpSlope = -1d / (slope == 0 ? 0.001 : slope); 
		
		// Centered at our mouse position. 
		double perpB = e.getY() / SIZE_MULTIPLIER - perpSlope * (e.getX() / SIZE_MULTIPLIER);
		
		// Get point of intersection. 
		double interS = slope - perpSlope, 
			   interB = perpB - b, 
			   interX = interB / interS;
		
		hallwayX = (int)interX; 
		hallwayY = (int)(perpSlope * interX + perpB); 
		
		mouseX = e.getX(); 
		mouseY = e.getY(); 
		
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class Node
{
	LinkedList<Node> connectors; 
	int key; 
	double x, y; 
	
	Node(int key, double x, double y) 
	{
		this.key = key;
		this.x = x; 
		this.y = y; 
		connectors = new LinkedList<Node>();
	}
	
	Node add(int key, double x, double y)
	{ 
		Node node = new Node(key, x, y);
		connectors.add(node);
		return node; 
	}
	
	void connect(int otherKey)
	{
		HashSet<Integer> visited = new HashSet<>();
		LinkedList<Node> unvisited = new LinkedList<>();
		visited.add(key);
		unvisited.addAll(connectors);
		
		while (unvisited.size() > 0)
		{
			Node current = unvisited.removeFirst(); 
			for (Node node : current.connectors)
			{
				if (!visited.contains(node.key))
				{
					visited.add(node.key);
					unvisited.addLast(node);
					
					if (node.key == otherKey)
					{
						connectors.add(node);
						return; 
					}
				}
			}
		}
	}
	
	void connect(Node other)
	{
		connectors.add(other); 
		other.connectors.add(this); 
	}
	
	double distance(Node other)
	{
		return distance(this, other); 
	}
	
	static double distance(Node left, Node right)
	{
		return distance(left.x, left.y, right.x, right.y); 
	}
	
	static double distance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)); 
	}
	
	public String toString()
	{
		return key + "";
	}
}