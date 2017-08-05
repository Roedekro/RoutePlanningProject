import java.util.ArrayList;

import tool.Node;

public class CHNode extends BiRedBlackNode {

	private static final long serialVersionUID = -5164538509090265370L;
	public ArrayList<Shortcut> shortcutsForward = new ArrayList<Shortcut>();
	public ArrayList<Shortcut> shortcutsBackward = new ArrayList<Shortcut>(); // Reversed
	public long hierarcyLevel = 0;
	public long edgeDistanceTo = 0;
	public long edgeDistanceFrom = 0;
	public boolean deletedFromGraph = false;
	
	public CHNode next = null;
	public CHNode prev = null;
	
	public CHNode(Node node) {
		super(node);
	}
	
	public CHNode(long l, int i, int j) {
		super(l,i,j);
	}
}
