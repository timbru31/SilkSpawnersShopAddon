# SilkSpawnersShopAddon
[![Build Status](https://ci.dustplanet.de/buildStatus/icon?job=SilkSpawnersShopAddon)](https://ci.dustplanet.de/job/SilkSpawnersShopAddon/)
[![Build Status](https://travis-ci.com/timbru31/SilkSpawnersShopAddon.svg?token=xMwFbvUujsG645zQBus3&branch=master)](https://travis-ci.com/timbru31/SilkSpawnersShopAddon)
[![Circle CI](https://img.shields.io/circleci/token/bd31b6c66e67b91f26ea508cc402402e8d53c0a1/project/github/timbru31/SilkSpawnersShopAddon/master.svg)](https://circleci.com/gh/timbru31/SilkSpawnersShopAddon)

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
* Shops are stored via three different storage providers:
  * yaml
  * mongoDB
  * MySQL

*Third party features, all of them can be disabled*
* bStats for usage statistics

## License

You do not have the permission to redistribute, share, sell or make this plugin in any other way available for others.
One purchase is valid for one server, if you plan to use it on multiple servers you will need to purchase the resource again.

## Standard config
```yaml
# Valid storage methods are YAML, MONGODB, and MYSQL
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
eggMode: false
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
```

## Permissions
(Fallback to OPs, if no permissions system is found)
All permissions are included in the silkspawners.* wildcard permission.

| Permission node          | Description                                |
|:-------------------------|:-------------------------------------------|
| silkspawners.createshop  | Grants the ability to create a shop        |
| silkspawners.destroyshop | Grants the ability to destroy a shop       |
| silkspawners.use.*       | Allows to interact with buy and sell shops |
| silkspawners.use.buy     | Allows to interact with buy shops          |
| silkspawners.use.buy.*   | Allows to interact with all buy shops      |
| silkspawners.use.sell    | Allows to interact with sell shops         |
| silkspawners.use.sell.*  | Allows to interact with all sell shops     |
| silkspawners.editshop    | Allows you to edit shops via commands      |
| silkspawners.updateshops | Allows you to remove invalid shops         |


## Commands
| Command                                                             | Aliases                                                | Description                                              | Permission node          |
|:--------------------------------------------------------------------|:-------------------------------------------------------|:---------------------------------------------------------|:-------------------------|
| /silkspawnersshopaddon <mode OR mob OR price OR amount> <new value> | silkspawnersshop, silkspawnershop, sshop, ssshop, shop | Edits the shop and updates the given value               | silkspawners.editshop    |
| /silkspawnersshopaddon check                                        | same as above                                          | Searches the database for invalid shops and removes them | silkspawners.updateshops |

## Support
For support visit the [SpigotMC page](https://www.spigotmc.org/resources/12028/) or open an [issue](https://github.com/timbru31/SilkSpawnersShopAddon/issues).

## Usage statistics

_stats images are returning soon!_

## Data usage collection of bStats

#### Disabling bStats
The file `./plugins/bStats/config.yml` contains an option to *opt-out*.

#### The following data is **read and sent** to https://bstats.org and can be seen under https://bstats.org/plugin/bukkit/SilkSpawnersShopAddon
* Your server's randomly generated UUID
* The amount of players on your server
* The online mode of your server
* The bukkit version of your server
* The java version of your system (e.g. Java 8)
* The name of your OS (e.g. Windows)
* The version of your OS
* The architecture of your OS (e.g. amd64)
* The system cores of your OS (e.g. 8)
* bStats-supported plugins
* Plugin version of bStats-supported plugins
* Storage method of SilkSpawnersShopAddon
