import java.util.ArrayList;

import tool.Node;

public class ALTNode extends AStarNode {

	private static final long serialVersionUID = -3468974575000235136L;
	public ArrayList<Integer> landmarksDistances = new ArrayList<Integer>();
	public ALTNode(Node node) {
		super(node);
	}

}
