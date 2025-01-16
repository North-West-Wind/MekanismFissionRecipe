package in.northwestw.fissionrecipe.mixin;

import in.northwestw.fissionrecipe.MekanismFission;
import in.northwestw.fissionrecipe.recipe.FissionRecipe;
import in.northwestw.fissionrecipe.recipe.FluidCoolantRecipe;
import in.northwestw.fissionrecipe.recipe.GasCoolantRecipe;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

@Mixin(FissionReactorMultiblockData.class)
public abstract class MixinFissionReactorMultiblockData extends MixinMultiblockData {
    // these are cached recipes
    @Unique
    private FissionRecipe fissionRecipe = null;
    @Unique
    private FluidCoolantRecipe fluidCoolantRecipe = null;
    @Unique
    private GasCoolantRecipe gasCoolantRecipe = null;

    @Shadow @Final public VariableHeatCapacitor heatCapacitor;

    @Shadow public abstract double getBoilEfficiency();

    @Shadow @Final public MergedTank coolantTank;

    @Shadow public long lastBoilRate;

    @Shadow protected abstract long clampCoolantHeated(double heated, long stored);

    @Shadow @Final public IChemicalTank heatedCoolantTank;

    @Shadow @Final public IChemicalTank fuelTank;

    @Shadow public double partialWaste;

    @Shadow public double burnRemaining;

    @Shadow @Final public IChemicalTank wasteTank;

    @Shadow public double rateLimit;

    @Shadow private int fuelAssemblies;

