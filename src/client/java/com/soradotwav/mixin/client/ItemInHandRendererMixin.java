package com.soradotwav.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soradotwav.WaylightClient;
import com.soradotwav.waylight.lantern.LanternPosition;
import com.soradotwav.waylight.lantern.VirtualLanternState;
import com.soradotwav.waylight.render.LanternModelRenderer;
import com.soradotwav.waylight.render.LanternPoseController;
import com.soradotwav.waylight.render.LanternRigResolver;
//? if >=26.3 {
/*import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
*///? } else {
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
//? }
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.3 {
/*@Mixin(FirstPersonHandsAndItemsRenderer.class)
*///? } else {
@Mixin(ItemInHandRenderer.class)
//? }
abstract class ItemInHandRendererMixin {
    //? if >=26.3 {
    /*@Inject(method = "submitHandsWithItems", at = @At("TAIL"))
    private void waylight$renderHandLantern(
            float tickDelta,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            PlayerRenderState playerRenderState,
            FirstPersonHandsAndItemsRenderState handsRenderState,
            CallbackInfo ci) {
        if (playerRenderState.avatarRenderState == null || handsRenderState.handRenderSelection == null) {
            return;
        }
        int packedLight = playerRenderState.avatarRenderState.lightCoords;
    *///? } else {
    //? if >=26.2 {
    /*@Inject(method = "submitHandsWithItems", at = @At("TAIL"))
    *///? } else {
    @Inject(
            method = "renderHandsWithItems",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"))
    //? }
    private void waylight$renderHandLantern(
            float tickDelta,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            LocalPlayer localPlayer,
            int packedLight,
            CallbackInfo ci) {
    //? }
        VirtualLanternState state = WaylightClient.runtime().lanternController().getState();
        if (state.lanternPosition() != LanternPosition.LEFT_HAND
                || !state.modelVisible()
                || state.temporarilySuppressed()) {
            return;
        }

        LanternRigResolver rigResolver = WaylightClient.runtime().rigResolver();
        LanternPoseController.PoseState poseState =
                WaylightClient.runtime().poseController().getPoseState();

        BlockState lanternBlockState = rigResolver.lanternBlockState(state.lanternType());
        LanternRigResolver.Transform transform = rigResolver.resolveThirdPerson(state, poseState);
        LanternRigResolver.Projection projection = rigResolver.projectHandLeft(
                transform, WaylightClient.runtime().configManager().get().firstPersonHandMotion);

        poseStack.pushPose();
        poseStack.translate(projection.translateX(), projection.translateY(), projection.translateZ());
        LanternModelRenderer.rotate(poseStack, Axis.YP.rotationDegrees(projection.rotateY()));
        LanternModelRenderer.rotate(poseStack, Axis.XP.rotationDegrees(projection.rotateX()));
        LanternModelRenderer.rotate(poseStack, Axis.ZP.rotationDegrees(projection.rotateZ()));
        poseStack.scale(projection.scale(), projection.scale(), projection.scale());
        poseStack.translate(-0.5F, -0.65F, -0.5F);
        LanternModelRenderer.submit(poseStack, submitNodeCollector, lanternBlockState, packedLight, 0);
        poseStack.popPose();
    }
}
