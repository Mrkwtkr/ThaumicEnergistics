package thaumicenergistics.proxy;

import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.registries.TileEntities;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy
{
	public void registerBlocks()
	{
		for( BlockEnum block : BlockEnum.values() )
		{
			GameRegistry.registerBlock( block.getBlock(), block.getUnlocalizedName() );
		}
	}

	public void registerFluids()
	{
		GaseousEssentia.registerGases();
	}

	public void registerItems()
	{

		for( ItemEnum item : ItemEnum.values() )
		{
			GameRegistry.registerItem( item.getItem(), item.getInternalName() );
		}

	}

	public void registerMovables()
	{
	}

	public void registerRenderers()
	{
	}

	public void registerTileEntities()
	{
		TileEntities.registerTiles();
	}

}
