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

package net.unknown.survival.fun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.unknown.core.managers.ListenerManager;
import net.unknown.core.util.MinecraftAdapter;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashSet;
import java.util.Set;

public class MonsterBall implements Listener {
    private static final Set<EntityType<?>> RESTRICTED_ENTITY_TYPES = new HashSet<>() {{
        add(EntityType.ENDER_DRAGON);
        add(EntityType.WITHER);
    }};

    @EventHandler
    public void onEggHit(ProjectileHitEvent event) {
        if (((CraftEntity) event.getEntity()).getHandle() instanceof ThrownEgg egg) {
            org.bukkit.inventory.ItemStack thrownEggItem = MinecraftAdapter.ItemStack.itemStack(egg.getItemRaw());
            if (thrownEggItem.hasItemMeta() && thrownEggItem.getItemMeta().hasDisplayName() && thrownEggItem.getItemMeta().displayName().equals(Component.text("モンスターボール", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))) {
                if (event.getHitEntity() != null) {
                    if (((CraftEntity) event.getHitEntity()).getHandle() instanceof Mob mob) {
                        if (SpawnEggItem.byId(mob.getType()) != null && !RESTRICTED_ENTITY_TYPES.contains(mob.getType())) {
                            CompoundTag entityTag = new CompoundTag();
                            mob.save(entityTag);
                            mob.remove(Entity.RemovalReason.DISCARDED);
                            entityTag.remove("Pos");
                            entityTag.remove("Motion");
                            entityTag.remove("Rotation");

                            ItemStack spawnEgg = new ItemStack(SpawnEggItem.byId(mob.getType()));
                            CompoundTag spawnEggTag = new CompoundTag();
                            spawnEggTag.put("EntityTag", entityTag);
                            spawnEgg.setTag(spawnEggTag);

                            ItemEntity e = new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), spawnEgg);
                            mob.level().addFreshEntity(e, CreatureSpawnEvent.SpawnReason.EGG);
                            mob.kill();
                            mob.level().explode(null, mob.getX(), mob.getY(), mob.getZ(), 0.1F, Level.ExplosionInteraction.NONE);
                        }
                    }
                }
                event.setCancelled(true);
                ListenerManager.waitForEvent(CreatureSpawnEvent.class, false, EventPriority.NORMAL, (e) -> {
                    return e.getLocation().getWorld().equals(event.getEntity().getLocation().getWorld()) && e.getLocation().distance(event.getEntity().getLocation()) < 15 && e.getEntityType() == org.bukkit.entity.EntityType.CHICKEN && e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG;
                }, (e) -> {
                    e.setCancelled(true);
                }, 1, ListenerManager.TimeType.SECONDS, () -> {});
            }
        }
    }
}
