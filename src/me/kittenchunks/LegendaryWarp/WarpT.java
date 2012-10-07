package me.kittenchunks.LegendaryWarp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpT
{
  private Server server;
  private File fWarps;
  private File fTmp;

  public WarpT(Server server, File folder)
  {
    this.server = server;
    this.fWarps = new File(folder, "warps.yml");
    this.fTmp = new File(folder, "warps.tmp");
  }

  public void warpList(CommandSender sender)
  {
    if (!this.fWarps.exists()) {
      sender.sendMessage(ChatColor.RED + "No warps available >:[");
      return;
    }
    try {
      Scanner scanner = new Scanner(new FileReader(this.fWarps));
      String buffer = ChatColor.AQUA + "Available warps: " + ChatColor.WHITE;
      while (scanner.hasNextLine()) {
        String[] items = scanner.nextLine().split(":");
        if (items.length > 0) {
          if (buffer.length() + items[0].length() + 2 >= 256) {
            sender.sendMessage(buffer);
            buffer = items[0] + ", ";
          } else {
            buffer = buffer + items[0] + ", ";
          }
        }
      }
      sender.sendMessage(buffer);
      scanner.close();
    } catch (Exception e) {
      System.out.println("Cannot create file " + this.fWarps.getName() + " - " + e.getMessage());
      sender.sendMessage(ChatColor.RED + "Listing warps failed! D:<");
    }
  }

  public void warpTo(CommandSender sender, String warp, String playerName)
  {
    Player player = null;
    if (playerName != null) {
      Player player1 = this.server.getPlayer(playerName);
      if (player1 == null) {
        sender.sendMessage(ChatColor.RED + "Unknown player " + ChatColor.WHITE + playerName);
        return;
      }
    }
    else
    {
      if (!(sender instanceof Player)) {
        sender.sendMessage(ChatColor.AQUA + "You have to be a player!");
        return;
      }
      player = (Player)sender;
    }

    if (!this.fWarps.exists()) {
      sender.sendMessage(ChatColor.AQUA + "No warps available");
      return;
    }
    try
    {
      boolean found = false;
      Scanner scanner = new Scanner(new FileReader(this.fWarps));
      while (scanner.hasNextLine()) {
        String[] cur = scanner.nextLine().split(":");
        if ((cur.length >= 6) && (cur[0].equalsIgnoreCase(warp))) {
          double x = Double.parseDouble(cur[1]);
          double y = Double.parseDouble(cur[2]);
          double z = Double.parseDouble(cur[3]);
          float yaw = Float.parseFloat(cur[4]);
          float pitch = Float.parseFloat(cur[5]);
          World world1;
          if (cur.length == 7) world1 = this.server.getWorld(cur[6]); else {
            world1 = (World)this.server.getWorlds().get(0);
          }
          Location loc = new Location(world1, x, y, z, yaw, pitch);
          player.teleport(loc);
          player.sendMessage(ChatColor.AQUA + "Warped to: " + ChatColor.WHITE + warp);
          found = true;
          break;
        }
      }
      scanner.close();
      if (!found) player.sendMessage(ChatColor.RED + "No such warp: " + ChatColor.WHITE + warp); 
    }
    catch (Exception e) {
      System.out.println("Cannot parse file " + this.fWarps.getName() + " - " + e.getMessage());
      sender.sendMessage(ChatColor.RED + "Warp failed!");
    }
  }

  public void warpAdd(CommandSender sender, String warp)
  {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.AQUA + "You have to be a player!");
      return;
    }

    if (!this.fWarps.exists()) {
      try {
        this.fWarps.createNewFile();
      } catch (Exception e) {
        System.out.println("Cannot create file " + this.fWarps.getName() + " - " + e.getMessage());
        sender.sendMessage(ChatColor.RED + "Setting warp failed! >:[");
        return;
      }
    }

    if (!warpRemoveInternal(warp)) {
      sender.sendMessage(ChatColor.RED + "Setting warp failed! >:[");
      return;
    }

    Player player = (Player)sender;
    Location loc = player.getLocation();
    try {
      FileWriter wrt = new FileWriter(this.fWarps, true);
      wrt.write(warp + ":" + 
        loc.getX() + ":" + 
        loc.getY() + ":" + 
        loc.getZ() + ":" + 
        loc.getYaw() + ":" + 
        loc.getPitch() + ":" + 
        player.getWorld().getName() + "\n");

      wrt.close();
    } catch (Exception e) {
      System.out.println("Unexpected error " + e.getMessage());
      sender.sendMessage(ChatColor.RED + "Setting warp failed!");
      return;
    }
    player.sendMessage(ChatColor.AQUA + "Warp: " + ChatColor.WHITE +  warp + ChatColor.RED + " has been set :D");
  }

  public void warpRemove(CommandSender sender, String warp)
  {
    if (!this.fWarps.exists()) {
      sender.sendMessage(ChatColor.RED + "No warps available :[");
      return;
    }
    if (warpRemoveInternal(warp))
      sender.sendMessage(ChatColor.RED + "Warp " + ChatColor.WHITE + warp + ChatColor.RED + " removed :P");
    else
      sender.sendMessage(ChatColor.RED + "Removing " + ChatColor.WHITE + " failed");
  }

  private boolean warpRemoveInternal(String name)
  {
    try
    {
      boolean found = false;
      Scanner scanner = new Scanner(new FileReader(this.fWarps));
      while (scanner.hasNextLine()) {
        String[] warp = scanner.nextLine().split(":");
        if ((warp.length >= 1) && (warp[0].equalsIgnoreCase(name))) {
          found = true;
          break;
        }
      }
      scanner.close();

      if (!found) {
        return true;
      }

      PrintWriter wrt = new PrintWriter(new FileWriter(this.fTmp));
      BufferedReader rdr = new BufferedReader(new FileReader(this.fWarps));
      while ((rdr.readLine()) != null)
      {
        String line1 = null;
        @SuppressWarnings("null")
		String[] warp = line1.split(":");
        if ((warp.length >= 1) && (warp[0].equalsIgnoreCase(name))) {
          continue;
        }
        wrt.println(line1);
      }

      wrt.close();
      rdr.close();
      if (!this.fWarps.delete()) {
        System.out.println("Cannot delete D:" + this.fWarps.getName());
        return false;
      }
      if (!this.fTmp.renameTo(this.fWarps)) {
        System.out.println("Cannot rename D:" + this.fTmp.getName() + " to " + this.fWarps.getName());
        return false;
      }
    } catch (Exception e) {
      System.out.println("Unexpected error D: " + e.getMessage());
      return false;
    }
    return true;
  }

  public boolean worldList(CommandSender sender)
  {
    sender.sendMessage(ChatColor.AQUA + "Available worlds:" + ChatColor.WHITE);
    for (World w : this.server.getWorlds()) {
      sender.sendMessage(w.getName());
    }
    return true;
  }

  public void worldTo(CommandSender sender, String world, String playerName)
  {
    Player player = null;
    if (playerName != null) {
      Player player1 = this.server.getPlayer(playerName);
      if (player1 == null) {
        sender.sendMessage(ChatColor.RED + "Unknown player " + ChatColor.WHITE + playerName);
        return;
      }
    }
    else
    {
      if (!(sender instanceof Player)) {
        sender.sendMessage(ChatColor.AQUA + "You have to be a player!");
        return;
      }
      player = (Player)sender;
    }

    World w = this.server.getWorld(world);
    if (w == null) {
      player.sendMessage(ChatColor.RED + "No such world: " + ChatColor.WHITE + world);
      return;
    }
    player.teleport(w.getSpawnLocation());
  }
}