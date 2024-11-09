package com.vinurl;

import net.minecraft.util.Identifier;

public class Helper {
	public static Identifier identifier(String modid, String name){
		//? if <1.20.5 {
		/*return new Identifier(modid, name);
		 *///?} else
		return Identifier.of(modid, name);
	}
}
