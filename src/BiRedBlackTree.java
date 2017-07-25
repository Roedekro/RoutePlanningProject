/**
 * Based on Introduction to Algorithms third edition chapter 13.
 * @author Martin
 *
 */
public class BiRedBlackTree {
	
	public BiRedBlackNode nilNode = new BiRedBlackNode(-1L,0,0); // key2/ID = -1
	public BiRedBlackNode root;
	public long size = 0;
	private int shift = 33;
	
	public BiRedBlackTree() {
		nilNode.key2 = -1L;
		nilNode.key = -1L;
		nilNode.id = -1L;
		root = nilNode;
		nilNode.parent2 = nilNode;
		nilNode.leftChild2 = nilNode;
		nilNode.rightChild2 = nilNode;
	}
	
	public void insertNode(BiRedBlackNode node) {

		size++;
		BiRedBlackNode newparent2 = nilNode;
		BiRedBlackNode search = root;
		// Find placement
		while(search.id != -1L) {
			newparent2 = search;
			if(node.key2 < search.key2) {
				search = search.leftChild2;
			}
			else {
				search = search.rightChild2;
			}
		}
		node.parent2 = newparent2;
		if(newparent2.id == -1L) {
			root = node;
		}
		else if(node.key2 < newparent2.key2) {
			newparent2.leftChild2 = node;
		}
		else {
			newparent2.rightChild2 = node;
		}
		node.leftChild2 = nilNode;
		node.rightChild2 = nilNode;
		node.colour2 = true;
			
		insertFixup(node);
	}
	
	private void insertFixup(BiRedBlackNode node) {
		// Need to fix the balance of the tree after insert.
		// 1) Root must be black.
		// 2) A red node has only black children.
		
		// While we have a red parent2
		while(node.parent2.colour2) {
			
			// If parent2 is a left child
			if(node.parent2.id == node.parent2.parent2.leftChild2.id) {
				BiRedBlackNode parent2sSibling = node.parent2.parent2.rightChild2;
				if(parent2sSibling.colour2) {
					node.parent2.colour2 = false;
					parent2sSibling.colour2 = false;
					node.parent2.parent2.colour2 = true;
					node = node.parent2.parent2;
				}
				else {
					if(node.id == node.parent2.rightChild2.id) {
						node = node.parent2;
						leftRotate(node);
					}
					node.parent2.colour2 = false;
					node.parent2.parent2.colour2 = true;
					rightRotate(node.parent2.parent2);
				}	
			}
			else {
				// parent2 is a right child
				BiRedBlackNode parent2sSibling = node.parent2.parent2.leftChild2;
				if(parent2sSibling.colour2) {
					node.parent2.colour2 = false;
					parent2sSibling.colour2 = false;
					node.parent2.parent2.colour2 = true;
					node = node.parent2.parent2;
				}
				else {
					if(node.id == node.parent2.leftChild2.id) {
						node = node.parent2;
						rightRotate(node);
					}
					node.parent2.colour2 = false;
					node.parent2.parent2.colour2 = true;
					leftRotate(node.parent2.parent2);
				}
				
			}			
		}
		
		root.colour2 = false;
	}
	
	private void leftRotate(BiRedBlackNode x) {
		
		BiRedBlackNode y = x.rightChild2;
		x.rightChild2 = y.leftChild2;
		if(y.leftChild2.id != -1L) {
			y.leftChild2.parent2 = x;
		}
		y.parent2 = x.parent2;
		if(x.parent2.id == -1L) {
			root = y;
		}
		else if(x.id == x.parent2.leftChild2.id) {
			x.parent2.leftChild2 = y;
		}
		else {
			x.parent2.rightChild2 = y;
		}
		y.leftChild2 = x;
		x.parent2 = y;
		
	}
	
	private void rightRotate(BiRedBlackNode x) {
		
		BiRedBlackNode y = x.leftChild2;
		x.leftChild2 = y.rightChild2;
		if(y.rightChild2.id != -1L) {
			y.rightChild2.parent2 = x;
		}
		y.parent2 = x.parent2;
		if(x.parent2.id == -1L) {
			root = y;
		}
		else if(x.id == x.parent2.rightChild2.id) {
			x.parent2.rightChild2 = y;
		}
		else {
			x.parent2.leftChild2 = y;
		}
		y.rightChild2 = x;
		x.parent2 = y;
		
	}
	
	/**
	 * @param node Root of (sub)tree
	 * @return Minimum node of (sub)tree
	 */
	private BiRedBlackNode minimum(BiRedBlackNode node) {
		BiRedBlackNode ret = node;
		while(ret.leftChild2.id != -1L) {
			ret = ret.leftChild2;
		}
		return ret;
	}
	
	private void transplant(BiRedBlackNode u, BiRedBlackNode v) {		
		if(u.parent2.id == -1L) {
			root = v;
		}
		else if(u.parent2.leftChild2.id == u.id) {
			u.parent2.leftChild2 = v;
		}
		else {
			u.parent2.rightChild2 = v;
		}
		v.parent2 = u.parent2;	
	}
	
	private void deleteNode(BiRedBlackNode z) {
		
		size--;
		BiRedBlackNode y = z;
		BiRedBlackNode x;
		boolean originalcolour2 = y.colour2;
		if(z.leftChild2.id == -1L) {
			x = z.rightChild2;
			transplant(z,z.rightChild2);
		}
		else if(z.rightChild2.id == -1L) {
			x = z.leftChild2;
			transplant(z,z.leftChild2);
		}
		else {
			
			y = minimum(z.rightChild2);
			originalcolour2 = y.colour2;
			x = y.rightChild2;
			if(y.parent2.id == z.id) {
				x.parent2 = y;
			}
			else {
				transplant(y,y.rightChild2);
				y.rightChild2 = z.rightChild2;
				y.rightChild2.parent2 = y;
			}
			transplant(z,y);
			y.leftChild2 = z.leftChild2;
			y.leftChild2.parent2 = y;
			y.colour2 = z.colour2;
		}
		
		if(originalcolour2 == false) {
			deleteFixup(x);
		}	
	}
	
