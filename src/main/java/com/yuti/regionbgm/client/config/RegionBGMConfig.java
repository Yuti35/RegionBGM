package com.yuti.regionbgm.client.config;

import com.yuti.regionbgm.RegionBGM;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.SlidingOption;
import net.minecraftforge.common.config.ConfigManager;

@Config(modid = RegionBGM.MODID, name=RegionBGM.MODID)
public class RegionBGMConfig {
	
	public void save() {
		ConfigManager.sync(RegionBGM.MODID, Config.Type.INSTANCE);
	}
	
	@SlidingOption()
	@RangeInt(min = 0, max = 100) 
	@Config.Comment("Region BGM volume")
	public static int volume = 100;
	
	@Config.Comment("Enables or disables the mod")
	public static boolean enabled = true;
}
