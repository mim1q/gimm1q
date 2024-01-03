package dev.mim1q.testmod;

import dev.mim1q.testmod.block.ThumperBlock;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestMod implements ModInitializer {
    public static final String ID = "testmod";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    private static final ThumperBlock THUMPER_BLOCK = registerBlock("thumper", new ThumperBlock(FabricBlockSettings.copyOf(Blocks.STONE)));

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }

    private static <B extends Block> B registerBlock(String name, B block) {
        var registeredBlock = Registry.register(Registries.BLOCK, id(name), block);
        Registry.register(Registries.ITEM, id(name), new BlockItem(registeredBlock, new FabricItemSettings()));
        return registeredBlock;
    }
}