package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.util.List;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


/**
 * Handles new ASTs and variable bindings that are generated by parsing a
 * list of source code files. Inserts the statements in the ASTs into a
 * Statements object if they are in the set of potential seed statements
 * or faulty statements (determined by statement coverage of the test
 * cases).
 * @author qhanam
 *
 */
public class MutationASTRequestor extends FileASTRequestor {
	
    private LineCoverage faultyLineCoverage;
    private LineCoverage seedLineCoverage;
	private Statements faultyStatements;
	private Statements seedStatements;
	
	/**
	 * TODO: We need to also accept lists of faulty statements and seed statements from
	 * 		 code coverage analysis of the passing and failing test cases.
	 * @param faultyStatements
	 * @param seedStatements
	 */
	public MutationASTRequestor(LineCoverage faultyLineCoverage, LineCoverage seedLineCoverage, Statements faultyStatements, Statements seedStatements){
		this.faultyLineCoverage = faultyLineCoverage;
		this.seedLineCoverage = seedLineCoverage;
		this.faultyStatements = faultyStatements;
		this.seedStatements = seedStatements;
	}
	
	public void acceptBinding(String bindingKey, IBinding binding) { }

	/**
	 * Handles new ASTs that are generated from parsing a list of source
	 * code files.
	 * 
	 * TODO: Store the test case coverage as member variables. Need to be read from a file (produced by JaCoCo).
	 */
	public void acceptAST(String sourceFilePath, CompilationUnit cu) { 
        /* Store the statements that are covered by test cases. */
        StatementASTVisitor statementASTVisitor = new StatementASTVisitor();
        cu.accept(statementASTVisitor);

		/* A demo of how to get variables in a class scope. */
		cu.accept(new VarASTVisitor());
	}
	
	/**
	 * Adds statements to the statement lists if they are in the faulty/seed
	 * statement coverage lists.
	 * @author qhanam
	 *
	 */
	private class StatementASTVisitor extends ASTVisitor {
		
		/**
		 * Checks a statement against the coverage lists and inserts valid statements.
		 * 
		 * @param s
		 */
		private void insertStatement(Statement s){
			/* Build the LCNode to check if this statement is in the line coverage. */
			CompilationUnit cu = (CompilationUnit) s.getRoot();
            String packageName = cu.getPackage().getName().toString();
//            String className = ((IClassFile)cu.getTypeRoot()).getType().getFullyQualifiedName();
            String className = cu.types().get(0).toString();
			int lineNumber = cu.getLineNumber(s.getStartPosition());
			LCNode node = new LCNode(packageName, className, lineNumber);
			
			/* Check if this statement has been covered. If so add it to the appropriate statement 
			 * list with its weight. */
			Double weight;
			if((weight = MutationASTRequestor.this.faultyLineCoverage.contains(node)) != null){
                MutationASTRequestor.this.faultyStatements.addStatement(s, weight);
			}
			if((weight = MutationASTRequestor.this.seedLineCoverage.contains(node)) != null){
                MutationASTRequestor.this.seedStatements.addStatement(s, 1);
			}
		}
		
		/**
		 * We need to handle all subtypes of Statement.
		 */
		public boolean visit(AssertStatement node){insertStatement(node); return false;}
	}
	
    /** 
     * Prints all the variables declared in the AST. This is useful for:
     * 	1. Finding the variables used in a statement.
     * 	2. Finding the variables used in a class.
     * 
     *  TODO: To be more precise, we should handle member variables and method
     *   	  variables separately (i.e., omit local variables from methods we 
     *   	  are not mutating).
     */
	private class VarASTVisitor extends ASTVisitor{
		public boolean visit(VariableDeclarationFragment var) {
			System.out.println("variable: " + var.getName());

			return false;
		}

		public boolean visit(MethodDeclaration md) {

			if (md.getName().toString().equals("method_test2")) {
				md.accept(new ASTVisitor() {
					public boolean visit(VariableDeclarationFragment fd) {
						System.out.println("in method: " + fd);
						return false;
					}
				});
			}
			return false;

		}
	}
}