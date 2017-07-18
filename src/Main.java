import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import tool.*;

public class Main {

	public static void main(String[] args) {
		testRedBlackTree();

	}
	
	public static void dijkstraTest(String input) throws FileNotFoundException, IOException {
		
		Tool tool = new Tool();
		ArrayList<RedBlackNode> nodes = tool.getRedBlackNodes(input);
		
		// Selection random source and target
		// Alternatively always use the same source and target
		Random random = new Random();
		long source = nodes.get(random.nextInt(nodes.size())).id;
		long target = nodes.get(random.nextInt(nodes.size())).id;
		
		
		
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
