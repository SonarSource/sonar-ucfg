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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.sonar.ucfg.UCFGBuilder.clazz;
import static org.sonar.ucfg.UCFGBuilder.constant;
import static org.sonar.ucfg.UCFGBuilder.fieldAccess;
import static org.sonar.ucfg.UCFGBuilder.newBasicBlock;
import static org.sonar.ucfg.UCFGBuilder.variableWithId;

class UCFGtoJsonTest {
  @Test
  void serialize_all_elements_in_ucfg() throws IOException {
    Expression.Variable arg = variableWithId("arg");
    UCFG ucfg = UCFGBuilder.createUCFGForMethod("A#method(Ljava/lang/String;)Ljava/lang/String;").addMethodParam(arg)
      .addStartingBlock(newBasicBlock("1")
        .assignTo(variableWithId("var1"), UCFGBuilder.call("fun").withArgs(arg))
        .jumpTo(UCFGBuilder.createLabel("2"), UCFGBuilder.createLabel("3"), UCFGBuilder.createLabel("4"), UCFGBuilder.createLabel("5")))
      .addBasicBlock(newBasicBlock("2")
        .assignTo(variableWithId("var2"), UCFGBuilder.call("fun").withArgs(arg))
        .ret(arg))
      .addBasicBlock(newBasicBlock("3")
        .assignTo(variableWithId("var3"), UCFGBuilder.call("fun").withArgs(arg))
        .ret(variableWithId("var3"), new LocationInFile("fileKey", 2, 2, 2, 10)))
      .addBasicBlock(newBasicBlock("4")
        .assignTo(fieldAccess(variableWithId("var4")), UCFGBuilder.call("__id").withArgs(fieldAccess(clazz("org.foo.A"), variableWithId("field"))))
        .ret(fieldAccess(variableWithId("target"), variableWithId("var4")), new LocationInFile("fileKey", 3, 2, 3, 10)))
      .addBasicBlock(newBasicBlock("5")
        .newObject(variableWithId("var5"), "org.foo.A")
        .assignTo(variableWithId("var5"), UCFGBuilder.call("org.foo.A#init()V").withArgs(variableWithId("var5")))
        .ret(constant("This is the end; My only friend; The end"), new LocationInFile("fileKey", 4, 2, 4, 10)))
      .build();
    String jsonString = UCFGtoJson.toJson(ucfg);
    validateJsonString(jsonString);
    assertThat(jsonString).isEqualTo(Files.readAllLines(new File("src/test/resources/serialized.json").toPath()).stream().collect(Collectors.joining("\n")));
  }

  private void validateJsonString(String json) throws IOException {
    JsonSchemaFactory f = new JsonSchemaFactory();
    String collect = Files.readAllLines(new File("src/test/resources/schema.json").toPath()).stream().collect(Collectors.joining("\n"));
    JsonSchema schema = f.getSchema(collect);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(json);

    Set<ValidationMessage> validate = schema.validate(node);
    if(!validate.isEmpty()) {
      fail(validate.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n")));
    }
  }

  @Test
  void private_constructor() throws Exception {
    assertThat(isFinal(UCFGtoJson.class.getModifiers())).isTrue();
    Constructor<UCFGtoJson> constructor = UCFGtoJson.class.getDeclaredConstructor();
    assertThat(isPrivate(constructor.getModifiers())).isTrue();
    assertThat(constructor.isAccessible()).isFalse();
    constructor.setAccessible(true);
    constructor.newInstance();
  }

}
