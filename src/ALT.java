import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tool.Edge;
import tool.Node;
import tool.Tool;

public class ALT {

	private int invalidateTreshold = 10;
	private int minEdgesInvalidate = 2;
	private static int shift = 33;
	double cmHour = 130*27.7777778;
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
	
	/**
	 * 
	 * @param input Node file
	 * @param source
	 * @param target
	 * @param k number of landmarks
	 * @param u number of landmarks to use in search
	 * @param o number of optimizations on landmarks, if relevant
	 * @param typeOfLandMark typeOfLandMark 1 = random, 2 = farthest, 3 = farthest optimized
	 * @param runs
	 * @return length of shortest path in milliseconds
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public long ALTBidirectionalSearch(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		ArrayList<ALTNode> nodes = new ArrayList<ALTNode>();
		ALTNode sourceNode = null;
		ALTNode targetNode = null;
		ALTNode node = null;
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
			nodes.add(node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Build up hashmap
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		// Fill in hashmap
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			hashMap.put(node.id, node);
		}
		// Fill in reverse edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			ALTNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		ArrayList<ALTNode> landmarks = null;
		if(typeOfLandMark == 1) {
			landmarks = computeRandomLandmarks(k, nodes);
		}
		else if(typeOfLandMark == 2) {
			landmarks = computeFarthestLandmarks(k, nodes);
		}
		else if(typeOfLandMark == 3) {
			landmarks = computeOptimizedFarthestLandmarks(k, o, nodes);
		}
		
		ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
		System.out.println("Invalidated "+invalidLandmarks.size() + " landmarks");
		
		// Pick which landmarks to use for the forward and reverse search
		ArrayList<Integer> forwardLandmarks = new ArrayList<Integer>();
		ArrayList<Integer> backwardLandmarks = new ArrayList<Integer>();
		long min = Long.MAX_VALUE;
		int minNode = 0;
		long dist = 0;
		long dist2 = 0;
		boolean contains = false;
		
		// Select the u closest landmarks to source for the reverse search.
		// The landmark must also be further away from the target than the source.
		for(int i = 0; i < u; i++) {
			
			min = Long.MAX_VALUE;
			minNode = -1;
			dist = 0;
			
			for(int j = 0; j < landmarks.size(); j++) {
				contains = false;
				// Brute force check if we already selected this landmark
				for(int x = 0; x < backwardLandmarks.size(); x++) {
					if(j == backwardLandmarks.get(x)) {
						contains = true;
					}
				}
				for(int x = 0; x < invalidLandmarks.size(); x++) {
					if(j == invalidLandmarks.get(x)) {
						contains = true;
					}
				}			
				if(!contains) {
					node = landmarks.get(j);
					dist = calculateDistance(sourceNode, node);
					dist2 = calculateDistance(targetNode, node);
					if(dist < min && dist < dist2) {
						min = dist;
						minNode = j;
					}
				}
			}
			if(minNode == -1) {
				System.out.println("Warning1: Did not find landmark "+i);
			}
			else {
				backwardLandmarks.add(minNode);
			}
			
		}
		
		// Select the u closest landmarks to target for the search.
		// The landmark must also be further away from the source than the target.
		for(int i = 0; i < u; i++) {
			
			min = Long.MAX_VALUE;
			minNode = -1;
			dist = 0;
			
			for(int j = 0; j < landmarks.size(); j++) {
				contains = false;
				// Brute force check if we already selected this landmark
				for(int x = 0; x < forwardLandmarks.size(); x++) {
					if(j == forwardLandmarks.get(x)) {
						contains = true;
					}
				}
				for(int x = 0; x < invalidLandmarks.size(); x++) {
					if(j == invalidLandmarks.get(x)) {
						contains = true;
					}
				}
				if(!contains) {
					node = landmarks.get(j);
					dist = calculateDistance(targetNode, node);
					dist2 = calculateDistance(sourceNode, node);
					if(dist < min && dist < dist2) {
						min = dist;
						minNode = j;
					}
				}
			}
			if(minNode == -1) {
				System.out.println("Warning2: Did not find landmark "+i);
			}
			else {
				forwardLandmarks.add(minNode);
			}
			
		}
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		long shortest = Long.MAX_VALUE; // For use below
		ALTNode smallest = null;
		
		for(int r = 0; r < runs; r++) {
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			shortest = Long.MAX_VALUE;
			smallest = null;
			
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				// Reset
				node.key = Long.MAX_VALUE - i;
				node.key2 = Long.MAX_VALUE - i;
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
				node.keyLength = Long.MAX_VALUE;
				node.keyLength2 = Long.MAX_VALUE;
				node.potential = 0;
				node.potential2 = 0;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
					node.keyLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
					node.key2 = 0;
					node.pathLength2 = 0;
					node.keyLength2 = 0;
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
			ALTNode node1 = sourceNode;
			ALTNode node2 = targetNode;
			//while(true) {
			while(node1.id != targetNode.id && node2.id != sourceNode.id) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(node1.deleted2) {
					break;
				}
				if(node1.potential == 0) {
					long forward = 0;
					long backward = 0;
					for(int i = 0; i < node1.landmarksForwardDistances.size(); i++) {
						if(forward < node1.landmarksForwardDistances.get(i)) {
							forward = node1.landmarksForwardDistances.get(i);
						}
					}
					for(int i = 0; i < node1.landmarksBackwardDistances.size(); i++) {
						if(backward < node1.landmarksBackwardDistances.get(i)) {
							backward = node1.landmarksBackwardDistances.get(i);
						}
					}
					node1.potential = (forward-backward)/2;
				}
				node2 = (ALTNode) biTree.deleteMin();
				nodesChecked++;
				if(node2.deleted) {
					break;
				}
				if(node2.potential2 == 0) {
					long forward = 0;
					long backward = 0;
					for(int i = 0; i < node2.landmarksForwardDistances.size(); i++) {
						if(forward < node2.landmarksForwardDistances.get(i)) {
							forward = node2.landmarksForwardDistances.get(i);
						}
					}
					for(int i = 0; i < node2.landmarksBackwardDistances.size(); i++) {
						if(backward < node2.landmarksBackwardDistances.get(i)) {
							backward = node2.landmarksBackwardDistances.get(i);
						}
					}
					node2.potential2 = (backward - forward)/2;
				}
				if(node1.pathLength + node2.pathLength2 >= shortest) {
					break;
				}
				node1.deleted = true;
				node2.deleted2 = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					if(decreaseNode1.potential == 0) {
						long forward = 0;
						long backward = 0;
						for(int j = 0; j < decreaseNode1.landmarksForwardDistances.size(); j++) {
							if(forward < decreaseNode1.landmarksForwardDistances.get(j)) {
								forward = decreaseNode1.landmarksForwardDistances.get(j);
							}
						}
						for(int j = 0; j < decreaseNode1.landmarksBackwardDistances.size(); j++) {
							if(backward < decreaseNode1.landmarksBackwardDistances.get(j)) {
								backward = decreaseNode1.landmarksBackwardDistances.get(j);
							}
						}
						decreaseNode1.potential = (forward-backward)/2;
					}
					long newKeyLength = node1.pathLength + edge1.travelTime + 
							decreaseNode1.potential - node1.potential;
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
						decreaseNode1.keyLength = newKeyLength;
						long newMin = decreaseNode1.pathLength+decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode1.pathLength+decreaseNode1.pathLength2;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newKeyLength);
						}
						else {
							decreaseNode1.key = calcKey(newKeyLength,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
				Edge edge2 = null;
				ALTNode decreaseNode2 = null;
				for(int i = 0; i < node2.edges2.size(); i++) {
					edge2 = node2.edges2.get(i);
					decreaseNode2 = hashMap.get(edge2.nodeID);
					if(decreaseNode2.potential2 == 0) {
						long forward = 0;
						long backward = 0;
						for(int j = 0; j < decreaseNode2.landmarksForwardDistances.size(); j++) {
							if(forward < decreaseNode2.landmarksForwardDistances.get(j)) {
								forward = decreaseNode2.landmarksForwardDistances.get(j);
							}
						}
						for(int j = 0; j < decreaseNode2.landmarksBackwardDistances.size(); j++) {
							if(backward < decreaseNode2.landmarksBackwardDistances.get(j)) {
								backward = decreaseNode2.landmarksBackwardDistances.get(j);
							}
						}
						decreaseNode2.potential2 = (backward - forward)/2;
					}
					long newKeyLength = node2.pathLength2 + edge2.travelTime + 
							decreaseNode2.potential2 - node2.potential2;
					if(!decreaseNode2.deleted2 && newKeyLength < decreaseNode2.keyLength2) {
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = node2.pathLength2 + edge2.travelTime;
						decreaseNode2.keyLength2 = newKeyLength;
						long newMin = decreaseNode2.pathLength+decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode2.pathLength+decreaseNode2.pathLength2;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newKeyLength);
						}
						else {
							decreaseNode2.key2 = calcKey(newKeyLength,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
			
			ArrayList<ALTNode> path = new ArrayList<ALTNode>();
			
			// Found a node on shortest path, follow it
			node = smallest;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (ALTNode) node.path;
			}
			path.add(node);
			node = smallest;
			while(node.id != targetNode.id) {
				path.add(node);
				node = (ALTNode) node.path2;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
		}
		
		return shortest;
	}
	
	
	
	/**
	 * 
	 * @param input Node file
	 * @param source
	 * @param target
	 * @param k number of landmarks
	 * @param u number of landmarks to use in search
	 * @param o number of optimizations on landmarks, if relevant
	 * @param typeOfLandMark typeOfLandMark 1 = random, 2 = farthest, 3 = farthest optimized
	 * @param runs
	 * @return length of shortest path in milliseconds
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public long ALTBidirectionalWorksButShouldnt(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		ArrayList<ALTNode> nodes = new ArrayList<ALTNode>();
		ALTNode sourceNode = null;
		ALTNode targetNode = null;
		ALTNode node = null;
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
			nodes.add(node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Build up hashmap
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		// Fill in hashmap
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			hashMap.put(node.id, node);
		}
		// Fill in reverse edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			ALTNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		ArrayList<ALTNode> landmarks = null;
		if(typeOfLandMark == 1) {
			landmarks = computeRandomLandmarks(k, nodes);
		}
		else if(typeOfLandMark == 2) {
			landmarks = computeFarthestLandmarks(k, nodes);
		}
		else if(typeOfLandMark == 3) {
			landmarks = computeOptimizedFarthestLandmarks(k, o, nodes);
		}
		
		ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
		System.out.println("Invalidated "+invalidLandmarks.size() + " landmarks");
		
		// Pick which landmarks to use for the forward and reverse search
		ArrayList<Integer> forwardLandmarks = new ArrayList<Integer>();
		ArrayList<Integer> backwardLandmarks = new ArrayList<Integer>();
		long min = Long.MAX_VALUE;
		int minNode = 0;
		long dist = 0;
		long dist2 = 0;
		boolean contains = false;
		
		// Select the u closest landmarks to source for the reverse search.
		// The landmark must also be further away from the target than the source.
		for(int i = 0; i < u; i++) {
			
			min = Long.MAX_VALUE;
			minNode = -1;
			dist = 0;
			
			for(int j = 0; j < landmarks.size(); j++) {
				contains = false;
				// Brute force check if we already selected this landmark
				for(int x = 0; x < backwardLandmarks.size(); x++) {
					if(j == backwardLandmarks.get(x)) {
						contains = true;
					}
				}
				for(int x = 0; x < invalidLandmarks.size(); x++) {
					if(j == invalidLandmarks.get(x)) {
						contains = true;
					}
				}			
				if(!contains) {
					node = landmarks.get(j);
					dist = calculateDistance(sourceNode, node);
					dist2 = calculateDistance(targetNode, node);
					if(dist < min && dist < dist2) {
						min = dist;
						minNode = j;
					}
				}
			}
			if(minNode == -1) {
				System.out.println("Warning1: Did not find landmark "+i);
			}
			else {
				backwardLandmarks.add(minNode);
			}
			
		}
		
		// Select the u closest landmarks to target for the search.
		// The landmark must also be further away from the source than the target.
		for(int i = 0; i < u; i++) {
			
			min = Long.MAX_VALUE;
			minNode = -1;
			dist = 0;
			
			for(int j = 0; j < landmarks.size(); j++) {
				contains = false;
				// Brute force check if we already selected this landmark
				for(int x = 0; x < forwardLandmarks.size(); x++) {
					if(j == forwardLandmarks.get(x)) {
						contains = true;
					}
				}
				for(int x = 0; x < invalidLandmarks.size(); x++) {
					if(j == invalidLandmarks.get(x)) {
						contains = true;
					}
				}
				if(!contains) {
					node = landmarks.get(j);
					dist = calculateDistance(targetNode, node);
					dist2 = calculateDistance(sourceNode, node);
					if(dist < min && dist < dist2) {
						min = dist;
						minNode = j;
					}
				}
			}
			if(minNode == -1) {
				System.out.println("Warning2: Did not find landmark "+i);
			}
			else {
				forwardLandmarks.add(minNode);
			}
			
		}
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		long shortest = Long.MAX_VALUE; // For use below
		ALTNode smallest = null;
		
		for(int r = 0; r < runs; r++) {
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			shortest = Long.MAX_VALUE;
			smallest = null;
			
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				// Reset
				node.key = Long.MAX_VALUE - i;
				node.key2 = Long.MAX_VALUE - i;
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
				node.keyLength = Long.MAX_VALUE;
				node.keyLength2 = Long.MAX_VALUE;
				node.potential = 0;
				node.potential2 = 0;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
					node.keyLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
					node.key2 = 0;
					node.pathLength2 = 0;
					node.keyLength2 = 0;
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
			ALTNode node1 = sourceNode;
			ALTNode node2 = targetNode;
			//while(true) {
			while(node1.id != targetNode.id && node2.id != sourceNode.id) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(node1.deleted2) {
					break;
				}
				node2 = (ALTNode) biTree.deleteMin();
				nodesChecked++;
				if(node2.deleted) {
					break;
				}
				/*if(node1.potential+node1.potential2 >= shortest || node2.potential+node2.potential2 >= shortest) {
					//System.out.println("TEST "+shortest);
					break;
				}*/
				/*if((node1.pathLength + node1.pathLength2 > 0 && node1.pathLength + node1.pathLength2 >= shortest)
						|| node2.pathLength + node2.pathLength2 > 0 && node2.pathLength + node2.pathLength2 >= shortest) {
					System.out.println("Test break "+nodesChecked);
					break;
				}*/
				node1.deleted = true;
				node2.deleted2 = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					long estimate = Long.MIN_VALUE;
					for(int j = 0; j < forwardLandmarks.size(); j++) {
						int l = forwardLandmarks.get(j);
						// dist(v,w) >= dist(v,L) - dist(w,L)
						//long val = node1.landmarksForwardDistances.get(l) - decreaseNode1.landmarksForwardDistances.get(l);
						//long val = node1.landmarksForwardDistances.get(l) - targetNode.landmarksForwardDistances.get(l);
						long val = decreaseNode1.landmarksForwardDistances.get(l) - targetNode.landmarksForwardDistances.get(l);
						if(val > estimate) {
							estimate = val;
						}
					}
					long newKeyLength = node1.pathLength + edge1.travelTime + 
							estimate;
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
						decreaseNode1.keyLength = newKeyLength;
						long newMin = decreaseNode1.pathLength+decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode1.pathLength+decreaseNode1.pathLength2;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newKeyLength);
						}
						else {
							decreaseNode1.key = calcKey(newKeyLength,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
				Edge edge2 = null;
				ALTNode decreaseNode2 = null;
				for(int i = 0; i < node2.edges2.size(); i++) {
					edge2 = node2.edges2.get(i);
					decreaseNode2 = hashMap.get(edge2.nodeID);
					long estimate = Long.MIN_VALUE;
					for(int j = 0; j < backwardLandmarks.size(); j++) {
						int l = backwardLandmarks.get(j);
						// dist(v,w) >= dist(L,w) - dist(L,v)
						//long val = decreaseNode2.landmarksBackwardDistances.get(l) - node2.landmarksBackwardDistances.get(l);
						long val = decreaseNode2.landmarksBackwardDistances.get(l) - sourceNode.landmarksBackwardDistances.get(l);
						if(val > estimate) {
							estimate = val;
						}
					}
					long newKeyLength = node2.pathLength2 + edge2.travelTime + 
							estimate;
					if(!decreaseNode2.deleted2 && newKeyLength < decreaseNode2.keyLength2) {
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = node2.pathLength2 + edge2.travelTime;
						decreaseNode2.keyLength2 = newKeyLength;
						long newMin = decreaseNode2.pathLength+decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode2.pathLength+decreaseNode2.pathLength2;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newKeyLength);
						}
						else {
							decreaseNode2.key2 = calcKey(newKeyLength,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
			
			ArrayList<ALTNode> path = new ArrayList<ALTNode>();
			
			// Found a node on shortest path, follow it
			node = smallest;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (ALTNode) node.path;
			}
			path.add(node);
			node = smallest;
			while(node.id != targetNode.id) {
				path.add(node);
				node = (ALTNode) node.path2;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
		}
		
		return shortest;
	}
	
	/**
	 * 
	 * @param input Node file
	 * @param source
	 * @param target
	 * @param k number of landmarks
	 * @param u number of landmarks to use in search
	 * @param o number of optimizations on landmarks, if relevant
	 * @param typeOfLandMark typeOfLandMark 1 = random, 2 = farthest, 3 = farthest optimized
	 * @param runs
	 * @return length of shortest path in milliseconds
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public long ALTBidirectionalSearchSymmetric(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		ArrayList<ALTNode> nodes = new ArrayList<ALTNode>();
		ALTNode sourceNode = null;
		ALTNode targetNode = null;
		ALTNode node = null;
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
			nodes.add(node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Build up hashmap
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		// Fill in hashmap
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			hashMap.put(node.id, node);
		}
		// Fill in reverse edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			ALTNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		ArrayList<ALTNode> landmarks = null;
		if(typeOfLandMark == 1) {
			landmarks = computeRandomLandmarks(k, nodes);
		}
		else if(typeOfLandMark == 2) {
			landmarks = computeFarthestLandmarks(k, nodes);
		}
		else if(typeOfLandMark == 3) {
			landmarks = computeOptimizedFarthestLandmarks(k, o, nodes);
		}
		
		ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
		System.out.println("Invalidated "+invalidLandmarks.size() + " landmarks");
		
		// Pick which landmarks to use for the forward and reverse search
		ArrayList<Integer> forwardLandmarks = new ArrayList<Integer>();
		ArrayList<Integer> backwardLandmarks = new ArrayList<Integer>();
		long min = Long.MAX_VALUE;
		int minNode = 0;
		long dist = 0;
		long dist2 = 0;
		boolean contains = false;
		
		// Select the u closest landmarks to source for the reverse search.
		// The landmark must also be further away from the target than the source.
		for(int i = 0; i < u; i++) {
			
			min = Long.MAX_VALUE;
			minNode = -1;
			dist = 0;
			
			for(int j = 0; j < landmarks.size(); j++) {
				contains = false;
				// Brute force check if we already selected this landmark
				for(int x = 0; x < backwardLandmarks.size(); x++) {
					if(j == backwardLandmarks.get(x)) {
						contains = true;
					}
				}
				for(int x = 0; x < invalidLandmarks.size(); x++) {
					if(j == invalidLandmarks.get(x)) {
						contains = true;
					}
				}			
				if(!contains) {
					node = landmarks.get(j);
					dist = calculateDistance(sourceNode, node);
					dist2 = calculateDistance(targetNode, node);
					if(dist < min && dist < dist2) {
						min = dist;
						minNode = j;
					}
				}
			}
			if(minNode == -1) {
				System.out.println("Warning1: Did not find landmark "+i);
			}
			else {
				backwardLandmarks.add(minNode);
			}
			
		}
		
		// Select the u closest landmarks to target for the search.
		// The landmark must also be further away from the source than the target.
		for(int i = 0; i < u; i++) {
			
			min = Long.MAX_VALUE;
			minNode = -1;
			dist = 0;
			
			for(int j = 0; j < landmarks.size(); j++) {
				contains = false;
				// Brute force check if we already selected this landmark
				for(int x = 0; x < forwardLandmarks.size(); x++) {
					if(j == forwardLandmarks.get(x)) {
						contains = true;
					}
				}
				for(int x = 0; x < invalidLandmarks.size(); x++) {
					if(j == invalidLandmarks.get(x)) {
						contains = true;
					}
				}
				if(!contains) {
					node = landmarks.get(j);
					dist = calculateDistance(targetNode, node);
					dist2 = calculateDistance(sourceNode, node);
					if(dist < min && dist < dist2) {
						min = dist;
						minNode = j;
					}
				}
			}
			if(minNode == -1) {
				System.out.println("Warning2: Did not find landmark "+i);
			}
			else {
				forwardLandmarks.add(minNode);
			}
			
		}
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		long shortest = Long.MAX_VALUE; // For use below
		ALTNode smallest = null;
		
		for(int r = 0; r < runs; r++) {
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			shortest = Long.MAX_VALUE;
			smallest = null;
			
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				// Reset
				node.key = Long.MAX_VALUE - i;
				node.key2 = Long.MAX_VALUE - i;
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
				node.keyLength = Long.MAX_VALUE;
				node.keyLength2 = Long.MAX_VALUE;
				node.potential = 0;
				node.potential2 = 0;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
					node.keyLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
					node.key2 = 0;
					node.pathLength2 = 0;
					node.keyLength2 = 0;
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
			ALTNode node1 = sourceNode;
			ALTNode node2 = targetNode;
			//while(true) {
			while(tree.size > 0 && biTree.size > 0) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(node1.potential == 0) {
					long estimate = Long.MIN_VALUE;
					for(int j = 0; j < forwardLandmarks.size(); j++) {
						int l = forwardLandmarks.get(j);
						long val = node1.landmarksForwardDistances.get(l) - targetNode.landmarksForwardDistances.get(l);					
						if(val > estimate) {
							estimate = val;
						}
					}
					node1.potential = estimate;
				}
				node2 = (ALTNode) biTree.deleteMin();
				nodesChecked++;
				if(node2.potential2 == 0) {
					long estimate = Long.MIN_VALUE;
					for(int j = 0; j < forwardLandmarks.size(); j++) {
						int l = forwardLandmarks.get(j);
						long val = node2.landmarksBackwardDistances.get(l) - sourceNode.landmarksBackwardDistances.get(l);					
						if(val > estimate) {
							estimate = val;
						}
					}
					node2.potential2 = estimate;
				}
				if(node1.pathLength + node1.potential >= shortest) {
					break;
				}
				if(node2.pathLength2 + node2.potential2 >= shortest) {
					break;
				}
				node1.deleted = true;
				node2.deleted2 = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					if(decreaseNode1.potential == 0) {
						long estimate = Long.MIN_VALUE;
						for(int j = 0; j < forwardLandmarks.size(); j++) {
							int l = forwardLandmarks.get(j);
							// dist(v,w) >= dist(v,L) - dist(w,L)
							//long val = node1.landmarksForwardDistances.get(l) - decreaseNode1.landmarksForwardDistances.get(l);
							long val = decreaseNode1.landmarksForwardDistances.get(l) - targetNode.landmarksForwardDistances.get(l);					
							if(val > estimate) {
								estimate = val;
							}
						}
						decreaseNode1.potential = estimate;
					}
					
					long newKeyLength = node1.pathLength + edge1.travelTime + 
							decreaseNode1.potential;
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
						decreaseNode1.keyLength = newKeyLength;
						long newMin = decreaseNode1.pathLength+decreaseNode1.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode1.pathLength+decreaseNode1.pathLength2;
							smallest = decreaseNode1;
						}
						if(decreaseNode1.inserted) {
							tree.decreaseKey(decreaseNode1, newKeyLength);
						}
						else {
							decreaseNode1.key = calcKey(newKeyLength,decreaseNode1.id);
							decreaseNode1.inserted = true;
							tree.insertNode(decreaseNode1);
						}
					}
				}
				Edge edge2 = null;
				ALTNode decreaseNode2 = null;
				for(int i = 0; i < node2.edges2.size(); i++) {
					edge2 = node2.edges2.get(i);
					decreaseNode2 = hashMap.get(edge2.nodeID);
					if(decreaseNode2.potential2 == 0) {
						long estimate = Long.MIN_VALUE;
						for(int j = 0; j < backwardLandmarks.size(); j++) {
							int l = backwardLandmarks.get(j);
							long val = decreaseNode2.landmarksBackwardDistances.get(l) - sourceNode.landmarksBackwardDistances.get(l);
							if(val > estimate) {
								estimate = val;
							}
						}
					}
					long newKeyLength = node2.pathLength2 + edge2.travelTime + 
							decreaseNode2.potential2;
					if(!decreaseNode2.deleted2 && newKeyLength < decreaseNode2.keyLength2) {
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = node2.pathLength2 + edge2.travelTime;
						decreaseNode2.keyLength2 = newKeyLength;
						long newMin = decreaseNode2.pathLength+decreaseNode2.pathLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode2.pathLength+decreaseNode2.pathLength2;
							smallest = decreaseNode2;
						}
						if(decreaseNode2.inserted2) {
							biTree.decreasekey(decreaseNode2, newKeyLength);
						}
						else {
							decreaseNode2.key2 = calcKey(newKeyLength,decreaseNode2.id);
							decreaseNode2.inserted2 = true;
							biTree.insertNode(decreaseNode2);
						}
					}
				}
			}
			
			ArrayList<ALTNode> path = new ArrayList<ALTNode>();
			
			// Found a node on shortest path, follow it
			node = smallest;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (ALTNode) node.path;
			}
			path.add(node);
			node = smallest;
			while(node.id != targetNode.id) {
				path.add(node);
				node = (ALTNode) node.path2;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
		}
		
		return shortest;
	}
	
	/**
	 * 
	 * @param input Node file
	 * @param source
	 * @param target
	 * @param k number of landmarks
	 * @param u number of landmarks to use in search
	 * @param o number of optimizations on landmarks, if relevant
	 * @param typeOfLandMark typeOfLandMark 1 = random, 2 = farthest, 3 = farthest optimized
	 * @param runs
	 * @return length of shortest path in milliseconds
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public long ALTBidirectionalSearchSymmetricLowerBounding(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		ArrayList<ALTNode> nodes = new ArrayList<ALTNode>();
		ALTNode sourceNode = null;
		ALTNode targetNode = null;
		ALTNode node = null;
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
			nodes.add(node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Build up hashmap
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		// Fill in hashmap
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			hashMap.put(node.id, node);
		}
		// Fill in reverse edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			ALTNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		ArrayList<ALTNode> landmarks = null;
		if(typeOfLandMark == 1) {
			landmarks = computeRandomLandmarks(k, nodes);
		}
		else if(typeOfLandMark == 2) {
			landmarks = computeFarthestLandmarks(k, nodes);
		}
		else if(typeOfLandMark == 3) {
			landmarks = computeOptimizedFarthestLandmarks(k, o, nodes);
		}
		
		ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
		System.out.println("Invalidated "+invalidLandmarks.size() + " landmarks");
		
		// Pick which landmarks to use for the forward and reverse search
		ArrayList<Integer> forwardLandmarks = new ArrayList<Integer>();
		ArrayList<Integer> backwardLandmarks = new ArrayList<Integer>();
		long min = Long.MAX_VALUE;
		int minNode = 0;
		long dist = 0;
		long dist2 = 0;
		boolean contains = false;
		
		// Select the u closest landmarks to source for the reverse search.
		// The landmark must also be further away from the target than the source.
		for(int i = 0; i < u; i++) {
			
			min = Long.MAX_VALUE;
			minNode = -1;
			dist = 0;
			
			for(int j = 0; j < landmarks.size(); j++) {
				contains = false;
				// Brute force check if we already selected this landmark
				for(int x = 0; x < backwardLandmarks.size(); x++) {
					if(j == backwardLandmarks.get(x)) {
						contains = true;
					}
				}
				for(int x = 0; x < invalidLandmarks.size(); x++) {
					if(j == invalidLandmarks.get(x)) {
						contains = true;
					}
				}			
				if(!contains) {
					node = landmarks.get(j);
					dist = calculateDistance(sourceNode, node);
					dist2 = calculateDistance(targetNode, node);
					if(dist < min && dist < dist2) {
						min = dist;
						minNode = j;
					}
				}
			}
			if(minNode == -1) {
				System.out.println("Warning1: Did not find landmark "+i);
			}
			else {
				backwardLandmarks.add(minNode);
			}
			
		}
		
		// Select the u closest landmarks to target for the search.
		// The landmark must also be further away from the source than the target.
		for(int i = 0; i < u; i++) {
			
			min = Long.MAX_VALUE;
			minNode = -1;
			dist = 0;
			
			for(int j = 0; j < landmarks.size(); j++) {
				contains = false;
				// Brute force check if we already selected this landmark
				for(int x = 0; x < forwardLandmarks.size(); x++) {
					if(j == forwardLandmarks.get(x)) {
						contains = true;
					}
				}
				for(int x = 0; x < invalidLandmarks.size(); x++) {
					if(j == invalidLandmarks.get(x)) {
						contains = true;
					}
				}
				if(!contains) {
					node = landmarks.get(j);
					dist = calculateDistance(targetNode, node);
					dist2 = calculateDistance(sourceNode, node);
					if(dist < min && dist < dist2) {
						min = dist;
						minNode = j;
					}
				}
			}
			if(minNode == -1) {
				System.out.println("Warning2: Did not find landmark "+i);
			}
			else {
				forwardLandmarks.add(minNode);
			}
			
		}
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		long shortest = Long.MAX_VALUE; // For use below
		ALTNode smallest = null;
		
		for(int r = 0; r < runs; r++) {
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			shortest = Long.MAX_VALUE;
			smallest = null;
			
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				// Reset
				node.key = Long.MAX_VALUE - i;
				node.key2 = Long.MAX_VALUE - i;
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
				node.keyLength = Long.MAX_VALUE;
				node.keyLength2 = Long.MAX_VALUE;
				node.potential = 0;
				node.potential2 = 0;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
					node.keyLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
					node.key2 = 0;
					node.pathLength2 = 0;
					node.keyLength2 = 0;
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
			ALTNode node1 = sourceNode;
			ALTNode node2 = targetNode;
			//while(true) {
			while(tree.size > 0 && biTree.size > 0) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(node1.potential == 0) {
					long estimate = Long.MIN_VALUE;
					for(int j = 0; j < forwardLandmarks.size(); j++) {
						int l = forwardLandmarks.get(j);
						long val = node1.landmarksForwardDistances.get(l) - targetNode.landmarksForwardDistances.get(l);					
						if(val > estimate) {
							estimate = val;
						}
					}
					node1.potential = estimate;
				}
				node2 = (ALTNode) biTree.deleteMin();
				nodesChecked++;
				if(node2.potential2 == 0) {
					long estimate = Long.MIN_VALUE;
					for(int j = 0; j < forwardLandmarks.size(); j++) {
						int l = forwardLandmarks.get(j);
						long val = node2.landmarksBackwardDistances.get(l) - sourceNode.landmarksBackwardDistances.get(l);					
						if(val > estimate) {
							estimate = val;
						}
					}
					node2.potential2 = estimate;
				}
				if(node1.pathLength + node1.potential >= shortest) {
					break;
				}
				if(node2.pathLength2 + node2.potential2 >= shortest) {
					break;
				}
				node1.deleted = true;
				node2.deleted2 = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					if(!decreaseNode1.deleted2) {
						if(decreaseNode1.potential == 0) {
							long estimate = Long.MIN_VALUE;
							for(int j = 0; j < forwardLandmarks.size(); j++) {
								int l = forwardLandmarks.get(j);
								// dist(v,w) >= dist(v,L) - dist(w,L)
								//long val = node1.landmarksForwardDistances.get(l) - decreaseNode1.landmarksForwardDistances.get(l);
								long val = decreaseNode1.landmarksForwardDistances.get(l) - targetNode.landmarksForwardDistances.get(l);					
								if(val > estimate) {
									estimate = val;
								}
							}
							decreaseNode1.potential = estimate;
						}
						
						long newKeyLength = node1.pathLength + edge1.travelTime + 
								decreaseNode1.potential;
						if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
							decreaseNode1.path = node1;
							decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
							decreaseNode1.keyLength = newKeyLength;
							long newMin = decreaseNode1.pathLength+decreaseNode1.pathLength2;
							if(newMin > 0 && newMin < shortest) {
								shortest = decreaseNode1.pathLength+decreaseNode1.pathLength2;
								smallest = decreaseNode1;
							}
							if(decreaseNode1.inserted) {
								tree.decreaseKey(decreaseNode1, newKeyLength);
							}
							else {
								decreaseNode1.key = calcKey(newKeyLength,decreaseNode1.id);
								decreaseNode1.inserted = true;
								tree.insertNode(decreaseNode1);
							}
						}
					}
				}
				Edge edge2 = null;
				ALTNode decreaseNode2 = null;
				for(int i = 0; i < node2.edges2.size(); i++) {
					edge2 = node2.edges2.get(i);
					decreaseNode2 = hashMap.get(edge2.nodeID);
					if(!decreaseNode2.deleted) {
						if(decreaseNode2.potential2 == 0) {
							long estimate = Long.MIN_VALUE;
							for(int j = 0; j < backwardLandmarks.size(); j++) {
								int l = backwardLandmarks.get(j);
								long val = decreaseNode2.landmarksBackwardDistances.get(l) - sourceNode.landmarksBackwardDistances.get(l);
								if(val > estimate) {
									estimate = val;
								}
							}
						}
						long newKeyLength = node2.pathLength2 + edge2.travelTime + 
								decreaseNode2.potential2;
						if(!decreaseNode2.deleted2 && newKeyLength < decreaseNode2.keyLength2) {
							decreaseNode2.path2 = node2;
							decreaseNode2.pathLength2 = node2.pathLength2 + edge2.travelTime;
							decreaseNode2.keyLength2 = newKeyLength;
							long newMin = decreaseNode2.pathLength+decreaseNode2.pathLength2;
							if(newMin > 0 && newMin < shortest) {
								shortest = decreaseNode2.pathLength+decreaseNode2.pathLength2;
								smallest = decreaseNode2;
							}
							if(decreaseNode2.inserted2) {
								biTree.decreasekey(decreaseNode2, newKeyLength);
							}
							else {
								decreaseNode2.key2 = calcKey(newKeyLength,decreaseNode2.id);
								decreaseNode2.inserted2 = true;
								biTree.insertNode(decreaseNode2);
							}
						}
					}
				}
			}
			
			ArrayList<ALTNode> path = new ArrayList<ALTNode>();
			
			// Found a node on shortest path, follow it
			node = smallest;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (ALTNode) node.path;
			}
			path.add(node);
			node = smallest;
			while(node.id != targetNode.id) {
				path.add(node);
				node = (ALTNode) node.path2;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
		}
		
		return shortest;
	}
	
	/**
	 * Calculates the shortest path (forward and backward) between all nodes and all landmarks.
	 * @param landmarks
	 * @param nodes
	 */
	public ArrayList<Integer> calculateDistancesToLandmarks(ArrayList<ALTNode> landmarks, ArrayList<ALTNode> nodes, HashMap<Long,ALTNode> hashMap) {
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ALTNode node = null;
		// Do a Dijkstra search, and reverse, from each landmark, adding the shortest path to each node
		for(int i = 0; i < landmarks.size(); i++) {
			node = landmarks.get(i);
			computeForward(node, nodes, hashMap, ret, i);
			computeBackward(node, nodes, hashMap, ret, i);
		}
		return ret;
	}
	
	/**
	 * Performs a Dijkstra search from source to all nodes via. the reverse graph, thus computing
	 * the shortest distance from all nodes to source. Adds this to landmarksForwardDistances.
	 * @param source
	 * @param nodes
	 * @param hashMap
	 */
	private void computeForward(ALTNode source, ArrayList<ALTNode> nodes, HashMap<Long,ALTNode> hashMap, ArrayList<Integer> invalid, int number) {
		// Use the reverse edges for this method.
		
		// Reset variables, could have been used previously
		BiRedBlackTree tree = new BiRedBlackTree();
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			// Reset
			node.key2 = Long.MAX_VALUE - i;
			node.colour2 = false;
			node.deleted2 = false;
			node.inserted2 = false;
			node.parent2 = null;
			node.leftChild2 = null;
			node.rightChild2 = null;
			node.path2 = null;
			node.pathLength2 = Long.MAX_VALUE;
			node.landmarksForwardDistances.add(Long.MAX_VALUE); // Add dummy value
			//node.landmarksForwardDistances.add(0L); // Add dummy value
		}
		source.key2 = 0;
		source.pathLength2 = 0;
		tree.insertNode(source);
		int checked = 0;
		while(tree.size > 0) {
			node = (ALTNode) tree.deleteMin();
			node.landmarksForwardDistances.remove(number); // Delete dummy value
			node.landmarksForwardDistances.add(node.pathLength2);
			node.deleted2 = true;
			Edge edge = null;
			ALTNode decreaseNode = null;
			checked++;
			for(int i = 0; i < node.edges2.size(); i++) {
				edge = node.edges2.get(i);
				decreaseNode = hashMap.get(edge.nodeID);
				long newPathLength = node.pathLength2 + edge.travelTime;
				if(!decreaseNode.deleted2 && newPathLength < decreaseNode.pathLength2) {
					decreaseNode.pathLength2 = newPathLength;
					if(decreaseNode.inserted2) {
						tree.decreasekey(decreaseNode, newPathLength);
					}
					else {
						decreaseNode.key2 = calcKey(newPathLength,decreaseNode.id);
						decreaseNode.inserted2 = true;
						tree.insertNode(decreaseNode);
					}
				}
			}
		}
		//if(checked < nodes.size()) {
		//if(checked < invalidateTreshold) {
		if(checked < nodes.size()/2) {
			invalid.add(number);
		} 

	}
	
	/**
	 * Performs a Dijkstra search from source to all nodes, thus computing
	 * the shortest distance from source to all nodes. Adds this to landmarksBackwardDistances.
	 * @param source
	 * @param nodes
	 * @param hashMap
	 */
	private void computeBackward(ALTNode source, ArrayList<ALTNode> nodes, HashMap<Long,ALTNode> hashMap, ArrayList<Integer> invalid, int number) {
		// Use the normal edges for this method.
		
		// Reset variables, could have been used previously
		RedBlackTree tree = new RedBlackTree();
		ALTNode node = null;
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
			node.landmarksBackwardDistances.add(Long.MAX_VALUE); // Dummy value
			//node.landmarksBackwardDistances.add(0L); // Dummy value
		}
		source.key = 0;
		source.pathLength = 0;
		tree.insertNode(source);
		int checked = 0;
		while(tree.size > 0) {
			node = (ALTNode) tree.deleteMin();
			node.landmarksBackwardDistances.remove(number); // Delete dummy
			node.landmarksBackwardDistances.add(node.pathLength);
			node.deleted = true;
			Edge edge = null;
			ALTNode decreaseNode = null;
			checked++;
			for(int i = 0; i < node.edges.size(); i++) {
				edge = node.edges.get(i);
				decreaseNode = hashMap.get(edge.nodeID);
				long newPathLength = node.pathLength + edge.travelTime;
				if(!decreaseNode.deleted && newPathLength < decreaseNode.pathLength) {
					decreaseNode.pathLength = newPathLength;
					if(decreaseNode.inserted) {
						tree.decreaseKey(decreaseNode, newPathLength);
					}
					else {
						decreaseNode.key = calcKey(newPathLength,decreaseNode.id);
						decreaseNode.inserted = true;
						tree.insertNode(decreaseNode);
					}
				}
			}
		}
		//if(checked < nodes.size()) {
		//if(checked < invalidateTreshold) {
		if(checked < nodes.size()/2) {
			invalid.add(number);
		}
	}
	
	// Shift val shift places to the left to make space for ID of 8bil.
	private long calcKey(long newPathLenght, long id) {
		long ret = newPathLenght;
		ret = ret << shift;
		ret = ret+id;
		return ret;
	}
	
	/**
	 * Computes k landmarks chosen by the random landmark method
	 * @param k landmarks to chose
	 * @param nodes ArrayList of ALTNodes in the graph
	 * @return ArrayList of ALTNode denoting the landmarks
	 */
	public ArrayList<ALTNode> computeRandomLandmarks(int k, ArrayList<ALTNode> nodes) {
		ArrayList<ALTNode> ret = new ArrayList<ALTNode>();
		Random random = new Random();
		ALTNode node = null;
		while(ret.size() < k) {
			node = nodes.get(random.nextInt(nodes.size()));
			if(!ret.contains(node) && node.edges.size() >= minEdgesInvalidate && node.edges2.size() >= minEdgesInvalidate) {
				ret.add(node);
			}
		}	
		return ret;
	}
	
	/**
	 * Computes k landmarks chosen by the farthest landmark method
	 * @param k landmarks to chose
	 * @param nodes ArrayList of ALTNodes in the graph
	 * @return ArrayList of ALTNode denoting the landmarks
	 */
	public ArrayList<ALTNode> computeFarthestLandmarks(int k, ArrayList<ALTNode> nodes) {
		ArrayList<ALTNode> ret = new ArrayList<ALTNode>();
		Random random = new Random();
		// Select random source node
		ALTNode sourceNode = nodes.get(random.nextInt(nodes.size()));
		// Find the node furthest away from the source node
		ALTNode node = null;
		long farthest = 0;
		ALTNode farthestNode = null;
		long temp = 0;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			temp = calculateDistance(node, sourceNode);
			if(temp > farthest && node.edges.size() >= minEdgesInvalidate && node.edges2.size() >= minEdgesInvalidate) {
				farthest = temp;
				farthestNode = node;
			}
		}
		// Found the furthest node, now add it to ret
		ret.add(farthestNode);	
		// Recursively find the node furthest away from the other landmarks
		for(int i = 0; i < k-1; i++) {
			node = findFurthestNodeAwayFromSet(ret, nodes);
			ret.add(node);
		}
		
		return ret;
	}
	
	/**
	 * Computes k landmarks chosen by the optimized farthest landmark method
	 * @param k landmarks to chose
	 * @param o the number of optimizations
	 * @param nodes ArrayList of ALTNodes in the graph
	 * @return ArrayList of ALTNode denoting the landmarks
	 */
	public ArrayList<ALTNode> computeOptimizedFarthestLandmarks(int k, int o, ArrayList<ALTNode> nodes) {
		ArrayList<ALTNode> ret = new ArrayList<ALTNode>();
		Random random = new Random();
		// Select random source node
		ALTNode sourceNode = nodes.get(random.nextInt(nodes.size()));
		// Find the node furthest away from the source node
		ALTNode node = null;
		long farthest = 0;
		ALTNode farthestNode = null;
		long temp = 0;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			temp = calculateDistance(node, sourceNode);
			if(temp > farthest && node.edges.size() >= minEdgesInvalidate && node.edges2.size() >= minEdgesInvalidate) {
				farthest = temp;
				farthestNode = node;
			}
		}
		// Found the furthest node, now add it to ret
		ret.add(farthestNode);	
		// Recursively find the node furthest away from the other landmarks
		for(int i = 0; i < k-1; i++) {
			node = findFurthestNodeAwayFromSet(ret, nodes);
			ret.add(node);
		}
		
		// Calculate distances to all other landmarks
		ALTNode landmark = null;
		long worstDist = Long.MAX_VALUE;
		ALTNode worstNode = null;
		for(int i = 0; i < ret.size(); i++) {
			node = ret.get(i);
			temp = 0;
			for(int j = 0; j < ret.size(); j++) {
				landmark = ret.get(j);
				temp += calculateDistance(node, landmark);
			}
			if(temp < worstDist) {
				worstDist = temp;
				worstNode = node;
			}
		}
		
		// Now recursively o times find a better candidate than the worst in ret and replace it.
		for(int i = 0; i < o; i++) {
			landmark = findFurthestNodeAwayFromSet(ret, nodes);
			ret.remove(worstNode);
			ret.add(landmark);
			// Recalculate all distances
			worstDist = Long.MAX_VALUE;
			for(int x = 0; x < ret.size(); x++) {
				node = ret.get(x);
				temp = 0;
				for(int y = 0; y < ret.size(); y++) {
					landmark = ret.get(y);
					temp += calculateDistance(node, landmark);
				}
				if(temp < worstDist) {
					worstDist = temp;
					worstNode = node;
				}
			}
		}
		
		return ret;
	}
	
	private ALTNode findFurthestNodeAwayFromSet(ArrayList<ALTNode> landmarks, ArrayList<ALTNode> nodes) {
		
		ALTNode node = nodes.get(0);
		ALTNode ret = node;
		ALTNode landmark = null;
		long total = 0;
		long farthest = 0;
		
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			total = 0;
			for(int j = 0; j < landmarks.size(); j++) {
				landmark = landmarks.get(j);
				total += calculateDistance(node, landmark);
			}
			if(total > farthest && !landmarks.contains(node) && node.edges.size() >= minEdgesInvalidate && node.edges2.size() >= minEdgesInvalidate) {
				farthest = total;
				ret = node;
			}
		}
		
		return ret;
	}
	
	private long calculateDistance(ALTNode node1, ALTNode node2) {
		
		double lat1 = node1.lat;
		double lon1 = node1.lon;
		double lat2 = node2.lat;
		double lon2 = node2.lon;
	
		double piDiv180 = Math.PI / 180;
		double lat1r = lat1 * piDiv180;
		//double lon1r = lon1 * piDiv180;
		double lat2r = lat2 * piDiv180;
		//double lon2r = lon2 * piDiv180;
		double deltaLatr = (lat2-lat1) * piDiv180;
		double deltaLonr = (lon2-lon1) * piDiv180;
		
		double expression1 = Math.sin(deltaLatr/2) * Math.sin(deltaLatr/2);
		double expression2 = Math.sin(deltaLonr/2) * Math.sin(deltaLonr/2);
		double expression3 = Math.cos(lat1r) * Math.cos(lat2r) * expression2;
		double expression4 = Math.sqrt(expression1 + expression3);
		// 2x the earths radius times arcsin of the above
		double distance = 12742 * Math.asin(expression4);
		// Different way of writing the above
		//double distance = 12742 * Math.atan2(expression4, Math.sqrt(1-expression1-expression3));
		
		return Math.round(distance*100000); // Cm because Math.round is long
		
	}
	
}
