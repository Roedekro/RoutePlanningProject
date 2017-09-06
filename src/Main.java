import java.awt.Color;
import java.io.BufferedWriter;
import java.io.Console;
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
		
		boolean test = true;
		test = false;
		if(test) {
			oldTests();
		}
		else {
			Console console = System.console();
			String in = null;
			String input = "Danmark";
			long source = 279727745L; // Ålborg rådhus
			long target = 3474332864L; // Københavns rådhus
			
			System.out.println("Type help for a list of commands");
			
			while(true) {
				in = console.readLine();
				String[] split = in.split(" ");
				int runs = 1;
				if(split[0].equalsIgnoreCase("help")) {
					System.out.println("Commands are:");
					System.out.println("dijkstra <runs> <write 0/1>");
					System.out.println("dijkstraBidirectional <runs> <write 0/1>");
					System.out.println("A* <runs> <write 0/1>");
					System.out.println("A*bidirectional <runs> <write 0/1>");
					System.out.println("ALT <runs> <#landmarks> <#landmarks to be used> <optimize> <type> <write 0/1>");
					System.out.println("ALTperfect <runs> <#landmarks> <#landmarks to be used> <optimize> <type> <write 0/1>");
					System.out.println("ALTmodified <runs> <#landmarks> <#landmarks to be used> <optimize> <type> <write 0/1>");
					System.out.println("ALTuni <runs> <#landmarks> <#landmarks to be used> <optimize> <type> <write 0/1>");
					System.out.println("CH <runs> <write 0/1>");
					System.out.println("CHnaive <runs>");
					System.out.println("Create <input> <output> <height> <width> <minLat> <maxLat> <minLon> <maxLon>");
					System.out.println("Paint <inputImage> <inputNodes> <output> <height> <width> <minLat> <maxLat> <minLon> <maxLon> <colour> (1=blue,2=red,3=green)");
					System.out.println("A*Distance <runs> <write 0/1>");
					System.out.println("ALTuniDistance <runs> <#landmarks> <#landmarks to be used> <optimize> <type> <write 0/1>");
				}
				else if(split[0].equalsIgnoreCase("exit")) {
					System.exit(0);
				}
				else if(split[0].equalsIgnoreCase("dijkstra")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[2]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						dijkstraDelayedInsert(input, source, target, runs, write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("dijkstraBidirectional")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[2]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						bidirectionalDijkstraDelayedInsert(input, source, target, runs, write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("A*")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[2]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						aStarEuclidian(input, source, target, runs,write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("A*bidirectional")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[2]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						aStarBiDirectionalEuclidian(input, source, target, runs,write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("ALT")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[6]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						ALT(input, source, target, runs, Integer.parseInt(split[2]), 
								Integer.parseInt(split[3]), Integer.parseInt(split[4]), 
								Integer.parseInt(split[5]),write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("ALTperfect")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[6]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						ALTperfect(input, source, target, runs, Integer.parseInt(split[2]), 
								Integer.parseInt(split[3]), Integer.parseInt(split[4]), 
								Integer.parseInt(split[5]),write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("ALTmodified")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[6]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						ALTWorksButShouldnt(input, source, target, runs, Integer.parseInt(split[2]), 
								Integer.parseInt(split[3]), Integer.parseInt(split[4]), 
								Integer.parseInt(split[5]),write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("ALTuni")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[6]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						ALTuni(input, source, target, runs, Integer.parseInt(split[2]), 
								Integer.parseInt(split[3]), Integer.parseInt(split[4]), 
								Integer.parseInt(split[5]),write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("CH")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[2]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						CHByPQ(input, source, target, runs,write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("CHnaive")) {
					try {
						runs = Integer.parseInt(split[1]);
						CHNaive(input, source, target, runs);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("Create")) {
					try {
						createImage(split[1], split[2], Integer.parseInt(split[3]),
								Integer.parseInt(split[4]), Double.parseDouble(split[5]),
								Double.parseDouble(split[6]), Double.parseDouble(split[7]), 
								Double.parseDouble(split[8]));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("Paint")) {
					try {
						int col = Integer.parseInt(split[10]);
						Color color;
						if(col == 1) {
							color = Color.BLUE;
						}
						else if(col == 2) {
							color = Color.RED;
						}
						else {
							color = Color.GREEN;
						}
						paintOnImage(split[1], split[2], split[3], Integer.parseInt(split[4]),
								Integer.parseInt(split[5]), Double.parseDouble(split[6]),
								Double.parseDouble(split[7]), Double.parseDouble(split[8]), 
								Double.parseDouble(split[9]), color);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("A*distance")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[2]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						aStarDistance(input, source, target, runs,write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("ALTuniDistance")) {
					try {
						runs = Integer.parseInt(split[1]);
						int writeInt = Integer.parseInt(split[6]);
						boolean write = false;
						if(writeInt > 0) {
							write = true;
						}
						ALTuniDistance(input, source, target, runs, Integer.parseInt(split[2]), 
								Integer.parseInt(split[3]), Integer.parseInt(split[4]), 
								Integer.parseInt(split[5]),write);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(split[0].equalsIgnoreCase("")) {
					
				}
				else if(split[0].equalsIgnoreCase(" ")) {
					
				}
				else {
					System.out.println("Unknown Command");
				}
			}
		}
		
	}
	
	private static void oldTests() {
		//testRedBlackTree();
		//testRedBlackTree2();
		String input = "Roedekro";
		long source = 2234623300L; // Birkeparken
		long target = 691716575L; // Lillevang
		int runs = 10;
		
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
				// Reverse search //CHNaive(input,target,source,runs);
				//CHNaive(input,source,target,runs);
				//CHByPQ(input, source, target, runs);
				createTextFileFromNodes();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void createImage(String input, String output, int height, int width, double minLat, 
			double maxLat,	double minLon, double maxLon) throws IOException {
		
		Tool tool = new Tool();
		tool.createAndFillImage(input, output, height, width, minLat, maxLat, minLon, maxLon);
	}
	
	public static void paintOnImage(String inputImage, String inputNodes, String output, int height, int width, double minLat, 
			double maxLat,	double minLon, double maxLon, Color color) throws IOException {
		
		Tool tool = new Tool();
		tool.drawOnImage(inputImage, inputNodes, output, height, width, minLat, maxLat, minLon, maxLon, color);
	}
	
	public static void createTextFileFromNodes() throws FileNotFoundException, IOException {
		
		Tool tool = new Tool();
		ArrayList<Node> nodes = tool.getNodesAsArrayList("Danmark");
		BufferedWriter out = new BufferedWriter(new FileWriter("DanmarkNodes.txt"));
		Node node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		out.write("end");
		out.flush();
		out.close();
	}
	
	public static void CHByPQ(String input, long source, long target, int runs, boolean write) throws FileNotFoundException, IOException {
		CH ch = new CH();
		ch.write = write;
		ArrayList<CHNode> nodes1 = ch.CHPreprocess(input);
		long ret = ch.CHContractionByPQ(nodes1, source, target, runs);
		System.out.println("CH by PQ finished");
		ArrayList<CHNode> nodes = ch.check;
		System.out.println(ret+" "+nodes.size()+" "+ch.nodesChecked);
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"CHbyPQ.txt"));
		CHNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+ch.preprocessTime);
		System.out.println("Query took "+ch.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "CH1.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "CH2.txt", "2.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.RED);
			paintOnImage("2.png", "DanmarkCHbyPQ.txt", "DKCH.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
	}
	
	public static void CHNaive(String input, long source, long target, int runs) throws FileNotFoundException, IOException {
		CH ch = new CH();
		long ret = ch.CHNaivebyNodeID(input, source, target, runs);
		ArrayList<CHNode> nodes = ch.check;
		System.out.println("CH Naive finished");
		System.out.println(ret+" "+nodes.size()+" "+ch.nodesChecked);
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"CHNaive.txt"));
		CHNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}			
		out.flush();
		out.close();
		System.out.println("Preprocess took "+ch.preprocessTime);
		System.out.println("Query took "+ch.queryTime);
	}
	
	//typeOfLandMark 1 = random, 2 = farthest, 3 = farthest optimized
	public static void ALT(String input, long source, long target, int runs, int k, int u, int o, int type, boolean write) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		alt.write = write;
		long ret = alt.ALTBidirectionalSearch(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"ALT.txt"));
		System.out.println("ALT finished");
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+alt.preprocessTime);
		System.out.println("Query took "+alt.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "ALT1.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "ALT2.txt", "2.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.RED);
			paintOnImage("2.png", "DanmarkALT.txt", "DKALT.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
	}
	
	public static void ALTperfect(String input, long source, long target, int runs, int k, int u, int o, int type, boolean write) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		alt.write = write;
		long ret = alt.ALTPerfectBidirectionalSearch(input, source, target, 1, 1, 1, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"ALTperfect.txt"));
		System.out.println("ALT finished");
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+alt.preprocessTime);
		System.out.println("Query took "+alt.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "ALT1.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "ALT2.txt", "2.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.RED);
			paintOnImage("2.png", "DanmarkALTperfect.txt", "DKALTperfect.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
	}
	
	public static void ALTSymmetric(String input, long source, long target, int runs, int k, int u, int o, int type) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		long ret = alt.ALTBidirectionalSearchSymmetric(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"ALTSymmetric.txt"));
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void ALTSymmetricLowerBound(String input, long source, long target, int runs, int k, int u, int o, int type) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		long ret = alt.ALTBidirectionalSearchSymmetricLowerBounding(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"ALTSymmetricLowerBound.txt"));
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void ALTWorksButShouldnt(String input, long source, long target, int runs, int k, int u, int o, int type,boolean write) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		alt.write = write;
		long ret = alt.ALTBidirectionalWorksButShouldnt(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"ALTWorksButShouldnt.txt"));
		System.out.println("ALT modified finished");
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}	
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+alt.preprocessTime);
		System.out.println("Query took "+alt.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "ALT1.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "ALT2.txt", "2.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.RED);
			paintOnImage("2.png", "DanmarkALTWorksButShouldnt.txt", "DKALTmod.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
	}
	
	public static void ALTuni(String input, long source, long target, int runs, int k, int u, int o, int type,boolean write) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		alt.write = write;
		long ret = alt.ALTUnidirectionalSearch(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"ALTuni.txt"));
		System.out.println("ALT unidirectional finished");
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}	
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+alt.preprocessTime);
		System.out.println("Query took "+alt.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "ALT1.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "DanmarkALTuni.txt", "DKALTuni.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
	}
	
	public static void ALTuniDistance(String input, long source, long target, int runs, int k, int u, int o, int type,boolean write) throws FileNotFoundException, IOException {
		ALT alt = new ALT();
		alt.write = write;
		long ret = alt.ALTUniDistance(input, source, target, k, u, o, type, runs);
		ArrayList<ALTNode> nodes = alt.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"ALTuniDistance.txt"));
		System.out.println("ALT unidirectional finished");
		System.out.println(ret + " " + nodes.size() + " " + alt.nodesChecked);
		ALTNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}	
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+alt.preprocessTime);
		System.out.println("Query took "+alt.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "ALT1.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "DanmarkALTuniDistance.txt", "DKALTuniDistance.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
	}
	
	public static void aStarEuclidian(String input, long source, long target, int runs, boolean write) throws FileNotFoundException, IOException {
		AStar aStar = new AStar();
		aStar.write = write;
		long ret = aStar.aStarEuclidian(input, source, target, runs);
		ArrayList<AStarNode> nodes = aStar.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"PathAStarEuclidian.txt"));
		System.out.println("A* finished");
		System.out.println(ret + " " + nodes.size() + " " + aStar.nodesChecked);
		AStarNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+aStar.preprocessTime);
		System.out.println("Query took "+aStar.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "AstarNodes.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "DanmarkPathAStarEuclidian.txt", "DKAstar.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
	}
	
	public static void aStarDistance(String input, long source, long target, int runs, boolean write) throws FileNotFoundException, IOException {
		AStar aStar = new AStar();
		aStar.write = write;
		long ret = aStar.aStarDistance(input, source, target, runs);
		ArrayList<AStarNode> nodes = aStar.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"PathAStarDistance.txt"));
		System.out.println("A* finished");
		System.out.println(ret + " " + nodes.size() + " " + aStar.nodesChecked);
		AStarNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+aStar.preprocessTime);
		System.out.println("Query took "+aStar.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "AstarNodes.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "DanmarkPathAStarDistance.txt", "DKAstarDistance.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
	}
	
	public static void aStarBiDirectionalEuclidian(String input, long source, long target, int runs, boolean write) throws FileNotFoundException, IOException {
		AStar aStar = new AStar();
		aStar.write = write;
		long ret = aStar.aStarBiDirectionalEuclidian(input, source, target, runs);
		ArrayList<AStarNode> nodes = aStar.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"PathAStarBiDirectionalEuclidian.txt"));
		System.out.println("A* bidirectional finished");
		System.out.println(ret + " " + nodes.size() + " " + aStar.nodesChecked);
		AStarNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+aStar.preprocessTime);
		System.out.println("Query took "+aStar.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "BidirectionalAstar1.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "BidirectionalAstar2.txt", "2.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.RED);
			paintOnImage("2.png", "DanmarkPathAStarBiDirectionalEuclidian.txt", "DKBidirecionalAstar.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
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
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}			
		out.flush();
		out.close();
	}
	
	public static void bidirectionalDijkstraDelayedInsert(String input, long source, long target, int runs, boolean write) throws FileNotFoundException, IOException {
		Dijkstra dijkstra = new Dijkstra();
		dijkstra.write = write;
		long ret = dijkstra.bidirectionalDijkstraDelayedInsert(input, source, target, runs);
		ArrayList<RedBlackNode> nodes = dijkstra.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"PathBiDelayed.txt"));
		System.out.println("Dijkstra Bidirectional Delayed Insert finished");
		System.out.println(ret + " " + nodes.size() + " " + dijkstra.nodesChecked);
		RedBlackNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		out.write("end");
		out.flush();
		out.close();
		System.out.println("Preprocess took "+dijkstra.preprocessTime);
		System.out.println("Query took "+dijkstra.queryTime);
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "BidirectionalDijkstraNodes1.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "BidirectionalDijkstraNodes2.txt", "2.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.RED);
			paintOnImage("2.png", "DanmarkPathBiDelayed.txt", "DKBidirecionalDijkstra.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
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
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
			node = node.path;
		}
		out.write(Long.toString(node.id));
		out.flush();
		out.close();
		
	}
	
	public static void dijkstraDelayedInsert(String input, long source, long target, int runs, boolean write) throws IOException {
		Dijkstra dijkstra = new Dijkstra();
		dijkstra.write = write;
		long ret = dijkstra.dijkstraDelayedInsert(input, source, target, runs);
		ArrayList<RedBlackNode> nodes = dijkstra.check;
		BufferedWriter out = new BufferedWriter(new FileWriter(input+"Path4.txt"));
		System.out.println("Dijkstra Delayed Insert finished");
		System.out.println(ret + " " + nodes.size() + " " + dijkstra.nodesChecked);
		RedBlackNode node = null;
		for(int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			out.write(node.id+" "+node.lat+" "+node.lon);
			out.newLine();
		}
		System.out.println("Preprocess took "+dijkstra.preprocessTime);
		System.out.println("Query took "+dijkstra.queryTime);
		out.write("end");
		out.flush();
		out.close();
		if(write) {
			paintOnImage("DanmarkBlackGray.png", "DijkstraNodes.txt", "1.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.BLUE);
			paintOnImage("1.png", "DanmarkPath4.txt", "DKDijkstra.png", 1284, 1880, 54.55, 57.76, 8, 12.7, Color.GREEN);
		}
		
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
