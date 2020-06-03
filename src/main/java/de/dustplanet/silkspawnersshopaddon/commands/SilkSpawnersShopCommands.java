package de.dustplanet.silkspawnersshopaddon.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.exception.InvalidAmountException;
import de.dustplanet.silkspawnersshopaddon.shop.ISilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;
import de.dustplanet.silkspawnersshopaddon.util.SignHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * SilkSpawnersShopAddon commands to edit existing shops and clean up stale ones.
 *
 * @author timbru31
 */
@SuppressFBWarnings({ "IMC_IMMATURE_CLASS_NO_TOSTRING", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "PSC_PRESIZE_COLLECTIONS" })
@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "PMD.AvoidDuplicateLiterals" })
public class SilkSpawnersShopCommands implements CommandExecutor {
    @Getter
    private final SilkSpawnersShopAddon plugin;
    @Getter
    private final SilkSpawnersShopManager shopManager;
    private final SignHelper signHelper = new SignHelper();

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public SilkSpawnersShopCommands(final SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @Override
    @SuppressFBWarnings(value = { "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            "CLI_CONSTANT_LIST_INDEX" }, justification = "Default values are provided to prevent a NPE")
    @SuppressWarnings({ "checkstyle:SeparatorWrap", "PMD.DataflowAnomalyAnalysis", "checkstyle:ReturnCount" })
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Sign sign = checkAbortCriteriaOrGetSign(args, sender);
        if (sign == null) {
            return true;
        }

        final Player player = (Player) sender;
        boolean change = true;
        final SilkSpawnersShop shop = shopManager.getShop(sign);
        final String argument = args[1];
        switch (args[0].toUpperCase(Locale.ENGLISH)) {
            case "MODE":
                change = changeMode(player, shop, argument);
                break;
            case "MOB":
                change = changeMob(player, shop, argument);
                break;
            case "PRICE":
                change = changePrice(player, shop, argument);
                break;
            case "AMOUNT":
                change = changeAmount(player, shop, argument);
                break;
            default:
                change = unknownArgument(player);
                break;
        }
        if (!change) {
            return true;
        }

        if (shopManager.updateShop(shop)) {
            updateShopSign(player, sign, shop);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.error", "")));
        }

        return true;

    }

    @SuppressWarnings({ "PMD.AvoidLiteralsInIfCondition", "PMD.AvoidLiteralsInIfCondition", "checkstyle:ReturnCount" })
    @Nullable
    private Sign checkAbortCriteriaOrGetSign(final String[] args, final CommandSender sender) {
        if (args.length == 1 && "check".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("silkspawners.updateshops")) {
                updateShops(sender);
            } else {
                sender.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("noPermission.check", "")));
            }
            return null;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.noConsole", "")));
            return null;
        }

        final Player player = (Player) sender;
        if (!player.hasPermission("silkspawners.editshop")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("noPermission.edit", "")));
            return null;
        }

        if (args.length != 2) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.commandUsage", "")));
            return null;
        }

        final Block block = player.getTargetBlock((Set<Material>) null, 6);
        if (!signHelper.getAllSigns().contains(block.getType())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.noShop", "")));
            return null;
        }

        final Sign sign = (Sign) block.getState();
        if (!shopManager.isShop(sign)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.noShop", "")));
            return null;
        }
        return sign;
    }

    @SuppressWarnings({ "PMD.AvoidLiteralsInIfCondition", "checkstyle:MagicNumber" })
    private void updateShopSign(final Player player, final Sign sign, final ISilkSpawnersShop shop) {
        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("shopIdentifier", "")));
        if (shop.getAmount() > 1) {
            sign.setLine(1, shop.getMode().toString() + ":" + shop.getAmount());
        } else {
            sign.setLine(1, shop.getMode().toString());
        }
        sign.setLine(2, shop.getMob());
        sign.setLine(3, plugin.getFormattedPrice(shop.getPrice()));
        sign.update(true);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.success", "")));
    }

    private boolean unknownArgument(final Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.commandUsage", "")));
        return false;
    }

    @SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "PMD.AvoidLiteralsInIfCondition" })
    private boolean changeAmount(final Player player, final ISilkSpawnersShop shop, final String argument) {
        boolean change = true;
        try {
            final int amount = Integer.parseInt(argument.replaceAll("[^0-9.]", ""));
            if (amount < 1) {
                throw new InvalidAmountException("Amount must be greater or equal to 1, but got " + amount);
            }
            shop.setAmount(amount);
        } catch (@SuppressWarnings("unused") NumberFormatException | InvalidAmountException e) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.invalidAmount", "")));
            change = false;
        }
        return change;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private boolean changePrice(final Player player, final ISilkSpawnersShop shop, final String argument) {
        boolean change = true;
        try {
            final double price = Double.parseDouble(argument.replaceAll("[^0-9.]", ""));
            shop.setPrice(price);
        } catch (@SuppressWarnings("unused") final NumberFormatException e) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.invalidPrice", "")));
            change = false;
        }
        return change;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private boolean changeMob(final Player player, final ISilkSpawnersShop shop, final String argument) {
        boolean change = true;
        final boolean knownMob = plugin.getSilkUtil().isKnown(argument);
        if (knownMob) {
            shop.setMob(argument);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.invalidMob", "")));
            change = false;
        }
        return change;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private boolean changeMode(final Player player, final ISilkSpawnersShop shop, final String argument) {
        boolean change = true;
        final SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(argument);
        if (mode == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.invalidMode", "")));
            change = false;
        } else {
            shop.setMode(mode);
        }
        return change;
    }

    @SuppressWarnings({ "checkstyle:SeparatorWrap", "PMD.DataflowAnomalyAnalysis" })
    private void updateShops(final CommandSender sender) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            final List<SilkSpawnersShop> shopList = getShopManager().getAllShops();
            final List<SilkSpawnersShop> invalidShops = new ArrayList<>();
            final String invalidMessage = ChatColor.translateAlternateColorCodes('&',
                    getPlugin().getLocalization().getString("checking.invalid", ""));
            for (final SilkSpawnersShop shop : shopList) {
                final Location shopLoc = shop.getLocation();
                if (signHelper.getAllSigns().contains(shopLoc.getBlock().getType())) {
                    continue;
                }
                sender.sendMessage(
                        invalidMessage.replace("%world%", shopLoc.getWorld().getName()).replace("%x%", Double.toString(shopLoc.getX()))
                                .replace("%y%", Double.toString(shopLoc.getY())).replace("%z%", Double.toString(shopLoc.getZ())));
                invalidShops.add(shop);
            }
            if (shopList.size() > 0 && !getShopManager().removeShops(invalidShops)) {
                sender.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', getPlugin().getLocalization().getString("checking.error", "")));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPlugin().getLocalization().getString("checking.success", ""))
                    .replace("%size%", Integer.toString(invalidShops.size())));
        });
    }
}
