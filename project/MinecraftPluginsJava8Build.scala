import sbt._
import Keys._
import java.io.File
import sbtassembly.Plugin._
import AssemblyKeys._

object MinecraftPluginsJava8Build extends Build {

  val projectUrl = "https://github.com/joshcough/MinecraftPluginsJava8"

  lazy val standardSettings = join(
    Defaults.defaultSettings,
    bintray.Plugin.bintraySettings,
    libDeps(
      "javax.servlet"      % "servlet-api" % "2.5"        % "provided->default",
      "org.bukkit"         % "bukkit" % "1.7.2-R0.2"
    ),
    Seq(
      organization := "com.joshcough",
      version := "0.3.3",
      scalaVersion := "2.11.0",
      crossScalaVersions := Seq("2.10.3", "2.11.0"),
      licenses <++= version(v => Seq("MIT" -> url(projectUrl + "/blob/%s/LICENSE".format(v)))),
      publishMavenStyle := true,
      resolvers += Resolver.sonatypeRepo("snapshots"),
      resolvers += "Bukkit" at "http://repo.bukkit.org/content/repositories/releases",
      traceLevel := 10
      //,logLevel := Level.Warn
    )
  )

  // this is the main project, that builds all subprojects.
  // it doesnt contain any code itself.
  lazy val all = Project(
    id = "all",
    base = file("."),
    settings = standardSettings,
    aggregate = Seq(core, MultiPlayerCommands)
  )

  // the core plugin library
  // how the bukkit api should have been written (in java).
  // this backports most of my interesting features from scala to java.
  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = join(
      standardSettings,
      copyPluginToBukkitSettings(None),
      named("java-minecraft-plugin-api")
    )
  )

//  // a special example project...
//  lazy val microExample = Project(id = "microexample", base = file("scala/microexample"))
//
//  // a whole pile of example projects
//  lazy val Arena               = exampleProject("Arena")
//  lazy val BanArrows           = exampleProject("BanArrows")
//  lazy val BlockChanger        = exampleProject("BlockChanger")
//  lazy val BlockChangerGold    = exampleProject("BlockChangerGold")
//  lazy val Farmer              = exampleProject("Farmer")
//  lazy val GetOffMyLawn        = exampleProject("GetOffMyLawn")
//  lazy val God                 = exampleProject("God")
//  lazy val LightningArrows     = exampleProject("LightningArrows")
  lazy val MultiPlayerCommands = exampleProject("MultiPlayerCommands")
//  lazy val NoRain              = exampleProject("NoRain")
//  lazy val PluginCommander     = exampleProject("PluginCommander")
//  lazy val Shock               = exampleProject("Shock")
//  lazy val Thor                = exampleProject("Thor")
//  lazy val TeleportBows        = exampleProject("TeleportBows")
//  lazy val TreeDelogger        = exampleProject("TreeDelogger")
//  lazy val WorldEdit           = exampleProject("WorldEdit")
//  lazy val YellowBrickRoad     = exampleProject("YellowBrickRoad")
//  lazy val ZombieApocalypse    = exampleProject("ZombieApocalypse")

  def exampleProject(exampleProjectName: String, deps: sbt.ModuleID*) = {
    val pluginClassname = "com.joshcough.minecraft.examples." + exampleProjectName
    Project(
      id = exampleProjectName,
      base = file("examples/" + exampleProjectName),
      settings = join(
        standardSettings,
        named(exampleProjectName),
        pluginYmlSettings(pluginClassname, "JoshCough"),
        copyPluginToBukkitSettings(None),
        libDeps(deps:_*)
      ),
      dependencies = Seq(core)
    )
  }

  def copyPluginToBukkitSettings(meta: Option[String]) = Seq(
    // make publish local also copy jars to my bukkit server :)
    publishLocal <<= (packagedArtifacts, publishLocal) map { case (r, _) =>
      r collectFirst { case (Artifact(_,"jar","jar", m, _, _, name), f) if m == meta =>
        println("copying " + f.name + " to bukkit server")
        IO.copyFile(f, new File("bukkit/plugins/" + f.name))
      }
    }
  )

  def join(settings: Seq[Def.Setting[_]]*) = settings.flatten
  def named(pname: String) = Seq(name := pname)
  def libDeps(libDeps: sbt.ModuleID*) = Seq(libraryDependencies ++= libDeps)

  def pluginYmlSettings(pluginClassname: String, author: String): Seq[Setting[_]] = Seq[Setting[_]](
    resourceGenerators in Compile <+=
      (resourceManaged in Compile, streams, productDirectories in Compile, dependencyClasspath in Compile, version, compile in Compile, runner) map {
        (dir, s, cp1, cp2, v, _, r) =>
          Run.run(
            "com.joshcough.minecraft.YMLGenerator", (Attributed.blankSeq(cp1) ++ cp2).map(_.data),
            Seq(pluginClassname, author, v, dir.getAbsolutePath),
            s.log)(r)
          Seq(dir / "plugin.yml") //, dir / "config.yml")
      }
  )

}
