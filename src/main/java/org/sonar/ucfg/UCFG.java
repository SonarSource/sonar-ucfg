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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.ucfg.util.WorkSet;

public class UCFG {

  private final String methodId;
  private final List<Expression.Variable> parameters;
  private Map<Label, BasicBlock> basicBlocks;
  private Map<Label, BasicBlock> nonRedundantGraph;
  private Set<BasicBlock> nonRedundantEntryBlocks;
  private LocationInFile location;
  private boolean requiresDeadEnd = false;

  public UCFG(String methodId, List<Expression.Variable> parameters, Set<BasicBlock> basicBlocks, Set<BasicBlock> entryBlocks, LocationInFile location) {
    this.methodId = methodId;
    this.parameters = parameters;
    this.basicBlocks = basicBlocks.stream().collect(Collectors.toMap(BasicBlock::label, Function.identity()));
    this.location = location;
    computeFilteredGraph(entryBlocks);
  }

  public String methodId() {
    return methodId;
  }

  public List<Expression.Variable> parameters() {
    return parameters;
  }

  private void computeFilteredGraph(Set<BasicBlock> entryBlocks) {
    nonRedundantEntryBlocks = entryBlocks.stream().flatMap(bb -> {
      if(bb.isRedundant()) {
        return nonRedundantSuccessors(bb).stream();
      } else {
        return Stream.of(bb);
      }
    }).collect(Collectors.toSet());
    Map<BasicBlock, Set<BasicBlock>> newSuccessors = basicBlocks.values().stream()
      .filter(b -> !b.isRedundant())
      .collect(Collectors.toMap(Function.identity(), this::nonRedundantSuccessors));
    newSuccessors.forEach(BasicBlock::updateSuccs);
    nonRedundantGraph = newSuccessors.keySet().stream().collect(Collectors.toMap(BasicBlock::label, Function.identity()));
    if (requiresDeadEnd) {
      nonRedundantGraph.put(BasicBlock.DEAD_END.label(), BasicBlock.DEAD_END);
      basicBlocks.put(BasicBlock.DEAD_END.label(), BasicBlock.DEAD_END);
    }
    if(nonRedundantGraph.values().stream().anyMatch(BasicBlock::isRedundant)) {
      throw new IllegalStateException("Pruned graph should not contain any redundant blocks.");
    }
  }

  private Set<BasicBlock> nonRedundantSuccessors(BasicBlock basicBlock) {
    Set<BasicBlock> res = new HashSet<>();
    Set<BasicBlock> seen = new HashSet<>();
    WorkSet<BasicBlock> workSet = new WorkSet<>(successors(basicBlock));
    while (!workSet.isEmpty()) {
      BasicBlock succ = workSet.pop();
      if(!seen.add(succ)) {
        continue;
      }
      if (succ.isRedundant()) {
        workSet.addAll(successors(succ));
      } else {
        res.add(succ);
      }
    }
    if (!seen.isEmpty() && res.isEmpty()) {
      // found a cycle where everything is redundant
      this.requiresDeadEnd = true;
      return Collections.singleton(BasicBlock.DEAD_END);
    }
    return res;
  }

  private Set<BasicBlock> successors(BasicBlock b) {
    return b.successors().stream().map(basicBlocks::get).collect(Collectors.toSet());
  }

  public Map<Label, BasicBlock> basicBlocks() {
    return nonRedundantGraph;
  }

  public Set<BasicBlock> entryBlocks() {
    return nonRedundantEntryBlocks;
  }

  public LocationInFile location() {
    return location;
  }
}
