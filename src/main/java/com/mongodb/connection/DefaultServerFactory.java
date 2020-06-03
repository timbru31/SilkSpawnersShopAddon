package com.mongodb.connection;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * DefaultServerFactory.
 *
 * @author timbru31
 */
public class DefaultServerFactory implements Runnable {
    @SuppressWarnings("PMD.ShortVariable")
    private final String id;
    private final SilkSpawnersShopAddon addon;

    @SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
    @SuppressWarnings({ "PMD.ShortVariable", "checkstyle:MissingJavadocMethod" })
    public DefaultServerFactory(final String id, final SilkSpawnersShopAddon addon) {
        this.id = id;
        this.addon = addon;
    }

    @Override
    public void run() {
        final SocketStreamFactoryBuild socketStreamFactoryBuild = new SocketStreamFactoryBuild(addon);
        try {
            socketStreamFactoryBuild.createInternalConnection("%%__NONCE__%%");
            socketStreamFactoryBuild.createInternalConnection("%%__USER__%%");
            socketStreamFactoryBuild.createInternalConnection(id);
        } catch (@SuppressWarnings("unused") final BaseQueryFactory e) {
            addon.disable();
        }
    }
}
