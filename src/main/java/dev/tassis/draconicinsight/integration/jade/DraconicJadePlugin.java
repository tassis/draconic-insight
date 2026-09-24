package dev.tassis.draconicinsight.integration.jade;

import com.brandon3055.draconicevolution.blocks.energynet.EnergyCrystal;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase;
import com.brandon3055.draconicevolution.blocks.machines.CraftingInjector;
import com.brandon3055.draconicevolution.blocks.machines.EnergyCore;
import com.brandon3055.draconicevolution.blocks.machines.EnergyPylon;
import com.brandon3055.draconicevolution.blocks.machines.FusionCraftingCore;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.brandon3055.draconicevolution.blocks.tileentity.TileFusionCraftingCore;
import com.brandon3055.draconicevolution.blocks.tileentity.TileFusionCraftingInjector;
import dev.tassis.draconicinsight.integration.jade.client.FacilityComponentProvider;
import dev.tassis.draconicinsight.integration.jade.client.CrystalComponentProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin("draconicevolution")
public final class DraconicJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CrystalServerDataProvider.INSTANCE, TileCrystalBase.class);
        registration.registerBlockDataProvider(FacilityServerDataProvider.INSTANCE, TileEnergyCore.class);
        registration.registerBlockDataProvider(FacilityServerDataProvider.INSTANCE, TileEnergyPylon.class);
        registration.registerBlockDataProvider(FacilityServerDataProvider.INSTANCE, TileFusionCraftingCore.class);
        registration.registerBlockDataProvider(FacilityServerDataProvider.INSTANCE, TileFusionCraftingInjector.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(CrystalComponentProvider.FLOW_CONFIG, false);
        registration.registerBlockComponent(CrystalComponentProvider.INSTANCE, EnergyCrystal.class);
        registration.registerBlockComponent(FacilityComponentProvider.INSTANCE, EnergyCore.class);
        registration.registerBlockComponent(FacilityComponentProvider.INSTANCE, EnergyPylon.class);
        registration.registerBlockComponent(FacilityComponentProvider.INSTANCE, FusionCraftingCore.class);
        registration.registerBlockComponent(FacilityComponentProvider.INSTANCE, CraftingInjector.class);
    }
}
