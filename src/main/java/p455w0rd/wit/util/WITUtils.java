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
package p455w0rd.wit.util;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.ICustomWirelessTerminalItem;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wit.api.IWirelessInterfaceTerminalItem;
import p455w0rd.wit.api.WITApi;
import p455w0rd.wit.container.ContainerWIT;
import p455w0rd.wit.init.ModKeybindings;
import p455w0rdslib.LibGlobals.Mods;

public class WITUtils {

	public static final String INFINITY_ENERGY_NBT = "InfinityEnergy";
	public static final String BOOSTER_SLOT_NBT = "BoosterSlot";
	public static final String IN_RANGE_NBT = "IsInRange";
	public static final String AUTOCONSUME_BOOSTER_NBT = "AutoConsumeBoosters";

	public static NonNullList<ItemStack> getInterfaceTerminals(final EntityPlayer player) {
		final NonNullList<ItemStack> terminalList = NonNullList.<ItemStack>create();
		final InventoryPlayer playerInventory = player.inventory;
		for (final ItemStack fluidTerm : playerInventory.mainInventory) {
			if (isAnyWIT(fluidTerm)) {
				terminalList.add(fluidTerm);
			}
		}
		if (Mods.BAUBLES.isLoaded()) {
			final Set<Pair<Integer, ItemStack>> pairSet = WTApi.instance().getBaublesUtility().getAllWTBaublesByType(player, IWirelessInterfaceTerminalItem.class);
			for (final Pair<Integer, ItemStack> pair : pairSet) {
				terminalList.add(pair.getRight());
			}
		}
		return terminalList;
	}

	@Nonnull
	public static ItemStack getInterfaceTerm(final InventoryPlayer playerInv) {
		if (!playerInv.player.getHeldItemMainhand().isEmpty() && (playerInv.player.getHeldItemMainhand().getItem() instanceof IWirelessInterfaceTerminalItem || WTApi.instance().getWUTUtility().doesWUTSupportType(playerInv.player.getHeldItemMainhand(), IWirelessInterfaceTerminalItem.class))) {
			return playerInv.player.getHeldItemMainhand();
		}
		ItemStack fluidTerm = ItemStack.EMPTY;
		if (Mods.BAUBLES.isLoaded()) {
			final List<Pair<Integer, ItemStack>> baubleList = Lists.newArrayList(WTApi.instance().getBaublesUtility().getAllWTBaublesByType(playerInv.player, IWirelessInterfaceTerminalItem.class));
			if (baubleList.size() > 0) {
				fluidTerm = baubleList.get(0).getRight();
			}
		}
		if (fluidTerm.isEmpty()) {
			final int invSize = playerInv.getSizeInventory();
			if (invSize <= 0) {
				return ItemStack.EMPTY;
			}
			for (int i = 0; i < invSize; ++i) {
				final ItemStack item = playerInv.getStackInSlot(i);
				if (item.isEmpty()) {
					continue;
				}
				if (item.getItem() instanceof IWirelessInterfaceTerminalItem || WTApi.instance().getWUTUtility().doesWUTSupportType(item, IWirelessInterfaceTerminalItem.class)) {
					fluidTerm = item;
					break;
				}
			}
		}
		return fluidTerm;
	}

	public static ItemStack getWITBySlot(final EntityPlayer player, final int slot) {
		if (slot >= 0) {
			return WTApi.instance().getWTBySlot(player, slot, IWirelessInterfaceTerminalItem.class);
		}
		return ItemStack.EMPTY;
	}

