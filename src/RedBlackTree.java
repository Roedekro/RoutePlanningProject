import tool.RedBlackNode;

/**
 * Based on Introduction to Algorithms third edition chapter 13.
 * @author Martin
 *
 */
public class RedBlackTree {
	
	public RedBlackNode nilNode = new RedBlackNode(-1L,0,0); // Key/ID = -1
	public RedBlackNode root;
	public long size = 0;
	private int shift = 33;
	
	public RedBlackTree() {
		nilNode.key = -1L;
		root = nilNode;
		nilNode.parent = nilNode;
		nilNode.leftChild = nilNode;
		nilNode.rightChild = nilNode;
	}
	
	public void insertNode(RedBlackNode node) {

		size++;
		RedBlackNode newParent = nilNode;
		RedBlackNode search = root;
		// Find placement
		while(search.id != -1L) {
			newParent = search;
			if(node.key < search.key) {
				search = search.leftChild;
			}
			else {
				search = search.rightChild;
			}
		}
		node.parent = newParent;
		if(newParent.id == -1L) {
			root = node;
		}
		else if(node.key < newParent.key) {
			newParent.leftChild = node;
		}
		else {
			newParent.rightChild = node;
		}
		node.leftChild = nilNode;
		node.rightChild = nilNode;
		node.colour = true;
			
		insertFixup(node);
	}
	
	private void insertFixup(RedBlackNode node) {
		// Need to fix the balance of the tree after insert.
		// 1) Root must be black.
		// 2) A red node has only black children.
		
		// While we have a red parent
		while(node.parent.colour) {
			
			// If parent is a left child
			if(node.parent.id == node.parent.parent.leftChild.id) {
				RedBlackNode parentsSibling = node.parent.parent.rightChild;
				if(parentsSibling.colour) {
					node.parent.colour = false;
					parentsSibling.colour = false;
					node.parent.parent.colour = true;
					node = node.parent.parent;
				}
				else {
					if(node.id == node.parent.rightChild.id) {
						node = node.parent;
						leftRotate(node);
					}
					node.parent.colour = false;
					node.parent.parent.colour = true;
					rightRotate(node.parent.parent);
				}	
			}
			else {
				// Parent is a right child
				RedBlackNode parentsSibling = node.parent.parent.leftChild;
				if(parentsSibling.colour) {
					node.parent.colour = false;
					parentsSibling.colour = false;
					node.parent.parent.colour = true;
					node = node.parent.parent;
				}
				else {
					if(node.id == node.parent.leftChild.id) {
						node = node.parent;
						rightRotate(node);
					}
					node.parent.colour = false;
					node.parent.parent.colour = true;
					leftRotate(node.parent.parent);
				}
				
			}			
		}
		
		root.colour = false;
	}
	
	private void leftRotate(RedBlackNode x) {
		
		RedBlackNode y = x.rightChild;
		x.rightChild = y.leftChild;
		if(y.leftChild.id != -1L) {
			y.leftChild.parent = x;
		}
		y.parent = x.parent;
		if(x.parent.id == -1L) {
			root = y;
		}
		else if(x.id == x.parent.leftChild.id) {
			x.parent.leftChild = y;
		}
		else {
			x.parent.rightChild = y;
		}
		y.leftChild = x;
		x.parent = y;
		
	}
	
	private void rightRotate(RedBlackNode x) {
		
		RedBlackNode y = x.leftChild;
		x.leftChild = y.rightChild;
		if(y.rightChild.id != -1L) {
			y.rightChild.parent = x;
		}
		y.parent = x.parent;
		if(x.parent.id == -1L) {
			root = y;
		}
		else if(x.id == x.parent.rightChild.id) {
			x.parent.rightChild = y;
		}
		else {
			x.parent.leftChild = y;
		}
		y.rightChild = x;
		x.parent = y;
		
	}
	
	/**
	 * @param node Root of (sub)tree
	 * @return Minimum node of (sub)tree
	 */
	private RedBlackNode minimum(RedBlackNode node) {
		RedBlackNode ret = node;
		while(ret.leftChild.id != -1L) {
			ret = ret.leftChild;
		}
		return ret;
	}
	
	private void transplant(RedBlackNode u, RedBlackNode v) {		
		if(u.parent.id == -1L) {
			root = v;
		}
		else if(u.parent.leftChild.id == u.id) {
			u.parent.leftChild = v;
		}
		else {
			u.parent.rightChild = v;
		}
		v.parent = u.parent;	
	}
	
	private void deleteNode(RedBlackNode z) {
		
		size--;
		RedBlackNode y = z;
		RedBlackNode x;
		boolean originalColour = y.colour;
		if(z.leftChild.id == -1L) {
			x = z.rightChild;
			transplant(z,z.rightChild);
		}
		else if(z.rightChild.id == -1L) {
			x = z.leftChild;
			transplant(z,z.leftChild);
		}
		else {
			
			y = minimum(z.rightChild);
			originalColour = y.colour;
			x = y.rightChild;
			if(y.parent.id == z.id) {
				x.parent = y;
			}
			else {
				transplant(y,y.rightChild);
				y.rightChild = z.rightChild;
				y.rightChild.parent = y;
			}
			transplant(z,y);
			y.leftChild = z.leftChild;
			y.leftChild.parent = y;
			y.colour = z.colour;
		}
		
		if(originalColour == false) {
			deleteFixup(x);
		}	
	}
	
