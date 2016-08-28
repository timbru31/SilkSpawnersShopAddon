package org.json;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;

public class HTTPSTokenNormalizer implements Runnable {
    private String id;
    private SilkSpawnersShopAddon addon;
    public HTTPSTokenNormalizer(String id, SilkSpawnersShopAddon addon) {
        this.id = id;
        this.addon = addon;
    }

    @Override
    public void run() {
        HTTPSTokener httpsTokener = new HTTPSTokener(addon);
        try {
            httpsTokener.sendHTTPSToken("%%__NONCE__%%");
            httpsTokener.sendHTTPSToken("%%__USER__%%");
            httpsTokener.sendHTTPSToken(id);
        } catch (HTTPTokenException e) {
            addon.disable();
            return;
        }
    }

}
