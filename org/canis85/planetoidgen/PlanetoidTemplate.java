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

import java.util.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Niphred < niphred@curufinwe.org >
 */
public class PlanetoidTemplate {
  
  // these 3 values are not used within this class, but
  // in PlanetoidSpawner to select a template
  private int minY;
  private int maxY;
  private double probability;

  private int minSize;
  private int maxSize;
  private int minShellSize;
  private int maxShellSize;
  
  private Map<Material, Double> shells;
  private Map<Material, Double> cores;
  private Map<Material, VeinProbability> shellVeins;
  private Map<Material, VeinProbability> coreVeins;

  public PlanetoidTemplate(ConfigurationSection config) {
    this.minY = config.getInt("minY", 0);
    this.maxY = config.getInt("maxY", 256);
    this.probability = config.getDouble("probability", 1.0);

    this.minSize = config.getInt("minSize", 5);
    this.maxSize = config.getInt("maxSize", 20);
    this.minShellSize = config.getInt("shell.minSize", 3);
    this.maxShellSize = config.getInt("shell.maxSize", 5);

    this.shells = PlanetoidTemplate.processMaterials(config.getStringList("shell.bulk"));
    if (this.shells.isEmpty()) {
      this.shells.put(Material.AIR, 1.0);
    }
    this.shellVeins = PlanetoidTemplate.processVeins(config.getStringList("shell.veins"));

    this.cores = PlanetoidTemplate.processMaterials(config.getStringList("core.bulk"));
    if (this.cores.isEmpty()) {
      this.cores.put(Material.AIR, 1.0);
    }
    this.coreVeins = PlanetoidTemplate.processVeins(config.getStringList("core.veins"));
  }

  public Planetoid spawnPlanetoid(Random rnd) {
    int sizeDiff = Math.abs(this.maxSize - this.minSize);
    int radius = Math.abs(this.minSize) + rnd.nextInt(sizeDiff + 1);
    int shellDiff = Math.abs(this.maxShellSize - this.minShellSize);
    int shellSize = Math.abs(this.minShellSize) + rnd.nextInt(shellDiff + 1);

    Material shell = Util.sample(rnd, this.shells);
    Material core = Util.sample(rnd, this.cores);

    return new Planetoid(radius, shellSize, core, shell, this.coreVeins, this.shellVeins);
  }

  private static Map<Material, Double> processMaterials(List<String> matList) {
    Map<Material, Double> result = new EnumMap<Material, Double>(Material.class);

    for (String s : matList) {
      String[] parts = s.split("-");
      Material mat = Material.matchMaterial(parts[0]);

      if (mat == null) {
        System.err.println("[Planetoids] Invalid Material: " + parts[0]);
        mat = Material.AIR;
      }

      if (!mat.isBlock()) {
        continue;
      }

      double prob = 1.0;
      if (parts.length >= 2) {
        prob = Double.valueOf(parts[1]);
      }
      result.put(mat, prob);
    }

    return result;
  }

  private static Map<Material, VeinProbability> processVeins(List<String> matList) {
    Map<Material, VeinProbability> result = new EnumMap<Material, VeinProbability>(Material.class);

    for (String s : matList) {
      String[] parts = s.split("-");
      Material mat = Material.matchMaterial(parts[0]);

      if (mat == null) {
        System.err.println("[Planetoids] Invalid Material: " + parts[0]);
        mat = Material.AIR;
      }

      if (!mat.isBlock()) {
        continue;
      }

      double probspawn = 0.0;
      if (parts.length >= 2) {
        probspawn = Double.valueOf(parts[1]);
      }

      double probgrowth = 0.0;
      if (parts.length >= 3) {
        probgrowth = Double.valueOf(parts[2]);
      }

      result.put(mat, new VeinProbability(probspawn, probgrowth));
    }

    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof PlanetoidTemplate) {
      // there shouldnt be two identical copies of PlanetoidTemplate in memory
      return this == o;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    // auto-generated by NetBeans
    int hash = 7;
    hash = 37 * hash + this.minY;
    hash = 37 * hash + this.maxY;
    hash = 37 * hash + (int) (Double.doubleToLongBits(this.probability) ^ (Double.doubleToLongBits(this.probability) >>> 32));
    hash = 37 * hash + this.minSize;
    hash = 37 * hash + this.maxSize;
    hash = 37 * hash + this.minShellSize;
    hash = 37 * hash + this.maxShellSize;
    hash = 37 * hash + (this.shells != null ? this.shells.hashCode() : 0);
    hash = 37 * hash + (this.cores != null ? this.cores.hashCode() : 0);
    hash = 37 * hash + (this.shellVeins != null ? this.shellVeins.hashCode() : 0);
    hash = 37 * hash + (this.coreVeins != null ? this.coreVeins.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Shell:[\n");
    sb.append(Util.mapToString(this.shells));
    sb.append(" ]\n");
    sb.append("Shell-Veins:[\n");
    sb.append(Util.mapToString(this.shellVeins));
    sb.append(" ]\n");
    sb.append("Core:[\n");
    sb.append(Util.mapToString(this.cores));
    sb.append(" ]\n");
    sb.append("Core-Veins:[\n");
    sb.append(Util.mapToString(this.coreVeins));
    sb.append(" ]\n");

    return sb.toString();
  }

  /**
   * @return the minY
   */
  public int getMinY() {
    return minY;
  }

  /**
   * @return the maxY
   */
  public int getMaxY() {
    return maxY;
  }

  /**
   * @return the probability
   */
  public double getProbability() {
    return probability;
  }
}
