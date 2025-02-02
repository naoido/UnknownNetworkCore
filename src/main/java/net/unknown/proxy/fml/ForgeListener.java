/*
 * Copyright (c) 2023 Unknown Network Developers and contributors.
 *
 * All rights reserved.
 *
 * NOTICE: This license is subject to change without prior notice.
 *
 * Redistribution and use in source and binary forms, *without modification*,
 *     are permitted provided that the following conditions are met:
 *
 * I. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 * II. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * III. Neither the name of Unknown Network nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 *
 * IV. This source code and binaries is provided by the copyright holders and contributors "AS-IS" and
 *     any express or implied warranties, including, but not limited to, the implied warranties of
 *     merchantability and fitness for a particular purpose are disclaimed.
 *     In not event shall the copyright owner or contributors be liable for
 *     any direct, indirect, incidental, special, exemplary, or consequential damages
 *     (including but not limited to procurement of substitute goods or services;
 *     loss of use data or profits; or business interruption) however caused and on any theory of liability,
 *     whether in contract, strict liability, or tort (including negligence or otherwise)
 *     arising in any way out of the use of this source code, event if advised of the possibility of such damage.
 */

package net.unknown.proxy.fml;

import com.ryuuta0217.packets.S2CModList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.LoginPayloadRequest;
import net.unknown.proxy.ModdedInitialHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Logger;

public class ForgeListener implements Listener {
    private static final Logger LOGGER = ProxyServer.getInstance().getLogger();
    private static final Random RANDOM = new Random();

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        if (!(event.getConnection() instanceof ModdedInitialHandler handler)) return;
        String logPrefix = "[" + handler.getName() + "|" + handler.getSocketAddress() + "]";

        if (handler.getHandshake().getProtocolVersion() >= ProtocolConstants.MINECRAFT_1_13 && handler.getExtraDataInHandshake().contains("FML2")) {
            LOGGER.info(logPrefix + "  -> Connected as using FML2 protocol");
            LOGGER.info(logPrefix + "  -  Initializing FML2 Handshake (S2CModList)");
            LoginPayloadRequest loginPayloadRequest = new LoginPayloadRequest();
            S2CModList packet = new S2CModList(new HashSet<>() {{
                add("minecraft");
                add("forge");
            }}, new HashMap<>() {{
                put("forge:tier_sorting", "1.0");
            }}, new HashSet<>());
            loginPayloadRequest.setData(ByteBufUtil.getBytes(packet.encode(Unpooled.buffer())));
            loginPayloadRequest.setChannel("fml:handshake");
            int id = RANDOM.nextInt(Short.MAX_VALUE);
            if (ModdedInitialHandler.PENDING_FORGE_PLAYER_CONNECTIONS.containsKey(id)) {
                LOGGER.info(logPrefix + "  -  FML2 handshake Message ID " + id + " is duplicated, re-generating.");
                while (ModdedInitialHandler.PENDING_FORGE_PLAYER_CONNECTIONS.containsKey(id)) {
                    id = RANDOM.nextInt(Integer.MAX_VALUE);
                    LOGGER.info(logPrefix + "  -  FML2 handshake new Message ID is " + id);
                }
                LOGGER.info(logPrefix + "  -  FML2 handshake Message ID duplicate is fixed.");
            } else {
                LOGGER.info(logPrefix + "  -  FML2 handshake Message ID is " + id);
            }
            ModdedInitialHandler.PENDING_FORGE_PLAYER_CONNECTIONS.put(id, event.getConnection());
            loginPayloadRequest.setId(id);
            event.getConnection().unsafe().sendPacket(loginPayloadRequest);
            LOGGER.info(logPrefix + " <-  LoginPayloadRequest (FML2 Handshake S2CModList) Sent");
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ModdedInitialHandler.FORGE_PLAYERS.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onConnectedToServer(ServerConnectedEvent event) {
        if (ModdedInitialHandler.FORGE_PLAYERS.containsKey(event.getPlayer().getName())) {
            ModdedPlayer fp = ModdedInitialHandler.FORGE_PLAYERS.get(event.getPlayer().getName());
            ByteBuf buf = Unpooled.buffer();
            fp.getData(buf, event.getPlayer().getUniqueId());
            event.getServer().sendData("unknown:forge", buf.array());
        }
    }
}
