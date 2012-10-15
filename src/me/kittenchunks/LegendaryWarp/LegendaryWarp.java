package me.kittenchunks.LegendaryWarp;

import java.io.File;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class LegendaryWarp extends JavaPlugin
{
  private WarpT worker;

  public void onEnable()
  {
    PluginDescriptionFile pdfFile = getDescription();

    File dir = getDataFolder();
    if (!dir.exists()) {
      dir.mkdir();
      if (!dir.exists()) {
        System.out.println("Cannot create plugin directory for " + pdfFile.getName() + "!");
        return;
      }
    }
    this.worker = new WarpT(getServer(), getDataFolder());
    System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
  }

  public void onDisable() {
    PluginDescriptionFile pdfFile = getDescription();
    System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " stopping...");
  }
//start commands
  public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
  {
    String cmd = command.getName();
    if (cmd.equalsIgnoreCase("warplist")) {
      this.worker.warpList(sender);
      return true;
    }
    if (cmd.equalsIgnoreCase("worldlist")) { //start world commands
      this.worker.worldList(sender);
      return true;
    }
    if (args.length == 0)
      return false;
    if (cmd.equalsIgnoreCase("warpto")) {
      this.worker.warpTo(sender, args[0], args.length > 1 ? args[1] : null);
      return true;
    }
    if (cmd.equalsIgnoreCase("warpremove")) {
      this.worker.warpRemove(sender, args[0]); //need to fix this 
      return true;
    }
    if (cmd.equalsIgnoreCase("setwarp")) {
      this.worker.warpAdd(sender, args[0]);
      return true;
    }
    if (cmd.equalsIgnoreCase("world")) {
      this.worker.worldTo(sender, args[0], args.length > 1 ? args[1] : null); //tp to a world, still buggy
      return true;
    }
    return false;
  }
}