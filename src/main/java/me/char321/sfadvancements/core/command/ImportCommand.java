package me.char321.sfadvancements.core.command;

import me.char321.sfadvancements.SFAdvancements;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class ImportCommand implements SubCommand {
    @Override
    public boolean onExecute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /" + label + " import <nombre del plugin>");
            return false;
        }

        Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
        if (pl == null) {
            sender.sendMessage(ChatColor.RED + "El plugin " + args[1] + " no se encontró.");
            return false;
        }

        sender.sendMessage("Importando el plugin " + pl.getName() + " los ajustes de progreso...");

        InputStream advInputStream = pl.getResource("sfadvancements.yml");
        if (advInputStream == null) {
            sender.sendMessage(ChatColor.RED + "El plugin " + pl.getName() + " no trae en su jar el fichero sfadvancements.yml de progresos");
            return false;
        }

        InputStream groupInputStream = pl.getResource("sfagroups.yml");
        if (groupInputStream == null) {
            sender.sendMessage(ChatColor.YELLOW + "El plugin " + pl.getName() + " no trae en su jar el fichero sfagroups.yml de grupos de progreso");
            sender.sendMessage(ChatColor.YELLOW + "Todo lo importado va al grupo por defecto.");
        }

        saveBackups();
        importGroups(pl, groupInputStream);
        importAdvancements(pl, advInputStream);

        sender.sendMessage("Hecho. Reinicia el servidor o usa /sfa reload para que se aplique.");
        return true;
    }

    private void saveBackups() {
        File dataFolder = SFAdvancements.instance().getDataFolder();
        File backupFolder = new File(dataFolder, "backups");
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
        if (!backupFolder.isDirectory()) {
            throw new IllegalStateException(backupFolder + " no es un directorio válido.");
        }

        File groupFile = new File(dataFolder, "groups.yml");
        File advFile = new File(dataFolder, "advancements.yml");

        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        int index = 0;
        File groupFileOut;
        File advFileOut;
        do {
            index++;
            groupFileOut = new File(backupFolder, "groups-backup-" + now + " (" + index + ").yml");
            advFileOut = new File(backupFolder, "advancements-backup-" + now + " (" + index + ").yml");
        } while (groupFileOut.exists() || advFileOut.exists());

        try {
            if (groupFile.exists()) {
                Files.copy(groupFile.toPath(), groupFileOut.toPath());
            }
            if (advFile.exists()) {
                Files.copy(advFile.toPath(), advFileOut.toPath());
            }
        } catch (IOException ex) {
            SFAdvancements.logger().log(Level.SEVERE, ex, () -> "Error al crear la copia de seguridad");
        }
    }

    private void importGroups(Plugin pl, InputStream groupInputStream) {
        if (groupInputStream == null) {
            return;
        }

        File outfile = new File(SFAdvancements.instance().getDataFolder(), "groups.yml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(groupInputStream));
        YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
        YamlConfiguration original = SFAdvancements.instance().getGroupsConfig();

        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            if (original.isSet(key)) {
                SFAdvancements.info("grupo de progreso " + key + " ya existe; no se sobrescribe.");
            } else {
                original.set(key, config.get(key));
            }
        }
        try {
            original.save(outfile);
        } catch (IOException ex) {
            SFAdvancements.logger().log(Level.SEVERE, ex, () -> "No se pudieron guardar los grupos de progreso");
        }
    }

    private void importAdvancements(Plugin pl, InputStream advInputStream) {
        File outfile = new File(SFAdvancements.instance().getDataFolder(), "advancements.yml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(advInputStream));
        YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
        YamlConfiguration original = SFAdvancements.instance().getAdvancementConfig();

        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            if (original.isSet(key)) {
                SFAdvancements.info("progreso " + key + " ya existe; no se sobrescribe.");
            } else {
                original.set(key, config.get(key));
            }
        }
        try {
            original.save(outfile);
        } catch (IOException ex) {
            SFAdvancements.logger().log(Level.SEVERE, ex, () -> "No se pudieron guardar los progresos");
        }
    }

    @Nonnull
    @Override
    public String getCommandName() {
        return "import";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> res = new ArrayList<>();
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        for (Plugin plugin : plugins) {
            String name = plugin.getName();
            if (name.contains(args[1])) {
                res.add(name);
            }
        }
        return res;
    }
}
