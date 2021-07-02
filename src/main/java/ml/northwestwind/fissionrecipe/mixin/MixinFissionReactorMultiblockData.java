package ml.northwestwind.fissionrecipe.mixin;

import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.inventory.AutomationType;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.registries.MekanismGases;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.RecipeStorage;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Mixin(value = FissionReactorMultiblockData.class, remap = false)
public abstract class MixinFissionReactorMultiblockData extends MixinMultiblockData {
    @Shadow
    public IGasTank fuelTank;
    @Shadow public int fuelAssemblies;
    @Shadow public double burnRemaining;
    @Shadow public double rateLimit;
    @Shadow public MultiblockHeatCapacitor<FissionReactorMultiblockData> heatCapacitor;
    @Shadow public double partialWaste;
    @Shadow public IGasTank wasteTank;
    @Shadow public double lastBurnRate;
    @Shadow public IGasTank heatedCoolantTank;
    @Shadow public IGasTank gasCoolantTank;

    @Inject(at = @At("RETURN"), method = "<init>")
    public void construct(TileEntityFissionReactorCasing tile, CallbackInfo ci) {
        if (tile.getLevel() == null) return;
        IRecipeType<FissionRecipe> type = (IRecipeType<FissionRecipe>) Registry.RECIPE_TYPE.get(FissionRecipe.RECIPE_TYPE_ID);
        if (type == null) return;
        List<FissionRecipe> recipes = tile.getLevel().isClientSide ? RecipeStorage.getFissionRecipes() : tile.getLevel().getServer().getRecipeManager().getAllRecipesFor(type);
        this.fuelTank = MultiblockChemicalTankBuilder.GAS.create((FissionReactorMultiblockData) (Object) this, tile,
                () -> (long)this.fuelAssemblies * 8000L,
                (stack, automationType) -> automationType != AutomationType.EXTERNAL,
                (stack, automationType) -> this.isFormed(),
                (gas) -> recipes.stream().anyMatch(recipe -> recipe.getInput().testType(gas)),
                ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        this.gasTanks.clear();
        this.gasTanks.addAll(Arrays.asList(this.fuelTank, this.heatedCoolantTank, this.wasteTank, this.gasCoolantTank));
    }

    /**
     * @author Mekanism
     * @reason To handle custom fissile fuel
     */
    @Overwrite
    public void burnFuel(World world) {
        IRecipeType<FissionRecipe> type = (IRecipeType<FissionRecipe>) Registry.RECIPE_TYPE.get(FissionRecipe.RECIPE_TYPE_ID);
        if (type == null) return;
        List<FissionRecipe> recipes = this.getWorld().isClientSide ? RecipeStorage.getFissionRecipes() : this.getWorld().getServer().getRecipeManager().getAllRecipesFor(type);
        Optional<FissionRecipe> recipe = recipes.stream().filter(r -> r.getInput().testType(fuelTank.getType())).findFirst();
        if (!recipe.isPresent()) return;
        if (!wasteTank.isEmpty() && !wasteTank.isTypeEqual(recipe.get().getOutputRepresentation().getType())) return;
        double storedFuel = fuelTank.getStored() + this.burnRemaining;
        double toBurn = Math.min(Math.min(this.rateLimit, storedFuel), fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get());
        storedFuel -= toBurn;
        fuelTank.setStackSize((long) storedFuel, Action.EXECUTE);
        burnRemaining = storedFuel % 1;
        this.heatCapacitor.handleHeat(toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue() * recipe.get().getHeat());
        this.partialWaste += toBurn * recipe.get().getOutput(null).getAmount();
        long newWaste = (long) Math.floor(partialWaste);
        if (newWaste > 0) {
            partialWaste %= 1;
            long leftoverWaste = Math.max(0, newWaste - this.wasteTank.getNeeded());
            GasStack wasteToAdd = recipe.get().getOutput(null);
            wasteToAdd.setAmount(newWaste);
            wasteTank.insert(wasteToAdd, Action.EXECUTE, AutomationType.INTERNAL);
            if (leftoverWaste > 0) {
                double radioactivity = wasteToAdd.getType().get(GasAttributes.Radiation.class).getRadioactivity();
                Mekanism.radiationManager.radiate(new Coord4D(this.getBounds().getCenter(), world), leftoverWaste * radioactivity);
            }
        }
        this.lastBurnRate = toBurn;
    }
}