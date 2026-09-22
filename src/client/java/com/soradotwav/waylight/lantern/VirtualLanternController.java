package com.soradotwav.waylight.lantern;

import com.soradotwav.waylight.config.WaylightConfig;
import com.soradotwav.waylight.config.WaylightConfigManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public final class VirtualLanternController {
    private static final float EQUIP_VOLUME = 0.8F;
    private static final float EQUIP_PITCH_ON = 1.0F;
    private static final float EQUIP_PITCH_OFF = 0.9F;

    private static final float EXTINGUISH_VOLUME = 0.2F;
    private static final float EXTINGUISH_PITCH = 1.1F;
    private static final float RELIGHT_VOLUME = 0.22F;
    private static final float RELIGHT_PITCH = 1.25F;

    private final WaylightConfigManager configManager;

    private VirtualLanternState currentState =
            new VirtualLanternState(false, LanternType.NORMAL, LanternPosition.RIGHT_HIP, false, false, false, false);

    public VirtualLanternController(WaylightConfigManager configManager) {
        this.configManager = configManager;
    }

    public void toggle(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }

        WaylightConfig config = configManager.get();
        int localBrightness = client.level.getMaxLocalRawBrightness(player.blockPosition());

        if (!config.enabled && config.autoUnequipInBrightness && localBrightness > config.autoLightThreshold) {
            sendOverlayMessage(player, Component.translatable("message.waylight.too_bright"));
            return;
        }

        if (config.enabled && config.autoEquipInDarkness && localBrightness <= config.autoLightThreshold) {
            sendOverlayMessage(player, Component.translatable("message.waylight.too_dark"));
            return;
        }

        config.enabled = !config.enabled;
        configManager.save();

        playLanternSound(player, config.enabled);
        sendOverlayMessage(
                player,
                Component.translatable(config.enabled ? "message.waylight.lantern_on" : "message.waylight.lantern_off"));
    }

    private static void sendOverlayMessage(LocalPlayer player, Component message) {
        //? if >=26.1 {
        /*player.sendOverlayMessage(message);
        *///? } else {
        player.displayClientMessage(message, true);
        //? }
    }

    public void tick(Minecraft client) {
        applyAutoLanternBehavior(client);

        VirtualLanternState previousState = currentState;
        currentState = resolveState(client);

        LocalPlayer player = client.player;
        if (player != null
                && previousState.enabled()
                && currentState.enabled()
                && hasTransition(previousState, currentState)) {
            playTransitionSound(player, previousState, currentState);
        }
    }

    public VirtualLanternState getState() {
        return currentState;
    }

    private VirtualLanternState resolveState(Minecraft client) {
        WaylightConfig config = configManager.get();
        return resolveState(client, config, config.enabled);
    }

    private VirtualLanternState resolveState(Minecraft client, WaylightConfig config, boolean enabled) {
        LocalPlayer player = client.player;
        boolean firstPerson = client.options.getCameraType() == CameraType.FIRST_PERSON;

        if (!enabled || player == null || !player.isAlive() || player.isRemoved()) {
            return new VirtualLanternState(
                    enabled, config.lanternType, config.lanternPosition, false, false, false, false);
        }

        if (config.lanternPosition.isHandHeld()
                && (player.isSwimming() || !player.getOffhandItem().isEmpty())) {
            return new VirtualLanternState(true, config.lanternType, config.lanternPosition, false, false, true, false);
        }

        boolean lightActive = !firstPerson || config.firstPersonLight;
        boolean modelVisible = config.lanternPosition.isHandHeld() || !firstPerson;

        if (config.extinguishUnderwater && player.isUnderWater()) {
            return new VirtualLanternState(
                    true, config.lanternType, config.lanternPosition, false, modelVisible, false, true);
        }

        return new VirtualLanternState(
                true, config.lanternType, config.lanternPosition, lightActive, modelVisible, false, false);
    }

    private void applyAutoLanternBehavior(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }

        WaylightConfig config = configManager.get();
        if (!config.autoEquipInDarkness && !config.autoUnequipInBrightness) {
            return;
        }

        int localBrightness = client.level.getMaxLocalRawBrightness(player.blockPosition());
        boolean tooDark = localBrightness <= config.autoLightThreshold;
        boolean brightEnough = localBrightness > config.autoLightThreshold;
        boolean changed = false;

        if (config.autoEquipInDarkness && !config.enabled && tooDark && canAutoEnable(client, config, player)) {
            config.enabled = true;
            playLanternSound(player, true);
            changed = true;
        } else if (config.autoUnequipInBrightness && config.enabled && brightEnough) {
            config.enabled = false;
            playLanternSound(player, false);
            changed = true;
        }

        if (changed) {
            configManager.save();
        }
    }

    private boolean canAutoEnable(Minecraft client, WaylightConfig config, LocalPlayer player) {
        VirtualLanternState resolved = resolveState(client, config, true);
        return !resolved.temporarilySuppressed()
                && !resolved.underwaterExtinguished()
                && (resolved.lightActive() || resolved.modelVisible())
                && player.isAlive()
                && !player.isRemoved();
    }

    private static void playLanternSound(LocalPlayer player, boolean active) {
        player.playSound(
                SoundEvents.ARMOR_EQUIP_CHAIN.value(), EQUIP_VOLUME, active ? EQUIP_PITCH_ON : EQUIP_PITCH_OFF);
    }

    private static void playTransitionSound(
            LocalPlayer player, VirtualLanternState previousState, VirtualLanternState currentState) {
        if (previousState.temporarilySuppressed() != currentState.temporarilySuppressed()) {
            playLanternSound(player, !currentState.temporarilySuppressed());
            return;
        }

        if (previousState.underwaterExtinguished() && !currentState.underwaterExtinguished()) {
            player.playSound(SoundEvents.FLINTANDSTEEL_USE, RELIGHT_VOLUME, RELIGHT_PITCH);
            return;
        }

        if (!previousState.underwaterExtinguished() && currentState.underwaterExtinguished()) {
            player.playSound(SoundEvents.FIRE_EXTINGUISH, EXTINGUISH_VOLUME, EXTINGUISH_PITCH);
            return;
        }

        if (previousState.lightActive() != currentState.lightActive()) {
            playLanternSound(player, currentState.lightActive());
        }
    }

    private static boolean hasTransition(VirtualLanternState previousState, VirtualLanternState currentState) {
        return previousState.temporarilySuppressed() != currentState.temporarilySuppressed()
                || previousState.underwaterExtinguished() != currentState.underwaterExtinguished()
                || previousState.lightActive() != currentState.lightActive();
    }
}
