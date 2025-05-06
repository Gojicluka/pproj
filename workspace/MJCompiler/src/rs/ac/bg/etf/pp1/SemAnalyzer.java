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
        if(currentMethod == null){
            varObj = Tab.find(varDecl.getI1());
        }else{
            varObj = Tab.currentScope().findSymbol(varDecl.getI1());
        }

        if(varObj == null || varObj == Tab.noObj) {
            varObj = Tab.insert(Obj.Var, varDecl.getI1(), currentType);
           
        } else{
            report_error("Variable " + varDecl.getI1() + " is already defined", varDecl);
        }
    }

    @Override
    public void visit(VarDecl_array varDecl) {
        Obj varObj = null;
        if(currentMethod == null){
            varObj = Tab.find(varDecl.getI1());
        }else{
            varObj = Tab.currentScope().findSymbol(varDecl.getI1());
        }

        if(varObj == null || varObj == Tab.noObj) {
            varObj = Tab.insert(Obj.Var, varDecl.getI1(), new Struct(Struct.Array, currentType));
        } else{
            report_error("Variable " + varDecl.getI1() + " is already defined", varDecl);
        }
    }

    // ====================Method declarations====================
    @Override
    public void visit(MethodSigniture_Type methodSignature) {
        Obj methodObj = Tab.find(methodSignature.getI2());
        if(methodObj != Tab.noObj) {
            report_error("Method " + methodSignature.getI2() + " is already defined", methodSignature);
        } else{
            if(currentType == null) currentType = Tab.noType;
            currentMethod =  Tab.insert(Obj.Meth, methodSignature.getI2(), currentType);
            Tab.openScope();
        }
    }

    @Override
    public void visit(MethodSigniture_Void methodSigniture){
        Obj methodObj = Tab.find(methodSigniture.getI1());
        if(methodObj != Tab.noObj) {
            report_error("Method " + methodSigniture.getI1() + " is already defined", methodSigniture);
        } else{
            currentMethod =  Tab.insert(Obj.Meth, methodSigniture.getI1(), Tab.noType);
            Tab.openScope();
        }
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        Tab.chainLocalSymbols(currentMethod);
        Tab.closeScope();
        currentMethod = null;
    }
}
