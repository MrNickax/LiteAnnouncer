package studio.trc.bukkit.liteannouncer.configuration;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.liteannouncer.message.MessageUtil;

/**
 * Used to manage configuration files.
 * @author Dean
 */
public class RobustConfiguration
{
    private final FileConfiguration config;
    private final ConfigurationType type;
    
    public RobustConfiguration(FileConfiguration config, ConfigurationType type) {
        this.config = config;
        this.type = type;
    }
    
    public void repairConfigurationSection(String path) {
        if (type.equals(ConfigurationType.ANNOUNCEMENTS) || type.equals(ConfigurationType.COMPONENTS)) return;
        FileConfiguration defaultFile = DefaultConfigurationFile.getDefaultConfig(type);
        config.set(path, defaultFile.get(path) != null ? defaultFile.get(path) : "null");
        saveConfig();
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{config}", type.getFileName());
        placeholders.put("{path}", path);
        MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Repaired-Config-Section", placeholders);
    }
    
    public Object get(String path) {
        return config.get(path);
    }

    public String getString(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getString(path);
        } else {
            return config.getString(path);
        }
    }

    public int getInt(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getInt(path);
        } else {
            return config.getInt(path);
        }
    }

    public double getDouble(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getDouble(path);
        } else {
            return config.getDouble(path);
        }
    }

    public long getLong(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getLong(path);
        } else {
            return config.getLong(path);
        }
    }

    public boolean getBoolean(String path) {
        if (config.get(path) == null) {
            return false;
        } else {
            return config.getBoolean(path);
        }
    }
    
    public List getList(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getList(path);
        } else {
            return config.getList(path);
        }
    }

    public List<String> getStringList(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getStringList(path);
        } else {
            return config.getStringList(path);
        }
    }

    public List<Integer> getIntegerList(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getIntegerList(path);
        } else {
            return config.getIntegerList(path);
        }
    }

    public ItemStack getItemStack(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getItemStack(path);
        } else {
            return config.getItemStack(path);
        }
    }

    public ConfigurationSection getConfigurationSection(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getConfigurationSection(path);
        } else {
            return config.getConfigurationSection(path);
        }
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    public void set(String path, Object obj) {
        config.set(path, obj);
    }
    
    public void saveConfig() {
        ConfigurationUtil.saveConfig(type);
    }
    
    public FileConfiguration getRawConfig() {
        return config;
    }
    
    public ConfigurationType getConfigType() {
        return type;
    }
}
