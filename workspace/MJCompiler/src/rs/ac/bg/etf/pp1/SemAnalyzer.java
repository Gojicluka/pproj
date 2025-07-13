package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import org.apache.log4j.Logger;

public class SemAnalyzer extends VisitorAdaptor {
    
    private boolean errorDetected = false;
    Logger log = Logger.getLogger(getClass());

    private Obj currentProgram = null;
    private Struct currentType = null;
    private int constant = 0;
    private Struct constantType = null;
    private Obj currentMethod = null;
    private boolean hasMain = false;
    private Obj currentClass = null;

    private Struct boolType = Tab.find("bool").getType();

    public void report_error(String message, SyntaxNode info) {
        errorDetected = true;
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0 : info.getLine();
        if (line > 0) {
            msg.append(" at line ").append(line).append(": ");
        }
        log.error(msg.toString());
    }

    public void report_info(String message, SyntaxNode info) {
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0 : info.getLine();
        if (line > 0) {
            msg.append(" at line ").append(line).append(": ");
        }
        log.info(msg.toString());
    }

    public boolean passed() {
        return !errorDetected;
    }

    // semantic pass

    @Override
    public void visit(Program program) {
        Tab.chainLocalSymbols(currentProgram);
        Tab.closeScope();
        currentProgram = null;

        if(!hasMain){
            report_error("Program must have a main method", program);
        }
    }

    @Override 
    public void visit(ProgramName programName) {
        currentProgram = Tab.insert(Obj.Prog, programName.getI1(), Tab.noType);
        Tab.openScope();
    }

    @Override
    public void visit(Type type) {
        Obj typeObj = Tab.find(type.getTypeName());

        if (typeObj == Tab.noObj) {
            report_error("Type " + type.getTypeName() + " is not defined", type);
        } else if (typeObj.getKind() != Obj.Type) {
            report_error(type.getTypeName() + " is not a type", type);
        } else {
            currentType = typeObj.getType();
        }
    }

    // ====================Const declarations====================

    @Override
    public void visit(ConstDecl constDecl) {
        Obj conObj = Tab.find(constDecl.getI1());
        if(conObj != Tab.noObj) {
            report_error("Constant " + constDecl.getI1() + " is already defined", constDecl);
        } else{
            if(constantType.assignableTo(currentType)){
                Obj conobj = Tab.insert(Obj.Con, constDecl.getI1(), currentType);
                conobj.setAdr(constant);
            }
            else{
                report_error("Constant " + constDecl.getI1() + " type mismatch", constDecl);
            }
        }
    }

    @Override
    public void visit(Constant_number number){
        constant = number.getN1();
        constantType = Tab.intType;
    }

    @Override
    public void visit(Constant_character chara){
        constant = chara.getC1();
        constantType = Tab.charType;
    }

    @Override
    public void visit(Constant_boolean bool_const){
        constant = bool_const.getB1() ? 1 : 0;
        constantType = boolType;
    }

    // ====================Var declarations====================
    @Override
    public void visit(VarDecl_regular varDecl) {
        Obj varObj = null;
        if(currentMethod == null && currentClass == null){
            varObj = Tab.find(varDecl.getI1());
        }else{
            varObj = Tab.currentScope().findSymbol(varDecl.getI1());
        }

        if(varObj == null || varObj == Tab.noObj) {
            if(currentClass == null){
                varObj = Tab.insert(Obj.Var, varDecl.getI1(), currentType);
            }else{
                varObj = Tab.insert(Obj.Var, varDecl.getI1(), currentType);
                varObj.setLevel(2);
            }

           
        } else{
            report_error("Variable " + varDecl.getI1() + " is already defined", varDecl);
        }
    }

    @Override
    public void visit(VarDecl_array varDecl) {
        Obj varObj = null;
        if(currentMethod == null && currentClass == null){
            varObj = Tab.find(varDecl.getI1());
        }else{
            varObj = Tab.currentScope().findSymbol(varDecl.getI1());
        }

        if(varObj == null || varObj == Tab.noObj) {
            if(currentClass == null){
                varObj = Tab.insert(Obj.Var, varDecl.getI1(), new Struct(Struct.Array, currentType));
            }else{
                varObj = Tab.insert(Obj.Var, varDecl.getI1(), new Struct(Struct.Array, currentType));
                varObj.setLevel(2);
            }
            
        } else{
            report_error("Variable " + varDecl.getI1() + " is already defined", varDecl);
        }
    }

    // ====================Method declarations====================
    // @Override
    // public void visit(MethodSigniture_Type methodSignature) {
    //     Obj methodObj = Tab.find(methodSignature.getI2());
    //     if(methodObj != Tab.noObj) {
    //         report_error("Method " + methodSignature.getI2() + " is already defined", methodSignature);
    //     } else{
    //         if(currentType == null) currentType = Tab.noType;
    //         currentMethod =  Tab.insert(Obj.Meth, methodSignature.getI2(), currentType);
    //         Tab.openScope();
    //     }
    // }

    @Override
    public void visit(MethodName methodName){
        Obj methodObj = Tab.find(methodName.getMethodname());
        if(methodObj != Tab.noObj) {
            report_error("Method " + methodName.getMethodname() + " is already defined", methodName);
        } else{
            if(currentType == null) currentType = Tab.noType;
            currentMethod =  Tab.insert(Obj.Meth, methodName.getMethodname(), currentType);
            Tab.openScope();
        }
    }

