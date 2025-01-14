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

package net.unknown.survival.commands.home.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.unknown.core.commands.Suggestions;
import net.unknown.core.util.MessageUtil;
import net.unknown.survival.commands.home.HomesCommand;
import net.unknown.survival.enums.Permissions;
import org.bukkit.Bukkit;

import java.util.UUID;

public class AHomesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.literal("ahomes");
        builder.requires(Permissions.COMMAND_AHOMES::check);
        builder.then(Commands.argument("対象", StringArgumentType.word())
                .suggests(Suggestions.ALL_PLAYER_SUGGEST)
                .executes(AHomesCommand::sendHomeList)
                .then(Commands.argument("ページ", IntegerArgumentType.integer(1))
                        .executes(AHomesCommand::sendHomeList)));
        dispatcher.register(builder);
    }

    public static int sendHomeList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String inputPlayerId = StringArgumentType.getString(ctx, "対象");
        UUID playerUniqueId = Bukkit.getPlayerUniqueId(inputPlayerId);

        if (playerUniqueId == null) {
            MessageUtil.sendAdminErrorMessage(ctx.getSource(), "プレイヤー " + inputPlayerId + " は存在しません");
            return 0;
        }

        return HomesCommand.sendHomeList(ctx, playerUniqueId);
    }
}
