package com.soradotwav.waylight.render;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.chat.Component;

public enum FirstPersonHandMotionMode {
    @SerializedName("physics")
    PHYSICS("waylight.config.value.first_person_hand_motion.physics"), @SerializedName("static")
    STATIC("waylight.config.value.first_person_hand_motion.static");

    private final String translationKey;

    FirstPersonHandMotionMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public static FirstPersonHandMotionMode orDefault(FirstPersonHandMotionMode value) {
        return value == null ? PHYSICS : value;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }

    @Override
    public String toString() {
        return label().getString();
    }
}
