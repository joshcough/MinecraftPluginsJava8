package com.joshcough.minecraft.db;

import com.joshcough.minecraft.fp.BetterJavaPlugin;

public class WarpPluginDB extends BetterJavaPlugin {
  public WarpPluginDB(){
    addDependency("JavaPluginAPI");
    addDbClass(Warp.class);
  }
}
