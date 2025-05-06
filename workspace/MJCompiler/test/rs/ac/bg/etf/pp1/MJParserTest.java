package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.ast.Type;
import rs.ac.bg.etf.pp1.SemAnalyzer;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		Logger log = Logger.getLogger(MJParserTest.class);
		
		Reader br = null;
		try {
			File sourceCode = new File(args[0]);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
			
			log.info(s);

	        if (s == null || s.value == null) {
	            log.error("Parsing failed: No valid parse tree generated.");
	            return;
	        }

	        Program prog = (Program) s.value;

	        if (prog == null) {
	            log.error("Parsing failed: Program node is null.");
	            return;
	        }

			// Semanticka analiza
			
			Tab.init();
			Struct boolType = new Struct(Struct.Bool);
			Tab.currentScope.addToLocals(new Obj(Obj.Type, "bool", boolType));

			SemAnalyzer sa = new SemAnalyzer();
			prog.traverseBottomUp(sa);

			log.info("============================================");
			Tab.dump();


	        log.info(prog.toString(""));
	        log.info("============================================");

	        if (!p.errorDetected && sa.passed()) {
	            log.info("Parsing successfully completed!");
	        } else {
	            log.error("Parsing failed due to syntax errors.");
	        }
		}
				finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}
	}
}


// public static void main(String[] args) throws Exception {
// 	Logger log = Logger.getLogger(MJParserTest.class);
// 	if (args.length < 2) {
// 		log.error("Not enough arguments supplied! Usage: MJParser <source-file> <obj-file> ");
// 		return;
// 	}
	
// 	File sourceCode = new File(args[0]);
// 	if (!sourceCode.exists()) {
// 		log.error("Source file [" + sourceCode.getAbsolutePath() + "] not found!");
// 		return;
// 	}
		
// 	log.info("Compiling source file: " + sourceCode.getAbsolutePath());
	
// 	try (BufferedReader br = new BufferedReader(new FileReader(sourceCode))) {
// 		Yylex lexer = new Yylex(br);
// 		MJParser p = new MJParser(lexer);
// 		Symbol s = p.parse();  //pocetak parsiranja
// 		SyntaxNode prog = (SyntaxNode)(s.value);
		
// 		Tab.init(); // Universe scope
// 		SemanticPass semanticCheck = new SemanticPass();
// 		prog.traverseBottomUp(semanticCheck);
		
// 		log.info("Print calls = " + semanticCheck.printCallCount);
// 		Tab.dump();
		
// 		if (!p.errorDetected && semanticCheck.passed()) {
// 			File objFile = new File(args[1]);
// 			log.info("Generating bytecode file: " + objFile.getAbsolutePath());
// 			if (objFile.exists())
// 				objFile.delete();
			
// 			// Code generation...
// 			CodeGenerator codeGenerator = new CodeGenerator();
// 			prog.traverseBottomUp(codeGenerator);
// 			Code.dataSize = semanticCheck.nVars;
// 			Code.mainPc = codeGenerator.getMainPc();
// 			Code.write(new FileOutputStream(objFile));
// 			log.info("Parsiranje uspesno zavrseno!");
// 		}
// 		else {
// 			log.error("Parsiranje NIJE uspesno zavrseno!");
// 		}
// 		}
// 	}
// }