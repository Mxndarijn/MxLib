package nl.mxndarijn.mxlib.mxscoreboard;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nl.mxndarijn.mxlib.util.Functions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class MxScoreBoard {
    public final int MAX_LINE_LENGTH = 128;
    private final List<MxScoreBoardTeam> teams;
    private final JavaPlugin plugin;
    @Getter
    public List<UUID> playersUsingScoreboard;
    @Getter
    private Scoreboard scoreboard;
    private Objective objective;
    @Setter
    private long updateTimer = -1;
    private BukkitTask task;


    public MxScoreBoard(JavaPlugin plugin) {
        this.playersUsingScoreboard = new ArrayList<>();
        this.teams = new ArrayList<>();

        this.plugin = plugin;
    }


    public void addPlayer(UUID uuid) {
        if (playersUsingScoreboard.contains(uuid))
            return;
        createScoreboardIfNotExists();
        playersUsingScoreboard.add(uuid);
        Player p = Bukkit.getPlayer(uuid);
        p.setScoreboard(scoreboard);
        checkTimer();
    }

    public void removePlayer(UUID uuid) {
        if (!playersUsingScoreboard.contains(uuid))
            return;

        playersUsingScoreboard.remove(uuid);
        Player p = Bukkit.getPlayer(uuid);
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        if (playersUsingScoreboard.isEmpty())
            delete();
    }

    private void createScoreboardIfNotExists() {
        if (this.scoreboard != null)
            return;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.scoreboard = manager.getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("Title", "dummy", MiniMessage.miniMessage().deserialize("<!i>" + "Default"));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScoreboard();
        setPlayersScoreboard();
        checkTimer();

    }

    private void setPlayersScoreboard() {
        if (scoreboard == null)
            return;

        playersUsingScoreboard.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null)
                return;

            p.setScoreboard(scoreboard);
        });
    }

    public void delete() {
        if (this.scoreboard == null)
            return;
        if (task != null) {
            task.cancel();
            task = null;
        }
        removeAllPlayers();
        teams.forEach(t -> {
            t.destroy();
        });
        teams.clear();
        objective.unregister();
        this.objective = null;
        this.scoreboard = null;
    }

    private void removeAllPlayers() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        playersUsingScoreboard.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);

            if (p != null) {
                p.setScoreboard(manager.getMainScoreboard());
            }
        });
    }

    public void updateScoreboard() {
        if(playersUsingScoreboard.isEmpty())
            return;
        String title = getTitle();
        List<String> lines = getLines();

        if (!Functions.convertComponentToString(this.objective.displayName()).equals(title)) {
            this.objective.displayName(MiniMessage.miniMessage().deserialize("<!i>" + title));
        }

        while (teams.size() < lines.size()) {
            MxScoreBoardTeam team = new MxScoreBoardTeam(this);
            teams.add(team);
        }

        while (teams.size() > lines.size()) {
            MxScoreBoardTeam team = teams.remove(teams.size() - 1);
            team.destroy();
        }
        Collections.reverse(lines);
        for (int i = 0; i < teams.size(); i++) {
            MxScoreBoardTeam team = teams.get(i);
            String line = lines.get(i);
            if (!team.getLine().equals(line)) {
                team.setLine(line);
            }
            objective.getScore(team.getEntry()).setScore(i + 1);
        }
    }

    abstract String getTitle();

    abstract List<String> getLines();

    private void checkTimer() {
        if (this.updateTimer == -1 || task != null)
            return;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updateScoreboard();
        }, 0L, updateTimer);

    }
}
