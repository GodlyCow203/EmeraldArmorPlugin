package net.godlycow.org.emeraldarmorplugin;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class EmeraldArmorPlugin extends JavaPlugin {

    private FileConfiguration cfg;
    public static NamespacedKey EMERALD_ARMOR_KEY;
    private Metrics metrics;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cfg = getConfig();

        EMERALD_ARMOR_KEY = new NamespacedKey(this, "emerald_armor");

        metrics = new Metrics(this, 27884);

        getServer().getPluginManager().registerEvents(new ArmorEquipListener(this), this);

        if (cfg.getBoolean("recipes.enable", true)) {
            registerRecipes();
        }

        getLogger().info("EmeraldArmor enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("EmeraldArmor disabled");
    }

    private void registerRecipes() {
        registerColoredLeatherRecipe(Material.LEATHER_HELMET, "emerald_helmet",
                new String[]{
                        "EEE",
                        "E E",
                        "   "
                });

        registerColoredLeatherRecipe(Material.LEATHER_CHESTPLATE, "emerald_chest",
                new String[]{
                        "E E",
                        "EEE",
                        "EEE"
                });

        registerColoredLeatherRecipe(Material.LEATHER_LEGGINGS, "emerald_leggings",
                new String[]{
                        "EEE",
                        "E E",
                        "E E"
                });

        registerColoredLeatherRecipe(Material.LEATHER_BOOTS, "emerald_boots",
                new String[]{
                        "   ",
                        "E E",
                        "E E"
                });
    }



    private void registerColoredLeatherRecipe(Material resultMaterial, String keySuffix, String[] shape) {
        ItemStack result = new ItemStack(resultMaterial);
        LeatherArmorMeta meta = (LeatherArmorMeta) result.getItemMeta();
        meta.setColor(Color.GREEN);
        meta.setDisplayName("§aEmerald " + resultMaterial.name().replace("LEATHER_", "").toLowerCase());

        meta.addEnchant(Enchantment.PROTECTION, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        if (resultMaterial == Material.LEATHER_HELMET) meta.addEnchant(Enchantment.AQUA_AFFINITY, 3, true);
        if (resultMaterial == Material.LEATHER_BOOTS) meta.addEnchant(Enchantment.FEATHER_FALLING, 4, true);

        result.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, keySuffix);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape);

        recipe.setIngredient('E', Material.EMERALD);

        Bukkit.addRecipe(recipe);
    }


    public FileConfiguration getCfg() {
        return cfg;
    }
}
