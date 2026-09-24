package dev.tassis.draconicinsight.integration.jei;

import dev.tassis.draconicinsight.DraconicInsight;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class DraconicJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
        DraconicInsight.MOD_ID,
        "draconic_information"
    );

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        CrystalInfoRegistration.register(registration);
    }
}
