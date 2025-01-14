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

package net.unknown.survival.commands.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.unknown.core.util.MessageUtil;
import net.unknown.core.util.MinecraftAdapter;
import net.unknown.survival.data.PlayerData;
import net.unknown.survival.data.model.Home;
import net.unknown.survival.data.model.HomeGroup;
import net.unknown.survival.enums.Permissions;
import org.bukkit.Location;

// /minecraft:sethome - send usage
// /minecraft:sethome <String: 新規ホーム名> [Boolean: 上書き]
public class SetHomeCommand {
    public static final int DEFAULT_MAXIMUM_HOME_COUNT = 3;
    public static final int REGULAR_MAXIMUM_HOME_COUNT = 5;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.literal("sethome");
        builder.requires(Permissions.COMMAND_SETHOME::checkAndIsPlayer);

        builder.then(Commands.argument("新規ホーム名", StringArgumentType.string())
                .executes(ctx -> SetHomeCommand.setHome(ctx, false))
                .then(Commands.argument("上書き", BoolArgumentType.bool())
                        .executes(ctx -> SetHomeCommand.setHome(ctx, BoolArgumentType.getBool(ctx, "上書き")))));

        dispatcher.register(builder);
    }

    private static int setHome(CommandContext<CommandSourceStack> ctx, boolean overwrite) throws CommandSyntaxException {
        String newHomeName = StringArgumentType.getString(ctx, "新規ホーム名");

        PlayerData.HomeData data = PlayerData.of(ctx.getSource().getPlayerOrException().getUUID()).getHomeData();
        int homeCount = data.getDefaultGroup().getHomes().size(); // TODO グループごとにホームの数を決めるか、全体のホーム数にするか
        int maxHomeCount = data.getMaxHomeCount();
        HomeGroup group = data.getDefaultGroup();

        boolean isHomeExists = group.hasHome(newHomeName);
        if ((!overwrite || !isHomeExists) && maxHomeCount != -1) {
            /*if (maxHomeCount == REGULAR_MAXIMUM_HOME_COUNT && Permissions.REGULAR.check(ctx) && homeCount >= maxHomeCount) {
                MessageUtil.sendErrorMessage(ctx.getSource(), "常連プレイヤーが設定できるホームは" + REGULAR_MAXIMUM_HOME_COUNT + "個までです。\n" +
                        "/delhome <ホーム名> するか、ショップで追加券を購入してください。");
                return REGULAR_MAXIMUM_HOME_COUNT;
            } else if (maxHomeCount == DEFAULT_MAXIMUM_HOME_COUNT && Permissions.DEFAULT.check(ctx) && homeCount >= maxHomeCount) {
                MessageUtil.sendErrorMessage(ctx.getSource(), "一般プレイヤーが設定できるホームは" + DEFAULT_MAXIMUM_HOME_COUNT + "個までです。\n" +
                        "/delhome <ホーム名> するか、ショップで追加券を購入してください。");
                return DEFAULT_MAXIMUM_HOME_COUNT;
            } else */
            if (homeCount >= maxHomeCount) {
                MessageUtil.sendErrorMessage(ctx.getSource(), "設定可能なホームは" + maxHomeCount + "個までです。\n" +
                        "/delhome <ホーム名> してください。");
                return maxHomeCount;
            }
        }

        if (maxHomeCount == 0) {
            MessageUtil.sendErrorMessage(ctx.getSource(), "何らかのバグが発生しています。運営にご連絡ください。");
            return 0;
        }

        if (!isHomeExists || overwrite) {
            Location loc = MinecraftAdapter.location(ctx.getSource().getLevel(), ctx.getSource().getPosition(), ctx.getSource().getRotation());;
            group.addHome(new Home(newHomeName, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()), overwrite);
            MessageUtil.sendMessage(ctx.getSource(), "グループ " + data.getDefaultGroup().getName() + " にホーム " + newHomeName + " を設定しました");
        } else {
            MessageUtil.sendErrorMessage(ctx.getSource(), "ホーム " + newHomeName + " はグループ " + data.getDefaultGroup() + " に既に設定されています。\n" +
                    "/delhome " + newHomeName + "で削除するか、/" + ctx.getInput().replaceAll(" false$", "") + " true で上書きするか、別の名前を検討してください。");
        }
        return 1;
    }
}
