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

package net.unknown.survival.gui.hopper.view;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.unknown.core.gui.view.PaginationView;
import net.unknown.core.util.MinecraftAdapter;
import net.unknown.core.util.NewMessageUtil;
import net.unknown.launchwrapper.hopper.Filter;
import net.unknown.launchwrapper.hopper.ItemFilter;
import net.unknown.launchwrapper.hopper.TagFilter;
import net.unknown.survival.gui.hopper.ConfigureHopperGui;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Iterator;

public class FiltersView extends PaginationView<Filter, ConfigureHopperGui> {
    private final ConfigureHopperViewBase parentView;

    public FiltersView(ConfigureHopperViewBase parentView) {
        super(parentView.getGui(), parentView.getGui().getMixinHopper().getFilters(), (filter) -> {
            ItemStack viewItem = ItemStack.EMPTY;
            if (filter instanceof ItemFilter itemFilter) {
                viewItem = new ItemStack(itemFilter.getItem());
                if (itemFilter.getNbt() != null) viewItem.setTag(itemFilter.getNbt());
            } else if (filter instanceof TagFilter tagFilter) {
                Holder<Item> taggedFirstItem = BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR);
                Iterable<Holder<Item>> taggedItems = BuiltInRegistries.ITEM.getTagOrEmpty(tagFilter.getTag());
                Iterator<Holder<Item>> taggedItemsIterator = taggedItems.iterator();
                if (taggedItemsIterator.hasNext()) taggedFirstItem = taggedItemsIterator.next();
                viewItem = new ItemStack(taggedFirstItem);
                if (tagFilter.getNbt() != null) viewItem.setTag(tagFilter.getNbt());
            }
            return MinecraftAdapter.ItemStack.itemStack(viewItem);
        }, true, true);
        this.parentView = parentView;
    }

    @Override
    public void onElementButtonClicked(InventoryClickEvent event, Filter filter) {
        switch (event.getClick()) {
            case SHIFT_RIGHT -> {
                parentView.getGui().getMixinHopper().getFilters().remove(filter);
                this.setData(parentView.getGui().getMixinHopper().getFilters(), true);
            }
        }
    }

    @Override
    public void onPreviousButtonClicked(InventoryClickEvent event) {
        this.getGui().getView().clearInventory();
        this.getGui().setView(this.getParentView());
        this.getGui().getView().initialize();
    }

    @Override
    public void onCreateNewButtonClicked(InventoryClickEvent event) {
        NewMessageUtil.sendErrorMessage(event.getWhoClicked(), "まだなんも作れねーよボケ");
    }

    public ConfigureHopperViewBase getParentView() {
        return this.parentView;
    }
}