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

package net.unknown.core.economy.repository;

import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Save to: ~/../economy/banks/UUID/BankName.yml
// exf) /root/unknown-network/economy/banks/UUID/BankName.yml
public class BankRepository {
    private static final Map<UUID, Map<String, Bank>> BANKS = new HashMap<>();

    public static void createBank(UUID owner, String bankName) {
        BANKS.put(owner, new HashMap<>());
        BANKS.get(owner).put(bankName, new Bank(owner, new BigDecimal(0)));
    }

    public static class Bank {
        private final UUID owner;
        private final BigDecimal value;

        private Bank(UUID owner, BigDecimal value) {
            this.owner = owner;
            this.value = value;
        }

        public void withdraw(double value) {

        }
    }
}
