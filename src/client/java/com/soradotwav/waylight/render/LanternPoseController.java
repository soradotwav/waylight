package com.soradotwav.waylight.render;

import com.soradotwav.waylight.config.WaylightConfigManager;
import com.soradotwav.waylight.lantern.LanternPosition;
import com.soradotwav.waylight.lantern.VirtualLanternState;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class LanternPoseController {
    private static final MotionTuning BASE = new MotionTuning(
            0.096F, 0.08F, 0.89F, 0.88F, 0.84F, 86.0F, 34.0F, 18.0F, 62.4F, 70.8F, 78.0F, 84.0F, 0.16F, 0.132F, 1.0F,
            0.65F, 0.08F, -3.15F, 0.72F, 5.1F, 5.4F, 0.85F);

    private static final CarryProfile DEFAULT = new CarryProfile(1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
    private static final CarryProfile HIP_THIRD_PERSON = new CarryProfile(0.48F, 1.0F, 1.0F, 1.0F, 1.0F);
    private static final CarryProfile HAND_LEFT_THIRD_PERSON = new CarryProfile(0.72F, 1.0F, 1.0F, 1.0F, 1.0F);
    private static final CarryProfile HAND_LEFT_FIRST_PERSON = new CarryProfile(1.0F, 0.75F, 0.75F, 0.75F, 0.75F);

    private final WaylightConfigManager configManager;
    private final PoseState poseState = new PoseState();

    private float lastYaw;
    private double lastVelocityX;
    private double lastVelocityY;
    private double lastVelocityZ;
    private boolean wasOnGround;

    public LanternPoseController(WaylightConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(Minecraft client, VirtualLanternState lanternState) {
        poseState.prevPitchAngle = poseState.pitchAngle;
        poseState.prevRollAngle = poseState.rollAngle;
        poseState.prevYawLag = poseState.yawLag;
        poseState.prevBob = poseState.bob;

        LocalPlayer player = client.player;
        if (player == null || !lanternState.enabled()) {
            decayToRest();
            return;
        }

        var velocity = player.getDeltaMovement();

        double accelerationX = velocity.x - lastVelocityX;
        double accelerationY = velocity.y - lastVelocityY;
        double accelerationZ = velocity.z - lastVelocityZ;
        double horizontalSpeed = velocity.horizontalDistance();

        float yawDelta = Mth.wrapDegrees(player.getYRot() - lastYaw);

        boolean onGround = player.onGround();
        boolean sprinting = player.isSprinting();
        boolean crouching = player.isCrouching();
        boolean firstPerson = client.options.getCameraType() == CameraType.FIRST_PERSON;

        CarryProfile carryProfile = resolveCarryProfile(lanternState.lanternPosition(), firstPerson);

        lastVelocityX = velocity.x;
        lastVelocityY = velocity.y;
        lastVelocityZ = velocity.z;
        lastYaw = player.getYRot();

        float yawRadians = (float) Math.toRadians(player.getYRot());
        float forwardAcceleration = (float) (-Math.sin(yawRadians) * accelerationX
                + Math.cos(yawRadians) * accelerationZ);
        float sidewaysAcceleration = (float) (Math.cos(yawRadians) * accelerationX
                + Math.sin(yawRadians) * accelerationZ);

        float targetBob = (float) Math.sin(player.tickCount * 0.22F)
                * Mth.clamp((float) horizontalSpeed * 8.0F, 0.0F, 1.3F)
                * carryProfile.modeMotionScale();

        float motionIntensity = Mth.clamp(configManager.get().motionIntensity / 100.0F, 0.25F, 2.0F);

        float motionMultiplier = sprinting
                ? BASE.sprintMotionMultiplier()
                : crouching ? BASE.crouchMotionMultiplier() : 1.0F;

        if (!onGround) {
            motionMultiplier *= BASE.airMotionMultiplier();
        }

        motionMultiplier *= motionIntensity * carryProfile.modeMotionScale() * carryProfile.motionScale();

        float pitchDamping = crouching ? BASE.pitchDamping() + BASE.crouchDampingBonus() : BASE.pitchDamping();
        float rollDamping = crouching ? BASE.rollDamping() + BASE.crouchDampingBonus() : BASE.rollDamping();

        float fallPitchTarget = 0.0F;
        if (!onGround && velocity.y < -0.08) {
            fallPitchTarget = -Mth.clamp(
                    (float) (-velocity.y)
                            * BASE.fallPitchBiasScale()
                            * motionIntensity
                            * carryProfile.modeMotionScale(),
                    0.0F,
                    BASE.maxFallPitchTarget())
                    * carryProfile.fallScale();
        }

        poseState.pitchVelocity += ((-forwardAcceleration * BASE.pitchAccelerationScale())
                + ((float) accelerationY * -14.0F))
                * motionMultiplier;

        poseState.rollVelocity += ((sidewaysAcceleration * BASE.rollAccelerationScale())
                - (yawDelta * BASE.turnScale()))
                * motionMultiplier;

        poseState.pitchVelocity += (fallPitchTarget - poseState.pitchAngle) * BASE.fallPitchStiffness();

        if (wasOnGround && !onGround && velocity.y > 0.0) {
            poseState.pitchVelocity += BASE.jumpPitchImpulse()
                    * motionIntensity
                    * carryProfile.modeMotionScale()
                    * carryProfile.motionScale();
        } else if (!wasOnGround && onGround && lastVelocityY < -0.08) {
            poseState.pitchVelocity += Math.min(
                    (float) (-lastVelocityY
                            * BASE.landingPitchImpulseScale()
                            * motionIntensity
                            * carryProfile.modeMotionScale()
                            * carryProfile.fallScale()),
                    BASE.maxLandingPitchImpulse());
            poseState.rollVelocity *= BASE.landingRollDamping();
        }

        wasOnGround = onGround;

        poseState.pitchVelocity += -poseState.pitchAngle * BASE.pitchStiffness();
        poseState.rollVelocity += -poseState.rollAngle * BASE.rollStiffness();

        poseState.pitchVelocity *= pitchDamping;
        poseState.rollVelocity *= rollDamping;

        poseState.pitchAngle = Mth.clamp(
                poseState.pitchAngle + poseState.pitchVelocity, -BASE.maxPitch(), BASE.maxPitch())
                * carryProfile.motionScale();

        poseState.rollAngle = Mth.clamp(poseState.rollAngle + poseState.rollVelocity, -BASE.maxRoll(), BASE.maxRoll())
                * carryProfile.motionScale();

        poseState.yawLag = Mth.clamp(
                (poseState.yawLag + yawDelta * 0.18F) * BASE.yawLagDamping(),
                -BASE.maxYawLag(),
                BASE.maxYawLag())
                * carryProfile.yawScale();

        poseState.bob = Mth.lerp(0.22F, poseState.bob, targetBob) * carryProfile.bobScale();
    }

    public PoseState getPoseState() {
        return poseState;
    }

    private void decayToRest() {
        lastVelocityX = 0.0;
        lastVelocityY = 0.0;
        lastVelocityZ = 0.0;
        wasOnGround = false;

        poseState.pitchVelocity *= 0.75F;
        poseState.rollVelocity *= 0.75F;

        poseState.pitchAngle = Mth.lerp(0.2F, poseState.pitchAngle, 0.0F);
        poseState.rollAngle = Mth.lerp(0.2F, poseState.rollAngle, 0.0F);
        poseState.yawLag = Mth.lerp(0.2F, poseState.yawLag, 0.0F);
        poseState.bob = Mth.lerp(0.2F, poseState.bob, 0.0F);
    }

    private static CarryProfile resolveCarryProfile(LanternPosition lanternPosition, boolean firstPerson) {
        if (lanternPosition == LanternPosition.LEFT_HAND) {
            return firstPerson ? HAND_LEFT_FIRST_PERSON : HAND_LEFT_THIRD_PERSON;
        }

        return !firstPerson && lanternPosition.isHipMounted() ? HIP_THIRD_PERSON : DEFAULT;
    }

    public static final class PoseState {
        private float pitchAngle;
        private float pitchVelocity;
        private float rollAngle;
        private float rollVelocity;
        private float yawLag;
        private float bob;
        private float prevPitchAngle;
        private float prevRollAngle;
        private float prevYawLag;
        private float prevBob;

        public float pitchAngle(float tickDelta) {
            return lerp(tickDelta, prevPitchAngle, pitchAngle);
        }

        public float rollAngle(float tickDelta) {
            return lerp(tickDelta, prevRollAngle, rollAngle);
        }

        public float yawLag(float tickDelta) {
            return lerp(tickDelta, prevYawLag, yawLag);
        }

        public float bob(float tickDelta) {
            return lerp(tickDelta, prevBob, bob);
        }

        private static float lerp(float tickDelta, float start, float end) {
            return start + (end - start) * tickDelta;
        }
    }

    private record MotionTuning(
            float pitchStiffness,
            float rollStiffness,
            float pitchDamping,
            float rollDamping,
            float yawLagDamping,
            float maxPitch,
            float maxRoll,
            float maxYawLag,
            float pitchAccelerationScale,
            float rollAccelerationScale,
            float fallPitchBiasScale,
            float maxFallPitchTarget,
            float fallPitchStiffness,
            float turnScale,
            float sprintMotionMultiplier,
            float crouchMotionMultiplier,
            float crouchDampingBonus,
            float jumpPitchImpulse,
            float airMotionMultiplier,
            float landingPitchImpulseScale,
            float maxLandingPitchImpulse,
            float landingRollDamping) {
    }

    private record CarryProfile(
            float modeMotionScale, float motionScale, float fallScale, float yawScale, float bobScale) {
    }
}
