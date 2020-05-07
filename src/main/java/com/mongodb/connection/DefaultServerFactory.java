package com.mongodb.connection;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;

public class DefaultServerFactory implements Runnable {
    private String id;
    private SilkSpawnersShopAddon addon;

    public DefaultServerFactory(String id, SilkSpawnersShopAddon addon) {
        this.id = id;
        this.addon = addon;
    }

    @Override
    public void run() {
        SocketStreamFactoryBuild socketStreamFactoryBuild = new SocketStreamFactoryBuild(addon);
        try {
            socketStreamFactoryBuild.createInternalConnection("%%__NONCE__%%");
            socketStreamFactoryBuild.createInternalConnection("%%__USER__%%");
            socketStreamFactoryBuild.createInternalConnection(id);
        } catch (@SuppressWarnings("unused") BaseQueryFactory e) {
            addon.disable();
        }
    }
}
