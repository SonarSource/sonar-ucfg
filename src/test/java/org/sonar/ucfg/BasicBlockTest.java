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


import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BasicBlockTest {

  @Test
  void test_block_equality() {
    Label dest = new Label("dest");
    BasicBlock b1 = new BasicBlock(new Label("label1"), Collections.emptyList(), new Instruction.Jump(Collections.singletonList(dest)), null);
    BasicBlock b2 = new BasicBlock(new Label("label2"), Collections.emptyList(), new Instruction.Jump(Collections.singletonList(dest)), null);
    BasicBlock b3 = new BasicBlock(new Label("label1"), Collections.emptyList(), new Instruction.Jump(Collections.singletonList(dest)), null);
    assertThat(b1).isEqualTo(b1).isEqualTo(b3).isNotEqualTo(new Object()).isNotEqualTo(null).isNotEqualTo(b2);
  }

  @Test
  void isRedundant_block() {
    Label dest = new Label("dest");
    BasicBlock b1 = new BasicBlock(new Label("label1"), Collections.emptyList(), new Instruction.Jump(Collections.singletonList(dest)), null);
    assertThat(b1.isRedundant()).isTrue();

    BasicBlock b2 = new BasicBlock(new Label("label1"), Collections.emptyList(), new Instruction.Ret(null, null), null);
    assertThat(b2.isRedundant()).isFalse();

    BasicBlock b3 = new BasicBlock(new Label("label1"), Collections.singletonList(new Instruction.AssignCall(null, null, "foo", Collections.emptyList())), new Instruction.Jump(Collections.singletonList(dest)), null);
    assertThat(b3.isRedundant()).isFalse();

    BasicBlock b4 = new BasicBlock(new Label("label1"), Collections.singletonList(new Instruction.AssignCall(null, null, "foo", Collections.emptyList())), new Instruction.Ret(null, null), null);
    assertThat(b4.isRedundant()).isFalse();
  }

  @Test
  void dead_end_is_not_redundant_but_cycling_and_not_modifiable() {
    assertThat(BasicBlock.DEAD_END.isRedundant()).isFalse();
    assertThat(BasicBlock.DEAD_END.successors()).containsOnly(BasicBlock.DEAD_END.label());
    assertThat(BasicBlock.DEAD_END.locationInFile()).isNull();
    assertThat(BasicBlock.DEAD_END.terminator().type()).isEqualTo(Instruction.InstructionType.JUMP);
    assertThat(((Instruction.Jump) BasicBlock.DEAD_END.terminator()).destinations()).containsOnly(BasicBlock.DEAD_END.label());

    Label newSuccessorLabel = new Label("1");
    Set<BasicBlock> newSuccessors = Collections.singleton(new BasicBlock(newSuccessorLabel, Collections.emptyList(), new Instruction.Ret(null, null), null));
    BasicBlock.DEAD_END.updateSuccs(newSuccessors);

    assertThat(BasicBlock.DEAD_END.successors()).containsOnly(BasicBlock.DEAD_END.label());
  }
}
