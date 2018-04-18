package BachProject;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class NewModel {


public static void solveModel(int arc, int comm, int vert, double[][] Dik, double[] ua,
		double[][] Cak, double[] fa, double[][] dpi, double[][] dni) {
	try {
		
		IloCplex model = new IloCplex();
		
		IloIntVar[][] xak = new IloIntVar[arc][comm];
		for(int i = 0; i < arc; i++) {
			for(int j = 0; j < comm; j++) {
				xak[i][j] = model.intVar(0, Integer.MAX_VALUE);	
			}
		}
		
		IloIntVar[] ya = new IloIntVar[arc];
		for(int i = 0; i < arc; i++) {
			ya[i] = model.intVar(0,1);
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
		int p = 1;
		int n = -1;
		
		List<IloRange> constraints = new ArrayList<IloRange>();
		for(int z = 0; z < vert; z++) {
			for(int i = 0; i < comm; i++) {
				System.out.println(i);
				System.out.println(dpi[z].length);
				System.out.println(dni[z].length);
				IloLinearNumExpr constraint = model.linearNumExpr();
				for(int j = 0; j < dpi[z].length; j++) {
					int parc = (int) dpi[z][j];
					constraint.addTerm(xak[parc-1][i], p);
				}
				for(int j = 0; j < dni[z].length; j++) {
					int narc = (int) dni[z][j];
					constraint.addTerm(xak[narc-1][i], n);
				}
				constraints.add(model.addEq(constraint, Dik[z][i]));
				System.out.println(constraints);
			}	
		}
		for(int i = 0; i < arc; i++) {
			IloLinearNumExpr constraint = model.linearNumExpr();
			for(int j = 0; j < comm; j++) {
				constraint.addTerm(p, xak[i][j]);
			}
			constraints.add((IloRange) model.addGe(model.prod(ua[i], ya[i]), constraint));
		}

		
		boolean isSolved = model.solve();
		if(isSolved) {
			double objValue = model.getObjValue();
			System.out.println("obj_val = " 	+ objValue);
			for(int i = 0; i < arc; i++) {
				for(int j = 0; j < comm; j++) {
					System.out.println("xak[" + (i+1) + "] [" + (j+1) + "] = " + model.getValue(xak[i][j]));
				}
				System.out.println("path " + (i+1) + " is " + model.getValue(ya[i]));
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