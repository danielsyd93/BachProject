package BachProject;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.*;
import ilog.cplex.*;

public class BaseModel {

	public static void main(String[] args) {
		int arc = 12;
		int comm = 20;
		int vert = 4;
		double[][] Dik = {{-1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 1, 1, 1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0}, {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1}};
		double[] ua = {20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20};
		double[][] Cak = {{100, 110, 120, 90, 80, 105, 110, 115, 99, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110}, 
							{105, 120, 100, 90, 90, 115, 80, 125, 80, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110},
							{100, 110, 120, 90, 80, 105, 110, 115, 99, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110}, 
							{105, 120, 100, 90, 90, 115, 80, 125, 80, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110},
							{100, 110, 120, 90, 80, 105, 110, 115, 99, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110}, 
							{105, 120, 100, 90, 90, 115, 80, 125, 80, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110},
							{100, 110, 120, 90, 80, 105, 110, 115, 99, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110}, 
							{105, 120, 100, 90, 90, 115, 80, 125, 80, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110},
							{100, 110, 120, 90, 80, 105, 110, 115, 99, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110}, 
							{105, 120, 100, 90, 90, 115, 80, 125, 80, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110},
							{100, 110, 120, 90, 80, 105, 110, 115, 99, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110}, 
							{105, 120, 100, 90, 90, 115, 80, 125, 80, 80, 95, 120, 115, 110, 100, 90, 95, 80, 100, 110}};
		double[] fa = {110, 50, 105, 90, 130, 90, 100, 90, 80, 110, 105, 70};
		double[][] dpi = {{4, 5, 10}, {1, 11, 6}, {12, 8, 3}, {2, 9, 7}};
		double[][] dni = {{1, 9, 8}, {2, 12, 5}, {11, 4, 7}, {6, 10, 3}};
		
		solveModel(arc, comm, vert, Dik, ua, Cak, fa, dpi, dni);
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
						System.out.println("Reduced cost " + (i+1) + " = " + model.getReducedCost(xak[i][j]));
					}
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

