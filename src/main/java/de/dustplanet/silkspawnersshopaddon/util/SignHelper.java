package de.dustplanet.silkspawnersshopaddon.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.Material;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * Sign helper to support legacy and new wood type signs.
 *
 * @author timbru31
 */
@SuppressFBWarnings({ "IMC_IMMATURE_CLASS_NO_TOSTRING", "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS" })
@SuppressWarnings({ "checkstyle:MissingCtor", "checkstyle:SeparatorWrap", "PMD.AtLeastOneConstructor" })
public class SignHelper {
    @Getter
    private final Collection<Material> allSigns = Arrays.stream(Material.values())
            .filter(material -> material.name().endsWith("SIGN") || material.name().endsWith("SIGN_POST")).collect(Collectors.toList());

    @Getter
    private final Collection<Material> wallSigns = Arrays.stream(Material.values())
            .filter(material -> material.name().endsWith("WALL_SIGN")).collect(Collectors.toList());

    @Getter
    private final Collection<Material> standingSigns = allSigns.stream().filter(e -> !wallSigns.contains(e)).collect(Collectors.toList());

}