	private void deleteFixup(BiRedBlackNode x) {
		
		// Push node upwards until we meet a red node or root
		while(x.id != root.id && !x.colour2) {
			if(x.id == x.parent2.leftChild2.id) {
				// Left child
				BiRedBlackNode w = x.parent2.rightChild2;
				if(w.colour2) {
					w.colour2 = false;
					x.parent2.colour2 = true;
					leftRotate(x.parent2);
					w = x.parent2.rightChild2;
				}
				
				if(!w.leftChild2.colour2 && !w.rightChild2.colour2) {
					w.colour2 = true;
					x = x.parent2;
				}
				else {
					if(!w.rightChild2.colour2) {
						w.leftChild2.colour2 = false;
						w.colour2 = true;
						rightRotate(w);
						w = x.parent2.rightChild2;
					}
					w.colour2 = x.parent2.colour2;
					x.parent2.colour2 = false;
					w.rightChild2.colour2 = false;
					leftRotate(x.parent2);
					x = root; // Break
				}
			}
			else {
				// Right child
				BiRedBlackNode w = x.parent2.leftChild2;
				if(w.colour2) {
					w.colour2 = false;
					x.parent2.colour2 = true;
					rightRotate(x.parent2);
					w = x.parent2.leftChild2;
				}
				
				if(!w.leftChild2.colour2 && !w.rightChild2.colour2) {
					w.colour2 = true;
					x = x.parent2;
				}
				else {
					if(!w.leftChild2.colour2) {
						w.rightChild2.colour2 = false;
						w.colour2 = true;
						leftRotate(w);
						w = x.parent2.leftChild2;
					}
					w.colour2 = x.parent2.colour2;
					x.parent2.colour2 = false;
					w.leftChild2.colour2 = false;
					rightRotate(x.parent2);
					x = root; // Break
				}
			}
		}
		x.colour2 = false;
	}
	
	public BiRedBlackNode deleteMin() {
		BiRedBlackNode ret = minimum(root);
		deleteNode(ret);
		return ret;
	}
	
	/**
	 * Returns the node with key2, or nilNode if not present
	 * @param key2
	 * @return
	 */
	public BiRedBlackNode find(long key2) {
		BiRedBlackNode ret = root;
		while(ret.key2 != key2 && ret.key2 != -1L) {
			if(key2 < ret.key2) {
				ret = ret.leftChild2;
			}
			else {
				ret = ret.rightChild2;
			}
		}
		return ret;
	}
	
	/*public BiRedBlackNode findSuccessor(long key2) {
		BiRedBlackNode ret = root;
		
	}*/
	
	/**
	 * Decreases the key2 of a given node by deleting it, setting its key2 equal to k
	 * shifted 33 bits left + id, and finally inserting it again.
	 * @param node
	 */
	public void decreasekey(BiRedBlackNode node, long newPathLenght) {
		deleteNode(node);
		long newkey2 = calckey(newPathLenght,node.id);
		node.key2 = newkey2;
		insertNode(node);
	}
	
	// Shift val shift places to the left to make space for ID of 8bil.
	private long calckey(long newPathLenght, long id) {
		long ret = newPathLenght;
		ret = ret << shift;
		ret = ret+id;
		return ret;
	}
	
	public void checkTree() {
		checkNode(root);
	}
	
	private void checkNode(BiRedBlackNode node) {
		if(node.colour2) {
			if(node.leftChild2.colour2 || node.rightChild2.colour2) {
				System.out.println("Error colour2 "+node.id+" "+node.colour2+" "+node.leftChild2.colour2+" "+node.rightChild2.colour2);
			}
		}
		if(node.leftChild2.id != -1L) {
			if(node.key2 <= node.leftChild2.key2) {
				System.out.println("Heap order invalid, left child larger "+node.id+" "+node.key2+" "+node.leftChild2.key2);
			}
		}
		if(node.rightChild2.id != -1L) {
			if(node.key2 > node.rightChild2.key2) {
				System.out.println("Heap order invalid, right child smaller "+node.id+" "+node.key2+" "+node.rightChild2.key2);
			}
		}
		if(node.id != root.id) {
			if(node.key2 < node.parent2.key2) {
				if(node.parent2.leftChild2.id != node.id) {
					System.out.println("Wrong parent2 "+node.id);
				}
			}
			else {
				if(node.parent2.rightChild2.id != node.id) {
					System.out.println("Wrong parent2 "+node.id);
				}
			}
		}
		
		if(node.leftChild2.id != -1L) {
			checkNode(node.leftChild2);
		}
		if(node.rightChild2.id != -1L) {
			checkNode(node.rightChild2);
		}
	}
	
	public void printTree() {
		printNode(root);
	}
	
	private void printNode(BiRedBlackNode node) {
		System.out.println(node.id + " "+node.parent2.id+" "+node.leftChild2.id+" "+node.rightChild2.id + " "+node.colour2);
		if(node.leftChild2.id != -1L) {
			printNode(node.leftChild2);
		}
		if(node.rightChild2.id != -1L) {
			printNode(node.rightChild2);
		}
	}
}

