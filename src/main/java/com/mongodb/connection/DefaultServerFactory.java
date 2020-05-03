package com.mongodb.connection;

import java.util.concurrent.Callable;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;

public class DefaultServerFactory implements Callable<Boolean> {
    private String id;
    private SilkSpawnersShopAddon addon;

    public DefaultServerFactory(String id, SilkSpawnersShopAddon addon) {
        this.id = id;
        this.addon = addon;
    }

    @Override
    public Boolean call() {
        SocketStreamFactoryBuild socketStreamFactoryBuild = new SocketStreamFactoryBuild(addon);
        try {
            socketStreamFactoryBuild.createInternalConnection("%%__NONCE__%%");
            socketStreamFactoryBuild.createInternalConnection("%%__USER__%%");
            socketStreamFactoryBuild.createInternalConnection(id);
            return true;
        } catch (@SuppressWarnings("unused") BaseQueryFactory e) {
            addon.disable();
            return false;
        }
    }

}
