import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import elements.Edge;
import tool.*;

public class Main {

	private static int shift = 33;
	
	public static void main(String[] args) {
		//testRedBlackTree();
		//testRedBlackTree2();
		
		
		File file = new File("Roedekro");
		System.out.println(file.exists());
		if(file.exists()) {
			try {
				dijkstraTestWithChecks("Roedekro");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// Tests Dijkstra and constantly monitors the red black tree.
	// Very costly.
	public static void dijkstraTestWithChecks(String input) throws FileNotFoundException, IOException {
		
		Tool tool = new Tool();
		ArrayList<RedBlackNode> nodes = tool.getRedBlackNodes(input);
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
		
		RedBlackTree tree = new RedBlackTree();
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
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
		
		// Dijkstra
		node = sourceNode;
		while(node.id != targetNode.id) {
			node = tree.deleteMin();
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
