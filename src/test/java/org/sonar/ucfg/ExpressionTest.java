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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionTest {

  @Test
  void test_constant() {
    assertThat(new Expression.Constant("const").isConstant()).isTrue();
    assertThat(new Expression.Variable("variable").isConstant()).isFalse();
    assertThat(new Expression.ClassName("classname").isConstant()).isFalse();
    assertThat(Expression.THIS.isConstant()).isFalse();
    assertThat(new Expression.FieldAccess(new Expression.Variable("field")).isConstant()).isFalse();
  }

  @Test
  void test_variable() {
    assertThat(new Expression.Constant("const").isVariable()).isFalse();
    assertThat(new Expression.Variable("variable").isVariable()).isTrue();
    assertThat(new Expression.ClassName("classname").isVariable()).isFalse();
    assertThat(Expression.THIS.isVariable()).isFalse();
    assertThat(new Expression.FieldAccess(new Expression.Variable("field")).isVariable()).isFalse();
  }

  @Test
  void assertEquals_hashcode() {
    Expression.Constant constant = new Expression.Constant("constant");
    Expression.Constant varConstant = new Expression.Constant("var1");
    Expression.Constant constantBis = new Expression.Constant("constant");
    Expression.Constant anotherConst = new Expression.Constant("anotherConst");
    Expression.Variable var1 = new Expression.Variable("var1");
    Expression.Variable var2 = new Expression.Variable("var2");
    Expression.Variable var1Bis = new Expression.Variable("var1");
    Expression.ClassName className = new Expression.ClassName("classname1");
    Expression.ClassName className2 = new Expression.ClassName("classname2");
    Expression.ClassName className3 = new Expression.ClassName("classname1");
    Expression.FieldAccess fieldAccess1 = new Expression.FieldAccess(var1, var2);
    Expression.FieldAccess fieldAccess1Bis = new Expression.FieldAccess(var1, var2);
    Expression.FieldAccess fieldAccess2 = new Expression.FieldAccess(var2, var1);
    Expression.FieldAccess thidFieldAccess1 = new Expression.FieldAccess(var1);
    Expression.FieldAccess thisFieldAccess2 = new Expression.FieldAccess(var2);
    Expression.FieldAccess staticFieldAccess = new Expression.FieldAccess(className, var1);

    assertThat(constant).isEqualTo(constant).isEqualTo(constantBis).isNotEqualTo(anotherConst).isNotEqualTo(var1).isNotEqualTo(null).isNotEqualTo(Expression.THIS);
    assertThat(constant.hashCode()).isEqualTo(constantBis.hashCode()).isNotEqualTo(anotherConst.hashCode()).isNotEqualTo(var1.hashCode());

    assertThat(var1).isEqualTo(var1).isEqualTo(var1Bis).isNotEqualTo(constant).isNotEqualTo(var2).isNotEqualTo(null).isNotEqualTo(Expression.THIS);
    assertThat(var1.hashCode()).isEqualTo(var1Bis.hashCode()).isNotEqualTo(anotherConst.hashCode()).isNotEqualTo(var2.hashCode());

    assertThat(className).isEqualTo(className).isEqualTo(className3).isNotEqualTo(constant).isNotEqualTo(className2).isNotEqualTo(null).isNotEqualTo(Expression.THIS);
    assertThat(className.hashCode()).isEqualTo(className3.hashCode()).isNotEqualTo(anotherConst.hashCode()).isNotEqualTo(className2.hashCode());

    assertThat(varConstant).isNotEqualTo(constant);

    assertThat(fieldAccess1).isEqualTo(fieldAccess1).isEqualTo(fieldAccess1Bis).isNotEqualTo(fieldAccess2).isNotEqualTo(thidFieldAccess1).isNotEqualTo(constant).isNotEqualTo(null);
    assertThat(thidFieldAccess1).isNotEqualTo(thisFieldAccess2).isNotEqualTo(staticFieldAccess);
    assertThat(fieldAccess1.hashCode()).isEqualTo(fieldAccess1Bis.hashCode()).isNotEqualTo(fieldAccess2.hashCode()).isNotEqualTo(staticFieldAccess.hashCode());
  }

  @Test
  void to_string() {
    Expression.Variable var1 = new Expression.Variable("var1");
    Expression.Variable var2 = new Expression.Variable("var2");
    Expression.Constant constant = new Expression.Constant("constant");
    Expression.FieldAccess fieldAccess1 = new Expression.FieldAccess(var1, var2);
    Expression.FieldAccess fieldAccess2 = new Expression.FieldAccess(var2);

    assertThat(var1.toString()).isEqualTo("var1").isEqualTo(var1.id());
    assertThat(constant.toString()).isEqualTo("\"constant\"");
    assertThat(fieldAccess1.toString()).isEqualTo("FieldAccess var1 var2");
    assertThat(fieldAccess2.toString()).isEqualTo("FieldAccess _this_ var2");
  }
}
