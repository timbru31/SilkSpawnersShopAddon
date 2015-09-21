# SilkSpawnersShopAddon [![Build Status](http://ci.dustplanet.de/buildStatus/icon?job=SilkSpawnersShopAddon)](http://ci.dustplanet.de/job/SilkSpawnersShopAddon/)

## Info
This CraftBukkit plugin adds an optional store for selling and buying spawner via signs.
The two modes are specified via BUY or SELL.
A valid sing looks like this.
````
[SilkSpawners]
BUY / SELL
mob (name or ID)
price (only numbers)
````
* Shops are stored via 4 different storage p
roviders:
  * yaml
  * mongoDB
  * MySQL
  * SQLite

*Third party features, all of them can be disabled*
* Metrics for usage statistics

## License
This plugin is released under the
*Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)* license.
Please see [LICENSE.md](LICENSE.md) for more information.

## Standard config
````yaml
# Valid storage methods are YAML, MONGODB, MYSQL and SQLITE
currencySign: '$'
allowedActions:
- RIGHT_CLICK_BLOCK
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
````

## Permissions
(Fallback to OPs, if no permissions system is found)
Both permissions are included in the silkspawners.* wildcard permission.

| Permission node | Description |
|:----------:|:----------:|
| silkspawners.createshop | Grants the ability to create a shop |
| silkspawners.destroyshop | Grants the ability to destroy a shop |
| silkspawners.use.* | Allows to interact with buy and sell shops |
| silkspawners.use.buy | Allows to interact with buy shops |
| silkspawners.use.sell | Allows to interact with sell shops |

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

#### The following data is **sent** to http://mcstats.org and can be seen under http://mcstats.org/plugin/SilkSpawnersShopAddon
* Metrics revision of the implementing class
* Server's GUID
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin

## Donation
[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif "Donation via PayPal")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=T9TEV7Q88B9M2)

![BitCoin](https://dl.dropboxusercontent.com/u/26476995/bitcoin_logo.png "Donation via BitCoins")
Address: 1NnrRgdy7CfiYN63vKHiypSi3MSctCP55C
