package com.soradotwav.waylight.lantern;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.chat.Component;

public enum LanternPosition {
    @SerializedName("right_hip")
    RIGHT_HIP("waylight.config.value.position.right_hip"), @SerializedName("left_hip")
    LEFT_HIP("waylight.config.value.position.left_hip"), @SerializedName("left_hand")
    LEFT_HAND("waylight.config.value.position.left_hand");

    private final String translationKey;

    LanternPosition(String translationKey) {
        this.translationKey = translationKey;
    }

    public static LanternPosition orDefault(LanternPosition value) {
        return value == null ? RIGHT_HIP : value;
    }

    public boolean isHandHeld() {
        return this == LEFT_HAND;
    }

    public boolean isHipMounted() {
        return this == RIGHT_HIP || this == LEFT_HIP;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }

    @Override
    public String toString() {
        return label().getString();
    }
}
