package com.soradotwav.waylight.render;

import com.soradotwav.waylight.lantern.LanternPosition;
import com.soradotwav.waylight.lantern.LanternType;
import com.soradotwav.waylight.lantern.VirtualLanternState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.joml.Vector3f;

public final class LanternRigResolver {
    private static final float BODY_ANCHOR_HEIGHT = 0.92F;
    private static final float BODY_ANCHOR_HEIGHT_CROUCH = 0.78F;
    private static final float LEFT_ARM_ANCHOR_X = -0.32F;
    private static final float LEFT_ARM_ANCHOR_Y = 1.18F;
    private static final float LEFT_ARM_ANCHOR_Y_CROUCH = 1.04F;
    private static final float LEFT_ARM_ANCHOR_Z = 0.0F;

    private static final float EMISSION_X = 0.0F;
    private static final float EMISSION_Y = -0.28F;
    private static final float EMISSION_Z = 0.0F;
    private static final float EMISSION_FORWARD_BIAS = 0.08F;

    private static final float FIRST_PERSON_BASE_X = -0.84F;
    private static final float FIRST_PERSON_BASE_Y = 0.78F;
    private static final float FIRST_PERSON_BASE_Z = -0.92F;
    private static final float FIRST_PERSON_BASE_ROT_X = 8.0F;
    private static final float FIRST_PERSON_BASE_ROT_Y = 16.0F;
    private static final float FIRST_PERSON_BASE_ROT_Z = -8.0F;
    private static final float FIRST_PERSON_BASE_SCALE = 1.08F;

    private static final float HAND_LEFT_BASE_TRANSLATE_X = 0.05F;
    private static final float HAND_LEFT_BASE_TRANSLATE_Y = 0.60F;
    private static final float HAND_LEFT_BASE_TRANSLATE_Z = 0.10F;
    private static final float HAND_LEFT_BASE_ROT_X = -50.0F;
    private static final float HAND_LEFT_BASE_ROT_Y = -10.0F;
    private static final float HAND_LEFT_BASE_ROT_Z = 0.0F;

    public Transform resolve(VirtualLanternState lanternState, LanternPoseController.PoseState poseState) {
        return lanternState.lanternPosition().isHandHeld()
                ? resolveHandLeft(poseState)
                : resolveHip(poseState, lanternState.lanternPosition());
    }

    public Transform resolveThirdPerson(VirtualLanternState lanternState, LanternPoseController.PoseState poseState) {
        return resolve(lanternState, poseState);
    }

    public Projection projectHandLeft(Transform transform, FirstPersonHandMotionMode motionMode) {
        boolean staticMode = motionMode == FirstPersonHandMotionMode.STATIC;

        float translateXDelta = staticMode ? 0.0F : transform.translateX() - HAND_LEFT_BASE_TRANSLATE_X;
        float translateYDelta = staticMode ? 0.0F : transform.translateY() - HAND_LEFT_BASE_TRANSLATE_Y;
        float translateZDelta = staticMode ? 0.0F : transform.translateZ() - HAND_LEFT_BASE_TRANSLATE_Z;

        float rotateXDelta = staticMode ? 0.0F : transform.rotateX() - HAND_LEFT_BASE_ROT_X;
        float rotateYDelta = staticMode ? 0.0F : transform.rotateY() - HAND_LEFT_BASE_ROT_Y;
        float rotateZDelta = staticMode ? 0.0F : transform.rotateZ() - HAND_LEFT_BASE_ROT_Z;

        return new Projection(
                FIRST_PERSON_BASE_X + translateXDelta,
                FIRST_PERSON_BASE_Y + translateYDelta,
                FIRST_PERSON_BASE_Z + translateZDelta,
                FIRST_PERSON_BASE_ROT_X + rotateXDelta,
                FIRST_PERSON_BASE_ROT_Y + rotateYDelta,
                FIRST_PERSON_BASE_ROT_Z + rotateZDelta,
                FIRST_PERSON_BASE_SCALE);
    }

    public BlockState lanternBlockState(LanternType lanternType) {
        return lanternType == LanternType.SOUL
                ? Blocks.SOUL_LANTERN.defaultBlockState()
                : Blocks.LANTERN.defaultBlockState();
    }

    public void resolveWorldLightCore(LocalPlayer player, Transform transform, Vector3d destination) {
        Vector3f offset = new Vector3f(
                transform.translateX() + transform.emissionX(),
                transform.translateY() + transform.emissionY(),
                transform.translateZ() + transform.emissionZ());

        offset.z += EMISSION_FORWARD_BIAS;

        offset.rotateZ((float) Math.toRadians(transform.rotateZ()));
        offset.rotateX((float) Math.toRadians(transform.rotateX()));
        offset.rotateY((float) Math.toRadians(transform.rotateY()));
        offset.rotateY((float) Math.toRadians(-player.yBodyRot));

        Vector3f anchor = resolveAnchor(player, transform.attachment());
        destination.set(
                player.getX() + anchor.x() + offset.x(),
                player.getY() + anchor.y() + offset.y(),
                player.getZ() + anchor.z() + offset.z());
    }

    private static Transform resolveHip(LanternPoseController.PoseState poseState, LanternPosition lanternPosition) {
        float sideSign = lanternPosition == LanternPosition.LEFT_HIP ? 1.0F : -1.0F;
        float rotationSideSign = -sideSign;

        return new Transform(
                Attachment.BODY,
                0.2F * sideSign,
                0.60F + poseState.bob(1.0F) * 0.03F,
                -0.15F,
                165.0F + poseState.pitchAngle(1.0F),
                rotationSideSign * -35.0F + poseState.yawLag(1.0F),
                rotationSideSign * 15.0F + poseState.rollAngle(1.0F),
                EMISSION_X,
                EMISSION_Y,
                EMISSION_Z);
    }

    private static Transform resolveHandLeft(LanternPoseController.PoseState poseState) {
        return new Transform(
                Attachment.LEFT_ARM,
                0.05F,
                0.60F,
                0.1F,
                -50.0F + poseState.pitchAngle(1.0F) * 0.2F,
                -10.0F - poseState.yawLag(1.0F) * 0.2F,
                poseState.rollAngle(1.0F) * 0.2F,
                EMISSION_X,
                EMISSION_Y,
                EMISSION_Z);
    }

    private static Vector3f resolveAnchor(LocalPlayer player, Attachment attachment) {
        if (attachment == Attachment.LEFT_ARM) {
            return new Vector3f(
                    LEFT_ARM_ANCHOR_X,
                    player.isCrouching() ? LEFT_ARM_ANCHOR_Y_CROUCH : LEFT_ARM_ANCHOR_Y,
                    LEFT_ARM_ANCHOR_Z);
        }

        return new Vector3f(0.0F, player.isCrouching() ? BODY_ANCHOR_HEIGHT_CROUCH : BODY_ANCHOR_HEIGHT, 0.0F);
    }

    public record Transform(
            Attachment attachment,
            float translateX,
            float translateY,
            float translateZ,
            float rotateX,
            float rotateY,
            float rotateZ,
            float emissionX,
            float emissionY,
            float emissionZ) {
    }

    public enum Attachment {
        BODY, LEFT_ARM
    }

    public record Projection(
            float translateX,
            float translateY,
            float translateZ,
            float rotateX,
            float rotateY,
            float rotateZ,
            float scale) {
    }
}
