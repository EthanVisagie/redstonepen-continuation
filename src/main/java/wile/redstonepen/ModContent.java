/*
 * @file ModContent.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.redstonepen;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import wile.redstonepen.blocks.*;
import wile.redstonepen.items.RedstonePenItem;
import wile.redstonepen.items.RemoteItem;
import wile.redstonepen.libmc.StandardBlocks;
import wile.redstonepen.libmc.Auxiliaries;
import wile.redstonepen.libmc.Registries;


public class ModContent
{
  private static net.minecraft.resources.Identifier id(String name)
  { return net.minecraft.resources.Identifier.fromNamespaceAndPath(ModConstants.MODID, name); }

  private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.block.Block> blockKey(String name)
  { return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, id(name)); }

  private static net.minecraft.resources.ResourceKey<Item> itemKey(String name)
  { return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, id(name)); }

  private static BlockBehaviour.Properties blockProperties(String name)
  { return BlockBehaviour.Properties.of().setId(blockKey(name)); }

  private static Item.Properties itemProperties(String name)
  { return new Item.Properties().setId(itemKey(name)); }

  public static void init()
  {
    initBlocks();
    initItems();
    Registries.addRecipeSerializer("crafting_extended_shapeless", ()->wile.redstonepen.libmc.ExtendedShapelessRecipe.SERIALIZER);
  }

  public static void initBlocks()
  {
    Registries.addBlock("track",
      ()->new RedstoneTrack.RedstoneTrackBlock(
        StandardBlocks.CFG_DEFAULT,
        blockProperties("track").noCollision().instabreak().dynamicShape().randomTicks()
      ),
      RedstoneTrack.TrackBlockEntity::new
    );
    Registries.addBlock("control_box",
      ()->new ControlBox.ControlBoxBlock(
        StandardBlocks.CFG_CUTOUT,
        blockProperties("control_box").noCollision().instabreak(),
        new AABB[]{
          Auxiliaries.getPixeledAABB(0,0,0, 16,2,16),
          Auxiliaries.getPixeledAABB(3,1,3, 13,3.9,13)
        }
      ),
      CircuitComponents.DirectedComponentBlockItem::new,
      ControlBox.ControlBoxBlockEntity::new,
      ControlBox.ControlBoxUiContainer::new
    );
    Registries.addBlock("relay",
      ()->new CircuitComponents.RelayBlock(
        StandardBlocks.CFG_CUTOUT,
        blockProperties("relay").noCollision().instabreak(),
        Auxiliaries.getPixeledAABB(5,0,0, 11,1,16)
      ),
      CircuitComponents.DirectedComponentBlockItem::new
    );
    Registries.addBlock("inverted_relay",
      ()->new CircuitComponents.InvertedRelayBlock(
        StandardBlocks.CFG_CUTOUT,
        blockProperties("inverted_relay").noCollision().instabreak(),
        Auxiliaries.getPixeledAABB(5,0,0, 11,1,16)
      ),
      CircuitComponents.DirectedComponentBlockItem::new
    );
    Registries.addBlock("pulse_relay",
      ()->new CircuitComponents.PulseRelayBlock(
        StandardBlocks.CFG_CUTOUT,
        blockProperties("pulse_relay").noCollision().instabreak(),
        Auxiliaries.getPixeledAABB(5,0,0, 11,1,16)
      ),
      CircuitComponents.DirectedComponentBlockItem::new
    );
    Registries.addBlock("bistable_relay",
      ()->new CircuitComponents.BistableRelayBlock(
        StandardBlocks.CFG_CUTOUT,
        blockProperties("bistable_relay").noCollision().instabreak(),
        Auxiliaries.getPixeledAABB(5,0,0, 11,1,16)
      ),
      CircuitComponents.DirectedComponentBlockItem::new
    );
    Registries.addBlock("bridge_relay",
      ()->new CircuitComponents.BridgeRelayBlock(
        StandardBlocks.CFG_CUTOUT,
        blockProperties("bridge_relay").noCollision().instabreak(),
        Auxiliaries.getPixeledAABB(5,0,0, 11,1,16)
      ),
      CircuitComponents.DirectedComponentBlockItem::new
    );
    Registries.addBlock("basic_gauge",
      ()->new BasicGauge.BasicGaugeBlock(
        StandardBlocks.CFG_TRANSLUCENT,
        blockProperties("basic_gauge").isValidSpawn(Blocks::never).strength(0.3f).sound(SoundType.COPPER).noCollision().lightLevel((s)->3)
      )
    );
    Registries.addBlock("basic_lever",
      ()->new BasicLever.BasicLeverBlock(
        new BasicLever.BasicLeverBlock.Config(0.8f, 0.9f),
        blockProperties("basic_lever").noCollision().isValidSpawn(Blocks::never).strength(0.3f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)
      )
    );
    Registries.addBlock("basic_button",
      ()->new BasicButton.BasicButtonBlock(
        new BasicButton.BasicButtonBlock.Config(0.8f, 0.9f, 20),
        blockProperties("basic_button").noCollision().isValidSpawn(Blocks::never).strength(0.3f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)
      )
    );
    Registries.addBlock("basic_pulse_button",
      ()->new BasicButton.BasicButtonBlock(
        new BasicButton.BasicButtonBlock.Config(0.8f, 0.9f, 2),
        blockProperties("basic_pulse_button").noCollision().isValidSpawn(Blocks::never).strength(0.3f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)
      )
    );
  }

  public static void initItems()
  {
    Registries.addItem("pen", ()->new RedstonePenItem(
      itemProperties("pen").rarity(Rarity.UNCOMMON).stacksTo(0).durability(256)
    ));
    Registries.addItem("quill", ()->new RedstonePenItem(
      itemProperties("quill").rarity(Rarity.UNCOMMON).stacksTo(1).durability(0)
    ));
    Registries.addItem("remote", ()->new RemoteItem(
      itemProperties("remote").rarity(Rarity.UNCOMMON).stacksTo(1).durability(0)
    ));
  }

  public static void initReferences()
  {
    Registries.instantiateAll();
    references.TRACK_BLOCK = (RedstoneTrack.RedstoneTrackBlock)Registries.getBlock("track");
    references.BRIDGE_RELAY_BLOCK = (CircuitComponents.BridgeRelayBlock)Registries.getBlock("bridge_relay");
    references.CONTROLBOX_BLOCK = (ControlBox.ControlBoxBlock)Registries.getBlock("control_box");
    references.BASIC_GAUGE_BLOCK = (BasicGauge.BasicGaugeBlock)Registries.getBlock("basic_gauge");
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Accessors
  //--------------------------------------------------------------------------------------------------------------------

  public static final class references
  {
    public static RedstoneTrack.RedstoneTrackBlock TRACK_BLOCK = null;
    public static CircuitComponents.BridgeRelayBlock BRIDGE_RELAY_BLOCK = null;
    public static ControlBox.ControlBoxBlock CONTROLBOX_BLOCK = null;
    public static BasicGauge.BasicGaugeBlock BASIC_GAUGE_BLOCK = null;
  }

}
