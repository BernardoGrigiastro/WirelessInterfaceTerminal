/*
 * This file is part of Wireless Interface Terminal. Copyright (c) 2017,
 * p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Interface Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Interface Terminal is distributed in the hope that it will be
 * useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Interface Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wit.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import appeng.api.config.*;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.*;
import appeng.items.misc.ItemEncodedPattern;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.*;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.*;
import appeng.util.inv.filter.IAEItemFilter;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.ae2wtlib.api.ICustomWirelessTerminalItem;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.api.container.ContainerWT;
import p455w0rd.wit.api.IWirelessInterfaceTerminalItem;
import p455w0rd.wit.init.ModNetworking;
import p455w0rd.wit.sync.packets.*;

/**
 * @author p455w0rd
 *
 */
public class ContainerWIT extends ContainerWT {

	private static long autoBase = Long.MIN_VALUE;
	private final Map<IInterfaceHost, InvTracker> diList = new HashMap<>();
	private final Map<Long, InvTracker> byId = new HashMap<>();
	private IGrid grid;
	private NBTTagCompound data = new NBTTagCompound();
	private IAEItemStack clientRequestedTargetItem = null;

	public ContainerWIT(final EntityPlayer player, final ITerminalHost hostIn, final int slot, final boolean isBauble) {
		super(player.inventory, getActionHost(getGuiObject(isBauble ? WTApi.instance().getBaublesUtility().getWTBySlot(player, slot, IWirelessInterfaceTerminalItem.class) : WTApi.instance().getWTBySlot(player, slot), player)), slot, isBauble, true, 152, 120);
		setCustomName("WITContainer");
		setTerminalHost(hostIn);
		initConfig(setClientConfigManager(new ConfigManager(this)));
		if (Platform.isServer()) {
			setServerConfigManager(getGuiObject().getConfigManager());
			if (getGuiObject() == null || getGuiObject().getActionableNode() == null || getGuiObject().getActionableNode().getGrid() == null) {
				setValidContainer(false);
			}
			else {
				grid = getGuiObject().getActionableNode().getGrid();
			}
		}

		bindPlayerInventory(player.inventory, 8, 222 - /* height of player inventory */82);
		//bindPlayerInventory(player.inventory, 8, 222 - 90);
		readNBT();
	}

	/*
	@SuppressWarnings("unchecked")
	public static WTGuiObject<IAEFluidStack> getGuiObject(final ItemStack it, final EntityPlayer player) {
		if (!it.isEmpty()) {
			final IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh instanceof ICustomWirelessTerminalItem) {
				return (WTGuiObject<IAEFluidStack>) WTApi.instance().getGUIObject((ICustomWirelessTerminalItem) wh, it, player);
			}
		}
		return null;
	}
	*/
	@Override
	public ItemStack slotClick(final int slot, final int dragType, final ClickType clickTypeIn, final EntityPlayer player) {
		ItemStack returnStack = ItemStack.EMPTY;
		try {
			returnStack = super.slotClick(slot, dragType, clickTypeIn, player);
		}
		catch (final IndexOutOfBoundsException e) {
		}
		writeToNBT();
		detectAndSendChanges();
		return returnStack;
	}

	@Override
	public void setTargetStack(final IAEItemStack stack) {
		if (Platform.isClient()) {
			if (stack == null && clientRequestedTargetItem == null) {
				return;
			}
			if (stack != null && stack.isSameType(clientRequestedTargetItem)) {
				return;
			}

			ModNetworking.instance().sendToServer(new PacketTargetItemStack((AEItemStack) stack));
		}

		clientRequestedTargetItem = stack == null ? null : stack.copy();
	}

