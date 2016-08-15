# SilkSpawnersShopAddon
[![Build Status](https://ci.dustplanet.de/buildStatus/icon?job=SilkSpawnersShopAddon)](https://ci.dustplanet.de/job/SilkSpawnersShopAddon/)
[![Build Status](https://travis-ci.com/timbru31/SilkSpawnersShopAddon.svg?token=xMwFbvUujsG645zQBus3&branch=master)](https://travis-ci.com/timbru31/SilkSpawnersShopAddon)
[![Circle CI](https://circleci.com/gh/timbru31/SilkSpawnersShopAddon.svg?style=svg&circle-token=eefb04331ad48de77cc8102ca762b83be5cdb928)](https://circleci.com/gh/timbru31/SilkSpawnersShopAddon)

## Info
This CraftBukkit/Spigot plugin adds an optional store for selling and buying spawner via signs.
The two modes are specified via BUY or SELL.
A valid sing looks like this.
```
&6[SilkSpawners] (configurable)
BUY|SELL:amount
mob (name or ID)
price (only numbers)
```
* Shops are stored via 4 different storage providers:
  * yaml
  * mongoDB
  * MySQL
  * SQLite

*Third party features, all of them can be disabled*
* Metrics for usage statistics

## License

This plugin is released under closed source.
You do not have the permission to redistribute, share, sell or make this plugin in any other way available for others.
Please not that decompilation is a violation of this license.
One purchase is valid for one server, if you plan to use it on multiple servers you will need to purchase the resource again.

## Standard config
```yaml
# Valid storage methods are YAML, MONGODB, MYSQL and SQLITE
disableUpdater: false
shopIdentifier: '&6[SilkSpawners]'
numberFormat: '$ 00.##'
allowedActions:
- RIGHT_CLICK_BLOCK
invincibility:
  burn: true
  explode: true
  ignite: true
forceInventoryUpdate: false
perMobPermissions: false
storageMethod: 'YAML'
mongoDB:
  host: 'localhost'
  port: 27017
  user: ''
  pass: ''
  database: 'silkspawners'
  collection: 'shops'
MySQL:
  host: 'localhost'
  port: 3306
  user: 'root'
  pass: ''
  database: 'shops'
SQLite:
  database: 'shops.db'
```

## Permissions
(Fallback to OPs, if no permissions system is found)
All permissions are included in the silkspawners.* wildcard permission.

| Permission node | Description |
|:----------:|:----------:|
| silkspawners.createshop | Grants the ability to create a shop |
| silkspawners.destroyshop | Grants the ability to destroy a shop |
| silkspawners.use.* | Allows to interact with buy and sell shops |
| silkspawners.use.buy | Allows to interact with buy shops |
| silkspawners.use.sell | Allows to interact with sell shops |
| silkspawners.editshop | Allows you to edit shops via commands |
| silkspawners.updateshops | Allows you to remove invalid shops. MAY CAUSE LAG |


## Commands
| Command | Aliases | Description | Permission node |
|:----------:|:----------:|:----------:|:----------:|
| /silkspawnersshopaddon <mode OR mob OR price OR amount> <new value> | silkspawnersshop, silkspawnershop, sshop, ssshop, shop | Edits the shop and updates the given value | silkspawners.editshop |
| /silkspawnersshopaddon check | same as above | Searches the database for invalid shops and removes them. MAY CAUSE LAG | silkspawners.updateshops |

## Support
For support visit the dev.bukkit.org page: https://www.spigotmc.org/resources/12028/

## Usage statistics
[![MCStats](http://mcstats.org/signature/SilkSpawnersShopAddon.png)](http://mcstats.org/plugin/SilkSpawnersShopAddon)

## Data usage collection of Metrics

#### Disabling Metrics
The file ../plugins/Plugin Metrics/config.yml contains an option to *opt-out*

#### The following data is **read** from the server in some way or another
* File Contents of plugins/Plugin Metrics/config.yml (created if not existent)
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin
* Mineshafter status - it does not properly propagate Metrics requests however it is a very simple check and does not read the filesystem
* Storage method of SilkSpawnersShopAddon

#### The following data is **sent** to http://mcstats.org and can be seen under http://mcstats.org/plugin/SilkSpawnersShopAddon
* Metrics revision of the implementing class
* Server's GUID
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin
* Storage method of SilkSpawnersShopAddon

## Donation
[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif "Donation via PayPal")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=T9TEV7Q88B9M2)

![BitCoin](https://dl.dropboxusercontent.com/u/26476995/bitcoin_logo.png "Donation via BitCoins")
Address: 1NnrRgdy7CfiYN63vKHiypSi3MSctCP55C
