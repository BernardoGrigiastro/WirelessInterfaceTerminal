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
package p455w0rd.wit.sync.packets;

import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.ae2wtlib.api.networking.INetworkInfo;
import p455w0rd.wit.container.ContainerWIT;
import p455w0rd.wit.sync.WITPacket;

/**
 * @author yueh
 *
 */
public class PacketTargetItemStack extends WITPacket {

	private AEItemStack stack;

	// automatic.
	public PacketTargetItemStack(final ByteBuf stream) {
		try {
			if (stream.readableBytes() > 0) {
				stack = AEItemStack.fromPacket(stream);
			}
			else {
				stack = null;
			}
		}
		catch (final Exception ex) {
			stack = null;
		}
	}

	// api
	public PacketTargetItemStack(final AEItemStack stack) {

		this.stack = stack;

		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		if (stack != null) {
			try {
				stack.writeToPacket(data);
			}
			catch (final Exception ex) {
			}
		}
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WITPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerWIT) {
			((ContainerWIT) player.openContainer).setTargetStack(stack);
		}
	}

}