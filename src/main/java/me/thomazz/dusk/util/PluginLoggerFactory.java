package me.thomazz.dusk.util;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

@UtilityClass
public class PluginLoggerFactory {
    @Nullable private Logger logger;

    public void init(Plugin plugin) {
        PluginLoggerFactory.logger = plugin.getLogger();
    }

    @SuppressWarnings("unused")
    public Logger getLogger() {
        if (PluginLoggerFactory.logger != null) {
            return PluginLoggerFactory.logger;
        }

        return Logger.getAnonymousLogger();
    }
}
