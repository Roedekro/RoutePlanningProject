import tool.Node;

public class AStarNode extends BiRedBlackNode {

	private static final long serialVersionUID = 6997277459570669339L;
	public long potential = 0; 
	public long potential2 = 0;
	public long keyLength = Long.MAX_VALUE;
	public long keyLength2 = Long.MAX_VALUE;
	public long realPathLength = Long.MAX_VALUE;
	public long realPathLength2 = Long.MAX_VALUE;
	
	public AStarNode(Node node) {
		super(node);
	}
	
	public AStarNode(long l, int i, int j) {
		super(l, i, j);
	}

}
