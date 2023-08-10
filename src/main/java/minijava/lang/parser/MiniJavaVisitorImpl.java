package minijava.lang.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import antlr4.MiniJavaBaseVisitor;
import antlr4.MiniJavaParser;
import antlr4.MiniJavaParser.ClassDeclarationContext;
import antlr4.MiniJavaParser.MainClassContext;
import antlr4.MiniJavaParser.ProgramContext;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.AST.Addition;
import minijava.lang.parser.AST.And;
import minijava.lang.parser.AST.ArrayAssignStatement;
import minijava.lang.parser.AST.ArrayLength;
import minijava.lang.parser.AST.AssignStatement;
import minijava.lang.parser.AST.Bool;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.ClassType;
import minijava.lang.parser.AST.ExprArray;
import minijava.lang.parser.AST.ExprClassMember;
import minijava.lang.parser.AST.ExprFalse;
import minijava.lang.parser.AST.MethodParam;
import minijava.lang.parser.AST.ExprId;
import minijava.lang.parser.AST.ExprNot;
import minijava.lang.parser.AST.ExprNumber;
import minijava.lang.parser.AST.ExprParenthesis;
import minijava.lang.parser.AST.ExprThis;
import minijava.lang.parser.AST.ExprTrue;
import minijava.lang.parser.AST.Expression;
import minijava.lang.parser.AST.Expression2;
import minijava.lang.parser.AST.Identifier;
import minijava.lang.parser.AST.IfStatement;
import minijava.lang.parser.AST.Int;
import minijava.lang.parser.AST.IntArray;
import minijava.lang.parser.AST.IntLiteral;
import minijava.lang.parser.AST.LessThan;
import minijava.lang.parser.AST.MainClass;
import minijava.lang.parser.AST.MethodDecl;
import minijava.lang.parser.AST.Multiplication;
import minijava.lang.parser.AST.NewClassDecl;
import minijava.lang.parser.AST.NewIntArrayDecl;
import minijava.lang.parser.AST.PrintStatement;
import minijava.lang.parser.AST.Program;
import minijava.lang.parser.AST.Statement;
import minijava.lang.parser.AST.StatementBlock;
import minijava.lang.parser.AST.Subtraction;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.VarDecl;
import minijava.lang.parser.AST.WhileLoop;

public class MiniJavaVisitorImpl extends MiniJavaBaseVisitor<ASTNode> {

   @Override
   public ASTNode visitProgram(ProgramContext ctx) {
      MainClass        mainClass = (MainClass) visit(ctx.mainClass());
      List<ClassDecl> classDecls = new ArrayList<>();
      ctx.classDeclaration()
         .forEach( (classDecl) -> classDecls.add((ClassDecl) visit(classDecl)) );

      return new Program(
         mainClass,
         classDecls
      );
   }

   @Override
   public ASTNode visitMainClass(MainClassContext ctx) {
      Identifier className = new Identifier(ctx.Identifier().get(0).getText());
      Identifier argName   = new Identifier(ctx.Identifier().get(1).getText());
      Statement statement  = (Statement) visit(ctx.statement());

      return new MainClass(
         className,
         argName,
         statement
      );
   }

   @Override
   public ASTNode visitClassDeclaration(ClassDeclarationContext ctx) {
      Identifier             className = new Identifier(ctx.Identifier().get(0).getText());
      Optional<Identifier>  superClass = (ctx.Identifier().size() > 1) ?
               Optional.of(new Identifier(ctx.Identifier().get(1).getText())) :
               Optional.empty();
      List<VarDecl>           varDecls = new ArrayList<>();
      ctx.varDeclaration()
         .forEach((varDecl) -> varDecls.add((VarDecl) visit(varDecl)));
      List<MethodDecl>     methodDecls = new ArrayList<>();
      ctx.methodDeclaration()
         .forEach( (methodDecl) -> methodDecls.add((MethodDecl) visit(methodDecl)) );

      return new ClassDecl(
         className,
         superClass,
         varDecls,
         methodDecls
      );
   }

   @Override
   public ASTNode visitVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
      Type       varType = (Type) visit(ctx.type());
      Identifier varName = new Identifier(ctx.Identifier().getText());

