package ml.northwestwind.fissionrecipe.mixin;

import mekanism.api.*;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

@Mixin(value = FissionReactorMultiblockData.class, remap = false)
public abstract class MixinFissionReactorMultiblockData extends MixinMultiblockData {
    private Optional<FissionRecipe> fissionRecipe = Optional.empty();
    private Optional<FluidCoolantRecipe> fluidCoolantRecipe = Optional.empty();
    private Optional<GasCoolantRecipe> gasCoolantRecipe = Optional.empty();

    @Shadow
    public IGasTank fuelTank;
    @Shadow
    public int fuelAssemblies;
    @Shadow
    public double burnRemaining;
    @Shadow
    public double rateLimit;
    @Shadow
    public VariableHeatCapacitor heatCapacitor;
    @Shadow
    public double partialWaste;
    @Shadow
    public IGasTank wasteTank;
    @Shadow
    public double lastBurnRate;

    @Shadow
    public abstract double getBoilEfficiency();

    @Shadow
    public VariableCapacityFluidTank fluidCoolantTank;
    @Shadow
    public IGasTank heatedCoolantTank;
    @Shadow
    public IGasTank gasCoolantTank;
    @Shadow
    public long lastBoilRate;

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/fluid/VariableCapacityFluidTank;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/IntSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/common/capabilities/fluid/VariableCapacityFluidTank;", ordinal = 0), method = "<init>")
    public VariableCapacityFluidTank customFluidCoolantTank(MultiblockData multiblock, IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return VariableCapacityFluidTank.input(multiblock, capacity, validator, listener);
        List<FluidCoolantRecipe> recipes = serverFluidCoolantRecipes(server);
        return VariableCapacityFluidTank.input(multiblock, capacity,
                (fluid) -> recipes.stream().anyMatch(recipe -> recipe.getInput().testType(fluid)) && gasCoolantTank.isEmpty(), listener);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0), method = "<init>")
    public <TANK extends IChemicalTank<Gas, GasStack>> TANK customGasCoolantTank(MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank> instance, MultiblockData multiblock, LongSupplier capacity, Predicate<Gas> validator, @Nullable IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return (TANK) instance.input(multiblock, capacity, validator, listener);
        List<GasCoolantRecipe> recipes = serverGasCoolantRecipes(server);
        return (TANK) instance.input(multiblock, capacity,
                (gas) -> recipes.stream().anyMatch(recipe -> recipe.getInput().testType((Gas) gas)) && fluidCoolantTank.isEmpty(), listener);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;output(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0), method = "<init>")
    public <TANK extends IChemicalTank<Gas, GasStack>> TANK customHeatedCoolantTank(MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank> instance, MultiblockData multiblock, LongSupplier capacity, Predicate<Gas> validator, @Nullable IContentsListener listener) {
        return (TANK) instance.output(multiblock, capacity, (gas) -> true, listener);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;input(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/chemical/attribute/ChemicalAttributeValidator;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0), method = "<init>")
    public <TANK extends IChemicalTank<Gas, GasStack>> TANK customFuelTank(MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank> instance, MultiblockData multiblock, LongSupplier capacity, Predicate<Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return (TANK) instance.input(multiblock, capacity, validator, attributeValidator, listener);
        List<FissionRecipe> recipes = serverFissionRecipes(server);
        return (TANK) instance.input(multiblock, capacity,
                (gas) -> recipes.stream().anyMatch(recipe -> recipe.getInput().testType(gas)),
                ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;output(Lmekanism/common/lib/multiblock/MultiblockData;Ljava/util/function/LongSupplier;Ljava/util/function/Predicate;Lmekanism/api/chemical/attribute/ChemicalAttributeValidator;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0), method = "<init>")
    public <TANK extends IChemicalTank<Gas, GasStack>> TANK customWasteTank(MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank> instance, MultiblockData multiblock, LongSupplier capacity, Predicate<Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        return (TANK) instance.output(multiblock, capacity, (gas) -> true, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /**
     * @author Mekanism
     * @reason To handle custom coolant
     */
    @Overwrite
    private void handleCoolant() {
        double temp = this.heatCapacitor.getTemperature();
        double heat = this.getBoilEfficiency() * (temp - HeatUtils.BASE_BOIL_TEMP) * this.heatCapacitor.getHeatCapacity();
        long coolantHeated = 0;

        if (!fluidCoolantTank.isEmpty()) {
            Optional<FluidCoolantRecipe> recipe;
            if (!fluidCoolantRecipe.isPresent())
                fluidCoolantRecipe = serverFluidCoolantRecipes(this.getWorld().getServer()).stream().filter(r -> r.getInput().testType(fluidCoolantTank.getFluid())).findFirst();
            recipe = fluidCoolantRecipe;
            if (!recipe.isPresent()) return;

            double caseCoolantHeat = heat * recipe.get().getConductivity();
            coolantHeated = (long) ((int) (recipe.get().getHeat(caseCoolantHeat) * caseCoolantHeat / recipe.get().getThermalEnthalpy()));
            coolantHeated = Mth.clamp(coolantHeated, 0, fluidCoolantTank.getFluidAmount());
            if (coolantHeated > 0L) {
                MekanismUtils.logMismatchedStackSize(this.fluidCoolantTank.shrinkStack((int) coolantHeated, Action.EXECUTE), coolantHeated);

                if (fluidCoolantTank.isEmpty()) fluidCoolantRecipe = Optional.empty();
                GasStack output = recipe.get().getOutputRepresentation();
                output.setAmount(coolantHeated);

                this.heatedCoolantTank.insert(output, Action.EXECUTE, AutomationType.INTERNAL);
                caseCoolantHeat = (double) coolantHeated * recipe.get().getThermalEnthalpy() / (recipe.get().getHeat(caseCoolantHeat));
                this.heatCapacitor.handleHeat(-caseCoolantHeat);
            }
        } else if (!this.gasCoolantTank.isEmpty()) {
            Optional<GasCoolantRecipe> recipe;
            if (!gasCoolantRecipe.isPresent())
                gasCoolantRecipe = serverGasCoolantRecipes(this.getWorld().getServer()).stream().filter(r -> r.getInput().testType(gasCoolantTank.getStack())).findFirst();
            recipe = gasCoolantRecipe;
            if (!recipe.isPresent()) return;

            double caseCoolantHeat = heat * recipe.get().getConductivity();
            coolantHeated = (long) ((int) (recipe.get().getHeat(caseCoolantHeat) / recipe.get().getThermalEnthalpy()));
            coolantHeated = Mth.clamp(coolantHeated, 0, gasCoolantTank.getStored());
            if (coolantHeated > 0L) {
                MekanismUtils.logMismatchedStackSize(this.gasCoolantTank.shrinkStack((int) coolantHeated, Action.EXECUTE), coolantHeated);
                if (gasCoolantTank.isEmpty()) gasCoolantRecipe = Optional.empty();
                GasStack output = recipe.get().getOutputRepresentation();
                output.setAmount(coolantHeated);
                this.heatedCoolantTank.insert(output, Action.EXECUTE, AutomationType.INTERNAL);
                caseCoolantHeat = (double) coolantHeated * recipe.get().getThermalEnthalpy();
                this.heatCapacitor.handleHeat(-caseCoolantHeat);
            }
        }

        this.lastBoilRate = coolantHeated;
    }

    /**
     * @author Mekanism
     * @reason To handle custom fissile fuel
     */
    @Overwrite
    private void burnFuel(Level world) {
        Optional<FissionRecipe> recipe;
        if (!fissionRecipe.isPresent()) fissionRecipe = serverFissionRecipes(this.getWorld().getServer()).stream().filter(r -> r.getInput().testType(fuelTank.getType())).findFirst();
        recipe = fissionRecipe;
        if (!recipe.isPresent()) return;
        if (!wasteTank.isEmpty() && !wasteTank.isTypeEqual(recipe.get().getOutputRepresentation().getType())) return;

        double lastPartialWaste = partialWaste;
        double lastBurnRemaining = burnRemaining;
        double storedFuel = fuelTank.getStored() + this.burnRemaining;
        double toBurn = Math.min(Math.min(this.rateLimit, storedFuel), fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get());
        storedFuel -= toBurn;
        fuelTank.setStackSize((long) storedFuel, Action.EXECUTE);

        if (fuelTank.isEmpty()) fissionRecipe = Optional.empty();

        burnRemaining = storedFuel % 1;
        this.heatCapacitor.handleHeat(MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue() * recipe.get().getHeat(toBurn));
        this.partialWaste += toBurn * recipe.get().getOutputRepresentation().getAmount();
        long newWaste = (long) Math.floor(partialWaste);
        if (newWaste > 0) {
            partialWaste %= 1;
            long leftoverWaste = Math.max(0, newWaste - this.wasteTank.getNeeded());
            GasStack wasteToAdd = recipe.get().getOutputRepresentation();
            wasteToAdd.setAmount(newWaste);
            wasteTank.insert(wasteToAdd, Action.EXECUTE, AutomationType.INTERNAL);
            if (leftoverWaste > 0) {
                GasAttributes.Radiation radiation = wasteToAdd.getType().get(GasAttributes.Radiation.class);
                if (radiation != null) {
                    double radioactivity = radiation.getRadioactivity();
                    MekanismAPI.getRadiationManager().radiate(new Coord4D(this.getBounds().getCenter(), world), leftoverWaste * radioactivity);
                }
            }
        }
        this.lastBurnRate = toBurn;
        if (lastPartialWaste != partialWaste || lastBurnRemaining != burnRemaining) {
            markDirty();
        }
    }

    @Unique
    private static List<FissionRecipe> serverFissionRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.Recipes.FISSION.getType());
    }

    @Unique
    private static List<FluidCoolantRecipe> serverFluidCoolantRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.Recipes.FLUID_COOLANT.getType());
    }

    @Unique
    private static List<GasCoolantRecipe> serverGasCoolantRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.Recipes.GAS_COOLANT.getType());
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/util/NBTUtils;setGasStackIfPresent(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Ljava/util/function/Consumer;)V", ordinal = 0), method = "readUpdateTag")
    public void setFuelTankGasStack(CompoundTag nbt, String key, Consumer<GasStack> setter) {
        NBTUtils.setGasStackIfPresent(nbt, key, (value) -> this.fuelTank.setStackUnchecked(value));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/util/NBTUtils;setGasStackIfPresent(Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;Ljava/util/function/Consumer;)V", ordinal = 2), method = "readUpdateTag")
    public void setWasteTankGasStack(CompoundTag nbt, String key, Consumer<GasStack> setter) {
        NBTUtils.setGasStackIfPresent(nbt, key, (value) -> this.wasteTank.setStackUnchecked(value));
    }
}