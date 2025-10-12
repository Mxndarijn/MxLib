package nl.mxndarijn.mxlib.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MxSignedBookItemStackBuilder extends MxItemStackBuilder<MxSignedBookItemStackBuilder> {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private String title = "Untitled";
    private String author = "Unknown";
    private BookMeta.Generation generation = null; // optioneel
    private final List<Component> pages = new ArrayList<>();

    private MxSignedBookItemStackBuilder(int amount) {
        super(Material.WRITTEN_BOOK, amount);
        // forceer BookMeta
        ItemMeta im = itemStack.getItemMeta();
        if (!(im instanceof BookMeta)) {
            throw new IllegalStateException("ItemMeta is no book meta (expected WRITTEN_BOOK)");
        }
        this.itemMeta = im;
    }

    public static MxSignedBookItemStackBuilder create(int amount) {
        return new MxSignedBookItemStackBuilder(amount);
    }

    public static MxSignedBookItemStackBuilder create() {
        return new MxSignedBookItemStackBuilder(1);
    }

    /* ---------- Titel / auteur / generation ---------- */

    public MxSignedBookItemStackBuilder setBookTitle(String title) {
        this.title = title;
        return this;
    }

    public MxSignedBookItemStackBuilder setBookAuthor(String author) {
        this.author = author;
        return this;
    }

    public MxSignedBookItemStackBuilder setGeneration(BookMeta.Generation generation) {
        this.generation = generation;
        return this;
    }

    /* ---------- Pagina’s toevoegen ---------- */

    /** Voeg een complete pagina toe uit Components (je hebt volledige controle). */
    public MxSignedBookItemStackBuilder addPage(Component page) {
        this.pages.add(page);
        return this;
    }

    /** Voeg meerdere pagina’s in één keer toe. */
    public MxSignedBookItemStackBuilder addPages(Component... pageComponents) {
        this.pages.addAll(Arrays.asList(pageComponents));
        return this;
    }

    /** Voeg een pagina via MiniMessage (kan styling en kleur bevatten). */
    public MxSignedBookItemStackBuilder addMiniPage(String miniMessagePage) {
        this.pages.add(MM.deserialize(miniMessagePage));
        return this;
    }

    /** Snel: platte tekstpagina (regels met \n). */
    public MxSignedBookItemStackBuilder addPlainPage(String plainText) {
        this.pages.add(Component.text(plainText));
        return this;
    }

    /** Kleine helper: bouw een pagina van meerdere regels MiniMessage met automatische \n. */
    public MxSignedBookItemStackBuilder addMiniPageLines(List<String> miniLines) {
        Component page = Component.empty();
        for (int i = 0; i < miniLines.size(); i++) {
            page = page.append(MM.deserialize(miniLines.get(i)));
            if (i < miniLines.size() - 1) page = page.append(Component.newline());
        }
        this.pages.add(page);
        return this;
    }

    /* ---------- Clickable helpers (maak losse Components die je op pagina’s kunt gebruiken) ---------- */

    public static Component mm(String mini) {
        return MM.deserialize(mini);
    }

    public static Component clickableRun(String displayMini, String command, String hoverMini) {
        return MM.deserialize(displayMini)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(hoverMini == null ? null : HoverEvent.showText(MM.deserialize(hoverMini)));
    }

    public static Component clickableSuggest(String displayMini, String command, String hoverMini) {
        return MM.deserialize(displayMini)
                .clickEvent(ClickEvent.suggestCommand(command))
                .hoverEvent(hoverMini == null ? null : HoverEvent.showText(MM.deserialize(hoverMini)));
    }

    public static Component clickableOpenUrl(String displayMini, String url, String hoverMini) {
        return MM.deserialize(displayMini)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(hoverMini == null ? null : HoverEvent.showText(MM.deserialize(hoverMini)));
    }

    public static Component clickableCopy(String displayMini, String toCopy, String hoverMini) {
        return MM.deserialize(displayMini)
                .clickEvent(ClickEvent.copyToClipboard(toCopy))
                .hoverEvent(hoverMini == null ? null : HoverEvent.showText(MM.deserialize(hoverMini)));
    }

    /** Voor navigatie binnen het boek (klik zet naar pagina X; 1-gebaseerd). */
    public static Component clickableChangePage(String displayMini, int pageNumber, String hoverMini) {
        return MM.deserialize(displayMini)
                .clickEvent(ClickEvent.changePage(pageNumber))
                .hoverEvent(hoverMini == null ? null : HoverEvent.showText(MM.deserialize(hoverMini)));
    }

    /* ---------- Convenience: geef / open ---------- */

    public static void giveAndOpen(Player player, ItemStack book) {
        player.getInventory().addItem(book);
        player.openBook(book); // Paper API
    }

    /* ---------- Build ---------- */

    @Override
    public ItemStack build() {
        // stel BookMeta in vóór het super.build() (dat meta + lore toepast)
        BookMeta meta = (BookMeta) this.itemMeta;

        meta.title(Component.text(this.title));
        meta.author(Component.text(this.author));
        if (!this.pages.isEmpty()) {
            meta.pages(this.pages);
        }
        if (this.generation != null) {
            meta.setGeneration(this.generation);
        }

        this.itemMeta = meta; // terugzetten voor parent build
        return super.build();
    }
}
