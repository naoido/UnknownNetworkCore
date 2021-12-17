/*
 * Copyright (c) 2021 Unknown Network Developers and contributors.
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
 *     loss of use data or profits; or business interpution) however caused and on any theory of liability,
 *     whether in contract, strict liability, or tort (including negligence or otherwise)
 *     arising in any way out of the use of this source code, event if advised of the possibility of such damage.
 */

package net.unknown.survival.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.unknown.core.util.BrigadierUtil;
import net.unknown.survival.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.UUID;

public class BrigadierCommandSuggestions {
    public static final SuggestionProvider<CommandSourceStack> ALL_PLAYER_SUGGEST = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName), builder);
    };
    public static final SuggestionProvider<CommandSourceStack> HOME_SUGGEST = (ctx, builder) -> {
        ServerPlayer p = BrigadierUtil.getArgumentOrDefault(ctx, ServerPlayer.class, "対象", ctx.getSource().getPlayerOrException());
        return SharedSuggestionProvider.suggest(PlayerData.of(p.getBukkitEntity()).getHomeNames("uncategorized").toArray(new String[0]), builder);
    };
    public static final SuggestionProvider<CommandSourceStack> OFFLINE_HOME_SUGGEST = (ctx, builder) -> {
        try {
            String playerId = StringArgumentType.getString(ctx, "対象");
            UUID uniqueId = Bukkit.getPlayerUniqueId(playerId);
            if (uniqueId != null) {
                return SharedSuggestionProvider.suggest(PlayerData.of(uniqueId).getHomeNames("uncategorized").toArray(new String[0]), builder);
            }
        } catch (IllegalArgumentException ignored) {
        }

        return SharedSuggestionProvider.suggest(new String[0], builder);
    };
    public static final SuggestionProvider<CommandSourceStack> WORLD_SUGGEST = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(Bukkit.getWorlds().stream().map(World::getName), builder);
    };
}
