import java.util.ArrayList;
import java.util.Random;

public class ALT {

	
	
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
			if(!ret.contains(node)) {
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
			if(temp > farthest) {
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
			if(temp > farthest) {
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
			if(total > farthest) {
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
