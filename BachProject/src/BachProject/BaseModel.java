package BachProject;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.*;
import ilog.cplex.*;

public class BaseModel {

	public static void main(String[] args) {

	}
	public static void solveModel(int arc, int comm, int vert, double[][] Dik, double[] ua,
			double[][] Cak, double[] fa, double[][] dpi, double[][] dni) {
		try {
			IloCplex model = new IloCplex();
			
			IloNumVar[][] xak = new IloNumVar[arc][comm];
			for(int i = 0; i < arc; i++) {
				for(int j = 0; j < comm; j++) {
					xak[i][j] = model.numVar(0, Double.MAX_VALUE);	
				}
			}
			
			IloNumVar[] ya = new IloNumVar[arc];
			for(int i = 0; i < arc; i++) {
				ya[i] = model.numVar(0,1);
			}
			
			IloLinearNumExpr obj = model.linearNumExpr();
			for(int i = 0; i < arc; i++) {
				for(int j = 0; j < comm; j++) {
					obj.addTerm(Cak[i][j], xak[i][j]);
				}
			}
			for(int i = 0; i < arc; i++) {
				obj.addTerm(fa[i],ya[i]);
			}
			
			model.addMinimize(obj);
			
			List<IloRange> constraints = new ArrayList<IloRange>();
			for(int i = 0; i < m; i++) {
				IloLinearNumExpr constraint = model.linearNumExpr();
				for(int j = 0; j < n; j++) {
					constraint.addTerm(A[i][j], x[j]);
				}
				constraints.add(model.addGe(constraint, b[i]));
			}
			
			boolean isSolved = model.solve();
			if(isSolved) {
				double objValue = model.getObjValue();
				System.out.println("obj_val =" 	+ objValue);
				for(int i = 0; i < n; i++) {
					System.out.println("x[" + (i+1) + "] =" + model.getValue(x[i]));
					System.out.println("Reduced cost " + (i+1) + " = " + model.getReducedCost(x[i]));
				}
				
				for(int i = 0; i < m; i++) {
					double slack = model.getSlack(constraints.get(i));
					double dual = model.getDual(constraints.get(i));
					if(slack == 0) {
						System.out.println("Constraint " + (i+1) + "is binding.");
					}
					else {
						System.out.println("Constraint " + (i+1) + "is non-binding");
					}
					System.out.println("Shadow price " + (i+1) + " = " + dual);
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

