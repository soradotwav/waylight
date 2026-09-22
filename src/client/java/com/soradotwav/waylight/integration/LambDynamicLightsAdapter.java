package com.soradotwav.waylight.integration;

import com.soradotwav.Waylight;
import com.soradotwav.waylight.lantern.VirtualLanternController;
import com.soradotwav.waylight.lantern.VirtualLanternState;
import com.soradotwav.waylight.render.LanternPoseController;
import com.soradotwav.waylight.render.LanternRigResolver;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

public final class LambDynamicLightsAdapter {
    private static final int LUMINANCE = 15;

    private final WaylightLanternBehavior behavior;
    private ClientLevel registeredLevel;

    public LambDynamicLightsAdapter(
            VirtualLanternController lanternController,
            LanternPoseController poseController,
            LanternRigResolver rigResolver) {
        this.behavior = new WaylightLanternBehavior(lanternController, poseController, rigResolver);
    }

    public void tick(Minecraft client) {

        if (registeredLevel != client.level) {
            registeredLevel = null;
            behavior.reset();
        }

        behavior.update(client);

        if (client.level != null && registeredLevel == null) {
            DynamicLightBehaviorManager behaviorManager = getBehaviorManager();

            if (behaviorManager != null) {
                behaviorManager.add(behavior);
                registeredLevel = client.level;
            }
        }
    }

    private static DynamicLightBehaviorManager getBehaviorManager() {
        try {
            Class<?> dynamicLightsClass = Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");
            Object instance = dynamicLightsClass.getMethod("get").invoke(null);

            return (DynamicLightBehaviorManager) dynamicLightsClass.getMethod("dynamicLightBehaviorManager")
                    .invoke(instance);
        } catch (ReflectiveOperationException exception) {
            Waylight.LOGGER.error("Failed to access LambDynamicLights behavior manager.", exception);
            return null;
        }
    }

    private static final class WaylightLanternBehavior implements DynamicLightBehavior {
        private final VirtualLanternController lanternController;
        private final LanternPoseController poseController;
        private final LanternRigResolver rigResolver;

        private final Vector3d position = new Vector3d();
        private final Vector3d previousPosition = new Vector3d(Double.NaN, Double.NaN, Double.NaN);

        private int luminance;
        private int previousLuminance = -1;

        private WaylightLanternBehavior(
                VirtualLanternController lanternController,
                LanternPoseController poseController,
                LanternRigResolver rigResolver) {
            this.lanternController = lanternController;
            this.poseController = poseController;
            this.rigResolver = rigResolver;
        }

        void reset() {
            position.zero();
            previousPosition.set(Double.NaN, Double.NaN, Double.NaN);
            luminance = 0;
            previousLuminance = -1;
        }

        void update(Minecraft client) {
            LocalPlayer player = client.player;
            if (player == null || client.level == null) {
                luminance = 0;
                return;
            }

            VirtualLanternState state = lanternController.getState();
            luminance = state.lightActive() ? LUMINANCE : 0;
            if (luminance <= 0) {
                return;
            }

            LanternRigResolver.Transform transform = rigResolver.resolve(state, poseController.getPoseState());

            rigResolver.resolveWorldLightCore(player, transform, position);
        }

        @Override
        public double lightAtPos(@NonNull BlockPos pos, double falloffRatio) {
            if (luminance <= 0) {
                return 0.0;
            }

            double dx = pos.getX() + 0.5 - position.x();
            double dy = pos.getY() + 0.5 - position.y();
            double dz = pos.getZ() + 0.5 - position.z();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            return Math.max(luminance - distance * falloffRatio, 0.0);
        }

        @Override
        public @NonNull BoundingBox getBoundingBox() {
            int x = (int) Math.floor(position.x());
            int y = (int) Math.floor(position.y());
            int z = (int) Math.floor(position.z());

            return new BoundingBox(x, y, z, x + 1, y + 1, z + 1);
        }

        @Override
        public boolean hasChanged() {
            boolean changed = !position.equals(previousPosition) || luminance != previousLuminance;
            if (changed) {
                previousPosition.set(position);
                previousLuminance = luminance;
            }

            return changed;
        }
    }
}
