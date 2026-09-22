package com.soradotwav;

import com.mojang.blaze3d.platform.InputConstants;
import com.soradotwav.waylight.WaylightRuntime;
import com.soradotwav.waylight.render.WaylightRenderHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///? } else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//? }
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class WaylightClient implements ClientModInitializer {
    private static final KeyMapping.Category WAYLIGHT_KEY_CATEGORY = KeyMapping.Category
            .register(Identifier.fromNamespaceAndPath(Waylight.MOD_ID, "general"));

    private static final WaylightRuntime RUNTIME = new WaylightRuntime();

    public static WaylightRuntime runtime() {
        return RUNTIME;
    }

    @Override
    public void onInitializeClient() {
        RUNTIME.configManager().load();
        WaylightRenderHooks.register();

        //? if >=26.1 {
        /*KeyMapping toggleLanternKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.waylight.toggle_lantern", InputConstants.KEY_G, WAYLIGHT_KEY_CATEGORY));
        *///? } else {
        KeyMapping toggleLanternKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.waylight.toggle_lantern", InputConstants.KEY_G, WAYLIGHT_KEY_CATEGORY));
        //? }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RUNTIME.lanternController().tick(client);
            RUNTIME.poseController().tick(client, RUNTIME.lanternController().getState());
            RUNTIME.dynamicLightsAdapter().tick(client);

            while (toggleLanternKey.consumeClick()) {
                RUNTIME.lanternController().toggle(client);
            }
        });

        Waylight.LOGGER.info("Initializing {} client", Waylight.MOD_ID);
    }
}
