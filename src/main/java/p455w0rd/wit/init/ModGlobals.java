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

import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.init.LibGlobals;

public class ModGlobals {

	public static final String MODID = "wit";
	public static final String VERSION = "1.0.0";
	public static final String NAME = "Wireless Interface Terminal";
	public static final String SERVER_PROXY = "p455w0rd.wit.proxy.CommonProxy";
	public static final String CLIENT_PROXY = "p455w0rd.wit.proxy.ClientProxy";
	public static final String DEP_LIST = LibGlobals.REQUIRE_DEP + "required-after:appliedenergistics2@[rv6-stable-6,);" + p455w0rdslib.LibGlobals.REQUIRE_DEP + "after:baubles;after:mousetweaks;after:itemscroller";
	public static final String CONFIG_FILE = WTApi.instance().getConfig().getConfigFile();

}
