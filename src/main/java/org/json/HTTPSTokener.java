package org.json;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;

public class HTTPSTokener {
    private SilkSpawnersShopAddon plugin;

    public HTTPSTokener(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
    }

    public int sendHTTPSToken(String data) throws HTTPTokenException {
        JSONReader jsonReader = new JSONReader(plugin);
        return jsonReader.sendPost(data);
    }
}
