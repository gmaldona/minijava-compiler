package minijava.lang.parser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AST {

   public interface ASTNode     {}

   public interface Scope       {}

   public interface StatementScope extends Statement, ASTNode, Scope {
      Statement statement();
   }

   public interface Type           extends ASTNode {}

   public interface Statement      extends ASTNode {}

   public interface Expression     extends ASTNode {}

   public interface Expression2    extends ASTNode {}

   public interface Declaration    extends ASTNode {}

   public interface ExprBoolean extends Expression {
      Optional<Expression2> expr2();
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

   public record  PrintStatement(Expression expr) implements Statement {}

   public record AssignStatement(Identifier varName,
                                 Expression expr) implements Statement {}

   public record ArrayAssignStatement(Expression indexExpr,
                                      Expression expr) implements Statement {}

   public record ExprNumber(IntLiteral            integer,
                            Optional<Expression2> expr2)  implements Expression {}

   public record ExprTrue(Optional<Expression2> expr2) implements ExprBoolean {}

   public record ExprFalse(Optional<Expression2> expr2) implements ExprBoolean {}

   public record  ExprId(Identifier            id,
                         Optional<Expression2> expr2) implements Expression {}

   public record ExprThis(Optional<Expression2> expr2) implements Expression {}

   public record  ExprNot(Expression expr,
                          Optional<Expression2> expr2) implements Expression {}

   public record NewIntArrayDecl(Expression            expr,
                                 Optional<Expression2> expr2) implements Expression {}

   public record NewClassDecl(Identifier className,
                              Optional<Expression2> expr2) implements Expression {}

   public record ExprParenthesis(Expression                 expr,
                                 Optional<Expression2>      expr2) implements Expression {}

   public record ExprClassMember(Identifier                 id,
                                 List<Expression>           memberParams,
                                 Optional<Expression2>      expr2) implements Expression2 {}

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