    @Shadow public double lastBurnRate;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/fluid/VariableCapacityFluidTank;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/IntSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/common/capabilities/fluid/VariableCapacityFluidTank;", ordinal = 0))
    public VariableCapacityFluidTank customFluidCoolantTank(MultiblockData multiblock, IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return VariableCapacityFluidTank.input(multiblock, capacity, validator, listener);
        List<FluidCoolantRecipe> recipes = serverFluidCoolantRecipes(server);
        return VariableCapacityFluidTank.input(multiblock, capacity, fluid -> recipes.stream().anyMatch(r -> r.test(fluid)), listener);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0))
    public IChemicalTank customGasCoolantTank(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return VariableCapacityChemicalTank.input(multiblock, capacity, validator, listener);
        List<GasCoolantRecipe> recipes = serverGasCoolantRecipes(server);
        return VariableCapacityChemicalTank.input(multiblock, capacity, gas -> recipes.stream().anyMatch(r -> r.getInput().testType(gas)), listener);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/chemical/attribute/ChemicalAttributeValidator;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0))
    public IChemicalTank customFuelTank(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return VariableCapacityChemicalTank.input(multiblock, capacity, validator, attributeValidator, listener);
        List<FissionRecipe> recipes = serverFissionRecipes(server);
        return VariableCapacityChemicalTank.input(multiblock, capacity, gas -> recipes.stream().anyMatch(r -> r.getInput().testType(gas)), attributeValidator, listener);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;output(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0))
    public IChemicalTank customHeatedCoolantTank(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        // just let there be anything. this is faster than actually checking when there are many recipes, and you can't manually pump chemicals into it anyway
        return VariableCapacityChemicalTank.output(multiblock, capacity, gas -> true, listener);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/VariableCapacityChemicalTank;output(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/chemical/attribute/ChemicalAttributeValidator;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0))
    public IChemicalTank customWasteTank(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        // same reason as heated coolant tank
        return VariableCapacityChemicalTank.output(multiblock, capacity, gas -> true, attributeValidator, listener);
    }

    /**
     * @author Mekanism
     * @reason To handle custom coolant
     */
    @Overwrite
    private void handleCoolant() {
        double temp = heatCapacitor.getTemperature();
        double heat = this.getBoilEfficiency() * (temp - HeatUtils.BASE_BOIL_TEMP) * heatCapacitor.getHeatCapacity();

        switch (coolantTank.getCurrentType()) {
            case EMPTY -> lastBoilRate = 0;
            case FLUID -> {
                IExtendedFluidTank fluidCoolantTank = this.coolantTank.getFluidTank();
                if (fluidCoolantRecipe == null) {
                    fluidCoolantRecipe = serverFluidCoolantRecipes(this.getLevel().getServer()).stream().filter(r -> r.getInput().testType(fluidCoolantTank.getFluid())).findFirst().orElse(null);
                    if (fluidCoolantRecipe == null) {
                        lastBoilRate = 0;
                        return;
                    }
                }
                double caseCoolantHeat = heat * fluidCoolantRecipe.getConductivity();
                lastBoilRate = this.clampCoolantHeated(fluidCoolantRecipe.getEfficiency() * caseCoolantHeat / fluidCoolantRecipe.getThermalEnthalpy(),
                        fluidCoolantTank.getFluidAmount());
                if (lastBoilRate > 0) {
                    MekanismUtils.logMismatchedStackSize(fluidCoolantTank.shrinkStack((int) lastBoilRate, Action.EXECUTE), lastBoilRate);
                    // extra steam is dumped
                    heatedCoolantTank.insert(fluidCoolantRecipe.getOutputRepresentation().copyWithAmount(lastBoilRate * fluidCoolantRecipe.getOutputRepresentation().getAmount() / fluidCoolantRecipe.getInput().getRepresentations().get(0).getAmount()), Action.EXECUTE, AutomationType.INTERNAL);
                    caseCoolantHeat = lastBoilRate * fluidCoolantRecipe.getThermalEnthalpy() / fluidCoolantRecipe.getEfficiency();
                    heatCapacitor.handleHeat(-caseCoolantHeat);
                } else {
                    lastBoilRate = 0;
                }
            }
            case CHEMICAL -> {
                IChemicalTank chemicalCoolantTank = coolantTank.getChemicalTank();
                if (gasCoolantRecipe == null) {
                    gasCoolantRecipe = serverGasCoolantRecipes(this.getLevel().getServer()).stream().filter(r -> r.getInput().testType(chemicalCoolantTank.getStack())).findFirst().orElse(null);
                    if (gasCoolantRecipe == null) {
                        lastBoilRate = 0;
                        return;
                    }
                }
                double caseCoolantHeat = heat * gasCoolantRecipe.getConductivity();
                lastBoilRate = clampCoolantHeated(caseCoolantHeat / gasCoolantRecipe.getThermalEnthalpy(), chemicalCoolantTank.getStored());
                if (lastBoilRate > 0) {
                    MekanismUtils.logMismatchedStackSize(chemicalCoolantTank.shrinkStack(lastBoilRate, Action.EXECUTE), lastBoilRate);
                    ChemicalStack output = gasCoolantRecipe.getOutput(chemicalCoolantTank.getStack());
                    output.setAmount(lastBoilRate * output.getAmount() / gasCoolantRecipe.getInput().getRepresentations().get(0).getAmount());
                    heatedCoolantTank.insert(output, Action.EXECUTE, AutomationType.INTERNAL);
                    caseCoolantHeat = lastBoilRate * gasCoolantRecipe.getThermalEnthalpy();
                    heatCapacitor.handleHeat(-caseCoolantHeat);
                }
            }
        }
    }

    /**
     * @author Mekanism
     * @reason To handle custom fissile fuel
     */
    @Overwrite
    private void burnFuel(Level world) {
        // get fission recipe by input
        if (fissionRecipe == null) {
            fissionRecipe = serverFissionRecipes(this.getLevel().getServer()).stream().filter(r -> r.getInput().testType(this.fuelTank.getType())).findFirst().orElse(null);
            if (fissionRecipe == null) return;
        }

        // check if waste tank matches
        if (!wasteTank.isEmpty() && !wasteTank.isTypeEqual(this.fissionRecipe.getOutputRaw().getChemical())) return;

        double lastPartialWaste = partialWaste;
        double lastBurnRemaining = burnRemaining;
        double storedFuel = fuelTank.getStored() + burnRemaining;
        double toBurn = Math.min(Math.min(rateLimit, storedFuel), fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get());
        storedFuel -= toBurn;
        fuelTank.setStackSize((long) storedFuel, Action.EXECUTE);
        burnRemaining = storedFuel % 1;
        heatCapacitor.handleHeat(toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get() * this.fissionRecipe.getHeat(toBurn));
        // handle waste
        partialWaste += toBurn * this.fissionRecipe.getOutputRaw().getAmount();
        long newWaste = Mth.lfloor(partialWaste) * this.fissionRecipe.getOutputRaw().getAmount() / this.fissionRecipe.getInput().amount();
        if (newWaste > 0) {
            partialWaste %= 1;
            long leftoverWaste = Math.max(0, newWaste - wasteTank.getNeeded());
            ChemicalStack wasteToAdd = fissionRecipe.getOutputRaw().copyWithAmount(newWaste);
            this.wasteTank.insert(wasteToAdd, Action.EXECUTE, AutomationType.INTERNAL);
            if (leftoverWaste > 0 && IRadiationManager.INSTANCE.isRadiationEnabled()) {
                //Check if radiation is enabled in order to allow for short-circuiting when it will NO-OP further down the line anyway
                ChemicalAttributes.Radiation attribute = wasteToAdd.get(ChemicalAttributes.Radiation.class);
                if (attribute != null) {
                    IRadiationManager.INSTANCE.radiate(GlobalPos.of(world.dimension(), getBounds().getCenter()), leftoverWaste * attribute.getRadioactivity());
                }
            }
        }
        // update previous burn
        lastBurnRate = toBurn;
        if (lastPartialWaste != this.partialWaste || lastBurnRemaining != this.burnRemaining) {
            markDirty();
        }
    }

    @Unique
    private static List<FissionRecipe> serverFissionRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RecipeTypes.FISSION.get()).stream().map(holder -> (FissionRecipe) holder.value()).toList();
    }

    @Unique
    private static List<FluidCoolantRecipe> serverFluidCoolantRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RecipeTypes.FLUID_COOLANT.get()).stream().map(RecipeHolder::value).toList();
    }

    @Unique
    private static List<GasCoolantRecipe> serverGasCoolantRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RecipeTypes.GAS_COOLANT.get()).stream().map(holder -> (GasCoolantRecipe) holder.value()).toList();
    }
}