      return new VarDecl(
         varType,
         varName
      );
   }

   @Override
   public ASTNode visitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
      Type              methodType   = (Type) visit(ctx.type().get(0));
      Identifier        methodName   = new Identifier(ctx.Identifier().get(0).getText());
      List<MethodParam> methodParams = new ArrayList<>();
      for (int index = 1; index < ctx.Identifier().size(); index ++ ) {
         methodParams.add(new MethodParam(
            (Type) visit(ctx.type(index)),
            new Identifier(ctx.Identifier().get(index).getText())
         ));
      }
      List<VarDecl> varDecls         = new ArrayList<>();
      ctx.varDeclaration()
         .forEach((varDecl) -> varDecls.add((VarDecl) visit(varDecl)));
      List<Statement> statements     = new ArrayList<>();
      ctx.statement()
         .forEach((statement) -> statements.add((Statement) visit(statement)));
      Expression returnExpr          = (Expression) visit(ctx.expression());

      return new MethodDecl(
         methodType,
         methodName,
         methodParams,
         varDecls,
         statements,
         returnExpr
      );
   }

   @Override
   public ASTNode visitIntArrayType(MiniJavaParser.IntArrayTypeContext ctx) {
      return new IntArray();
   }

   @Override
   public ASTNode visitBoolType(MiniJavaParser.BoolTypeContext ctx) {
      return new Bool();
   }

   @Override
   public ASTNode visitIntType(MiniJavaParser.IntTypeContext ctx) {
      return new Int();
   }

   @Override
   public ASTNode visitIdType(MiniJavaParser.IdTypeContext ctx) {
      Identifier className = new Identifier(ctx.Identifier().getText());

      return new ClassType(
         className
      );
   }

   @Override
   public ASTNode visitStatementBlock(MiniJavaParser.StatementBlockContext ctx) {
      List<Statement> statements = new ArrayList<>();
      ctx.statement()
         .forEach((statement) -> statements.add((Statement) visit(statement)));

      return new StatementBlock(statements);
   }

   @Override
   public ASTNode visitIfStatement(MiniJavaParser.IfStatementContext ctx) {
      Expression expr         = (Expression) visit(ctx.expression());
      Statement statement     = (Statement)  visit(ctx.statement(0));
      Statement elseStatement = (Statement)  visit(ctx.statement(1));

      return new IfStatement(
         expr,
         statement,
         elseStatement
      );
   }

   @Override
   public ASTNode visitWhileLoop(MiniJavaParser.WhileLoopContext ctx) {
      Expression expr     = (Expression) visit(ctx.expression());
      Statement statement = (Statement)  visit(ctx.statement());

      return new WhileLoop(
         expr,
         statement
      );
   }

   @Override
   public ASTNode visitPrintExpr(MiniJavaParser.PrintExprContext ctx) {
      Expression expr = (Expression) visit(ctx.expression());

      return new PrintStatement(expr);
   }

   @Override
   public ASTNode visitAssign(MiniJavaParser.AssignContext ctx) {
      Identifier varName = new Identifier(ctx.Identifier().getText());
      Expression expr    = (Expression) visit(ctx.expression());

      return new AssignStatement(
         varName,
         expr
      );
   }

   @Override
   public ASTNode visitArrayAssign(MiniJavaParser.ArrayAssignContext ctx) {
      Expression indexExpr = (Expression) visit(ctx.expression(0));
      Expression expr      = (Expression) visit(ctx.expression(1));

      return new ArrayAssignStatement(
         indexExpr,
         expr
      );
   }

   @Override
   public ASTNode visitExprNumber(MiniJavaParser.ExprNumberContext ctx) {
      IntLiteral integer          = (IntLiteral) visit(ctx.IntegerLiteral());
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprNumber(
         integer,
         expr2
      );
   }

   @Override
   public ASTNode visitExprTrue(MiniJavaParser.ExprTrueContext ctx) {
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprTrue(
         expr2
      );
   }

   @Override
   public ASTNode visitExprFalse(MiniJavaParser.ExprFalseContext ctx) {
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprFalse(
         expr2
      );
   }

   @Override
   public ASTNode visitExprId(MiniJavaParser.ExprIdContext ctx) {
      Identifier id               = new Identifier(ctx.Identifier().getText());
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprId(
         id,
         expr2
      );

   }

   @Override
   public ASTNode visitExprThis(MiniJavaParser.ExprThisContext ctx) {
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprThis(
         expr2
      );
   }

   @Override
   public ASTNode visitNewIntArray(MiniJavaParser.NewIntArrayContext ctx) {
      Expression expr             = (Expression) visit(ctx.expression());
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new NewIntArrayDecl(
         expr,
         expr2
      );
   }

   @Override
   public ASTNode visitNewObject(MiniJavaParser.NewObjectContext ctx) {
      Identifier className        =  new Identifier(ctx.Identifier().getText());
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new NewClassDecl(
         className,
         expr2
      );
   }

   @Override
   public ASTNode visitExprNot(MiniJavaParser.ExprNotContext ctx) {
      Expression expr             = (Expression) visit(ctx.expression());
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprNot(
         expr,
         expr2
      );
   }

   @Override
   public ASTNode visitExprParenthesis(MiniJavaParser.ExprParenthesisContext ctx) {
      Expression expr             = (Expression) visit(ctx.expression());
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprParenthesis(
         expr,
         expr2
      );
   }

   @Override
   public ASTNode visitExprClassMember(MiniJavaParser.ExprClassMemberContext ctx) {
      Identifier id              = new Identifier(ctx.Identifier().getText());
      List<Expression> paramList = new ArrayList<>();
      ctx.expression()
         .forEach((param) -> paramList.add((Expression) visit(param)));
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprClassMember(
         id,
         paramList,
         expr2
      );
   }

   @Override
   public ASTNode visitExprLength(MiniJavaParser.ExprLengthContext ctx) {
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ArrayLength(
         expr2
      );
   }

   @Override
   public ASTNode visitExprArray(MiniJavaParser.ExprArrayContext ctx) {
      Expression expr             = (Expression) visit(ctx.expression());
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return new ExprArray(
         expr,
         expr2
      );
   }

   @Override
   public ASTNode visitExprOp(MiniJavaParser.ExprOpContext ctx) {
      Expression expr = (Expression) visit(ctx.expression());
      Optional<Expression2> expr2 = (ctx.expression2().children != null) ?
         Optional.of((Expression2) visit(ctx.expression2())) :
         Optional.empty();

      return switch (ctx.children.get(0).getText()) {
      case "&&"  -> new And(expr, expr2);
      case "<"   -> new LessThan(expr, expr2);
      case "+"   -> new Addition(expr, expr2);
      case "-"   -> new Subtraction(expr, expr2);
      case "*"   -> new Multiplication(expr, expr2);
      default    -> null;
      };
   }
}
