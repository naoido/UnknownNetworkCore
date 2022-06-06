/*
 * Copyright (c) 2022 Unknown Network Developers and contributors.
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

package net.unknown.core.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.ryuuta0217.util.HTTPUtil;
import net.unknown.UnknownNetworkCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class SkinManager implements Listener {
    private static final Logger LOGGER = Logger.getLogger("SkinManager");
    private static final Map<UUID, Map<String, Skin>> SAVED_SKINS = new HashMap<>();
    private static final Map<UUID, Skin> CACHED_SKINS = new HashMap<>();
    private static final Map<UUID, Skin> CURRENT_SKINS = new HashMap<>();

    public static void setSkin(Player player, Skin data) {
        PlayerProfile profile = player.getPlayerProfile();
        profile.getProperties().forEach(property -> {
            if (property.getName().equals("textures")) {
                cacheSkinData(player.getUniqueId(), new Skin(SkinSource.PLAYER_PROFILE, property.getValue(), property.getSignature()));
            }
        });
        profile.setProperty(data.asProfileProperty());
        player.setPlayerProfile(profile);
        CURRENT_SKINS.put(player.getUniqueId(), data);
    }

    public static void resetSkin(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        profile.setProperty(SkinManager.getSkinInRemote(player.getUniqueId()).asProfileProperty());
        player.setPlayerProfile(profile);
        CURRENT_SKINS.remove(player.getUniqueId());
    }

    public static Skin getSkin(UUID uniqueId) {
        return CACHED_SKINS.getOrDefault(uniqueId, SkinManager.getSkinInRemote(uniqueId));
    }

    @Nullable
    public static Skin getSkinInRemote(UUID uniqueId) {
        HTTPUtil profileInfo = new HTTPUtil("GET", "https://sessionserver.mojang.com/session/minecraft/profile/" + uniqueId + "?unsigned=false");
        try {
            JSONObject profileData = (JSONObject) UnknownNetworkCore.getJsonParser().parse(profileInfo.request());
            if (profileData.containsKey("properties")) {
                JSONArray properties = (JSONArray) profileData.get("properties");
                if (properties.size() > 0) {
                    JSONObject property = (JSONObject) properties.get(0);
                    String base64 = property.containsKey("value") ? property.get("value").toString() : "";
                    String signature = property.containsKey("signature") ? property.get("signature").toString() : null;
                    return cacheSkinData(uniqueId, new Skin(SkinSource.OFFICIAL, base64, signature));
                }
            }
            return null;
        } catch (ParseException e) {
            LOGGER.warning("Failed to parse retrieved profile data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void cacheSkinFromPlayerProfile(PlayerProfile profile) {
        profile.getProperties().forEach(property -> {
            if (property.getName().equals("textures")) {
                cacheSkinData(profile.getId(), new Skin(SkinSource.PLAYER_PROFILE, property.getValue(), property.getSignature()));
            }
        });
    }

    private static Skin cacheSkinData(UUID uniqueId, Skin data) {
        CACHED_SKINS.put(uniqueId, data);
        return data;
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (CURRENT_SKINS.containsKey(event.getUniqueId())) {
            PlayerProfile profile = event.getPlayerProfile();
            profile.setProperty(CURRENT_SKINS.get(event.getUniqueId()).asProfileProperty());
            event.setPlayerProfile(profile);
        }
    }

    public enum SkinSource {
        OFFICIAL,
        PLAYER_PROFILE,
        LOCAL,
        CUSTOM
    }

    public record Skin(SkinSource source, String base64, String signature) {
        public ProfileProperty asProfileProperty() {
            return new ProfileProperty("textures", base64, signature);
        }
    }
}