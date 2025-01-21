package in.northwestw.fissionrecipe.jei;

import mekanism.client.recipe_viewer.recipe.BoilerRecipeViewerRecipe;
import mekanism.client.recipe_viewer.type.FakeRVRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlocks;

import java.util.List;

public class RecipeViewerRecipeTypes {
    public static final FakeRVRecipeType<FissionJEIRecipe> FISSION = new FakeRVRecipeType<>(
            Mekanism.rl("fission"),
            MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "radioactive.png"),
            null,
            GeneratorsLang.FISSION_REACTOR,
            FissionJEIRecipe.class,
            -6, -13, 182, 60,
            List.of(GeneratorsBlocks.FISSION_REACTOR_CASING, GeneratorsBlocks.FISSION_REACTOR_PORT, GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, GeneratorsBlocks.CONTROL_ROD_ASSEMBLY)
    );

    public static final FakeRVRecipeType<BoilerJEIRecipe> BOILER = new FakeRVRecipeType<>(
            Mekanism.rl("boiler"),
            MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "heat.png"),
            MekanismLang.BOILER,
            BoilerJEIRecipe.class,
            -6, -13, 180, 60,
            MekanismBlocks.BOILER_CASING, MekanismBlocks.BOILER_VALVE, MekanismBlocks.PRESSURE_DISPERSER, MekanismBlocks.SUPERHEATING_ELEMENT
    );
}
