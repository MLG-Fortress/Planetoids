package org.canis85.planetoidgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Niphred < niphred@curufinwe.org >
 */
public class PlanetoidTemplateManager {
  private Map<PlanetoidTemplate, Double> templates = new HashMap<PlanetoidTemplate, Double>();

  public PlanetoidTemplateManager(ConfigurationSection config) {
    Set<String> sections = config.getKeys(false);
    for (String section: sections) {
      PlanetoidTemplate tmpl = new PlanetoidTemplate(config.getConfigurationSection(section));
      this.templates.put(tmpl, tmpl.getProbability());
    }
  }

  public PlanetoidTemplate selectTemplate(Random rnd) {
    PlanetoidTemplate result = Util.sample(rnd, this.templates);
    return result;
  }
}
