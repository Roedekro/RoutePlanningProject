import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import tool.Edge;
import tool.Node;
import tool.Tool;

public class CH {
	
	private int hoplimit = 100;
	private static int shift = 33;
	private int pqdif = 10000;
	private int lazyUpdateLimit = 5;

	private long preprocessStart = 0;
	private long preprocessStop = 0;
	private long queryStart = 0;
	private long queryStop = 0;
	private long preprocessTotal = 0;
	private long queryTotal = 0;
	long preprocessTime = 0;
	long queryTime = 0;
	ArrayList<CHNode> check = null;
	long nodesChecked = 0;
	HashMap<Long,CHNode> hashMap = null;
	
	public long CHNaivebyNodeID(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		/*ArrayList<Node> normalNodes = createTest();
		source = 8;
		target = 1;*/
		
		ArrayList<CHNode> nodes = new ArrayList<CHNode>();
		CHNode sourceNode = null;
		CHNode targetNode = null;
		CHNode node = null;
		hashMap = new HashMap<Long,CHNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new CHNode(normalNodes.get(i));
			node.hierarcyLevel = node.id; // Naive approach
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Fill in reverse edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			CHNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		// Contract edges
		for(int i = 0; i < nodes.size(); i++) {
			
			
			node = nodes.get(i);
			
			//System.out.println("=== Contracting node "+node.id+" "+i);
			
			// Add neighbours
			ArrayList<CHNode> neighboursIngoing = new ArrayList<CHNode>(); // List of higher ranked neighbours
			ArrayList<CHNode> neighboursOutgoing = new ArrayList<CHNode>();
			CHNode neighbour = null;
			// Find all ingoing and outgoing edges in the current graph
			Edge nEdge = null;
			for(int j = 0; j < node.edges.size(); j++) {
				nEdge = node.edges.get(j);
				neighbour = hashMap.get(nEdge.nodeID);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistanceTo = nEdge.travelTime;
					neighboursOutgoing.add(neighbour);
				}
			}
			Shortcut nShort = null;
			for(int j = 0; j < node.shortcutsForward.size(); j++) {
				nShort = node.shortcutsForward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistanceTo = nShort.travelTime;
					neighboursOutgoing.add(neighbour);
				}
			}
			
