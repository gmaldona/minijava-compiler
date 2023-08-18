package minijava.lang.parser;

import java.util.List;
import java.util.Optional;

public class AST {

   public interface ASTNode     {}

   public interface Scope       {}

   public interface StatementScope extends Statement, ASTNode, Scope {
      Statement statement();
   }

   public interface Type            extends ASTNode {}

   public interface Statement       extends ASTNode {}

   public interface IExpression     extends ASTNode {}

   public interface Expression  extends IExpression {}

   public interface Expression2 extends IExpression {}

   public interface Declaration     extends ASTNode {}

   public interface ExprBoolean extends Expression {
      Optional<Expression2> expr2();
   }

   public abstract static class ClassExpression implements Expression {

      private Optional<Expression2> expr2;

      public ClassExpression(Optional<Expression2> expr2) {
         this.expr2 = expr2;
      }

      abstract Identifier identifier();

      public Optional<Expression2> expr2() {
         return expr2;
      }

      public Optional<Expression2> expr2(Optional<Expression2> expr2) {
         this.expr2 = expr2;
         return this.expr2;
      }
   }

   public interface Operation      extends Expression2 {
      Expression            expr();
      Optional<Expression2> expr2();
   }

   public record Program(MainClass        mainClass,
                         List<ClassDecl> classDecls) implements ASTNode, Scope {}

   public record MainClass(Identifier     className,
                           Identifier     argName,
                           Statement      statement) implements ASTNode, Scope {}

   public record ClassDecl(Identifier           className,
                           Optional<Identifier> superClass,
                           List<VarDecl>        varDecls,
                           List<MethodDecl>     methodDecls) implements ASTNode, Scope {}

   public record VarDecl(Type         varType,
                         Identifier   varName) implements Declaration {}

   public record MethodParam(Type type, Identifier name) implements Declaration {}

   public record MethodDecl(Type                  methodType,
                            Identifier            methodName,
                            List<MethodParam>     methodParams,
                            List<VarDecl>         varDecls,
                            List<Statement>       statements,
                            Expression            returnExpr) implements ASTNode, Scope {}

   public record StatementBlock(List<Statement> statements) implements Statement, ASTNode, Scope {}

   public record IfStatement(Expression expr,
                             Statement  statement,
                             Statement  elseStatement) implements StatementScope {}

   public record WhileLoop(Expression expr,
                           Statement  statement) implements StatementScope {}

   public record PrintStatement(Expression expr) implements Statement {}

   public record AssignStatement(Identifier varName,
                                 Expression expr) implements Statement {}

   public record ArrayAssignStatement(Expression indexExpr,
                                      Expression expr) implements Statement {}

   public record ExprNumber(IntLiteral            integer,
                            Optional<Expression2> expr2)  implements Expression {}

   public record ExprTrue(Optional<Expression2> expr2) implements ExprBoolean {}

   public record ExprFalse(Optional<Expression2> expr2) implements ExprBoolean {}

   public static class ExprId extends ClassExpression {
      private final Identifier className;

      public ExprId(Identifier className, Optional<Expression2> expr2) {
         super(expr2);
         this.className = className;
      }

      @Override
      public Identifier identifier() {
         return className;
      }
   }

   public static class ExprThis extends ClassExpression implements IExpression {

      public ExprThis(Optional<Expression2> expr2) {
         super(expr2);
      }

      @Override
      Identifier identifier() {
         return null;
      }
   }

   public record  ExprNot(Expression expr,
                          Optional<Expression2> expr2) implements Expression {}

   public record NewIntArrayDecl(Expression            expr,
                                 Optional<Expression2> expr2) implements Expression {}

   public static class NewClassDecl extends ClassExpression {

      private final Identifier className;

      public NewClassDecl(Identifier className, Optional<Expression2> expr2) {
         super(expr2);
         this.className = className;
      }

      @Override
      public Identifier identifier() {
         return className;
      }
   }

   public record ExprParenthesis(Expression                 expr,
                                 Optional<Expression2>      expr2) implements Expression {}

   public static class ExprClassMember extends ClassExpression implements Expression2 {

      private final Identifier identifier;
      private final List<Expression> memberParams;

      public ExprClassMember(Identifier identifier,
                             List<Expression> memberParams,
                             Optional<Expression2> expr2) {
         super(expr2);
         this.identifier = identifier;
         this.memberParams = memberParams;
      }

      @Override
      public Identifier identifier() {
         return identifier;
      }

      public List<Expression> memberParams() {
         return memberParams;
      }
   }

   public record ExprArray(Expression            expr,
                           Optional<Expression2> expr2) implements Expression2 {}

   public record ArrayLength(Optional<Expression2> expr2) implements Expression2 {}

   public record IntLiteral(Integer integer) implements ASTNode {
   }

   public record Identifier(String id) implements ASTNode {
      public String toString() {
         return id;
      }

      public boolean equals(Object anObject) {
         if (this == anObject) {
            return true;
         }
         return anObject instanceof Identifier
            && id.equals(((Identifier) anObject).id);
      }
   }

   public record IntArray() implements Type {
      public String toString() {
         return IntArray.class.getName();
      }
   }

   public record Bool() implements Type {
      public String toString() {
         return Bool.class.getName();
      }
   }

   public record Int() implements Type {
      public String toString() {
         return Int.class.getName();
      }
   }

   public record ClassType(Identifier identifier) implements Type {
      public String toString() {
         return identifier.id;
      }
   }

   public record            And(Expression            expr,
                                Optional<Expression2> expr2) implements Operation {}

   public record       Addition(Expression            expr,
                                Optional<Expression2> expr2) implements Operation {}

   public record    Subtraction(Expression            expr,
                                Optional<Expression2> expr2) implements Operation {}

   public record Multiplication(Expression            expr,
                                Optional<Expression2> expr2) implements Operation {}

   public record       LessThan(Expression            expr,
                                Optional<Expression2> expr2) implements Operation {}
}
