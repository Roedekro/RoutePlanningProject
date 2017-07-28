import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tool.Edge;
import tool.Node;
import tool.Tool;

public class Dijkstra {
	
	private static int shift = 33;
	
	private long preprocessStart = 0;
	private long preprocessStop = 0;
	private long queryStart = 0;
	private long queryStop = 0;
	private long preprocessTotal = 0;
	private long queryTotal = 0;
	long preprocessTime = 0;
	long queryTime = 0;
	ArrayList<RedBlackNode> check = null;
	long nodesChecked = 0;

	public long dijkstra(String input, long source, long target, int runs) throws IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		
		HashMap<Long,RedBlackNode> hashMap = new HashMap<Long,RedBlackNode>();
		
		// Selection random source and target
		// Alternatively always use the same source and target
		if(source == 0 || target == 0) {
			Random random = new Random();
			source = normalNodes.get(random.nextInt(normalNodes.size())).id;
			target = normalNodes.get(random.nextInt(normalNodes.size())).id;
		}

		RedBlackNode sourceNode = null;
		RedBlackNode targetNode = null;
		RedBlackNode node = null;
		Node normalNode = null;
		
		ArrayList<RedBlackNode> nodes = new ArrayList<RedBlackNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			normalNode = normalNodes.get(i);
			node = new RedBlackNode(normalNode);
			nodes.add(node);
			hashMap.put(node.id, node);
		}
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		for(int r = 0; r < runs; r++) {
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			RedBlackTree tree = new RedBlackTree();
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				// Reset
				node.key = Long.MAX_VALUE - i;
				node.colour = false;
				node.deleted = false;
				node.parent = null;
				node.leftChild = null;
				node.rightChild = null;
				node.path = null;
				node.pathLength = Long.MAX_VALUE;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
				}
			}
			preprocessStop = System.currentTimeMillis();
			preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			// Insert
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				tree.insertNode(node);
			}
			
			// Dijkstra
			node = sourceNode;
			while(node.id != targetNode.id) {
				node = tree.deleteMin();
				nodesChecked++;
				node.deleted = true;
				Edge edge = null;
				RedBlackNode decreaseNode = null;
				for(int i = 0; i < node.edges.size(); i++) {
					edge = node.edges.get(i);
					decreaseNode = hashMap.get(edge.nodeID);
					long newPathLenght = node.pathLength + edge.travelTime;
					if(!decreaseNode.deleted && newPathLenght < decreaseNode.pathLength) {
						decreaseNode.path = node;
						decreaseNode.pathLength = newPathLenght;
						tree.decreaseKey(decreaseNode, newPathLenght);
					}
				}
			}
			
			// Need to find the path as well, not just the path length
			ArrayList<RedBlackNode> path = new ArrayList<RedBlackNode>();
			
			// Run through path writing out the route
			while(node.id != sourceNode.id) {
				path.add(node);
				node = node.path;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
			
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
		
		return targetNode.pathLength;
		
	}
	
	public long dijkstraDelayedInsert(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		
		HashMap<Long,RedBlackNode> hashMap = new HashMap<Long,RedBlackNode>();
		
		// Selection random source and target
		// Alternatively always use the same source and target
		if(source == 0 || target == 0) {
			Random random = new Random();
			source = normalNodes.get(random.nextInt(normalNodes.size())).id;
			target = normalNodes.get(random.nextInt(normalNodes.size())).id;
		}

		RedBlackNode sourceNode = null;
		RedBlackNode targetNode = null;
		RedBlackNode node = null;
		Node normalNode = null;
		
		ArrayList<RedBlackNode> nodes = new ArrayList<RedBlackNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			normalNode = normalNodes.get(i);
			node = new RedBlackNode(normalNode);
			nodes.add(node);
			hashMap.put(node.id, node);
		}
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		for(int r = 0; r < runs; r++) {
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			RedBlackTree tree = new RedBlackTree();
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				// Reset
				node.key = Long.MAX_VALUE - i;
				node.colour = false;
				node.deleted = false;
				node.inserted = false;
				node.parent = null;
				node.leftChild = null;
				node.rightChild = null;
				node.path = null;
				node.pathLength = Long.MAX_VALUE;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
				}
			}
			preprocessStop = System.currentTimeMillis();
			preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			// Insert
			sourceNode.inserted = true;
			tree.insertNode(sourceNode);
			
			// Dijkstra
			node = sourceNode;
			while(node.id != targetNode.id) {
				node = tree.deleteMin();
				nodesChecked++;
				node.deleted = true;
				Edge edge = null;
				RedBlackNode decreaseNode = null;
				for(int i = 0; i < node.edges.size(); i++) {
					edge = node.edges.get(i);
					decreaseNode = hashMap.get(edge.nodeID);
					long newPathLenght = node.pathLength + edge.travelTime;
					if(!decreaseNode.deleted && newPathLenght < decreaseNode.pathLength) {
						decreaseNode.path = node;
						decreaseNode.pathLength = newPathLenght;
						if(decreaseNode.inserted) {
							tree.decreaseKey(decreaseNode, newPathLenght);
						}
						else {
							decreaseNode.key = calcKey(newPathLenght,decreaseNode.id);
							decreaseNode.inserted = true;
							tree.insertNode(decreaseNode);
						}
					}
				}
			}
			
			// Need to find the path as well, not just the path length
			ArrayList<RedBlackNode> path = new ArrayList<RedBlackNode>();
			
			// Run through path writing out the route
			while(node.id != sourceNode.id) {
				path.add(node);
				node = node.path;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
		
		return targetNode.pathLength;
	}
	
	// Shift val shift places to the left to make space for ID of 8bil.
	private long calcKey(long newPathLenght, long id) {
		long ret = newPathLenght;
		ret = ret << shift;
		ret = ret+id;
		return ret;
	}
	
	public long bidirectionalDijkstra(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> nodes = tool.getNodesAsArrayList(input);
		ArrayList<BiRedBlackNode> binodes = new ArrayList<BiRedBlackNode>();
		
		HashMap<Long,BiRedBlackNode> hashMap = new HashMap<Long,BiRedBlackNode>();
		
		// Selection random source and target
		// Alternatively always use the same source and target
		if(source == 0 || target == 0) {
			Random random = new Random();
			source = nodes.get(random.nextInt(nodes.size())).id;
			target = nodes.get(random.nextInt(nodes.size())).id;
		}

		BiRedBlackNode sourceNode = null;
		BiRedBlackNode targetNode = null;
		BiRedBlackNode node = null;
		Node normalNode = null;
		
		for(int i = 0; i < nodes.size(); i++) {
			normalNode = nodes.get(i);
			node = new BiRedBlackNode(normalNode);
			binodes.add(node);
			hashMap.put(node.id, node);
		}
		
		// Add opposite edges
		for(int i = 0; i < binodes.size(); i++) {
			node = binodes.get(i);
			Edge biEdge = null;
			Edge newEdge = null;
			BiRedBlackNode toNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				biEdge = node.edges.get(j);
				newEdge = new Edge(node.id,biEdge.type,biEdge.distance,biEdge.maxSpeed,biEdge.travelTime);
				toNode = hashMap.get(biEdge.nodeID);
				toNode.addEdge2(newEdge);
			}
			
		}
		
		long shortest = Long.MAX_VALUE; // For use below
		BiRedBlackNode smallest = null;
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		for(int r = 0; r < runs; r++) {
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			shortest = Long.MAX_VALUE;
			smallest = null;
			
			RedBlackTree tree = new RedBlackTree();
			BiRedBlackTree biTree = new BiRedBlackTree();
			for(int i = 0; i < binodes.size(); i++) {
				node = binodes.get(i);
				node.key = node.key - i;
				node.key2 = node.key2 - i;
				node.colour = false;
				node.colour2 = false;
				node.deleted = false;
				node.deleted2 = false;
				node.parent = null;
				node.parent2 = null;
				node.leftChild = null;
				node.leftChild2 = null;
				node.rightChild = null;
				node.rightChild2 = null;
				node.path = null;
				node.path2 = null;
				node.pathLength = Long.MAX_VALUE;
				node.pathLength2 = Long.MAX_VALUE;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
					node.key2 = 0;
					node.pathLength2 = 0;
				}
			}
			
			preprocessStop = System.currentTimeMillis();
			preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			// Insert
			for(int i = 0; i < binodes.size(); i++) {
				node = binodes.get(i);
				tree.insertNode(node);
				biTree.insertNode(node);
			}
			
			// Bidirectional Dijkstra
			BiRedBlackNode node1 = sourceNode;
			BiRedBlackNode node2 = targetNode;
			while(node1.id != targetNode.id || node2.id != sourceNode.id) {
				node1 = (BiRedBlackNode) tree.deleteMin();
				nodesChecked++;
				if(node1.deleted2) {
					break;
				}
				node2 = biTree.deleteMin();
				nodesChecked++;
				node1.deleted = true;
				node2.deleted2 = true;
				if(node2.deleted) {
					break;
				}
				if(node1.pathLength + node2.pathLength2 >= shortest) {
					//System.out.println("Test");
					break;
				}
				Edge edge1 = null;
				BiRedBlackNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					long newMin = 0;
					long newPathLenght = node1.pathLength + edge1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = newPathLenght;
						newMin = decreaseNode1.pathLength + decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode1;
						}
						tree.decreaseKey(decreaseNode1, newPathLenght);
					}
				}
				Edge edge2 = null;
				BiRedBlackNode decreaseNode2 = null;
				for(int i = 0; i < node2.edges2.size(); i++) {
					edge2 = node2.edges2.get(i);
					decreaseNode2 = hashMap.get(edge2.nodeID);
					long newMin = 0;
					long newPathLenght = node2.pathLength2 + edge2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = newPathLenght;
						newMin = decreaseNode2.pathLength + decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = newMin;
							smallest = decreaseNode2;
						}
						biTree.decreasekey(decreaseNode2, newPathLenght);
					}
				}
			}
			
			ArrayList<RedBlackNode> path = new ArrayList<RedBlackNode>();
			
			// Run through all nodes and find smallest pathLenght + pathLength2
			/*long val = 0;
			for(int i = 0; i < binodes.size(); i++) {
				node = binodes.get(i);
				val = node.pathLength + node.pathLength2;
				if(val < shortest && val > 1 ) {
					shortest = val;
					smallest = node;
				}
			}*/
			
			// Found a node on shortest path, follow it
			node = smallest;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (BiRedBlackNode) node.path;
			}
			path.add(node);
			node = smallest;
			while(node.id != targetNode.id) {
				path.add(node);
				node = (BiRedBlackNode) node.path2;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;

		return shortest;
	}
	
