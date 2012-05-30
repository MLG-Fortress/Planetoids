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
