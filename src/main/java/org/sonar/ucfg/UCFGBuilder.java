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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.ucfg.UCFGElement.Instruction;
import org.sonar.ucfg.util.WorkSet;

public class UCFGBuilder {

  public static final LocationInFile LOC = new LocationInFile("__unknown_file",1,1,1,1);

  private final List<BasicBlock> blocks = new ArrayList<>();

  private final String methodId;
  private List<Expression.Variable> parameters;
  private Set<BasicBlock> startingBlocks = new HashSet<>();
  private LocationInFile location = LOC;

  private UCFGBuilder(String methodId) {
    this.methodId = methodId;
    this.parameters = new ArrayList<>();
  }

  public List<BasicBlock> getBlocks() {
    return blocks;
  }

  public static BlockBuilder newBasicBlock(String labelId) {
    return newBasicBlock(labelId, LOC);
  }
  public static BlockBuilder newBasicBlock(String labelId, @Nullable LocationInFile loc) {
    return new BlockBuilder(labelId, loc);
  }

  public static class BlockBuilder {

    private UCFGElement.Terminator terminator;
    private List<Instruction> instructions = new ArrayList<>();
    private final Label label;
    private final LocationInFile loc;

    BlockBuilder(String labelId, @Nullable LocationInFile loc) {
      this.label = new Label(labelId);
      this.loc = loc;
    }

    public BlockBuilder terminator(UCFGElement.Terminator terminator) {
      if(this.terminator != null) {
        throw new IllegalStateException("A terminator is already set for block "+label.id());
      }
      this.terminator = terminator;
      return this;
    }

    public BlockBuilder assignTo(Expression.Variable lhs, CallBuilder callBuilder) {
      return assignTo(lhs, callBuilder, LOC);
    }

    public BlockBuilder assignTo(Expression.Variable lhs, CallBuilder callBuilder, LocationInFile loc) {
      instructions.add(new UCFGElement.AssignCall(loc, lhs, callBuilder.methodId, callBuilder.arguments));
      return this;
    }

    public BlockBuilder assignTo(Expression.FieldAccess lhs, CallBuilder callBuilder) {
      return assignTo(lhs, callBuilder, LOC);
    }

    public BlockBuilder assignTo(Expression.FieldAccess lhs, CallBuilder callBuilder, LocationInFile loc) {
      instructions.add(new UCFGElement.AssignCall(loc, lhs, callBuilder.methodId, callBuilder.arguments));
      return this;
    }

    public BlockBuilder newObject(Expression.Variable lhs, String instanceType) {
      return newObject(lhs, instanceType, LOC);
    }

    public BlockBuilder newObject(Expression.Variable lhs, String instanceType, LocationInFile loc) {
      instructions.add(new UCFGElement.NewObject(loc, lhs, instanceType));
      return this;
    }

    public BlockBuilder newObject(Expression.FieldAccess lhs, String instanceType) {
      return newObject(lhs, instanceType, LOC);
    }

    public BlockBuilder newObject(Expression.FieldAccess lhs, String instanceType, LocationInFile loc) {
      instructions.add(new UCFGElement.NewObject(loc, lhs, instanceType));
      return this;
    }

    BasicBlock build() {
      if(terminator == null) {
        throw new IllegalStateException("A terminator should be set for block "+label.id());
      }
      return new BasicBlock(label, instructions, terminator, loc);
    }

    public BlockBuilder ret(Expression expression) {
      return ret(expression, LOC);
    }

    public BlockBuilder ret(Expression expression, LocationInFile locationInFile) {
      return terminator(new UCFGElement.Ret(locationInFile, expression));
    }

    public BlockBuilder jumpTo(Label... labels) {
      return terminator(new UCFGElement.Jump(Arrays.asList(labels)));
    }
  }

  public UCFGBuilder at(LocationInFile location) {
    this.location = location;
    return this;
  }

  public UCFGBuilder addStartingBlock(BlockBuilder blockBuilder) {
    BasicBlock block = blockBuilder.build();
    startingBlocks.add(block);
    blocks.add(block);
    return this;
  }

  public UCFGBuilder addBasicBlock(BlockBuilder blockBuilder) {
    blocks.add(blockBuilder.build());
    return this;
  }

  public UCFG build() {
    if(blocks.isEmpty()) {
      addBasicBlock(newBasicBlock("startLabel").ret(constant("const")));
    }
    if(startingBlocks.isEmpty()) {
      startingBlocks.add(blocks.get(0));
    }

    return new UCFG(methodId, parameters, removeUnreachable(blocks, startingBlocks), startingBlocks, location);
  }

  /**
   * Remove parts of the graph not reachable from entry point.
   */
  private static Set<BasicBlock> removeUnreachable(List<BasicBlock> blocks, Set<BasicBlock> startingBlocks) {
    Set<BasicBlock> reached = new HashSet<>();
    WorkSet<BasicBlock> workSet = new WorkSet<>(startingBlocks);
    Map<Label, BasicBlock> blockByLabel = blocks.stream().collect(Collectors.toMap(BasicBlock::label, Function.identity()));
    while (!workSet.isEmpty()) {
      BasicBlock current = workSet.pop();
      if(reached.add(current)) {
        current.successors().forEach(l -> workSet.add(blockByLabel.get(l)));
      }
    }
    return reached;
  }

  public static UCFGBuilder createUCFGForMethod(String methodId) {
    return new UCFGBuilder(methodId);
  }

  public static Label createLabel(String labelId) {
    return new Label(labelId);
  }

  public static CallBuilder call(String methodId) {
    return new CallBuilder(methodId);
  }

  public static Expression.Variable variableWithId(String id) {
    return new Expression.Variable(id);
  }

  public static Expression.Constant constant(String value) {
    return new Expression.Constant(value);
  }

  public static Expression.ClassName clazz(String classname) {
    return new Expression.ClassName(classname);
  }

  public static Expression.FieldAccess fieldAccess(Expression.Variable field) {
    return new Expression.FieldAccess(field);
  }

  public static Expression.FieldAccess fieldAccess(Expression.Variable object, Expression.Variable field) {
    return new Expression.FieldAccess(object, field);
  }

  public static Expression.FieldAccess fieldAccess(Expression.ClassName object, Expression.Variable field) {
    return new Expression.FieldAccess(object, field);
  }

  public UCFGBuilder addMethodParam(Expression.Variable parameter) {
    parameters.add(parameter);
    return this;
  }

  public static class CallBuilder {
    private final String methodId;
    private List<Expression> arguments = new ArrayList<>();

    private CallBuilder(String methodId) {
      this.methodId = methodId;
    }

    public CallBuilder withArgs(Expression... args) {
      arguments = Arrays.asList(args);
      return this;
    }
  }
}
