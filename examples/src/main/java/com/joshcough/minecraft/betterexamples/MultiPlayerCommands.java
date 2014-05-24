package com.joshcough.minecraft.betterexamples;

import com.joshcough.minecraft.betterjava.*;
import static com.joshcough.minecraft.betterjava.JavaParsers.*;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MultiPlayerCommands extends BetterJavaPlugin {

  public MultiPlayerCommands(){
    Commands(
      Command("set-time", "Sets the time.", integer, (p,i) -> p.getWorld().setTime(i.longValue())),
      Command("day", "Sets the time to day.", p -> p.getWorld().setTime(1)),
      Command("night", "Sets the time to night.", p -> p.getWorld().setTime(15000)),
      Command("entities", "Display all the entities.", p ->
        p.getWorld().getEntities().forEach(e -> p.sendMessage(e.toString()))
      ),
      Command("feed", "Fill a players hunger bar", player,
        (you, them) -> doTo(you, them, () -> them.setFoodLevel(20), "fed")
      ),
      Command("starve", "Drain a players hunger bar", player,
        (you, them) -> doTo(you, them, () -> them.setFoodLevel(0), "starved")
      ),
      Command("shock", "Shock a player.", player,
        (you, them) -> doTo(you, them, () -> them.getWorld().strikeLightning(them.getLocation()), "shocked")
      ),
      Command("gm", "Set your game mode", gamemode, Player::setGameMode),
      Command("gms", "Set your game mode to survival", you -> you.setGameMode(GameMode.SURVIVAL)),
      Command("gmc", "Set your game mode to creative", you -> you.setGameMode(GameMode.CREATIVE)),
      Command("spawn", "Spawn some mobs.", entity.and(opt(integer)),
        (p, t) -> spawnN(t._1(), t._2().getOrElse(() -> 1), p.getLocation())
      ),
      Command("ban", "Ban some players.", anyString.plus(),
        (you, ss) -> {
          for(Player them: getServer().getOnlinePlayers()){
            if(ss.contains(them.getName())) ban(them, you.getName() + " doesn't like you.");
          }
          for(OfflinePlayer them: getServer().getOfflinePlayers()){
            if(ss.contains(them.getName())) them.setBanned(true);
          }
        }
      ),
      Command("up", "Go up to the surface.", p ->
        p.teleport(p.getWorld().getHighestBlockAt(p.getLocation()).getLocation())
      ),
      Command("kill", "Kill entities.", either(match("player").and(player), entity),
        (killer, e) -> {
          if(e.isLeft()) doTo(killer, e.getLeft()._2(), () -> e.getLeft()._2().setHealth(0), "killed");
          else for(Entity en: killer.getWorld().getEntities()){
            if(en.getType() == e.getRight()) en.remove();
          }
        }
      ),
      Command("goto", "Teleport to a player.",
        either(player, integer.and(integer).and(opt(integer))),
        (you, e) -> {
          if (e.isLeft()) you.teleport(e.getLeft());
          else {
            Tuple2<Tuple2<Integer, Integer>, Option<Integer>> t = e.getRight();
            int x = t._1()._1();
            int z = t._1()._2();
            t._2().foldV(
              () -> teleport(you, x, you.getWorld().getHighestBlockYAt(x, z), z),
              y -> teleport(you, x, z, y)
            );
          }
        }
      )
//  /**
//   TODO: still need to implement these, but they require all the block pimping
//   which will be non-trivial and ugly in java.
//
//   Command("box",      "Put a box around yourself, made of any material.",
//     args(material){ case p ~ m  => p.blocksAround.foreach(_ changeTo m) }),
//
//   Command("safe",     "Put yourself in a box made of bedrock.",
//    noArgs(_.blocksAround.foreach(_ changeTo BEDROCK))),
//
//   Command("drill",    "Drill down to bedrock immediately.", noArgs(p =>
//     for (b <- p.blockOn.blocksBelow.takeWhile(_ isNot BEDROCK); if (b isNot AIR)) {
//     b.erase
//     if (b.blockBelow is BEDROCK) b.nthBlockAbove(2) changeTo STATIONARY_WATER
//    })),
//
//   Command("creeper-kill", "Surround a player with creepers", opOnly(p2p((_, them) => {
//    them.setGameMode(SURVIVAL)
//    them.loc.block.neighbors8.foreach(_.loc.spawn(CREEPER))
//   })
//   **/
    );
  }
}
