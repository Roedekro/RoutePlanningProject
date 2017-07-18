package tool;

import java.util.ArrayList;

/**
 * Wrapper for a Node object to be placed in a Red Black Tree.
 * @author Martin
 *
 */
public class RedBlackNode extends Node {

	private static final long serialVersionUID = -8197458607384982079L;
	public long key = Long.MAX_VALUE;
	public RedBlackNode path; // Previous node on the shortest path tree
	public RedBlackNode parent;
	public RedBlackNode leftChild;
	public RedBlackNode rightChild;
	public boolean colour = false; // False = black, true = red
	
	public RedBlackNode(long id, double lat, double lon) {
		super(id, lat, lon);
	}
	
	public RedBlackNode(Node node) {
		super(node.id, node.lat, node.lon);
		// Copy edges
		this.edges = new ArrayList<Edge>();
		this.edges.addAll(node.edges);
	}

}
