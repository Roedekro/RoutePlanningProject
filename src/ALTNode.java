import java.util.ArrayList;

import tool.Node;

public class ALTNode extends AStarNode {

	private static final long serialVersionUID = -3468974575000235136L;
	public ArrayList<Long> landmarksForwardDistances = new ArrayList<Long>(); // Distance to landmark
	public ArrayList<Long> landmarksBackwardDistances = new ArrayList<Long>(); // Distance from landmark
	public ALTNode(Node node) {
		super(node);
	}

}
