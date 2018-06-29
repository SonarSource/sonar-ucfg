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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class BasicBlock {
  private final Label label;
  private final List<Instruction> instructions;
  private final LocationInFile loc;
  private Instruction.Terminator terminator;

  private static final Label DEAD_END_LABEL = new Label("DEAD_END");
  public static final BasicBlock DEAD_END = new BasicBlock(DEAD_END_LABEL, Collections.emptyList(), new Instruction.Jump(Collections.singletonList(DEAD_END_LABEL)), null) {
    @Override
    public boolean isRedundant() {
      return false;
    }

    @Override
    public void updateSuccs(Set<BasicBlock> successors) {
      // Do not change the successors of DEAD_EAD, should always loop on itself only
    }
  };

  BasicBlock(Label label, List<Instruction> instructions, Instruction.Terminator terminator, @Nullable LocationInFile loc) {
    this.label = label;
    this.instructions = instructions;
    this.terminator = terminator;
    this.loc = loc;
  }

  public Label label() {
    return label;
  }

  public List<Instruction> instructions() {
    return instructions;
  }

  public Instruction.Terminator terminator() {
    return terminator;
  }

  public boolean isRedundant() {
    return instructions.isEmpty() && !successors().isEmpty();
  }

  public List<Label> successors() {
    if(terminator.type() == Instruction.InstructionType.JUMP) {
      return ((Instruction.Jump) terminator).destinations();
    }
    return Collections.emptyList();
  }

  public void updateSuccs(Set<BasicBlock> successors) {
    if (terminator.type() == Instruction.InstructionType.JUMP) {
      this.terminator = new Instruction.Jump(successors.stream().map(BasicBlock::label).collect(Collectors.toList()));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BasicBlock that = (BasicBlock) o;
    return Objects.equals(label, that.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label);
  }

  @Override
  public String toString() {
    return label + "\n" + instructions.stream().map(Instruction::toString).collect(Collectors.joining("\n")) + "\n" + terminator.toString();
  }

  @Nullable
  public LocationInFile locationInFile() {
    return loc;
  }
}
