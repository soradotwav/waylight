package com.soradotwav.waylight.lantern;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.chat.Component;

public enum LanternType {
    @SerializedName("normal")
    NORMAL("waylight.config.value.lantern.normal"), @SerializedName("soul")
    SOUL("waylight.config.value.lantern.soul");

    private final String translationKey;

    LanternType(String translationKey) {
        this.translationKey = translationKey;
    }

    public static LanternType orDefault(LanternType value) {
        return value == null ? NORMAL : value;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }

    @Override
    public String toString() {
        return label().getString();
    }
}