	@Override
	public void detectAndSendChanges() {
		if (Platform.isServer()) {

			if (getGuiObject() != null) {
				if (getWirelessTerminal() != getGuiObject().getItemStack()) {
					if (!getWirelessTerminal().isEmpty()) {
						if (ItemStack.areItemsEqual(getGuiObject().getItemStack(), getWirelessTerminal())) {
							getPlayerInv().setInventorySlotContents(getPlayerInv().currentItem, getGuiObject().getItemStack());
						}
						else {
							setValidContainer(false);
						}
					}
					else {
						setValidContainer(false);
					}
				}
			}
			else {
				setValidContainer(false);
			}

			super.detectAndSendChanges();

			//===
			if (grid == null) {
				return;
			}

			int total = 0;
			boolean missing = false;

			final IActionHost host = getGuiObject();
			if (host != null) {
				final IGridNode agn = host.getActionableNode();
				if (agn != null && agn.isActive()) {
					for (final IGridNode gn : grid.getMachines(TileInterface.class)) {
						if (gn.isActive()) {
							final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
							if (ih.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
								continue;
							}

							final InvTracker t = diList.get(ih);

							if (t == null) {
								missing = true;
							}
							else {
								final DualityInterface dual = ih.getInterfaceDuality();
								if (!t.unlocalizedName.equals(dual.getTermName())) {
									missing = true;
								}
							}

							total++;
						}
					}

					for (final IGridNode gn : grid.getMachines(PartInterface.class)) {
						if (gn.isActive()) {
							final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
							if (ih.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
								continue;
							}

							final InvTracker t = diList.get(ih);

							if (t == null) {
								missing = true;
							}
							else {
								final DualityInterface dual = ih.getInterfaceDuality();
								if (!t.unlocalizedName.equals(dual.getTermName())) {
									missing = true;
								}
							}

							total++;
						}
					}
				}
			}

			if (total != diList.size() || missing) {
				regenList(data);
			}
			else {
				for (final Entry<IInterfaceHost, InvTracker> en : diList.entrySet()) {
					final InvTracker inv = en.getValue();
					for (int x = 0; x < inv.server.getSlots(); x++) {
						if (isDifferent(inv.server.getStackInSlot(x), inv.client.getStackInSlot(x))) {
							addItems(data, inv, x, 1);
						}
					}
				}
			}

			if (!data.hasNoTags()) {
				try {
					ModNetworking.instance().sendTo(new PacketCompressedNBT(data), (EntityPlayerMP) getPlayerInv().player);
				}
				catch (final IOException e) {
					// :P
				}

				data = new NBTTagCompound();
			}
			//===

			if (!isInRange()) {
				if (!hasInfiniteRange()) {
					if (isValidContainer()) {
						getPlayer().sendMessage(PlayerMessages.OutOfRange.get());
					}
					setValidContainer(false);
				}
				if (!networkIsPowered()) {
					if (isValidContainer()) {
						getPlayer().sendMessage(new TextComponentString("No Network Power"));
					}
					setValidContainer(false);
				}
			}
			else if (!hasAccess(SecurityPermissions.CRAFT, true) || !hasAccess(SecurityPermissions.EXTRACT, true) || !hasAccess(SecurityPermissions.INJECT, true)) {
				if (isValidContainer()) {
					getPlayer().sendMessage(PlayerMessages.CommunicationError.get());
				}
				setValidContainer(false);
			}
			if (getWirelessTerminal().getItem() instanceof IWirelessInterfaceTerminalItem && ((IWirelessInterfaceTerminalItem) getWirelessTerminal().getItem()).getAECurrentPower(getWirelessTerminal()) <= 0) {
				if (isValidContainer()) {
					getPlayer().sendMessage(new TextComponentString("No Power"));
				}
				setValidContainer(false);
			}
		}
	}

