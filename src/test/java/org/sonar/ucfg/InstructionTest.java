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

import org.sonar.ucfg.Expression;
import org.sonar.ucfg.Instruction;
import org.sonar.ucfg.Label;
import org.sonar.ucfg.LocationInFile;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class InstructionTest {

  private LocationInFile loc = new LocationInFile("fileId", 1, 12, 1, 15);

  @Test
  void jump_should_have_no_location_and_empty_sources() {
    Label label1 = new Label("1");
    Label label2 = new Label("2");
    Instruction.Jump jump = new Instruction.Jump(Arrays.asList(label1, label2));
    assertThat(jump.type()).isSameAs(Instruction.InstructionType.JUMP);
    assertThat(jump.destinations()).containsExactly(label1, label2);
    assertThat(jump.toString()).isEqualTo("    jump 1, 2");

    Instruction.Jump jump2 = new Instruction.Jump(Arrays.asList(label1, label2));
    Instruction.Jump jump3 = new Instruction.Jump(Collections.singletonList(label1));
    assertThat(jump).isEqualTo(jump).isEqualTo(jump2)
      .isNotEqualTo(null).isNotEqualTo(new Object()).isNotEqualTo(jump3);
    assertThat(jump.hashCode()).isEqualTo(jump.hashCode()).isEqualTo(jump2.hashCode()).isNotEqualTo(jump3.hashCode());
  }

  @Test
  void cannot_create_empty_jump() {
    try {
      new Instruction.Jump(Collections.emptyList());
      fail("jump construction should not have succeeded");
    }catch (IllegalStateException ise) {
      assertThat(ise).hasMessage("Cannot create jump with empty destinations");
    }
  }

  @Test
  void ret_should_have_one_source_and_no_dest() {
    Instruction ret = new Instruction.Ret(loc, new Expression.Variable("var#0"));
    assertThat(ret.type()).isSameAs(Instruction.InstructionType.RET);
    assertThat(ret.location()).isSameAs(loc);
    assertThat(ret.toString()).isEqualTo("     ret var#0\n" +
      "         in fileId\n" +
      "         at 1:12 - 1:15");

    Instruction ret2 = new Instruction.Ret(loc, new Expression.Variable("var#0"));
    Instruction ret3 = new Instruction.Ret(loc, new Expression.Variable("var#1"));

    assertThat(ret).isEqualTo(ret).isEqualTo(ret2)
      .isNotEqualTo(null).isNotEqualTo(new Object()).isNotEqualTo(ret3);
    assertThat(ret.hashCode()).isEqualTo(ret2.hashCode()).isNotEqualTo(ret3.hashCode());
  }

  @Test
  void call_has_only_one_dest() {
    Instruction call = new Instruction.AssignCall(loc, new Expression.Variable("dest"), "methodId", Arrays.asList(new Expression.Variable("expr1"), new Expression.Constant("expr2")));
    assertThat(call.type()).isSameAs(Instruction.InstructionType.CALL);
    assertThat(call.location()).isSameAs(loc);
    assertThat(call.toString()).isEqualTo("    call dest = methodId (expr1, \"expr2\")\n" +
      "         in fileId\n" +
      "         at 1:12 - 1:15");


    Instruction call2 = new Instruction.AssignCall(loc, new Expression.Variable("dest"), "methodId", Arrays.asList(new Expression.Variable("expr1"), new Expression.Constant("expr2")));
    Instruction call3 = new Instruction.AssignCall(loc, new Expression.Variable("dest"), "methodId", Collections.singletonList(new Expression.Variable("expr1")));
    
    assertThat(call).isEqualTo(call).isEqualTo(call2)
      .isNotEqualTo(null).isNotEqualTo(new Object()).isNotEqualTo(call3);
    assertThat(call.hashCode()).isEqualTo(call2.hashCode()).isNotEqualTo(call3.hashCode());
  }
}
