package ml.northwestwind.fissionrecipe.events;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.GasDeferredRegister;
import ml.northwestwind.fissionrecipe.MekanismFission;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MekanismFission.MOD_ID)
public class RegistryEvents {
    private static final GasDeferredRegister GASES = new GasDeferredRegister(MekanismFission.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MekanismFission.MOD_ID);

    public static void registerGases() {
        GASES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void registerItems() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    static {
        GASES.register("pure_plutonium_239", () -> new Gas(GasBuilder.builder().color(0x20a9b6).with(new GasAttributes.Radiation(0.02D))));
        GASES.register("pure_plutonium_240", () -> new Gas(GasBuilder.builder().color(0x208cb6).with(new GasAttributes.Radiation(0.02D))));
        GASES.register("pure_plutonium_241", () -> new Gas(GasBuilder.builder().color(0x2062b6).with(new GasAttributes.Radiation(0.02D))));
        GASES.register("plutonium_240", () -> new Gas(GasBuilder.builder().color(0x1c808a).with(new GasAttributes.Radiation(0.02D))));
        GASES.register("plutonium_241", () -> new Gas(GasBuilder.builder().color(0x12747e).with(new GasAttributes.Radiation(0.02D))));
        GASES.register("plutonium_difluoride", () -> new Gas(GasBuilder.builder().color(0x609979)));
        GASES.register("plutonium_hexafluoride", () -> new Gas(GasBuilder.builder().color(0x609989)));
        GASES.register("fissile_fuel_mk2", () -> new Gas(GasBuilder.builder().color(0x2e3332)));
        GASES.register("fissile_fuel_mk3", () -> new Gas(GasBuilder.builder().color(0x2e3233)));
        GASES.register("decayed_plutonium_239", () -> new Gas(GasBuilder.builder().color(0x2a4f4c).with(new GasAttributes.Radiation(0.03D))));
        GASES.register("decayed_plutonium_241", () -> new Gas(GasBuilder.builder().color(0x1e3c39).with(new GasAttributes.Radiation(0.03D))));
        GASES.register("stable_plutonium_239", () -> new Gas(GasBuilder.builder().color(0x21aab7)));
        GASES.register("stable_plutonium_241", () -> new Gas(GasBuilder.builder().color(0x218db7)));

        ITEMS.register("radioisotope_stabilizer", () -> new Item(new Item.Properties().tab(Mekanism.tabMekanism)));
    }
}
