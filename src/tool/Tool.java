package tool;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import elements.Node;

/**
 * Tool for reading in Node files.
 * @author Martin
 *
 */
public class Tool {

	int B = 8192;
	
	/**
	 * Reads in a file of Nodes and returns them as an ArrayList.
	 * @param input File of Nodes
	 * @return ArrayList<Node>
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ArrayList<Node> getNodes(String input) throws FileNotFoundException, IOException {
		
		ArrayList<Node> ret = new ArrayList<Node>();
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(new FileInputStream(input),B));
		Node node = null;
		while(true) {
			try {
				node = (Node) oin.readObject();
				ret.add(node);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// EOF
				break;
			}
		}
		oin.close();
	
		return ret;
	}
	
	/**
	 * Reads in a file of Nodes, wraps them into RedBlackNodes, and returns them as an ArrayList.
	 * @param input File of Nodes
	 * @return ArrayList<RedBlackNode>
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ArrayList<RedBlackNode> getRedBlackNodes(String input) throws FileNotFoundException, IOException {
		
		ArrayList<RedBlackNode> ret = new ArrayList<RedBlackNode>();
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(new FileInputStream(input),B));
		Node node = null;
		int i = 0;
		while(true) {
			i++;
			try {
				System.out.println(i);
				node = (Node) oin.readObject();
				ret.add(new RedBlackNode(node));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// EOF
				break;
			}
		}
		oin.close();
	
		return ret;
	}
	
	
}
