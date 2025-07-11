package studio.trc.bungee.liteannouncer.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import studio.trc.bungee.liteannouncer.Main;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationType;
import studio.trc.bungee.liteannouncer.configuration.ConfigurationUtil;
import studio.trc.bungee.liteannouncer.message.color.ColorUtils;
import studio.trc.bungee.liteannouncer.util.PluginControl;

public class MessageUtil
{
    private static final Map<String, String> defaultPlaceholders = new HashMap();
    
    public static void loadPlaceholders() {
        defaultPlaceholders.clear();
        defaultPlaceholders.put("{plugin_version}", Main.getInstance().getDescription().getVersion());
        defaultPlaceholders.put("{language}", getLanguage());
        defaultPlaceholders.put("{prefix}", getPrefix());
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param path Messages.yml's path
     */
    public static void sendMessage(CommandSender sender, String path) {
        if (sender == null) return;
        List<String> messages = ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getStringList(getLanguage() + "." + path);
        if (messages.isEmpty() && !ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path).equals("[]")) {
            sender.sendMessage(ColorUtils.toColor(replacePlaceholders(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path), new HashMap())));
        } else {
            for (String message : messages) {
                sender.sendMessage(ColorUtils.toColor(replacePlaceholders(sender, message, new HashMap())));
            }
        }
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param path Messages.yml's path
     * @param placeholders If the text contains a placeholder,
     *                      The placeholder will be replaced with the specified text.
     */
    public static void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        if (sender == null) return;
        List<String> messages = ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getStringList(getLanguage() + "." + path);
        if (messages.isEmpty() && !ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path).equals("[]")) {
            sender.sendMessage(ColorUtils.toColor(replacePlaceholders(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path), placeholders)));
        } else {
            for (String message : messages) {
                sender.sendMessage(ColorUtils.toColor(replacePlaceholders(sender, message, placeholders)));
            }
        }
    }
    
    /**
     * Send Json message to player or console
     * @param sender Receiver.
     * @param message Target text.
     * @param baseComponents Json components.
     */
    public static void sendJsonMessage(CommandSender sender, String message, Map<String, BaseComponent> baseComponents) {
        List<BaseComponent> components = createJsonMessage(sender, message, baseComponents);
        if (sender instanceof ProxiedPlayer) {
            ((ProxiedPlayer) sender).sendMessage(components.toArray(new BaseComponent[0]));
        } else {
            StringBuilder sb = new StringBuilder();
            for (BaseComponent compoents : components) {
                sb.append(compoents.toPlainText());
            }
            sender.sendMessage(ColorUtils.toColor(sb.toString()));
        }
    }
    
    /**
     * Send Json message to player or console
     * @param sender Receiver.
     * @param message Target text.
     * @param baseComponents Json components.
     * @param placeholders Replace placeholders.
     */
    public static void sendJsonMessage(CommandSender sender, String message, Map<String, BaseComponent> baseComponents, Map<String, String> placeholders) {
        List<BaseComponent> components = createJsonMessage(sender, replacePlaceholders(sender, message, placeholders), baseComponents);
        if (sender instanceof ProxiedPlayer) {
            ((ProxiedPlayer) sender).sendMessage(components.toArray(new BaseComponent[0]));
        } else {
            StringBuilder sb = new StringBuilder();
            for (BaseComponent compoents : components) {
                sb.append(compoents.toPlainText());
            }
            sender.sendMessage(ColorUtils.toColor(sb.toString()));
        }
    }
    
    /**
     * Replace all placeholders to the corresponding text.
     * @param sender Use for hook PlaceholderAPI
     * @param message Target text.
     * @param placeholders Placeholders.
     *                      Defaultly come with {prefix} for plugin's prefix.
     * @return 
     */
    public static String replacePlaceholders(CommandSender sender, String message, Map<String, String> placeholders) {
        placeholders.put("{prefix}", PluginControl.getPrefix());
        List<TextParagraph> splitedTexts = splitIntoParagraphs(message, placeholders);
        StringBuilder string = new StringBuilder();
        for (TextParagraph paragraph : splitedTexts) {
            if (paragraph.isPlaceholder()) {
                string.append(paragraph.getText());
            } else {
                string.append(message.substring(paragraph.start(), paragraph.end()).replace("/n", "\n"));
            }
        }
        return string.toString();
    }
    
    /**
     * Create Json message
     * @param sender Use for hook PlaceholderAPI
     * @param message Target text.
     * @param baseComponents Json components placeholders.
     * @return 
     */
    public static List<BaseComponent> createJsonMessage(CommandSender sender, String message, Map<String, BaseComponent> baseComponents) {
        List<TextParagraph> splitedTexts = splitIntoComponentParagraphs(message, baseComponents);
        List<BaseComponent> components = new ArrayList<>();
        for (TextParagraph paragraph : splitedTexts) {
            if (paragraph.isPlaceholder()) {
                components.add(paragraph.getComponent());
            } else {
                components.add(new TextComponent(ColorUtils.toColor(message.substring(paragraph.start(), paragraph.end()).replace("/n", "\n"))));
            }
        }
        return components;
    }
    
    /**
     * Split placeholders at text into paragraphs.
     * 
     * Example: 
     *     Text: "Today is {year}/{month}/{day}."
     *     Placeholders: "{year}"="2021", "{month}"="7", "{day}"="1"
     *     ------->
     *     "[Today is ], [2021], [/], [7], [/], [1], [.]" as array instance.
     * 
     * @param message Target text.
     * @param placeholders Placeholders.
     * @return 
     */
    public static List<TextParagraph> splitIntoParagraphs(String message, Map<String, String> placeholders) {
        List<TextParagraph> splitedTexts = new ArrayList<>();
        splitedTexts.add(new TextParagraph(0, message.length(), message));
        for (String placeholder : placeholders.keySet()) {
            List<TextParagraph> newArray = new ArrayList();
            for (TextParagraph textParagraphs : splitedTexts) {
                String message_lowerCase = textParagraphs.getText().toLowerCase();
                String placeholder_lowerCase = placeholder.toLowerCase();
                if (message_lowerCase.contains(placeholder_lowerCase)) {
                    String[] splitText = message_lowerCase.split(escape(placeholder_lowerCase), -1);
                    int last = textParagraphs.start();
                    for (String paragraph : splitText) {
                        int next = last + paragraph.length();
                        if (last != next) {
                            TextParagraph subParagraph = new TextParagraph(last, next, paragraph);
                            last = last + paragraph.length();
                            newArray.add(subParagraph);
                        }
                        if (last < textParagraphs.end()) {
                            TextParagraph insertPlaceholder = new TextParagraph(last, last + placeholder.length(), placeholders.get(placeholder), placeholder);
                            last = last + placeholder.length();
                            newArray.add(insertPlaceholder);
                        }
                    }
                } else {
                    newArray.add(textParagraphs);
                }
            }
            splitedTexts.clear();
            splitedTexts.addAll(newArray);
        }
        return splitedTexts;
    }
    
    /**
     * Split placeholders at text components into paragraphs.
     * 
     * Example: 
     *     Text: "Today is {year}/{month}/{day}."
     *     Placeholders: "{year}"="2021", "{month}"="7", "{day}"="1"
     *     ------->
     *     "[Today is ], [2021], [/], [7], [/], [1], [.]" as array instance.
     * 
     * @param message Target text.
     * @param baseComponents Json components placeholders.
     * @return 
     */
    public static List<TextParagraph> splitIntoComponentParagraphs(String message, Map<String, BaseComponent> baseComponents) {
        List<TextParagraph> splitedTexts = new ArrayList<>();
        splitedTexts.add(new TextParagraph(0, message.length(), new TextComponent(message)));
        for (String placeholder : baseComponents.keySet()) {
            List<TextParagraph> newArray = new ArrayList();
            for (TextParagraph textParagraphs : splitedTexts) {
                String message_lowerCase = textParagraphs.getComponent().toPlainText().toLowerCase();
                String placeholder_lowerCase = placeholder.toLowerCase();
                if (message_lowerCase.contains(placeholder_lowerCase)) {
                    String[] splitText = message_lowerCase.split(placeholder_lowerCase, -1);
                    int last = textParagraphs.start();
                    for (String paragraph : splitText) {
                        int next = last + paragraph.length();
                        if (last != next) {
                            TextParagraph subParagraph = new TextParagraph(last, next, new TextComponent(paragraph));
                            last = last + paragraph.length();
                            newArray.add(subParagraph);
                        }
                        if (last < textParagraphs.end()) {
                            TextParagraph insertComponent = new TextParagraph(last, last + placeholder.length(), baseComponents.get(placeholder), placeholder);
                            last = last + placeholder.length();
                            newArray.add(insertComponent);
                        }
                    }
                } else {
                    newArray.add(textParagraphs);
                }
            }
            splitedTexts.clear();
            splitedTexts.addAll(newArray);
        }
        return splitedTexts;
    }
    
    public static String escape(String text) {
        return text.replace("{", "\\{").replace("}", "\\}");
    }
    
    public static String getMessage(String path) {
        return ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path);
    }
    
    public static List<String> getMessageList(String path) {
        return ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getStringList(getLanguage() + "." + path);
    }
    
    public static String getLanguage() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Language");
    }

    public static String getPrefix() {
        return ColorUtils.toColor(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Prefix"));
    }
    
    public static String toLocallyPlaceholders(String text, ProxiedPlayer player) {
        Map<String, String> placeholders = new HashMap();
        placeholders.put("%player_ip%", player.getSocketAddress().toString().substring(1).split(":")[0]);
        placeholders.put("%player_country%", player.getLocale().getCountry());
        placeholders.put("%player_language%", player.getLocale().getLanguage());
        placeholders.put("%player_server%", player.getServer().getInfo().getName());
        placeholders.put("%player_displayname%", player.getDisplayName());
        placeholders.put("%player_name%", player.getName());
        placeholders.put("%player_ping%", String.valueOf(player.getPing()));
        placeholders.put("%player_uuid%", player.getUniqueId().toString());
        return ColorUtils.toColor(replacePlaceholders(player, text, placeholders));
    }
    
    /**
     * Plugin langauge
     */
    public static enum Language {
        
        /**
         * Simplified Chinese
         */
        SIMPLIFIED_CHINESE("Simplified-Chinese"),
        
        /**
         * Traditional Chinese
         */
        TRADITIONAL_CHINESE("Traditional-Chinese"),
        
        /**
         * English
         */
        ENGLISH("English");
        
        public static Language getLocaleLanguage() {
            Locale lang = Locale.getDefault();
            if (lang.equals(Locale.SIMPLIFIED_CHINESE)) {
                return SIMPLIFIED_CHINESE;
            } else if (lang.equals(Locale.TRADITIONAL_CHINESE)) {
                return TRADITIONAL_CHINESE;
            } else if (lang.equals(Locale.CHINA) || lang.equals(Locale.CHINESE)) {
                return SIMPLIFIED_CHINESE;
            } else {
                return ENGLISH;
            }
        }
        
        private final String folderName;
        
        private Language(String folderName) {
            this.folderName = folderName;
        }
        
        public String getFolderName() {
            return folderName;
        }
    }
    
    /**
     * Use for placeholders manager.
     */
    public static class TextParagraph {
        
        private final int startsWith;
        private final int endsWith;
        private final BaseComponent component;
        private final String text;
        private final String placeholder;
        
        public TextParagraph(int startsWith, int endsWith, BaseComponent component, String placeholder) {
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.component = component;
            this.placeholder = placeholder;
            this.text = component.toPlainText();
        }
        
        public TextParagraph(int startsWith, int endsWith, BaseComponent component) {
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.component = component;
            this.placeholder = null;
            this.text = component.toPlainText();
        }
        
        public TextParagraph(int startsWith, int endsWith, String text, String placeholder) {
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.component = null;
            this.placeholder = placeholder;
            this.text = text;
        }
        
        public TextParagraph(int startsWith, int endsWith, String text) {
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.component = null;
            this.placeholder = null;
            this.text = text;
        }
        
        public int start() {
            return startsWith;
        }
        
        public int end() {
            return endsWith;
        }
        
        public boolean isPlaceholder() {
            return placeholder != null;
        }
        
        public BaseComponent getComponent() {
            return component;
        }
        
        public String getText() {
            return text;
        }
        
        public String getPlaceholder() {
            return placeholder;
        }
    }
}
