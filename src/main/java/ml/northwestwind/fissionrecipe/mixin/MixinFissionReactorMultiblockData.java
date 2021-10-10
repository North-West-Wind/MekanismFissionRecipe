package ml.northwestwind.fissionrecipe.mixin;

import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.inventory.AutomationType;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

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

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;create(Lmekanism/common/lib/multiblock/MultiblockData;Lmekanism/common/tile/prefab/TileEntityMultiblock;Ljava/util/function/LongSupplier;Ljava/util/function/BiPredicate;Ljava/util/function/BiPredicate;Ljava/util/function/Predicate;Lmekanism/api/chemical/attribute/ChemicalAttributeValidator;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 0), method = "<init>")
    public <MULTIBLOCK extends MultiblockData, TANK extends IChemicalTank<Gas, GasStack>> TANK customFuelTank(MultiblockChemicalTankBuilder<Gas, GasStack, TANK> multiblockChemicalTankBuilder, MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, BiPredicate<Gas, AutomationType> canExtract, BiPredicate<Gas, AutomationType> canInsert, Predicate<Gas> validator, ChemicalAttributeValidator attributeValidator, IContentsListener listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<FissionRecipe> recipes = server == null ? getRecipesOnClient() : getRecipesOnServer(server);
        return multiblockChemicalTankBuilder.create(multiblock, tile, capacity, canExtract, canInsert,
                (gas) -> recipes.stream().anyMatch(recipe -> recipe.getInput().testType(gas)),
                ChemicalAttributeValidator.ALWAYS_ALLOW, null);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/chemical/multiblock/MultiblockChemicalTankBuilder;create(Lmekanism/common/lib/multiblock/MultiblockData;Lmekanism/common/tile/prefab/TileEntityMultiblock;Ljava/util/function/LongSupplier;Ljava/util/function/BiPredicate;Ljava/util/function/BiPredicate;Ljava/util/function/Predicate;Lmekanism/api/chemical/attribute/ChemicalAttributeValidator;Lmekanism/api/IContentsListener;)Lmekanism/api/chemical/IChemicalTank;", ordinal = 1), method = "<init>")
    public <MULTIBLOCK extends MultiblockData, TANK extends IChemicalTank<Gas, GasStack>> TANK customWasteTank(MultiblockChemicalTankBuilder<Gas, GasStack, TANK> multiblockChemicalTankBuilder, MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, BiPredicate<Gas, AutomationType> canExtract, BiPredicate<Gas, AutomationType> canInsert, Predicate<Gas> validator, ChemicalAttributeValidator attributeValidator, IContentsListener listener) {
        return multiblockChemicalTankBuilder.create(multiblock, tile, capacity, canExtract, canInsert,
                (gas) -> true,
                ChemicalAttributeValidator.ALWAYS_ALLOW, null);
    }

    /**
     * @author Mekanism
     * @reason To handle custom fissile fuel
     */
    @Overwrite
    public void burnFuel(World world) {
        List<FissionRecipe> recipes = this.getWorld().isClientSide ? getRecipesOnClient() : getRecipesOnServer(this.getWorld().getServer());
        Optional<FissionRecipe> recipe = recipes.stream().filter(r -> r.getInput().testType(fuelTank.getType())).findFirst();
        if (!recipe.isPresent()) return;
        if (!wasteTank.isEmpty() && !wasteTank.isTypeEqual(recipe.get().getOutputRepresentation().getType())) return;
        double storedFuel = fuelTank.getStored() + this.burnRemaining;
        double toBurn = Math.min(Math.min(this.rateLimit, storedFuel), fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get());
        storedFuel -= toBurn;
        fuelTank.setStackSize((long) storedFuel, Action.EXECUTE);
        burnRemaining = storedFuel % 1;
        this.heatCapacitor.handleHeat(MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue() * recipe.get().getHeat(toBurn));
        this.partialWaste += toBurn * recipe.get().getOutput(null).getAmount();
        long newWaste = (long) Math.floor(partialWaste);
        if (newWaste > 0) {
            partialWaste %= 1;
            long leftoverWaste = Math.max(0, newWaste - this.wasteTank.getNeeded());
            GasStack wasteToAdd = recipe.get().getOutput(null);
            wasteToAdd.setAmount(newWaste);
            wasteTank.insert(wasteToAdd, Action.EXECUTE, AutomationType.INTERNAL);
            if (leftoverWaste > 0) {
                GasAttributes.Radiation radiation = wasteToAdd.getType().get(GasAttributes.Radiation.class);
                if (radiation != null) {
                    double radioactivity = radiation.getRadioactivity();
                    Mekanism.radiationManager.radiate(new Coord4D(this.getBounds().getCenter(), world), leftoverWaste * radioactivity);
                }
            }
        }
        this.lastBurnRate = toBurn;
    }

    @Unique
    @OnlyIn(Dist.CLIENT)
    private static List<FissionRecipe> getRecipesOnClient() {
        return Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FISSION.getType());
    }

    @Unique
    private static List<FissionRecipe> getRecipesOnServer(MinecraftServer server) {
        return server.getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FISSION.getType());
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