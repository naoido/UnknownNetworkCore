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

package net.unknown.core.commands;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.unknown.core.enums.Permissions;
import net.unknown.core.util.MessageUtil;
import org.bukkit.ChatColor;

@SuppressWarnings("removal")
public class NickCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.literal("nick");
        builder.requires(Permissions.COMMAND_NICK::checkAndIsPlayer);
        builder.then(Commands.argument("nickName", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String nickName = StringArgumentType.getString(ctx, "nickName");
                    String coloredNickName = ChatColor.translateAlternateColorCodes('&', nickName);
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    if (nickName.length() <= 16) {
                        CraftPlayerProfile profile = (CraftPlayerProfile) player.getBukkitEntity().getPlayerProfile();
                        profile.setName(coloredNickName);
                        player.getBukkitEntity().setPlayerProfile(profile);
                    }

                    player.getBukkitEntity().setPlayerListName(coloredNickName);
                    player.getBukkitEntity().setDisplayName(coloredNickName);

                    MessageUtil.sendMessage(ctx.getSource(), "ニックネームを変更しました" + (nickName.length() > 16 ? " (Profileは変更されませんでした)" : ""));
                    return 0;
                }));
        dispatcher.register(builder);
    }
}
