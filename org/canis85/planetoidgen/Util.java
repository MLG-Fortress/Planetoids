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

import java.util.Map;
import java.util.Random;

/**
 *
 * @author Niphred < niphred@curufinwe.org >
 */
public class Util {
  public static <T> T sample(Random rnd, Map<T, Double> m) {
    double sum = 0.0;

    for (T key: m.keySet()) {
      sum += Math.abs(m.get(key));
    }

    if (sum == 0.0) {
      // stupid user is stupid
      sum = 1.0;
    }

    double goal = rnd.nextDouble();

    T numeric_backup = null;
    for (T key: m.keySet()) {
      numeric_backup = key;
      double prob = Math.abs(m.get(key)) / sum;
      goal -= prob;
      if (goal <= 0.0) {
        return key;
      }
    }

    // seems we got some numeric errors while doing substractions => return the last value
    return numeric_backup;
  }
}
