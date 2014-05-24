package com.joshcough.minecraft;

// A helper object I use to auto generate all my plugin.yml files.
class YMLGenerator {

    static public void main(String[] args) throws Exception {
        String className = args[0];
        String author    = args[1];
        String version   = args[2];
        String outputDir = args[3];
        generateYml(className, author, version, outputDir);
    }

  static BetterJavaPlugin create(String className) throws Exception {
      return (BetterJavaPlugin) Class.forName(className).newInstance();
  }

  static void generateYml(String className, String author, String version, String outputDir) throws Exception {
    create(className).writeYML(author, version, outputDir);
  }
}
