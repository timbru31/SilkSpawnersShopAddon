package com.mongodb.connection;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;

public class SocketStreamFactoryBuild {
    private SilkSpawnersShopAddon plugin;

    public SocketStreamFactoryBuild(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
    }

    public int createInternalConnection(String data) throws BaseQueryFactory {
        InternalConnectionFactoryBuilder internalConnectionFactoryBuilder = new InternalConnectionFactoryBuilder(plugin);
        return internalConnectionFactoryBuilder.buildInternalConnection(data);
    }
}
