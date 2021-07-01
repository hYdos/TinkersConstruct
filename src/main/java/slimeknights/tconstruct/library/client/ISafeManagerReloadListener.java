package slimeknights.tconstruct.library.client;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.fml.ModLoader;

/**
 * Same as {@link ResourceManagerReloadListener}, but only runs if the mod loader state is valid
 */
public interface ISafeManagerReloadListener extends ResourceManagerReloadListener {
  @Override
  default void onResourceManagerReload(ResourceManager resourceManager) {
    if (ModLoader.isLoadingStateValid()) {
      onReloadSafe(resourceManager);
    }
  }

  /**
   * Safely handle a resource manager reload. Only runs if the mod loading state is valid
   * @param resourceManager  Resource manager
   */
  void onReloadSafe(ResourceManager resourceManager);
}
