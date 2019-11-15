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
package p455w0rd.wit.init;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.relauncher.Side;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wit.WIT;
import p455w0rd.wit.api.IWirelessInterfaceTerminalItem;
import p455w0rd.wit.api.WITApi;
import p455w0rd.wit.sync.packets.PacketOpenGui;
import p455w0rd.wit.util.WITUtils;

/**
 * @author p455w0rd
 *
 */
public class ModAPIImpl extends WITApi {

	private static ModAPIImpl INSTANCE = null;

	public static ModAPIImpl instance() {
		if (ModAPIImpl.INSTANCE == null) {
			if (!ModAPIImpl.hasFinishedPreInit()) {
				return null;
			}
			ModAPIImpl.INSTANCE = new ModAPIImpl();
		}
		return INSTANCE;
	}

	protected static boolean hasFinishedPreInit() {
		if (WIT.PROXY.getLoaderState() == LoaderState.NOINIT) {
			ModLogger.warn("API is not available until WIT finishes the PreInit phase.");
			return false;
		}

		return true;
	}

	@Override
	public void openWITGui(final EntityPlayer player, final boolean isBauble, final int witSlot) {
		if (player == null || player instanceof FakePlayer || player instanceof EntityPlayerMP || FMLCommonHandler.instance().getSide() == Side.SERVER) {
			return;
		}
		final ItemStack is = isBauble ? WTApi.instance().getBaublesUtility().getWTBySlot(player, witSlot, IWirelessInterfaceTerminalItem.class) : WITUtils.getWITBySlot(player, witSlot);
		if (!is.isEmpty() && WTApi.instance().isTerminalLinked(is)) {
			ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_WIT, isBauble, witSlot));
		}
	}

}
