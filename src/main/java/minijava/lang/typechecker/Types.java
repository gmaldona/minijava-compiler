package minijava.lang.typechecker;

import minijava.lang.parser.AST.Type;

public class Types {

   /**
    * Check if two given {@link Type}s are compatible
    * @return {@link Type} if they are equal
    * @throws {@link IllegalStateException} if the types are not equal
    */
   protected static Type areCompatibleTypes(Type type, Type otherType) {
      if (! hasCompatibleTypes(type, otherType)) {
         throw new IllegalStateException("Types are not compatible: " + type + ", " + otherType);
      }
      return type;
   }

   protected static Type areCompatibleTypes(Class<? extends Type> type, Type otherType) {
      if (! hasCompatibleTypes(type, otherType)) {
         throw new IllegalStateException("Types are not compatible: " + type.getName() + ", " + otherType);
      }
      return otherType;
   }

   protected static boolean hasCompatibleTypes(Type type, Type otherType) {
      return hasCompatibleTypes(type.getClass(), otherType);
   }

   protected static boolean hasCompatibleTypes(Class<? extends Type> type, Type otherType) {
      return type.equals(otherType.getClass());
   }

   protected static Type areCompatibleTypes(Class<? extends Type> type, Type... otherTypes) {
      for (Type otherType : otherTypes) {
         if (! type.equals(otherType.getClass())) {
            throw new IllegalStateException("Types are not compatible: " + type + ", " + otherType);
         }
      }
      return otherTypes[0];
   }

}
