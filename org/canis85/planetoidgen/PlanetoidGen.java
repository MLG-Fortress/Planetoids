package org.canis85.planetoidgen;

import java.util.Arrays;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.Configuration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Planetoids server mod for Minecraft
 *
 * @author Canis85
 * @author Niphred < niphred@curufinwe.org >
 */
public class PlanetoidGen extends JavaPlugin {

  String worldName = null;
  private BukkitScheduler scheduler;
  public static World planetoids = null;
  public static Random seedPod = new Random();

  private void loadDefaults() {
    Configuration config = this.getConfig();
    config.options().copyDefaults(true);
    
    config.addDefault("planetoids.autocreateworld", true);
    config.addDefault("planetoids.worldname", "Planetoids");
    config.addDefault("planetoids.alwaysnight", false);
    config.addDefault("planetoids.weather", false);
    config.addDefault("planetoids.commands.pltp", true);
    config.addDefault("planetoids.disablemonsters", true);
    config.addDefault("planetoids.disableanimals", false);
    config.addDefault("planetoids.seed", seedPod.nextLong());

    config.addDefault("planetoids.floor.height", 0);
    config.addDefault("planetoids.floor.material", Material.BEDROCK.toString());
    config.addDefault("planetoids.floor.layer0Bedrock", false);

    config.addDefault("planetoids.planets.density", 750);
    config.addDefault("planetoids.planets.minDistance", 10);

    config.addDefault("planetoids.planets.templates.default.minY", 0);
    config.addDefault("planetoids.planets.templates.default.maxY", 256);
    config.addDefault("planetoids.planets.templates.default.minSize", 5);
    config.addDefault("planetoids.planets.templates.default.maxSize", 20);
    config.addDefault("planetoids.planets.templates.default.probability", 0.0);

    config.addDefault("planetoids.planets.templates.default.shell.minSize", 3);
    config.addDefault("planetoids.planets.templates.default.shell.maxSize", 5);
    config.addDefault("planetoids.planets.templates.default.shell.bulk", Arrays.asList(Material.DIRT.toString() + "-1.0"));
    config.addDefault("planetoids.planets.templates.default.shell.veins", Arrays.asList(Material.COAL_ORE.toString() + "-0.05"));

    config.addDefault("planetoids.planets.templates.default.core.bulk", Arrays.asList(Material.STONE.toString() + "-1.0"));
    config.addDefault("planetoids.planets.templates.default.core.veins", Arrays.asList(Material.IRON_ORE.toString() + "-0.01"));

    this.saveConfig();
  }

  @Override
  public void onDisable() {
    if (worldName != null) {
      getServer().unloadWorld(worldName, true);
    }

    PluginDescriptionFile pdfFile = this.getDescription();
    System.out.println(pdfFile.getName() + " unloaded.");
  }

  @Override
  public void onEnable() {
    loadDefaults();
    PluginDescriptionFile pdfFile = this.getDescription();
    scheduler = getServer().getScheduler();

    if (getConfig().getBoolean("planetoids.autocreateworld")) {
      if (scheduler.scheduleSyncDelayedTask(this, new Runnable() {
        @Override
        public void run() {
          createWorld();
        }
      }) == -1) {
        System.out.println(pdfFile.getName() + ": Unable to schedule world auto-creation");
      }
    }

    System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
  }

  @Override
  public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
    return new PGChunkGenerator(this);
  }

  public void createWorld() {
    worldName = getConfig().getString("planetoids.worldname", "Planetoids");

    if (getConfig().getBoolean("planetoids.commands.pltp")) {
      getCommand("pltp").setExecutor(new PGPltpCommand(this, worldName));
    }

    //Create chunk generator
    PGChunkGenerator pgGen = new PGChunkGenerator(this);

    WorldCreator wc = new WorldCreator(worldName);
    wc.seed((long) getConfig().getLong("planetoids.seed"));
    wc.environment(Environment.NORMAL);
    wc.generator(pgGen);

    planetoids = getServer().createWorld(wc);

    if (!getConfig().getBoolean("planetoids.weather")) {
      planetoids.setWeatherDuration(0);
    }

    planetoids.setSpawnFlags(!getConfig().getBoolean("planetoids.disablemonsters"), !getConfig().getBoolean("planetoids.disableanimals"));

    PGRunnable task = new PGRunnable();
    if (getConfig().getBoolean("planetoids.alwaysnight")) {
      scheduler.scheduleSyncRepeatingTask(this, task, 60L, 8399L);
    }
  }
}
