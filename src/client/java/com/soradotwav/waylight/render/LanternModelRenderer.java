package com.soradotwav.waylight.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public final class LanternModelRenderer {
    private LanternModelRenderer() {}

    public static void submit(PoseStack poses, SubmitNodeCollector collector, BlockState state, int light, int outline) {
        //? if >=26.1 {
        /*var model = net.minecraft.client.Minecraft.getInstance().getModelManager().getBlockModelSet().get(state);
        var renderState = new net.minecraft.client.renderer.block.BlockModelRenderState();
        model.update(renderState, state, net.minecraft.client.renderer.block.model.BlockDisplayContext.create(), 42L);
        renderState.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, outline);
        *///? } else {
        collector.submitBlock(poses, state, light, OverlayTexture.NO_OVERLAY, outline);
        //? }
    }

    public static void rotate(PoseStack poses, Quaternionf rotation) {
        //? if >=26.3 {
        /*poses.rotate(rotation);
        *///? } else {
        poses.mulPose(rotation);
        //? }
    }
}
