package com.soradotwav.waylight.lantern;

public record VirtualLanternState(
        boolean enabled,
        LanternType lanternType,
        LanternPosition lanternPosition,
        boolean lightActive,
        boolean modelVisible,
        boolean temporarilySuppressed,
        boolean underwaterExtinguished) {
}
