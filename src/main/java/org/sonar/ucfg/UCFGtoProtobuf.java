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
      .addAllInstructions(basicBlock.calls().stream().map(UCFGtoProtobuf::toProtobuf).collect(Collectors.toList()));

    if (basicBlock.terminator().type() == Instruction.InstructionType.RET) {
      Instruction.Ret ret = (Instruction.Ret) basicBlock.terminator();
      builder.setRet(Ucfg.Return.newBuilder().setLocation(toProtobuf(ret.location())).setReturnedExpression(toProtobuf(ret.getReturnedExpression())));
    } else {
      Ucfg.Jump.Builder jump = Ucfg.Jump.newBuilder();
      Instruction.Jump terminator = (Instruction.Jump) basicBlock.terminator();
      jump.addAllDestinations(terminator.destinations().stream().map(Label::id).collect(Collectors.toList()));
      builder.setJump(jump);
    }
    return builder.build();
  }

  private static Ucfg.Instruction toProtobuf(Instruction.AssignCall assignCall) {
    return Ucfg.Instruction.newBuilder().setLocation(toProtobuf(assignCall.location()))
      .setVariable(assignCall.getLhs().id())
      .setMethodId(assignCall.getMethodId())
      .addAllArgs(assignCall.getArgExpressions().stream().map(UCFGtoProtobuf::toProtobuf).collect(Collectors.toList()))
      .build();
  }

  private static Ucfg.Expression toProtobuf(Expression expression) {
    Ucfg.Expression.Builder builder = Ucfg.Expression.newBuilder();
    if (expression.isConstant()) {
      builder.setConst(Ucfg.Constant.newBuilder().setValue(((Expression.Constant) expression).value()).build());
    } else {
      builder.setVar(Ucfg.Variable.newBuilder().setName(((Expression.Variable) expression).id()).build());
    }
    return builder.build();
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

  public static UCFG fromProtobufFile(String filename) throws IOException {
    try (FileInputStream fis = new FileInputStream(filename)) {
      Ucfg.UCFG ucfg = Ucfg.UCFG.parseFrom(fis);
      UCFGBuilder builder = UCFGBuilder.createUCFGForMethod(ucfg.getMethodId()).at(fromProtobuf(ucfg.getLocation()));
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
    }
  }

  private static UCFGBuilder.BlockBuilder fromProtobuf(Ucfg.BasicBlock bb) {
    UCFGBuilder.BlockBuilder blockBuilder = UCFGBuilder.newBasicBlock(bb.getId(), fromProtobuf(bb.getLocation()));

    bb.getInstructionsList().forEach(
      i -> blockBuilder.assignTo(UCFGBuilder.variableWithId(i.getVariable()),
        UCFGBuilder.call(i.getMethodId()).withArgs(i.getArgsList().stream().map(UCFGtoProtobuf::fromProtobuf).toArray(Expression[]::new)),
        fromProtobuf(i.getLocation())));

    if(bb.hasJump()) {
      Ucfg.Jump jump = bb.getJump();
      blockBuilder.jumpTo(jump.getDestinationsList().stream().map(UCFGBuilder::createLabel).toArray(Label[]::new));
    }
    if(bb.hasRet()) {
      Ucfg.Return ret = bb.getRet();
      blockBuilder.ret(fromProtobuf(ret.getReturnedExpression()), fromProtobuf(ret.getLocation()));
    }
    return blockBuilder;
  }

  private static Expression fromProtobuf(Ucfg.Expression expr) {
    if(expr.hasConst()) {
      return UCFGBuilder.constant(expr.getConst().getValue());
    }
    return UCFGBuilder.variableWithId(expr.getVar().getName());
  }

  private static LocationInFile fromProtobuf(Ucfg.Location location) {
    if(location.equals(Ucfg.Location.getDefaultInstance())) {
      return null;
    }
    return new LocationInFile(location.getFileId(), location.getStartLine(), location.getStartLineOffset(), location.getEndLine(), location.getEndLineOffset());
  }


}
