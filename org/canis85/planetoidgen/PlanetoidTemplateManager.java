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
      System.out.println("planetoid " + tmpl.toString());
    }
  }

  public PlanetoidTemplate selectTemplate(Random rnd) {
    PlanetoidTemplate result = Util.sample(rnd, this.templates);
    System.out.println("planetoid " + result);
    return result;
  }
}
