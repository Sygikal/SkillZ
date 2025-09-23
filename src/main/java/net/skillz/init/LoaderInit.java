package net.skillz.init;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.util.Creator;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.data.SkillLoader;
import net.skillz.data.RestrictionLoader;
import net.skillz.data.PopulateLoader;
import net.skillz.data.populate.Populator;
import net.skillz.data.populate.impl.*;
import net.skillz.level.LevelManager;
import net.skillz.util.PacketHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaderInit {

    public static final Creator<JsonObject, Populator> populateCreator = new Creator<>();

    public static final List<Populator> POPULATORS = Lists.newArrayList();

    public static final Map<String, Pair<List<Pair<String, Identifier>>, List<Item>>> itemsForRePopulation = new HashMap<>();
    public static final Map<Block, List<Pair<String, Integer>>> blockForRePopulation = new HashMap<>();
    public static final Map<Block, List<Pair<String, Integer>>> blockForRePopulation2 = new HashMap<>();



    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SkillLoader());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new RestrictionLoader());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PopulateLoader());

        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer -> {
            POPULATORS.forEach(Populator::postPopulate);
        }));

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {

                for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
                    ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayerList().get(i);
                    PacketHelper.updateSkills(serverPlayerEntity);
                    PacketHelper.updatePlayerSkills(serverPlayerEntity, null);
                }

                POPULATORS.forEach(Populator::postPopulate);

                SkillZMain.LOGGER.info("Finished reload on {}", Thread.currentThread());
            } else {
                SkillZMain.LOGGER.error("Failed to reload on {}", Thread.currentThread());
            }
        });

        populateCreator.registerCreator(ArmorMaterialPopulator.ID, data ->
                new ArmorMaterialPopulator(
                        OptionalObject.get(data, "whitelist", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "blacklist", new JsonArray()).getAsJsonArray()));

        populateCreator.registerCreator(ToolMaterialPopulator.ID, data ->
                new ToolMaterialPopulator(
                        ToolMaterialPopulator.ToolSubType.valueOf(data.get("tool_type").getAsString().toUpperCase())));

        populateCreator.registerCreator(SkillZMain.identifierOf("mining_populator"), data ->
                new BlockPopulator(SkillZMain.identifierOf("mining_populator"), LevelManager.MINING_RESTRICTIONS, LoaderInit.blockForRePopulation,
                        OptionalObject.get(data, "hidden", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "additional", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "filters", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "tags", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "tag_blacklist", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "min", 0.0).getAsFloat(),
                        OptionalObject.get(data, "max", 50.0).getAsFloat()));

        populateCreator.registerCreator(SkillZMain.identifierOf("block_usage_populator"), data ->
                new BlockPopulator(SkillZMain.identifierOf("block_usage_populator"), LevelManager.BLOCK_RESTRICTIONS, LoaderInit.blockForRePopulation2,
                        OptionalObject.get(data, "hidden", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "additional", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "filters", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "tags", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "tag_blacklist", new JsonArray()).getAsJsonArray(),
                        OptionalObject.get(data, "min", 0.0).getAsFloat(),
                        OptionalObject.get(data, "max", 50.0).getAsFloat()));

        populateCreator.registerCreator(EnchantmentPopulator.ID, data ->
                new EnchantmentPopulator(
                        OptionalObject.get(data, "cursed", false).getAsBoolean(),
                        OptionalObject.get(data, "treasure", true).getAsBoolean()));
    }

}
