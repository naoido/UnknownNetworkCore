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

package net.unknown.survival.fun;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.unknown.UnknownNetworkCore;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DemolitionGun implements Listener {
    private static final int TICKS = 72000 - 80;

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player bukkitPlayer && event.getBow() != null) {
            ServerPlayer player = ((CraftPlayer) bukkitPlayer).getHandle();
            if (event.getBow().getItemMeta().getDisplayName().contains("§cデモリッションガン")) {
                if (event.getProjectile() instanceof Arrow arrow) {
                    if (player.getUseItemRemainingTicks() < TICKS) {
                        arrow.setVelocity(arrow.getVelocity().multiply(3));
                        arrow.setDamage(arrow.getDamage() * 6);
                        arrow.setGlowing(true);
                        arrow.setGravity(false);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!arrow.isDead() && !arrow.isOnGround()) {
                                    if (arrow.getTicksLived() > 150) arrow.setGravity(true);
                                    arrow.getLocation().getWorld().spawnParticle(Particle.REDSTONE, arrow.getLocation(), 15, new Particle.DustOptions(Color.AQUA, 1.5f));
                                } else {
                                    arrow.setGlowing(false);
                                    this.cancel();
                                }
                            }
                        }.runTaskTimerAsynchronously(UnknownNetworkCore.getInstance(), 0L, 1L);
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public static class BowPullIndicator extends BukkitRunnable {
        private static BowPullIndicator INSTANCE;
        private static BukkitTask TASK;
        private static final Map<UUID, BossBar> PROGRESS = new HashMap<>();

        private BowPullIndicator() {
            if (INSTANCE != null && !INSTANCE.isCancelled()) {
                INSTANCE.cancel();
            }

            INSTANCE = this;
        }

        public static BukkitTask boot() {
            TASK = new BowPullIndicator().runTaskTimerAsynchronously(UnknownNetworkCore.getInstance(), 0L, 1L);
            return TASK;
        }

        @Override
        public void run() {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getItemInUse() != null) {
                    if (player.getItemInUse().getType() == Material.BOW) {
                        if (!player.getItemInUse().getItemMeta().getDisplayName().contains("§cデモリッションガン")) return;

                        if (!PROGRESS.containsKey(player.getUniqueId())) {
                            PROGRESS.put(player.getUniqueId(), BossBar.bossBar(Component.text("..."), 0f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_6));
                        }

                        if (player.getItemUseRemainingTime() < 72000) {
                            int usedTicks = 72000 - player.getItemUseRemainingTime();
                            if (usedTicks > 80) usedTicks = 80;
                            float progress = (float) (usedTicks / 80.0);
                            PROGRESS.get(player.getUniqueId()).progress(progress);
                            player.showBossBar(PROGRESS.get(player.getUniqueId()));
                            return;
                        }
                    }
                }

                if (PROGRESS.containsKey(player.getUniqueId())) {
                    player.hideBossBar(PROGRESS.get(player.getUniqueId()));
                }
            });
        }
    }
}