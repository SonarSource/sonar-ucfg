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
package org.sonar.ucfg.util;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkSetTest {

  @Test
  void empty() {
    WorkSet<String> w = new WorkSet<>();
    assertThat(w.isEmpty()).isTrue();
    Assertions.assertThrows(IllegalStateException.class, w::pop);
  }

  @Test
  void single_element() {
    WorkSet<String> w = new WorkSet<>();
    w.add("abc");
    assertThat(w.isEmpty()).isFalse();
    String popped = w.pop();
    assertThat(popped).isEqualTo("abc");
    assertThat(w.isEmpty()).isTrue();
  }

  @Test
  void several_elements() {
    WorkSet<String> w = new WorkSet<>();
    w.addAll(Arrays.asList("c", "a"));
    w.add("b");
    assertThat(w.isEmpty()).isFalse();
    assertThat(w.size()).isEqualTo(3);
    assertThat(w.pop()).isEqualTo("c");
    assertThat(w.isEmpty()).isFalse();
    assertThat(w.size()).isEqualTo(2);
    assertThat(w.pop()).isEqualTo("a");
    assertThat(w.isEmpty()).isFalse();
    assertThat(w.size()).isEqualTo(1);
    assertThat(w.pop()).isEqualTo("b");
    assertThat(w.isEmpty()).isTrue();
    assertThat(w.size()).isEqualTo(0);
  }

  @Test
  void elements_should_not_be_duplicated() {
    WorkSet<String> w = new WorkSet<>();
    w.addAll(Arrays.asList("abc", "abc"));
    assertThat(w.pop()).isEqualTo("abc");
    assertThat(w.isEmpty()).isTrue();
  }

}
