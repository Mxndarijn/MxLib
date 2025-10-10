package nl.mxndarijn.mxlib.mxscoreboard;

import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Supplier;

@Setter
public class MxSupplierScoreBoard extends MxScoreBoard {

    private Supplier<String> titleSupplier;
    private Supplier<List<String>> linesSupplier;

    public MxSupplierScoreBoard(JavaPlugin plugin, Supplier<String> titleSupplier, Supplier<List<String>> linesSupplier) {
        super(plugin);
        this.titleSupplier = titleSupplier;
        this.linesSupplier = linesSupplier;
    }

    @Override
    String getTitle() {
        return titleSupplier.get();
    }

    @Override
    List<String> getLines() {
        return linesSupplier.get();
    }
}
