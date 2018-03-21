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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.ucfg.UCFGBuilder.constant;
import static org.sonar.ucfg.UCFGBuilder.createLabel;
import static org.sonar.ucfg.UCFGBuilder.newBasicBlock;
import static org.sonar.ucfg.UCFGBuilder.variableWithId;

class UCFGtoProtobufTest {


  @Test
  void serialize_and_deserialize_ucfg() throws IOException {
    Expression.Variable parameter1 = UCFGBuilder.variableWithId("parameter1");
    UCFGBuilder ucfgBuilder = UCFGBuilder.createUCFGForMethod("myMethod").addMethodParam(parameter1);
    Expression.Variable var1 = UCFGBuilder.variableWithId("var1");
    ucfgBuilder.addStartingBlock(newBasicBlock("startLabel", new LocationInFile("fileKey", 1,1, 1,12))
      .assignTo(var1, UCFGBuilder.call("__id").withArgs(parameter1), new LocationInFile("fileKey", 1,1, 1,12))
      .jumpTo(createLabel("label2")));
    Expression.Variable var2 = variableWithId("var2");
    ucfgBuilder.addBasicBlock(newBasicBlock("label2", new LocationInFile("fileKey", 2, 1, 2,12))
      .assignTo(var2,UCFGBuilder.call("__id").withArgs(constant("AConstant")), new LocationInFile("fileKey", 2, 1, 2,12))
      .ret(var2));
    UCFG ucfg = ucfgBuilder.build();


    String filename = "target/serialized_ucfg.protobuf";
    File file = new File(filename);
    file.delete();
    UCFGtoProtobuf.toProtobufFile(ucfg, filename);
    assertThat(file).exists();
    assertThat(Files.size(file.toPath())).isGreaterThan(0);
    UCFG read_ucfg = UCFGtoProtobuf.fromProtobufFile(filename);
    assertThat(read_ucfg).isNotNull();

    assertThat(read_ucfg.basicBlocks().keySet()).containsExactlyElementsOf(ucfg.basicBlocks().keySet());
    assertThat(read_ucfg.basicBlocks().values()).containsExactlyElementsOf(ucfg.basicBlocks().values());
    assertThat(read_ucfg.basicBlocks().values().stream().map(BasicBlock::toString)).containsExactlyElementsOf(ucfg.basicBlocks().values().stream().map(BasicBlock::toString).collect(Collectors.toList()));

    file.delete();
  }
}