import tool.Edge;

public class Shortcut extends Edge {

	private static final long serialVersionUID = -7586138079585985522L;
	public boolean upward = false;
	public long level = 0; // Rank of the node it points to
	public long fromID;
	public long node; // The node that is cut out

	// long id, String type, int distance, int maxSpeed, int travelTime
	public Shortcut(Edge e) {
		super(e.nodeID, e.type, e.distance, e.maxSpeed, e.travelTime);
	}
	
	public Shortcut(long id, long fromID, int travelTime, long level, boolean upward) {
		super(id," ",0,0,travelTime);
		this.level = level;
		this.fromID = fromID;
		this.upward = upward;
	}
	
	public Shortcut(long id, long fromID, int travelTime, long level, boolean upward, long node) {
		super(id," ",0,0,travelTime);
		this.level = level;
		this.fromID = fromID;
		this.upward = upward;
		this.node = node;
	}
	
	// contrucstor for reverse shortcut
	public Shortcut(Shortcut shortcut, long newLevel) {
		super(shortcut.fromID," ",0,0,shortcut.travelTime);
		this.level = newLevel;
		this.fromID = shortcut.nodeID;
		this.upward = false;
		this.node = shortcut.node;
	}
	
}
