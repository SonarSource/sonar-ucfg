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

public class DumpToTxt {

  public static void main(String[] args) {
    File ucfgDir = new File("/home/nicolasperu/Development/SonarSource/ucfg_cs_tobias");
    File txtDir = new File(ucfgDir, "textFormat");
    txtDir.mkdir();
    for (File file : ucfgDir.listFiles()) {
      try {
        if(file.isFile()) {
          UCFG ucfg = UCFGtoProtobuf.fromProtobufFile(file);
          UCFGPrinter ucfgPrinter = new UCFGPrinter(ucfg);
          Files.write(new File(txtDir, file.getName()+".txt").toPath(), ucfgPrinter.toString().getBytes());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


}
