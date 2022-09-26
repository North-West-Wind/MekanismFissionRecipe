package ml.northwestwind.fissionrecipe.mixin;

import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;
import java.util.function.*;

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
    public MultiblockHeatCapacitor<FissionReactorMultiblockData> heatCapacitor;
    @Shadow
    public double partialWaste;
    @Shadow
    public IGasTank wasteTank;
    @Shadow
    public double lastBurnRate;

    @Shadow
    public abstract double getBoilEfficiency();

    @Shadow
    public MultiblockFluidTank<FissionReactorMultiblockData> fluidCoolantTank;
    @Shadow
    public IGasTank heatedCoolantTank;
    @Shadow
    public IGasTank gasCoolantTank;
    @Shadow
    public long lastBoilRate;

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/fluid/MultiblockFluidTank;create(Lmekanism/common/lib/multiblock/MultiblockData;Lmekanism/common/tile/prefab/TileEntityMultiblock;Ljava/util/function/IntSupplier;Ljava/util/function/BiPredicate;Ljava/util/function/BiPredicate;Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;)Lmekanism/common/capabilities/fluid/MultiblockFluidTank;", ordinal = 0), method = "<init>")
    public <MULTIBLOCK extends MultiblockData> MultiblockFluidTank<MULTIBLOCK> customFluidCoolantTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, IntSupplier capacity, BiPredicate<FluidStack, AutomationType> canExtract, BiPredicate<FluidStack, AutomationType> canInsert, Predicate<FluidStack> validator, IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<FluidCoolantRecipe> recipes = serverFluidCoolantRecipes(server);
        return MultiblockFluidTank.create(multiblock, tile, capacity, canExtract, canInsert,
                (fluid) -> recipes.stream().anyMatch(recipe -> recipe.getInput().testType(fluid)), listener);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;create(Lmekanism/common/lib/multiblock/MultiblockData;Lmekanism/common/tile/prefab/TileEntityMultiblock;Ljava/util/function/LongSupplier;Ljava/util/function/BiPredicate;Ljava/util/function/BiPredicate;Ljava/util/function/Predicate;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0), method = "<init>")
    public <MULTIBLOCK extends MultiblockData, TANK extends IChemicalTank<Gas, GasStack>> TANK customGasCoolantTank(MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank> instance, MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, BiPredicate<Gas, AutomationType> canExtract, BiPredicate<Gas, AutomationType> canInsert, Predicate<Gas> validator) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<GasCoolantRecipe> recipes = serverGasCoolantRecipes(server);
        return (TANK) instance.create(multiblock, tile, capacity, canExtract, canInsert,
                (gas) -> recipes.stream().anyMatch(recipe -> recipe.getInput().testType(gas)));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;create(Lmekanism/common/lib/multiblock/MultiblockData;Lmekanism/common/tile/prefab/TileEntityMultiblock;Ljava/util/function/LongSupplier;Ljava/util/function/BiPredicate;Ljava/util/function/BiPredicate;Ljava/util/function/Predicate;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 1), method = "<init>")
    public <MULTIBLOCK extends MultiblockData, TANK extends IChemicalTank<Gas, GasStack>> TANK customHeatedCoolantTank(MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank> instance, MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, BiPredicate<Gas, AutomationType> canExtract, BiPredicate<Gas, AutomationType> canInsert, Predicate<Gas> validator) {
        return (TANK) instance.create(multiblock, tile, capacity, canExtract, canInsert, (gas) -> true);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;create(Lmekanism/common/lib/multiblock/MultiblockData;Lmekanism/common/tile/prefab/TileEntityMultiblock;Ljava/util/function/LongSupplier;Ljava/util/function/BiPredicate;Ljava/util/function/BiPredicate;Ljava/util/function/Predicate;Lmekanism/api/chemical/attribute/ChemicalAttributeValidator;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0), method = "<init>")
    public <MULTIBLOCK extends MultiblockData, TANK extends IChemicalTank<Gas, GasStack>> TANK customFuelTank(MultiblockChemicalTankBuilder<Gas, GasStack, TANK> multiblockChemicalTankBuilder, MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, BiPredicate<Gas, AutomationType> canExtract, BiPredicate<Gas, AutomationType> canInsert, Predicate<Gas> validator, ChemicalAttributeValidator attributeValidator, IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<FissionRecipe> recipes = serverFissionRecipes(server);
        return multiblockChemicalTankBuilder.create(multiblock, tile, capacity, canExtract, canInsert,
                (gas) -> recipes.stream().anyMatch(recipe -> recipe.getInput().testType(gas)),
                ChemicalAttributeValidator.ALWAYS_ALLOW, null);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;create(Lmekanism/common/lib/multiblock/MultiblockData;Lmekanism/common/tile/prefab/TileEntityMultiblock;Ljava/util/function/LongSupplier;Ljava/util/function/BiPredicate;Ljava/util/function/BiPredicate;Ljava/util/function/Predicate;Lmekanism/api/chemical/attribute/ChemicalAttributeValidator;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 1), method = "<init>")
    public <MULTIBLOCK extends MultiblockData, TANK extends IChemicalTank<Gas, GasStack>> TANK customWasteTank(MultiblockChemicalTankBuilder<Gas, GasStack, TANK> multiblockChemicalTankBuilder, MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, BiPredicate<Gas, AutomationType> canExtract, BiPredicate<Gas, AutomationType> canInsert, Predicate<Gas> validator, ChemicalAttributeValidator attributeValidator, IContentsListener listener) {
        return multiblockChemicalTankBuilder.create(multiblock, tile, capacity, canExtract, canInsert, (gas) -> true, ChemicalAttributeValidator.ALWAYS_ALLOW, null);
    }

    /**
     * @author Mekanism
     * @reason To handle custom coolant
     */
    @Overwrite
    private void handleCoolant() {
        double temp = this.heatCapacitor.getTemperature();
        double heat = this.getBoilEfficiency() * (temp - HeatUtils.BASE_BOIL_TEMP) * this.heatCapacitor.getHeatCapacity();
        long coolantHeated = 0L;
        if (!this.fluidCoolantTank.isEmpty()) {
            Optional<FluidCoolantRecipe> recipe;
            if (!fluidCoolantRecipe.isPresent())
                fluidCoolantRecipe = serverFluidCoolantRecipes(this.getWorld().getServer()).stream().filter(r -> r.getInput().testType(fluidCoolantTank.getFluid())).findFirst();
            recipe = fluidCoolantRecipe;
            if (!recipe.isPresent()) return;
            double caseCoolantHeat = heat * 0.5D;
            coolantHeated = (long) ((int) (HeatUtils.getSteamEnergyEfficiency() * caseCoolantHeat / HeatUtils.getWaterThermalEnthalpy()));
            coolantHeated = Math.max(0L, Math.min(coolantHeated, this.fluidCoolantTank.getFluidAmount()));
            long originalCoolantHeated = coolantHeated;
            coolantHeated = (long) recipe.get().getHeat(coolantHeated);
            if (coolantHeated > 0L) {
                MekanismUtils.logMismatchedStackSize((long) this.fluidCoolantTank.shrinkStack((int) coolantHeated, Action.EXECUTE), coolantHeated);
                if (fluidCoolantTank.isEmpty()) fluidCoolantRecipe = Optional.empty();
                GasStack output = recipe.get().getOutputRepresentation();
                output.setAmount(coolantHeated);
                this.heatedCoolantTank.insert(output, Action.EXECUTE, AutomationType.INTERNAL);
                caseCoolantHeat = (double) originalCoolantHeated * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
                this.heatCapacitor.handleHeat(-caseCoolantHeat);
            }
        } else if (!this.gasCoolantTank.isEmpty()) {
            Optional<GasCoolantRecipe> recipe;
            if (!gasCoolantRecipe.isPresent())
                gasCoolantRecipe = serverGasCoolantRecipes(this.getWorld().getServer()).stream().filter(r -> r.getInput().testType(gasCoolantTank.getStack())).findFirst();
            recipe = gasCoolantRecipe;
            if (!recipe.isPresent()) return;
            double caseCoolantHeat = heat * recipe.get().getConductivity();
            coolantHeated = (long) ((int) (caseCoolantHeat / recipe.get().getThermalEnthalpy()));
            coolantHeated = Math.max(0L, Math.min(coolantHeated, this.gasCoolantTank.getStored()));
            long originalCoolantHeated = coolantHeated;
            coolantHeated = (long) recipe.get().getHeat(coolantHeated);
            if (coolantHeated > 0L) {
                MekanismUtils.logMismatchedStackSize(this.gasCoolantTank.shrinkStack((int) coolantHeated, Action.EXECUTE), coolantHeated);
                if (gasCoolantTank.isEmpty()) gasCoolantRecipe = Optional.empty();
                GasStack output = recipe.get().getOutputRepresentation();
                output.setAmount(coolantHeated);
                this.heatedCoolantTank.insert(output, Action.EXECUTE, AutomationType.INTERNAL);
                caseCoolantHeat = (double) originalCoolantHeated * recipe.get().getThermalEnthalpy();
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
    public void burnFuel(World world) {
        Optional<FissionRecipe> recipe;
        if (!fissionRecipe.isPresent()) fissionRecipe = serverFissionRecipes(this.getWorld().getServer()).stream().filter(r -> r.getInput().testType(fuelTank.getType())).findFirst();
        recipe = fissionRecipe;
        if (!recipe.isPresent()) return;
        if (!wasteTank.isEmpty() && !wasteTank.isTypeEqual(recipe.get().getOutputRepresentation().getType())) return;
        double storedFuel = fuelTank.getStored() + this.burnRemaining;
        double toBurn = Math.min(Math.min(this.rateLimit, storedFuel), fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get());
        double lastPartialWaste = partialWaste;
        double lastBurnRemaining = burnRemaining;
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
        // update previous burn
        lastBurnRate = toBurn;
        if (lastPartialWaste != partialWaste || lastBurnRemaining != burnRemaining) {
            markDirty();
        }
    }

    @Unique
    private static List<FissionRecipe> serverFissionRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FISSION.getType());
    }

    @Unique
    private static List<FluidCoolantRecipe> serverFluidCoolantRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FLUID_COOLANT.getType());
    }

    @Unique
    private static List<GasCoolantRecipe> serverGasCoolantRecipes(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.GAS_COOLANT.getType());
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/util/NBTUtils;setGasStackIfPresent(Lnet/minecraft/nbt/CompoundNBT;Ljava/lang/String;Ljava/util/function/Consumer;)V", ordinal = 0), method = "readUpdateTag")
    public void setFuelTankGasStack(CompoundNBT nbt, String key, Consumer<GasStack> setter) {
        NBTUtils.setGasStackIfPresent(nbt, key, (value) -> this.fuelTank.setStackUnchecked(value));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/util/NBTUtils;setGasStackIfPresent(Lnet/minecraft/nbt/CompoundNBT;Ljava/lang/String;Ljava/util/function/Consumer;)V", ordinal = 2), method = "readUpdateTag")
    public void setWasteTankGasStack(CompoundNBT nbt, String key, Consumer<GasStack> setter) {
        NBTUtils.setGasStackIfPresent(nbt, key, (value) -> this.wasteTank.setStackUnchecked(value));
    }
}