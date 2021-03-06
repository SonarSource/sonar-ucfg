/*
 * sonar-ucfg
 * Copyright (C) 2018-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.ucfg;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public abstract class UCFGElement {

  private final UCFGElementType type;
  final LocationInFile locationInFile;

  private UCFGElement(UCFGElementType type, @Nullable LocationInFile locationInFile) {
    this.type = type;
    this.locationInFile = locationInFile;
  }

  public UCFGElementType type() {
    return type;
  }

  public LocationInFile location() {
    return locationInFile;
  }

  public enum UCFGElementType {
    CALL,
    NEW,
    RET,
    JUMP
  }

  public abstract static class Terminator extends UCFGElement {
    private Terminator(UCFGElementType type, @Nullable LocationInFile locationInFile) {
      super(type, locationInFile);
    }
  }
  public abstract static class Instruction extends UCFGElement {
    private Instruction(UCFGElementType type, @Nullable LocationInFile locationInFile) {
      super(type, locationInFile);
    }
  }

  public static class NewObject extends Instruction {
    private final Expression lhs;
    private final String instanceType;
    private final int hash;

    private NewObject(LocationInFile locationInFile, Expression lhs, String instanceType) {
      super(UCFGElementType.NEW, locationInFile);
      this.lhs = lhs;
      this.instanceType = instanceType;
      this.hash = Objects.hash(UCFGElementType.NEW, lhs, instanceType, locationInFile);
    }

    public NewObject(LocationInFile locationInFile, Expression.Variable lhs, String instanceType) {
      this(locationInFile, (Expression) lhs, instanceType);
    }

    public NewObject(LocationInFile locationInFile, Expression.FieldAccess lhs, String instanceType) {
      this(locationInFile, (Expression) lhs, instanceType);
    }

    public String instanceType() {
      return instanceType;
    }

    public Expression getLhs() {
      return lhs;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      NewObject newObject = (NewObject) o;
      return Objects.equals(instanceType, newObject.instanceType)
        && Objects.equals(lhs, newObject.lhs)
        && Objects.equals(locationInFile, newObject.locationInFile);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    @Override
    public String toString() {
      return "  " + lhs + " = new " + instanceType + locationInFile;
    }
  }

  public static class AssignCall extends Instruction {
    private final Expression lhs;
    private final String methodId;
    private final List<Expression> argExpressions;
    private final int hash;

    private AssignCall(LocationInFile locationInFile, Expression lhs, String methodId, List<Expression> argExpressions) {
      super(UCFGElementType.CALL, locationInFile);
      this.lhs = lhs;
      this.methodId = methodId;
      this.argExpressions = argExpressions;
      this.hash = Objects.hash(UCFGElementType.CALL, lhs, methodId, argExpressions, locationInFile);
      if (!"__id".equals(methodId) && argExpressions.stream().anyMatch(e -> e instanceof Expression.FieldAccess)) {
        throw new IllegalArgumentException(String.format("Field access cannot be use as argument of method : %s", methodId));
      }
    }

    public AssignCall(LocationInFile locationInFile, Expression.Variable lhs, String methodId, List<Expression> argExpressions) {
      this(locationInFile, (Expression) lhs, methodId, argExpressions);
    }

    public AssignCall(LocationInFile locationInFile, Expression.FieldAccess lhs, String methodId, List<Expression> argExpressions) {
      this(locationInFile, (Expression) lhs, methodId, argExpressions);
    }

    public String getMethodId() {
      return methodId;
    }

    public List<Expression> getArgExpressions() {
      return argExpressions;
    }

    @Override
    public String toString() {
      return "    call " + lhs + " = " + methodId + " (" + argExpressions.stream().map(Expression::toString).collect(Collectors.joining(", ")) + ")" + locationInFile.toString();
    }

    public Expression getLhs() {
      return lhs;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      AssignCall that = (AssignCall) o;
      return Objects.equals(lhs, that.lhs) &&
        Objects.equals(methodId, that.methodId) &&
        Objects.equals(argExpressions, that.argExpressions) &&
        Objects.equals(locationInFile, that.locationInFile);
    }

    @Override
    public int hashCode() {
      return hash;
    }
  }

  public static class Ret extends Terminator {
    private final Expression returnedExpression;
    private final int hash;

    public Ret(LocationInFile locationInFile, Expression returnedExpression) {
      super(UCFGElementType.RET, locationInFile);
      this.returnedExpression = returnedExpression;
      this.hash = Objects.hash(UCFGElementType.RET, returnedExpression, locationInFile);
    }

    @Override
    public String toString() {
      return "     ret " + returnedExpression + locationInFile.toString();
    }

    public Expression getReturnedExpression() {
      return returnedExpression;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Ret ret = (Ret) o;
      return Objects.equals(returnedExpression, ret.returnedExpression) && Objects.equals(locationInFile, ret.locationInFile);
    }

    @Override
    public int hashCode() {
      return hash;
    }
  }

  public static class Jump extends Terminator {
    private final List<Label> destinations;
    private int hash;

    public Jump(List<Label> destinations) {
      super(UCFGElementType.JUMP, null);
      if(destinations.isEmpty()) {
        throw new IllegalStateException("Cannot create jump with empty destinations");
      }
      this.destinations = destinations;
      this.hash = Objects.hash(UCFGElementType.JUMP, destinations);
    }

    @Override
    public String toString() {
      return "    jump "+destinations.stream().map(Label::id).collect(Collectors.joining(", "));
    }

    public List<Label> destinations() {
      return destinations;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Jump jump = (Jump) o;
      return Objects.equals(destinations, jump.destinations);
    }

    @Override
    public int hashCode() {
      return hash;
    }
  }

}
