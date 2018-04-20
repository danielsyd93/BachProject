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
					num_sailing_arcs++;
					
					// If you want to use numbers in your code, you first need to convert them to integer or double type, for example:
					int from_port_id = Integer.parseInt(lineArray[0]);
					double cost = Double.parseDouble(lineArray[3]);	
					
				} else if(data_type == 3){
					
					// Reading a transshipment arc
					System.out.println("Arc from_port_id=" + lineArray[0] + ", to_port_id=" + lineArray[1] + ", duration=" + lineArray[2] + ", cost=" + lineArray[3]);
					num_transshipment_arcs++;
					
				} else if(data_type == 4){
					
					// Reading a commodity
					System.out.println("Commodity id=" + lineArray[0] + ", origin_port_id=" + lineArray[1] + ", destination_port_id=" + lineArray[2] + ", quantity=" + lineArray[3] + ", unit_revenue=" + lineArray[4]);
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
	}

public static void solveModel(int arc, int comm, int vert, double[][] AposSV, double[][] AnegSV, int arcS, int arcT, double[] costArcS, double[] costArcT, int vessal, 
		double[] containersK, double[] revenueK, double[] timeA, double[] MaxTimeK, int MaxCargo, double[][] Aposv, double[][] Anegv,
		double[] DestVertK, double[] OrigVertK) {
	try {
		
		IloCplex model = new IloCplex();
		
		IloIntVar[][] xka = new IloIntVar[arc][comm];
		IloIntVar[] ya = new IloIntVar[arc];
		for(int a = 0; a < arc; a++) {
			for(int k = 0; k < comm; k++) {
				xka[k][a] = model.intVar(0,1);	
			}
			ya[a] = model.intVar(0,1);
		}
		
		IloLinearNumExpr obj = model.linearNumExpr();
		for(int a = 0; a < arcS; a++) {
			obj.addTerm(costArcS[a],ya[a]);
		}
		
		for(int a = 0; a < arcT; a++) {
			for(int k = 0; k < comm; k++) {
				obj.addTerm(costArcS[a],xka[k][a]);
			}
		}
		for (int k = 0; k < comm; k++) {
			for(int v = 0; v < DestVertK.length; v++) {
				for(int a = 0; a < Anegv[v].length; a++) {
					double temp = (containersK[k]* revenueK[k]);
					obj.addTerm(xka[k][a], temp);
				}
			}
		}
		
		model.addMinimize(obj);
		
		
		
		List<IloRange> constraints = new ArrayList<IloRange>();
		for(int k = 0; k < comm; k++) {
			IloLinearNumExpr constraint1 = model.linearNumExpr();
			IloLinearNumExpr constraint2 = model.linearNumExpr();
			for(int v = 0; v < DestVertK.length; v++) {
				for(int a = 0; a < Anegv[v].length; a++) {
					constraint1.addTerm(xka[k][a], 1);
				}
				for(int a = 0; a < Aposv[v].length; a++) {
					constraint2.addTerm(xka[k][a], 1);
				}
			}
			constraints.add(model.addLe(constraint1, 1));
			constraints.add(model.addLe(constraint2, 0));
			for(int v = 0; v < vert; v++) {
				IloLinearNumExpr constraint = model.linearNumExpr();
				if(v != DestVertK[k] && v != OrigVertK[k]) {
					for(int a = 0; a < Aposv[v].length; a++) {
						constraint.addTerm(xka[k][a], 1);
					}
					for(int a = 0; a < Anegv[v].length; a++) {
						constraint.addTerm(xka[k][a], -1);
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
				constraint1.addTerm(ya[a], 1);
				constraint2.addTerm(ya[a], 1);
			}
			constraints.add(model.addLe(constraint1, 1));
			for(int a = 0; a < AnegSV[v].length; a++) {
				constraint2.addTerm(ya[a],-1);
			}
			constraints.add(model.addEq(constraint2, 0));
		}
		
		for(int a = 0; a < arcS; a++) {
			IloLinearNumExpr constraint = model.linearNumExpr();
			for(int k = 0; k < comm; k++) {
				constraint.addTerm(containersK[k],xka[k][a]);
			}
			constraints.add((IloRange) model.addLe(constraint, model.prod(MaxCargo, ya[a])));
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