
import java.util.ArrayList;

import tool.Edge;
import tool.Node;

/**
 * Wrapper for a Node object to be placed in a Red Black Tree.
 * @author Martin
 *
 */
public class RedBlackNode extends Node {

	private static final long serialVersionUID = -8197458607384982079L;
	public long key = Long.MAX_VALUE;
	public RedBlackNode path = null; // Previous node on the shortest path tree
	public long pathLength = Long.MAX_VALUE;
	public boolean deleted = false;
	public boolean inserted = false;
	public RedBlackNode parent = null;;
	public RedBlackNode leftChild = null;;
	public RedBlackNode rightChild = null;;
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
