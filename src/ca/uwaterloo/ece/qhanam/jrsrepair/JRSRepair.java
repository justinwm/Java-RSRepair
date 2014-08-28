package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class JRSRepair {
	
	File sourcePath;
	File faultyCoverageFile;
	File seedCoverageFile;
	
	/**
	 * Creates a JRSRepair object with the path to the source folder
	 * of the program we are mutating.
	 * @param sourcePath The path to the source folder of the program we are mutating.
	 */
	public JRSRepair(File sourcePath, File faultyCoverageFile, File seedCoverageFile){
		this.sourcePath = sourcePath;
		this.faultyCoverageFile = faultyCoverageFile;
		this.seedCoverageFile = seedCoverageFile;
	}
	
	/**
	 * Builds ASTs for all the source files.
	 */
	public void buildASTs() throws Exception{
		/* TODO: We have two options: 
		 * 	1. Set the environment using Eclipse java project and IJavaProject. 
		 *  2. Set the environment using ASTParser.setEnvironment. */
		
		/* We first need to get the set of source files for which to build ASTs. Which files
		 * we create ASTs for depends on:
		 * 	1. The potentially faulty statements from fault localization.
		 * 	2. The candidate seed statements.
		 * To start, we assume our candidate seed statements can come from any part of the
		 * program's source code (external libraries are excluded).
		 */

		Collection<File> sourceFiles;
		String[] sourceFilesArray;
		
		/* If the buggy file is a directory, get all the java files in that directory. */
		if(this.sourcePath.isDirectory()){
			sourceFiles = FileUtils.listFiles(this.sourcePath, new SuffixFileFilter(".java"), TrueFileFilter.INSTANCE);
			for (File javaFile : sourceFiles){
				System.out.println(javaFile);
			}
		}
		/* The buggy file may also be a source code file. */
		else{
			sourceFiles = new LinkedList<File>();
			sourceFiles.add(this.sourcePath);
		}
		
		/* Create the String array. */
		sourceFilesArray = new String[sourceFiles.size()];
		int i = 0;
		for(File sourceFile : sourceFiles){
			sourceFilesArray[i] = sourceFile.getCanonicalPath();
			i++;
		}
		
		/* Create the ASTParser with the source files to generate ASTs for, and set up the
		 * environment using ASTParser.setEnvironment.
		 */
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		/* setEnvironment(
		 * String[] classpathEntries,
		 * String[] sourcepathEntries, 
		 * String[] encodings,
		 * boolean includeRunningVMBootclasspath)
		 */
		parser.setEnvironment(new String[] {}, sourceFilesArray, null, false);
		parser.setResolveBindings(false); // Throws an error when set to 'true' for some reason.
		
		/* Set up the AST handler. We need to create LineCoverage and Statements classes to store 
		 * and filter the statements from the ASTs. */
		LineCoverage faultyLineCoverage = new LineCoverage(this.faultyCoverageFile);
		LineCoverage seedLineCoverage = new LineCoverage(this.seedCoverageFile);
		Statements faultyStatements = new Statements();
		Statements seedStatements = new Statements();
		FileASTRequestor fileASTRequestor = new MutationASTRequestor(faultyLineCoverage, seedLineCoverage, faultyStatements, seedStatements);
		
		/* createASTs(
		 * String[] sourceFilePaths, 
		 * String[] encodings, - the source file encodings (e.g., "ASCII", "UTF8", "UTF-16"). Can be set to null if platform encoding is sufficient.
		 * String[] bindingKeys, 
		 * FileASTRequestor requestor, 
		 * IProgressMonitor monitor) 
		 */
		parser.createASTs(sourceFilesArray, null, new String[] {}, fileASTRequestor, null);
		
		/* Let's see what we get. */
		System.out.print("Faulty Statements:\n" + faultyStatements.toString() + "\n\n");
		System.out.print("Seed Statements:\n" + seedStatements.toString() + "\n\n");
	}
		
}