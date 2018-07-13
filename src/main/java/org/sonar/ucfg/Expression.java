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

import java.util.Objects;

public interface Expression {

  Expression THIS = new Expression() {
    @Override
    public String toString() {
      return "_this_";
    }
  };

  default boolean isConstant(){
    return false;
  }
  default boolean isVariable(){
    return false;
  }

  class Variable implements Expression {
    private final String id;
    private final int hashcode;

    public Variable(String id) {
      this.id = id;
      this.hashcode = Objects.hash(id);
    }

    public String id() {
      return id;
    }

    @Override
    public boolean isVariable() {
      return true;
    }

    @Override
    public String toString() {
      return id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Variable variable = (Variable) o;
      return Objects.equals(id, variable.id);
    }

    @Override
    public int hashCode() {
      return hashcode;
    }
  }

  class Constant implements Expression {
    private final String value;

    public Constant(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }

    @Override
    public String toString() {
      return "\"" + value + "\"";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Constant constant = (Constant) o;
      return Objects.equals(value, constant.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }

    @Override
    public boolean isConstant() {
      return true;
    }
  }

  class ClassName implements Expression {
    private final String typeName;
    private final int hashcode;

    public ClassName(String typeName) {
      this.typeName = typeName;
      this.hashcode = Objects.hash(typeName);
    }

    public String typeName() {
      return typeName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ClassName thatClassName = (ClassName) o;
      return Objects.equals(this.typeName, thatClassName.typeName);
    }

    @Override
    public int hashCode() {
      return hashcode;
    }

    @Override
    public String toString() {
      return "ClassName:"+ typeName;
    }
  }

  class FieldAccess implements Expression {

    private final Expression object;
    private final Expression.Variable field;
    private final int hashcode;

    private FieldAccess(Expression object, Expression.Variable field) {
      this.object = object;
      this.field = field;
      this.hashcode = Objects.hash(object, field);
    }

    public FieldAccess(Expression.Variable field) {
      this(THIS, field);
    }

    public FieldAccess(Expression.Variable object, Expression.Variable field) {
      this((Expression) object, field);
    }

    public FieldAccess(Expression.ClassName object, Expression.Variable field) {
      this((Expression) object, field);
    }

    public Expression object() {
      return object;
    }

    public Expression.Variable field() {
      return field;
    }

    @Override
    public String toString() {
      return String.format("FieldAccess %s %s", object, field);
    }

    @Override
    public int hashCode() {
      return hashcode;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      FieldAccess fieldAccess = (FieldAccess) o;
      return Objects.equals(object, fieldAccess.object)
        && Objects.equals(field, fieldAccess.field);
    }
  }
}