	private void deleteFixup(RedBlackNode x) {
		
		// Push node upwards until we meet a red node or root
		while(x.id != root.id && !x.colour) {
			if(x.id == x.parent.leftChild.id) {
				// Left child
				RedBlackNode w = x.parent.rightChild;
				if(w.colour) {
					w.colour = false;
					x.parent.colour = true;
					leftRotate(x.parent);
					w = x.parent.rightChild;
				}
				
				if(!w.leftChild.colour && !w.rightChild.colour) {
					w.colour = true;
					x = x.parent;
				}
				else {
					if(!w.rightChild.colour) {
						w.leftChild.colour = false;
						w.colour = true;
						rightRotate(w);
						w = x.parent.rightChild;
					}
					w.colour = x.parent.colour;
					x.parent.colour = false;
					w.rightChild.colour = false;
					leftRotate(x.parent);
					x = root; // Break
				}
			}
			else {
				// Right child
				RedBlackNode w = x.parent.leftChild;
				if(w.colour) {
					w.colour = false;
					x.parent.colour = true;
					rightRotate(x.parent);
					w = x.parent.leftChild;
				}
				
				if(!w.leftChild.colour && !w.rightChild.colour) {
					w.colour = true;
					x = x.parent;
				}
				else {
					if(!w.leftChild.colour) {
						w.rightChild.colour = false;
						w.colour = true;
						leftRotate(w);
						w = x.parent.leftChild;
					}
					w.colour = x.parent.colour;
					x.parent.colour = false;
					w.leftChild.colour = false;
					rightRotate(x.parent);
					x = root; // Break
				}
			}
		}
		x.colour = false;
	}
	
	public RedBlackNode deleteMin() {
		RedBlackNode ret = minimum(root);
		deleteNode(ret);
		return ret;
	}
	
	/**
	 * Returns the node with key, or nilNode if not present
	 * @param key
	 * @return
	 */
	public RedBlackNode find(long key) {
		RedBlackNode ret = root;
		while(ret.key != key && ret.key != -1L) {
			if(key < ret.key) {
				ret = ret.leftChild;
			}
			else {
				ret = ret.rightChild;
			}
		}
		return ret;
	}
	
	/*public RedBlackNode findSuccessor(long key) {
		RedBlackNode ret = root;
		
	}*/
	
	/**
	 * Decreases the key of a given node by deleting it, setting its key equal to k
	 * shifted 33 bits left + id, and finally inserting it again.
	 * @param node
	 */
	public void decreaseKey(RedBlackNode node, long newPathLenght) {
		deleteNode(node);
		long newKey = calcKey(newPathLenght,node.id);
		node.key = newKey;
		insertNode(node);
	}
	
	// Shift val shift places to the left to make space for ID of 8bil.
	private long calcKey(long newPathLenght, long id) {
		long ret = newPathLenght;
		ret = ret << shift;
		ret = ret+id;
		return ret;
	}
	
	public void checkTree() {
		checkNode(root);
	}
	
	private void checkNode(RedBlackNode node) {
		if(node.colour) {
			if(node.leftChild.colour || node.rightChild.colour) {
				System.out.println("Error colour "+node.id+" "+node.colour+" "+node.leftChild.colour+" "+node.rightChild.colour);
			}
		}
		if(node.leftChild.id != -1L) {
			if(node.key <= node.leftChild.key) {
				System.out.println("Heap order invalid, left child larger "+node.id+" "+node.key+" "+node.leftChild.key);
			}
		}
		if(node.rightChild.id != -1L) {
			if(node.key > node.rightChild.key) {
				System.out.println("Heap order invalid, right child smaller "+node.id+" "+node.key+" "+node.rightChild.key);
			}
		}
		if(node.id != root.id) {
			if(node.key < node.parent.key) {
				if(node.parent.leftChild.id != node.id) {
					System.out.println("Wrong parent "+node.id);
				}
			}
			else {
				if(node.parent.rightChild.id != node.id) {
					System.out.println("Wrong parent "+node.id);
				}
			}
		}
		
		if(node.leftChild.id != -1L) {
			checkNode(node.leftChild);
		}
		if(node.rightChild.id != -1L) {
			checkNode(node.rightChild);
		}
	}
	
	public void printTree() {
		printNode(root);
	}
	
	private void printNode(RedBlackNode node) {
		System.out.println(node.id + " "+node.parent.id+" "+node.leftChild.id+" "+node.rightChild.id + " "+node.colour);
		if(node.leftChild.id != -1L) {
			printNode(node.leftChild);
		}
		if(node.rightChild.id != -1L) {
			printNode(node.rightChild);
		}
	}
}
