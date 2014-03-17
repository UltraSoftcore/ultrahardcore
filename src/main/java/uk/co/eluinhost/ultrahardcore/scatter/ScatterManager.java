package uk.co.eluinhost.ultrahardcore.scatter;

import org.bukkit.Location;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import uk.co.eluinhost.ultrahardcore.scatter.exceptions.MaxAttemptsReachedException;
import uk.co.eluinhost.ultrahardcore.scatter.exceptions.ScatterTypeConflictException;
import uk.co.eluinhost.ultrahardcore.scatter.types.AbstractScatterType;

import java.util.List;

public interface ScatterManager extends Runnable {

    /**
     * @return true if currently busy, false otherwise
     */
    boolean isScatterInProgress();

    /**
     * Add a scatter type to the system
     * @param type the scatterer to add
     * @throws uk.co.eluinhost.ultrahardcore.scatter.exceptions.ScatterTypeConflictException if the scatter ID is already taken
     */
    void addScatterType(AbstractScatterType type) throws ScatterTypeConflictException;

    /**
     * @param scatterID the ID to look for
     * @return the scatter type if found or null if not
     */
    AbstractScatterType getScatterType(String scatterID);

    /**
     * @return unmodifiable list of all scatter types
     */
    List<AbstractScatterType> getScatterTypes();

    /**
     * @return a list of all the scatterIDs
     */
    String[] getScatterTypeNames();

    /**
     * Scatters the player and protects them from damage using Protector
     * @param player the player to scatter
     * @param loc the location to scatter to
     */
    void teleportSafe(Player player, Location loc);

    /**
     * @return unmodifiable list of teleports left to process
     */
    Iterable<Teleporter> getRemainingTeleports();

    /**
     * @return the maximum amount of tries to scatter
     */
    int getMaxTries();

    /**
     * Scatter the players
     * @param type the scatter logic to use
     * @param params the parameters to scatter with
     * @param players the player to scatter
     * @param sender the sender who issued the command to be kept updated
     * @throws uk.co.eluinhost.ultrahardcore.scatter.exceptions.MaxAttemptsReachedException if scatter couldn't complete
     */
    void scatter(AbstractScatterType type, Parameters params, Iterable<Player> players, Conversable sender) throws MaxAttemptsReachedException;
}
