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
	public boolean write = false;
	
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
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Build up hashmap
		/*HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		// Fill in hashmap
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			hashMap.put(node.id, node);
		}*/
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
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		long shortest = Long.MAX_VALUE; // For use below
		ALTNode smallest = null;
		
		BufferedWriter out1 = null;
		BufferedWriter out2 = null;
		double cmMsec = 130*0.0277777778;
		Double potCon = (double) 0.95;		
		
		nodesChecked = 0;
		
		for(int r = 0; r < runs; r++) {
			
			System.out.println("Run "+(r+1));
			
			out1 = new BufferedWriter(new FileWriter("ALT1.txt"));
			out2 = new BufferedWriter(new FileWriter("ALT2.txt"));
			
			preprocessStart = System.currentTimeMillis();
			
			shortest = Long.MAX_VALUE;
			smallest = null;
			
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				// Reset
				node.landmarksForwardDistances = new ArrayList<Long>();
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
			
			//ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
			ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarksUnidirectional(landmarks, nodes,hashMap);
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
			
			forwardLandmarks.addAll(backwardLandmarks);
			
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
			
			Double sourcePotential = Double.MIN_VALUE;
			for(int x = 0; x < forwardLandmarks.size(); x++) {
				int y = forwardLandmarks.get(x);
				long distV = sourceNode.landmarksForwardDistances.get(y);
				long distW = targetNode.landmarksForwardDistances.get(y);
				Double backward = ((distW-distV)/cmMsec);
				distV = sourceNode.landmarksForwardDistances.get(y);
				distW = targetNode.landmarksForwardDistances.get(y);
				Double forward = ((distV-distW)/cmMsec);
				Double potential = ((forward-backward)/2);
				if(potential > sourcePotential) {
					sourcePotential = potential;
				}
			}
			/*for(int x = 0; x < forwardLandmarks.size(); x++) {
				int y = forwardLandmarks.get(x);
				long distV = sourceNode.landmarksForwardDistances.get(y);
				long distW = targetNode.landmarksForwardDistances.get(y);
				Double backward = ((distW-distV)/cmMsec);
				distV = sourceNode.landmarksBackwardDistances.get(y);
				distW = targetNode.landmarksBackwardDistances.get(y);
				Double forward = ((distV-distW)/cmMsec);
				Double potential = ((forward-backward)/2);
				if(potential > sourcePotential) {
					sourcePotential = potential;
				}
			}*/

			sourceNode.potential = Math.round(sourcePotential);
			
			double targetPotential = Double.MIN_VALUE;
			for(int x = 0; x < backwardLandmarks.size(); x++) {
				int y = backwardLandmarks.get(x);
				long distV = targetNode.landmarksForwardDistances.get(y);
				long distW = sourceNode.landmarksForwardDistances.get(y);
				double backward = ((distW-distV)/cmMsec);
				distV = targetNode.landmarksForwardDistances.get(y);
				distW = sourceNode.landmarksForwardDistances.get(y);
				double forward = ((distV-distW)/cmMsec);
				double potential = ((backward-forward)/2);
				if(potential > targetPotential) {
					targetPotential = potential;
				}
			}
			/*for(int x = 0; x < backwardLandmarks.size(); x++) {
				int y = backwardLandmarks.get(x);
				long distV = targetNode.landmarksForwardDistances.get(y);
				long distW = sourceNode.landmarksForwardDistances.get(y);
				double backward = ((distW-distV)/cmMsec);
				distV = targetNode.landmarksBackwardDistances.get(y);
				distW = sourceNode.landmarksBackwardDistances.get(y);
				double forward = ((distV-distW)/cmMsec);
				double potential = ((backward-forward)/2);
				if(potential > targetPotential) {
					targetPotential = potential;
				}
			}*/

			targetNode.potential2 = Math.round(targetPotential);
			
			//System.out.println("Source potential = "+sourceNode.potential);
			//System.out.println("Target potential = "+targetNode.potential2);
			
			// Bidirectional Dijkstra
			ALTNode node1 = sourceNode;
			ALTNode node2 = targetNode;
			//while(true) {
			while(node1.id != targetNode.id && node2.id != sourceNode.id) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(write) {
					out1.write(node1.id+" "+node1.lat+" "+node1.lon);
					out1.newLine();
				}
				/*if(node1.deleted2) {
					System.out.println("Break1");
					break;
				}*/
				/*if(node1.potential == 0) {
					long forward = 0;
					long backward = 0;
					for(int i = 0; i < forwardLandmarks.size(); i++) {
						int j = forwardLandmarks.get(i);
						if(forward < node1.landmarksForwardDistances.get(j) - targetNode.landmarksForwardDistances.get(j)) {
							forward = node1.landmarksForwardDistances.get(j)  - targetNode.landmarksForwardDistances.get(j);
						}
						if(backward < sourceNode.landmarksForwardDistances.get(j) - node1.landmarksForwardDistances.get(j)) {
							backward =  sourceNode.landmarksForwardDistances.get(j) - node1.landmarksForwardDistances.get(j);
						}
					}
					node1.potential = (forward-backward)/2;
				}*/
				node2 = (ALTNode) biTree.deleteMin();
				nodesChecked++;
				if(write) {
					out2.write(node2.id+" "+node2.lat+" "+node2.lon);
					out2.newLine();
				}
				/*if(node2.deleted) {
					System.out.println("Break2");
					break;
				}*/
				/*if(node2.potential2 == 0) {
					long forward = 0;
					long backward = 0;
					for(int i = 0; i < backwardLandmarks.size(); i++) {
						int j = backwardLandmarks.get(i);
						if(forward < node2.landmarksBackwardDistances.get(j) - targetNode.landmarksBackwardDistances.get(j)) {
							forward = node2.landmarksBackwardDistances.get(j)  - targetNode.landmarksBackwardDistances.get(j);
						}
						if(backward < sourceNode.landmarksBackwardDistances.get(j) - node2.landmarksBackwardDistances.get(j)) {
							backward =  sourceNode.landmarksBackwardDistances.get(j) - node2.landmarksBackwardDistances.get(j);
						}
					}
					node2.potential2 = (backward-forward)/2;
				}*/
				if(node1.keyLength + node2.keyLength2 > shortest && node1.keyLength + node2.keyLength2 > 0) {
					//System.out.println("Break3");
					break;
				}
				/*if(node1.keyLength > shortest && node2.keyLength2 > shortest) {
					System.out.println("Break4");
					break;
				}*/
				node1.deleted = true;
				node2.deleted2 = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					/*if(decreaseNode1.potential == 0) {
						long forward = 0;
						long backward = 0;
						for(int x = 0; x < forwardLandmarks.size(); x++) {
							int y = forwardLandmarks.get(x);
							if(forward < decreaseNode1.landmarksForwardDistances.get(y) - targetNode.landmarksForwardDistances.get(y)) {
								forward = decreaseNode1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y);
							}
							if(backward < sourceNode.landmarksForwardDistances.get(y) - decreaseNode1.landmarksForwardDistances.get(y)) {
								backward =  sourceNode.landmarksForwardDistances.get(y) - decreaseNode1.landmarksForwardDistances.get(y);
							}
						}
						decreaseNode1.potential = (forward-backward)/2;
					}
					long newKeyLength = node1.keyLength + edge1.travelTime + 
							decreaseNode1.potential - node1.potential;*/
					
					// Choose potential for W that maximizes the discount to the edge (V,W)
					long potential = Long.MIN_VALUE;
					long tempPotential = 0;
					long distWb = 0;
					long distWf = 0;
					long distTarget = 0;
					long distSource = 0;
					double forward = 0;
					double backward = 0;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distWb = decreaseNode1.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						forward = ((distWb - distTarget) / cmMsec);						
						/*distWf = decreaseNode1.landmarksBackwardDistances.get(y);
						distSource = sourceNode.landmarksBackwardDistances.get(y);*/
						distWf = decreaseNode1.landmarksForwardDistances.get(y);
						distSource = sourceNode.landmarksForwardDistances.get(y);
						backward = ((distSource - distWf) / cmMsec);
						tempPotential = Math.round((forward-backward)/2);
						/*if(distWb == Long.MAX_VALUE) {
							tempPotential = forward;
							System.out.println("---B "+decreaseNode1.id);
						}
						else if(distWf == Long.MAX_VALUE) {
							tempPotential = backward;
							System.out.println("---F "+decreaseNode1.id);
						}*/
						if(potential < tempPotential - node1.potential) {
							potential = tempPotential - node1.potential;
							decreaseNode1.potential = tempPotential;
						}
					}
					
					/*for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distWb = decreaseNode1.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						forward = ((distWb - distTarget) / cmMsec);						
						distWf = decreaseNode1.landmarksBackwardDistances.get(y);
						distSource = sourceNode.landmarksBackwardDistances.get(y);
						backward = ((distSource - distWf) / cmMsec);
						tempPotential = Math.round((forward-backward)/2);
						if(potential < tempPotential - node1.potential) {
							potential = tempPotential - node1.potential;
							decreaseNode1.potential = tempPotential;
						}
					}*/
					
					/*if(edge1.travelTime + potential < 0) {
						System.out.println("!!! "+edge1.travelTime+" "+potential);
						System.out.println(distWb+" "+distWf+" "+distTarget);
						System.out.println(forward+" "+backward);
						System.out.println(tempPotential+" "+node1.potential);
					}*/
					
					// +1 to counteract rounding errors
					long newKeyLength = node1.keyLength + edge1.travelTime + potential;
					
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
						decreaseNode1.keyLength = newKeyLength;
						long newMin = decreaseNode1.keyLength+decreaseNode1.keyLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode1.keyLength+decreaseNode1.keyLength2;
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
					/*if(decreaseNode2.potential2 == 0) {
						long forward = 0;
						long backward = 0;
						for(int x = 0; x < backwardLandmarks.size(); x++) {
							int y = backwardLandmarks.get(x);
							if(forward < decreaseNode2.landmarksBackwardDistances.get(y) - targetNode.landmarksBackwardDistances.get(y)) {
								forward = decreaseNode2.landmarksBackwardDistances.get(y)  - targetNode.landmarksBackwardDistances.get(y);
							}
							if(backward < sourceNode.landmarksBackwardDistances.get(y) - decreaseNode2.landmarksBackwardDistances.get(y)) {
								backward =  sourceNode.landmarksBackwardDistances.get(y) - decreaseNode2.landmarksBackwardDistances.get(y);
							}
						}
						decreaseNode2.potential2 = (backward-forward)/2;
					}
					long newKeyLength = node2.keyLength2 + edge2.travelTime + 
							decreaseNode2.potential2 - node2.potential2;*/
					
					
					long potential = Long.MIN_VALUE;
					long tempPotential = 0;
					long distWb = 0;
					long distWf = 0;
					long distTarget = 0;
					long distSource = 0;
					double forward = 0;
					double backward = 0;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distWb = decreaseNode2.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						forward = ((distWb - distTarget) / cmMsec);						
						distWf = decreaseNode2.landmarksForwardDistances.get(y);
						distSource = sourceNode.landmarksForwardDistances.get(y);
						backward = ((distSource-distWf) / cmMsec);
						tempPotential = Math.round((backward-forward)/2);
						if(potential < tempPotential - node2.potential2) {
							potential = tempPotential - node2.potential2;
							decreaseNode2.potential2 = tempPotential;
						}
					}
					/*for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distWb = decreaseNode2.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						forward = ((distWb - distTarget) / cmMsec);						
						distWf = decreaseNode2.landmarksBackwardDistances.get(y);
						distSource = sourceNode.landmarksBackwardDistances.get(y);
						backward = ((distSource-distWf) / cmMsec);
						tempPotential = Math.round((backward-forward)/2);
						if(potential < tempPotential - node2.potential2) {
							potential = tempPotential - node2.potential2;
							decreaseNode2.potential2 = tempPotential;
						}
					}*/
					
					long newKeyLength = node2.keyLength2 + edge2.travelTime + potential;
					
					if(!decreaseNode2.deleted2 && newKeyLength < decreaseNode2.keyLength2) {
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = node2.pathLength2 + edge2.travelTime;
						decreaseNode2.keyLength2 = newKeyLength;
						long newMin = decreaseNode2.keyLength+decreaseNode2.keyLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode2.keyLength+decreaseNode2.keyLength2;
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
			
			//System.out.println(smallest.pathLength+" "+smallest.pathLength2 + " "+shortest);
			//System.out.println(targetNode.pathLength);
			
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
			
			out1.write("end");
			out1.flush();
			out1.close();
			out2.write("end");
			out2.flush();
			out2.close();
		}
		
		nodesChecked = nodesChecked / runs;
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
		
		return (smallest.pathLength + smallest.pathLength2);
		//return shortest;
	}
	
	public long ALTBidirectionalSearchBackup(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
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
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Build up hashmap
		/*HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		// Fill in hashmap
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			hashMap.put(node.id, node);
		}*/
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
		
		//ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
		ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarksUnidirectional(landmarks, nodes,hashMap);
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
		
		BufferedWriter out1 = null;
		BufferedWriter out2 = null;
		double cmMsec = 130*0.0277777778;
		Double potCon = (double) 0.95;
		
		// Experiment
		forwardLandmarks.addAll(backwardLandmarks);
		
		for(int r = 0; r < runs; r++) {
			
			System.out.println("Run "+(r+1));
			
			out1 = new BufferedWriter(new FileWriter("ALT1.txt"));
			out2 = new BufferedWriter(new FileWriter("ALT2.txt"));
			
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
			//preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			RedBlackTree tree = new RedBlackTree();
			BiRedBlackTree biTree = new BiRedBlackTree();
			
			// Insert
			tree.insertNode(sourceNode);
			sourceNode.inserted = true;
			biTree.insertNode(targetNode);
			targetNode.inserted2 = true;
			
			Double sourcePotential = Double.MIN_VALUE;
			for(int x = 0; x < forwardLandmarks.size(); x++) {
				int y = forwardLandmarks.get(x);
				long distV = sourceNode.landmarksForwardDistances.get(y);
				long distW = targetNode.landmarksForwardDistances.get(y);
				Double backward = ((distW-distV)/cmMsec);
				distV = sourceNode.landmarksForwardDistances.get(y);
				distW = targetNode.landmarksForwardDistances.get(y);
				Double forward = ((distV-distW)/cmMsec);
				Double potential = ((forward-backward)/2);
				if(potential > sourcePotential) {
					sourcePotential = potential;
				}
			}
			/*for(int x = 0; x < forwardLandmarks.size(); x++) {
				int y = forwardLandmarks.get(x);
				long distV = sourceNode.landmarksForwardDistances.get(y);
				long distW = targetNode.landmarksForwardDistances.get(y);
				Double backward = ((distW-distV)/cmMsec);
				distV = sourceNode.landmarksBackwardDistances.get(y);
				distW = targetNode.landmarksBackwardDistances.get(y);
				Double forward = ((distV-distW)/cmMsec);
				Double potential = ((forward-backward)/2);
				if(potential > sourcePotential) {
					sourcePotential = potential;
				}
			}*/

			sourceNode.potential = Math.round(sourcePotential);
			
			double targetPotential = Double.MIN_VALUE;
			for(int x = 0; x < backwardLandmarks.size(); x++) {
				int y = backwardLandmarks.get(x);
				long distV = targetNode.landmarksForwardDistances.get(y);
				long distW = sourceNode.landmarksForwardDistances.get(y);
				double backward = ((distW-distV)/cmMsec);
				distV = targetNode.landmarksForwardDistances.get(y);
				distW = sourceNode.landmarksForwardDistances.get(y);
				double forward = ((distV-distW)/cmMsec);
				double potential = ((backward-forward)/2);
				if(potential > targetPotential) {
					targetPotential = potential;
				}
			}
			/*for(int x = 0; x < backwardLandmarks.size(); x++) {
				int y = backwardLandmarks.get(x);
				long distV = targetNode.landmarksForwardDistances.get(y);
				long distW = sourceNode.landmarksForwardDistances.get(y);
				double backward = ((distW-distV)/cmMsec);
				distV = targetNode.landmarksBackwardDistances.get(y);
				distW = sourceNode.landmarksBackwardDistances.get(y);
				double forward = ((distV-distW)/cmMsec);
				double potential = ((backward-forward)/2);
				if(potential > targetPotential) {
					targetPotential = potential;
				}
			}*/

			targetNode.potential2 = Math.round(targetPotential);
			
			//System.out.println("Source potential = "+sourceNode.potential);
			//System.out.println("Target potential = "+targetNode.potential2);
			
			// Bidirectional Dijkstra
			ALTNode node1 = sourceNode;
			ALTNode node2 = targetNode;
			//while(true) {
			while(node1.id != targetNode.id && node2.id != sourceNode.id) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(write) {
					out1.write(node1.id+" "+node1.lat+" "+node1.lon);
					out1.newLine();
				}
				/*if(node1.deleted2) {
					System.out.println("Break1");
					break;
				}*/
				/*if(node1.potential == 0) {
					long forward = 0;
					long backward = 0;
					for(int i = 0; i < forwardLandmarks.size(); i++) {
						int j = forwardLandmarks.get(i);
						if(forward < node1.landmarksForwardDistances.get(j) - targetNode.landmarksForwardDistances.get(j)) {
							forward = node1.landmarksForwardDistances.get(j)  - targetNode.landmarksForwardDistances.get(j);
						}
						if(backward < sourceNode.landmarksForwardDistances.get(j) - node1.landmarksForwardDistances.get(j)) {
							backward =  sourceNode.landmarksForwardDistances.get(j) - node1.landmarksForwardDistances.get(j);
						}
					}
					node1.potential = (forward-backward)/2;
				}*/
				node2 = (ALTNode) biTree.deleteMin();
				nodesChecked++;
				if(write) {
					out2.write(node2.id+" "+node2.lat+" "+node2.lon);
					out2.newLine();
				}
				/*if(node2.deleted) {
					System.out.println("Break2");
					break;
				}*/
				/*if(node2.potential2 == 0) {
					long forward = 0;
					long backward = 0;
					for(int i = 0; i < backwardLandmarks.size(); i++) {
						int j = backwardLandmarks.get(i);
						if(forward < node2.landmarksBackwardDistances.get(j) - targetNode.landmarksBackwardDistances.get(j)) {
							forward = node2.landmarksBackwardDistances.get(j)  - targetNode.landmarksBackwardDistances.get(j);
						}
						if(backward < sourceNode.landmarksBackwardDistances.get(j) - node2.landmarksBackwardDistances.get(j)) {
							backward =  sourceNode.landmarksBackwardDistances.get(j) - node2.landmarksBackwardDistances.get(j);
						}
					}
					node2.potential2 = (backward-forward)/2;
				}*/
				if(node1.keyLength + node2.keyLength2 > shortest && node1.keyLength + node2.keyLength2 > 0) {
					//System.out.println("Break3");
					break;
				}
				/*if(node1.keyLength > shortest && node2.keyLength2 > shortest) {
					System.out.println("Break4");
					break;
				}*/
				node1.deleted = true;
				node2.deleted2 = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					/*if(decreaseNode1.potential == 0) {
						long forward = 0;
						long backward = 0;
						for(int x = 0; x < forwardLandmarks.size(); x++) {
							int y = forwardLandmarks.get(x);
							if(forward < decreaseNode1.landmarksForwardDistances.get(y) - targetNode.landmarksForwardDistances.get(y)) {
								forward = decreaseNode1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y);
							}
							if(backward < sourceNode.landmarksForwardDistances.get(y) - decreaseNode1.landmarksForwardDistances.get(y)) {
								backward =  sourceNode.landmarksForwardDistances.get(y) - decreaseNode1.landmarksForwardDistances.get(y);
							}
						}
						decreaseNode1.potential = (forward-backward)/2;
					}
					long newKeyLength = node1.keyLength + edge1.travelTime + 
							decreaseNode1.potential - node1.potential;*/
					
					// Choose potential for W that maximizes the discount to the edge (V,W)
					long potential = Long.MIN_VALUE;
					long tempPotential = 0;
					long distWb = 0;
					long distWf = 0;
					long distTarget = 0;
					long distSource = 0;
					double forward = 0;
					double backward = 0;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distWb = decreaseNode1.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						forward = ((distWb - distTarget) / cmMsec);						
						/*distWf = decreaseNode1.landmarksBackwardDistances.get(y);
						distSource = sourceNode.landmarksBackwardDistances.get(y);*/
						distWf = decreaseNode1.landmarksForwardDistances.get(y);
						distSource = sourceNode.landmarksForwardDistances.get(y);
						backward = ((distSource - distWf) / cmMsec);
						tempPotential = Math.round((forward-backward)/2);
						/*if(distWb == Long.MAX_VALUE) {
							tempPotential = forward;
							System.out.println("---B "+decreaseNode1.id);
						}
						else if(distWf == Long.MAX_VALUE) {
							tempPotential = backward;
							System.out.println("---F "+decreaseNode1.id);
						}*/
						if(potential < tempPotential - node1.potential) {
							potential = tempPotential - node1.potential;
							decreaseNode1.potential = tempPotential;
						}
					}
					
					/*for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distWb = decreaseNode1.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						forward = ((distWb - distTarget) / cmMsec);						
						distWf = decreaseNode1.landmarksBackwardDistances.get(y);
						distSource = sourceNode.landmarksBackwardDistances.get(y);
						backward = ((distSource - distWf) / cmMsec);
						tempPotential = Math.round((forward-backward)/2);
						if(potential < tempPotential - node1.potential) {
							potential = tempPotential - node1.potential;
							decreaseNode1.potential = tempPotential;
						}
					}*/
					
					/*if(edge1.travelTime + potential < 0) {
						System.out.println("!!! "+edge1.travelTime+" "+potential);
						System.out.println(distWb+" "+distWf+" "+distTarget);
						System.out.println(forward+" "+backward);
						System.out.println(tempPotential+" "+node1.potential);
					}*/
					
					// +1 to counteract rounding errors
					long newKeyLength = node1.keyLength + edge1.travelTime + potential;
					
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
						decreaseNode1.keyLength = newKeyLength;
						long newMin = decreaseNode1.keyLength+decreaseNode1.keyLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode1.keyLength+decreaseNode1.keyLength2;
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
					/*if(decreaseNode2.potential2 == 0) {
						long forward = 0;
						long backward = 0;
						for(int x = 0; x < backwardLandmarks.size(); x++) {
							int y = backwardLandmarks.get(x);
							if(forward < decreaseNode2.landmarksBackwardDistances.get(y) - targetNode.landmarksBackwardDistances.get(y)) {
								forward = decreaseNode2.landmarksBackwardDistances.get(y)  - targetNode.landmarksBackwardDistances.get(y);
							}
							if(backward < sourceNode.landmarksBackwardDistances.get(y) - decreaseNode2.landmarksBackwardDistances.get(y)) {
								backward =  sourceNode.landmarksBackwardDistances.get(y) - decreaseNode2.landmarksBackwardDistances.get(y);
							}
						}
						decreaseNode2.potential2 = (backward-forward)/2;
					}
					long newKeyLength = node2.keyLength2 + edge2.travelTime + 
							decreaseNode2.potential2 - node2.potential2;*/
					
					
					long potential = Long.MIN_VALUE;
					long tempPotential = 0;
					long distWb = 0;
					long distWf = 0;
					long distTarget = 0;
					long distSource = 0;
					double forward = 0;
					double backward = 0;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distWb = decreaseNode2.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						forward = ((distWb - distTarget) / cmMsec);						
						distWf = decreaseNode2.landmarksForwardDistances.get(y);
						distSource = sourceNode.landmarksForwardDistances.get(y);
						backward = ((distSource-distWf) / cmMsec);
						tempPotential = Math.round((backward-forward)/2);
						if(potential < tempPotential - node2.potential2) {
							potential = tempPotential - node2.potential2;
							decreaseNode2.potential2 = tempPotential;
						}
					}
					/*for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distWb = decreaseNode2.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						forward = ((distWb - distTarget) / cmMsec);						
						distWf = decreaseNode2.landmarksBackwardDistances.get(y);
						distSource = sourceNode.landmarksBackwardDistances.get(y);
						backward = ((distSource-distWf) / cmMsec);
						tempPotential = Math.round((backward-forward)/2);
						if(potential < tempPotential - node2.potential2) {
							potential = tempPotential - node2.potential2;
							decreaseNode2.potential2 = tempPotential;
						}
					}*/
					
					long newKeyLength = node2.keyLength2 + edge2.travelTime + potential;
					
					if(!decreaseNode2.deleted2 && newKeyLength < decreaseNode2.keyLength2) {
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = node2.pathLength2 + edge2.travelTime;
						decreaseNode2.keyLength2 = newKeyLength;
						long newMin = decreaseNode2.keyLength+decreaseNode2.keyLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode2.keyLength+decreaseNode2.keyLength2;
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
			
			//System.out.println(smallest.pathLength+" "+smallest.pathLength2 + " "+shortest);
			//System.out.println(targetNode.pathLength);
			
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
			
			out1.write("end");
			out1.flush();
			out1.close();
			out2.write("end");
			out2.flush();
			out2.close();
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
		
		return (smallest.pathLength + smallest.pathLength2);
		//return shortest;
	}
	
	/**
	 * This method uses pre-selected landmarks to do a "perfect" search.
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
	public long ALTPerfectBidirectionalSearch(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
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
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		
		ArrayList<ALTNode> landmarks = new ArrayList<ALTNode>();
		long landmark1 = 7204186L; // Trafick light near CPH Airport
		long landmark2 = 250680502L; // Skagen
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
			else if(node.id == landmark1) {
				landmarks.add(node);
			}
			else if(node.id == landmark2) {
				landmarks.add(node);
			}
		}
		
		
		
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
		
		//System.out.println(landmarks.get(forwardLandmarks.get(0)).id);
		//System.out.println(landmarks.get(backwardLandmarks.get(0)).id);
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		long shortest = Long.MAX_VALUE; // For use below
		ALTNode smallest = null;
		
		BufferedWriter out1 = null;
		BufferedWriter out2 = null;
		
		
		for(int r = 0; r < runs; r++) {
			
			System.out.println("Run "+(r+1));
			
			out1 = new BufferedWriter(new FileWriter("ALT1.txt"));
			out2 = new BufferedWriter(new FileWriter("ALT2.txt"));
			
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
			//preprocessTotal += (preprocessStop-preprocessStart);
			
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
				if(write) {
					out1.write(node1.id+" "+node1.lat+" "+node1.lon);
					out1.newLine();
				}
				if(node1.deleted2) {
					break;
				}
				if(node1.potential == 0) {
					long forward = 0;
					long backward = 0;
					for(int i = 0; i < forwardLandmarks.size(); i++) {
						int j = forwardLandmarks.get(i);
						if(forward < node1.landmarksForwardDistances.get(j) - targetNode.landmarksForwardDistances.get(j)) {
							forward = node1.landmarksForwardDistances.get(j)  - targetNode.landmarksForwardDistances.get(j);
						}
						if(backward < sourceNode.landmarksForwardDistances.get(j) - node1.landmarksForwardDistances.get(j)) {
							backward =  sourceNode.landmarksForwardDistances.get(j) - node1.landmarksForwardDistances.get(j);
						}
					}
					node1.potential = (forward-backward)/2;
				}
				node2 = (ALTNode) biTree.deleteMin();
				nodesChecked++;
				if(write) {
					out2.write(node2.id+" "+node2.lat+" "+node2.lon);
					out2.newLine();
				}
				if(node2.deleted) {
					break;
				}
				if(node2.potential2 == 0) {
					long forward = 0;
					long backward = 0;
					for(int i = 0; i < backwardLandmarks.size(); i++) {
						int j = backwardLandmarks.get(i);
						if(forward < node2.landmarksBackwardDistances.get(j) - targetNode.landmarksBackwardDistances.get(j)) {
							forward = node2.landmarksBackwardDistances.get(j)  - targetNode.landmarksBackwardDistances.get(j);
						}
						if(backward < sourceNode.landmarksBackwardDistances.get(j) - node2.landmarksBackwardDistances.get(j)) {
							backward =  sourceNode.landmarksBackwardDistances.get(j) - node2.landmarksBackwardDistances.get(j);
						}
					}
					node2.potential2 = (backward-forward)/2;
				}
				if(node1.keyLength + node2.keyLength2 >= shortest) {
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
						for(int x = 0; x < forwardLandmarks.size(); x++) {
							int y = forwardLandmarks.get(x);
							if(forward < decreaseNode1.landmarksForwardDistances.get(y) - targetNode.landmarksForwardDistances.get(y)) {
								forward = decreaseNode1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y);
							}
							if(backward < sourceNode.landmarksForwardDistances.get(y) - decreaseNode1.landmarksForwardDistances.get(y)) {
								backward =  sourceNode.landmarksForwardDistances.get(y) - decreaseNode1.landmarksForwardDistances.get(y);
							}
						}
						decreaseNode1.potential = (forward-backward)/2;
					}
					long newKeyLength = node1.keyLength + edge1.travelTime + 
							decreaseNode1.potential - node1.potential;
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
						decreaseNode1.keyLength = newKeyLength;
						long newMin = decreaseNode1.keyLength+decreaseNode1.keyLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode1.keyLength+decreaseNode1.keyLength2;
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
						for(int x = 0; x < backwardLandmarks.size(); x++) {
							int y = backwardLandmarks.get(x);
							if(forward < decreaseNode2.landmarksBackwardDistances.get(y) - targetNode.landmarksBackwardDistances.get(y)) {
								forward = decreaseNode2.landmarksBackwardDistances.get(y)  - targetNode.landmarksBackwardDistances.get(y);
							}
							if(backward < sourceNode.landmarksBackwardDistances.get(y) - decreaseNode2.landmarksBackwardDistances.get(y)) {
								backward =  sourceNode.landmarksBackwardDistances.get(y) - decreaseNode2.landmarksBackwardDistances.get(y);
							}
						}
						decreaseNode2.potential2 = (backward-forward)/2;
					}
					long newKeyLength = node2.keyLength2 + edge2.travelTime + 
							decreaseNode2.potential2 - node2.potential2;
					if(!decreaseNode2.deleted2 && newKeyLength < decreaseNode2.keyLength2) {
						decreaseNode2.path2 = node2;
						decreaseNode2.pathLength2 = node2.pathLength2 + edge2.travelTime;
						decreaseNode2.keyLength2 = newKeyLength;
						long newMin = decreaseNode2.keyLength+decreaseNode2.keyLength2;
						if(newMin > 0 && newMin < shortest) {
							shortest = decreaseNode2.keyLength+decreaseNode2.keyLength2;
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
			
			out1.write("end");
			out1.flush();
			out1.close();
			out2.write("end");
			out2.flush();
			out2.close();
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
		
		return (smallest.pathLength + smallest.pathLength2);
		//return shortest;
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
	public long ALTBidirectionalSearchFinalBackup(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
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
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Build up hashmap
		/*HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		// Fill in hashmap
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			hashMap.put(node.id, node);
		}*/
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
			
			System.out.println("Run "+(r+1));
			
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
			//preprocessTotal += (preprocessStop-preprocessStart);
			
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
					for(int i = 0; i < forwardLandmarks.size(); i++) {
						int j = forwardLandmarks.get(i);
						if(forward < node1.landmarksForwardDistances.get(j)) {
							forward = node1.landmarksForwardDistances.get(j);
						}
					}
					for(int i = 0; i < backwardLandmarks.size(); i++) {
						int j = backwardLandmarks.get(i);
						if(backward < node1.landmarksBackwardDistances.get(j)) {
							backward = node1.landmarksBackwardDistances.get(j);
						}
					}
					/*for(int i = 0; i < node1.landmarksForwardDistances.size(); i++) {
						if(forward < node1.landmarksForwardDistances.get(i)) {
							forward = node1.landmarksForwardDistances.get(i);
						}
					}
					for(int i = 0; i < node1.landmarksBackwardDistances.size(); i++) {
						if(backward < node1.landmarksBackwardDistances.get(i)) {
							backward = node1.landmarksBackwardDistances.get(i);
						}
					}*/
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
					for(int i = 0; i < forwardLandmarks.size(); i++) {
						int j = forwardLandmarks.get(i);
						if(forward < node2.landmarksForwardDistances.get(j)) {
							forward = node2.landmarksForwardDistances.get(j);
						}
					}
					for(int i = 0; i < backwardLandmarks.size(); i++) {
						int j = backwardLandmarks.get(i);
						if(backward < node2.landmarksBackwardDistances.get(j)) {
							backward = node2.landmarksBackwardDistances.get(j);
						}
					}
					/*for(int i = 0; i < node2.landmarksForwardDistances.size(); i++) {
						if(forward < node2.landmarksForwardDistances.get(i)) {
							forward = node2.landmarksForwardDistances.get(i);
						}
					}
					for(int i = 0; i < node2.landmarksBackwardDistances.size(); i++) {
						if(backward < node2.landmarksBackwardDistances.get(i)) {
							backward = node2.landmarksBackwardDistances.get(i);
						}
					}*/
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
						for(int x = 0; x < forwardLandmarks.size(); x++) {
							int y = forwardLandmarks.get(x);
							if(forward < decreaseNode1.landmarksForwardDistances.get(y)) {
								forward = decreaseNode1.landmarksForwardDistances.get(y);
							}
						}
						for(int x = 0; x < backwardLandmarks.size(); x++) {
							int y = backwardLandmarks.get(x);
							if(backward < decreaseNode1.landmarksBackwardDistances.get(y)) {
								backward = decreaseNode1.landmarksBackwardDistances.get(y);
							}
						}
						/*for(int j = 0; j < decreaseNode1.landmarksForwardDistances.size(); j++) {
							if(forward < decreaseNode1.landmarksForwardDistances.get(j)) {
								forward = decreaseNode1.landmarksForwardDistances.get(j);
							}
						}
						for(int j = 0; j < decreaseNode1.landmarksBackwardDistances.size(); j++) {
							if(backward < decreaseNode1.landmarksBackwardDistances.get(j)) {
								backward = decreaseNode1.landmarksBackwardDistances.get(j);
							}
						}*/
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
						for(int x = 0; x < forwardLandmarks.size(); x++) {
							int y = forwardLandmarks.get(x);
							if(forward < decreaseNode2.landmarksForwardDistances.get(y)) {
								forward = decreaseNode2.landmarksForwardDistances.get(y);
							}
						}
						for(int x = 0; x < backwardLandmarks.size(); x++) {
							int y = backwardLandmarks.get(x);
							if(backward < decreaseNode2.landmarksBackwardDistances.get(y)) {
								backward = decreaseNode2.landmarksBackwardDistances.get(y);
							}
						}
						/*for(int j = 0; j < decreaseNode2.landmarksForwardDistances.size(); j++) {
							if(forward < decreaseNode2.landmarksForwardDistances.get(j)) {
								forward = decreaseNode2.landmarksForwardDistances.get(j);
							}
						}
						for(int j = 0; j < decreaseNode2.landmarksBackwardDistances.size(); j++) {
							if(backward < decreaseNode2.landmarksBackwardDistances.get(j)) {
								backward = decreaseNode2.landmarksBackwardDistances.get(j);
							}
						}*/
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
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
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
		
		ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarksTravelTime(landmarks, nodes,hashMap);
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
		
		BufferedWriter out1 = null;
		BufferedWriter out2 = null;
		
		for(int r = 0; r < runs; r++) {
			
			System.out.println("Run "+(r+1));
			
			out1 = new BufferedWriter(new FileWriter("ALT1.txt"));
			out2 = new BufferedWriter(new FileWriter("ALT2.txt"));
			
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
			//preprocessTotal += (preprocessStop-preprocessStart);
			
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
				if(write) {
					out1.write(node1.id+" "+node1.lat+" "+node1.lon);
					out1.newLine();
				}
				if(node1.deleted2) {
					break;
				}
				node2 = (ALTNode) biTree.deleteMin();
				nodesChecked++;
				if(write) {
					out2.write(node2.id+" "+node2.lat+" "+node2.lon);
					out2.newLine();
				}
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
			
			out1.write("end");
			out1.flush();
			out1.close();
			out2.write("end");
			out2.flush();
			out2.close();
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
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
	public long ALTUnidirectionalSearch(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
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
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
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
		// Needed to compute forward distances
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			ALTNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				// public Edge(long id, String type, int distance, int maxSpeed, int travelTime)
				reverseNode.addEdge2(new Edge(node.id, edge.type, edge.distance, edge.maxSpeed, edge.travelTime));
			}
		}
		
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		double cmMsec = 130*0.0277777778;
		BufferedWriter out1 = null;
		nodesChecked = 0;
		
		for(int r = 0; r < runs; r++) {
			
			System.out.println("Run "+(r+1));
			
			out1 = new BufferedWriter(new FileWriter("ALT1.txt"));
			
			preprocessStart = System.currentTimeMillis();
			
			
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				// Reset
				node.landmarksForwardDistances = new ArrayList<Long>();
				node.key = Long.MAX_VALUE - i;
				node.colour = false;
				node.deleted = false;
				node.inserted = false;
				node.parent = null;
				node.leftChild = null;
				node.rightChild = null;
				node.path = null;
				node.pathLength = Long.MAX_VALUE;
				node.keyLength = Long.MAX_VALUE;
				node.potential = 0;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
					node.keyLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
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
			
			ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarksUnidirectional(landmarks, nodes,hashMap);
			//ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
			System.out.println("Invalidated "+invalidLandmarks.size() + " landmarks");
			
			// Pick which landmarks to use for the forward and reverse search
			ArrayList<Integer> forwardLandmarks = new ArrayList<Integer>();
			long min = Long.MAX_VALUE;
			int minNode = 0;
			long dist = 0;
			long dist2 = 0;
			boolean contains = false;
			
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
				node.keyLength = Long.MAX_VALUE;
				node.potential = 0;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
					node.keyLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
				}
			}

			preprocessStop = System.currentTimeMillis();
			preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			RedBlackTree tree = new RedBlackTree();
			
			// Insert
			tree.insertNode(sourceNode);
			sourceNode.inserted = true;
			targetNode.inserted2 = true;

			// Calculate potential of source node
			// Choose max distance
			Double sourcePotential = Double.MIN_VALUE;
			for(int x = 0; x < forwardLandmarks.size(); x++) {
				int y = forwardLandmarks.get(x);
				long distV = sourceNode.landmarksForwardDistances.get(y);
				long distW = targetNode.landmarksForwardDistances.get(y);
				Double potential = (distV-distW)/cmMsec;
				if(potential > sourcePotential) {
					sourcePotential = potential;
				}
			}

			sourceNode.potential = Math.round(sourcePotential);
			
			// Dijkstra
			ALTNode node1 = sourceNode;
			//while(true) {
			while(node1.id != targetNode.id) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(write) {
					out1.write(node1.id+" "+node1.lat+" "+node1.lon);
					out1.newLine();
				}
				node1.deleted = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					
					// Choose potential for W that maximizes the discount to the edge (V,W)
					long potential = Long.MIN_VALUE;
					//long potential = Long.MAX_VALUE;
					long potentialW;
					long distW = 0;
					long distTarget;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distW = decreaseNode1.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						potentialW = Math.round((distW-distTarget) / cmMsec);
						if(potential < potentialW - node1.potential) {
							potential = potentialW - node1.potential;
							decreaseNode1.potential = potentialW;
						}
					}

					long newKeyLength = node1.keyLength + edge1.travelTime + potential;
					
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
						decreaseNode1.keyLength = newKeyLength;
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
			
			ArrayList<ALTNode> path = new ArrayList<ALTNode>();
			
			// Found a node on shortest path, follow it
			node = node1;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (ALTNode) node.path;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
			
			out1.write("end");
			out1.flush();
			out1.close();
		}
		
		nodesChecked = nodesChecked/runs;
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
		
		//System.out.println("Forward landmark used was "+landmarks.get(forwardLandmarks.get(0)).id);
		
		return (targetNode.pathLength);
		//return shortest;
	}
	
	public long ALTUnidirectionalSearchBackup(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
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
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
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
		// Needed to compute forward distances
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			ALTNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				// public Edge(long id, String type, int distance, int maxSpeed, int travelTime)
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
		
		ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarksUnidirectional(landmarks, nodes,hashMap);
		//ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
		System.out.println("Invalidated "+invalidLandmarks.size() + " landmarks");
		
		// Pick which landmarks to use for the forward and reverse search
		ArrayList<Integer> forwardLandmarks = new ArrayList<Integer>();
		long min = Long.MAX_VALUE;
		int minNode = 0;
		long dist = 0;
		long dist2 = 0;
		boolean contains = false;
		
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
		
		double cmMsec = 130*0.0277777778;
		BufferedWriter out1 = null;
		
		for(int r = 0; r < runs; r++) {
			
			System.out.println("Run "+(r+1));
			
			out1 = new BufferedWriter(new FileWriter("ALT1.txt"));
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			
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
				node.keyLength = Long.MAX_VALUE;
				node.potential = 0;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
					node.keyLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
				}
			}
			
			// Experiment
			//targetNode = landmarks.get(forwardLandmarks.get(0));
			
			preprocessStop = System.currentTimeMillis();
			//preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			RedBlackTree tree = new RedBlackTree();
			
			// Insert
			tree.insertNode(sourceNode);
			sourceNode.inserted = true;
			targetNode.inserted2 = true;
			
			// Test
			/*int z = forwardLandmarks.get(0);
			System.out.println(Long.toString((long) (sourceNode.landmarksForwardDistances.get(z)/cmMsec)));
			*/
			
			// Test
			//targetNode.id = 1;
			
			
			// Calculate potential of source node
			// Choose max distance
			Double sourcePotential = Double.MIN_VALUE;
			for(int x = 0; x < forwardLandmarks.size(); x++) {
				int y = forwardLandmarks.get(x);
				/*long distV = sourceNode.landmarksBackwardDistances.get(y);
				long distW = targetNode.landmarksBackwardDistances.get(y);
				Double potential = (distW-distV)/cmMsec;*/
				long distV = sourceNode.landmarksForwardDistances.get(y);
				long distW = targetNode.landmarksForwardDistances.get(y);
				Double potential = (distV-distW)/cmMsec;
				if(potential > sourcePotential) {
					sourcePotential = potential;
				}
			}
			/*for(int x = 0; x < forwardLandmarks.size(); x++) {
				int y = forwardLandmarks.get(x);
				long distV = sourceNode.landmarksBackwardDistances.get(y);
				long distW = targetNode.landmarksBackwardDistances.get(y);
				Double potential = (distW-distV)/cmMsec;
				if(potential > sourcePotential) {
					sourcePotential = potential;
				}
			}*/
			sourceNode.potential = Math.round(sourcePotential);
			
			// Dijkstra
			ALTNode node1 = sourceNode;
			//while(true) {
			while(node1.id != targetNode.id) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(write) {
					out1.write(node1.id+" "+node1.lat+" "+node1.lon);
					out1.newLine();
				}
				node1.deleted = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					/*if(decreaseNode1.potential == 0) {
						long forward = 0;
						for(int x = 0; x < forwardLandmarks.size(); x++) {
							int y = forwardLandmarks.get(x);
							if(forward < decreaseNode1.landmarksForwardDistances.get(y) - targetNode.landmarksForwardDistances.get(y)) {
								forward = decreaseNode1.landmarksForwardDistances.get(y) - targetNode.landmarksForwardDistances.get(y);
							}
						}
						decreaseNode1.potential = forward;
					}
					long newKeyLength = node1.keyLength + edge1.travelTime + 
							decreaseNode1.potential - node1.potential;*/
					
					//long potential = Long.MAX_VALUE;
					/*for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						potentialV = node1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y);
						potentialW = decreaseNode1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y);
						if(potential > potentialW - potentialV) {
							potential = potentialW - potentialV;
						}
					}*/
					/*for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						potentialW = (long) ((decreaseNode1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y)) / cmMsec);
						if(potentialW < 0) {
							potentialW = potentialW * -1;
						}
						if(potential < potentialW) {
							potential = potentialW;
						}
					}*/
					
					/*Double potential = Double.MAX_VALUE;
					//Double potential = Double.MIN_VALUE;
					Double potentialV;
					Double potentialW;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						//potentialW = Math.round((decreaseNode1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y)) / cmMsec);
						/*potentialW = ((decreaseNode1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y)) / cmMsec);
						/*if(potentialW < 0) {
							potentialW = potentialW * -1;
						}*/
						//potentialV = Math.round((node1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y)) / cmMsec);
						/*potentialV = ((node1.landmarksForwardDistances.get(y)  - targetNode.landmarksForwardDistances.get(y)) / cmMsec);
						/*if(potentialV < 0) {
							potentialV = potentialV * -1;
						}*/
						/*if(potential > potentialW - potentialV) {
							potential = potentialW - potentialV;
						}
					}
					
					long newKeyLength = node1.keyLength + edge1.travelTime + ((long)(potential*0.6));
					// Virker med min potential long newKeyLength = node1.keyLength + edge1.travelTime + ((long)(potential*0.6));
					//long newKeyLength = node1.keyLength + edge1.travelTime + (potential/2);
					//long newKeyLength = node1.keyLength + edge1.travelTime + potential;
					 * 
					 */
					
					/*Double potential;
					boolean max = false;
					long distNode = decreaseNode1.landmarksForwardDistances.get(0).longValue();
					long distTarget = targetNode.landmarksForwardDistances.get(0).longValue();
					if(distNode < 1 || distTarget < 1) {
						System.out.println("111 "+distNode+" "+distTarget);
					}
					/*if(distNode == Long.MAX_VALUE) {
						// Not reachable in the reverse graph, so we cant use it.
						continue;
					}*/
					/*if(distNode == Long.MAX_VALUE || distTarget == Long.MAX_VALUE) {
						//System.out.println("222 "+distNode+" "+distTarget);
						max = true;
					}
					potential = ((distNode - distTarget) / cmMsec);
					//potential = ((distTarget - distNode) / cmMsec);
					if(potential < 0) {
						potential = potential * -1;
					}
					if(potential == Double.MAX_VALUE || potential == Double.MIN_VALUE) {
						System.out.println("333");
					}
					/*Double potential2;
					distNode = decreaseNode1.landmarksBackwardDistances.get(0).longValue();
					distTarget = targetNode.landmarksBackwardDistances.get(0).longValue();
					potential2 = ((distTarget-distNode) / cmMsec);
					if(potential2 < 0) {
						potential2 = potential2 * -1;
					}
					if(potential2 > potential || max) {
						potential = potential2;
					}*/
					/*if(max && (distNode == Long.MAX_VALUE || distTarget == Long.MAX_VALUE)) {
						continue;
					}
					
					long newKeyLength = node1.keyLength + edge1.travelTime + (Math.round(potential));
					*/
					
					
					//WORKS
					//Double potential = Double.MAX_VALUE;
					/*Double potential = Double.MIN_VALUE;
					Double potentialV;
					Double potentialW;
					long distV = 0;
					long distW = 0;
					long distTarget;
					distV = node1.landmarksBackwardDistances.get(0);
					distW = decreaseNode1.landmarksBackwardDistances.get(0);
					distTarget = targetNode.landmarksBackwardDistances.get(0);
					potentialW = ((distTarget  - distW) / cmMsec);
					potentialV = ((distTarget  - distV) / cmMsec);
					/*if(potentialW < 0) {
						potentialW = potentialW * -1;
					}
					if(potentialV < 0) {
						potentialV = potentialV * -1;
					}*/
					
					/*potential = potentialW - potentialV;
					if(edge1.travelTime +  Math.round(potential) < 0) {
						//potential = (double) edge1.travelTime*-1;
						System.out.println("!!! "+edge1.travelTime+" "+potential);
						System.out.println(distV+" "+distW+" "+distTarget);
					}
					long newKeyLength = node1.keyLength + edge1.travelTime + Math.round(potential*0.95);
					*/
					
					/*Double potential;
					 
					WORKS - pathlength for this type, keylength for other type
					long distNode = decreaseNode1.landmarksBackwardDistances.get(0).longValue();
					long distTarget = targetNode.landmarksBackwardDistances.get(0).longValue();
					potential = ((distTarget - distNode) / cmMsec);
					if(potential < 0) {
						potential = potential * -1;
					}
					if(distNode == Long.MAX_VALUE || distTarget == Long.MAX_VALUE) {
						continue;
					}
					long newKeyLength = node1.pathLength+ edge1.travelTime + Math.round(potential);
					*/
					
					// Works single landmark
					//Double potential = Double.MAX_VALUE;
					/*Double potential = Double.MIN_VALUE;
					Double potentialV;
					Double potentialW;
					long distV = 0;
					long distW = 0;
					long distTarget;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distV = node1.landmarksBackwardDistances.get(y);
						distW = decreaseNode1.landmarksBackwardDistances.get(y);
						distTarget = targetNode.landmarksBackwardDistances.get(y);
						potentialW = ((distTarget  - distW) / cmMsec);
						potentialV = ((distTarget  - distV) / cmMsec);
						/*if(potentialW < 0) {
							potentialW = potentialW * -1;
						}
						if(potentialV < 0) {
							potentialV = potentialV * -1;
						}*/
						/*if(potential < potentialW - potentialV) {
							potential = potentialW - potentialV;
						}
					}

					long newKeyLength = node1.keyLength + edge1.travelTime + Math.round(potential);
					*/
					
					// Choose potential for W that maximizes the discount to the edge (V,W)
					long potential = Long.MIN_VALUE;
					//long potential = Long.MAX_VALUE;
					long potentialW;
					long distW = 0;
					long distTarget;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distW = decreaseNode1.landmarksForwardDistances.get(y);
						distTarget = targetNode.landmarksForwardDistances.get(y);
						potentialW = Math.round((distW-distTarget) / cmMsec);
						if(potential < potentialW - node1.potential) {
							potential = potentialW - node1.potential;
							decreaseNode1.potential = potentialW;
						}
					}
					/*for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distW = decreaseNode1.landmarksBackwardDistances.get(y);
						distTarget = targetNode.landmarksBackwardDistances.get(y);
						potentialW = Math.round((distTarget  - distW) / cmMsec);
						if(potential < potentialW - node1.potential) {
							potential = potentialW - node1.potential;
							decreaseNode1.potential = potentialW;
						}
					}*/
					long newKeyLength = node1.keyLength + edge1.travelTime + potential;
					
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.travelTime;
						decreaseNode1.keyLength = newKeyLength;
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
			
			ArrayList<ALTNode> path = new ArrayList<ALTNode>();
			
			// Found a node on shortest path, follow it
			node = node1;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (ALTNode) node.path;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
			
			out1.write("end");
			out1.flush();
			out1.close();
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
		
		//System.out.println("Forward landmark used was "+landmarks.get(forwardLandmarks.get(0)).id);
		
		return (targetNode.pathLength);
		//return shortest;
	}
	
	public long ALTUniDistance(String input, long source, long target, int k, int u, int o, int typeOfLandMark, int runs) throws FileNotFoundException, IOException {
		
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
		HashMap<Long,ALTNode> hashMap = new HashMap<Long,ALTNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			node = new ALTNode(normalNodes.get(i));
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
		// Needed to compute forward distances
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge edge = null;
			ALTNode reverseNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				edge = node.edges.get(j);
				reverseNode = hashMap.get(edge.nodeID);
				// public Edge(long id, String type, int distance, int maxSpeed, int travelTime)
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
		
		//ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarksUnidirectional(landmarks, nodes,hashMap);
		ArrayList<Integer> invalidLandmarks = calculateDistancesToLandmarks(landmarks, nodes,hashMap);
		System.out.println("Invalidated "+invalidLandmarks.size() + " landmarks");
		
		// Pick which landmarks to use for the forward and reverse search
		ArrayList<Integer> forwardLandmarks = new ArrayList<Integer>();
		long min = Long.MAX_VALUE;
		int minNode = 0;
		long dist = 0;
		long dist2 = 0;
		boolean contains = false;
		
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
		
		double cmMsec = 130*0.0277777778;
		BufferedWriter out1 = null;
		
		for(int r = 0; r < runs; r++) {
			
			System.out.println("Run "+(r+1));
			
			out1 = new BufferedWriter(new FileWriter("ALT1.txt"));
			
			nodesChecked = 0;
			preprocessStart = System.currentTimeMillis();
			
			
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
				node.keyLength = Long.MAX_VALUE;
				node.potential = 0;
				if(node.id == source) {
					sourceNode = node;
					node.key = 0;
					node.pathLength = 0;
					node.keyLength = 0;
				}
				else if(node.id == target) {
					targetNode = node;
				}
			}
			
			// Experiment
			//targetNode = landmarks.get(forwardLandmarks.get(0));
			
			preprocessStop = System.currentTimeMillis();
			//preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			RedBlackTree tree = new RedBlackTree();
			
			// Insert
			tree.insertNode(sourceNode);
			sourceNode.inserted = true;
			targetNode.inserted2 = true;
			
			// Test
			/*int z = forwardLandmarks.get(0);
			System.out.println(Long.toString((long) (sourceNode.landmarksForwardDistances.get(z)/cmMsec)));
			*/
			
			// Test
			//targetNode.id = 1;
			
			// Dijkstra
			ALTNode node1 = sourceNode;
			//while(true) {
			while(node1.id != targetNode.id) {
				node1 = (ALTNode) tree.deleteMin();
				nodesChecked++;
				if(write) {
					out1.write(node1.id+" "+node1.lat+" "+node1.lon);
					out1.newLine();
				}
				node1.deleted = true;
				Edge edge1 = null;
				ALTNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					
					
					//Double potential = Double.MAX_VALUE;
					long potential = Long.MIN_VALUE;
					long potentialV = 0;
					long potentialW = 0;
					long distV = 0;
					long distW = 0;
					long distTarget = 0;
					for(int x = 0; x < forwardLandmarks.size(); x++) {
						int y = forwardLandmarks.get(x);
						distV = node1.landmarksBackwardDistances.get(y);
						distW = decreaseNode1.landmarksBackwardDistances.get(y);
						distTarget = targetNode.landmarksBackwardDistances.get(y);
						potentialW = distTarget  - distW;
						potentialV = distTarget  - distV;
						/*if(potentialW < 0) {
							potentialW = potentialW * -1;
						}
						if(potentialV < 0) {
							potentialV = potentialV * -1;
						}*/
						if(potential < potentialW - potentialV) {
							potential = potentialW - potentialV;
						}
					}
					if(edge1.distance+potential < 0) {
						System.out.println("!!! "+edge1.distance + " " + potential);
						System.out.println(distV+" "+distW+" "+distTarget);
						System.out.println(potentialV+" "+potentialW);
					}

					long newKeyLength = node1.keyLength + edge1.distance + potential;
					
					
					/*long potential = Long.MIN_VALUE;
					long potentialV;
					long potentialW;
					long distV = 0;
					long distW = 0;
					long distTarget;
					distV = node1.landmarksBackwardDistances.get(0);
					distW = decreaseNode1.landmarksBackwardDistances.get(0);
					distTarget = targetNode.landmarksBackwardDistances.get(0);
					potentialW = distTarget - distW;
					potentialV = distTarget - distV;
					if(potentialW < 0) {
						potentialW = potentialW * -1;
					}
					if(potentialV < 0) {
						potentialV = potentialV * -1;
					}
					
					potential = potentialW - potentialV;
					if(edge1.distance +  potential < 0) {
						System.out.println("!!! "+edge1.distance+" "+potential);
						System.out.println(distV+" "+distW+" "+distTarget);
					}
					long newKeyLength = node1.keyLength + edge1.distance + potential;*/
					
					/*long potential;
					long distNode = decreaseNode1.landmarksBackwardDistances.get(0).longValue();
					long distTarget = targetNode.landmarksBackwardDistances.get(0).longValue();
					potential = distTarget - distNode;
					if(potential < 0) {
						potential = potential * -1;
					}
					if(distNode == Long.MAX_VALUE || distTarget == Long.MAX_VALUE) {
						System.out.println("!!!");
						continue;
					}
					long newKeyLength = node1.pathLength+ edge1.distance + potential;*/
					
					
					if(!decreaseNode1.deleted && newKeyLength < decreaseNode1.keyLength) {
						decreaseNode1.path = node1;
						decreaseNode1.pathLength = node1.pathLength + edge1.distance;
						decreaseNode1.keyLength = newKeyLength;
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
			
			ArrayList<ALTNode> path = new ArrayList<ALTNode>();
			
			// Found a node on shortest path, follow it
			node = node1;
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (ALTNode) node.path;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
			
			out1.write("end");
			out1.flush();
			out1.close();
		}
		
		preprocessTime = preprocessTotal / runs;
		queryTime = queryTotal / runs;
		
		//System.out.println("Forward landmark used was "+landmarks.get(forwardLandmarks.get(0)).id);
		
		return (targetNode.pathLength);
		//return shortest;
	}
	
	/**
	 * Calculates the shortest path (forward and backward) between all nodes and all landmarks.
	 * @param landmarks
	 * @param nodes
	 */
	public ArrayList<Integer> calculateDistancesToLandmarksTravelTime(ArrayList<ALTNode> landmarks, ArrayList<ALTNode> nodes, HashMap<Long,ALTNode> hashMap) {
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ALTNode node = null;
		// Do a Dijkstra search, and reverse, from each landmark, adding the shortest path to each node
		for(int i = 0; i < landmarks.size(); i++) {
			System.out.println("Calculating all distances to landmark "+(i+1));
			node = landmarks.get(i);
			computeForwardTravelTime(node, nodes, hashMap, ret, i);
			computeBackwardTravelTime(node, nodes, hashMap, ret, i);
		}
		return ret;
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
			System.out.println("Calculating all distances to landmark "+(i+1));
			node = landmarks.get(i);
			computeForward(node, nodes, hashMap, ret, i);
			computeBackward(node, nodes, hashMap, ret, i);
		}
		return ret;
	}
	
	/**
	 * Calculates the shortest path (forward and backward) between all nodes and all landmarks.
	 * @param landmarks
	 * @param nodes
	 */
	public ArrayList<Integer> calculateDistancesToLandmarksUnidirectional(ArrayList<ALTNode> landmarks, ArrayList<ALTNode> nodes, HashMap<Long,ALTNode> hashMap) {
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ALTNode node = null;
		// Do a Dijkstra search, and reverse, from each landmark, adding the shortest path to each node
		for(int i = 0; i < landmarks.size(); i++) {
			System.out.println("Calculating all distances to landmark "+(i+1));
			node = landmarks.get(i);
			computeForward(node, nodes, hashMap, ret, i);
			//computeBackward(node, nodes, hashMap, ret, i);
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
			node.landmarksForwardDistances.add(new Long(node.pathLength2));
			node.deleted2 = true;
			Edge edge = null;
			ALTNode decreaseNode = null;
			checked++;
			for(int i = 0; i < node.edges2.size(); i++) {
				edge = node.edges2.get(i);
				decreaseNode = hashMap.get(edge.nodeID);
				long newPathLength = node.pathLength2 + edge.distance;
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
		//System.out.println("Landmark Checked "+checked);
	}
	
	/**
	 * Performs a Dijkstra search from source to all nodes via. the reverse graph, thus computing
	 * the shortest distance from all nodes to source. Adds this to landmarksForwardDistances.
	 * @param source
	 * @param nodes
	 * @param hashMap
	 */
	private void computeForwardTravelTime(ALTNode source, ArrayList<ALTNode> nodes, HashMap<Long,ALTNode> hashMap, ArrayList<Integer> invalid, int number) {
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
	private void computeBackwardTravelTime(ALTNode source, ArrayList<ALTNode> nodes, HashMap<Long,ALTNode> hashMap, ArrayList<Integer> invalid, int number) {
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
				long newPathLength = node.pathLength + edge.distance;
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
		System.out.println("Computing farthest landmark 1");
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
			System.out.println("Computing farthest landmark "+(i+2));
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
		System.out.println("Computing optimized farthest landmark 1");
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
			System.out.println("Computing optimized farthest landmark "+(i+2));
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
			System.out.println("Finding extra landmark "+(i+1));
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
