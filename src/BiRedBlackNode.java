import java.util.ArrayList;

import tool.Edge;
import tool.Node;

public class BiRedBlackNode extends RedBlackNode {

	private static final long serialVersionUID = 6081276626173724589L;
	// Variables for the second Red Black Tree
	public long key2 = Long.MAX_VALUE;
	public BiRedBlackNode path2 = null; // Previous node on the shortest path tree
	public long pathLength2 = Long.MAX_VALUE;
	public boolean deleted2 = false;
	public boolean inserted2 = false;
	public BiRedBlackNode parent2 = null;;
	public BiRedBlackNode leftChild2 = null;;
	public BiRedBlackNode rightChild2 = null;;
	public boolean colour2 = false; // False = black, true = red
	ArrayList<Edge> edges2 = new ArrayList<Edge>();
	
	public BiRedBlackNode(Node node) {
		super(node);
	}

	public BiRedBlackNode(long l, int i, int j) {
		super(l,i,j);
	}

	public void addEdge2(Edge edge) {
		edges2.add(edge);
	}

}
