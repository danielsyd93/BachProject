package BachProject;
import ilog.concert.*;
import ilog.cplex.*;

public class trial1 {
	public static void main(String[] args) {
		model();
		
	}
	
	public static void model() {
		try {
			IloCplex cplex = new IloCplex();
			
			IloNumVar x = cplex.numVar(0, Double.MAX_VALUE, "x");
			IloNumVar y = cplex.numVar(0, Double.MAX_VALUE, "y");
			
			IloLinearNumExpr objectiv = cplex.linearNumExpr();
			objectiv.addTerm(0.12, x);
			objectiv.addTerm(0.15, y);
			
			cplex.addMinimize(objectiv);
			
			cplex.addGe(cplex.sum(cplex.prod(60, x),cplex.prod(60, y)), 300);
			cplex.addGe(cplex.sum(cplex.prod(12, x),cplex.prod(6, y)), 36);
			cplex.addGe(cplex.sum(cplex.prod(10, x),cplex.prod(30, y)),90);
			
			cplex.solve();
			System.out.println("obj = "+cplex.getObjValue());
			System.out.println("x   = "+cplex.getValue(x));
			System.out.println("y   = "+cplex.getValue(y));
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
