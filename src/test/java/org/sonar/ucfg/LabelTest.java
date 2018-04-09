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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LabelTest {

  @Test
  void test_toString() {
    Label label = new Label("1");
    assertThat(label.toString()).isEqualTo("  label 1:");
  }

  @Test
  void label_equality() {
    Label label1 = new Label("1");
    Label label2 = new Label("2");
    assertThat(label1).isEqualTo(label1).isNotEqualTo(label2).isNotEqualTo(new Object()).isNotEqualTo(null);
  }
}
