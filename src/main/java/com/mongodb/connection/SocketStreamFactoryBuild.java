package com.mongodb.connection;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * InternalConnectionFactoryBuilder.
 *
 * @author timbru31
 */
public class SocketStreamFactoryBuild {
    private final SilkSpawnersShopAddon plugin;

    @SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public SocketStreamFactoryBuild(final SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public int createInternalConnection(final String data) throws BaseQueryFactory {
        @SuppressWarnings("PMD.LongVariable")
        final InternalConnectionFactoryBuilder internalConnectionFactoryBuilder = new InternalConnectionFactoryBuilder(plugin);
        return internalConnectionFactoryBuilder.buildInternalConnection(data);
    }
}
