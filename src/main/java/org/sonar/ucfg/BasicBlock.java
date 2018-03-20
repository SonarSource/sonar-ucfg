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
  private final List<Instruction.AssignCall> calls;
  private final LocationInFile loc;
  private Instruction.Terminator terminator;

  BasicBlock(Label label, List<Instruction.AssignCall> calls, Instruction.Terminator terminator, @Nullable LocationInFile loc) {
    this.label = label;
    this.calls = calls;
    this.terminator = terminator;
    this.loc = loc;
  }

  public Label label() {
    return label;
  }

  public List<Instruction.AssignCall> calls() {
    return calls;
  }

  public Instruction.Terminator terminator() {
    return terminator;
  }

  public boolean isRedundant() {
    return calls.isEmpty() && !successors().isEmpty();
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
    return label + "\n" + calls.stream().map(Instruction.AssignCall::toString).collect(Collectors.joining("\n")) + "\n" + terminator.toString();
  }

  @Nullable
  public LocationInFile locationInFile() {
    return loc;
  }
}
