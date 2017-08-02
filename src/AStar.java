import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tool.Edge;
import tool.Node;
import tool.Tool;

public class AStar {
	
private static int shift = 33;
	
	private long preprocessStart = 0;
	private long preprocessStop = 0;
	private long queryStart = 0;
	private long queryStop = 0;
	private long preprocessTotal = 0;
	private long queryTotal = 0;
	long preprocessTime = 0;
	long queryTime = 0;
	ArrayList<AStarNode> check = null;
	long nodesChecked = 0;
	
	public long aStarEuclidian(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		
		HashMap<Long,AStarNode> hashMap = new HashMap<Long,AStarNode>();
		
		// Selection random source and target
		// Alternatively always use the same source and target
		if(source == 0 || target == 0) {
			Random random = new Random();
			source = normalNodes.get(random.nextInt(normalNodes.size())).id;
			target = normalNodes.get(random.nextInt(normalNodes.size())).id;
		}

		AStarNode sourceNode = null;
		AStarNode targetNode = null;
		AStarNode node = null;
		Node normalNode = null;
		
		ArrayList<AStarNode> nodes = new ArrayList<AStarNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			normalNode = normalNodes.get(i);
			node = new AStarNode(normalNode);
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// NOPE - Wait till they are touched by one of the heaps.
		// Add potential
		double cmHour = 130*27.7777778;
		/*for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			// Euclidian distance in cm divided by 130km/h in cm/h. 
			// Dont use Math.round as it must be a lower bound. Casting to long will round it down.
			node.potential = (long) (calculateDistance(node.lat, node.lon, targetNode.lat, targetNode.lon) / cmHour);
		}*/
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		for(int r = 0; r < runs; r++) {
			
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
			
			preprocessStop = System.currentTimeMillis();
			preprocessTotal += (preprocessStop-preprocessStart);
			
			queryStart = System.currentTimeMillis();
			
			RedBlackTree tree = new RedBlackTree();
			tree.insertNode(sourceNode);
			
			// Dijkstra
			node = sourceNode;
			while(node.id != targetNode.id) {
				node = (AStarNode) tree.deleteMin();
				nodesChecked++;
				node.deleted = true;
				// Calculate potential if not already done
				if(node.potential == 0) {
					node.potential = (long) (calculateDistance(node.lat, node.lon, targetNode.lat, targetNode.lon) / cmHour);
				}
				Edge edge = null;
				AStarNode decreaseNode = null;
				for(int i = 0; i < node.edges.size(); i++) {
					edge = node.edges.get(i);
					decreaseNode = hashMap.get(edge.nodeID);
					// Calculate potential
					if(decreaseNode.potential == 0) {
						decreaseNode.potential = (long) (calculateDistance(decreaseNode.lat, decreaseNode.lon, targetNode.lat, targetNode.lon) / cmHour);
					}
					// Give discount if we move towards target
					// and likewhise give penalty if we move away
					long newKeyLength = node.pathLength + edge.travelTime + 
							(decreaseNode.potential - node.potential);
					if(!decreaseNode.deleted && newKeyLength < decreaseNode.keyLength) {
						decreaseNode.path = node;
						decreaseNode.pathLength = node.pathLength + edge.travelTime;
						decreaseNode.keyLength = newKeyLength;
						if(decreaseNode.inserted) {
							tree.decreaseKey(decreaseNode, newKeyLength);
						}
						else {
							decreaseNode.key = calcKey(newKeyLength,decreaseNode.id);
							decreaseNode.inserted = true;
							tree.insertNode(decreaseNode);
						}
					}
				}
			}
			
			// Need to find the path as well, not just the path length
			ArrayList<AStarNode> path = new ArrayList<AStarNode>();
			
			// Run through path writing out the route
			while(node.id != sourceNode.id) {
				path.add(node);
				node = (AStarNode) node.path;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
		}

		return targetNode.pathLength;
		//return targetNode.pathLength + (((AStarNode)(targetNode.path)).potential);
	}
	
	public long aStarBiDirectionalEuclidian(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		
		preprocessTotal = 0;
		queryTotal = 0;
		preprocessTime = 0;
		queryTime = 0;
		
		preprocessStart = System.currentTimeMillis();
		
		Tool tool = new Tool();
		ArrayList<Node> normalNodes = tool.getNodesAsArrayList(input);
		
		HashMap<Long,AStarNode> hashMap = new HashMap<Long,AStarNode>();
		
		// Selection random source and target
		// Alternatively always use the same source and target
		if(source == 0 || target == 0) {
			Random random = new Random();
			source = normalNodes.get(random.nextInt(normalNodes.size())).id;
			target = normalNodes.get(random.nextInt(normalNodes.size())).id;
		}

		AStarNode sourceNode = null;
		AStarNode targetNode = null;
		AStarNode node = null;
		Node normalNode = null;
		
		ArrayList<AStarNode> nodes = new ArrayList<AStarNode>();
		
		for(int i = 0; i < normalNodes.size(); i++) {
			normalNode = normalNodes.get(i);
			node = new AStarNode(normalNode);
			nodes.add(node);
			hashMap.put(node.id, node);
			if(node.id == source) {
				sourceNode = node;
			}
			else if(node.id == target) {
				targetNode = node;
			}
		}
		
		// Nope - Wait till the nodes are touched.
		// Was also wrong potential.
		// Add potential
		double cmHour = 130*27.7777778;
		/*long potential1 = 0;
		long potential2 = 0;
		long potentialSourceTarget = (long) (calculateDistance(sourceNode.lat, sourceNode.lon, targetNode.lat, targetNode.lon) / cmHour);
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			// Euclidian distance in cm divided by 130km/h in cm/h. 
			// Dont use Math.round as it must be a lower bound. Casting to long will round it down.
			
			potential1 = (long) (calculateDistance(node.lat, node.lon, targetNode.lat, targetNode.lon) / cmHour);
			potential2 = (long) (calculateDistance(node.lat, node.lon, sourceNode.lat, sourceNode.lon) / cmHour);
			node.potential = ((potential1 - potential2)/2) + potentialSourceTarget;
			node.potential2 = ((potential2 - potential1)/2) + potentialSourceTarget;
		}*/
		
		// Add opposite edges
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			Edge biEdge = null;
			Edge newEdge = null;
			AStarNode toNode = null;
			for(int j = 0; j < node.edges.size(); j++) {
				biEdge = node.edges.get(j);
				newEdge = new Edge(node.id,biEdge.type,biEdge.distance,biEdge.maxSpeed,biEdge.travelTime);
				toNode = hashMap.get(biEdge.nodeID);
				toNode.addEdge2(newEdge);
			}
			
		}
		
		preprocessStop = System.currentTimeMillis();
		preprocessTotal += (preprocessStop-preprocessStart);
		preprocessTotal = preprocessTotal * runs; // To be averaged out later
		
		long shortest = Long.MAX_VALUE; // For use below
		AStarNode smallest = null;
		
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
			AStarNode node1 = sourceNode;
			AStarNode node2 = targetNode;
			//while(true) {
			while(node1.id != targetNode.id && node2.id != sourceNode.id) {
				node1 = (AStarNode) tree.deleteMin();
				nodesChecked++;
				if(node1.deleted2) {
					break;
				}
				if(node1.potential == 0) {
					node1.potential = (long) ((calculateDistance(node1.lat,node1.lon,targetNode.lat,targetNode.lon) - calculateDistance(sourceNode.lat,sourceNode.lon,node1.lat,node1.lon))/(2*cmHour));
				}
				node2 = (AStarNode) biTree.deleteMin();
				nodesChecked++;
				if(node2.deleted) {
					break;
				}
				if(node2.potential2 == 0) {
					node2.potential2 = (long) ((calculateDistance(sourceNode.lat,sourceNode.lon,node2.lat,node2.lon) - calculateDistance(node2.lat,node2.lon,targetNode.lat,targetNode.lon))/(2*cmHour));
				}
				if(node1.potential+node1.potential2 >= shortest || node2.potential+node2.potential2 >= shortest) {
					//System.out.println("TEST "+shortest);
					break;
				}
				node1.deleted = true;
				node2.deleted2 = true;
				Edge edge1 = null;
				AStarNode decreaseNode1 = null;
				for(int i = 0; i < node1.edges.size(); i++) {
					edge1 = node1.edges.get(i);
					decreaseNode1 = hashMap.get(edge1.nodeID);
					if(decreaseNode1.potential == 0) {
						decreaseNode1.potential = (long) ((calculateDistance(decreaseNode1.lat,decreaseNode1.lon,targetNode.lat,targetNode.lon) - calculateDistance(sourceNode.lat,sourceNode.lon,decreaseNode1.lat,decreaseNode1.lon))/(2*cmHour));
					}
					long newKeyLength = node1.pathLength + edge1.travelTime + 
							(decreaseNode1.potential - node1.potential);
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
				AStarNode decreaseNode2 = null;
				for(int i = 0; i < node2.edges2.size(); i++) {
					edge2 = node2.edges2.get(i);
					decreaseNode2 = hashMap.get(edge2.nodeID);
					if(decreaseNode2.potential2 == 0) {
						decreaseNode2.potential2 = (long) ((calculateDistance(sourceNode.lat,sourceNode.lon,decreaseNode2.lat,decreaseNode2.lon) - calculateDistance(decreaseNode2.lat,decreaseNode2.lon,targetNode.lat,targetNode.lon))/(2*cmHour));
					}
					long newKeyLength = node2.pathLength2 + edge2.travelTime + 
							(decreaseNode2.potential2 - node2.potential2);
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
			
			ArrayList<AStarNode> path = new ArrayList<AStarNode>();
			
			// Run through all nodes and find smallest pathLenght + pathLength2
			/*long val = 0;
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
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
				node = (AStarNode) node.path;
			}
			path.add(node);
			node = smallest;
			while(node.id != targetNode.id) {
				path.add(node);
				node = (AStarNode) node.path2;
			}
			path.add(node);
			
			queryStop = System.currentTimeMillis();
			queryTotal += (queryStop - queryStart);
			
			check = path;
				
		}

		return shortest;
	}
	
	protected long calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		
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
	
	// Shift val shift places to the left to make space for ID of 8bil.
	private long calcKey(long newPathLenght, long id) {
		long ret = newPathLenght;
		ret = ret << shift;
		ret = ret+id;
		return ret;
	}

}