	@Override
	public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
		final InvTracker inv = byId.get(id);
		if (inv != null) {
			final ItemStack is = inv.server.getStackInSlot(slot);
			final boolean hasItemInHand = !player.inventory.getItemStack().isEmpty();

			final InventoryAdaptor playerHand = new AdaptorItemHandler(new WrapperCursorItemHandler(player.inventory));

			final IItemHandler theSlot = new WrapperFilteredItemHandler(new WrapperRangeItemHandler(inv.server, slot, slot + 1), new PatternSlotFilter());
			final InventoryAdaptor interfaceSlot = new AdaptorItemHandler(theSlot);

			switch (action) {
			case PICKUP_OR_SET_DOWN:

				if (hasItemInHand) {
					ItemStack inSlot = theSlot.getStackInSlot(0);
					if (inSlot.isEmpty()) {
						player.inventory.setItemStack(interfaceSlot.addItems(player.inventory.getItemStack()));
					}
					else {
						inSlot = inSlot.copy();
						final ItemStack inHand = player.inventory.getItemStack().copy();

						ItemHandlerUtil.setStackInSlot(theSlot, 0, ItemStack.EMPTY);
						player.inventory.setItemStack(ItemStack.EMPTY);

						player.inventory.setItemStack(interfaceSlot.addItems(inHand.copy()));

						if (player.inventory.getItemStack().isEmpty()) {
							player.inventory.setItemStack(inSlot);
						}
						else {
							player.inventory.setItemStack(inHand);
							ItemHandlerUtil.setStackInSlot(theSlot, 0, inSlot);
						}
					}
				}
				else {
					ItemHandlerUtil.setStackInSlot(theSlot, 0, playerHand.addItems(theSlot.getStackInSlot(0)));
				}

				break;
			case SPLIT_OR_PLACE_SINGLE:

				if (hasItemInHand) {
					ItemStack extra = playerHand.removeItems(1, ItemStack.EMPTY, null);
					if (!extra.isEmpty()) {
						extra = interfaceSlot.addItems(extra);
					}
					if (!extra.isEmpty()) {
						playerHand.addItems(extra);
					}
				}
				else if (!is.isEmpty()) {
					ItemStack extra = interfaceSlot.removeItems((is.getCount() + 1) / 2, ItemStack.EMPTY, null);
					if (!extra.isEmpty()) {
						extra = playerHand.addItems(extra);
					}
					if (!extra.isEmpty()) {
						interfaceSlot.addItems(extra);
					}
				}

				break;
			case SHIFT_CLICK:

				final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(player);

				ItemHandlerUtil.setStackInSlot(theSlot, 0, playerInv.addItems(theSlot.getStackInSlot(0)));

				break;
			case MOVE_REGION:

				final InventoryAdaptor playerInvAd = InventoryAdaptor.getAdaptor(player);
				for (int x = 0; x < inv.server.getSlots(); x++) {
					ItemHandlerUtil.setStackInSlot(inv.server, x, playerInvAd.addItems(inv.server.getStackInSlot(x)));
				}

				break;
			case CREATIVE_DUPLICATE:

				if (player.capabilities.isCreativeMode && !hasItemInHand) {
					player.inventory.setItemStack(is.isEmpty() ? ItemStack.EMPTY : is.copy());
				}

				break;
			default:
				return;
			}
			updateHeld(player);
		}
	}

	@Override
	protected void updateHeld(final EntityPlayerMP p) {
		if (Platform.isServer()) {
			try {
				ModNetworking.instance().sendTo(new PacketInventoryAction(InventoryAction.UPDATE_HAND, 0, AEItemStack.fromItemStack(p.inventory.getItemStack())), p);
			}
			catch (final IOException e) {
			}
		}
	}

	public boolean isPowered() {
		final double pwr = ((ICustomWirelessTerminalItem) getWirelessTerminal().getItem()).getAECurrentPower(getWirelessTerminal());
		return pwr > 0.0;
	}

	@Override
	public void saveChanges() {
	}

	@Override
	public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
	}

	private void regenList(final NBTTagCompound data) {
		byId.clear();
		diList.clear();

		final IActionHost host = getGuiObject();
		if (host != null) {
			final IGridNode agn = host.getActionableNode();
			if (agn != null && agn.isActive()) {
				for (final IGridNode gn : grid.getMachines(TileInterface.class)) {
					final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
					final DualityInterface dual = ih.getInterfaceDuality();
					if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
						diList.put(ih, new InvTracker(dual, dual.getPatterns(), dual.getTermName()));
					}
				}

				for (final IGridNode gn : grid.getMachines(PartInterface.class)) {
					final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
					final DualityInterface dual = ih.getInterfaceDuality();
					if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
						diList.put(ih, new InvTracker(dual, dual.getPatterns(), dual.getTermName()));
					}
				}
			}
		}

		data.setBoolean("clear", true);

		for (final Entry<IInterfaceHost, InvTracker> en : diList.entrySet()) {
			final InvTracker inv = en.getValue();
			byId.put(inv.which, inv);
			addItems(data, inv, 0, inv.server.getSlots());
		}
	}

	private boolean isDifferent(final ItemStack a, final ItemStack b) {
		if (a.isEmpty() && b.isEmpty()) {
			return false;
		}

		if (a.isEmpty() || b.isEmpty()) {
			return true;
		}

		return !ItemStack.areItemStacksEqual(a, b);
	}

	private void addItems(final NBTTagCompound data, final InvTracker inv, final int offset, final int length) {
		final String name = '=' + Long.toString(inv.which, Character.MAX_RADIX);
		final NBTTagCompound tag = data.getCompoundTag(name);

		if (tag.hasNoTags()) {
			tag.setLong("sortBy", inv.sortBy);
			tag.setString("un", inv.unlocalizedName);
		}

		for (int x = 0; x < length; x++) {
			final NBTTagCompound itemNBT = new NBTTagCompound();

			final ItemStack is = inv.server.getStackInSlot(x + offset);

			// "update" client side.
			ItemHandlerUtil.setStackInSlot(inv.client, x + offset, is.isEmpty() ? ItemStack.EMPTY : is.copy());

			if (!is.isEmpty()) {
				is.writeToNBT(itemNBT);
			}

			tag.setTag(Integer.toString(x + offset), itemNBT);
		}

		data.setTag(name, tag);
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
		return ItemStack.EMPTY;
	}

	private static class InvTracker {

		private final long sortBy;
		private final long which = autoBase++;
		private final String unlocalizedName;
		private final IItemHandler client;
		private final IItemHandler server;

		public InvTracker(final DualityInterface dual, final IItemHandler patterns, final String unlocalizedName) {
			server = patterns;
			client = new AppEngInternalInventory(null, server.getSlots());
			this.unlocalizedName = unlocalizedName;
			sortBy = dual.getSortValue();
		}
	}

	private static class PatternSlotFilter implements IAEItemFilter {
		@Override
		public boolean allowExtract(final IItemHandler inv, final int slot, final int amount) {
			return true;
		}

		@Override
		public boolean allowInsert(final IItemHandler inv, final int slot, final ItemStack stack) {
			return !stack.isEmpty() && stack.getItem() instanceof ItemEncodedPattern;
		}
	}

}
