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
package org.sonar.ucfg.ucfg;

import org.sonar.ucfg.LocationInFile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationInFileTest {

  private static final String FILE_KEY = "someFile.java";

  @Test
  void equals_hashcode() {
    LocationInFile loc1 = new LocationInFile(FILE_KEY, 1, 2, 3, 4);
    LocationInFile loc2 = new LocationInFile(FILE_KEY, 1, 2, 3, 4);
    LocationInFile loc3 = new LocationInFile(FILE_KEY, 1, 3, 4, 5);
    LocationInFile loc4 = new LocationInFile("someOtherFile.java", 1, 3, 4, 5);

    assertThat(loc1).isEqualTo(loc1).isEqualTo(loc2).isNotEqualTo(loc3).isNotEqualTo(loc4).isNotEqualTo(null).isNotEqualTo(new Object());
    assertThat(loc1.hashCode()).isEqualTo(loc2.hashCode()).isNotEqualTo(loc3.hashCode()).isNotEqualTo(loc4.hashCode());
  }

  @Test
  void to_string() {
    LocationInFile loc1 = new LocationInFile(FILE_KEY, 1, 2, 3, 4);
    assertThat(loc1.toString()).isEqualTo(
      "\n" +
      "         in someFile.java\n" +
      "         at 1:2 - 3:4");
  }
}
