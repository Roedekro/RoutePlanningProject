import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import tool.*;

public class Main {

	private static int shift = 33;
	
	public static void main(String[] args) {
		//testRedBlackTree();
		//testRedBlackTree2();
		String input = "Roedekro";
		long source = 2234623300L; // Birkeparken
		long target = 691716575L; // Lillevang
		int runs = 1;
		
		File file = new File("Roedekro");
		System.out.println(file.exists());
		if(file.exists()) {
			try {
				//dijkstraTestWithChecks("Roedekro");
				//bidirectionalDijkstraTest("Roedekro");
				
				// Correct output is 203260
				// Other numbers will vary, either 77 or 78 and xxxx.
				// This is because bidirectional will add the shortest node twice to output.
				//normalDijkstra(input,source,target,runs);
				//dijkstraDelayedInsert(input,source,target,runs);
				//bidirectionalDijkstra(input,source,target,runs);
				//bidirectionalDijkstraDelayedInsert(input,source,target,runs);
				//aStarEuclidian(input,source,target,runs);
				//aStarBiDirectionalEuclidian(input, source, target, runs);
				//ALTWorksButShouldnt(input,source,target,runs,16,1,0,1);
				//ALTWorksButShouldnt(input,source,target,runs,16,4,0,2);
				//ALT(input,source,target,runs,16,4,0,2);
				//ALTSymmetric(input,source,target,runs,16,4,0,2);
				//ALTSymmetricLowerBound(input,source,target,runs,16,4,0,2); // Doesnt seem to be correct
				//CHNaive(input,source,target,runs);
				CHNaive(input,target,source,runs);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void CHNaive(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		CH ch = new CH();
		long ret = ch.CHNaivebyNodeID(input, source, target, runs);
		System.out.println(ret);
	}
	
	//typeOfLandMark 1 = random, 2 = farthest, 3 = farthest optimized
	public static void ALT(String input, long source, long target, int runs, int k, int u, int o, int type) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		long ret = alt.ALTBidirectionalSearch(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"ALT.txt"));
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(Long.toString(node.id));
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void ALTSymmetric(String input, long source, long target, int runs, int k, int u, int o, int type) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		long ret = alt.ALTBidirectionalSearchSymmetric(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"ALTSymmetric.txt"));
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(Long.toString(node.id));
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void ALTSymmetricLowerBound(String input, long source, long target, int runs, int k, int u, int o, int type) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		long ret = alt.ALTBidirectionalSearchSymmetricLowerBounding(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"ALTSymmetricLowerBound.txt"));
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(Long.toString(node.id));
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void ALTWorksButShouldnt(String input, long source, long target, int runs, int k, int u, int o, int type) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		long ret = alt.ALTBidirectionalWorksButShouldnt(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"ALTWorksButShouldnt.txt"));
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(Long.toString(node.id));
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void aStarEuclidian(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		AStar aStar = new AStar();
		long ret = aStar.aStarEuclidian(input, source, target, runs);
		ArrayList<AStarNode> nodes = aStar.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"PathAStarEuclidian.txt"));
		System.out.println(ret + " " + nodes.size() + " " + aStar.nodesChecked);
		AStarNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(Long.toString(node.id));
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void aStarBiDirectionalEuclidian(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		AStar aStar = new AStar();
		long ret = aStar.aStarBiDirectionalEuclidian(input, source, target, runs);
		ArrayList<AStarNode> nodes = aStar.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"PathAStarBiDirectionalEuclidian.txt"));
		System.out.println(ret + " " + nodes.size() + " " + aStar.nodesChecked);
		AStarNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(Long.toString(node.id));
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void bidirectionalDijkstra(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		Dijkstra dijkstra = new Dijkstra();
		long ret = dijkstra.bidirectionalDijkstra(input, source, target, runs);
		ArrayList<RedBlackNode> nodes = dijkstra.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"PathBi.txt"));
		System.out.println(ret + " " + nodes.size() + " " + dijkstra.nodesChecked);
		RedBlackNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(Long.toString(node.id));
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void bidirectionalDijkstraDelayedInsert(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		Dijkstra dijkstra = new Dijkstra();
		long ret = dijkstra.bidirectionalDijkstraDelayedInsert(input, source, target, runs);
		ArrayList<RedBlackNode> nodes = dijkstra.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"PathBiDelayed.txt"));
		System.out.println(ret + " " + nodes.size() + " " + dijkstra.nodesChecked);
		RedBlackNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(Long.toString(node.id));
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void normalDijkstra(String input, long source, long target, int runs) throws IOException {
		Dijkstra dijkstra = new Dijkstra();
		long ret = dijkstra.dijkstra(input, source, target, runs);
		ArrayList<RedBlackNode> nodes = dijkstra.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"Path3.txt"));
		System.out.println(ret + " " + nodes.size() + " " + dijkstra.nodesChecked);
		RedBlackNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			if(node.id == target) {
				break;
			}
		}			
		// Run through path writing out the route
		while(node.id != source) {
			out.write(Long.toString(node.id));
			out.newLine();
			node = node.path;
		}
		out.write(Long.toString(node.id));
		out.flush();
		out.close();
		
	}
	
	public static void dijkstraDelayedInsert(String input, long source, long target, int runs) throws IOException {
		Dijkstra dijkstra = new Dijkstra();
		long ret = dijkstra.dijkstraDelayedInsert(input, source, target, runs);
		ArrayList<RedBlackNode> nodes = dijkstra.check;
		BufferedWriter out = new BufferedWriter(new FileWriter("Roedekro"+"Path4.txt"));
		System.out.println(ret + " " + nodes.size() + " " + dijkstra.nodesChecked);
		RedBlackNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			if(node.id == target) {
				break;
			}
		}			
		// Run through path writing out the route
		while(node.id != source) {
			out.write(Long.toString(node.id));
			out.newLine();
			node = node.path;
		}
		out.write(Long.toString(node.id));
		out.flush();
		out.close();
		
	}
	
	// Tests Dijkstra and constantly monitors the red black tree.
	// Very costly.
	public static void dijkstraTestWithChecks(String input) throws FileNotFoundException, IOException {
		
		Tool tool = new Tool();
		ArrayList<Node> nodes = tool.getNodesAsArrayList(input);
		
		HashMap<Long,RedBlackNode> hashMap = new HashMap<Long,RedBlackNode>();
		
		System.out.println("Size is "+nodes.size());
		
		// Selection random source and target
		// Alternatively always use the same source and target
		Random random = new Random();
		//Long source = nodes.get(random.nextInt(nodes.size())).id;
		//Long target = nodes.get(random.nextInt(nodes.size())).id;
		long source = 2234623300L; // Birkeparken
		long target = 691716575L; // Lillevang
		RedBlackNode sourceNode = null;
		RedBlackNode targetNode = null;
		RedBlackNode node = null;
		Node normalNode = null;
		
		RedBlackTree tree = new RedBlackTree();
		for(int i = 0; i < nodes.size(); i++) {
			normalNode = nodes.get(i);
			node = new RedBlackNode(normalNode);
			node.key = node.key - i;
			if(node.id == source) {
				sourceNode = node;
				node.key = 0;
				node.pathLength = 0;
			}
			else if(node.id == target) {
				targetNode = node;
			}
			tree.insertNode(node);
			hashMap.put(node.id, node);
		}
		
		ArrayList<Long> check = new ArrayList<Long>();
		check.add(0L);
		long lastNode = 0;
		
		// Dijkstra
		node = sourceNode;
		while(node.id != targetNode.id) {
			node = tree.deleteMin();
			if(node.id == lastNode) {
				System.out.println("BROKE ==========================================");
			}
			lastNode = node.id;
			Collections.sort(check);
			//if(node.key != check.get(0)) {
			if(Long.compare(node.key, check.get(0)) != 0) {
				System.out.println("=====================================================================");
				System.out.println("Wrong min: "+node.key + " "+ (node.key >> shift) + " "+check.get(0));
				System.out.println((check.get(0) >> shift)+" " +node.id);
				tree.checkTree();
				RedBlackNode find = tree.find(check.get(0));
				System.out.println(find.id + " "+node.key);
				/*long manipulate = check.get(0);
				long nodeToLookUp = (manipulate - ((manipulate >> shift) << shift));
				System.out.println(hashMap.get(nodeToLookUp).deleted);*/
				System.out.println("=====================================================================");
			}
			check.remove(0);
			node.deleted = true;
			Edge edge = null;
			RedBlackNode decreaseNode = null;
			for(int i = 0; i < node.edges.size(); i++) {
				edge = node.edges.get(i);
				decreaseNode = hashMap.get(edge.nodeID);
				long newPathLenght = node.pathLength + edge.travelTime;
				if(!decreaseNode.deleted && newPathLenght < decreaseNode.pathLength) {
					for(int j = 0; j < check.size(); j++) {
						long to = check.get(j);
						if(to == decreaseNode.key) {
							check.remove(j);
							break;
						}
					}
					check.add(calcKey(newPathLenght,decreaseNode.id));
					decreaseNode.path = node;
					decreaseNode.pathLength = newPathLenght;
					tree.decreaseKey(decreaseNode, newPathLenght);
				}
			}
		}
		
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"Path.txt"));
		
		// Run through path writing out the route
		while(node.id != sourceNode.id) {
			out.write(Long.toString(node.id));
			out.newLine();
			node = node.path;
		}
		out.write(Long.toString(node.id));
		out.flush();
		out.close();
		
		System.out.println("The travel time is "+targetNode.pathLength);
		
	}
	
	public static void bidirectionalDijkstraTest(String input) throws FileNotFoundException, IOException {
		
		Tool tool = new Tool();
		ArrayList<Node> nodes = tool.getNodesAsArrayList(input);
		ArrayList<BiRedBlackNode> binodes = new ArrayList<BiRedBlackNode>();
		
		HashMap<Long,BiRedBlackNode> hashMap = new HashMap<Long,BiRedBlackNode>();
		
		System.out.println("Size is "+nodes.size());
		
		// Selection random source and target
		// Alternatively always use the same source and target
		Random random = new Random();
		//Long source = nodes.get(random.nextInt(nodes.size())).id;
		//Long target = nodes.get(random.nextInt(nodes.size())).id;
		long source = 2234623300L; // Birkeparken
		long target = 691716575L; // Lillevang
		BiRedBlackNode sourceNode = null;
		BiRedBlackNode targetNode = null;
		BiRedBlackNode node = null;
		Node normalNode = null;
		
		RedBlackTree tree = new RedBlackTree();
		BiRedBlackTree biTree = new BiRedBlackTree();
		for(int i = 0; i < nodes.size(); i++) {
			normalNode = nodes.get(i);
			node = new BiRedBlackNode(normalNode);
			binodes.add(node);
			node.key = node.key - i;
			node.key2 = node.key2 - i;
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
			tree.insertNode(node);
			biTree.insertNode(node);
		}
		
		tree.checkTree();
		long lastID = 0;

		System.out.println("Dijkstra");
		
		// Bidirectional Dijkstra
		BiRedBlackNode node1 = sourceNode;
		BiRedBlackNode node2 = targetNode;
		while(node1.id != targetNode.id || node2.id != sourceNode.id) {
			node1 = (BiRedBlackNode) tree.deleteMin();
			//System.out.println("Deleted "+node1.id);
			if(node1.id == lastID) {
				System.out.println("BROKE ==========================================");
				break;
			}
			lastID = node1.id;
			if(node1.deleted2) {
				System.out.println("Broke on node1 "+node1.id);
				break;
			}
			node2 = biTree.deleteMin();
			node1.deleted = true;
			node2.deleted2 = true;
			if(node2.deleted) {
				// Done
				System.out.println("Broke on node2 "+node2.id);
				break;
			}
			Edge edge1 = null;
			BiRedBlackNode decreaseNode1 = null;
			for(int i = 0; i < node1.edges.size(); i++) {
				edge1 = node1.edges.get(i);
				decreaseNode1 = hashMap.get(edge1.nodeID);
				long newPathLenght = node1.pathLength + edge1.travelTime;
				if(!decreaseNode1.deleted && newPathLenght < decreaseNode1.pathLength) {
					decreaseNode1.path = node1;
					decreaseNode1.pathLength = newPathLenght;
					tree.decreaseKey(decreaseNode1, newPathLenght);
				}
			}
			Edge edge2 = null;
			BiRedBlackNode decreaseNode2 = null;
			for(int i = 0; i < node2.edges2.size(); i++) {
				edge2 = node2.edges2.get(i);
				decreaseNode2 = hashMap.get(edge2.nodeID);
				long newPathLenght = node2.pathLength2 + edge2.travelTime;
				if(!decreaseNode2.deleted2 && newPathLenght < decreaseNode2.pathLength2) {
					decreaseNode2.path2 = node2;
					decreaseNode2.pathLength2 = newPathLenght;
					biTree.decreasekey(decreaseNode2, newPathLenght);
				}
			}
		}
		
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"Path2.txt"));
		
		System.out.println("Finding shortest");
		
		// Run through all nodes and find smallest pathLenght + pathLength2
		BiRedBlackNode smallest = null;
		long shortest = Long.MAX_VALUE;
		long val = 0;
		for(int i = 0; i < binodes.size(); i++) {
			node = binodes.get(i);
			val = node.pathLength + node.pathLength2;
			if(val < shortest && val > 1 ) {
				shortest = val;
				smallest = node;
			}
		}
		
		System.out.println("Found node on shortest path "+smallest.id+" with value "+shortest);
		System.out.println(smallest.pathLength + " " + smallest.pathLength2);
		
		// Found a node on shortest path, follow it
		node = smallest;
		while(node.id != sourceNode.id) {
			out.write(Long.toString(node.id));
			out.newLine();
			node = (BiRedBlackNode) node.path;
		}
		out.write(Long.toString(node.id));
		out.newLine();
		out.write("======================================================================");
		out.newLine();
		node = smallest;
		while(node.id != targetNode.id) {
			out.write(Long.toString(node.id));
			out.newLine();
			node = (BiRedBlackNode) node.path2;
		}
		out.write(Long.toString(node.id));
		out.flush();
		out.close();
		
		System.out.println("The travel time is "+shortest);
	}
	
	// Shift val shift places to the left to make space for ID of 8bil.
	private static long calcKey(long newPathLenght, long id) {
		long ret = newPathLenght;
		ret = ret << shift;
		ret = ret+id;
		return ret;
	}
	
	public static void testRedBlackTree() {
		
		// The problem was duplicate values. No duplicates allowed
		// in a tree that performs rotations.
		
		int test = 1000;
		System.out.println("Begin");
		Random random = new Random();
		RedBlackTree tree = new RedBlackTree();
		RedBlackNode node = null;
		for(int i = 0; i < test; i++) {
			//int valkey = random.nextInt(test)+1;
			int valkey = i+1;
			node = new RedBlackNode(valkey,0,0);
			node.key = valkey;
			tree.insertNode(node);
			/*System.out.println("=== In");
			System.out.println("Inserted "+valkey);
			tree.checkTree();
			tree.printTree();
			System.out.println("=== Ok");*/
		}
		
		tree.checkTree();
		
		//System.out.println("=== Onward");
		//tree.printTree();
		
		long lastval = 0;
		for(int i = 0; i < test; i++) {
			node = tree.deleteMin();
			if(node.id < lastval) {
				System.out.println("Error "+node.id+" "+lastval+" "+i);
			}
			lastval = node.id;
			//System.out.println(lastval);
		}
		
		System.out.println("Done");
	}
	
	public static void testRedBlackTree2() {
		
		int test = 1000;
		System.out.println("Begin");
		Random random = new Random();
		RedBlackTree tree = new RedBlackTree();
		RedBlackNode node = null;
		ArrayList<Long> check = new ArrayList<Long>();
		ArrayList<RedBlackNode> nodes = new ArrayList<RedBlackNode>();
		for(int i = 0; i < test; i++) {
			int rint = random.nextInt(1000000);
			Long valkey = calcKey(rint,rint);
			check.add(valkey);
			node = new RedBlackNode(valkey,0,0);
			node.key = valkey;
			nodes.add(node);
			tree.insertNode(node);

		}
		
		int i1 = random.nextInt(1000000);
		node = nodes.get(10);
		long r1 = node.key;
		check.add(calcKey(i1,node.id));
		tree.decreaseKey(node, i1);
		System.out.println(calcKey(i1,node.id) + " " + r1);
		
		int i2 = random.nextInt(1000000000);
		node = nodes.get(20);
		long r2 = node.key;
		check.add(calcKey(i2,node.id));
		tree.decreaseKey(node, i2);
		System.out.println(calcKey(i2,node.id) + " " + r2);
		
		int i3 = random.nextInt(1000000000);
		node = nodes.get(30);
		long r3 = node.key;
		check.add(calcKey(i3,node.id));
		tree.decreaseKey(node, i3);
		System.out.println(calcKey(i3,node.id) + " " + r3);
		
		Collections.sort(check);
		
		
		for(int i = 0; i < test; i++) {
			node = tree.deleteMin();
			if(check.get(i) == r1) {
			//if(Long.compare(check.get(i), r1) == 0) {
				//System.out.println(i+" "+check.get(i));
				i++;
				//System.out.println(i+" "+check.get(i));
			}
			if(check.get(i) == r2) {
				i++;
			}
			if(check.get(i) == r3) {
				i++;
			}
			if(node.key != check.get(i)) {
				System.out.println("Error "+i+" "+node.key+" "+check.get(i));
			}
		}
		
		System.out.println("Done");
	}

}
