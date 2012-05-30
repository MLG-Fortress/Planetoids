package org.canis85.planetoidgen;

import java.awt.Point;
import java.io.*;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.Configuration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

/**
 * Generates a Planetoids world.
 *
 * Planetoids are generated in "systems" that are (by default) 100x100 chunks
 * (1600x1600 blocks) in size.
 *
 * @author Canis85
 * @author Niphred < niphred@curufinwe.org >
 */
public class PGChunkGenerator extends ChunkGenerator {

  private Plugin plugin;     //ref to plugin
  private Map<Point, List<Planetoid>> cache;
  private static final int SYSTEM_SIZE = 50;
  private long seed;   //Seed for generating planetoids
  private int density; //Number of planetoids it will try to create per "system"
  private int minDistance; //Minimum distance between planets, in blocks
  private int floorHeight; //Floor height
  private Material floorBlock; //BlockID for the floor
  private boolean layer0Bedrock; // if true layer 0 will be bedrock
  private PlanetoidTemplateManager tmplManager; // holds all the planetoid templates

  public PGChunkGenerator(Plugin plugin) {
    Configuration config = plugin.getConfig();

    this.plugin = plugin;
    this.seed = (long) config.getLong("planetoids.seed");
    this.density = config.getInt("planetoids.planets.density");
    this.minDistance = config.getInt("planetoids.planets.minDistance");

    this.floorBlock = Material.matchMaterial(config.getString("planetoids.floor.material"));
    this.floorHeight = config.getInt("planetoids.floor.height");
    this.layer0Bedrock = config.getBoolean("planetoids.floor.layer0Bedrock");

    this.tmplManager = new PlanetoidTemplateManager(config.getConfigurationSection("planetoids.planets.templates"));

    cache = new HashMap<Point, List<Planetoid>>();
  }

  @Override
  public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
    world.setBiome(x, z, Biome.SKY);
    int height = world.getMaxHeight();
    short[][] retVal = new short[height / 16][];

    int sysX;
    if (x >= 0) {
      sysX = x / SYSTEM_SIZE;
    } else {
      sysX = (int) Math.ceil((-x) / (SYSTEM_SIZE + 1));
      sysX = -sysX;
    }

    int sysZ;
    if (z >= 0) {
      sysZ = z / SYSTEM_SIZE;
    } else {
      sysZ = (int) Math.ceil((-z) / (SYSTEM_SIZE + 1));
      sysZ = -sysZ;
    }

    //check if the "system" this chunk is in is cached
    List<Planetoid> curSystem = cache.get(new Point(sysX, sysZ));

