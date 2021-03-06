package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.network.packet.PacketClientEssentiaTerminal;
import thaumicenergistics.network.packet.PacketServerEssentiaTerminal;
import thaumicenergistics.parts.AEPartEssentiaTerminal;
import thaumicenergistics.util.EssentiaCellTerminalWorker;
import thaumicenergistics.util.EssentiaConversionHelper;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;

/**
 * Inventory container for the essentia terminal.
 * @author Nividica
 *
 */
public class ContainerEssentiaTerminal
	extends ContainerCellTerminalBase
{
	/**
	 * The terminal this is associated with.
	 */
	private AEPartEssentiaTerminal terminal = null;
	
	/**
	 * The AE machine source representation of the terminal.
	 */
	private MachineSource machineSource = null;

	/**
	 * Creates the container
	 * @param terminal Parent terminal.
	 * @param player Player that owns this container.
	 */
	public ContainerEssentiaTerminal( AEPartEssentiaTerminal terminal, EntityPlayer player )
	{
		// Call the super
		super( player );
		
		// Set the terminal
		this.terminal = terminal;
		
		// Get and set the machine source
		this.machineSource = terminal.getTerminalMachineSource();

		// Is this server side?
		if ( !player.worldObj.isRemote )
		{
			// Get the monitor
			this.monitor = terminal.getGridBlock().getFluidMonitor();

			// Attach to the monitor
			this.attachToMonitor();

			// Add this container to the terminal's list
			terminal.addContainer( this );
		}

		// Bind our inventory
		this.bindToInventory( terminal.getInventory() );
	}

	/**
	 * Gets the current list from the AE monitor and sends
	 * it to the client.
	 */
	@Override
	public void forceAspectUpdate()
	{
		if ( this.monitor != null )
		{
			new PacketClientEssentiaTerminal( this.player, EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor
							.getStorageList() ) ).sendPacketToPlayer( this.player );
		}
	}

	/**
	 * Removes this container from the terminal.
	 */
	@Override
	public void onContainerClosed( EntityPlayer player )
	{
		super.onContainerClosed( player );

		if ( !player.worldObj.isRemote && ( this.terminal != null ) )
		{
			this.terminal.removeContainer( this );
		}
	}

	/**
	 * Updates the list of aspects, and sends that list to the client.
	 */
	@Override
	public void postChange( IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource source )
	{
		super.postChange( monitor, change, source );

		new PacketClientEssentiaTerminal( this.player, this.aspectStackList ).sendPacketToPlayer( this.player );
	}

	/**
	 * Updates the selected aspect and gui.
	 */
	@Override
	public void receiveSelectedAspect( Aspect selectedAspect )
	{	
		// Set the selected aspect
		this.selectedAspect = selectedAspect;
		
		// Is this client side?
		if ( this.player.worldObj.isRemote )
		{
			// Update the gui
			this.guiBase.updateSelectedAspect();
		}
		else
		{
			// Send the change back to the client
			new PacketClientEssentiaTerminal( this.player, this.selectedAspect ).sendPacketToPlayer( this.player );
		}
	}

	/**
	 * Called when the user has clicked on an aspect.
	 * Sends that change to the server for validation.
	 */
	@Override
	public void setSelectedAspect( Aspect selectedAspect )
	{
		new PacketServerEssentiaTerminal( this.player, selectedAspect ).sendPacketToServer();
	}
	
	/**
	 * Checks if there is any work to perform.
	 * If there is it does so.
	 */
	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		
		// Do we have a monitor
		if( this.monitor != null )
		{	

			// Can we lock the inventory?
			if( this.terminal.lockInventoryForWork() )
			{
				// Try block ensures lock gets released.
				try
				{
					// Do we have work to do?
					if( EssentiaCellTerminalWorker.hasWork( this.inventory ) )
					{
						// Do the work.
						EssentiaCellTerminalWorker.doWork( this.inventory, this.monitor, this.machineSource, this.selectedAspect );
					}
				}
				catch( Exception e )
				{
				}
				finally
				{
					// Release the lock.
					this.terminal.unlockInventory();
				}
			}
		}
	}

}
