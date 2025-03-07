package slimeknights.tconstruct.library.recipe.melting;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.common.recipe.LoggingRecipeSerializer;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Recipe to melt an ingredient into a specific fuel
 */
@RequiredArgsConstructor
public class MeltingRecipe implements IMeltingRecipe {
  @Getter
  private final ResourceLocation id;
  @Getter
  private final String group;
  private final Ingredient input;
  @Getter(AccessLevel.PROTECTED)
  private final FluidStack output;
  @Getter
  private final int temperature;
  /** Number of "steps" needed to melt this, by default lava increases steps by 5 every 4 ticks (25 a second) */
  @Getter
  private final int time;
  protected final List<FluidStack> byproducts;
  private List<List<FluidStack>> outputWithByproducts;

  @Override
  public boolean matches(IMeltingInventory inv, Level world) {
    return input.test(inv.getStack());
  }

  @Override
  public int getTemperature(IMeltingInventory inv) {
    return temperature;
  }

  @Override
  public int getTime(IMeltingInventory inv) {
    return time;
  }

  @Override
  public FluidStack getOutput(IMeltingInventory inv) {
    return output.copy();
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return NonNullList.of(Ingredient.EMPTY, input);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerSmeltery.meltingSerializer.get();
  }

  /** If true, this recipe is an ore recipe with increased output based on the machine */
  public boolean isOre() {
    return false;
  }

  @Override
  public void handleByproducts(IMeltingInventory inv, IFluidHandler handler) {
    // fill byproducts until we run out of space or byproducts
    for (FluidStack fluidStack : byproducts) {
      handler.fill(fluidStack.copy(), FluidAction.EXECUTE);
    }
  }

  /** Gets the recipe output for display in JEI */
  public List<List<FluidStack>> getDisplayOutput() {
    return Collections.singletonList(Collections.singletonList(output));
  }

  /** Gets the recipe output for display in JEI */
  public List<List<FluidStack>> getOutputWithByproducts() {
    if (outputWithByproducts == null) {
      outputWithByproducts = Stream.concat(Stream.of(output), byproducts.stream())
                                   .map(Collections::singletonList)
                                   .collect(Collectors.toList());
    }
    return outputWithByproducts;
  }

  /** Interface for use in the serializer */
  @FunctionalInterface
  public interface IFactory<T extends MeltingRecipe> {
    /** Creates a new instance of this recipe */
    T create(ResourceLocation id, String group, Ingredient input, FluidStack output, int temperature, int time, List<FluidStack> byproducts);
  }

  /**
   * Serializer for {@link MeltingRecipe}
   */
  @RequiredArgsConstructor
  public static class Serializer<T extends MeltingRecipe> extends LoggingRecipeSerializer<T> {
    private final IFactory<T> factory;

    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
      String group = GsonHelper.getAsString(json, "group", "");
      Ingredient input = Ingredient.fromJson(json.get("ingredient"));
      FluidStack output = RecipeHelper.deserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));

      // temperature calculates
      int temperature = GsonHelper.getAsInt(json, "temperature");
      int time = GsonHelper.getAsInt(json, "time");
      // validate values
      if (temperature < 0) throw new JsonSyntaxException("Melting temperature must be greater than zero");
      if (time <= 0) throw new JsonSyntaxException("Melting time must be greater than zero");
      List<FluidStack> byproducts = Collections.emptyList();
      if (json.has("byproducts")) {
        byproducts = JsonHelper.parseList(json, "byproducts", RecipeHelper::deserializeFluidStack);
      }

      return factory.create(id, group, input, output, temperature, time, byproducts);
    }

    @Nullable
    @Override
    protected T readSafe(ResourceLocation id, FriendlyByteBuf buffer) {
      String group = buffer.readUtf(Short.MAX_VALUE);
      Ingredient input = Ingredient.fromNetwork(buffer);
      FluidStack output = FluidStack.readFromPacket(buffer);
      int temperature = buffer.readInt();
      int time = buffer.readVarInt();
      ImmutableList.Builder<FluidStack> builder = ImmutableList.builder();
      int byproductCount = buffer.readVarInt();
      for (int i = 0; i < byproductCount; i++) {
        builder.add(FluidStack.readFromPacket(buffer));
      }
      return factory.create(id, group, input, output, temperature, time, builder.build());
    }

    @Override
    protected void writeSafe(FriendlyByteBuf buffer, MeltingRecipe recipe) {
      buffer.writeUtf(recipe.group);
      recipe.input.toNetwork(buffer);
      recipe.output.writeToPacket(buffer);
      buffer.writeInt(recipe.temperature);
      buffer.writeVarInt(recipe.time);
      buffer.writeVarInt(recipe.byproducts.size());
      for (FluidStack fluidStack : recipe.byproducts) {
        fluidStack.writeToPacket(buffer);
      }
    }
  }
}
