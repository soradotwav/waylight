package com.soradotwav.waylight;

import com.soradotwav.waylight.config.WaylightConfigManager;
import com.soradotwav.waylight.integration.LambDynamicLightsAdapter;
import com.soradotwav.waylight.lantern.VirtualLanternController;
import com.soradotwav.waylight.render.LanternPoseController;
import com.soradotwav.waylight.render.LanternRigResolver;

public final class WaylightRuntime {
    private final WaylightConfigManager configManager = new WaylightConfigManager();

    private final LanternRigResolver rigResolver = new LanternRigResolver();
    private final VirtualLanternController lanternController = new VirtualLanternController(configManager);
    private final LanternPoseController poseController = new LanternPoseController(configManager);

    private final LambDynamicLightsAdapter dynamicLightsAdapter = new LambDynamicLightsAdapter(lanternController,
            poseController, rigResolver);

    public WaylightConfigManager configManager() {
        return configManager;
    }

    public LanternRigResolver rigResolver() {
        return rigResolver;
    }

    public VirtualLanternController lanternController() {
        return lanternController;
    }

    public LanternPoseController poseController() {
        return poseController;
    }

    public LambDynamicLightsAdapter dynamicLightsAdapter() {
        return dynamicLightsAdapter;
    }
}
