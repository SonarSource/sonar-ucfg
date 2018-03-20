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

import java.util.stream.Collectors;

public class UCFGPrinter {

  private final UCFG ucfg;

  public UCFGPrinter(UCFG ucfg) {
    this.ucfg = ucfg;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Signature: ");
    sb.append(ucfg.methodId());
    sb.append('\n');

    sb.append("Parameters:\n");
    for (int i = 0; i < ucfg.parameters().size(); i++) {
      sb.append("  #");
      sb.append(i);
      sb.append(" -> ");
      sb.append(ucfg.parameters().get(i));
      sb.append('\n');
    }

    sb.append("Instructions:\n");
    sb.append(ucfg.basicBlocks().values().stream().map(BasicBlock::toString).collect(Collectors.joining("\n")));
    return sb.toString();
  }

}
