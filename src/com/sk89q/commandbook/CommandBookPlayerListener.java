// $Id$
/*
 * CommandBook
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.commandbook;

import static com.sk89q.commandbook.CommandBookUtil.replaceColorMacros;
import static com.sk89q.commandbook.CommandBookUtil.sendMessage;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handler for player events.
 * 
 * @author sk89q
 */
public class CommandBookPlayerListener extends PlayerListener {
    
    protected final static Pattern namePattern = Pattern.compile(
            "^[abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_]{1,16}$");
    
    protected CommandBookPlugin plugin;
    
    /**
     * Construct the listener.
     * 
     * @param plugin
     */
    public CommandBookPlayerListener(CommandBookPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called on player login.
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.verifyNameFormat && !namePattern.matcher(player.getName()).matches()) {
            event.disallow(Result.KICK_OTHER, "Invalid player name detected!");
            return;
        }
        
        try {
            if (plugin.getBanDatabase().isBannedName(event.getPlayer().getName())
                    || plugin.getBanDatabase().isBannedAddress(
                            event.getPlayer().getAddress().getAddress())) {
                event.disallow(Result.KICK_BANNED, plugin.getBanMessage());
                return;
            }
        } catch (NullPointerException e) {
            // Bug in CraftBukkit
        }
    }
    
    /**
     * Called on player join.
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Show the MOTD.
        String motd = plugin.getMessage("motd");
        
        if (motd != null) {
            sendMessage(player,
                    replaceColorMacros(
                    plugin.replaceMacros(
                    player, motd)));
        }
        
        // Show the online list
        if (plugin.getConfiguration().getBoolean("online-on-join", true)) {
            CommandBookUtil.sendOnlineList(
                    plugin.getServer().getOnlinePlayers(), player);
        }
    }

    /**
     * Called on player disconnect.
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getMessageTargets().remove(plugin.toUniqueName(event.getPlayer()));
    }
    
}
