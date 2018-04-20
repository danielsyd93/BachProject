package BachProject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class NewModel {

	public static void main(String[] args) throws IOException{
		
		//String filepath = System.getProperty("user.dir") + "\\input_data\\";
		String filename = "Baltic_3P_4K.txt";
				
		readInputFile(filename);
	}
	
public static void readInputFile(String filename) throws IOException{
		
		System.out.print("Reading input file " + filename + "...");
		
		// Define separator that separates columns in the input file
		String sep = ";";
		
		// Read file and store file content in 'bf'
		FileReader fr = new FileReader(filename);
		BufferedReader bf = new BufferedReader(fr);
		
		String line = null;
		int lineCount = 0;
		String[] lineArray;	// will be filled with values of a single line (separated by separator 'sep' as defined above)
		
		// data_type defines the type of data the reader is currently reading
		// -1=none; 0=summary; 1=nodes; 2=sailing arcs; 3=transshipment arcs; 4=commodities
		int data_type = -1;
		
		// variables that will count the number of data elements that have been read in
		int num_nodes = 0;
		int num_sailing_arcs = 0;
		int num_transshipment_arcs = 0;
		int num_commodities = 0;
		
		// Read 'bf' line by line
		// At each iteration of the while loop the next line is read
		int count = 0;
		double[] costArcS1 = new double [812]; 
		double[] costArcT1 = new double [812];
		int[] containersK1 = new int [4];
		double[] revenueK1 = new double [4];
		int[] timeA1 = new int [812];
		int[] DestVertK1 = new int [4];
		int[] OrigVertK1 = new int [4];
		int[] arcS1 = new int [224];
		int[] arcT1 = new int [588];
		int to_port_id = 0;
		int from_port_id = 0;
		int indexpos = 0;
		int indexneg = 0;
		
		
		// Declare the 2D array list
		ArrayList<ArrayList<Integer>> AposSV2;

		// Create the 2D array list
		AposSV2 = new ArrayList<ArrayList<Integer>>();

		// Add an element to the first dimension
		for(int i = 0; i < 42; i++) {
			AposSV2.add(new ArrayList<Integer>());
		}
		
		// Declare the 2D array list
		ArrayList<ArrayList<Integer>> AnegSV2;

		// Create the 2D array list
		AnegSV2 = new ArrayList<ArrayList<Integer>>();

		// Add an element to the first dimension
		for(int i = 0; i < 42; i++) {
			AnegSV2.add(new ArrayList<Integer>());
		}
		
		// Declare the 2D array list
		ArrayList<ArrayList<Integer>> Aposv2;

		// Create the 2D array list
		Aposv2 = new ArrayList<ArrayList<Integer>>();

		// Add an element to the first dimension
		for(int i = 0; i < 42; i++) {
			Aposv2.add(new ArrayList<Integer>());
		}
		
		// Declare the 2D array list
		ArrayList<ArrayList<Integer>> Anegv2;

		// Create the 2D array list
		Anegv2 = new ArrayList<ArrayList<Integer>>();

		// Add an element to the first dimension
		for(int i = 0; i < 42; i++) {
			Anegv2.add(new ArrayList<Integer>());
		}



		
		
		while((line = bf.readLine()) != null){
			// Split the line based on the seperator 'sep' as defined above
			lineArray = line.split(sep);
			
			// Check if line is comment or header line (starting with "//")
			if(lineArray[0].length() > 1 && lineArray[0].substring(0,2).equals("//")){
				// If line starts with '//', identify which type of data will follow after that line
				if(lineArray[0].substring(0,8).equals("// Nodes")){
					data_type = 1;
				} else if(lineArray[0].substring(0,15).equals("// Sailing arcs")){
					data_type = 2;
				} else if(lineArray[0].substring(0,21).equals("// Transshipment arcs")){
					data_type = 3;
				} else if(lineArray[0].substring(0,14).equals("// Commodities")){
					data_type = 4;
				}
			} else {
				// If a line does NOT start with '//', we assume it contains data
				if(data_type == 1){
					
					// Reading a node
					System.out.println("Node id=" + lineArray[0] + ", port_id=" + lineArray[1] + ", time=" + lineArray[2]);
					num_nodes++;
					
				} else if(data_type == 2){
					
					// Reading a sailing arc
					System.out.println("Arc from_port_id=" + lineArray[0] + ", to_port_id=" + lineArray[1] + ", duration=" + lineArray[2] + ", cost=" + lineArray[3]);
					
					costArcS1[count] = Double.parseDouble(lineArray[3]);
					costArcT1[count] = 1000000000;
					
					// If you want to use numbers in your code, you first need to convert them to integer or double type, for example:
					
					to_port_id = Integer.parseInt(lineArray[1]);
					AposSV2.get(to_port_id).add(count);
					
					from_port_id = Integer.parseInt(lineArray[0]);
					AnegSV2.get(from_port_id).add(count);
					
					Aposv2.get(to_port_id).add(count);
					Anegv2.get(from_port_id).add(count);
					
					timeA1[count] = Integer.parseInt(lineArray[2]);
					
					arcS1[num_sailing_arcs] = count;
					
					num_sailing_arcs++;
					
					
					
					count++;
					
				} else if(data_type == 3){
					
					// Reading a transshipment arc
					System.out.println("Arc from_port_id=" + lineArray[0] + ", to_port_id=" + lineArray[1] + ", duration=" + lineArray[2] + ", cost=" + lineArray[3]);
					
					
					costArcT1[count] = Double.parseDouble(lineArray[3]);
					costArcS1[count] = 1000000000;
					
					timeA1[count] = Integer.parseInt(lineArray[2]);
					
					to_port_id = Integer.parseInt(lineArray[1]);
					
					from_port_id = Integer.parseInt(lineArray[0]);
					Aposv2.get(to_port_id).add(count);
					Anegv2.get(from_port_id).add(count);
					
					
					arcT1[num_transshipment_arcs] = count;
					
					num_transshipment_arcs++;
					
					count++;
				} else if(data_type == 4){
					
					// Reading a commodity
					System.out.println("Commodity id=" + lineArray[0] + ", origin_port_id=" + lineArray[1] + ", destination_port_id=" + lineArray[2]
							+ ", quantity=" + lineArray[3] + ", unit_revenue=" + lineArray[4]);
					
					containersK1[num_commodities] = Integer.parseInt(lineArray[3]);
					revenueK1[num_commodities] = Integer.parseInt(lineArray[4]);
					
					DestVertK1[num_commodities] = Integer.parseInt(lineArray[2]);
					OrigVertK1[num_commodities] = Integer.parseInt(lineArray[1]);
					
					num_commodities++;
				}
			}
				
			lineCount++;
		}
		
		// close file
		bf.close();
		
		
		System.out.println("Lines read: " + lineCount);
		System.out.println("# of nodes read in: " + num_nodes);
		System.out.println("# of sailing arcs read in: " + num_sailing_arcs);
		System.out.println("# of transshipment arcs read in: " + num_transshipment_arcs);
		System.out.println("# of commodities read in: " + num_commodities);
	
		System.out.println("Finished reading input file.");
		
		int arc = num_sailing_arcs + num_transshipment_arcs;
		int comm = num_commodities;
		int vert = num_nodes;
		//int[][] AposSV = AposSV1;
		//int[][] AnegSV = AnegSV1;
		int[] arcS = arcS1;
		int[] arcT = arcT1;
		double[] costArcS = costArcS1;
		double[] costArcT = costArcT1;
		int vessal = 4;
		int[] containersK = containersK1;
		double[] revenueK = revenueK1;
		int[] timeA = timeA1;
		double[] MaxTimeK = {240, 264, 240, 168};
		int MaxCargo = 450;
		//int[][] Aposv = Aposv1;
		//int[][] Anegv = Anegv1;
		int[] DestVertK = DestVertK1;
		int[] OrigVertK = OrigVertK1;
		
		
		//solveModel(arc, comm, vert, AposSV, AnegSV, arcS, arcT, costArcS, costArcT, vessal, containersK, 
		//		revenueK, timeA, MaxTimeK, MaxCargo, Aposv, Anegv, DestVertK, OrigVertK);
	}

public static void solveModel(int arc, int comm, int vert, int[][] AposSV, int[][] AnegSV, int[] arcS, int[] arcT, double[] costArcS, double[] costArcT, int vessal, 
		int[] containersK, double[] revenueK, int[] timeA, double[] MaxTimeK, int MaxCargo, int[][] Aposv, int[][] Anegv,
		int[] DestVertK, int[] OrigVertK) {
	try {
		
		IloCplex model = new IloCplex();
		
		IloIntVar[][] xka = new IloIntVar[comm][arc];
		for(int a = 0; a < arc; a++) {
			for(int k = 0; k < comm; k++) {
				xka[k][a] = model.intVar(0,1);	
			}
		}
		
		IloIntVar[] ya = new IloIntVar[arc];
		for(int a = 0; a < arcS.length; a++) {
			ya[a] = model.intVar(0,1);
		}
		
		IloLinearNumExpr obj = model.linearNumExpr();
		for(int a = 0; a < arcS.length; a++) {
			obj.addTerm(costArcS[arcS[a]],ya[arcS[a]]);
		}
		
		for(int a = 0; a < arcT.length; a++) {
			for(int k = 0; k < comm; k++) {
				obj.addTerm(costArcT[arcT[a]],xka[k][arcT[a]]);
			}
		}
		
		for (int k = 0; k < comm; k++) {
			for(int v = 0; v < DestVertK.length; v++) {
				for(int a = 0; a < Anegv[DestVertK[k]].length; a++) {
					double temp = (containersK[k]* revenueK[k]);
					obj.addTerm(xka[k][Anegv[DestVertK[k]][a]], temp);
				}
			}
		}
		
		model.addMinimize(obj);
		
		
		
		List<IloRange> constraints = new ArrayList<IloRange>();
		for(int k = 0; k < comm; k++) {
			IloLinearNumExpr constraint1 = model.linearNumExpr();
			IloLinearNumExpr constraint2 = model.linearNumExpr();
			for(int v = 0; v < DestVertK.length; v++) {
				for(int a = 0; a < Anegv[DestVertK[k]].length; a++) {
					constraint1.addTerm(xka[k][Anegv[DestVertK[k]][a]], 1);
				}
				for(int a = 0; a < Aposv[v].length; a++) {
					constraint2.addTerm(xka[k][Aposv[DestVertK[k]][a]], 1);
				}
			}
			constraints.add(model.addLe(constraint1, 1));
			constraints.add(model.addLe(constraint2, 0));
			for(int v = 0; v < vert; v++) {
				IloLinearNumExpr constraint = model.linearNumExpr();
				if(v != DestVertK[k] && v != OrigVertK[k]) {
					for(int a = 0; a < Aposv[v].length; a++) {
						constraint.addTerm(xka[k][Aposv[v][a]], 1);
					}
					for(int a = 0; a < Anegv[v].length; a++) {
						constraint.addTerm(xka[k][Anegv[v][a]], -1);
					}
				}
				constraints.add(model.addEq(constraint, 0));
			}
			IloLinearNumExpr constraint = model.linearNumExpr();
			for(int a = 0; a < arc; a++) {
				constraint.addTerm(timeA[a], xka[k][a]);
			}
			constraints.add(model.addLe(constraint, MaxTimeK[k]));
		}
		
		
		for(int v = 0; v < vert; v++) {
			IloLinearNumExpr constraint1 = model.linearNumExpr();
			IloLinearNumExpr constraint2 = model.linearNumExpr();
			for(int a = 0; a < AposSV[v].length; a++) {
				constraint1.addTerm(ya[AposSV[v][a]], 1);
				constraint2.addTerm(ya[AposSV[v][a]], 1);
			}
			constraints.add(model.addLe(constraint1, 1));
			for(int a = 0; a < AnegSV[v].length; a++) {
				constraint2.addTerm(ya[AnegSV[v][a]],-1);
			}
			constraints.add(model.addEq(constraint2, 0));
		}
		
		for(int a = 0; a < arcS.length; a++) {
			IloLinearNumExpr constraint = model.linearNumExpr();
			for(int k = 0; k < comm; k++) {
				constraint.addTerm(containersK[k],xka[k][arcS[a]]);
			}
			constraints.add((IloRange) model.addLe(constraint, model.prod(MaxCargo, ya[arcS[a]])));
		}
		
		
		
		boolean isSolved = model.solve();
		if(isSolved) {
			double objValue = model.getObjValue();
			System.out.println("obj_val = " 	+ objValue);
			for(int a = 0; a < arc; a++) {
				for(int k = 0; k < comm; k++) {
					System.out.println("xka[" + (k+1) + "] [" + (a+1) + "] = " + model.getValue(xka[k][a]));
				}
				System.out.println("path " + (a+1) + " is " + model.getValue(ya[a]));
			}
		}
		else {
			System.out.println("Model not solved");
		}
	}
	catch(IloException ex) {
		ex.printStackTrace();
	}		
}
}