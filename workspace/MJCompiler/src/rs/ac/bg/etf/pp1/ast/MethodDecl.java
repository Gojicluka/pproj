// generated with ast extension for cup
// version 0.8
// 8/4/2025 12:59:29


package rs.ac.bg.etf.pp1.ast;

public class MethodDecl implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Obj obj = null;

    private MethodSigniture MethodSigniture;
    private VarDeclListOptional VarDeclListOptional;
    private StatementList StatementList;

    public MethodDecl (MethodSigniture MethodSigniture, VarDeclListOptional VarDeclListOptional, StatementList StatementList) {
        this.MethodSigniture=MethodSigniture;
        if(MethodSigniture!=null) MethodSigniture.setParent(this);
        this.VarDeclListOptional=VarDeclListOptional;
        if(VarDeclListOptional!=null) VarDeclListOptional.setParent(this);
        this.StatementList=StatementList;
        if(StatementList!=null) StatementList.setParent(this);
    }

    public MethodSigniture getMethodSigniture() {
        return MethodSigniture;
    }

    public void setMethodSigniture(MethodSigniture MethodSigniture) {
        this.MethodSigniture=MethodSigniture;
    }

    public VarDeclListOptional getVarDeclListOptional() {
        return VarDeclListOptional;
    }

    public void setVarDeclListOptional(VarDeclListOptional VarDeclListOptional) {
        this.VarDeclListOptional=VarDeclListOptional;
    }

    public StatementList getStatementList() {
        return StatementList;
    }

    public void setStatementList(StatementList StatementList) {
        this.StatementList=StatementList;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MethodSigniture!=null) MethodSigniture.accept(visitor);
        if(VarDeclListOptional!=null) VarDeclListOptional.accept(visitor);
        if(StatementList!=null) StatementList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MethodSigniture!=null) MethodSigniture.traverseTopDown(visitor);
        if(VarDeclListOptional!=null) VarDeclListOptional.traverseTopDown(visitor);
        if(StatementList!=null) StatementList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MethodSigniture!=null) MethodSigniture.traverseBottomUp(visitor);
        if(VarDeclListOptional!=null) VarDeclListOptional.traverseBottomUp(visitor);
        if(StatementList!=null) StatementList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MethodDecl(\n");

        if(MethodSigniture!=null)
            buffer.append(MethodSigniture.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarDeclListOptional!=null)
            buffer.append(VarDeclListOptional.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(StatementList!=null)
            buffer.append(StatementList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MethodDecl]");
        return buffer.toString();
    }
}

