package com.soradotwav.waylight.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.soradotwav.Waylight;
import com.soradotwav.waylight.lantern.LanternPosition;
import com.soradotwav.waylight.lantern.LanternType;
import com.soradotwav.waylight.render.FirstPersonHandMotionMode;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

public final class WaylightConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("waylight.json");

    private WaylightConfig config = new WaylightConfig();

    public void load() {
        if (Files.notExists(CONFIG_PATH)) {
            config = new WaylightConfig();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            WaylightConfig loaded = GSON.fromJson(reader, WaylightConfig.class);
            config = sanitize(loaded);
        } catch (IOException | JsonParseException exception) {
            Waylight.LOGGER.warn("Failed to load Waylight config from {}", CONFIG_PATH, exception);
            config = new WaylightConfig();
            save();
        }
    }

    public WaylightConfig get() {
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            Waylight.LOGGER.error("Failed to save Waylight config to {}", CONFIG_PATH, exception);
        }
    }

    public void update(Consumer<WaylightConfig> updater) {
        updater.accept(config);
        save();
    }

    private static WaylightConfig sanitize(WaylightConfig loaded) {
        WaylightConfig sanitized = loaded == null ? new WaylightConfig() : loaded;

        sanitized.lanternType = LanternType.orDefault(sanitized.lanternType);
        sanitized.lanternPosition = LanternPosition.orDefault(sanitized.lanternPosition);
        sanitized.firstPersonHandMotion = FirstPersonHandMotionMode.orDefault(sanitized.firstPersonHandMotion);

        sanitized.autoLightThreshold = Mth.clamp(sanitized.autoLightThreshold, 0, 15);
        sanitized.motionIntensity = Mth.clamp(sanitized.motionIntensity, 25, 200);

        return sanitized;
    }
}
