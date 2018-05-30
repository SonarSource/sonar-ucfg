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
import static org.sonar.ucfg.UCFGBuilder.call;
import static org.sonar.ucfg.UCFGBuilder.constant;
import static org.sonar.ucfg.UCFGBuilder.createLabel;
import static org.sonar.ucfg.UCFGBuilder.createUCFGForMethod;
import static org.sonar.ucfg.UCFGBuilder.newBasicBlock;
import static org.sonar.ucfg.UCFGBuilder.variableWithId;

public class UCFGTest {

  @Test
  public void redundant_cycles_are_replaced_by_DEAD_END() {
    /*
     * => start --> call --> startLoop --+
     * . . | . . . . . . . . . . ^ . . . |
     * . . v . . . . . . . . . . | . . . v
     * .. exit . . . . . . . . . +-- endLoop
     */
    Label start = createLabel("start");
    Label call = createLabel("call");
    Label startLoop = createLabel("startLoop");
    Label endLoop = createLabel("endLoop");
    Label exit = createLabel("exit");

    UCFGBuilder builder = createUCFGForMethod("foo").addMethodParam(variableWithId("arg0"));
    builder.addBasicBlock(newBasicBlock(start.id()).jumpTo(call, exit));
    builder.addBasicBlock(newBasicBlock(call.id()).assignTo(variableWithId("var1"), call("signature").withArgs(variableWithId("arg0"))).jumpTo(startLoop));
    builder.addBasicBlock(newBasicBlock(startLoop.id()).jumpTo(endLoop));
    builder.addBasicBlock(newBasicBlock(endLoop.id()).jumpTo(startLoop));
    builder.addBasicBlock(newBasicBlock(exit.id()).ret(constant("implicit return")));

    UCFG ucfg = builder.build();

    /*
     * => call --> DEAD_END ---+
     * . . . . . . . . ^ . . . |
     * . . . . . . . . | . . . |
     * => exit . . . . +------ +
     */
    assertThat(ucfg.entryBlocks().stream().map(BasicBlock::label)).containsOnly(call, exit);
    assertThat(ucfg.basicBlocks().keySet())
      .doesNotContain(start, startLoop, endLoop)
      .containsOnly(call, exit, BasicBlock.DEAD_END.label());
    assertThat(ucfg.basicBlocks().get(call).successors()).containsOnly(BasicBlock.DEAD_END.label());
  }

}