	/**
	 * gets the first available Wireless Interface Terminal
	 * the Integer of the Pair tells the slotNumber
	 * the boolean tells whether or not the Integer is a Baubles slot
	 */
	@Nonnull
	public static Pair<Boolean, Pair<Integer, ItemStack>> getFirstWirelessInterfaceTerminal(final InventoryPlayer playerInv) {
		boolean isBauble = false;
		int slotID = -1;
		ItemStack wirelessTerm = ItemStack.EMPTY;
		if (!playerInv.player.getHeldItemMainhand().isEmpty() && (playerInv.player.getHeldItemMainhand().getItem() instanceof IWirelessInterfaceTerminalItem || WTApi.instance().getWUTUtility().doesWUTSupportType(playerInv.player.getHeldItemMainhand(), IWirelessInterfaceTerminalItem.class))) {
			slotID = playerInv.currentItem;
			wirelessTerm = playerInv.player.getHeldItemMainhand();
		}
		else {
			if (Mods.BAUBLES.isLoaded()) {
				final Pair<Integer, ItemStack> bauble = WTApi.instance().getBaublesUtility().getFirstWTBaubleByType(playerInv.player, IWirelessInterfaceTerminalItem.class);
				if (!bauble.getRight().isEmpty()) {
					wirelessTerm = bauble.getRight();
					slotID = bauble.getLeft();
					if (!wirelessTerm.isEmpty()) {
						isBauble = true;
					}
				}
			}
			if (wirelessTerm.isEmpty()) {
				final int invSize = playerInv.getSizeInventory();
				if (invSize > 0) {
					for (int i = 0; i < invSize; ++i) {
						final ItemStack item = playerInv.getStackInSlot(i);
						if (item.isEmpty()) {
							continue;
						}
						if (item.getItem() instanceof IWirelessInterfaceTerminalItem || WTApi.instance().getWUTUtility().doesWUTSupportType(item, IWirelessInterfaceTerminalItem.class)) {
							wirelessTerm = item;
							slotID = i;
							break;
						}
					}
				}
			}
		}
		return Pair.of(isBauble, Pair.of(slotID, wirelessTerm));
	}

	public static boolean isAnyWIT(@Nonnull final ItemStack interfaceTerm) {
		return interfaceTerm.getItem() instanceof IWirelessInterfaceTerminalItem || WTApi.instance().getWUTUtility().doesWUTSupportType(interfaceTerm, IWirelessInterfaceTerminalItem.class);
	}

	public static boolean isWITCreative(final ItemStack interfaceTerm) {
		return !interfaceTerm.isEmpty() && ((ICustomWirelessTerminalItem) interfaceTerm.getItem()).isCreative();
	}

	@SideOnly(Side.CLIENT)
	public static EntityPlayer player() {
		return Minecraft.getMinecraft().player;
	}

	public static EntityPlayer player(final InventoryPlayer playerInv) {
		return playerInv.player;
	}

	@SideOnly(Side.CLIENT)
	public static World world() {
		return Minecraft.getMinecraft().world;
	}

	public static World world(final EntityPlayer player) {
		return player.getEntityWorld();
	}

	public static void chatMessage(final EntityPlayer player, final ITextComponent message) {
		player.sendMessage(message);
	}

	@SideOnly(Side.CLIENT)
	public static void handleKeybind() {
		final EntityPlayer p = WITUtils.player();
		if (p.openContainer == null) {
			return;
		}
		if (ModKeybindings.openInterfaceTerminal.getKeyCode() != Keyboard.CHAR_NONE && ModKeybindings.openInterfaceTerminal.isPressed()) {
			final ItemStack is = WITUtils.getInterfaceTerm(p.inventory);
			if (is.isEmpty()) {
				return;
			}
			final ICustomWirelessTerminalItem interfaceTerm = (ICustomWirelessTerminalItem) is.getItem();
			if (interfaceTerm != null) {
				if (!(p.openContainer instanceof ContainerWIT)) {
					final Pair<Boolean, Pair<Integer, ItemStack>> witPair = WITUtils.getFirstWirelessInterfaceTerminal(p.inventory);
					WITApi.instance().openWITGui(p, witPair.getLeft(), witPair.getRight().getLeft());
					//ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_WIT));
				}
				else {
					p.closeScreen();
				}
			}
		}
	}

}
