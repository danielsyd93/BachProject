package BachProject;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class NewModel {


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