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

package net.unknown.survival;

import net.milkbowl.vault.economy.Economy;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.unknown.UnknownNetworkCore;
import net.unknown.survival.antivillagerlag.AntiVillagerLag;
import net.unknown.survival.chat.ChatManager;
import net.unknown.survival.commands.Commands;
import net.unknown.survival.data.PlayerData;
import net.unknown.survival.enchants.HatakeWatari;
import net.unknown.survival.enchants.RangedMining;
import net.unknown.survival.fml.FMLConnectionListener;
import net.unknown.survival.fml.ModdedPlayerManager;
import net.unknown.survival.fun.DemolitionGun;
import net.unknown.survival.fun.PathfinderGrapple;
import net.unknown.survival.listeners.ColorCodeListener;
import net.unknown.survival.listeners.MainGuiOpenListener;
import net.unknown.survival.wrapper.economy.WrappedEconomy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import java.util.logging.Logger;

public class UnknownNetworkSurvival {
    private static final Logger LOGGER = Logger.getLogger("UNC/Survival");
    private static boolean HOLOGRAPHIC_DISPLAYS_ENABLED = false;
    private static boolean WORLD_GUARD_ENABLED = false;
    private static boolean VAULT_ENABLED = false;
    private static boolean JECON_ENABLED = false;

    public static void onLoad() {
        Commands.init();
    }

    public static void onEnable() {
        HOLOGRAPHIC_DISPLAYS_ENABLED = Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null && Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
        WORLD_GUARD_ENABLED = Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
        VAULT_ENABLED = Bukkit.getPluginManager().getPlugin("Vault") != null && Bukkit.getPluginManager().isPluginEnabled("Vault");
        JECON_ENABLED = Bukkit.getPluginManager().getPlugin("Jecon") != null && Bukkit.getPluginManager().isPluginEnabled("Jecon");

        PlayerData.loadExists();
        AntiVillagerLag.startLoopTask();

        Bukkit.getPluginManager().registerEvents(new MainGuiOpenListener(), UnknownNetworkCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new ChatManager(), UnknownNetworkCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new ColorCodeListener(), UnknownNetworkCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new ModdedPlayerManager(), UnknownNetworkCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new PathfinderGrapple(), UnknownNetworkCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new DemolitionGun(), UnknownNetworkCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new HatakeWatari(), UnknownNetworkCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new RangedMining(), UnknownNetworkCore.getInstance());

        Bukkit.getMessenger().registerIncomingPluginChannel(UnknownNetworkCore.getInstance(), "unknown:forge", new FMLConnectionListener());

        DemolitionGun.BowPullIndicator.boot();

        if (isVaultEnabled() && isJeconEnabled()) {
            WrappedEconomy wrapped = new WrappedEconomy(Bukkit.getServicesManager().getRegistration(Economy.class).getProvider());
            Bukkit.getServicesManager().register(Economy.class, wrapped, UnknownNetworkCore.getInstance(), ServicePriority.Highest);
        }
        LOGGER.info("Plugin enabled - Running as Survival mode.");
    }

    public static void onDisable() {

    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static boolean isHolographicDisplaysEnabled() {
        return HOLOGRAPHIC_DISPLAYS_ENABLED;
    }

    public static boolean isWorldGuardEnabled() {
        return WORLD_GUARD_ENABLED;
    }

    public static boolean isVaultEnabled() {
        return VAULT_ENABLED;
    }

    public static boolean isJeconEnabled() {
        return JECON_ENABLED;
    }
}
