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

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wit.init.ModGlobals;
import p455w0rd.wit.init.ModItems;

/**
 * @author p455w0rd
 *
 */
public class ItemWITCreative extends ItemWIT {

	public ItemWITCreative() {
		super(new ResourceLocation(ModGlobals.MODID, "wit_creative"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, getModelResource(this));
	}

	@Override
	public ModelResourceLocation getModelResource(final Item item) {
		return new ModelResourceLocation(ModItems.WIT.getRegistryName(), "inventory");
	}

	@Override
	public double getAECurrentPower(final ItemStack wirelessTerm) {
		return WTApi.instance().getConfig().getWTMaxPower();
	}

	@Override
	public EnumRarity getRarity(final ItemStack wirelessTerm) {
		return EnumRarity.RARE;
	}

	@Override
	public boolean isCreative() {
		return true;
	}

}
