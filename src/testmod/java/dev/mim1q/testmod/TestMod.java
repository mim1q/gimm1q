package dev.mim1q.testmod;

import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.mim1q.gimm1q.effect.ExtendedStatusEffect;
import dev.mim1q.gimm1q.screenshake.ScreenShakeModifiers;
import dev.mim1q.testmod.block.EasingTesterBlock;
import dev.mim1q.testmod.block.EasingTesterBlockEntity;
import dev.mim1q.testmod.block.ThumperBlock;
import dev.mim1q.testmod.item.OverlayTesterItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TestMod implements ModInitializer {
    public static final String ID = "testmod";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final ThumperBlock THUMPER_BLOCK = registerBlock("thumper", new ThumperBlock(FabricBlockSettings.copyOf(Blocks.STONE)));
    public static final Item HIGHLIGHT_STICK = registerItem("highlight_stick", new Item(new FabricItemSettings()));
    public static final Block EASING_TESTER = registerBlock("easing_tester", new EasingTesterBlock(FabricBlockSettings.copyOf(Blocks.STONE)));
    public static final BlockEntityType<EasingTesterBlockEntity> EASING_TESTER_BE = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        id("easing_tester"),
        BlockEntityType.Builder.create(EasingTesterBlockEntity::new, EASING_TESTER).build(null)
    );
    public static final OverlayTesterItem OVERLAY_TESTER = registerItem("overlay_tester", new OverlayTesterItem(new FabricItemSettings()));

    private static class PossiblyIncurableStatusEffect extends StatusEffect implements ExtendedStatusEffect {
        private final boolean curable;

        protected PossiblyIncurableStatusEffect(boolean curable) {
            super(StatusEffectCategory.NEUTRAL, 0x000000);
            this.curable = curable;
        }

        @Override
        public boolean canBeCuredWithMilk() {
            return curable;
        }
    }

    public static final StatusEffect INCURABLE_EFFECT = Registry.register(
        Registries.STATUS_EFFECT, id("incurable"), new PossiblyIncurableStatusEffect(false)
    );

    public static final StatusEffect CURABLE_EFFECT = Registry.register(
        Registries.STATUS_EFFECT, id("curable"), new PossiblyIncurableStatusEffect(true)
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        CommandRegistrationCallback.EVENT.register((listener, registry, environment) -> {
            listener.register(literal("modify_screenshake")
                .then(argument("value", FloatArgumentType.floatArg())
                    .executes(context -> {
                        float value = FloatArgumentType.getFloat(context, "value");
                        ScreenShakeModifiers.setModifier("testmod", value);
                        return 0;
                    })
                )
            );
        });

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, items) -> {
            if (group == Registries.ITEM_GROUP.getOrThrow(ItemGroups.TOOLS)) {
                items.add(HIGHLIGHT_STICK);
                items.add(THUMPER_BLOCK);
                items.add(EASING_TESTER);
            }
        });
    }

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }

    private static <B extends Block> B registerBlock(String name, B block) {
        var registeredBlock = Registry.register(Registries.BLOCK, id(name), block);
        Registry.register(Registries.ITEM, id(name), new BlockItem(registeredBlock, new FabricItemSettings()));
        return registeredBlock;
    }

    private static <I extends Item> I registerItem(String name, I item) {
        return Registry.register(Registries.ITEM, id(name), item);
    }
}