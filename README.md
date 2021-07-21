# Region BGM API

Welcome to the **RegionBGM** API repository!

This mod can be used by others mods as an API in order to stream background music to players, from different sources (youtube, twitch, almost anything).

It can be used in different ways. For example, defining zones with a background music.

The mod use the [lavaplayer libray](https://github.com/sedmelluq/lavaplayer) from **sedmelluq** !

For more information about the libraries used by the mod, please check the **LICENSE-DEPENDENCIES** and **NOTICE** files.

Currently the mod is made for **1.12.2**.

You can download the mod on [curseforge](https://www.curseforge.com/minecraft/mc-mods/regionbgm-api).

## Mods using the API

The RegionBGM mod is useless alone, it requires to have another mod installed to be able to exploit it.

Usually, you need to install RegionBGM on client-side and on server-side, and the mod exploiting it on server-side.

For now, here's the list of the mods using RegionBGM :

* [Zone BGM](https://www.curseforge.com/minecraft/mc-mods/forge-essentials-zone-bgm) : A mod using Forge Essentials and RegionBGM in order to define zones where music can be played. It adds immersion in different zones of your server (city, pirate ships, etc...). The mod is required on server-side only.

I hope more mods will come later!

## Configuration

You can edit two parameters in the mod's configuration GUI :

* **enabled** : Enables or disables the mod.
* **volume**: Set the background music volume.

In-game, each mod using the RegionBGM API is associated to a gamerule **bgm_modid** which is set to **true** by default. If this rule is set on false, the mod using the **RegionBGM API** will be "disabled" (won't be able to send packets to clients and triggers musics).

## Developers

You want to use the RegionBGM API in your mod ? No problem ! 

The mod is brand new, it has not been published in a Maven repository yet.

You can use a **deobf jar** as a dependency, for now.

### Using the API in your code

#### Setup the API

This API should be used **on server-side**. It will send packets to clients.

To setup and use the API in your code, simply get an instance of  **APIRegionBGM** for your mod :

```java
APIRegionBGM API = APIRegionBGM.getInstance("yourmodid");

//During server initialization
API.registerOnServer(server);
```
#### Play music

You can use the **playResource** in order to stream a music to a player.

```java
void playResource(World world, EntityPlayerMP player, String resource, boolean looping)
```
This method uses 4 parameters :

* **world** : The world where the player is
* **player** : The targeted player
* **resource** : The resource you want to be played (an URL, youtube, twitch, almost anything)
* **looping** : Tells is the resource should loop or not

Example with a youtube URL :

```java
API.playResource(world, player, "https://www.youtube.com/watch?v=NH-GAwLAO30", true);
```

#### Stop music

You can ask a client to stop the music currently playing.

```java
void stopMusic(World world, EntityPlayerMP player) 
```

This method uses 2 parameters :

* **world** : The world where the player is
* **player** : The targeted player

#### PlayerAskRefreshBgmEvent handling

When a player reactivate the mod trough the configuration, you may want to be informed on server-side to do an action. (for example, in [Zone BGM](https://www.curseforge.com/minecraft/mc-mods/forge-essentials-zone-bgm) : play the current zone music to the player).

You simply need to handle the **PlayerAskRefreshBgmEvent** which will provides you the player asking for the refresh.

### Javadoc

You can find the Javadoc of the mod here : [RegionBGM documentation](https://yuti35.github.io/RegionBGM/)

### Advanced example

Here's an example of how to register and use the API in your code.

#### Mod initialization

Initialize and register the API (for example, during the mod and server initialization).

```java
@Mod(modid = MyMod.MODID, name = MyMod.NAME, version = MyMod.VERSION)
public class MyMod
{
    public static final String MODID = "mymodid";
    public static final String NAME = "My mod name";
    public static final String VERSION = "1.0";
    
    //Initialization of the API
    public static final ApiRegionBGM ApiBGM = ApiRegionBGM.getInstance(MODID);
    
    @EventHandler
    public static void initServer(FMLServerStartingEvent event) {
    	//Register the gamerule
    	ApiBGM.registerOnServer(event.getServer());
    	ServerHandler serverHandler = new ServerHandler();
    	MinecraftForge.EVENT_BUS.register(serverHandler);
   }
}
```

#### Use the API

Example of usage, in an event handler.

We want to play the music from the URL the user typed in chat and stop the music if they type "stop".

We don't want the music to loop (so we'll specify **false** for the looping parameter)

We also want to intercept the **PlayerAskRefreshBgmEvent** event to do something (in our example : play the 1-UP sound from Mario).

Remember : **The API must be used on server-side**.

```java
public class ServerHandler {

	@SubscribeEvent
	public void onTalk(ServerChatEvent event) {
		String message = event.getMessage();
		EntityPlayer player = event.getPlayer();
		World world = player.getEntityWorld();
		if(message.equals("stop")) {
			MyMod.ApiBGM.stopMusic(world, player);
		}
		else {
			MyMod.ApiBGM.playResource(world, player, message, false);
		}
	}
	
	@SubscribeEvent
	public void onAskRefresh(PlayerAskRefreshBgmEvent event) {
		EntityPlayer player = event.getPlayer();
		World world = player.getEntityWorld();
		MyMod.ApiBGM.playResource(world, player, "https://www.youtube.com/watch?v=o3Tlv7h9I3Y", false);
	}
}
```

### Supported sources

The music can be a source from internet : A youtube video, a youtube playlist, a twitch livestream...

As the the mod uses [lavaplayer](https://github.com/sedmelluq/lavaplayer) you can check all the supported sources [here](https://github.com/sedmelluq/lavaplayer#supported-formats). 
