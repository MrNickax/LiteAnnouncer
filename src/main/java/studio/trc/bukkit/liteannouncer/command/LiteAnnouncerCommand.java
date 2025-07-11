package studio.trc.bukkit.liteannouncer.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import studio.trc.bukkit.liteannouncer.configuration.ConfigurationType;
import studio.trc.bukkit.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bukkit.liteannouncer.message.MessageUtil;
import studio.trc.bukkit.liteannouncer.util.PluginControl;
import studio.trc.bukkit.liteannouncer.util.tools.Announcement;

public class LiteAnnouncerCommand
    implements CommandExecutor, TabCompleter
{
    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("la") || command.getName().equalsIgnoreCase("liteannouncer")) {
            if (args.length == 0) {
                MessageUtil.sendCommandMessage(sender, "Unknown-Command");
            } else {
                if (args[0].equalsIgnoreCase("help")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.Help")) {
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "No-Permission");
                        return true;
                    }
                    MessageUtil.sendCommandMessage(sender, "Help-Command");
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.Reload")) {
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "No-Permission");
                        return true;
                    }
                    PluginControl.reload();
                    MessageUtil.sendCommandMessage(sender, "Reload");
                } else if (args[0].equalsIgnoreCase("view")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.View")) {
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "No-Permission");
                        return true;
                    }
                    if (args.length == 1) {
                        MessageUtil.sendCommandMessage(sender, "View.Help");
                        return true;
                    } else {
                        for (Announcement announcement: PluginControl.getAnnouncements()) {
                            if (args[1].equalsIgnoreCase(announcement.getName())) {
                                announcement.view(sender);
                                return true;
                            }
                        }
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{announcement}", args[1]);
                        MessageUtil.sendCommandMessage(sender, "View.Not-Found", placeholders);
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("broadcast")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.Broadcast")) {
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "No-Permission");
                        return true;
                    }
                    if (args.length == 1) {
                        MessageUtil.sendCommandMessage(sender, "Broadcast.Help");
                        return true;
                    } else {
                        for (Announcement announcement: PluginControl.getAnnouncements()) {
                            if (args[1].equalsIgnoreCase(announcement.getName())) {
                                announcement.broadcast();
                                return true;
                            }
                        }
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{announcement}", args[1]);
                        MessageUtil.sendCommandMessage(sender, "Broadcast.Not-Found", placeholders);
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.List")) {
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "No-Permission");
                        return true;
                    }
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    List<String> name = new ArrayList();
                    PluginControl.getAnnouncements().stream().forEach(announcement -> name.add(announcement.getName()));
                    placeholders.put("{list}", name.toString().substring(1, name.toString().length() - 1));
                    MessageUtil.sendCommandMessage(sender, "List", placeholders);
                    return true;
                } else if (args[0].equalsIgnoreCase("switch")) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.Switch")) {
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "No-Permission");
                        return true;
                    }
                    if (args.length < 2) {
                        MessageUtil.sendCommandMessage(sender, "Switch.Help");
                        return true;
                    }
                    Player player = Bukkit.getPlayer(args[1]);
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    if (player == null) {
                        placeholders.put("{player}", args[1]);
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Player-Not-Exist", placeholders);
                        return true;
                    }
                    placeholders.put("{player}", player.getName());
                    FileConfiguration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
                    List<String> list = data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") != null ? data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") : new ArrayList();
                    data.set("PlayerData." + player.getUniqueId() + ".Name", player.getName());
                    if (list.contains("ALL")) {
                        list.remove("ALL");
                        data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                        ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                        MessageUtil.sendCommandMessage(sender, "Switch.Switch-On", placeholders);
                    } else {
                        list.add("ALL");
                        data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                        ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                        MessageUtil.sendCommandMessage(sender, "Switch.Switch-Off", placeholders);
                    }
                } else if (args[0].equalsIgnoreCase("ignore")) {
                    if (args.length < 3) {
                        if (sender instanceof Player player) {
                            Announcement announcement = PluginControl.getAnnouncementsByPriority().stream().filter(announcement_ -> announcement_.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                            if (announcement == null) {
                                placeholders.put("{announcement}", args[1]);
                                MessageUtil.sendCommandMessage(sender, "Ignore.Not-Found", placeholders);
                                return true;
                            }
                            placeholders.put("{announcement}", announcement.getName());
                            FileConfiguration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
                            List<String> list = data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") != null ? data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") : new ArrayList();
                            data.set("PlayerData." + player.getUniqueId() + ".Name", player.getName());
                            if (list.contains(announcement.getName())) {
                                list.remove(announcement.getName());
                                data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                                ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                                MessageUtil.sendCommandMessage(sender, "Ignore.Ignore-On", placeholders);
                            } else {
                                list.add(announcement.getName());
                                data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                                ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                                MessageUtil.sendCommandMessage(sender, "Ignore.Ignore-Off", placeholders);
                            }
                        }
                        return true;
                    } else {
                        if (!PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                            MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "No-Permission");
                            return true;
                        }

                        Player player = Bukkit.getPlayer(args[2]);
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        if (player == null) {
                            placeholders.put("{player}", args[2]);
                            MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Player-Not-Exist", placeholders);
                            return true;
                        }
                        placeholders.put("{player}", player.getName());
                        Announcement announcement = PluginControl.getAnnouncementsByPriority().stream().filter(announcement_ -> announcement_.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);
                        if (announcement == null) {
                            placeholders.put("{announcement}", args[1]);
                            MessageUtil.sendCommandMessage(sender, "Ignore.Not-Found", placeholders);
                            return true;
                        }
                        placeholders.put("{announcement}", announcement.getName());
                        FileConfiguration data = ConfigurationUtil.getFileConfiguration(ConfigurationType.PLAYER_DATA);
                        List<String> list = data.get("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") != null ? data.getStringList("PlayerData." + player.getUniqueId() + ".Ignored-Announcements") : new ArrayList();
                        data.set("PlayerData." + player.getUniqueId() + ".Name", player.getName());
                        if (list.contains(announcement.getName())) {
                            list.remove(announcement.getName());
                            data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                            ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                            MessageUtil.sendCommandMessage(sender, "Ignore.Ignore-On", placeholders);
                        } else {
                            list.add(announcement.getName());
                            data.set("PlayerData." + player.getUniqueId() + ".Ignored-Announcements", list);
                            ConfigurationUtil.getConfig(ConfigurationType.PLAYER_DATA).saveConfig();
                            MessageUtil.sendCommandMessage(sender, "Ignore.Ignore-Off", placeholders);
                        }
                    }
                } else {
                    MessageUtil.sendCommandMessage(sender, "Unknown-Command");
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("view") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.View")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("broadcast") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Broadcast")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("switch") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Switch")) {
                return getTabPlayersName(args, 2);
            }
            if (args[0].equalsIgnoreCase("ignore") && args.length == 2 && PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                return getAnnouncements(args[1]);
            }
            if (args[0].equalsIgnoreCase("ignore") && args.length == 3 && PluginControl.hasPermission(sender, "Permissions.Commands.Ignore")) {
                return getTabPlayersName(args, 3);
            }
            return getCommands(args[0]);
        } else {
            return getCommands(null);
        }
    }
    
    private List<String> getAnnouncements(String args) {
        if (args != null) {
            List<String> names = new ArrayList();
            PluginControl.getAnnouncements().stream().filter(announcement -> announcement.getName().toLowerCase().startsWith(args.toLowerCase())).forEach(announcement -> {
                names.add(announcement.getName());
            });
            return names;
        } 
        return new ArrayList();
    }
    
    private List<String> getCommands(String args) {
        List<String> commands = Arrays.asList("help", "reload", "broadcast",  "view", "list", "switch", "ignore");
        if (args != null) {
            List<String> names = new ArrayList();
            commands.stream().filter(command -> command.startsWith(args.toLowerCase())).forEach(names::add);
            return names;
        }
        return commands;
    }
    
    private List<String> getTabPlayersName(String[] args, int length) {
        if (args.length == length) {
            List<String> onlines = Bukkit.getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
            List<String> names = new ArrayList();
            onlines.stream().filter(command -> command.toLowerCase().startsWith(args[length - 1].toLowerCase())).forEach(names::add);
            return names;
        }
        return new ArrayList();
    }
}