    if (curSystem == null) {
      //if not, does it exist on disk?
      File systemFolder = new File(plugin.getDataFolder(), "Systems");
      if (!systemFolder.exists()) {
        systemFolder.mkdir();
      }
      File systemFile = new File(systemFolder, "system_" + sysX + "." + sysZ + ".dat");
      if (systemFile.exists()) {
        try {
          //load and cache
          FileInputStream fis = new FileInputStream(systemFile);
          ObjectInputStream ois = new ObjectInputStream(fis);
          curSystem = (List<Planetoid>) ois.readObject();
          cache.put(new Point(sysX, sysZ), curSystem);
          ois.close();
          fis.close();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      } else {
        //generate, save, and cache
        curSystem = generatePlanets(sysX, sysZ, world.getMaxHeight());
        try {
          systemFile.createNewFile();
          FileOutputStream fos = new FileOutputStream(systemFile);
          ObjectOutputStream oos = new ObjectOutputStream(fos);
          oos.writeObject(curSystem);
          oos.flush();
          oos.close();
          fos.flush();
          fos.close();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        cache.put(new Point(sysX, sysZ), curSystem);
      }
    }

    //figure out the chunk's position in the "system"
    int chunkXPos;
    if (x >= 0) {
      chunkXPos = (x % SYSTEM_SIZE) * 16;
    } else {
      chunkXPos = ((-x) % SYSTEM_SIZE) * 16;
      if (chunkXPos == 0) {
        chunkXPos = SYSTEM_SIZE * 16;
      }
      chunkXPos = (SYSTEM_SIZE) * 16 - chunkXPos;
      //chunkXPos = SYSTEM_SIZE * 16 + ((x % SYSTEM_SIZE) * 16);
    }
    int chunkZPos;
    if (z >= 0) {
      chunkZPos = (z % SYSTEM_SIZE) * 16;
    } else {
      chunkZPos = ((-z) % SYSTEM_SIZE) * 16;
      if (chunkZPos == 0) {
        chunkZPos = SYSTEM_SIZE * 16;
      }
      chunkZPos = (SYSTEM_SIZE) * 16 - chunkZPos;
      //chunkZPos = SYSTEM_SIZE * 16 + ((z % SYSTEM_SIZE) * 16);
    }

    //Go through the current system's planetoids and fill in this chunk as needed.
    for (Planetoid curPl : curSystem) {
      //Find planet's center point relative to this chunk.
      int relCenterX = curPl.getxPos() - chunkXPos;
      int relCenterZ = curPl.getzPos() - chunkZPos;

      //Generate shell
      for (int curX = -curPl.getRadius(); curX <= curPl.getRadius(); curX++) {
        int blkX = curX + relCenterX;
        if (blkX >= 0 && blkX < 16) {
          //Figure out radius of this circle
          int distFromCenter = Math.abs(curX);
          int radius = (int) Math.ceil(Math.sqrt((curPl.getRadius() * curPl.getRadius()) - (distFromCenter * distFromCenter)));
          for (int curZ = -radius; curZ <= radius; curZ++) {
            int blkZ = curZ + relCenterZ;
            if (blkZ >= 0 && blkZ < 16) {
              int zDistFromCenter = Math.abs(curZ);
              int zRadius = (int) Math.ceil(Math.sqrt((radius * radius) - (zDistFromCenter * zDistFromCenter)));
              for (int curY = -zRadius; curY <= zRadius; curY++) {
                int blkY = curPl.getyPos() + curY;
                //retVal[(blkX * 16 + blkZ) * 128 + blkY] = (byte) curPl.shellBlk.getId();
                if (retVal[blkY >> 4] == null) {
                  retVal[blkY >> 4] = new short[4096];
                }
                retVal[blkY >> 4][((blkY & 0xF) << 8) | (blkZ << 4) | blkX] = (byte) curPl.getShellMat().getId();
              }
            }
          }
        }
      }

      //Generate core
      int coreRadius = curPl.getRadius() - curPl.getShellThickness();
      if (coreRadius > 0) {
        for (int curX = -coreRadius; curX <= coreRadius; curX++) {
          int blkX = curX + relCenterX;
          if (blkX >= 0 && blkX < 16) {
            //Figure out radius of this circle
            int distFromCenter = Math.abs(curX);
            int radius = (int) Math.ceil(Math.sqrt((coreRadius * coreRadius) - (distFromCenter * distFromCenter)));
            for (int curZ = -radius; curZ <= radius; curZ++) {
              int blkZ = curZ + relCenterZ;
              if (blkZ >= 0 && blkZ < 16) {
                int zDistFromCenter = Math.abs(curZ);
                int zRadius = (int) Math.ceil(Math.sqrt((radius * radius) - (zDistFromCenter * zDistFromCenter)));
                for (int curY = -zRadius; curY <= zRadius; curY++) {
                  int blkY = curPl.getyPos() + curY;
                  //retVal[(blkX * 16 + blkZ) * 128 + blkY] = (byte) curPl.coreBlk.getId();
                  if (retVal[blkY >> 4] == null) {
                    retVal[blkY >> 4] = new short[4096];
                  }
                  retVal[blkY >> 4][((blkY & 0xF) << 8) | (blkZ << 4) | blkX] = (byte) curPl.getCoreMat().getId();
                }
              }
            }
          }
        }
      }
    }
    //Fill in the floor
    if (floorHeight > 0 && retVal[0] == null) {
      retVal[0] = new short[4096];
    }
    for (int i = 0; i < floorHeight; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 16; k++) {
          if (i == 0 && layer0Bedrock) {
            //retVal[j * 2048 + k * 128 + i] = (byte) Material.BEDROCK.getId();
            retVal[i >> 4][((i & 0xF) << 8) | (j << 4) | k] = (byte) Material.BEDROCK.getId();
          } else {
            //retVal[j * 2048 + k * 128 + i] = (byte) floorBlock.getId();
            retVal[i >> 4][((i & 0xF) << 8) | (j << 4) | k] = (byte) floorBlock.getId();
          }
        }
      }
    }
    return retVal;
  }

  @Override
  public boolean canSpawn(World world, int x, int z) {
    return true;
  }

  @Override
  public Location getFixedSpawnLocation(World world, Random random) {
    return new Location(world, 7, 77, 7);
  }

  private List<Planetoid> generatePlanets(int x, int z, int height) {
    List<Planetoid> planetoids = new ArrayList<Planetoid>();

    //If x and Z are zero, generate a log/leaf planet close to 0,0
    if (x == 0 && z == 0) {
      Planetoid spawnPl = new Planetoid(6, 3, Material.LOG, Material.LEAVES,
                                        new EnumMap<Material, Double>(Material.class),
                                        new EnumMap<Material, Double>(Material.class));
      spawnPl.setxPos(7);
      spawnPl.setyPos(70);
      spawnPl.setzPos(7);
      planetoids.add(spawnPl);
    }

    //if X is negative, left shift seed by one
    if (x < 0) {
      seed = seed << 1;
    } //if Z is negative, change sign on seed.
    if (z < 0) {
      seed = -seed;
    }

    Random rand = new Random(seed);
    for (int i = 0; i
            < Math.abs(x) + Math.abs(z); i++) {
      //cycle generator
      rand.nextDouble();
    }

    for (int i = 0; i < density; i++) {
      //Try to make a planet
      PlanetoidTemplate curPlTmpl = this.tmplManager.selectTemplate(rand);
      if (curPlTmpl == null) {
        continue;
      }
      Planetoid curPl = curPlTmpl.spawnPlanetoid(rand);

      //Set position, check bounds with system edges
      int xPos = -1;
      while (xPos == -1) {
        int curTry = rand.nextInt(SYSTEM_SIZE * 16);
        if (curTry + curPl.getRadius() < SYSTEM_SIZE * 16 && curTry - curPl.getRadius() >= 0) {
          xPos = curTry;
        }
      }
      curPl.setxPos(xPos);

      int minY = Math.max(floorHeight + curPl.getRadius(), curPlTmpl.getMinY());
      int maxY = Math.min(height - curPl.getRadius() - 1, curPlTmpl.getMaxY());
      int yPos = rand.nextInt(maxY - minY + 1) + minY;
      curPl.setyPos(yPos);

      int zPos = -1;
      while (zPos == -1) {
        int curTry = rand.nextInt(SYSTEM_SIZE * 16);
        if (curTry + curPl.getRadius() < SYSTEM_SIZE * 16 && curTry - curPl.getRadius() >= 0) {
          zPos = curTry;
        }
      }
      curPl.setzPos(zPos);

      //Created a planet, check for collisions with existing planets
      //If any collision, discard planet
      boolean discard = false;
      for (Planetoid pl : planetoids) {
        //each planetoid has to be at least pl1.radius + pl2.radius + min distance apart
        int distMin = pl.getRadius() + curPl.getRadius() + minDistance;
        if (distanceSquared(pl, curPl) < distMin * distMin) {
          discard = true;
          break;
        }
      }
      if (!discard) {
        planetoids.add(curPl);
      }
    }
    System.out.println("Made new system with " + planetoids.size() + " planetoids."); //DEBUG
    return planetoids;
  }

  private int distanceSquared(Planetoid pl1, Planetoid pl2) {
    int xDist = pl2.getxPos() - pl1.getxPos();
    int yDist = pl2.getyPos() - pl1.getyPos();
    int zDist = pl2.getzPos() - pl1.getzPos();

    return xDist * xDist + yDist * yDist + zDist * zDist;
  }

}
