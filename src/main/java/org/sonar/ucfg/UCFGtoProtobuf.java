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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.ucfg.protobuf.Ucfg;

public final class UCFGtoProtobuf {

  private UCFGtoProtobuf() {
    // empty constructor
  }

  public static void toProtobufFile(UCFG ucfg, String filename) throws IOException {
    Ucfg.UCFG protobufUCFG = Ucfg.UCFG.newBuilder()
      .setMethodId(ucfg.methodId())
      .setLocation(toProtobuf(ucfg.location()))
      .addAllParameters(ucfg.parameters().stream().map(Expression.Variable::id).collect(Collectors.toList()))
      .addAllEntries(ucfg.entryBlocks().stream().map(BasicBlock::label).map(Label::id).collect(Collectors.toList()))
      .addAllBasicBlocks(ucfg.basicBlocks().values().stream().map(UCFGtoProtobuf::toProtobuf).collect(Collectors.toList()))
      .build();
    try (FileOutputStream fos = new FileOutputStream(filename)) {
      protobufUCFG.writeTo(fos);
    }

  }

  private static Ucfg.BasicBlock toProtobuf(BasicBlock basicBlock) {
    Ucfg.BasicBlock.Builder builder = Ucfg.BasicBlock.newBuilder()
      .setId(basicBlock.label().id())
      .setLocation(toProtobuf(basicBlock.locationInFile()))
      .addAllInstructions(basicBlock.instructions().stream().map(i -> {
          if (i.type() == UCFGElement.UCFGElementType.CALL) {
            return toProtobuf((UCFGElement.AssignCall) i);
          }
          return toProtobuf((UCFGElement.NewObject) i);
        }).collect(Collectors.toList()));

    if (basicBlock.terminator().type() == UCFGElement.UCFGElementType.RET) {
      UCFGElement.Ret ret = (UCFGElement.Ret) basicBlock.terminator();
      builder.setRet(Ucfg.Return.newBuilder().setLocation(toProtobuf(ret.location())).setReturnedExpression(toProtobuf(ret.getReturnedExpression())));
    } else {
      Ucfg.Jump.Builder jump = Ucfg.Jump.newBuilder();
      UCFGElement.Jump terminator = (UCFGElement.Jump) basicBlock.terminator();
      jump.addAllDestinations(terminator.destinations().stream().map(Label::id).collect(Collectors.toList()));
      builder.setJump(jump);
    }
    return builder.build();
  }

  private static Ucfg.Instruction toProtobuf(UCFGElement.AssignCall assignCall) {
    Ucfg.AssignCall.Builder builder = Ucfg.AssignCall.newBuilder()
      .setLocation(toProtobuf(assignCall.location()))
      .setMethodId(assignCall.getMethodId())
      .addAllArgs(assignCall.getArgExpressions().stream().map(UCFGtoProtobuf::toProtobuf).collect(Collectors.toList()));
    Expression lhs = assignCall.getLhs();

    if (lhs instanceof Expression.Variable) {
      builder.setVariable(toProtobuf((Expression.Variable) lhs));
    } else {
      builder.setFieldAccess(toProtobuf((Expression.FieldAccess) lhs));
    }
    return Ucfg.Instruction.newBuilder().setAssigncall(builder.build()).build();
  }

  private static Ucfg.Instruction toProtobuf(UCFGElement.NewObject newObject) {
    Ucfg.NewObject.Builder builder = Ucfg.NewObject.newBuilder()
      .setLocation(toProtobuf(newObject.location()))
      .setType(newObject.instanceType());

    Expression lhs = newObject.getLhs();
    if (lhs instanceof Expression.Variable) {
      builder.setVariable(toProtobuf((Expression.Variable) lhs));
    } else {
      builder.setFieldAccess(toProtobuf((Expression.FieldAccess) lhs));
    }
    return Ucfg.Instruction.newBuilder().setNewObject(builder.build()).build();
  }

