import tool.Edge;

public class Shortcut extends Edge {

	private static final long serialVersionUID = -7586138079585985522L;
	public boolean upward = false;
	public long level = 0; // Rank of the node it points to
	public long fromID;

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
	
	// contrucstor for reverse shortcut
	public Shortcut(Shortcut shortcut) {
		super(shortcut.fromID," ",0,0,shortcut.travelTime);
		this.level = shortcut.level;
		this.fromID = shortcut.nodeID;
		this.upward = false;
	}
	
}
