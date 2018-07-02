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



    assertThat(constant).isEqualTo(constant).isEqualTo(constantBis).isNotEqualTo(anotherConst).isNotEqualTo(var1).isNotEqualTo(null).isNotEqualTo(Expression.THIS);
    assertThat(constant.hashCode()).isEqualTo(constantBis.hashCode()).isNotEqualTo(anotherConst.hashCode()).isNotEqualTo(var1.hashCode());

    assertThat(var1).isEqualTo(var1).isEqualTo(var1Bis).isNotEqualTo(constant).isNotEqualTo(var2).isNotEqualTo(null).isNotEqualTo(Expression.THIS);
    assertThat(var1.hashCode()).isEqualTo(var1Bis.hashCode()).isNotEqualTo(anotherConst.hashCode()).isNotEqualTo(var2.hashCode());

    assertThat(className).isEqualTo(className).isEqualTo(className3).isNotEqualTo(constant).isNotEqualTo(className2).isNotEqualTo(null).isNotEqualTo(Expression.THIS);
    assertThat(className.hashCode()).isEqualTo(className3.hashCode()).isNotEqualTo(anotherConst.hashCode()).isNotEqualTo(className2.hashCode());

    assertThat(varConstant).isNotEqualTo(constant);
  }

  @Test
  void to_string() {
    Expression.Variable var1 = new Expression.Variable("var1");
    Expression.Constant constant = new Expression.Constant("constant");

    assertThat(var1.toString()).isEqualTo("var1").isEqualTo(var1.id());
    assertThat(constant.toString()).isEqualTo("\"constant\"");
  }
}
