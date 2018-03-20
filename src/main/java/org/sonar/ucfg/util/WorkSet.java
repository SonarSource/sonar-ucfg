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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class WorkSet<T> {

  private final Set<T> elements;

  public WorkSet() {
    elements = new LinkedHashSet<>();
  }

  public WorkSet(Collection<T> initialElements) {
    elements = new LinkedHashSet<>(initialElements);
  }

  public boolean isEmpty() {
    return elements.isEmpty();
  }

  /**
   * Removes an element from the WorkSet and returns this element.
   * The element which is removed is the one which was added first (FIFO).
   * @return the removed element
   * @throws IllegalStateException if the WorkSet is empty
   */
  public T pop() {
    if (isEmpty()) {
      throw new IllegalStateException("Cannot pop from an empty WorkSet");
    }
    T element = elements.iterator().next();
    elements.remove(element);
    return element;
  }

  public void add(T element) {
    elements.add(element);
  }

  public void addAll(Collection<T> newElements) {
    elements.addAll(newElements);
  }
}
