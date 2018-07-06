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

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class UCFGElementTest {

  private LocationInFile loc = new LocationInFile("fileId", 1, 12, 1, 15);

  @Test
  void jump_should_have_no_location_and_empty_sources() {
    Label label1 = new Label("1");
    Label label2 = new Label("2");
    UCFGElement.Jump jump = new UCFGElement.Jump(Arrays.asList(label1, label2));
    assertThat(jump.type()).isSameAs(UCFGElement.UCFGElementType.JUMP);
    assertThat(jump.destinations()).containsExactly(label1, label2);
    assertThat(jump.toString()).isEqualTo("    jump 1, 2");

    UCFGElement.Jump jump2 = new UCFGElement.Jump(Arrays.asList(label1, label2));
    UCFGElement.Jump jump3 = new UCFGElement.Jump(Collections.singletonList(label1));
    assertThat(jump).isEqualTo(jump).isEqualTo(jump2)
      .isNotEqualTo(null).isNotEqualTo(new Object()).isNotEqualTo(jump3);
    assertThat(jump.hashCode()).isEqualTo(jump.hashCode()).isEqualTo(jump2.hashCode()).isNotEqualTo(jump3.hashCode());
  }

  @Test
  void cannot_create_empty_jump() {
    try {
      new UCFGElement.Jump(Collections.emptyList());
      fail("jump construction should not have succeeded");
    }catch (IllegalStateException ise) {
      assertThat(ise).hasMessage("Cannot create jump with empty destinations");
    }
  }

  @Test
  void ret_should_have_one_source_and_no_dest() {
    UCFGElement ret = new UCFGElement.Ret(loc, new Expression.Variable("var#0"));
    assertThat(ret.type()).isSameAs(UCFGElement.UCFGElementType.RET);
    assertThat(ret.location()).isSameAs(loc);
    assertThat(ret.toString()).isEqualTo("     ret var#0\n" +
      "         in fileId\n" +
      "         at 1:12 - 1:15");

    UCFGElement ret2 = new UCFGElement.Ret(loc, new Expression.Variable("var#0"));
    UCFGElement ret3 = new UCFGElement.Ret(loc, new Expression.Variable("var#1"));

    assertThat(ret).isEqualTo(ret).isEqualTo(ret2)
      .isNotEqualTo(null).isNotEqualTo(new Object()).isNotEqualTo(ret3);
    assertThat(ret.hashCode()).isEqualTo(ret2.hashCode()).isNotEqualTo(ret3.hashCode());
  }

  @Test
  void assign_call_instruction() {
    UCFGElement call = new UCFGElement.AssignCall(loc, new Expression.Variable("dest"), "methodId", Arrays.asList(new Expression.Variable("expr1"), new Expression.Constant("expr2")));
    assertThat(call.type()).isSameAs(UCFGElement.UCFGElementType.CALL);
    assertThat(call.location()).isSameAs(loc);
    assertThat(call.toString()).isEqualTo("    call dest = methodId (expr1, \"expr2\")\n" +
      "         in fileId\n" +
      "         at 1:12 - 1:15");


    UCFGElement call2 = new UCFGElement.AssignCall(loc, new Expression.Variable("dest"), "methodId", Arrays.asList(new Expression.Variable("expr1"), new Expression.Constant("expr2")));
    UCFGElement call3 = new UCFGElement.AssignCall(loc, new Expression.Variable("dest"), "methodId", Collections.singletonList(new Expression.Variable("expr1")));
    UCFGElement fieldAccessCall = new UCFGElement.AssignCall(loc, new Expression.FieldAccess(new Expression.Variable("dest")), "methodId",
      Collections.singletonList(new Expression.Variable("expr1")));

    assertThat(call)
      .isEqualTo(call)
      .isEqualTo(call2)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object())
      .isNotEqualTo(call3)
      .isNotEqualTo(fieldAccessCall);
    assertThat(call.hashCode()).isEqualTo(call2.hashCode()).isNotEqualTo(call3.hashCode());

    UCFGElement.AssignCall assignCall_id_accept_FieldAccess =
      new UCFGElement.AssignCall(loc, new Expression.Variable("dest1"), "__id", Arrays.asList(new Expression.FieldAccess(new Expression.Variable("field1")), new Expression.Constant("expr2")));
    assertThat(assignCall_id_accept_FieldAccess.getArgExpressions().stream().filter(e -> e instanceof Expression.FieldAccess)).isNotEmpty();
    try {
      new UCFGElement.AssignCall(loc, new Expression.Variable("dest1"), "some_method", Arrays.asList(new Expression.FieldAccess(new Expression.Variable("field1")), new Expression.Constant("expr2")));
      fail("This assign call with a field access should not have been instantiated");
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessage("Field access cannot be use as argument of method : some_method");
    }
  }

  @Test
  void new_object_instruction() {
    UCFGElement newObject = new UCFGElement.NewObject(loc, new Expression.Variable("var#0"), "com.foo.bar.Qix");
    assertThat(newObject.type()).isSameAs(UCFGElement.UCFGElementType.NEW);
    assertThat(newObject.location()).isSameAs(loc);
    assertThat(newObject.toString()).isEqualTo("  var#0 = new com.foo.bar.Qix\n" +
      "         in fileId\n" +
      "         at 1:12 - 1:15");


    UCFGElement newObject2 = new UCFGElement.NewObject(loc, new Expression.Variable("var#0"), "com.foo.bar.Qix");
    UCFGElement newObject3 = new UCFGElement.NewObject(loc, new Expression.Variable("var#1"), "com.foo.bar.Qix");
    UCFGElement newObject4 = new UCFGElement.NewObject(loc, new Expression.Variable("var#0"), "com.foo.bar.Qix2");
    UCFGElement fieldAccessNewObject = new UCFGElement.NewObject(loc, new Expression.FieldAccess(new Expression.Variable("var#0")), "com.foo.bar.Qix");
    assertThat(newObject)
      .isEqualTo(newObject)
      .isEqualTo(newObject2)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object())
      .isNotEqualTo(newObject3)
      .isNotEqualTo(newObject4)
      .isNotEqualTo(fieldAccessNewObject);
    assertThat(newObject.hashCode()).isEqualTo(newObject2.hashCode()).isNotEqualTo(newObject3.hashCode());
  }
}
