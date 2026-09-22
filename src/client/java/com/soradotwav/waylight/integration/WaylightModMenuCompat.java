package com.soradotwav.waylight.integration;

import com.soradotwav.WaylightClient;
import com.soradotwav.waylight.config.WaylightConfig;
import com.soradotwav.waylight.config.WaylightConfigManager;
import com.soradotwav.waylight.lantern.LanternPosition;
import com.soradotwav.waylight.lantern.LanternType;
import com.soradotwav.waylight.render.FirstPersonHandMotionMode;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class WaylightModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            WaylightConfigManager configManager = WaylightClient.runtime().configManager();
            WaylightConfig config = configManager.get();

            return YetAnotherConfigLib.createBuilder()
                    .title(Component.translatable("waylight.config.title"))
                    .category(ConfigCategory.createBuilder()
                            .name(Component.translatable("waylight.config.category.general"))
                            .group(buildLanternGroup(configManager, config))
                            .group(buildBehaviorGroup(configManager, config))
                            .group(buildDebugGroup(configManager, config))
                            .build())
                    .save(configManager::save)
                    .build()
                    .generateScreen(parent);
        };
    }

    private static OptionGroup buildLanternGroup(WaylightConfigManager configManager, WaylightConfig config) {
        return OptionGroup.createBuilder()
                .name(Component.translatable("waylight.config.group.lantern"))
                .collapsed(false)
                .option(Option.<LanternType>createBuilder()
                        .name(Component.translatable("waylight.config.option.lantern_type"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.lantern_type.desc")))
                        .binding(
                                config.lanternType,
                                () -> configManager.get().lanternType,
                                value -> configManager.update(cfg -> cfg.lanternType = value))
                        .controller(
                                option -> EnumControllerBuilder.create(option).enumClass(LanternType.class))
                        .build())
                .option(Option.<LanternPosition>createBuilder()
                        .name(Component.translatable("waylight.config.option.lantern_position"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.lantern_position.desc")))
                        .binding(
                                config.lanternPosition,
                                () -> configManager.get().lanternPosition,
                                value -> configManager.update(cfg -> cfg.lanternPosition = value))
                        .controller(
                                option -> EnumControllerBuilder.create(option).enumClass(LanternPosition.class))
                        .build())
                .build();
    }

    private static OptionGroup buildBehaviorGroup(WaylightConfigManager configManager, WaylightConfig config) {
        return OptionGroup.createBuilder()
                .name(Component.translatable("waylight.config.group.behavior"))
                .collapsed(false)
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("waylight.config.option.first_person_light"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.first_person_light.desc")))
                        .binding(
                                config.firstPersonLight,
                                () -> configManager.get().firstPersonLight,
                                value -> configManager.update(cfg -> cfg.firstPersonLight = value))
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("waylight.config.option.extinguish_underwater"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.extinguish_underwater.desc")))
                        .binding(
                                config.extinguishUnderwater,
                                () -> configManager.get().extinguishUnderwater,
                                value -> configManager.update(cfg -> cfg.extinguishUnderwater = value))
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("waylight.config.option.auto_equip_in_darkness"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.auto_equip_in_darkness.desc")))
                        .binding(
                                config.autoEquipInDarkness,
                                () -> configManager.get().autoEquipInDarkness,
                                value -> configManager.update(cfg -> cfg.autoEquipInDarkness = value))
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("waylight.config.option.auto_unequip_in_brightness"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.auto_unequip_in_brightness.desc")))
                        .binding(
                                config.autoUnequipInBrightness,
                                () -> configManager.get().autoUnequipInBrightness,
                                value -> configManager.update(cfg -> cfg.autoUnequipInBrightness = value))
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(Component.translatable("waylight.config.option.auto_light_threshold"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.auto_light_threshold.desc")))
                        .binding(
                                config.autoLightThreshold,
                                () -> configManager.get().autoLightThreshold,
                                value -> configManager.update(cfg -> cfg.autoLightThreshold = value))
                        .controller(option -> IntegerSliderControllerBuilder.create(option)
                                .range(0, 15)
                                .step(1)
                                .formatValue(value -> Component.literal(Integer.toString(value))
                                        .withStyle(value == 7 ? ChatFormatting.GREEN : ChatFormatting.WHITE)))
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(Component.translatable("waylight.config.option.motion_intensity"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.motion_intensity.desc")))
                        .binding(
                                config.motionIntensity,
                                () -> configManager.get().motionIntensity,
                                value -> configManager.update(cfg -> cfg.motionIntensity = value))
                        .controller(option -> IntegerSliderControllerBuilder.create(option)
                                .range(25, 200)
                                .step(5)
                                .formatValue(value -> Component.literal(value + "%")
                                        .withStyle(value == 100 ? ChatFormatting.GREEN : ChatFormatting.WHITE)))
                        .build())
                .option(Option.<FirstPersonHandMotionMode>createBuilder()
                        .name(Component.translatable("waylight.config.option.first_person_hand_motion"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.first_person_hand_motion.desc")))
                        .binding(
                                config.firstPersonHandMotion,
                                () -> configManager.get().firstPersonHandMotion,
                                value -> configManager.update(cfg -> cfg.firstPersonHandMotion = value))
                        .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(FirstPersonHandMotionMode.class))
                        .build())
                .build();
    }

    private static OptionGroup buildDebugGroup(WaylightConfigManager configManager, WaylightConfig config) {
        return OptionGroup.createBuilder()
                .name(Component.translatable("waylight.config.group.debug"))
                .collapsed(true)
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("waylight.config.option.debug_anchor_gizmo"))
                        .description(OptionDescription.of(
                                Component.translatable("waylight.config.option.debug_anchor_gizmo.desc")))
                        .binding(
                                config.debugAnchorGizmo,
                                () -> configManager.get().debugAnchorGizmo,
                                value -> configManager.update(cfg -> cfg.debugAnchorGizmo = value))
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build();
    }
}
