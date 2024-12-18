package loxLanguage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;
    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER
    }
    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(List<Stmt> stmtList) {
        for (Stmt stmt : stmtList) {
            resolve(stmt);
        }
    }

    /** This is where all resolutions end up and exit to the interpreter **/
    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - i - 1);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        beginScope();

        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);

        currentFunction = enclosingFunction;
        endScope();
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Cannot redeclare variable in the same scope.");
        }

        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        scope.put(name.lexeme, true);
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary binary) {
        resolve(binary.left);
        resolve(binary.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call call) {
        resolve(call.callee);
        for (Expr arg : call.arguments) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get get) {
        resolve(get.object);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set set) {
        resolve(set.object);
        resolve(set.value);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super superExpr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(superExpr.keyword, "Cannot use 'super' keyword outside of class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            Lox.error(superExpr.keyword, "Cannot use 'super' keyword without superclass.");
        }

        resolveLocal(superExpr, superExpr.keyword);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This thisExpr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(thisExpr.keyword, "Cannot use 'this' keyword outside of class.");
        }

        resolveLocal(thisExpr, thisExpr.keyword);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping grouping) {
        resolve(grouping.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal literal) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary unary) {
        resolve(unary.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable variable) {
        Token name = variable.name;
        if (!scopes.empty() && !scopes.peek().get(name.lexeme)) {
            Lox.error(name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(variable, name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign assign) {
        resolve(assign.value);
        resolveLocal(assign, assign.name);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical logical) {
        resolve(logical.left);
        resolve(logical.right);
        return null;
    }
    
    @Override
    public Void visitExpressionStmt(Stmt.Expression expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print print) {
        resolve(print.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var var) {
        declare(var.name);
        if (var.initializer != null) {
            resolve(var.initializer);
        }
        define(var.name);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class classStmt) {
        ClassType previousClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(classStmt.name);
        define(classStmt.name);

        Expr.Variable superclass = classStmt.superclass;
        if (superclass != null && superclass.name.lexeme.equals(
                classStmt.name.lexeme
        )) {
            Lox.error(classStmt.name, "A class cannot inherit from itself.");
        }

        if (superclass != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(superclass);
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function function : classStmt.methods) {
            boolean isInitializer = function.name.lexeme.equals(LoxClass.INIT_KEYWORD);
            FunctionType declaration = isInitializer
                       ? FunctionType.INITIALIZER
                       : FunctionType.METHOD;

            resolveFunction(function, declaration);
        }

        endScope();
        if (superclass != null) {
            endScope();
        }
        currentClass = previousClass;
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return Return) {
        if (Return.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(Return.keyword, "Cannot return value from initializer.");
            } else if (currentFunction != FunctionType.FUNCTION) {
                Lox.error(Return.keyword, "Cannot return from top-level code.");
            }

            resolve(Return.value);
        }

        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function function) {
        declare(function.name);
        define(function.name);

        resolveFunction(function, FunctionType.FUNCTION);
        return null;
    }


    @Override
    public Void visitBlockStmt(Stmt.Block block) {
        beginScope();
        resolve(block.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If If) {
        resolve(If.condition);
        resolve(If.thenBranch);

        if (If.elseBranch != null) {
            resolve(If.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While While) {
        resolve(While.condition);
        resolve(While.body);
        return null;
    }
    // ------------- END STATEMENT RESOLVERS ------------- //
}