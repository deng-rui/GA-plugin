package extension.core;

import java.io.*;
import java.util.List;
import java.util.Arrays;
//Java

import arc.Core;
//Arc

import mindustry.maps.Map;
import mindustry.maps.Maps.*;
//Mindustry

import static mindustry.Vars.maps;
//Mindustry-Static

import extension.data.global.Config;
import extension.util.LogUtil;
import extension.util.file.FileUtil;
//GA-Exted

import static extension.data.db.SQLite.Player_Privilege_classification;
import static extension.data.db.SQLite.InitializationSQLite;
import static extension.data.global.Lists.addMaps_List;
import static extension.data.global.Lists.EmptyMaps_List;
import static extension.data.global.Lists.getMaps_List;
import static extension.data.json.Json.Initialization;
import static extension.data.json.Json.Initialize_permissions;
import static extension.dependent.Librarydependency.importLib;
//Static

public class Initialization {
	public static void Start_Initialization() {
		Data();
		SQL();
		Json();
	}

	public static void Follow_up_Initialization() {
		MapList();
	}

	private static void SQL() {
		importLib("org.xerial","sqlite-jdbc","3.30.1",Config.Plugin_Lib_Path);
		//notWork("sqlite-jdbc","3.30.1",Core.settings.getDataDirectory().child("mods/GA/Lib/"));
		if(!Core.settings.getDataDirectory().child("mods/GA/Data.db").exists())InitializationSQLite();
	}

	private static void Json() {
		if(!Core.settings.getDataDirectory().child("mods/GA/setting.json").exists())Initialization();
		if(!Core.settings.getDataDirectory().child("mods/GA/Authority.json").exists())Initialize_permissions();
		Player_Privilege_classification();
	}

	private static void Data() {
		try {
			List file = (List)FileUtil.readfile(true,new InputStreamReader(Initialization.class.getResourceAsStream("/other/FileList.txt"), "UTF-8"));
			for(int i=0;i<file.size();i++){
				//LogUtil.info((String)file.get(i));
				if(!FileUtil.File(Config.Plugin_Resources_Path+(String)file.get(i)).exists()) {
					//IPR必须加上/
					String a = (String)FileUtil.readfile(false,new InputStreamReader(Initialization.class.getResourceAsStream((String)file.get(i)), "UTF-8"));
					FileUtil.writefile(a,false);
					LogUtil.info("Defect : Start creating write external resource File",Config.Plugin_Resources_Path+(String)file.get(i));
				}
			}
		}catch(UnsupportedEncodingException e){
			LogUtil.fatal("File write error",e);
		}  
	}

	public static void MapList() {
		EmptyMaps_List();
		if(!maps.all().isEmpty()){
			for(Map map : maps.all()){
				if(map.custom) {
					switch(String.valueOf(map.file.name().charAt(0))){
						case "P" :
						case "p" :
							addMaps_List(map.name().replace(' ', '_')+" "+map.width+"x"+map.height+" pvp"+" P");
							break;
						case "S" :
						case "s" :
							addMaps_List(map.name().replace(' ', '_')+" "+map.width+"x"+map.height+" survival"+" S");
							break;
						case "A" :
						case "a" :
							addMaps_List(map.name().replace(' ', '_')+" "+map.width+"x"+map.height+" attack"+" A");
							break;
					}			
				}
			}
		} else {
			addMaps_List("The map is empty");
		}
	}
}