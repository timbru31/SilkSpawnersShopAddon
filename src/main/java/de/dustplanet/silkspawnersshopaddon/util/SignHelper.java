package de.dustplanet.silkspawnersshopaddon.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.Material;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({ "IMC_IMMATURE_CLASS_NO_TOSTRING", "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS" })
public class SignHelper {
    private Collection<Material> allSigns = Arrays.stream(Material.values()).filter(material -> material.name().endsWith("SIGN"))
            .collect(Collectors.toList());

    private Collection<Material> wallSigns = Arrays.stream(Material.values()).filter(material -> material.name().endsWith("WALL_SIGN"))
            .collect(Collectors.toList());

    private Collection<Material> signs = allSigns.stream().filter(e -> !wallSigns.contains(e)).collect(Collectors.toList());

    public Collection<Material> getAllSignMaterials() {
        return allSigns;
    }

    public Collection<Material> getWallSignMaterials() {
        return wallSigns;
    }

    public Collection<Material> getSignMaterials() {
        return signs;
    }
}
