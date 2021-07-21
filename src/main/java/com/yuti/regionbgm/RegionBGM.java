package com.yuti.regionbgm;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.yuti.regionbgm.client.config.RegionBGMConfig;
import com.yuti.regionbgm.network.PacketHandler;

/**
 * Main class for RegionBGM API mod.
 * The mod must be installed on client-side and server-side.
 * @author Yuti
 *
 */
@Mod(modid = RegionBGM.MODID, name = RegionBGM.NAME, version = RegionBGM.VERSION)
public class RegionBGM
{
    public static final String MODID = "regionbgm";
    public static final String NAME = "Region BGM";
    public static final String VERSION = "1.0.0";
    
	public final static RegionBGMConfig instance = new RegionBGMConfig();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
		PacketHandler.registerMessages(MODID);
    }
}
