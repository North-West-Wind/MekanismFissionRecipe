package ml.northwestwind.fissionrecipe.jei;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.heat.HeatAPI;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FissionReactorRecipeCategory extends BaseRecipeCategory<FissionReactorRecipeCategory.FissionJEIRecipe> {
    private static final ResourceLocation iconRL = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "radioactive.png");
    private final GuiGauge<?> coolantTank;
    private final GuiGauge<?> fuelTank;
    private final GuiGauge<?> heatedCoolantTank;
    private final GuiGauge<?> wasteTank;

    public FissionReactorRecipeCategory(IGuiHelper helper) {
        super(helper, GeneratorsBlocks.FISSION_REACTOR_CASING.getRegistryName(), GeneratorsLang.FISSION_REACTOR.translate(), createIcon(helper, iconRL), 6, 13, 182, 60);
        addElement(new GuiInnerScreen(this, 45, 17, 105, 56, () -> Arrays.asList(
                MekanismLang.STATUS.translate(EnumColor.BRIGHT_GREEN, BooleanStateDisplay.ActiveDisabled.of(true)),
                GeneratorsLang.GAS_BURN_RATE.translate(1.0),
                GeneratorsLang.FISSION_HEATING_RATE.translate(0),
                MekanismLang.TEMPERATURE.translate(EnumColor.BRIGHT_GREEN, MekanismUtils.getTemperatureDisplay(HeatAPI.AMBIENT_TEMP, UnitDisplayUtils.TemperatureUnit.KELVIN, true)),
                GeneratorsLang.FISSION_DAMAGE.translate(EnumColor.BRIGHT_GREEN, TextUtils.getPercent(0))
        )).spacing(2));
        coolantTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        fuelTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13).setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        heatedCoolantTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 152, 13).setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.GRAY)));
        wasteTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 171, 13).setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
    }

    public static List<FluidCoolantRecipe> getFluidCoolants() {
        return Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FLUID_COOLANT.getType());
    }

    public static List<GasCoolantRecipe> getGasCoolants() {
        return Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.GAS_COOLANT.getType());
    }

    @Nonnull
    public Class<? extends FissionJEIRecipe> getRecipeClass() {
        return FissionJEIRecipe.class;
    }

    public void setIngredients(FissionJEIRecipe recipe, IIngredients ingredients) {
        if (!recipe.isGasCoolant()) {
            ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidInputs()));
            ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        } else {
            ingredients.setInputLists(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.getGasInputs(), recipe.getInput().getRepresentations()));
        }
        ingredients.setOutputs(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.getOutput(), recipe.getOutputRepresentation()));
    }

    public void setRecipe(IRecipeLayout recipeLayout, FissionJEIRecipe recipe, @Nonnull IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        int chemicalTankIndex = 0;
        if (!recipe.isGasCoolant()) {
            initFluid(recipeLayout.getFluidStacks(), 0, true, coolantTank, recipe.getFluidInputs());
        } else {
            initChemical(gasStacks, chemicalTankIndex++, true, coolantTank, recipe.getGasInputs());
        }
        GasStack output = recipe.getOutput();
        initChemical(gasStacks, chemicalTankIndex++, true, fuelTank, recipe.getInput().getRepresentations());
        initChemical(gasStacks, chemicalTankIndex++, false, heatedCoolantTank, Collections.singletonList(output));
        initChemical(gasStacks, chemicalTankIndex, false, wasteTank, Collections.singletonList(recipe.getOutputRepresentation()));
    }

    public static class FissionJEIRecipe extends FissionRecipe {
        private final boolean gasCoolant;
        private final List<FluidStack> fluidInputs;
        private final List<GasStack> gasInputs;
        private final GasStack output;

        public FissionJEIRecipe(FissionRecipe recipe, GasStack output, List<FluidStack> inputs) {
            super(recipe.getId(), recipe.getInput(), recipe.getOutputRepresentation(), recipe.getHeatObject());
            this.gasCoolant = false;
            this.fluidInputs = inputs;
            this.gasInputs = null;
            this.output = output;
        }

        public FissionJEIRecipe(FissionRecipe recipe, List<GasStack> inputs, GasStack output) {
            super(recipe.getId(), recipe.getInput(), recipe.getOutputRepresentation(), recipe.getHeatObject());
            this.gasCoolant = true;
            this.fluidInputs = null;
            this.gasInputs = inputs;
            this.output = output;
        }

        public boolean isGasCoolant() {
            return this.gasCoolant;
        }

        public List<FluidStack> getFluidInputs() {
            return fluidInputs;
        }

        public List<GasStack> getGasInputs() {
            return gasInputs;
        }

        public GasStack getOutput() {
            return output;
        }
    }
}