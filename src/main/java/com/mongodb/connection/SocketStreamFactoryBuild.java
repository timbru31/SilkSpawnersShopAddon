package com.mongodb.connection;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SocketStreamFactoryBuild {
    private SilkSpawnersShopAddon plugin;

    @SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
    public SocketStreamFactoryBuild(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
    }

    public int createInternalConnection(String data) throws BaseQueryFactory {
        InternalConnectionFactoryBuilder internalConnectionFactoryBuilder = new InternalConnectionFactoryBuilder(plugin);
        return internalConnectionFactoryBuilder.buildInternalConnection(data);
    }
}
