package uk.co.eluinhost.ultrahardcore.features.playerfreeze;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import uk.co.eluinhost.configuration.ConfigManager;
import uk.co.eluinhost.ultrahardcore.features.UHCFeature;

import java.util.HashSet;
import java.util.Set;

@Singleton
public class PlayerFreezeFeature extends UHCFeature {

    private Set<String> m_players = new HashSet<String>();

    /**
     * handles frozen players
     * @param plugin the plugin
     * @param configManager the config manager
     */
    @Inject
    private PlayerFreezeFeature(Plugin plugin, ConfigManager configManager) {
        super(plugin, configManager);
    }

    /**
     * @param playerName the player name to freeze
     */
    public void addPlayer(String playerName){
        m_players.add(playerName);
        //TODO stop movement
    }

    /**
     * @param playerName the player name
     */
    public void removePlayer(String playerName){
        m_players.remove(playerName);
        //TODO allow movement
    }

    /**
     * Remove all from the frozen list
     */
    public void unfreezeAll(){
        //TODO allow movement for all
        m_players.clear();
    }

    /**
     * Whenever a player joins
     * @param pje the player join event
     */
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent pje){
        //TODO check whether to reapply freeze
    }

    /**
     * Called when the feature is being enabled
     */
    @Override
    protected void enableCallback(){
        //TODO reapply freezes?
    }

    /**
     * Called when the feature is being disabled
     */
    @Override
    protected void disableCallback(){
        //TODO remove freezes
    }

    @Override
    public String getFeatureID() {
        return "PlayerFreeze";
    }

    @Override
    public String getDescription() {
        return "Allows for freezing players in place";
    }
}