			for(int j = 0; j < node.edges2.size(); j++) {
				nEdge = node.edges2.get(j);
				neighbour = hashMap.get(nEdge.nodeID);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistanceFrom = nEdge.travelTime;
					neighboursIngoing.add(neighbour);
				}
			}
			
			for(int j = 0; j < node.shortcutsBackward.size(); j++) {
				nShort = node.shortcutsBackward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				////System.out.println("In "+neighbour.id+" "+neighbour.deletedFromGraph);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistanceFrom = nShort.travelTime;
					neighboursIngoing.add(neighbour);
				}
			}
			
			//System.out.println(node.id+" "+neighboursIngoing.size()+" "+neighboursOutgoing.size());
			
			// Now for all ingoing edges (w,v) find the shortest path excluding v
			// from w to all w' in the outgoing edges (v,w').
			// We do this using a normal Dijkstra search.
			CHNode localSource = null;
			ArrayList<Shortcut> toBeAdded = new ArrayList<Shortcut>();
			if(neighboursIngoing.size() > 0 && neighboursOutgoing.size() > 0) {
				for(int j = 0; j < neighboursIngoing.size(); j++) {
					ArrayList<CHNode> toBeReset = new ArrayList<CHNode>();
					localSource = neighboursIngoing.get(j);
					//System.out.println(node.id+" Checking ingoing from "+localSource.id);
					localSource.key = 0;
					localSource.pathLength = 0;
					RedBlackTree tree = new RedBlackTree();
					tree.insertNode(localSource);
					toBeReset.add(localSource);
					long counter = 0;
					int targetCounter = neighboursOutgoing.size();
					CHNode min = null;
					while(counter < hoplimit && targetCounter > 0 && tree.size > 0) {
						counter++;
						min = (CHNode) tree.deleteMin();
						//System.out.println("Extracted "+min.id);
						if(neighboursOutgoing.contains(min)) {
							targetCounter--;
						}
						Edge edge = null;
						CHNode decreaseNode = null;
						for(int x = 0; x < min.edges.size(); x++) {
							edge = min.edges.get(x);
							decreaseNode = hashMap.get(edge.nodeID);
							long newPathLength = min.pathLength + edge.travelTime;
							// Only nodes with higher rank
							if(decreaseNode.id != node.id && !decreaseNode.deletedFromGraph && 
									!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
								decreaseNode.path = min;
								decreaseNode.pathLength = newPathLength;
								if(decreaseNode.inserted) {
									//System.out.println("Decreasing "+decreaseNode.id);
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									//System.out.println("Inserting "+decreaseNode.id);
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
						// Same for shortcuts
						Shortcut shortcut = null;
						for(int x = 0; x < min.shortcutsForward.size(); x++) {
							shortcut = min.shortcutsForward.get(x);
							decreaseNode = hashMap.get(shortcut.nodeID);
							long newPathLength = min.pathLength + shortcut.travelTime;
							// Only nodes with higher rank
							if(decreaseNode.id != node.id && !decreaseNode.deletedFromGraph && 
									!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
								decreaseNode.path = min;
								decreaseNode.pathLength = newPathLength;
								if(decreaseNode.inserted) {
									//System.out.println("Decreasing "+decreaseNode.id);
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									//System.out.println("Inserting "+decreaseNode.id);
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
					}
					
					// Dijkstra completed, evaluate
					for(int x = 0; x < neighboursOutgoing.size(); x++) {
						min = neighboursOutgoing.get(x);
						//System.out.println(min.id+" "+min.pathLength+" "+(min.edgeDistanceTo+ localSource.edgeDistanceFrom));
						if(min.pathLength > min.edgeDistanceTo + localSource.edgeDistanceFrom) {
							// Found shortcut
							// long id, long fromID, int travelTime, long level, boolean upward
							Shortcut shortcut = new Shortcut(min.id,localSource.id,(int) (min.edgeDistanceTo + localSource.edgeDistanceFrom),min.hierarcyLevel,true,node.id);
							toBeAdded.add(shortcut);
						}
					}
					
					// Reset
					for(int x = 0; x < toBeReset.size(); x++) {
						min = toBeReset.get(x);
						min.key = Long.MAX_VALUE;
						min.deleted = false;
						min.inserted = false;
						min.path = null;
						min.pathLength = Long.MAX_VALUE;
					}
				}
			}
			
			// Add the shortcuts
			Shortcut shortcut = null;
			Shortcut reverseShortcut = null;
			CHNode fromNode = null;
			CHNode toNode = null;
			for(int x = 0; x < toBeAdded.size(); x++) {
				shortcut = toBeAdded.get(x);
				if(shortcut.nodeID != shortcut.fromID) {
					fromNode = hashMap.get(shortcut.fromID);
					toNode = hashMap.get(shortcut.nodeID);
					reverseShortcut = new Shortcut(shortcut,fromNode.hierarcyLevel);

					//System.out.println("Adding shortcut forward "+shortcut.fromID+" "+shortcut.nodeID +" "+shortcut.travelTime);
					fromNode.shortcutsForward.add(shortcut);
					//System.out.println("Adding shortcut backward "+reverseShortcut.fromID+" "+reverseShortcut.nodeID+" "+reverseShortcut.travelTime);
					toNode.shortcutsBackward.add(reverseShortcut);
				}	
			}
			
			node.deletedFromGraph = true;	
		}
		
		// All nodes now contracted	
		//System.out.println("=============== Dijkstra");
		
		CHNode meet = null;
		long shortest = Long.MAX_VALUE;
		CHNode smallest = null;
		long newMin = 0;
		// Perform the search.
		// The forward search will use upward shortcuts
		// and the reverse search will use downward shortcuts.
		
		RedBlackTree tree = new RedBlackTree();
		BiRedBlackTree biTree = new BiRedBlackTree();
		
		sourceNode.key = 0;
		sourceNode.path = null;
		sourceNode.pathLength = 0;
		sourceNode.inserted = true;
		targetNode.key2 = 0;
		targetNode.path2 = null;
		targetNode.pathLength2 = 0;
		targetNode.inserted2 = true;
		
		tree.insertNode(sourceNode);
		biTree.insertNode(targetNode);
		
		CHNode node1 = null;
		CHNode node2 = null;
		
		while(tree.size > 0 || biTree.size > 0) {
			if(tree.size > 0) {
				node1 = (CHNode) tree.deleteMin();
				//System.out.println("Extracted from 1 "+node1.id);
				//System.out.println(node1.edges.size()+" "+node1.edges2.size()+" "+node1.shortcutsForward.size()+" "+node1.shortcutsBackward.size());
				nodesChecked++;
			}
			node1.deleted = true;
			if(biTree.size > 0) {
				node2 = (CHNode) biTree.deleteMin();
				//System.out.println("Extracted from 2 "+node2.id);
				//System.out.println(node2.edges.size()+" "+node2.edges2.size()+" "+node2.shortcutsForward.size()+" "+node2.shortcutsBackward.size());
				nodesChecked++;
			}
			node2.deleted2 = true;
			/*if(node1.deleted2) {
				meet = node1;
				shortest = node1.pathLength + node1.pathLength2;
				//System.out.println("Met1");
				break;
			}
			if(node2.deleted) {
				meet = node2;
				shortest = node2.pathLength + node2.pathLength2;
				//System.out.println("Met2");
				break;
			}*/
			if(node1.pathLength >= shortest && node2.pathLength2 >= shortest) {
				//System.out.println("Break");
				break;
			}

			
			CHNode decreaseNode1 = null;
			Edge edge1 = null;
			for(int i = 0; i < node1.edges.size(); i++) {
				edge1 = node1.edges.get(i);
				decreaseNode1 = hashMap.get(edge1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + edge1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting edge1 from "+node1.id+" to "+decreaseNode1.id+" "+edge1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}				
			Shortcut shortcut1 = null;
			for(int i = 0; i < node1.shortcutsForward.size(); i++) {
				shortcut1 = node1.shortcutsForward.get(i);
				decreaseNode1 = hashMap.get(shortcut1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + shortcut1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting forward shortcut from "+ node1.id+" to "+decreaseNode1.id+" "+shortcut1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}
			
			CHNode decreaseNode2 = null;
			Edge edge2 = null;
			for(int i = 0; i < node2.edges2.size(); i++) {
				edge2 = node2.edges2.get(i);
				decreaseNode2 = hashMap.get(edge2.nodeID);
				if(decreaseNode2.hierarcyLevel > node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + edge2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting edge2 from "+decreaseNode2.id+" to "+node2.id+" "+edge2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}

			Shortcut shortcut2 = null;
			for(int i = 0; i < node2.shortcutsBackward.size(); i++) {
				shortcut2 = node2.shortcutsBackward.get(i);
				decreaseNode2 = hashMap.get(shortcut2.nodeID);
				if(decreaseNode2.hierarcyLevel > node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + shortcut2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting backward shortcut from "+decreaseNode2.id+" to "+node2.id+" "+shortcut2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
			
		}
		
		// We found a shortest node, but in order to retrieve the shortest path we need to "unpack" it
		// by recursively turning shortcuts into sub-shortcuts/edges until only edges remains.

		node = smallest;
		CHNode nextNode = null;
		
		// Reverse
		while(node.id != sourceNode.id) {
			nextNode = (CHNode) node.path;
			node.prev = nextNode;
			nextNode.next = node;
			node = nextNode;
		}
		// Forward
		node = smallest;
		while(node.id != targetNode.id) {
			nextNode = (CHNode) node.path2;
			node.next = nextNode;
			nextNode.prev = node;
			node = nextNode;
		}
		
		// Unpack the route to source
		// Unpack uses too many recursive calls, and will run out of stack space
		//unpack(sourceNode, targetNode);
		
		// Iterativ approach instead. Unpack left to right.
		//System.out.println("Unpacking shortcuts");
		node = sourceNode;
		while(node.id != targetNode.id) {
			
			nextNode = node.next;
			boolean edgeExists = false; // Denotes that an edge exists AND that there isnt a shorter shortcut.
			Edge edge = null;
			for(int i = 0; i < node.edges.size(); i++) {
				edge = node.edges.get(i);
				if(edge.nodeID == nextNode.id) {
					edgeExists = true;
					break;
				}
			}
			Shortcut shortcut = null;
			Shortcut s = null;
			int min = edge.distance;
			// Find shortest shortcut
			for(int i = 0; i < node.shortcutsForward.size(); i++) {
				shortcut = node.shortcutsForward.get(i);
				if(shortcut.nodeID == nextNode.id) {
					if(shortcut.distance < min) {
						s = shortcut;
						edgeExists = false;
					}
				}
			}
			
			if(edgeExists) {
				node = nextNode;
			}
			else if(s != null){
				CHNode insert = hashMap.get(s.node);
				node.next = insert;
				insert.prev = node;
				insert.next = nextNode;
				nextNode.prev = insert;
			}
			else {
				System.out.println("No edge or shortcut node "+node.id+" "+nextNode.id);
			}
		}
		
		
		check = new ArrayList<CHNode>();
		node = sourceNode;
		while(node.id != targetNode.id) {
			check.add(node);
			node = node.next;
		}
		check.add(targetNode);
		
		
		//System.out.println("Sizes "+tree.size + " " + biTree.size);
		////System.out.println("Meet="+meet.id+" "+meet.pathLength+" "+meet.pathLength2);
		//System.out.println(shortest);
		//System.out.println("Shortest = "+smallest.id+" "+smallest.pathLength+" "+smallest.pathLength2);
		return shortest;
	}
	

	/**
	 * Simulate the removal of node from the Graph.
	 * Return the difference in edges between the new and the old graph.
	 * Also takes into account the neighbours already deleted.
	 * @param node to simulate removal on
	 * @param nodes ArrayList of CHNodes forming a graph
	 * @return EdgeDifference, can be negative
	 */
	private int calculatePriority(CHNode node, ArrayList<CHNode> nodes) {
		
		// Add neighbours
		ArrayList<CHNode> neighboursIngoing = new ArrayList<CHNode>(); // List of higher ranked neighbours
		ArrayList<CHNode> neighboursOutgoing = new ArrayList<CHNode>();
		CHNode neighbour = null;
		int neighboursDeleted = 0;
		// Find all ingoing and outgoing edges in the current graph
		Edge nEdge = null;
		for(int j = 0; j < node.edges.size(); j++) {
			nEdge = node.edges.get(j);
			neighbour = hashMap.get(nEdge.nodeID);
			if(!neighbour.deletedFromGraph) {
				neighbour.edgeDistanceTo = nEdge.travelTime;
				neighboursOutgoing.add(neighbour);
			}
			else {
				neighboursDeleted++;
			}
		}
		Shortcut nShort = null;
		for(int j = 0; j < node.shortcutsForward.size(); j++) {
			nShort = node.shortcutsForward.get(j);
			neighbour = hashMap.get(nShort.nodeID);
			if(!neighbour.deletedFromGraph) {
				neighbour.edgeDistanceTo = nShort.travelTime;
				neighboursOutgoing.add(neighbour);
			}
			else {
				neighboursDeleted++;
			}
		}
		
		for(int j = 0; j < node.edges2.size(); j++) {
			nEdge = node.edges2.get(j);
			neighbour = hashMap.get(nEdge.nodeID);
			if(!neighbour.deletedFromGraph) {
				neighbour.edgeDistanceFrom = nEdge.travelTime;
				neighboursIngoing.add(neighbour);
			}
			else {
				neighboursDeleted++;
			}
		}
		
		for(int j = 0; j < node.shortcutsBackward.size(); j++) {
			nShort = node.shortcutsBackward.get(j);
			neighbour = hashMap.get(nShort.nodeID);
			////System.out.println("In "+neighbour.id+" "+neighbour.deletedFromGraph);
			if(!neighbour.deletedFromGraph) {
				neighbour.edgeDistanceFrom = nShort.travelTime;
				neighboursIngoing.add(neighbour);
			}
			else {
				neighboursDeleted++;
			}
		}
		
		//System.out.println(node.id+" "+neighboursIngoing.size()+" "+neighboursOutgoing.size());
		
		// Now for all ingoing edges (w,v) find the shortest path excluding v
		// from w to all w' in the outgoing edges (v,w').
		// We do this using a normal Dijkstra search.
		CHNode localSource = null;
		int toBeAdded = 0;
		int toBeRemoved = node.edges.size() + node.edges2.size() +
				node.shortcutsForward.size() + node.shortcutsBackward.size();
		if(neighboursIngoing.size() > 0 && neighboursOutgoing.size() > 0) {
			for(int j = 0; j < neighboursIngoing.size(); j++) {
				ArrayList<CHNode> toBeReset = new ArrayList<CHNode>();
				localSource = neighboursIngoing.get(j);
				//System.out.println(node.id+" Checking ingoing from "+localSource.id);
				localSource.key = 0;
				localSource.pathLength = 0;
				RedBlackTree tree = new RedBlackTree();
				tree.insertNode(localSource);
				toBeReset.add(localSource);
				long counter = 0;
				int targetCounter = neighboursOutgoing.size();
				CHNode min = null;
				while(counter < hoplimit && targetCounter > 0 && tree.size > 0) {
					counter++;
					min = (CHNode) tree.deleteMin();
					//System.out.println("Extracted "+min.id);
					if(neighboursOutgoing.contains(min)) {
						targetCounter--;
					}
					Edge edge = null;
					CHNode decreaseNode = null;
					for(int x = 0; x < min.edges.size(); x++) {
						edge = min.edges.get(x);
						decreaseNode = hashMap.get(edge.nodeID);
						long newPathLength = min.pathLength + edge.travelTime;
						// Only nodes with higher rank
						if(decreaseNode.id != node.id && !decreaseNode.deletedFromGraph && 
								!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
							decreaseNode.path = min;
							decreaseNode.pathLength = newPathLength;
							if(decreaseNode.inserted) {
								//System.out.println("Decreasing "+decreaseNode.id);
								tree.decreaseKey(decreaseNode, newPathLength);
							}
							else {
								//System.out.println("Inserting "+decreaseNode.id);
								toBeReset.add(decreaseNode);
								decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
								decreaseNode.inserted = true;
								tree.insertNode(decreaseNode);
							}
						}
					}
					// Same for shortcuts
					Shortcut shortcut = null;
					for(int x = 0; x < min.shortcutsForward.size(); x++) {
						shortcut = min.shortcutsForward.get(x);
						decreaseNode = hashMap.get(shortcut.nodeID);
						long newPathLength = min.pathLength + shortcut.travelTime;
						// Only nodes with higher rank
						if(decreaseNode.id != node.id && !decreaseNode.deletedFromGraph && 
								!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
							decreaseNode.path = min;
							decreaseNode.pathLength = newPathLength;
							if(decreaseNode.inserted) {
								//System.out.println("Decreasing "+decreaseNode.id);
								tree.decreaseKey(decreaseNode, newPathLength);
							}
							else {
								//System.out.println("Inserting "+decreaseNode.id);
								toBeReset.add(decreaseNode);
								decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
								decreaseNode.inserted = true;
								tree.insertNode(decreaseNode);
							}
						}
					}
				}
				
				// Dijkstra completed, evaluate
				for(int x = 0; x < neighboursOutgoing.size(); x++) {
					min = neighboursOutgoing.get(x);
					//System.out.println(min.id+" "+min.pathLength+" "+(min.edgeDistanceTo+ localSource.edgeDistanceFrom));
					if(min.pathLength > min.edgeDistanceTo + localSource.edgeDistanceFrom) {
						// Found shortcut
						// Add two, one for the shortcut, one for the reverse
						toBeAdded = toBeAdded+2;
					}
				}
				
				// Reset
				for(int x = 0; x < toBeReset.size(); x++) {
					min = toBeReset.get(x);
					min.key = Long.MAX_VALUE;
					min.deleted = false;
					min.inserted = false;
					min.path = null;
					min.pathLength = Long.MAX_VALUE;
				}
			}
		}
		
		
		return toBeAdded - toBeRemoved + neighboursDeleted;
	}
	
	/**
	 * Preprocesses nodes into CHNodes with a Contraction Hierarchy established.
	 * Fills out the hashMap.
	 * @param input File consisting of Node objects forming a graph.
	 * @return ArrayList of CHNode with shortcuts and hierarchy added.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ArrayList<CHNode> CHPreprocess(String input) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		//ArrayList<Node> normalNodes = createTest();
		//source = 8;
		//target = 1;
		
		ArrayList<CHNode> nodes = new ArrayList<CHNode>();
		CHNode sourceNode = null;
		CHNode targetNode = null;
		CHNode node = null;
		hashMap = new HashMap<Long,CHNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new CHNode(normalNodes.get(i));
			node.hierarcyLevel = node.id; // Naive approach
			nodes.add(node);
			hashMap.put(node.id, node);
		}
		
		// Fill in reverse edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			CHNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		// Use a priority queue to contract nodes
		BiRedBlackTree pq = new BiRedBlackTree();
		
		System.out.println("Giving initial priority");
		// Give all nodes initial priority
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			int priority = calculatePriority(node, nodes);
			node.key2 = calcKey(priority+pqdif, node.id);
			node.pathLength2 = priority;
			node.realPQval = priority;
			pq.insertNode(node);
		}
		
		System.out.println("Contracting nodes "+pq.size);
		// Contract edges
		long nodeRank = 0;
		for(int i = 0; i < nodes.size(); i++) {
			
			node = (CHNode) pq.deleteMin();
			
			System.out.println(i+" "+node.pathLength2 + " "+node.realPQval);
			// Lazy update
			while(node.pathLength2 - lazyUpdateLimit > node.realPQval
					|| node.pathLength2 + lazyUpdateLimit < node.realPQval) {
				node.pathLength2 = node.realPQval;
				node.key2 = calcKey(node.pathLength2+pqdif, node.id);
				pq.insertNode(node);
				node = (CHNode) pq.deleteMin();
			}
			
			nodeRank++;
			node.hierarcyLevel = nodeRank;
			
			//System.out.println("=== Contracting node "+node.id+" "+i);
			
			// Add neighbours
			ArrayList<CHNode> neighboursIngoing = new ArrayList<CHNode>(); // List of higher ranked neighbours
			ArrayList<CHNode> neighboursOutgoing = new ArrayList<CHNode>();
			ArrayList<CHNode> neighbours = new ArrayList<CHNode>();
			CHNode neighbour = null;
			// Find all ingoing and outgoing edges in the current graph
			Edge nEdge = null;
			for(int j = 0; j < node.edges.size(); j++) {
				nEdge = node.edges.get(j);
				neighbour = hashMap.get(nEdge.nodeID);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistanceTo = nEdge.travelTime;
					neighboursOutgoing.add(neighbour);
					neighbours.add(neighbour);
				}
			}
			Shortcut nShort = null;
			for(int j = 0; j < node.shortcutsForward.size(); j++) {
				nShort = node.shortcutsForward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistanceTo = nShort.travelTime;
					neighboursOutgoing.add(neighbour);
					if(!neighbours.contains(neighbour)) {
						neighbours.add(neighbour);
					}
				}
			}
			
			for(int j = 0; j < node.edges2.size(); j++) {
				nEdge = node.edges2.get(j);
				neighbour = hashMap.get(nEdge.nodeID);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistanceFrom = nEdge.travelTime;
					neighboursIngoing.add(neighbour);
					if(!neighbours.contains(neighbour)) {
						neighbours.add(neighbour);
					}
				}
			}
			
			for(int j = 0; j < node.shortcutsBackward.size(); j++) {
				nShort = node.shortcutsBackward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				////System.out.println("In "+neighbour.id+" "+neighbour.deletedFromGraph);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistanceFrom = nShort.travelTime;
					neighboursIngoing.add(neighbour);
					if(!neighbours.contains(neighbour)) {
						neighbours.add(neighbour);
					}
				}
			}
			
			//System.out.println(node.id+" "+neighboursIngoing.size()+" "+neighboursOutgoing.size());
			
			// Now for all ingoing edges (w,v) find the shortest path excluding v
			// from w to all w' in the outgoing edges (v,w').
			// We do this using a normal Dijkstra search.
			CHNode localSource = null;
			ArrayList<Shortcut> toBeAdded = new ArrayList<Shortcut>();
			if(neighboursIngoing.size() > 0 && neighboursOutgoing.size() > 0) {
				for(int j = 0; j < neighboursIngoing.size(); j++) {
					ArrayList<CHNode> toBeReset = new ArrayList<CHNode>();
					localSource = neighboursIngoing.get(j);
					//System.out.println(node.id+" Checking ingoing from "+localSource.id);
					localSource.key = 0;
					localSource.pathLength = 0;
					RedBlackTree tree = new RedBlackTree();
					tree.insertNode(localSource);
					toBeReset.add(localSource);
					long counter = 0;
					int targetCounter = neighboursOutgoing.size();
					CHNode min = null;
					while(counter < hoplimit && targetCounter > 0 && tree.size > 0) {
						counter++;
						min = (CHNode) tree.deleteMin();
						//System.out.println("Extracted "+min.id);
						if(neighboursOutgoing.contains(min)) {
							targetCounter--;
						}
						Edge edge = null;
						CHNode decreaseNode = null;
						for(int x = 0; x < min.edges.size(); x++) {
							edge = min.edges.get(x);
							decreaseNode = hashMap.get(edge.nodeID);
							long newPathLength = min.pathLength + edge.travelTime;
							// Only nodes with higher rank
							if(decreaseNode.id != node.id && !decreaseNode.deletedFromGraph && 
									!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
								decreaseNode.path = min;
								decreaseNode.pathLength = newPathLength;
								if(decreaseNode.inserted) {
									//System.out.println("Decreasing "+decreaseNode.id);
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									//System.out.println("Inserting "+decreaseNode.id);
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
						// Same for shortcuts
						Shortcut shortcut = null;
						for(int x = 0; x < min.shortcutsForward.size(); x++) {
							shortcut = min.shortcutsForward.get(x);
							decreaseNode = hashMap.get(shortcut.nodeID);
							long newPathLength = min.pathLength + shortcut.travelTime;
							// Only nodes with higher rank
							if(decreaseNode.id != node.id && !decreaseNode.deletedFromGraph && 
									!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
								decreaseNode.path = min;
								decreaseNode.pathLength = newPathLength;
								if(decreaseNode.inserted) {
									//System.out.println("Decreasing "+decreaseNode.id);
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									//System.out.println("Inserting "+decreaseNode.id);
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
					}
					
					// Dijkstra completed, evaluate
					for(int x = 0; x < neighboursOutgoing.size(); x++) {
						min = neighboursOutgoing.get(x);
						//System.out.println(min.id+" "+min.pathLength+" "+(min.edgeDistanceTo+ localSource.edgeDistanceFrom));
						if(min.pathLength > min.edgeDistanceTo + localSource.edgeDistanceFrom) {
							// Found shortcut
							// long id, long fromID, int travelTime, long level, boolean upward
							Shortcut shortcut = new Shortcut(min.id,localSource.id,(int) (min.edgeDistanceTo + localSource.edgeDistanceFrom),min.hierarcyLevel,true,node.id);
							toBeAdded.add(shortcut);
						}
					}
					
					// Reset
					for(int x = 0; x < toBeReset.size(); x++) {
						min = toBeReset.get(x);
						min.key = Long.MAX_VALUE;
						min.deleted = false;
						min.inserted = false;
						min.path = null;
						min.pathLength = Long.MAX_VALUE;
					}
				}
			}
			
			// Add the shortcuts
			Shortcut shortcut = null;
			Shortcut reverseShortcut = null;
			CHNode fromNode = null;
			CHNode toNode = null;
			for(int x = 0; x < toBeAdded.size(); x++) {
				shortcut = toBeAdded.get(x);
				if(shortcut.nodeID != shortcut.fromID) {
					fromNode = hashMap.get(shortcut.fromID);
					toNode = hashMap.get(shortcut.nodeID);
					reverseShortcut = new Shortcut(shortcut,fromNode.hierarcyLevel);

					//System.out.println("Adding shortcut forward "+shortcut.fromID+" "+shortcut.nodeID +" "+shortcut.travelTime);
					fromNode.shortcutsForward.add(shortcut);
					//System.out.println("Adding shortcut backward "+reverseShortcut.fromID+" "+reverseShortcut.nodeID+" "+reverseShortcut.travelTime);
					toNode.shortcutsBackward.add(reverseShortcut);
				}	
			}	
			node.deletedFromGraph = true;
			
			// Recalculate priority of all neighbours of the node
			for(int y = 0; y < neighbours.size(); y++) {
				neighbour = neighbours.get(y);
				int recalc = calculatePriority(neighbour, nodes);
				neighbour.realPQval = recalc;
				/*if(recalc != neighbour.pathLength2 ) {
					neighbour.pathLength2 = recalc;
					pq.decreasekey(neighbour, recalc+pqdif);
				}*/	
			}
		}
		
		// Reset all nodes.
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			node.key = Long.MAX_VALUE;
			node.key2 = Long.MAX_VALUE;
			node.path = null;
			node.path2 = null;
			node.pathLength = Long.MAX_VALUE;
			node.pathLength2 = Long.MAX_VALUE;
			node.deleted = false;
			node.deleted2 = false;
			node.deletedFromGraph = false;
			node.inserted = false;
			node.inserted2 = false;
			node.parent = null;
			node.parent2 = null;
			node.leftChild = null;
			node.leftChild2 = null;
			node.rightChild = null;
			node.rightChild2 = null;
			node.colour = false;
			node.colour2 = false;
		}
		
		return nodes;
	}
	
	public long CHContractionByPQ(ArrayList<CHNode> nodes, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		//source = 8;
		//target = 1;
		
		CHNode node = null;
		CHNode sourceNode = null;
		CHNode targetNode = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// All nodes now contracted	
		//System.out.println("=============== Dijkstra");
		
		CHNode meet = null;
		long shortest = Long.MAX_VALUE;
		CHNode smallest = null;
		long newMin = 0;
		// Perform the search.
		// The forward search will use upward shortcuts
		// and the reverse search will use downward shortcuts.
		
		RedBlackTree tree = new RedBlackTree();
		BiRedBlackTree biTree = new BiRedBlackTree();
		
		sourceNode.key = 0;
		sourceNode.path = null;
		sourceNode.pathLength = 0;
		sourceNode.inserted = true;
		targetNode.key2 = 0;
		targetNode.path2 = null;
		targetNode.pathLength2 = 0;
		targetNode.inserted2 = true;
		
		tree.insertNode(sourceNode);
		biTree.insertNode(targetNode);
		
		CHNode node1 = null;
		CHNode node2 = null;
		
		while(tree.size > 0 || biTree.size > 0) {
			if(tree.size > 0) {
				node1 = (CHNode) tree.deleteMin();
				//System.out.println("Extracted from 1 "+node1.id);
				//System.out.println(node1.edges.size()+" "+node1.edges2.size()+" "+node1.shortcutsForward.size()+" "+node1.shortcutsBackward.size());
				nodesChecked++;
			}
			node1.deleted = true;
			if(biTree.size > 0) {
				node2 = (CHNode) biTree.deleteMin();
				//System.out.println("Extracted from 2 "+node2.id);
				//System.out.println(node2.edges.size()+" "+node2.edges2.size()+" "+node2.shortcutsForward.size()+" "+node2.shortcutsBackward.size());
				nodesChecked++;
			}
			node2.deleted2 = true;
			/*if(node1.deleted2) {
				meet = node1;
				shortest = node1.pathLength + node1.pathLength2;
				//System.out.println("Met1");
				break;
			}
			if(node2.deleted) {
				meet = node2;
				shortest = node2.pathLength + node2.pathLength2;
				//System.out.println("Met2");
				break;
			}*/
			if(node1.pathLength >= shortest && node2.pathLength2 >= shortest) {
				//System.out.println("Break");
				break;
			}

			
			CHNode decreaseNode1 = null;
			Edge edge1 = null;
			for(int i = 0; i < node1.edges.size(); i++) {
				edge1 = node1.edges.get(i);
				decreaseNode1 = hashMap.get(edge1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + edge1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting edge1 from "+node1.id+" to "+decreaseNode1.id+" "+edge1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}				
			Shortcut shortcut1 = null;
			for(int i = 0; i < node1.shortcutsForward.size(); i++) {
				shortcut1 = node1.shortcutsForward.get(i);
				decreaseNode1 = hashMap.get(shortcut1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + shortcut1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting forward shortcut from "+ node1.id+" to "+decreaseNode1.id+" "+shortcut1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}
			
			CHNode decreaseNode2 = null;
			Edge edge2 = null;
			for(int i = 0; i < node2.edges2.size(); i++) {
				edge2 = node2.edges2.get(i);
				decreaseNode2 = hashMap.get(edge2.nodeID);
				if(decreaseNode2.hierarcyLevel > node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + edge2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting edge2 from "+decreaseNode2.id+" to "+node2.id+" "+edge2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}

			Shortcut shortcut2 = null;
			for(int i = 0; i < node2.shortcutsBackward.size(); i++) {
				shortcut2 = node2.shortcutsBackward.get(i);
				decreaseNode2 = hashMap.get(shortcut2.nodeID);
				if(decreaseNode2.hierarcyLevel > node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + shortcut2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting backward shortcut from "+decreaseNode2.id+" to "+node2.id+" "+shortcut2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
			
		}
		
		// We found a shortest node, but in order to retrieve the shortest path we need to "unpack" it
		// by recursively turning shortcuts into sub-shortcuts/edges until only edges remains.

		System.out.println(shortest);
		
		node = smallest;
		CHNode nextNode = null;
		
		// Reverse
		while(node.id != sourceNode.id) {
			nextNode = (CHNode) node.path;
			node.prev = nextNode;
			nextNode.next = node;
			node = nextNode;
		}
		// Forward
		node = smallest;
		while(node.id != targetNode.id) {
			nextNode = (CHNode) node.path2;
			node.next = nextNode;
			nextNode.prev = node;
			node = nextNode;
		}
		
		// Unpack the route to source
		// Unpack uses too many recursive calls, and will run out of stack space
		//unpack(sourceNode, targetNode);
		
		// Iterativ approach instead. Unpack left to right.
		//System.out.println("Unpacking shortcuts");
		node = sourceNode;
		while(node.id != targetNode.id) {
			
			nextNode = node.next;
			boolean edgeExists = false; // Denotes that an edge exists AND that there isnt a shorter shortcut.
			Edge edge = null;
			for(int i = 0; i < node.edges.size(); i++) {
				edge = node.edges.get(i);
				if(edge.nodeID == nextNode.id) {
					edgeExists = true;
					break;
				}
			}
			Shortcut shortcut = null;
			Shortcut s = null;
			int min = edge.distance;
			// Find shortest shortcut
			for(int i = 0; i < node.shortcutsForward.size(); i++) {
				shortcut = node.shortcutsForward.get(i);
				if(shortcut.nodeID == nextNode.id) {
					if(shortcut.distance < min) {
						s = shortcut;
						edgeExists = false;
					}
				}
			}
			
			if(edgeExists) {
				node = nextNode;
			}
			else if(s != null){
				CHNode insert = hashMap.get(s.node);
				node.next = insert;
				insert.prev = node;
				insert.next = nextNode;
				nextNode.prev = insert;
			}
			else {
				System.out.println("No edge or shortcut node "+node.id+" "+nextNode.id);
			}
		}
		
		
		check = new ArrayList<CHNode>();
		node = sourceNode;
		while(node.id != targetNode.id) {
			check.add(node);
			node = node.next;
		}
		check.add(targetNode);
		
		
		//System.out.println("Sizes "+tree.size + " " + biTree.size);
		////System.out.println("Meet="+meet.id+" "+meet.pathLength+" "+meet.pathLength2);
		//System.out.println(shortest);
		//System.out.println("Shortest = "+smallest.id+" "+smallest.pathLength+" "+smallest.pathLength2);
		return shortest;
	}
	
	// Unpacks the shortcuts in a forward direction
	public void unpack(CHNode node, CHNode targetNode) {
		CHNode nextNode = node.next;
		long nextID = nextNode.id;
		boolean edgeExists = false;
		Edge edge = null;
		for(int i = 0; i < node.edges.size(); i++) {
			edge = node.edges.get(i);
			if(edge.nodeID == nextID) {
				edgeExists = true;
			}
		}
		// If we have reduced the path to an edge
		if(edgeExists && nextNode.id != targetNode.id) {
			unpack(nextNode, targetNode);
		}
		else {
			// Unpack shortcut
			Shortcut shortcut = null;
			for(int i = 0; i < node.shortcutsForward.size(); i++) {
				shortcut = node.shortcutsForward.get(i);
				if(shortcut.nodeID == nextNode.id) {
					break;
				}
			}
			CHNode insert = hashMap.get(shortcut.nodeID);
			node.next = insert;
			insert.prev = node;
			insert.next = nextNode;
			nextNode.prev = insert;
			unpack(insert,targetNode);
			if(nextNode.id != targetNode.id) {
				unpack(nextNode,targetNode);
			}			
		}
	}

	
	// To access bottom of above function fast
	public void Bottom() {
		
	}
	
	/*public long CHNaivebyNodeIDSecondBackup(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		/*ArrayList<Node> normalNodes = createTest();
		source = 1;
		target = 8;*/
		
		/*ArrayList<CHNode> nodes = new ArrayList<CHNode>();
		CHNode sourceNode = null;
		CHNode targetNode = null;
		CHNode node = null;
		HashMap<Long,CHNode> hashMap = new HashMap<Long,CHNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new CHNode(normalNodes.get(i));
			node.hierarcyLevel = node.id; // Naive approach
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Fill in reverse edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			CHNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		// Contract edges
		for(int i = 0; i < nodes.size(); i++) {
			
			
			node = nodes.get(i);
			
			//System.out.println("=== Contracting node "+node.id+" "+i);
			
			// Add neighbours
			ArrayList<CHNode> neighboursIngoing = new ArrayList<CHNode>(); // List of higher ranked neighbours
			ArrayList<CHNode> neighboursOutgoing = new ArrayList<CHNode>();
			CHNode neighbour = null;
			// Find all ingoing and outgoing edges in the current graph
			Edge nEdge = null;
			for(int j = 0; j < node.edges.size(); j++) {
				nEdge = node.edges.get(j);
				neighbour = hashMap.get(nEdge.nodeID);
				//if(neighbour.hierarcyLevel >= node.hierarcyLevel) {
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nEdge.travelTime;
					neighboursOutgoing.add(neighbour);
				}
			}
			Shortcut nShort = null;
			for(int j = 0; j < node.shortcutsForward.size(); j++) {
				nShort = node.shortcutsForward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				//if(neighbour.hierarcyLevel >= node.hierarcyLevel) {
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nShort.travelTime;
					neighboursOutgoing.add(neighbour);
				}
			}
			
			for(int j = 0; j < node.edges2.size(); j++) {
				nEdge = node.edges2.get(j);
				neighbour = hashMap.get(nEdge.nodeID);
				//if(neighbour.hierarcyLevel >= node.hierarcyLevel) {
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nEdge.travelTime;
					neighboursIngoing.add(neighbour);
				}
			}
			
			for(int j = 0; j < node.shortcutsBackward.size(); j++) {
				nShort = node.shortcutsBackward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				////System.out.println("In "+neighbour.id+" "+neighbour.deletedFromGraph);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nShort.travelTime;
					neighboursIngoing.add(neighbour);
				}
			}
			
			//System.out.println(node.id+" "+neighboursIngoing.size()+" "+neighboursOutgoing.size());
			
			// Now for all ingoing edges (w,v) find the shortest path excluding v
			// from w to all w' in the outgoing edges (v,w').
			// We do this using a normal Dijkstra search.
			CHNode localSource = null;
			ArrayList<Shortcut> toBeAdded = new ArrayList<Shortcut>();
			if(neighboursIngoing.size() > 0 && neighboursOutgoing.size() > 0) {
				for(int j = 0; j < neighboursIngoing.size(); j++) {
					ArrayList<CHNode> toBeReset = new ArrayList<CHNode>();
					localSource = neighboursIngoing.get(j);
					//System.out.println(node.id+" Checking ingoing from "+localSource.id);
					localSource.key = 0;
					localSource.pathLength = 0;
					RedBlackTree tree = new RedBlackTree();
					tree.insertNode(localSource);
					toBeReset.add(localSource);
					long counter = 0;
					int targetCounter = neighboursOutgoing.size();
					CHNode min = null;
					while(counter < hoplimit && targetCounter > 0 && tree.size > 0) {
						counter++;
						min = (CHNode) tree.deleteMin();
						//System.out.println("Extracted "+min.id);
						if(neighboursOutgoing.contains(min)) {
							targetCounter--;
						}
						Edge edge = null;
						CHNode decreaseNode = null;
						for(int x = 0; x < min.edges.size(); x++) {
							edge = min.edges.get(x);
							decreaseNode = hashMap.get(edge.nodeID);
							long newPathLength = min.pathLength + edge.travelTime;
							// Only nodes with higher rank
							if(decreaseNode.id != node.id && !decreaseNode.deletedFromGraph && 
									!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
								decreaseNode.path = min;
								decreaseNode.pathLength = newPathLength;
								if(decreaseNode.inserted) {
									//System.out.println("Decreasing "+decreaseNode.id);
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									//System.out.println("Inserting "+decreaseNode.id);
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
						// Same for shortcuts
						Shortcut shortcut = null;
						for(int x = 0; x < min.shortcutsForward.size(); x++) {
							shortcut = min.shortcutsForward.get(x);
							decreaseNode = hashMap.get(shortcut.nodeID);
							long newPathLength = min.pathLength + shortcut.travelTime;
							// Only nodes with higher rank
							if(decreaseNode.id != node.id && !decreaseNode.deletedFromGraph && 
									!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
								decreaseNode.path = min;
								decreaseNode.pathLength = newPathLength;
								if(decreaseNode.inserted) {
									//System.out.println("Decreasing "+decreaseNode.id);
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									//System.out.println("Inserting "+decreaseNode.id);
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
					}
					
					// Dijkstra completed, evaluate
					for(int x = 0; x < neighboursOutgoing.size(); x++) {
						min = neighboursOutgoing.get(x);
						//System.out.println(min.id+" "+min.pathLength+" "+(min.edgeDistance+ localSource.edgeDistance));
						if(min.pathLength > min.edgeDistance + localSource.edgeDistance) {
							// Found shortcut
							// long id, long fromID, int travelTime, long level, boolean upward
							Shortcut shortcut = new Shortcut(min.id,localSource.id,(int) (min.edgeDistance + localSource.edgeDistance),min.hierarcyLevel,true);
							toBeAdded.add(shortcut);
						}
					}
					
					// Reset
					for(int x = 0; x < toBeReset.size(); x++) {
						min = toBeReset.get(x);
						min.key = Long.MAX_VALUE;
						min.deleted = false;
						min.inserted = false;
						min.path = null;
						min.pathLength = Long.MAX_VALUE;
						min.deletedFromGraph = false;
					}
				}
			}
			
			// Add the shortcuts
			Shortcut shortcut = null;
			Shortcut reverseShortcut = null;
			CHNode fromNode = null;
			CHNode toNode = null;
			for(int x = 0; x < toBeAdded.size(); x++) {
				shortcut = toBeAdded.get(x);
				if(shortcut.nodeID != shortcut.fromID) {
					reverseShortcut = new Shortcut(shortcut);
					fromNode = hashMap.get(shortcut.fromID);
					toNode = hashMap.get(shortcut.nodeID);

					//System.out.println("Adding shortcut forward "+shortcut.fromID+" "+shortcut.nodeID +" "+shortcut.travelTime);
					fromNode.shortcutsForward.add(shortcut);
					//System.out.println("Adding shortcut backward "+reverseShortcut.fromID+" "+reverseShortcut.nodeID+" "+reverseShortcut.travelTime);
					toNode.shortcutsBackward.add(reverseShortcut);

	
					

				}	
			}
			
			node.deletedFromGraph = true;
	
			
		}
		
		// All nodes now contracted
		
		// Add reverse shortcuts
		/*for(int i = 0; i < nodes.size(); i++)) {
			node = nodes.get(i);
			Shortcut shortcut = null;
			Shortcut newShortcut = null;
			for(int j = 0; j < node.shortcutsForward)
		}*/
		
		////System.out.println("=============== Dijkstra");
		
		/*CHNode meet = null;
		long shortest = Long.MAX_VALUE;
		CHNode smallest = null;
		long newMin = 0;
		// Perform the search.
		// The forward search will use upward shortcuts
		// and the reverse search will use downwar shortcuts.
		
		RedBlackTree tree = new RedBlackTree();
		BiRedBlackTree biTree = new BiRedBlackTree();
		
		sourceNode.key = 0;
		sourceNode.path = null;
		sourceNode.pathLength = 0;
		sourceNode.inserted = true;
		targetNode.key2 = 0;
		targetNode.path2 = null;
		targetNode.pathLength2 = 0;
		targetNode.inserted2 = true;
		
		tree.insertNode(sourceNode);
		biTree.insertNode(targetNode);
		
		CHNode node1 = null;
		CHNode node2 = null;
		
		while(tree.size > 0 || biTree.size > 0) {
			if(tree.size > 0) {
				node1 = (CHNode) tree.deleteMin();
				//System.out.println("Extracted from 1 "+node1.id);
			}
			node1.deleted = true;
			if(biTree.size > 0) {
				node2 = (CHNode) biTree.deleteMin();
				//System.out.println("Extracted from 2 "+node2.id);
			}
			node2.deleted2 = true;
			if(node1.deleted2) {
				meet = node1;
				shortest = node1.pathLength + node1.pathLength2;
				//System.out.println("Met1");
				break;
			}
			if(node2.deleted) {
				meet = node2;
				shortest = node2.pathLength + node2.pathLength2;
				//System.out.println("Met2");
				break;
			}
			
			CHNode decreaseNode1 = null;
			Edge edge1 = null;
			for(int i = 0; i < node1.edges.size(); i++) {
				edge1 = node1.edges.get(i);
				decreaseNode1 = hashMap.get(edge1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + edge1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting edge1 from "+node1.id+" to "+decreaseNode1.id+" "+edge1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}
			/*for(int i = 0; i < node1.edges2.size(); i++) {
				edge1 = node1.edges2.get(i);
				decreaseNode1 = hashMap.get(edge1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + edge1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting edge2 from "+decreaseNode1.id+" to "+node1.id+" "+edge1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}*/
			
			
			/*Shortcut shortcut1 = null;
			for(int i = 0; i < node1.shortcutsForward.size(); i++) {
				shortcut1 = node1.shortcutsForward.get(i);
				decreaseNode1 = hashMap.get(shortcut1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + shortcut1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting forward shortcut from "+ node1.id+" to "+decreaseNode1.id+" "+shortcut1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}
			
			/*for(int i = 0; i < node1.shortcutsBackward.size(); i++) {
				shortcut1 = node1.shortcutsBackward.get(i);
				decreaseNode1 = hashMap.get(shortcut1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + shortcut1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting backward shortcut from "+decreaseNode1.id+" to "+node1.id+" "+shortcut1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}*/
			
			/*CHNode decreaseNode2 = null;
			Edge edge2 = null;
			for(int i = 0; i < node2.edges2.size(); i++) {
				edge2 = node2.edges2.get(i);
				decreaseNode2 = hashMap.get(edge2.nodeID);
				if(decreaseNode2.hierarcyLevel < node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + edge2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting edge2 from "+decreaseNode2.id+" to "+node2.id+" "+edge2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
			/*for(int i = 0; i < node2.edges.size(); i++) {
				edge2 = node2.edges.get(i);
				decreaseNode2 = hashMap.get(edge2.nodeID);
				if(decreaseNode2.hierarcyLevel < node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + edge2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting edge1 from "+node2.id+" to "+node2.id+decreaseNode2.id+" "+edge2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}*/
			
			
			
			/*Shortcut shortcut2 = null;
			for(int i = 0; i < node2.shortcutsBackward.size(); i++) {
				shortcut2 = node2.shortcutsBackward.get(i);
				decreaseNode2 = hashMap.get(shortcut2.nodeID);
				if(decreaseNode2.hierarcyLevel < node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + shortcut2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting backward shortcut from "+decreaseNode2.id+" to "+node2.id+" "+shortcut2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
			/*for(int i = 0; i < node2.shortcutsForward.size(); i++) {
				shortcut2 = node2.shortcutsForward.get(i);
				decreaseNode2 = hashMap.get(shortcut2.nodeID);
				if(decreaseNode2.hierarcyLevel < node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + shortcut2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting forward shortcut from "+node2.id+" to "+node+decreaseNode2.id+" "+shortcut2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}*/
		/*}
		//System.out.println("Sizes "+tree.size + " " + biTree.size);
		//System.out.println("Meet="+meet.id+" "+meet.pathLength+" "+meet.pathLength2);
		//System.out.println("Shortet = "+smallest.id+" "+smallest.pathLength+" "+smallest.pathLength2);
		return shortest;
	}
	
	public long CHNaivebyNodeIDBackup(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		//ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		ArrayList<Node> normalNodes = createTest();
		source = 1;
		target = 8;
		
		ArrayList<CHNode> nodes = new ArrayList<CHNode>();
		CHNode sourceNode = null;
		CHNode targetNode = null;
		CHNode node = null;
		HashMap<Long,CHNode> hashMap = new HashMap<Long,CHNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new CHNode(normalNodes.get(i));
			node.hierarcyLevel = node.id; // Naive approach
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Fill in reverse edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			CHNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		// Contract edges
		for(int i = 0; i < nodes.size(); i++) {
			
			
			node = nodes.get(i);
			
			//System.out.println("=== Contracting node "+node.id+" "+i);
			
			// Add neighbours
			ArrayList<CHNode> neighboursIngoing = new ArrayList<CHNode>(); // List of higher ranked neighbours
			ArrayList<CHNode> neighboursOutgoing = new ArrayList<CHNode>();
			CHNode neighbour = null;
			// Find all ingoing and outgoing edges in the current graph
			Edge nEdge = null;
			for(int j = 0; j < node.edges.size(); j++) {
				nEdge = node.edges.get(j);
				neighbour = hashMap.get(nEdge.nodeID);
				//if(neighbour.hierarcyLevel >= node.hierarcyLevel) {
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nEdge.travelTime;
					neighboursOutgoing.add(neighbour);
				}
			}
			Shortcut nShort = null;
			for(int j = 0; j < node.shortcutsForward.size(); j++) {
				nShort = node.shortcutsForward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				//if(neighbour.hierarcyLevel >= node.hierarcyLevel) {
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nShort.travelTime;
					neighboursOutgoing.add(neighbour);
				}
			}
			
			for(int j = 0; j < node.edges2.size(); j++) {
				nEdge = node.edges2.get(j);
				neighbour = hashMap.get(nEdge.nodeID);
				//if(neighbour.hierarcyLevel >= node.hierarcyLevel) {
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nEdge.travelTime;
					neighboursIngoing.add(neighbour);
				}
			}
			for(int j = 0; j < node.shortcutsBackward.size(); j++) {
				nShort = node.shortcutsBackward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				////System.out.println("In "+neighbour.id+" "+neighbour.deletedFromGraph);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nShort.travelTime;
					neighboursIngoing.add(neighbour);
				}
			}
			
			//System.out.println(node.id+" "+neighboursIngoing.size()+" "+neighboursOutgoing.size());
			
			// Now for all ingoing edges (w,v) find the shortest path excluding v
			// from w to all w' in the outgoing edges (v,w').
			// We do this using a normal Dijkstra search.
			CHNode localSource = null;
			ArrayList<Shortcut> toBeAdded = new ArrayList<Shortcut>();
			if(neighboursIngoing.size() > 0 && neighboursOutgoing.size() > 0) {
				for(int j = 0; j < neighboursIngoing.size(); j++) {
					ArrayList<CHNode> toBeReset = new ArrayList<CHNode>();
					localSource = neighboursIngoing.get(j);
					//System.out.println(node.id+" Checking ingoing from "+localSource.id);
					localSource.key = 0;
					localSource.pathLength = 0;
					RedBlackTree tree = new RedBlackTree();
					tree.insertNode(localSource);
					toBeReset.add(localSource);
					long counter = 0;
					int targetCounter = neighboursOutgoing.size();
					CHNode min = null;
					while(counter < hoplimit && targetCounter > 0 && tree.size > 0) {
						counter++;
						min = (CHNode) tree.deleteMin();
						//System.out.println("Extracted "+min.id);
						if(neighboursOutgoing.contains(min)) {
							targetCounter--;
						}
						Edge edge = null;
						CHNode decreaseNode = null;
						for(int x = 0; x < min.edges.size(); x++) {
							edge = min.edges.get(x);
							decreaseNode = hashMap.get(edge.nodeID);
							long newPathLength = min.pathLength + edge.travelTime;
							// Only nodes with higher rank
							if(decreaseNode.id != node.id && decreaseNode.hierarcyLevel >= node.hierarcyLevel && 
									!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
								decreaseNode.path = min;
								decreaseNode.pathLength = newPathLength;
								if(decreaseNode.inserted) {
									//System.out.println("Decreasing "+decreaseNode.id);
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									//System.out.println("Inserting "+decreaseNode.id);
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
						// Same for shortcuts
						Shortcut shortcut = null;
						for(int x = 0; x < min.shortcutsForward.size(); x++) {
							shortcut = min.shortcutsForward.get(x);
							decreaseNode = hashMap.get(shortcut.nodeID);
							long newPathLength = min.pathLength + shortcut.travelTime;
							// Only nodes with higher rank
							if(decreaseNode.id != node.id && decreaseNode.hierarcyLevel >= node.hierarcyLevel && 
									!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
								decreaseNode.path = min;
								decreaseNode.pathLength = newPathLength;
								if(decreaseNode.inserted) {
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
					}
					
					// Dijkstra completed, evaluate
					for(int x = 0; x < neighboursOutgoing.size(); x++) {
						min = neighboursOutgoing.get(x);
						//System.out.println(min.id+" "+min.pathLength+" "+(min.edgeDistance+ localSource.edgeDistance));
						if(min.pathLength > min.edgeDistance + localSource.edgeDistance) {
							// Found shortcut
							// long id, long fromID, int travelTime, long level, boolean upward
							Shortcut shortcut = new Shortcut(min.id,localSource.id,(int) (min.edgeDistance + localSource.edgeDistance),min.hierarcyLevel,true);
							toBeAdded.add(shortcut);
						}
					}
					
					// Reset
					for(int x = 0; x < toBeReset.size(); x++) {
						min = toBeReset.get(x);
						min.key = Long.MAX_VALUE;
						min.deleted = false;
						min.inserted = false;
						min.path = null;
						min.pathLength = Long.MAX_VALUE;
						min.deletedFromGraph = false;
					}
				}
			}
			
			// Add the shortcuts
			Shortcut shortcut = null;
			Shortcut reverseShortcut = null;
			CHNode reverse = null;
			for(int x = 0; x < toBeAdded.size(); x++) {
				shortcut = toBeAdded.get(x);
				reverseShortcut = new Shortcut(shortcut);
				neighbour = hashMap.get(shortcut.fromID);
				reverse = hashMap.get(shortcut.nodeID);
				if(shortcut.nodeID != shortcut.fromID) {
					
					//System.out.println("Adding shortcut forward "+shortcut.fromID+" "+shortcut.nodeID +" "+shortcut.travelTime);
					/*neighbour.shortcutsForward.add(shortcut);
					//System.out.println("Adding shortcut backward "+reverseShortcut.fromID+" "+reverseShortcut.nodeID+" "+reverseShortcut.travelTime);
					reverse.shortcutsBackward.add(reverseShortcut);*/
					
					
					/*if(reverse.id >= neighbour.id) {
						//System.out.println("Adding shortcut forward "+shortcut.fromID+" "+shortcut.nodeID +" "+shortcut.travelTime);
						neighbour.shortcutsForward.add(shortcut);
						//System.out.println("Adding shortcut backward "+reverseShortcut.fromID+" "+reverseShortcut.nodeID+" "+reverseShortcut.travelTime);
						reverse.shortcutsBackward.add(reverseShortcut);
					}
					else {
						//System.out.println("Adding shortcut forward "+reverseShortcut.fromID+" "+reverseShortcut.nodeID +" "+reverseShortcut.travelTime);
						neighbour.shortcutsBackward.add(reverseShortcut);
						//System.out.println("Adding shortcut backward "+shortcut.fromID+" "+shortcut.nodeID+" "+shortcut.travelTime);
						reverse.shortcutsForward.add(shortcut);
					}
					
					
					/*if(neighbour.hierarcyLevel >= shortcut.level) {
					//System.out.println("Adding shortcut forward "+shortcut.fromID+" "+shortcut.nodeID +" "+shortcut.travelTime);
					neighbour.shortcutsForward.add(shortcut);
					}
					else {
						// Add it as a downward reverse shortcut
						shortcut = new Shortcut(shortcut);
						neighbour = hashMap.get(shortcut.fromID);
						//System.out.println("Adding shortcut backward "+shortcut.fromID+" "+shortcut.nodeID+" "+shortcut.travelTime);
						neighbour.shortcutsBackward.add(shortcut);
					}*/
				/*}	
			}
			
			// If shortcuts for all ingoing nodes were found we can safely delete the node
			//System.out.println("Check Delete "+node.id+" "+toBeAdded.size()+" "+neighboursOutgoing.size());
			/*if(toBeAdded.size() > 0 && neighboursOutgoing.size() != 0) {
				//System.out.println("Deleting "+node.id);
				node.deletedFromGraph = true;
			}
	
			
		}
		
		// All nodes now contracted
		
		// Add reverse shortcuts
		/*for(int i = 0; i < nodes.size(); i++)) {
			node = nodes.get(i);
			Shortcut shortcut = null;
			Shortcut newShortcut = null;
			for(int j = 0; j < node.shortcutsForward)
		}*/
		
		////System.out.println("=============== Dijkstra");
		
		/*CHNode meet = null;
		// Perform the search.
		// The forward search will use upward shortcuts
		// and the reverse search will use downwar shortcuts.
		
		RedBlackTree tree = new RedBlackTree();
		BiRedBlackTree biTree = new BiRedBlackTree();
		
		sourceNode.key = 0;
		sourceNode.path = null;
		sourceNode.pathLength = 0;
		sourceNode.inserted = true;
		targetNode.key2 = 0;
		targetNode.path2 = null;
		targetNode.pathLength2 = 0;
		targetNode.inserted2 = true;
		
		tree.insertNode(sourceNode);
		biTree.insertNode(targetNode);
		
		CHNode node1 = null;
		CHNode node2 = null;
		
		long shortest = Long.MAX_VALUE;
		while(tree.size > 0 || biTree.size > 0) {
			if(tree.size > 0) {
				node1 = (CHNode) tree.deleteMin();
				//System.out.println("Extracted from 1 "+node1.id);
			}
			node1.deleted = true;
			if(biTree.size > 0) {
				node2 = (CHNode) biTree.deleteMin();
				//System.out.println("Extracted from 2 "+node2.id);
			}
			node2.deleted2 = true;
			if(node1.deleted2) {
				meet = node1;
				shortest = node1.pathLength + node1.pathLength2;
				break;
			}
			if(node2.deleted) {
				meet = node2;
				shortest = node2.pathLength + node2.pathLength2;
				break;
			}
			Edge edge1 = null;
			CHNode decreaseNode1 = null;
			for(int i = 0; i < node1.edges.size(); i++) {
				edge1 = node1.edges.get(i);
				decreaseNode1 = hashMap.get(edge1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + edge1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting edge to "+decreaseNode1.id+" "+node1.id+" "+edge1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}
			Shortcut shortcut1 = null;
			for(int i = 0; i < node1.shortcutsForward.size(); i++) {
				shortcut1 = node1.shortcutsForward.get(i);
				decreaseNode1 = hashMap.get(shortcut1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + shortcut1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						//System.out.println("1 Inserting shortcut to "+decreaseNode1.id+" "+node1.id+" "+shortcut1.travelTime);
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newPathLenght);
						}
						else {
							decreaseNode1.key = calcKey(newPathLenght,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
			}
			
			Edge edge2 = null;
			CHNode decreaseNode2 = null;
			for(int i = 0; i < node2.edges2.size(); i++) {
				edge2 = node2.edges2.get(i);
				decreaseNode2 = hashMap.get(edge2.nodeID);
				if(decreaseNode2.hierarcyLevel < node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + edge2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting edge to "+decreaseNode2.id+" "+node2.id+" "+edge2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
			Shortcut shortcut2 = null;
			for(int i = 0; i < node2.shortcutsBackward.size(); i++) {
				shortcut2 = node2.shortcutsBackward.get(i);
				decreaseNode2 = hashMap.get(shortcut2.nodeID);
				if(decreaseNode2.hierarcyLevel < node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + shortcut2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						//System.out.println("2 Inserting shortcut to "+decreaseNode2.id+" "+node2.id+" "+shortcut2.travelTime);
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newPathLenght);
						}
						else {
							decreaseNode2.key2 = calcKey(newPathLenght,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
		}
		//System.out.println("Sizes "+tree.size + " " + biTree.size);
		//System.out.println("Meet="+meet.id+" "+meet.pathLength+" "+meet.pathLength2);
		return meet.pathLength+meet.pathLength2;
	}*/
	
	// Shift val shift places to the left to make space for ID of 8bil.
	private long calcKey(long newPathLenght, long id) {
		long ret = newPathLenght;
		ret = ret << shift;
		ret = ret+id;
		return ret;
	}
	
	private ArrayList<Node> createTest() {
		ArrayList<Node> normalNodes = new ArrayList<Node>();
		Node node1 = new Node(1, 1, 1);
		normalNodes.add(node1);
		Node node2 = new Node(2, 1, 1);
		normalNodes.add(node2);
		Node node3 = new Node(3, 1, 1);
		normalNodes.add(node3);
		Node node4 = new Node(4, 1, 1);
		normalNodes.add(node4);
		Node node5 = new Node(5, 1, 1);
		normalNodes.add(node5);
		Node node6 = new Node(6, 1, 1);
		normalNodes.add(node6);
		Node node7 = new Node(7, 1, 1);
		normalNodes.add(node7);
		Node node8 = new Node(8, 1, 1);
		normalNodes.add(node8);
		Node node9 = new Node(9, 1, 1);
		normalNodes.add(node9);
		
		node1.edges.add(new Edge(2, "", 0, 0, 1));
		node1.edges.add(new Edge(4, "", 0, 0, 1));
		
		node2.edges.add(new Edge(3, "", 0, 0, 1));
		node2.edges.add(new Edge(5, "", 0, 0, 1));
		
		node3.edges.add(new Edge(6, "", 0, 0, 1));
		
		node4.edges.add(new Edge(5, "", 0, 0, 1));
		node4.edges.add(new Edge(7, "", 0, 0, 1));
		
		node5.edges.add(new Edge(6, "", 0, 0, 3));
		node5.edges.add(new Edge(8, "", 0, 0, 3));
		
		node6.edges.add(new Edge(9, "", 0, 0, 1));
		
		node7.edges.add(new Edge(8, "", 0, 0, 1));
		
		node8.edges.add(new Edge(9, "", 0, 0, 1));
		
		node9.edges.add(new Edge(1, "", 0, 0, 1));

		return normalNodes;
	}
}
