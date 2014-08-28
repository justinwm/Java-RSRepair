package ca.uwaterloo.ece.qhanam.jrsrepair;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Set;

/**
 * Stores a list of statements and their weights. Randomly selects statements (with probabilities
 * determined by their weights).
 * 
 * Seed statements and faulty statements inserted here should have already been filtered according
 * to statement coverage. Seed statements should be in the execution path of one or more test cases,
 * while faulty statements should be in the execution path of one or more faulty test cases.
 * 
 * TODO: Add functionality to support filtering seed statements by variable scope.
 * 
 * @author qhanam
 *
 */
public class Statements {
	
	private NavigableMap<Double, Statement> statements;
	private double totalWeight; // We track the total weight so that we can randomly select a statement with weighting.

	public Statements() { 
		this.statements = new TreeMap();
		this.totalWeight = 0;
    }
	
	/**
	 * Add a statement to our statement maps. The statements are inserted according to their weight.
	 * @param s The statement to insert.
	 * @param weight The weight used to calculate the probability of this statement being selected.
	 * @param faulty Indicates the statement should be stored in the faulty statement list as well.
	 */
	public void addStatement(Statement s, double weight){
        this.totalWeight += weight;
        this.statements.put(this.totalWeight, s);
	}
	
	/**
	 * Randomly selects and returns a faulty statement (to be mutated).
	 * @return The faulty statement to be mutated.
	 */
	public Statement getRandomStatement(){
		/* Compute a random spot. */
		double random = Math.random() * this.totalWeight;
		
		/* Find the statement at that random spot. */
		return this.statements.ceilingEntry(random).getValue();
	}
	
	/**
	 * Returns a string containing the statements in this set and their weights.
	 */
	@Override
	public String toString(){
		String s = "";
		Set<NavigableMap.Entry<Double, Statement>> entrySet = statements.entrySet();
		for(NavigableMap.Entry<Double, Statement> entry : entrySet){
			s += entry.getKey() + " : " + entry.getValue() + "\n";
		}
		return s;
	}
}