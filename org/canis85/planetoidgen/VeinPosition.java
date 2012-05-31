/*
 * Copyright (c) 2012 Eugen "Niphred" Beck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.canis85.planetoidgen;

/**
 * Represents the location of a vein
 * @author Niphred < niphred@curufinwe.org >
 */
public class VeinPosition {
  private int x;
  private int y;
  private int z;
  private int generation;

  public VeinPosition(int x, int y, int z) {
    this(x, y, z, 0);
  }

  public VeinPosition(int x, int y, int z, int generation) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.generation = generation;
  }

  @Override
  public String toString() {
    return "<x: " + this.x + " y: " + this.y + " z: " + this.z + " gen: " + this.generation + ">";
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getZ() {
    return this.z;
  }

  public int getGeneration() {
    return this.generation;
  }

}
