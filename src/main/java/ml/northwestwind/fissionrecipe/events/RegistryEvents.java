package ml.northwestwind.fissionrecipe.events;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.MekanismFission;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = MekanismFission.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(MekanismFission.MOD_ID)
public class RegistryEvents {
    @SubscribeEvent
    public static void registerGas(final RegistryEvent.Register<Gas> event) {
        event.getRegistry().registerAll(
                Gases.PURE_PLUTONIUM_239,
                Gases.PURE_PLUTONIUM_240,
                Gases.PURE_PLUTONIUM_241,
                Gases.PLUTONIUM_240,
                Gases.PLUTONIUM_241,
                Gases.PLUTONIUM_DIFLUORIDE,
                Gases.PLUTONIUM_HEXAFLUORIDE,
                Gases.FISSILE_FUEL_MKII,
                Gases.FISSILE_FUEL_MKIII,
                Gases.DECAYED_PLUTONIUM_239,
                Gases.DECAYED_PLUTONIUM_241,
                Gases.STABLE_PLUTONIUM_239,
                Gases.STABLE_PLUTONIUM_241
        );
    }

    @SubscribeEvent
    public static void registerItem(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                Items.RADIOISOTOPE_STABILIZER
        );
    }

    public static class Gases {
        public static final Gas PURE_PLUTONIUM_239 = new Gas(GasBuilder.builder().color(0x20a9b6).with(new GasAttributes.Radiation(0.02D))).setRegistryName("pure_plutonium_239");
        public static final Gas PURE_PLUTONIUM_240 = new Gas(GasBuilder.builder().color(0x208cb6).with(new GasAttributes.Radiation(0.02D))).setRegistryName("pure_plutonium_240");
        public static final Gas PURE_PLUTONIUM_241 = new Gas(GasBuilder.builder().color(0x2062b6).with(new GasAttributes.Radiation(0.02D))).setRegistryName("pure_plutonium_241");
        public static final Gas PLUTONIUM_240 = new Gas(GasBuilder.builder().color(0x1c808a).with(new GasAttributes.Radiation(0.02D))).setRegistryName("plutonium_240");
        public static final Gas PLUTONIUM_241 = new Gas(GasBuilder.builder().color(0x12747e).with(new GasAttributes.Radiation(0.02D))).setRegistryName("plutonium_241");
        public static final Gas PLUTONIUM_DIFLUORIDE = new Gas(GasBuilder.builder().color(0x609979)).setRegistryName("plutonium_difluoride");
        public static final Gas PLUTONIUM_HEXAFLUORIDE = new Gas(GasBuilder.builder().color(0x609989)).setRegistryName("plutonium_hexafluoride");
        public static final Gas FISSILE_FUEL_MKII = new Gas(GasBuilder.builder().color(0x2e3332)).setRegistryName("fissile_fuel_mk2");
        public static final Gas FISSILE_FUEL_MKIII = new Gas(GasBuilder.builder().color(0x2e3233)).setRegistryName("fissile_fuel_mk3");
        public static final Gas DECAYED_PLUTONIUM_239 = new Gas(GasBuilder.builder().color(0x2a4f4c).with(new GasAttributes.Radiation(0.03D))).setRegistryName("decayed_plutonium_239");
        public static final Gas DECAYED_PLUTONIUM_241 = new Gas(GasBuilder.builder().color(0x1e3c39).with(new GasAttributes.Radiation(0.03D))).setRegistryName("decayed_plutonium_241");
        public static final Gas STABLE_PLUTONIUM_239 = new Gas(GasBuilder.builder().color(0x21aab7)).setRegistryName("stable_plutonium_239");
        public static final Gas STABLE_PLUTONIUM_241 = new Gas(GasBuilder.builder().color(0x218db7)).setRegistryName("stable_plutonium_241");
    }

    public static class Items {
        public static final Item RADIOISOTOPE_STABILIZER = new Item(new Item.Properties().tab(Mekanism.tabMekanism)).setRegistryName("radioisotope_stabilizer");
    }
}
