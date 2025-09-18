package net.skillz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.loader.SimpleDataLoader;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.data.populate.Populator;
import net.skillz.init.ConfigInit;
import net.skillz.init.LoaderInit;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.skillz.SkillZMain.LOGGER;

public class PopulateLoader extends SimpleDataLoader {

    public PopulateLoader() {
        super(SkillZMain.identifierOf("populate_loader"), "population");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.singleton(RestrictionLoader.ID);
    }

    @Override
    public void preReload() {
        LoaderInit.POPULATORS.clear();
        LoaderInit.itemsForRePopulation.clear();
        LoaderInit.blockForRePopulation.clear();
        LoaderInit.blockForRePopulation2.clear();
    }

    @Override
    public void reloadResource(JsonObject data, Identifier id, String fileName) {
        if (OptionalObject.get(data, "default", false).getAsBoolean() && !ConfigInit.MAIN.PROGRESSION.defaultPopulations) {
            return;
        }

        JsonObject popObj = data.get("populator").getAsJsonObject();
        Identifier populatorType = Identifier.tryParse(popObj.get("type").getAsString());
        Populator populator = LoaderInit.populateCreator.create(populatorType, popObj);

        OptionalObject.get(popObj, "id_blacklist", new JsonArray()).getAsJsonArray().forEach((elem) -> {
            populator.addToIdBlacklist(Identifier.tryParse(elem.getAsString()));
        });

        JsonArray skillObj = data.get("skills").getAsJsonArray();
        populator.populate(skillObj);

        LoaderInit.POPULATORS.add(populator);
    }

    @Override
    public void postReload() {

    }
}
