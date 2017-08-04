import java.util.ArrayList;

import tool.Node;

public class CHNode extends BiRedBlackNode {

	private static final long serialVersionUID = -5164538509090265370L;
	public ArrayList<Shortcut> shortcutsUpward = new ArrayList<Shortcut>();
	public ArrayList<Shortcut> shortcutsDownward = new ArrayList<Shortcut>(); // Reversed
	public long hierarcyLevel = 0;
	public long edgeDistance = 0;
	public boolean deletedFromGraph = false;
	
	public CHNode(Node node) {
		super(node);
	}
	
	public CHNode(long l, int i, int j) {
		super(l,i,j);
	}
}
