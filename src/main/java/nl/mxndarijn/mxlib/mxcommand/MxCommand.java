package nl.mxndarijn.mxlib.mxcommand;

import nl.mxndarijn.mxlib.chatprefix.ChatPrefixManager;
import nl.mxndarijn.mxlib.chatprefix.StandardChatPrefix;
import nl.mxndarijn.mxlib.language.LanguageManager;
import nl.mxndarijn.mxlib.language.StandardLanguageText;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.permission.PermissionType;
import nl.mxndarijn.mxlib.util.MessageUtil;
import nl.mxndarijn.mxlib.util.MxWorldFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Base command with generic, overridable policy hooks for execution control.
 * Projects can subclass and override {@link #hasPermission(CommandSender)},
 * {@link #canExecute(CommandSender, Command, String, String[])}, and
 * {@link #canExecutePlayer(Player, Command, String, String[])} to enforce custom rules.
 */
public abstract class MxCommand implements CommandExecutor {

    private final PermissionType permission;
    private final boolean onlyPlayersCanExecute;
    private final MxWorldFilter worldFilter;

    private final LanguageManager languageManager;
    private final ChatPrefixManager chatPrefixManager;

    public MxCommand(PermissionType permission, boolean onlyPlayersCanExecute, MxWorldFilter worldFilter) {
        this.permission = permission;
        this.onlyPlayersCanExecute = onlyPlayersCanExecute;
        this.worldFilter = worldFilter;

        this.languageManager = LanguageManager.getInstance();
        this.chatPrefixManager = ChatPrefixManager.getInstance();
    }

    public MxCommand(PermissionType permission, boolean onlyPlayersCanExecute) {
        this(permission, onlyPlayersCanExecute, null);
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sender type requirement
        if (onlyPlayersCanExecute && !(sender instanceof Player)) {
            MessageUtil.sendMessageToPlayer(sender,
                    languageManager.getLanguageString(
                            StandardLanguageText.NO_PLAYER,
                            Collections.emptyList(),
                            chatPrefixManager.requireFind(StandardChatPrefix.DEFAULT)));
            return true;
        }

        // Permission check (overridable)
        if (!hasPermission(sender)) {
            MessageUtil.sendMessageToPlayer(sender,
                    languageManager.getLanguageString(
                            StandardLanguageText.NO_PERMISSION,
                            Collections.emptyList(),
                            chatPrefixManager.requireFind(StandardChatPrefix.DEFAULT)));
            return true;
        }

        // World filter (generic)
        if (sender instanceof Player player && worldFilter != null) {
            if (!worldFilter.isPlayerInCorrectWorld(player)) {
                MessageUtil.sendMessageToPlayer(sender,
                        languageManager.getLanguageString(
                                StandardLanguageText.NOT_CORRECT_WORLD,
                                Collections.emptyList(),
                                chatPrefixManager.requireFind(StandardChatPrefix.DEFAULT)));
                return true;
            }
        }

        // Project-specific policy hooks
        if (!canExecute(sender, command, label, args)) {
            return true;
        }
        if (sender instanceof Player) {
            if (!canExecutePlayer((Player) sender, command, label, args)) {
                return true;
            }
        }

        try {
            execute(sender, command, label, args);
        } catch (Exception e) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXCOMMAND,
                    "Could not execute command " + command.getName());
            e.printStackTrace();
            MessageUtil.sendMessageToPlayer(sender,
                    languageManager.getLanguageString(
                            StandardLanguageText.ERROR_WHILE_EXECUTING_COMMAND,
                            Collections.emptyList(),
                            chatPrefixManager.requireFind(StandardChatPrefix.DEFAULT)));
        }
        return true;
    }

    /**
     * Overridable permission check. Defaults to Bukkit permission node from {@link PermissionType}.
     */
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(permission.node());
    }

    /**
     * Generic, overridable policy hook for all senders.
     * Return false to block execution (send your own feedback if desired).
     */
    protected boolean canExecute(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }

    /**
     * Generic, overridable policy hook for players.
     * Return false to block execution (send your own feedback if desired).
     */
    protected boolean canExecutePlayer(Player player, Command command, String label, String[] args) {
        return true;
    }

    public abstract void execute(CommandSender sender, Command command, String label, String[] args) throws Exception;
}