    @Override
    public void visit(MethodSigniture_Void methodSigniture){
        if(methodSigniture.getMethodName().getMethodname().equalsIgnoreCase("main")){
            hasMain = true;
        }
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        Tab.chainLocalSymbols(currentMethod);
        Tab.closeScope();
        currentMethod = null;
    }

    // ====================Form parameters====================

    @Override
    public void visit(FormalParamDecl_base formalParamDecl) {
        Obj varObj = null;
        if(currentMethod == null){
            report_error("Formal parameter " + formalParamDecl.getI2() + " is not defined", formalParamDecl);
        }else{
            varObj = Tab.currentScope().findSymbol(formalParamDecl.getI2());
        }

        if(varObj ==null || varObj == Tab.noObj){
            varObj = Tab.insert(Obj.Var, formalParamDecl.getI2(), currentType);
            varObj.setFpPos(1);
            currentMethod.setLevel(currentMethod.getLevel());
        }
        else{
            report_error("Formal parameter " + formalParamDecl.getI2() + " is already defined", formalParamDecl);
        }
    }

    @Override
    public void visit(FormalParamDecl_brakcet formalParamDecl) {
        Obj varObj = null;
        if(currentMethod == null){
            report_error("Formal parameter " + formalParamDecl.getI2() + " is not defined", formalParamDecl);
        }else{
            varObj = Tab.currentScope().findSymbol(formalParamDecl.getI2());
        }

        if(varObj ==null || varObj == Tab.noObj){
            varObj = Tab.insert(Obj.Var, formalParamDecl.getI2(), new Struct(Struct.Array, currentType));
            varObj.setFpPos(1);
            currentMethod.setLevel(currentMethod.getLevel());
        }
        else{
            report_error("Formal parameter " + formalParamDecl.getI2() + " is already defined", formalParamDecl);
        }
    }

    // ====================Class declarations====================

    @Override
    public void visit(ClassNameDeclaration classDecl) {
        Obj classObj = Tab.find(classDecl.getI1());
        if(classObj != Tab.noObj) {
            report_error("Class " + classDecl.getI1() + " is already defined", classDecl);
        } else{
            Struct struct = new Struct(Struct.Class);
            currentClass = Tab.insert(Obj.Type, classDecl.getI1(), struct);
            Tab.openScope();
            
            // Add "this" to symbol table for class context
            Obj thisObj = Tab.insert(Obj.Var, "this", currentClass.getType());
            thisObj.setLevel(0); // "this" reference level
        }
    }

    @Override
    public void visit(ClassDecl_base classDecl){
        Tab.chainLocalSymbols(currentClass);
        Tab.closeScope();
        currentClass = null;
    }

    @Override
    public void visit(ClassDecl_methodList classDecl){
        Tab.chainLocalSymbols(currentClass);
        Tab.closeScope();
        currentClass = null;
    }

    // Context conditisions

    @Override
    public void visit(Factor_const f){
        f.struct = constantType;
    }

    @Override 
    public void visit(Term_factor term){
        term.struct = term.getFactor().struct;
    }

    @Override
    public void visit(Term_mulop mulop){
        Struct t = mulop.getFactor().struct;
        if (t == Tab.intType){
            mulop.struct = t;
        }else{
            report_error("Operator " + mulop.getMulop().toString() + " cannot be applied to type " + t.getKind(), mulop);
            mulop.struct = Tab.noType;
        }
    }

    @Override
    public void visit(UnaryMinusExpr expr){
        Struct t = expr.getTerm().struct;
        if (t == Tab.intType){
            expr.struct = t;
        }
        else{
            report_error("Unary minus cannot be applied to type " + t.getKind(), expr);
            expr.struct = Tab.noType;
        }
    }

    @Override
    public void visit(Factor_designator fd){
        fd.struct = fd.getDesignator().obj.getType();
    }

    @Override
    public void visit(Factor_NewType_Epxr factor){
        if(factor.getExpr().struct.equals(Tab.intType)){
            factor.struct = new Struct(Struct.Array, currentType);
        }else{
            report_error("Array size must be an integer", factor);
            factor.struct = Tab.noType;
        }
    }

    @Override
    public void visit(Factor_Expr factor){
        factor.struct = factor.getExpr().struct;
    }

    // ====================Designator====================

    @Override
    public void visit(SimpleDesignator sd){
        Obj varObj = Tab.find(sd.getName());

        if(varObj == Tab.noObj){
            report_error("Variable " + sd.getName() + " is not defined", sd);
           sd.obj = Tab.noObj;
        }else if(varObj.getKind() != Obj.Var && varObj.getKind() != Obj.Con && varObj.getKind() != Obj.Meth){
            report_error("Variable " + sd.getName() + " is not a variable", sd);
            sd.obj = Tab.noObj;
        } 
        else {
            sd.obj = varObj;
        }
    }

    @Override
    public void visit(FieldAccess field_designator){
        // TODO 
    }

    @Override
    public void visit(ArrayAccess designator){
        if(designator.getDesignator().obj.getType().getKind() != Struct.Array) {
			report_error("Variable must be an array.", null);
			designator.obj = Tab.noObj;
			return;
		}

        if(designator.getExpr().struct != Tab.intType) {
			report_error("Size should be an integer.", null);
			designator.obj = Tab.noObj;
			return;
		}

        designator.obj = Tab.insert(
            Obj.Elem, designator.getDesignator().obj.getName(), 
            designator.getDesignator().obj.getType().getElemType()
        );
    }

    // Expr
}
