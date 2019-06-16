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
package p455w0rd.wit.integration;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import mezz.jei.api.*;
import net.minecraft.item.ItemStack;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wit.init.ModItems;
import p455w0rdslib.LibGlobals.Mods;

/**
 * @author p455w0rd
 *
 */
@JEIPlugin
public class JEI implements IModPlugin {

	@SuppressWarnings("deprecation")
	@Override
	public void register(@Nonnull final IModRegistry registry) {
		final String witBaublesDescKey = Mods.BAUBLES.isLoaded() ? WTApi.instance().getConstants().getTooltips().jeiCanBeWorn() : "";
		registry.addIngredientInfo(Lists.newArrayList(new ItemStack(ModItems.WIT)), ItemStack.class, "jei.wit.desc", witBaublesDescKey);
	}

}
