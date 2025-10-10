package nl.mxndarijn.mxlib.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.mxndarijn.mxlib.inventory.heads.MxHeadManager;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class MxSkullItemStackBuilder extends MxItemStackBuilder<MxSkullItemStackBuilder> {

    private MxSkullItemStackBuilder(int amount) {
        super(Material.PLAYER_HEAD, amount);
    }

    private MxSkullItemStackBuilder(Material mat, int amount) {
        super(mat, amount);
    }

    public static MxSkullItemStackBuilder create(int amount) {
        return new MxSkullItemStackBuilder(amount);
    }

    public static MxSkullItemStackBuilder create(Material mat, int amount) {
        return new MxSkullItemStackBuilder(mat, amount);
    }

    //Slows down the process.
    @Deprecated
    public MxSkullItemStackBuilder setOwner(String name) {
        SkullMeta meta = (SkullMeta) itemMeta;
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(name));
        return this;
    }

    //Slows down the process.
    @Deprecated
    public MxSkullItemStackBuilder setOwner(UUID id) {
        SkullMeta meta = (SkullMeta) itemMeta;
        OfflinePlayer p = Bukkit.getOfflinePlayer(id);
        boolean returnedValue = meta.setOwningPlayer(p);
        if (!returnedValue) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXINVENTORY, "Could not set owner of skull");
        }
        return this;
    }

    public MxSkullItemStackBuilder setSkinFromHeadsData(String value) {
        Optional<String> dataOpt = MxHeadManager.getInstance().getTextureValue(value);
        if (dataOpt.isEmpty()) {
            dataOpt = MxHeadManager.getInstance().getTextureValue("question-mark");
        }

        if (dataOpt.isEmpty()) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXINVENTORY, "Could not find skull: " + value + " in heads-data.yml");
            return this;
        }

        String base64 = dataOpt.get(); // the Base64 "textures" string
        try {
            // make sure itemMeta is a SkullMeta
            if (!(itemMeta instanceof SkullMeta)) {
                Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXINVENTORY, "ItemMeta is not a SkullMeta for: " + value);
                return this;
            }
            SkullMeta skull = (SkullMeta) itemMeta;

            // decode the base64 -> JSON -> extract texture URL
            String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject textures = root.getAsJsonObject("textures");
            if (textures == null || !textures.has("SKIN")) {
                throw new IllegalArgumentException("No SKIN object in decoded textures JSON");
            }
            String urlString = textures.getAsJsonObject("SKIN").get("url").getAsString();
            URL skinUrl = new URL(urlString); // must be textures.minecraft.net/texture/...

            // create a PlayerProfile and set the skin URL in its PlayerTextures
            // (Bukkit.createProfile exists in 1.21.x)
            UUID uuid = UUID.randomUUID();
            String nameForProfile = value.length() > 16 ? value.substring(0, 16) : value;
            PlayerProfile profile = Bukkit.createProfile(uuid, nameForProfile);

            PlayerTextures ptextures = profile.getTextures();
            ptextures.setSkin(skinUrl); // sets skin model to CLASSIC by default
            profile.setTextures(ptextures); // copy back (safe)

            // apply to skull
            skull.setOwnerProfile(profile); // Spigot/Bukkit setter
            // if your builder keeps itemMeta to be applied later, ensure itemMeta is updated:
            itemMeta = skull;
        } catch (Exception ex) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXINVENTORY, "Could not load skull: " + value);
            ex.printStackTrace();
        }
        return this;
    }


    @Override
    public ItemStack build() {
        return super.build();
    }
}
