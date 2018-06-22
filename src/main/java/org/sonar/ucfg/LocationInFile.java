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

import java.util.Objects;

public class LocationInFile {

  private final String fileId;
  private final int startLine;
  private final int startLineOffset;
  private final int endLine;
  private final int endLineOffset;
  private final int hash;

  public LocationInFile(String fileId, int startLine, int startLineOffset, int endLine, int endLineOffset) {
    this.fileId = fileId;
    this.startLine = startLine;
    this.startLineOffset = startLineOffset;
    this.endLine = endLine;
    this.endLineOffset = endLineOffset;
    this.hash = Objects.hash(fileId, startLine, startLineOffset, endLine, endLineOffset);
  }

  public String getFileId() {
    return fileId;
  }

  public int getStartLine() {
    return startLine;
  }

  public int getStartLineOffset() {
    return startLineOffset;
  }

  public int getEndLine() {
    return endLine;
  }

  public int getEndLineOffset() {
    return endLineOffset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationInFile that = (LocationInFile) o;
    return startLine == that.startLine &&
        startLineOffset == that.startLineOffset &&
        endLine == that.endLine &&
        endLineOffset == that.endLineOffset &&
        Objects.equals(fileId, that.fileId);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return "\n         in " + fileId + '\n' +
      "         at " + startLine + ':' + startLineOffset + " - " + endLine + ':' + endLineOffset;
  }
}