  private static Ucfg.Expression toProtobuf(Expression expression) {
    Ucfg.Expression.Builder builder = Ucfg.Expression.newBuilder();
    if (expression.isConstant()) {
      builder.setConst(toProtobuf((Expression.Constant) expression));
    } else if(expression == Expression.THIS) {
      builder.setThis(toProtobufThis());
    } else if (expression instanceof Expression.ClassName) {
      builder.setClassname(toProtobuf((Expression.ClassName) expression));
    } else if (expression instanceof Expression.FieldAccess) {
      builder.setFieldAccess(toProtobuf((Expression.FieldAccess) expression));
    } else {
      builder.setVar(toProtobuf((Expression.Variable) expression));
    }
    return builder.build();
  }

  private static Ucfg.Constant toProtobuf(Expression.Constant constant) {
    return Ucfg.Constant.newBuilder().setValue(constant.value()).build();
  }

  private static Ucfg.Variable toProtobuf(Expression.Variable variable) {
    return Ucfg.Variable.newBuilder().setName(variable.id()).build();
  }

  private static Ucfg.ClassName toProtobuf(Expression.ClassName className) {
    return Ucfg.ClassName.newBuilder().setClassname(className.typeName()).build();
  }

  private static Ucfg.This toProtobufThis() {
    return Ucfg.This.newBuilder().build();
  }

  private static Ucfg.FieldAccess toProtobuf(Expression.FieldAccess fieldAccess) {
    Ucfg.FieldAccess.Builder fieldAccessBuilder = Ucfg.FieldAccess.newBuilder();
    Expression object = fieldAccess.object();
    if (object == Expression.THIS) {
      fieldAccessBuilder.setThis(toProtobufThis());
    } else if (object instanceof Expression.Variable) {
      fieldAccessBuilder.setObject(toProtobuf((Expression.Variable) object));
    } else {
      fieldAccessBuilder.setClassname(toProtobuf((Expression.ClassName) object));
    }
    fieldAccessBuilder.setField(toProtobuf(fieldAccess.field()));
    return fieldAccessBuilder.build();
  }

  private static Ucfg.Location toProtobuf(@Nullable LocationInFile locationInFile) {
    if (locationInFile == null) {
      return Ucfg.Location.getDefaultInstance();
    }
    return Ucfg.Location.newBuilder()
      .setFileId(locationInFile.getFileId())
      .setStartLine(locationInFile.getStartLine())
      .setStartLineOffset(locationInFile.getStartLineOffset())
      .setEndLine(locationInFile.getEndLine())
      .setEndLineOffset(locationInFile.getEndLineOffset())
      .build();
  }
  public static UCFG fromProtobufFile(File protobufFile) throws IOException {
    try (FileInputStream fis = new FileInputStream(protobufFile)) {
      return deserializeUcfg(fis);
    }
  }

  private static UCFG deserializeUcfg(FileInputStream fis) throws IOException {
    String methodId = "";
    try {
      Ucfg.UCFG ucfg = Ucfg.UCFG.parseFrom(fis);
      methodId = ucfg.getMethodId();
      UCFGBuilder builder = UCFGBuilder.createUCFGForMethod(methodId).at(fromProtobuf(ucfg.getLocation()));
      ucfg.getParametersList().forEach(pId -> builder.addMethodParam(UCFGBuilder.variableWithId(pId)));

      Map<String, UCFGBuilder.BlockBuilder> blockById = ucfg.getBasicBlocksList().stream().collect(Collectors.toMap(Ucfg.BasicBlock::getId, UCFGtoProtobuf::fromProtobuf));
      for (Map.Entry<String, UCFGBuilder.BlockBuilder> entry : blockById.entrySet()) {
        if (ucfg.getEntriesList().contains(entry.getKey())) {
          builder.addStartingBlock(entry.getValue());
        } else {
          builder.addBasicBlock(entry.getValue());
        }
      }
      return builder.build();
    } catch (Exception e) {
      throw new IllegalStateException("An error occured while deserializing UCFG for method "+methodId, e);
    }
  }

