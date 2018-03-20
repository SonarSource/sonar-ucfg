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

import org.sonar.ucfg.UCFG;
import org.sonar.ucfg.UCFGBuilder;
import org.sonar.ucfg.UCFGPrinter;
import org.junit.jupiter.api.Test;

import static org.sonar.ucfg.UCFGBuilder.call;
import static org.sonar.ucfg.UCFGBuilder.constant;
import static org.sonar.ucfg.UCFGBuilder.createUCFGForMethod;
import static org.sonar.ucfg.UCFGBuilder.createLabel;
import static org.sonar.ucfg.UCFGBuilder.newBasicBlock;
import static org.sonar.ucfg.UCFGBuilder.variableWithId;
import static org.assertj.core.api.Assertions.assertThat;

class UCFGPrinterTest {

  @Test
  public void print_call_and_ret() {
    UCFGBuilder ucfgBuilder = createUCFGForMethod("outerSignature").addMethodParam(variableWithId("p1"));
    ucfgBuilder.addBasicBlock(UCFGBuilder.newBasicBlock("startLabel")
      .assignTo(variableWithId("var1"), call("signature").withArgs(variableWithId("arg0")))
      .ret(constant("const")));
    UCFG simpleCall = ucfgBuilder.build();
    assertThat(new UCFGPrinter(simpleCall).toString()).isEqualTo(
      "Signature: outerSignature\n" +
        "Parameters:\n" +
        "  #0 -> p1\n" +
        "Instructions:\n" +
        "  label startLabel:\n" +
        "    call var1 = signature (arg0)\n"+
        "         in __unknown_file\n" +
        "         at 1:1 - 1:1\n" +
        "     ret \"const\"\n" +
        "         in __unknown_file\n" +
        "         at 1:1 - 1:1"
    );


    ucfgBuilder = createUCFGForMethod("outerSignature").addMethodParam(variableWithId("p1"));
    ucfgBuilder.addBasicBlock(newBasicBlock("startLabel")
        .assignTo(variableWithId("var1"), call("signature").withArgs(variableWithId("arg0")))
        .ret(variableWithId("var1")));
    UCFG callAndRet = ucfgBuilder.build();
    assertThat(new UCFGPrinter(callAndRet).toString()).isEqualTo(
      "Signature: outerSignature\n" +
        "Parameters:\n" +
        "  #0 -> p1\n" +
        "Instructions:\n" +
        "  label startLabel:\n" +
        "    call var1 = signature (arg0)\n" +
        "         in __unknown_file\n" +
        "         at 1:1 - 1:1\n" +
        "     ret var1\n" +
        "         in __unknown_file\n" +
        "         at 1:1 - 1:1");
  }

  @Test
  void print_label_and_jump() {
    UCFGBuilder ucfgBuilder = UCFGBuilder.createUCFGForMethod("outerSignature").addMethodParam(variableWithId("p1"));
    ucfgBuilder.addBasicBlock(newBasicBlock("2").assignTo(variableWithId("var1"), call("signature").withArgs(variableWithId("arg0"))).jumpTo(createLabel("2")));
    assertThat(new UCFGPrinter(ucfgBuilder.build()).toString()).isEqualTo(
      "Signature: outerSignature\n" +
        "Parameters:\n" +
        "  #0 -> p1\n" +
        "Instructions:\n" +
        "  label 2:\n" +
        "    call var1 = signature (arg0)\n" +
        "         in __unknown_file\n" +
        "         at 1:1 - 1:1\n" +
        "    jump 2");
  }
}
