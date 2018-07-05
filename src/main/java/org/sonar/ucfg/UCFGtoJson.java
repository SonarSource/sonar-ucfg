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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

public final class UCFGtoJson {

  private UCFGtoJson() {
    // empty constructor
  }

  public static String toJson(UCFG ucfg) {
    GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
    gson.registerTypeAdapter(UCFG.class, new UcfgSerializer());
    gson.registerTypeAdapter(Label.class, new LabelSerializer());
    gson.registerTypeAdapter(Expression.Variable.class, new VariableSerializer());
    gson.registerTypeAdapter(Expression.Constant.class, new ConstantSerializer());
    gson.registerTypeAdapter(BasicBlock.class, new BasicBlockSerializer());
    gson.registerTypeAdapter(UCFGElement.AssignCall.class, new AssignCallSerializer());
    gson.registerTypeAdapter(UCFGElement.Ret.class, new ReturnSerializer());
    gson.registerTypeAdapter(UCFGElement.Jump.class, new JumpSerializer());
    gson.registerTypeAdapter(LocationInFile.class, new LocationSerializer());

    return gson.create().toJson(ucfg);
  }

  private static class UcfgSerializer implements JsonSerializer<UCFG> {
    @Override
    public JsonElement serialize(UCFG ucfg, Type type, JsonSerializationContext jsonSerializationContext) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("ucfgId", ucfg.methodId());
      jsonObject.add("ucfgLoc", jsonSerializationContext.serialize(ucfg.location()));
      jsonObject.add("ucfgParams", jsonSerializationContext.serialize(ucfg.parameters().stream().map(Expression.Variable::id).collect(Collectors.toList())));
      jsonObject.add("ucfgEntries", jsonSerializationContext.serialize(ucfg.entryBlocks().stream().map(BasicBlock::label).collect(Collectors.toSet())));
      jsonObject.add("ucfgBody", jsonSerializationContext.serialize(ucfg.basicBlocks().values()));
      return jsonObject;
    }
  }

  private static class LabelSerializer implements JsonSerializer<Label> {
    @Override
    public JsonElement serialize(Label src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.id());
    }
  }

  private static class VariableSerializer implements JsonSerializer<Expression.Variable> {
    @Override
    public JsonElement serialize(Expression.Variable src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("tag", "Var");
      jsonObject.addProperty("varName", src.id());
      return jsonObject;
    }
  }
  private static class ConstantSerializer implements JsonSerializer<Expression.Constant> {
    @Override
    public JsonElement serialize(Expression.Constant src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("tag", "Const");
      jsonObject.addProperty("constValue", src.toString());
      return jsonObject;
    }
  }

  private static class BasicBlockSerializer implements JsonSerializer<BasicBlock> {
    @Override
    public JsonElement serialize(BasicBlock src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("bbId", context.serialize(src.label()));
      jsonObject.add("bbLoc", context.serialize(src.locationInFile()));
      jsonObject.add("bbInstr", context.serialize(src.instructions()));
      jsonObject.add("bbTerm", context.serialize(src.terminator()));
      return jsonObject;
    }
  }

  private static class AssignCallSerializer implements JsonSerializer<UCFGElement.AssignCall> {
    @Override
    public JsonElement serialize(UCFGElement.AssignCall src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("instrLoc", context.serialize(src.location()));
      jsonObject.add("instrLhs", context.serialize(src.getLhs()));
      jsonObject.add("instrMeth", context.serialize(src.getMethodId()));
      jsonObject.add("instrArgs", context.serialize(src.getArgExpressions()));
      return jsonObject;
    }
  }

  private static class LocationSerializer implements JsonSerializer<LocationInFile> {
    @Override
    public JsonElement serialize(LocationInFile src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("locFileId", src.getFileId());
      jsonObject.addProperty("locStartLine", src.getStartLine());
      jsonObject.addProperty("locStartLineOffset", src.getStartLineOffset());
      jsonObject.addProperty("locEndLine", src.getEndLine());
      jsonObject.addProperty("locEndLineOffset", src.getEndLineOffset());
      return jsonObject;
    }
  }

  private static class ReturnSerializer implements JsonSerializer<UCFGElement.Ret> {
    @Override
    public JsonElement serialize(UCFGElement.Ret src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("tag", "Ret");
      jsonObject.add("retLoc", context.serialize(src.location()));
      jsonObject.add("retExpr", context.serialize(src.getReturnedExpression()));
      return jsonObject;
    }
  }

  private static class JumpSerializer implements JsonSerializer<UCFGElement.Jump> {
    @Override
    public JsonElement serialize(UCFGElement.Jump src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("tag", "Jump");
      jsonObject.add("jumpDest", context.serialize(src.destinations()));
      return jsonObject;
    }
  }
}
