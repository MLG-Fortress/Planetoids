package org.canis85.planetoidgen;

import java.io.Serializable;
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

  private Map<Material, Double> coreVeins;
  private Map<Material, Double> shellVeins;

  public Planetoid(int radius, int shellThick, Material coreMat, Material shellMat,
          Map<Material, Double> coreVeins, Map<Material, Double> shellVeins) {
    this.radius = radius;
    this.shellThickness = shellThick;

    this.coreMat = coreMat;
    this.shellMat = shellMat;

    this.coreVeins = coreVeins;
    this.shellVeins = shellVeins;
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
   * @return the coreVeins
   */
  public Map<Material, Double> getCoreVeins() {
    return coreVeins;
  }

  /**
   * @return the shellVeins
   */
  public Map<Material, Double> getShellVeins() {
    return shellVeins;
  }
}
