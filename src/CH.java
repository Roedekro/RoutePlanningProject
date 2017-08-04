import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import tool.Edge;
import tool.Node;
import tool.Tool;

public class CH {
	
	private int hoplimit = 100;
	private static int shift = 33;

	private long preprocessStart = 0;
	private long preprocessStop = 0;
	private long queryStart = 0;
	private long queryStop = 0;
	private long preprocessTotal = 0;
	private long queryTotal = 0;
	long preprocessTime = 0;
	long queryTime = 0;
	ArrayList<ALTNode> check = null;
	long nodesChecked = 0;
	
	public long CHNaivebyNodeID(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		//ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		ArrayList<Node> normalNodes = createTest();
		source = 1;
		target = 9;
		
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
			
			System.out.println("=== Contracting node "+node.id+" "+i);
			
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
			for(int j = 0; j < node.shortcutsUpward.size(); j++) {
				nShort = node.shortcutsUpward.get(j);
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
			for(int j = 0; j < node.shortcutsDownward.size(); j++) {
				nShort = node.shortcutsDownward.get(j);
				neighbour = hashMap.get(nShort.nodeID);
				if(!neighbour.deletedFromGraph) {
					neighbour.edgeDistance = nShort.travelTime;
					neighboursIngoing.add(neighbour);
				}
			}
			
			// Now for all ingoing edges (w,v) find the shortest path excluding v
			// from w to all w' in the outgoing edges (v,w').
			// We do this using a normal Dijkstra search.
			CHNode localSource = null;
			ArrayList<Shortcut> toBeAdded = new ArrayList<Shortcut>();
			if(neighboursIngoing.size() > 0 && neighboursOutgoing.size() > 0) {
				for(int j = 0; j < neighboursIngoing.size(); j++) {
					ArrayList<CHNode> toBeReset = new ArrayList<CHNode>();
					localSource = neighboursIngoing.get(j);
					System.out.println(node.id+" Checking ingoing from "+localSource.id);
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
						System.out.println("Extracted "+min.id);
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
									System.out.println("Decreasing "+decreaseNode.id);
									tree.decreaseKey(decreaseNode, newPathLength);
								}
								else {
									System.out.println("Inserting "+decreaseNode.id);
									toBeReset.add(decreaseNode);
									decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
									decreaseNode.inserted = true;
									tree.insertNode(decreaseNode);
								}
							}
						}
						// Same for shortcuts
						Shortcut shortcut = null;
						for(int x = 0; x < min.shortcutsUpward.size(); x++) {
							shortcut = min.shortcutsUpward.get(x);
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
						System.out.println(min.id+" "+min.pathLength+" "+(min.edgeDistance+ localSource.edgeDistance));
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
			for(int x = 0; x < toBeAdded.size(); x++) {
				shortcut = toBeAdded.get(x);
				neighbour = hashMap.get(shortcut.fromID);
				if(shortcut.nodeID != shortcut.fromID) {
					if(neighbour.hierarcyLevel >= shortcut.level) {
					System.out.println("Adding shortcut forward "+shortcut.fromID+" "+shortcut.nodeID +" "+shortcut.travelTime);
					neighbour.shortcutsUpward.add(shortcut);
					}
					else {
						// Add it as a downward reverse shortcut
						shortcut = new Shortcut(shortcut);
						neighbour = hashMap.get(shortcut.fromID);
						System.out.println("Adding shortcut backward "+shortcut.fromID+" "+shortcut.nodeID+" "+shortcut.travelTime);
						neighbour.shortcutsDownward.add(shortcut);
					}
				}	
			}
			
			// If shortcuts for all ingoing nodes were found we can safely delete the node
			if(toBeAdded.size() == neighboursOutgoing.size() && neighboursOutgoing.size() != 0) {
				System.out.println("Deleting "+node.id);
				node.deletedFromGraph = true;
			}
	
			
		}
		
		// All nodes now contracted
		
		// Add reverse shortcuts
		/*for(int i = 0; i < nodes.size(); i++)) {
			node = nodes.get(i);
			Shortcut shortcut = null;
			Shortcut newShortcut = null;
			for(int j = 0; j < node.shortcutsUpward)
		}*/
		
		System.out.println("=============== Dijkstra");
		
		CHNode meet = null;
		long shortest = 0;
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
				System.out.println("Extracted from 1 "+node1.id);
			}
			node1.deleted = true;
			if(biTree.size > 0) {
				node2 = (CHNode) biTree.deleteMin();
				System.out.println("Extracted from 2 "+node2.id);
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
						System.out.println("1 Inserting edge to "+decreaseNode1.id+" "+node1.id+" "+edge1.travelTime);
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
			for(int i = 0; i < node1.shortcutsUpward.size(); i++) {
				shortcut1 = node1.shortcutsUpward.get(i);
				decreaseNode1 = hashMap.get(shortcut1.nodeID);
				if(decreaseNode1.hierarcyLevel > node1.hierarcyLevel) {
					long newPathLenght = node1.pathLength + shortcut1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						System.out.println("1 Inserting shortcut to "+decreaseNode1.id+" "+node1.id+" "+shortcut1.travelTime);
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
						System.out.println("2 Inserting edge to "+decreaseNode2.id+" "+node2.id+" "+edge2.travelTime);
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
			for(int i = 0; i < node2.shortcutsDownward.size(); i++) {
				shortcut2 = node2.shortcutsDownward.get(i);
				decreaseNode2 = hashMap.get(shortcut2.nodeID);
				if(decreaseNode2.hierarcyLevel < node2.hierarcyLevel) {
					long newPathLenght = node2.pathLength2 + shortcut2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						System.out.println("2 Inserting shortcut to "+decreaseNode2.id+" "+node2.id+" "+shortcut2.travelTime);
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
		System.out.println("Sizes "+tree.size + " " + biTree.size);
		System.out.println("Meet="+meet.id+" "+meet.pathLength+" "+meet.pathLength2);
		return shortest;
	}
	
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