  private static UCFGBuilder.BlockBuilder fromProtobuf(Ucfg.BasicBlock bb) {
    UCFGBuilder.BlockBuilder blockBuilder = UCFGBuilder.newBasicBlock(bb.getId(), fromProtobuf(bb.getLocation()));

    bb.getInstructionsList().forEach(i -> {
      if (i.hasAssigncall()) {
        fromProtobuf(blockBuilder, i.getAssigncall());
      } else if (i.hasNewObject()) {
        fromProtobuf(blockBuilder, i.getNewObject());
      }
    });

    if (bb.hasJump()) {
      Ucfg.Jump jump = bb.getJump();
      blockBuilder.jumpTo(jump.getDestinationsList().stream().map(UCFGBuilder::createLabel).toArray(Label[]::new));
    }
    if (bb.hasRet()) {
      Ucfg.Return ret = bb.getRet();
      blockBuilder.ret(fromProtobuf(ret.getReturnedExpression()), fromProtobuf(ret.getLocation()));
    }
    return blockBuilder;
  }

  private static Expression.Variable fromProtobuf(Ucfg.Variable variable) {
    return UCFGBuilder.variableWithId(variable.getName());
  }

  private static Expression.FieldAccess fromProtobuf(Ucfg.FieldAccess fieldAccess) {
    Expression.Variable field = fromProtobuf(fieldAccess.getField());
    if (fieldAccess.hasThis()) {
      return UCFGBuilder.fieldAccess(field);
    } else if (fieldAccess.hasClassname()) {
      return UCFGBuilder.fieldAccess(fromProtobuf(fieldAccess.getClassname()), field);
    }
    return UCFGBuilder.fieldAccess(fromProtobuf(fieldAccess.getObject()), field);
  }

  private static Expression.ClassName fromProtobuf(Ucfg.ClassName className) {
    return UCFGBuilder.clazz(className.getClassname());
  }

  private static UCFGBuilder.BlockBuilder fromProtobuf(UCFGBuilder.BlockBuilder blockBuilder, Ucfg.AssignCall call) {
    UCFGBuilder.CallBuilder callBuilder = UCFGBuilder.call(call.getMethodId()).withArgs(call.getArgsList().stream().map(UCFGtoProtobuf::fromProtobuf).toArray(Expression[]::new));
    if (call.hasVariable()) {
      return blockBuilder.assignTo(fromProtobuf(call.getVariable()), callBuilder, fromProtobuf(call.getLocation()));
    }
    return blockBuilder.assignTo(fromProtobuf(call.getFieldAccess()), callBuilder, fromProtobuf(call.getLocation()));
  }

  private static UCFGBuilder.BlockBuilder fromProtobuf(UCFGBuilder.BlockBuilder blockBuilder, Ucfg.NewObject newObject) {
    if (newObject.hasVariable()) {
      return blockBuilder.newObject(fromProtobuf(newObject.getVariable()), newObject.getType(), fromProtobuf(newObject.getLocation()));
    }
    return blockBuilder.newObject(fromProtobuf(newObject.getFieldAccess()), newObject.getType(), fromProtobuf(newObject.getLocation()));
  }

  private static Expression fromProtobuf(Ucfg.Expression expr) {
    if (expr.hasConst()) {
      return UCFGBuilder.constant(expr.getConst().getValue());
    }
    if (expr.hasThis()) {
      return Expression.THIS;
    }
    if (expr.hasClassname()) {
      return fromProtobuf(expr.getClassname());
    }
    if (expr.hasFieldAccess()) {
      return fromProtobuf(expr.getFieldAccess());
    }
    return fromProtobuf(expr.getVar());
  }

  private static LocationInFile fromProtobuf(Ucfg.Location location) {
    if(location.equals(Ucfg.Location.getDefaultInstance())) {
      return null;
    }
    return new LocationInFile(location.getFileId(), location.getStartLine(), location.getStartLineOffset(), location.getEndLine(), location.getEndLineOffset());
  }


}
