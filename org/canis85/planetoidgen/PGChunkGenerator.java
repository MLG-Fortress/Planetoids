package org.canis85.planetoidgen;

import java.awt.Point;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
  private Map<World, Map<Point, List<Planetoid>>> cache;
  private static final int SYSTEM_SIZE = 50;
  private int density; //Number of planetoids it will try to create per "system"
  private int minDistance; //Minimum distance between planets, in blocks
  private boolean removeSingletons; // Remove veins that didnt grow at  least once

  private int floorHeight; //Floor height
  private Material floorBlock; //BlockID for the floor
  private boolean layer0Bedrock; // if true layer 0 will be bedrock

  private PlanetoidTemplateManager tmplManager; // holds all the planetoid templates
  private FileConfiguration config;

  public PGChunkGenerator(Plugin plugin) {
    this.plugin = plugin;
    cache = new HashMap<World, Map<Point, List<Planetoid>>>();
    // load default configuration
    this.setConfig(plugin.getConfig());
  }

  private void setConfig(FileConfiguration config) {
    this.config = config;

    this.density = config.getInt("planetoids.planets.density");
    this.minDistance = config.getInt("planetoids.planets.minDistance");
    this.removeSingletons = config.getBoolean("planetoids.removesingleveins");

    this.floorBlock = Material.matchMaterial(config.getString("planetoids.floor.material"));
    this.floorHeight = config.getInt("planetoids.floor.height");
    this.layer0Bedrock = config.getBoolean("planetoids.floor.layer0Bedrock");

    this.tmplManager = new PlanetoidTemplateManager(config.getConfigurationSection("planetoids.planets.templates"));
  }

  @Override
  public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biomes) {
    File world_dir = world.getWorldFolder();
    File world_cfg_location = new File(plugin.getDataFolder(), "world_" + world.getName() + ".yaml");
    if (!world_dir.exists()) {
      world_dir.mkdirs();
    }

    if (world_cfg_location.canRead()) {
      this.setConfig(YamlConfiguration.loadConfiguration(world_cfg_location));
    }
    else {
      try {
        this.config.save(world_cfg_location);
      }
      catch (IOException e) {
        this.plugin.getLogger().warning(String.format("Could not write world-specific configuration for world: %s", world.getName()));
        this.plugin.getLogger().warning(String.format("IOException: %s", e.getMessage()));
      }
    }

    world.setBiome(x, z, Biome.THE_END);
    int height = world.getMaxHeight();
    ChunkData retVal = createChunkData(world);

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

    if (!this.cache.containsKey(world)) {
      this.cache.put(world, new HashMap<Point, List<Planetoid>>());
    }
    //check if the "system" this chunk is in is cached
    List<Planetoid> curSystem = cache.get(world).get(new Point(sysX, sysZ));

    if (curSystem == null) {
      //if not, does it exist on disk?
      File systemFolder = new File(world_dir, "systems");
      if (!systemFolder.exists()) {
        systemFolder.mkdirs();
      }
      File systemFile = new File(systemFolder, "system_" + sysX + "." + sysZ + ".dat");
      if (systemFile.exists()) {
        try {
          //load and cache
          FileInputStream fis = new FileInputStream(systemFile);
          ObjectInputStream ois = new ObjectInputStream(fis);
          curSystem = (List<Planetoid>) ois.readObject();
          cache.get(world).put(new Point(sysX, sysZ), curSystem);
          ois.close();
          fis.close();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      } else {
        //generate, save, and cache
        curSystem = generatePlanets(world.getSeed(), sysX, sysZ, world.getMaxHeight());
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
        cache.get(world).put(new Point(sysX, sysZ), curSystem);
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

      // discard planets that will not be in this chunk
      int diffX = relCenterX - 8;
      int diffZ = relCenterZ - 8;
      if (Math.floor(Math.sqrt(diffX * diffX + diffZ * diffZ)) >= curPl.getRadius() + 12) {
        continue;
      }

      //Generate shell
      generateSphere(random, retVal, curPl.getRadius(), relCenterX, curPl.getyPos(), relCenterZ,
                     curPl.getShellMat(), curPl.getShellVeinsSpawn(), curPl.getShellVeinsGrowth());

      //Generate core
      int coreRadius = curPl.getRadius() - curPl.getShellThickness();
      if (coreRadius > 0) {
        generateSphere(random, retVal, coreRadius, relCenterX, curPl.getyPos(), relCenterZ,
                       curPl.getCoreMat(), curPl.getCoreVeinsSpawn(), curPl.getCoreVeinsGrowth());
      }
    }
    //Fill in the floor
    for (int i = 0; i < floorHeight; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 16; k++) {
          if (i == 0 && layer0Bedrock) {
            //retVal[j * 2048 + k * 128 + i] = (byte) Material.BEDROCK.getId();
            //retVal[i >> 4][((i & 0xF) << 8) | (j << 4) | k] = (byte) Material.BEDROCK.getId();
            retVal.setBlock(k, i, j, Material.BEDROCK);
          } else {
            //retVal[j * 2048 + k * 128 + i] = (byte) floorBlock.getId();
            //retVal[i >> 4][((i & 0xF) << 8) | (j << 4) | k] = (byte) floorBlock.getId();
            retVal.setBlock(k, i, j, floorBlock);
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

  private List<Planetoid> generatePlanets(long seed, int x, int z, int height) {
    List<Planetoid> planetoids = new ArrayList<Planetoid>();

    //If x and Z are zero, generate a log/leaf planet close to 0,0
    if (x == 0 && z == 0) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      Planetoid spawnPl = new Planetoid(6, 3,
              Tag.LOGS.getValues().toArray(new Material[0])[random.nextInt(0, Tag.LOGS.getValues().size())],
              Tag.LEAVES.getValues().toArray(new Material[0])[random.nextInt(0, Tag.LEAVES.getValues().size())],
              new EnumMap<Material, VeinProbability>(Material.class),
              new EnumMap<Material, VeinProbability>(Material.class));
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
    for (int i = 0; i < Math.abs(x) + Math.abs(z); i++) {
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

    // DEBUGGING INFO
    Logger logger = this.plugin.getLogger();
    logger.info(String.format("Made a new system with %d planetoids", planetoids.size()));
    logger.fine("Generated Planets:");
    for (Planetoid p: planetoids) {
      logger.fine(String.format("X: %d, Y: %d, Z: %d, Shell: %s, Core: %s",
                                p.getxPos(), p.getyPos(), p.getzPos(), p.getShellMat(), p.getCoreMat()));
    }
    return planetoids;
  }

  private int distanceSquared(Planetoid pl1, Planetoid pl2) {
    int xDist = pl2.getxPos() - pl1.getxPos();
    int yDist = pl2.getyPos() - pl1.getyPos();
    int zDist = pl2.getzPos() - pl1.getzPos();

    return xDist * xDist + yDist * yDist + zDist * zDist;
  }

  private void generateSphere(Random rnd, ChunkData chunkData, int radius,
                                     int relX, int y, int relZ, Material bulk,
                                     Map<Material, Double> veinSpawn,
                                     Map<Material, Double> veinGrowth) {
    Queue<VeinPosition> veinPositions = new ArrayDeque<VeinPosition>();

    for (int curX = -radius; curX <= radius; curX++) {
      int blkX = curX + relX;
      if (blkX >= 0 && blkX < 16) {
        //Figure out radius of this circle
        int distFromCenter = Math.abs(curX);
        int radiusX = (int) Math.ceil(Math.sqrt((radius * radius) - (distFromCenter * distFromCenter)));
        for (int curZ = -radiusX; curZ <= radiusX; curZ++) {
          int blkZ = curZ + relZ;
          if (blkZ >= 0 && blkZ < 16) {
            int zDistFromCenter = Math.abs(curZ);
            int zRadius = (int) Math.ceil(Math.sqrt((radiusX * radiusX) - (zDistFromCenter * zDistFromCenter)));
            for (int curY = -zRadius; curY <= zRadius; curY++) {
              int blkY = y + curY;
              //retVal[(blkX * 16 + blkZ) * 128 + blkY] = (byte) curPl.shellBlk.getId();
              Material mat = Util.sample(rnd, veinSpawn, false);
              if (mat == null) {
                mat = bulk;
              }
              else {
                veinPositions.add(new VeinPosition(blkX, blkY, blkZ));
              }
              chunkData.setBlock(blkX, blkY, blkZ, mat);
            }
          }
        }
      }
    }

    // postprocess the chunk to add veins
    while (!veinPositions.isEmpty()) {
      VeinPosition p = veinPositions.poll();
      //Material mat = chunkData.getType(p.getX(), p.getY(), p.getZ());
      Material mat = chunkData.getBlockData(p.getX(), p.getY(), p.getZ()).getMaterial();

      boolean remove = this.removeSingletons && (p.getGeneration() == 0);
      for (int i = 0; i < 6; i++) {
        if (rnd.nextDouble() > veinGrowth.get(mat) * (1.0 / (1 + p.getGeneration()))) {
          continue;
        }

        int newX = p.getX() + (1 - i % 2) * (i / 2 == 0 ? 1 : 0);
        int newY = p.getY() + (1 - i % 2) * (i / 2 == 1 ? 1 : 0);
        int newZ = p.getZ() + (1 - i % 2) * (i / 2 == 2 ? 1 : 0);

        if (newX >= 0 && newX < 16 && newY >= 0 && newY < chunkData.getMaxHeight() && newZ >= 0 && newZ < 16) {
          // new point is in the current chunk
          int diffX = newX - relX;
          int diffY = newY -    y;
          int diffZ = newZ - relZ;
          if (diffX*diffX + diffY*diffY + diffZ*diffZ <= radius*radius) {
            if (chunkData.getType(newX, newY, newZ) == bulk) {
              chunkData.setBlock(newX, newY, newZ, mat);
              veinPositions.add(new VeinPosition(newX, newY, newZ, p.getGeneration() + 1));
              remove = false;
            }
          }
        }
      }

      if (remove) {
        chunkData.setBlock(p.getX(), p.getY(), p.getZ(), bulk);
      }
    }
  }
}