public long bidirectionalDijkstraDelayedInsert(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> nodes = tool.getNodesAsArrayList(input);
		ArrayList<BiRedBlackNode> binodes = new ArrayList<BiRedBlackNode>();
		
		HashMap<Long,BiRedBlackNode> hashMap = new HashMap<Long,BiRedBlackNode>();
		
		// Selection random source and target
		// Alternatively always use the same source and target
		if(source == 0 || target == 0) {
			Random random = new Random();
			source = nodes.get(random.nextInt(nodes.size())).id;
			target = nodes.get(random.nextInt(nodes.size())).id;
		}

		BiRedBlackNode sourceNode = null;
		BiRedBlackNode targetNode = null;
		BiRedBlackNode node = null;
		Node normalNode = null;
		
		for(int i = 0; i < nodes.size(); i++) {
			normalNode = nodes.get(i);
			node = new BiRedBlackNode(normalNode);
			binodes.add(node);
			hashMap.put(node.id, node);
		}
		
		// Add opposite edges
		for(int i = 0; i < binodes.size(); i++) {
			node = binodes.get(i);
			Edge biEdge = null;
			Edge newEdge = null;
			BiRedBlackNode toNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				biEdge = node.edges.get(j);
				newEdge = new Edge(node.id,biEdge.type,biEdge.distance,biEdge.maxSpeed,biEdge.travelTime);
				toNode = hashMap.get(biEdge.nodeID);
				toNode.addEdge2(newEdge);
			}
			
		}
		
		long shortest = Long.MAX_VALUE; // For use below
		BiRedBlackNode smallest = null;
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		for(int r = 0; r < runs; r++) {
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			shortest = Long.MAX_VALUE;
			smallest = null;
						
			for(int i = 0; i < binodes.size(); i++) {
				node = binodes.get(i);
				node.key = node.key - i;
				node.key2 = node.key2 - i;
				node.colour = false;
				node.colour2 = false;
				node.deleted = false;
				node.deleted2 = false;
				node.inserted = false;
				node.inserted2 = false;
				node.parent = null;
				node.parent2 = null;
				node.leftChild = null;
				node.leftChild2 = null;
				node.rightChild = null;
				node.rightChild2 = null;
				node.path = null;
				node.path2 = null;
				node.pathLength = Long.MAX_VALUE;
				node.pathLength2 = Long.MAX_VALUE;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
					node.key2 = 0;
					node.pathLength2 = 0;
				}
			}
			
			preprocessStop = System.currentTimeMillis();
			preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			RedBlackTree tree = new RedBlackTree();
			BiRedBlackTree biTree = new BiRedBlackTree();
				
			// Insert
			tree.insertNode(sourceNode);
			sourceNode.inserted = true;
			biTree.insertNode(targetNode);
			targetNode.inserted2 = true;
			
			// Bidirectional Dijkstra
			BiRedBlackNode node1 = sourceNode;
			BiRedBlackNode node2 = targetNode;
			while(node1.id != targetNode.id && node2.id != sourceNode.id) {
				node1 = (BiRedBlackNode) tree.deleteMin();
				nodesChecked++;
				if(node1.deleted2) {
					break;
				}
				node2 = biTree.deleteMin();
				nodesChecked++;
				node1.deleted = true;
				node2.deleted2 = true;
				if(node2.deleted) {
					break;
				}
				if(node1.pathLength + node2.pathLength2 >= shortest) {
					//System.out.println("Test");
					break;
				}
				Edge edge1 = null;
				BiRedBlackNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					long newMin = 0;
					long newPathLenght = node1.pathLength + edge1.travelTime;
					if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
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
				Edge edge2 = null;
				BiRedBlackNode decreaseNode2 = null;
				for(int i = 0; i < node2.edges2.size(); i++) {
					edge2 = node2.edges2.get(i);
					decreaseNode2 = hashMap.get(edge2.nodeID);
					long newMin = 0;
					long newPathLenght = node2.pathLength2 + edge2.travelTime;
					if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
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
			
			ArrayList<RedBlackNode> path = new ArrayList<RedBlackNode>();
			
			// Run through all nodes and find smallest pathLenght + pathLength2
			/*long val = 0;
			for(int i = 0; i < binodes.size(); i++) {
				node = binodes.get(i);
				val = node.pathLength + node.pathLength2;
				if(val < shortest && val > 1 ) {
					shortest = val;
					smallest = node;
				}
			}*/
			
			// Found a node on shortest path, follow it
			node = smallest;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (BiRedBlackNode) node.path;
			}
			path.add(node);
			node = smallest;
			while(node.id != targetNode.id) {
				path.add(node);
				node = (BiRedBlackNode) node.path2;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;

		return shortest;
	}
}
