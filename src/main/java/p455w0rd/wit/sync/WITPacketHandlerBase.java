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
package p455w0rd.wit.sync;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import p455w0rd.wit.sync.packets.*;

public class WITPacketHandlerBase {
	private static final Map<Class<? extends WITPacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<>();

	public enum PacketTypes {
			PACKET_COMPRESSED_NBT(PacketCompressedNBT.class),

			PACKET_INVENTORY_ACTION(PacketInventoryAction.class),

			PACKET_OPENWIRELESSTERM(PacketOpenGui.class);

		private final Class<? extends WITPacket> packetClass;
		private final Constructor<? extends WITPacket> packetConstructor;

		PacketTypes(final Class<? extends WITPacket> c) {
			packetClass = c;

			Constructor<? extends WITPacket> x = null;
			try {
				x = packetClass.getConstructor(ByteBuf.class);
			}
			catch (final NoSuchMethodException ignored) {
			}
			catch (final SecurityException ignored) {
			}
			catch (final DecoderException ignored) {
			}

			packetConstructor = x;
			REVERSE_LOOKUP.put(packetClass, this);

			if (packetConstructor == null) {
				throw new IllegalStateException("Invalid Packet Class " + c + ", must be constructable on DataInputStream");
			}
		}

		public static PacketTypes getPacket(final int id) {
			return values()[id];
		}

		static PacketTypes getID(final Class<? extends WITPacket> c) {
			return REVERSE_LOOKUP.get(c);
		}

		public WITPacket parsePacket(final ByteBuf in) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			return packetConstructor.newInstance(in);
		}
	}
}