package com.soradotwav.waylight.render;

//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
*///? } else {
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
//? }
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public final class WaylightRenderHooks {
    public static final RenderStateDataKey<Boolean> LOCAL_PLAYER_RENDER_STATE =
            RenderStateDataKey.create(() -> "waylight:is_local_player");

    private WaylightRenderHooks() {}

    public static void register() {
        //? if >=26.1 {
        /*LivingEntityRenderLayerRegistrationCallback.EVENT.register(
        *///? } else {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
        //? }
                (entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
                        registrationHelper.register(new WaylightPlayerRenderFeature(avatarRenderer));
                    }
                });
    }
}
