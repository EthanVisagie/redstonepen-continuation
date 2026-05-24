/*
 * @file ExtendedShapelessRecipe.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.redstonepen.libmc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedShapelessRecipe implements CraftingRecipe
{
  public interface IRepairableToolItem
  {
    ItemStack onShapelessRecipeRepaired(ItemStack toolStack, int previousDamage, int repairedDamage);
  }

  //--------------------------------------------------------------------------------------------------------------------
  private final String group;
  private final CraftingBookCategory category;
  private final ItemStack result;
  private final NonNullList<Ingredient> ingredients;
  private final CompoundTag aspects;

  public ExtendedShapelessRecipe(String group, CraftingBookCategory cat, ItemStack output, NonNullList<Ingredient> ingredients, CompoundTag aspects)
  {
    this.group = group;
    this.category = cat;
    this.result = output;
    this.ingredients = ingredients;
    this.aspects=aspects;
  }

  @Override
  public RecipeSerializer<? extends CraftingRecipe> getSerializer()
  { return ExtendedShapelessRecipe.SERIALIZER; }

  public String getGroup()
  { return this.group; }

  @Override
  public CraftingBookCategory category()
  { return this.category; }

  public CompoundTag getAspects()
  { return aspects.copy(); }

  @Override
  public boolean isSpecial()
  { return isRepair() || Auxiliaries.nbtBoolean(aspects, "dynamic"); }

  public ItemStack getResultItem(HolderLookup.Provider ra)
  { return isSpecial() ? ItemStack.EMPTY : this.result; }

  public NonNullList<Ingredient> getIngredients()
  { return this.ingredients; }

  public boolean canCraftInDimensions(int i, int j)
  { return i * j >= this.ingredients.size(); }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
  {
    if(isRepair()) {
      NonNullList<ItemStack> remaining = getRepaired(inv).getB();
      for(int i=0; i<remaining.size(); ++i) {
        ItemStack rem_stack = remaining.get(i);
        ItemStack inv_stack = inv.getItem(i);
        if(inv_stack.isEmpty()) continue;
        if(!rem_stack.isEmpty() && !inv.getItem(i).is(rem_stack.getItem())) continue;
        remaining.set(i, ItemStack.EMPTY);
        if(!rem_stack.isEmpty()) rem_stack.grow(1);
        inv_stack.setCount(rem_stack.getCount());
      }
      return remaining;
    } else {
      final String tool_name = Auxiliaries.nbtString(aspects, "tool");
      final int tool_damage = getToolDamage();
      NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
      for(int i=0; i<remaining.size(); ++i) {
        final ItemStack stack = inv.getItem(i);
        if(Auxiliaries.getResourceLocation(stack.getItem()).toString().equals(tool_name)) {
          if(!stack.isDamageableItem()) {
            remaining.set(i, stack);
          } else { // implicitly !repair
            ItemStack rstack = stack.copy();
            rstack.setDamageValue(rstack.getDamageValue()+tool_damage);
            if(rstack.getDamageValue() < rstack.getMaxDamage()) {
              remaining.set(i, rstack);
            }
          }
        } else {
          final ItemStack remainder = stack.getItem().getCraftingRemainder();
          if(!remainder.isEmpty()) {
            remainder.setCount(stack.getCount());
            remaining.set(i, remainder);
          }
        }
      }
      return remaining;
    }
  }

  @Override
  public boolean matches(CraftingInput input, Level world)
  {
    final List<Ingredient> unmatched = new ArrayList<>(this.ingredients);
    for(int j=0; j<input.size(); ++j) {
      final ItemStack ingr = input.getItem(j);
      if(ingr.isEmpty()) continue;
      boolean matched = false;
      for(int i=0; i<unmatched.size(); ++i) {
        if(!unmatched.get(i).test(ingr)) continue;
        unmatched.remove(i);
        matched = true;
        break;
      }
      if(!matched) return false;
    }
    return unmatched.isEmpty();
  }

  @Override
  public ItemStack assemble(CraftingInput inv, HolderLookup.Provider ra)
  {
    if(isRepair()) {
      return getRepaired(inv).getA();
    } else {
      // Initial item crafting
      ItemStack rstack = result.copy();
      if(rstack.isEmpty()) return ItemStack.EMPTY;
      if(Auxiliaries.nbtInt(aspects, "initial_durability") > 0) {
        int dmg = Math.max(0, rstack.getMaxDamage() - Auxiliaries.nbtInt(aspects, "initial_durability"));
        if(dmg > 0) rstack.setDamageValue(dmg);
      } else if(Auxiliaries.nbtInt(aspects, "initial_damage") > 0) {
        int dmg = Math.min(Auxiliaries.nbtInt(aspects, "initial_damage"), rstack.getMaxDamage());
        if(dmg > 0) rstack.setDamageValue(dmg);
      }
      return rstack;
    }
  }

  @Override
  public PlacementInfo placementInfo()
  { return PlacementInfo.create(this.ingredients); }

  @Override
  public List<RecipeDisplay> display()
  {
    final List<SlotDisplay> ingredientDisplays = this.ingredients.stream().map(Ingredient::display).toList();
    return List.of(new ShapelessCraftingRecipeDisplay(
      ingredientDisplays,
      new SlotDisplay.ItemStackSlotDisplay(this.result),
      new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
    ));
  }

  //--------------------------------------------------------------------------------------------------------------------

  private int getToolDamage()
  {
    if(aspects.contains("tool_repair")) return (-Mth.clamp(Auxiliaries.nbtInt(aspects, "tool_repair"), 0, 4096));
    if(aspects.contains("tool_damage")) return (Mth.clamp(Auxiliaries.nbtInt(aspects, "tool_damage"), 1, 1024));
    return 0;
  }

  private boolean isRepair()
  { return getToolDamage() < 0; }

  private Tuple<ItemStack, NonNullList<ItemStack>> getRepaired(CraftingInput inv)
  {
    final String tool_name = Auxiliaries.nbtString(aspects, "tool");
    final Map<Item, Integer> repair_items = new HashMap<>();
    final NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    ItemStack tool_item = ItemStack.EMPTY;
    for(int i=0; i<inv.size(); ++i) {
      final ItemStack stack = inv.getItem(i);
      if(stack.isEmpty()) {
        continue;
      } else if(Auxiliaries.getResourceLocation(stack.getItem()).toString().equals(tool_name)) {
        tool_item = stack.copy();
      } else {
        remaining.set(i, stack.copy());
        repair_items.put(stack.getItem(), stack.getCount() + repair_items.getOrDefault(stack.getItem(), 0));
      }
    }
    if(tool_item.isEmpty()) {
      return new Tuple<>(ItemStack.EMPTY, remaining);
    } else if(!tool_item.isDamageableItem()) {
      Auxiliaries.logWarn("Repairing '" +  Auxiliaries.getResourceLocation(tool_item.getItem()) +"' can't work, the item is not damageable.");
      return new Tuple<>(ItemStack.EMPTY, remaining);
    } else {
      final int dmg = tool_item.getDamageValue();
      if((dmg <= 0) && (!Auxiliaries.nbtBoolean(aspects, "over_repair"))) return new Tuple<>(ItemStack.EMPTY, remaining);
      final int min_repair_item_count = repair_items.values().stream().mapToInt(Integer::intValue).min().orElse(0);
      if(min_repair_item_count <= 0) return new Tuple<>(ItemStack.EMPTY, remaining);
      final int single_repair_dur = Auxiliaries.nbtBoolean(aspects, "relative_repair_damage")
        ? Math.max(1, -getToolDamage() * tool_item.getMaxDamage() / 100)
        : Math.max(1, -getToolDamage());
      int num_repairs = dmg/single_repair_dur;
      if(num_repairs*single_repair_dur < dmg) ++num_repairs;
      num_repairs = Math.min(num_repairs, min_repair_item_count);
      for(Item ki: repair_items.keySet()) repair_items.put(ki, num_repairs);
      tool_item.setDamageValue(Math.max(dmg-(single_repair_dur*num_repairs), 0));
      for(int i=0; i<remaining.size(); ++i) {
        ItemStack stack = inv.getItem(i);
        if(stack.isEmpty()) continue;
        if(Auxiliaries.getResourceLocation(stack.getItem()).toString().equals(tool_name)) continue;
        final ItemStack remainder = stack.getItem().getCraftingRemainder();
        if(!remainder.isEmpty()) {
          remainder.setCount(stack.getCount());
          remaining.set(i, remainder);
        } else {
          remaining.set(i, stack.copy());
        }
      }
      for(int i=0; i<remaining.size(); ++i) {
        final ItemStack stack = remaining.get(i);
        final Item item = stack.getItem();
        if(!repair_items.containsKey(item)) continue;
        int n = repair_items.get(item);
        if(stack.getCount() >= n) {
          stack.shrink(n);
          repair_items.remove(item);
        } else {
          repair_items.put(item, n-stack.getCount());
          remaining.set(i, ItemStack.EMPTY);
        }
      }
      if((tool_item.getItem() instanceof IRepairableToolItem)) {
        tool_item = ((IRepairableToolItem)(tool_item.getItem())).onShapelessRecipeRepaired(tool_item, dmg, tool_item.getDamageValue());
      }
      return new Tuple<>(tool_item, remaining);
    }
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static final ExtendedShapelessRecipe.Serializer SERIALIZER = new ExtendedShapelessRecipe.Serializer();

  public static class Serializer implements RecipeSerializer<ExtendedShapelessRecipe>
  {
    @Override
    public MapCodec<ExtendedShapelessRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ExtendedShapelessRecipe> streamCodec() {
      return STREAM_CODEC;
    }

    @SuppressWarnings("unchecked")
    private static final MapCodec<ExtendedShapelessRecipe> CODEC = RecordCodecBuilder.<ExtendedShapelessRecipe>mapCodec(instance ->
        instance.group(Codec.STRING.optionalFieldOf("group", "")
                .forGetter(r->r.group),
        CraftingBookCategory.CODEC
                .fieldOf("category")
                .orElse(CraftingBookCategory.MISC)
                .forGetter(r->r.category),
        ItemStack.CODEC
                .fieldOf("result")
                .forGetter(r->r.result),
        Ingredient.CODEC
                .listOf().fieldOf("ingredients").flatXmap(list -> {
                    final Ingredient[] ingredients = list.stream().filter(ing->!ing.isEmpty()).toArray(Ingredient[]::new);
                    if(ingredients.length == 0) { return DataResult.error(() -> "no ingredients"); }
                    if(ingredients.length > 9) { return DataResult.error(() -> "too many ingredients"); }
                    final NonNullList<Ingredient> nnl = NonNullList.create();
                    Collections.addAll(nnl, ingredients);
                    return DataResult.success(nnl);
                  }, DataResult::success)
                .forGetter(r->r.ingredients),
        CompoundTag.CODEC
                .optionalFieldOf("aspects", new CompoundTag())
                .forGetter(r->r.aspects)
      )
      .apply(instance, ExtendedShapelessRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtendedShapelessRecipe> STREAM_CODEC = StreamCodec.of(
      ExtendedShapelessRecipe.Serializer::toNetwork,
      ExtendedShapelessRecipe.Serializer::fromNetwork
    );

    private static ExtendedShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf)
    {
      final String group = buf.readUtf();
      final CraftingBookCategory cat = buf.readEnum(CraftingBookCategory.class);
      final int size = buf.readVarInt();
      final NonNullList<Ingredient> ingredients = NonNullList.create();
      for(int i=0; i<size; ++i) ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
      final ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
      final CompoundTag aspects = buf.readNbt();
      return new ExtendedShapelessRecipe(group, cat, stack, ingredients, aspects);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, ExtendedShapelessRecipe recipe)
    {
      buf.writeUtf(recipe.group);
      buf.writeEnum(recipe.category);
      buf.writeVarInt(recipe.ingredients.size());
      for(Ingredient ingredient : recipe.ingredients) { Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient); }
      ItemStack.STREAM_CODEC.encode(buf, recipe.result);
      buf.writeNbt(recipe.getAspects());
    }
  }
}
