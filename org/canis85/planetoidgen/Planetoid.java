package org.canis85.planetoidgen;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;

/**
 * Holder class for an individual planetoid.
 *
 * @author Canis85
 * @author Niphred < niphred@curufinwe.org >
 */
public class Planetoid implements Serializable {
  //Position, local to the chunk.
  private int xPos;
  private int yPos;
  private int zPos;

  private int radius;
  private int shellThickness;

  private Material coreMat;
  private Material shellMat;

  private Map<Material, Double> coreVeinsSpawn;
  private Map<Material, Double> coreVeinsGrowth;
  private Map<Material, Double> shellVeinsSpawn;
  private Map<Material, Double> shellVeinsGrowth;

  public Planetoid(int radius, int shellThick, Material coreMat, Material shellMat,
          Map<Material, VeinProbability> coreVeins, Map<Material, VeinProbability> shellVeins) {
    this.radius = radius;
    this.shellThickness = shellThick;

    this.coreMat = coreMat;
    this.shellMat = shellMat;

    this.coreVeinsSpawn = new EnumMap<Material, Double>(Material.class);
    this.coreVeinsGrowth = new EnumMap<Material, Double>(Material.class);
    this.shellVeinsSpawn = new EnumMap<Material, Double>(Material.class);
    this.shellVeinsGrowth = new EnumMap<Material, Double>(Material.class);

    Planetoid.extractSpawn(coreVeins, this.coreVeinsSpawn);
    Planetoid.extractGrowth(coreVeins, this.coreVeinsGrowth);
    Planetoid.extractSpawn(shellVeins, this.shellVeinsSpawn);
    Planetoid.extractGrowth(shellVeins, this.shellVeinsGrowth);
  }

  private static void extractGrowth(Map<Material, VeinProbability> source, Map<Material, Double> dest) {
    for (Material m: source.keySet()) {
      dest.put(m, source.get(m).getGrowthProbability());
    }
  }

  private static void extractSpawn(Map<Material, VeinProbability> source, Map<Material, Double> dest) {
    for (Material m: source.keySet()) {
      dest.put(m, source.get(m).getSpawnProbability());
    }
  }

  /**
   * @return the xPos
   */
  public int getxPos() {
    return xPos;
  }

  /**
   * @param xPos the xPos to set
   */
  public void setxPos(int xPos) {
    this.xPos = xPos;
  }

  /**
   * @return the yPos
   */
  public int getyPos() {
    return yPos;
  }

  /**
   * @param yPos the yPos to set
   */
  public void setyPos(int yPos) {
    this.yPos = yPos;
  }

  /**
   * @return the zPos
   */
  public int getzPos() {
    return zPos;
  }

  /**
   * @param zPos the zPos to set
   */
  public void setzPos(int zPos) {
    this.zPos = zPos;
  }

  /**
   * @return the radius
   */
  public int getRadius() {
    return radius;
  }

  /**
   * @return the shellThickness
   */
  public int getShellThickness() {
    return shellThickness;
  }

  /**
   * @return the coreMat
   */
  public Material getCoreMat() {
    return coreMat;
  }

  /**
   * @return the shellMat
   */
  public Material getShellMat() {
    return shellMat;
  }

  /**
   * @return the coreVeinsSpawn
   */
  public Map<Material, Double> getCoreVeinsSpawn() {
    return coreVeinsSpawn;
  }

  /**
   * @return the coreVeinsGrowth
   */
  public Map<Material, Double> getCoreVeinsGrowth() {
    return coreVeinsGrowth;
  }

  /**
   * @return the shellVeinsSpawn
   */
  public Map<Material, Double> getShellVeinsSpawn() {
    return shellVeinsSpawn;
  }

  /**
   * @return the shellVeinsGrowth
   */
  public Map<Material, Double> getShellVeinsGrowth() {
    return shellVeinsGrowth;
  }
}
