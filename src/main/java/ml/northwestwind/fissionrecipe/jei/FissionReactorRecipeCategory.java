package ml.northwestwind.fissionrecipe.jei;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;

public class FissionReactorRecipeCategory extends BaseRecipeCategory<FissionRecipe> {
    private static final ResourceLocation iconRL;

    public FissionReactorRecipeCategory(IGuiHelper helper) {
        super(helper, GeneratorsBlocks.FISSION_REACTOR_CASING.getRegistryName(), GeneratorsLang.FISSION_REACTOR.translate(new Object[0]), 3, 12, 189, 70);
        this.icon = helper.drawableBuilder(iconRL, 0, 0, 18, 18).setTextureSize(18, 18).build();
    }

    protected void addGuiElements() {
        this.guiElements.add(new GuiInnerScreen(this, 45, 17, 105, 56));
        this.guiElements.add(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA, new Object[0])));
        this.guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13).setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN, new Object[0])));
        this.guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 152, 13).setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.GRAY, new Object[0])));
        this.guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 171, 13).setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN, new Object[0])));
    }

    private List<FluidStack> getWaterInput() {
        List<FluidStack> representations = new ArrayList();
        Iterator var2 = FluidTags.WATER.getValues().iterator();

        while(var2.hasNext()) {
            Fluid fluid = (Fluid)var2.next();
            representations.add(new FluidStack(fluid, 1000));
        }

        return representations;
    }

    @Nonnull
    public Class<? extends FissionRecipe> getRecipeClass() {
        return FissionRecipe.class;
    }

    public void setIngredients(FissionRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(this.getWaterInput()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(((GasStackIngredient)recipe.getInput()).getRepresentations()));
        ingredients.setOutputs(MekanismJEI.TYPE_GAS, Arrays.asList(MekanismGases.STEAM.getStack(1000L), (GasStack)recipe.getOutputRepresentation()));
    }

    public void setRecipe(IRecipeLayout recipeLayout, FissionRecipe recipe, @Nonnull IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        fluidStacks.init(0, true, 7 - this.xOffset, 14 - this.yOffset, 16, 58, 1, false, this.fluidOverlayLarge);
        fluidStacks.set(0, this.getWaterInput());
        this.initChemical(gasStacks, 0, true, 26 - this.xOffset, 14 - this.yOffset, 16, 58, ((GasStackIngredient)recipe.getInput()).getRepresentations(), true);
        this.initChemical(gasStacks, 1, false, 153 - this.xOffset, 14 - this.yOffset, 16, 58, Collections.singletonList(MekanismGases.STEAM.getStack(1000L)), true);
        this.initChemical(gasStacks, 2, false, 172 - this.xOffset, 14 - this.yOffset, 16, 58, Collections.singletonList(recipe.getOutputRepresentation()), true);
    }

    static {
        iconRL = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "radioactive.png");
    }
}