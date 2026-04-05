package nl.mxndarijn.mxlib.mxcommand;

import nl.mxndarijn.mxlib.chatprefix.MxChatPrefixManager;
import nl.mxndarijn.mxlib.chatprefix.MxStandardChatPrefix;
import nl.mxndarijn.mxlib.language.MxLanguageManager;
import nl.mxndarijn.mxlib.language.MxStandardLanguageText;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.permission.MxPermissionService;
import nl.mxndarijn.mxlib.permission.MxPermissionType;
import nl.mxndarijn.mxlib.util.MxMessageUtil;
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

    private final MxPermissionType permission;
    private final boolean onlyPlayersCanExecute;
    private final MxWorldFilter worldFilter;

    private final MxLanguageManager languageManager;
    private final MxChatPrefixManager chatPrefixManager;

    /**
     * Constructs a new {@code MxCommand}.
     *
     * @param permission            the permission required to execute this command
     * @param onlyPlayersCanExecute whether only players can execute this command
     * @param worldFilter           the world filter to apply (may be {@code null})
     */
    public MxCommand(MxPermissionType permission, boolean onlyPlayersCanExecute, MxWorldFilter worldFilter) {
        this.permission = permission;
        this.onlyPlayersCanExecute = onlyPlayersCanExecute;
        this.worldFilter = worldFilter;

        this.languageManager = MxLanguageManager.getInstance();
        this.chatPrefixManager = MxChatPrefixManager.getInstance();
    }

    /**
     * Constructs a new {@code MxCommand} without a world filter.
     *
     * @param permission            the permission required to execute this command
     * @param onlyPlayersCanExecute whether only players can execute this command
     */
    public MxCommand(MxPermissionType permission, boolean onlyPlayersCanExecute) {
        this(permission, onlyPlayersCanExecute, null);
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sender type requirement
        if (onlyPlayersCanExecute && !(sender instanceof Player)) {
            MxMessageUtil.sendMessageToPlayer(sender,
                    languageManager.getLanguageString(
                            MxStandardLanguageText.NO_PLAYER,
                            Collections.emptyList(),
                            chatPrefixManager.requireFind(MxStandardChatPrefix.DEFAULT)));
            return true;
        }

        // Permission check (overridable)
        if (!hasPermission(sender)) {
            MxMessageUtil.sendMessageToPlayer(sender,
                    languageManager.getLanguageString(
                            MxStandardLanguageText.NO_PERMISSION,
                            Collections.emptyList(),
                            chatPrefixManager.requireFind(MxStandardChatPrefix.DEFAULT)));
            return true;
        }

        // World filter (generic)
//        if (sender instanceof Player player && worldFilter != null) {
//            if (!worldFilter.isPlayerInCorrectWorld(player)) {
//                MxMessageUtil.sendMessageToPlayer(sender,
//                        languageManager.getLanguageString(
//                                MxStandardLanguageText.NOT_CORRECT_WORLD,
//                                Collections.emptyList(),
//                                chatPrefixManager.requireFind(MxStandardChatPrefix.DEFAULT)));
//                return true;
//            }
//        }

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
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXCOMMAND,
                    "Could not execute command " + command.getName());
            e.printStackTrace();
            MxMessageUtil.sendMessageToPlayer(sender,
                    languageManager.getLanguageString(
                            MxStandardLanguageText.ERROR_WHILE_EXECUTING_COMMAND,
                            Collections.emptyList(),
                            chatPrefixManager.requireFind(MxStandardChatPrefix.DEFAULT)));
        }
        return true;
    }

    /**
     * Overridable permission check. Defaults to Bukkit permission node from {@link MxPermissionType}.
     */
    protected boolean hasPermission(CommandSender sender) {
        return MxPermissionService.getInstance().hasSenderPermission(sender, permission);
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

    /**
     * Executes the command logic.
     * @param sender the command sender
     * @param command the Bukkit command object
     * @param label the command label used
     * @param args the command arguments
     * @throws Exception if an error occurs during execution
     */
    public abstract void execute(CommandSender sender, Command command, String label, String[] args) throws Exception;
}
