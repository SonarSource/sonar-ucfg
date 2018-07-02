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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.ucfg.protobuf.Ucfg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.sonar.ucfg.UCFGBuilder.clazz;
import static org.sonar.ucfg.UCFGBuilder.constant;
import static org.sonar.ucfg.UCFGBuilder.createLabel;
import static org.sonar.ucfg.UCFGBuilder.fieldAccess;
import static org.sonar.ucfg.UCFGBuilder.newBasicBlock;
import static org.sonar.ucfg.UCFGBuilder.variableWithId;

class UCFGtoProtobufTest {


  @Test
  void serialize_and_deserialize_ucfg() throws IOException {
    Expression.Variable parameter1 = UCFGBuilder.variableWithId("parameter1");
    UCFGBuilder ucfgBuilder = UCFGBuilder.createUCFGForMethod("myMethod").addMethodParam(parameter1);
    Expression.Variable var1 = UCFGBuilder.variableWithId("var1");
    ucfgBuilder.addStartingBlock(newBasicBlock("startLabel", null)
      .newObject(var1, "java.lang.Object", new LocationInFile("fileKey", 1,1, 1,12))
      .assignTo(var1, UCFGBuilder.call("__id").withArgs(parameter1), new LocationInFile("fileKey", 1,1, 1,12))
      .jumpTo(createLabel("label2"), createLabel("label3"), createLabel("label4")));
    Expression.Variable var2 = variableWithId("var2");
    ucfgBuilder.addBasicBlock(newBasicBlock("label2", new LocationInFile("fileKey", 2, 1, 2,12))
      .assignTo(var2,UCFGBuilder.call("__id").withArgs(constant("AConstant")), new LocationInFile("fileKey", 2, 1, 2,12))
      .ret(var2));
    ucfgBuilder.addBasicBlock(newBasicBlock("label3", new LocationInFile("fileKey", 3, 1, 3,12))
      .assignTo(var2,UCFGBuilder.call("__id").withArgs(Expression.THIS, clazz("myclass")), new LocationInFile("fileKey", 3, 1, 3,12))
      .ret(var2));
    Expression.Variable obj = variableWithId("obj");
    Expression.Variable field = variableWithId("field");
    Expression.Variable var3 = variableWithId("var3");
    ucfgBuilder.addBasicBlock(newBasicBlock("label4", new LocationInFile("fileKey", 4, 1, 4, 12))
      .assignTo(var3, UCFGBuilder.call("__id").withArgs(fieldAccess(obj, field)), new LocationInFile("fileKey", 4, 1, 4, 12))
      .assignTo(var3, UCFGBuilder.call("__id").withArgs(fieldAccess(field)), new LocationInFile("fileKey", 4, 1, 4, 12))
      .ret(var3));
    UCFG ucfg = ucfgBuilder.build();


    String filename = "target/serialized_ucfg.protobuf";
    File file = new File(filename);
    file.delete();
    UCFGtoProtobuf.toProtobufFile(ucfg, filename);
    assertThat(file).exists();
    assertThat(Files.size(file.toPath())).isGreaterThan(0);
    UCFG read_ucfg = UCFGtoProtobuf.fromProtobufFile(file);
    assertThat(read_ucfg).isNotNull();

    assertThat(read_ucfg.basicBlocks().keySet()).containsExactlyElementsOf(ucfg.basicBlocks().keySet());
    assertThat(read_ucfg.basicBlocks().values()).containsExactlyElementsOf(ucfg.basicBlocks().values());
    assertThat(read_ucfg.basicBlocks().values().stream().map(BasicBlock::toString)).containsExactlyElementsOf(ucfg.basicBlocks().values().stream().map(BasicBlock::toString).collect(Collectors.toList()));
    assertThat(read_ucfg.basicBlocks().values().stream().filter(bb->bb.locationInFile() == null).map(bb ->bb.label().id())).containsOnly("startLabel");
    file.delete();
  }


  @Test
  void error_when_deserializing() throws IOException {
    // construct a wrong ucfg
    Ucfg.UCFG.Builder builder = Ucfg.UCFG.newBuilder().setMethodId("someMethodId.wrong");
    builder.addBasicBlocks(builder.addBasicBlocksBuilder());
    Ucfg.UCFG protobufUCFG = builder.build();
    try (FileOutputStream fos = new FileOutputStream("serialized.wrong.pb")) {
      protobufUCFG.writeTo(fos);
    }

    try {

      UCFG read_ucfg = UCFGtoProtobuf.fromProtobufFile(new File("serialized.wrong.pb"));
      fail("some error should have occured");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("An error occured while deserializing UCFG for method someMethodId.wrong");
    } catch (Exception e) {
      fail("Wrong type of exception was thrown");
    }
  }
}
