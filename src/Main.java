import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tool.*;

public class Main {

	public static void main(String[] args) {
		//testRedBlackTree();
		try {
			dijkstraTest("boligKvarter");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void dijkstraTest(String input) throws FileNotFoundException, IOException {
		
		Tool tool = new Tool();
		ArrayList<RedBlackNode> nodes = tool.getRedBlackNodes(input);
		HashMap<Long,RedBlackNode> hashMap = new HashMap<Long,RedBlackNode>();
		
		// Selection random source and target
		// Alternatively always use the same source and target
		Random random = new Random();
		RedBlackNode source = nodes.get(random.nextInt(nodes.size()));
		RedBlackNode target = nodes.get(random.nextInt(nodes.size()));
		
		source.key = 0;
		
		RedBlackTree tree = new RedBlackTree();
		RedBlackNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			tree.insertNode(node);
			hashMap.put(node.id, node);
		}
		
		node = source;
		while(node.id != target.id) {
			node = tree.deleteMin();
			Edge edge = null;
			RedBlackNode decreaseNode = null;
			for(int i = 0; i < node.edges.size(); i++) {
				edge = node.edges.get(i);
				decreaseNode = hashMap.get(edge.nodeID);
				if(decreaseNode.path == null) {
					// No path means its still in the tree
					decreaseNode.path = node;
					decreaseNode.pathLength = edge.travelTime;
					tree.decreaseKey(decreaseNode, edge.travelTime);
				}
			}
		}
		
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"Path.txt"));
		
		// Run through path counting the length
		long pathLength = 0;
		while(node.id != source.id) {
			out.write(Long.toString(node.id));
			out.newLine();
			pathLength += node.pathLength;
			node = node.path;
		}
		
		out.flush();
		out.close();
		
		System.out.println("The travel time is "+pathLength);
		
		
		
	}
	
	// Shift val 33 places to the left to make space for ID of 8bil.
	private static long calcKey(int val, long id) {
		long ret = val;
		ret = ret << 33;
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

}
