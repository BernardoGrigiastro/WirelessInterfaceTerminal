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
package p455w0rd.wit.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import p455w0rd.ae2wtlib.api.client.IBaubleItem;
import p455w0rd.ae2wtlib.api.item.ItemWT;
import p455w0rd.wit.api.IWirelessInterfaceTerminalItem;
import p455w0rd.wit.api.WITApi;
import p455w0rd.wit.init.ModGlobals;

/**
 * @author p455w0rd
 *
 */
public class ItemWIT extends ItemWT implements IWirelessInterfaceTerminalItem, IBaubleItem {

	public ItemWIT() {
		this(new ResourceLocation(ModGlobals.MODID, "wit"));
	}

	public ItemWIT(final ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	public void openGui(final EntityPlayer player, final boolean isBauble, final int playerSlot) {
		WITApi.instance().openWITGui(player, isBauble, playerSlot);
	}

	@Override
	public ResourceLocation getMenuIcon() {
		return new ResourceLocation(ModGlobals.MODID, "textures/items/wit.png");
	}

}
