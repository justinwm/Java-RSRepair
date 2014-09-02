package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.text.edits.*;

public class ReplacementMutation extends Mutation {
	
	private ASTNode replacementNode;	// The copied seed statement that replaced the faulty statement
	
	public ReplacementMutation(HashMap<String, DocumentASTRewrite> sourceFileContents, SourceStatement faulty, SourceStatement seed){
		super(sourceFileContents, faulty, seed);
	}

	/**
	 * Replaces the faulty statement with the seed statement.
	 */
	@Override
	public void concreteMutate() throws Exception {
		ASTNode parent = faulty.statement.getParent();
		
		/* Start by assuming all parents are block statements. Later we can search for an ancestor that
		 * is a Block statement */
		AST ast = faulty.statement.getRoot().getAST();

		System.out.println("Applying replacement mutation...");

		if(parent instanceof Block){

            /* Make a copy of the seed statement and base it in the faulty statement's AST. */
            this.replacementNode = ASTNode.copySubtree(ast, seed.statement);
            
            /* Replace the faulty statement with the seed statement. */
            rewrite.replace(faulty.statement, this.replacementNode, null);
            
            /* Modify the source code file. */
            this.docrwt.resetModifiedDocument(); // Start with the original document to avoid the AST-doesn't-match-doc error.
            TextEdit edits = rewrite.rewriteAST(this.docrwt.modifiedDocument, null);
            edits.apply(this.docrwt.modifiedDocument, TextEdit.NONE);
		}
	}
	
	/**
	 * Replace the seed statement with the faulty statement.
	 */
	@Override
	public void concreteUndo() throws Exception{
        /* Undo the edit to the AST. */
		this.rewrite.replace(this.replacementNode, this.faulty.statement, null);

		/* We need to write the undo changes back to the source file because of recursion. */
        this.docrwt.resetModifiedDocument(); // Start with the original document to avoid the AST-doesn't-match-doc error.
        TextEdit edits = rewrite.rewriteAST(this.docrwt.modifiedDocument, null);
        edits.apply(this.docrwt.modifiedDocument, TextEdit.NONE);
	}
}
