package extension.util;

//import java.net.URL;
//import java.net.URLClassLoader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
//Java

//import arc.Core;
//Arc

import extension.dependent.UTF8Control;

import static extension.data.json.Json.getData;
//GA-Exted

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//Json
 
public class LocaleUtil {

	public static String language(String o,String t,String input,Object[] params) throws Exception {
		
		Locale locale = new Locale(o,t);
		//URLClassLoader file = new URLClassLoader(new URL[] {Core.settings.getDataDirectory().child("mods/GA/resources/bundles/").file().toURI().toURL()});
		//ResourceBundle bundle = ResourceBundle.getBundle("GA", locale, file, new UTF8Control());
		ResourceBundle bundle = ResourceBundle.getBundle("bundles/GA", locale, new UTF8Control());
		//UTF-8 害死人.jpg 外置资源
		if(input !=null){
			if(params == null){
				String result = bundle.getString(input);
				return result;
			}else{
				String result = new MessageFormat(bundle.getString(input),locale).format(params);
				return result;
			}
		}
		return null;
	}

	public static String getinput(String input,String... params) {
		JSONObject date = getData("mods/GA/setting.json");
		try {
			String o = (String) date.get("languageO");
			String t = (String) date.get("languageT");
			if (params == null) {
				return language(o,t,input,null);
			}
			Object[] ps = new Object[params.length];
			for(int i=0;i<params.length;i++) {
				ps[i] = params[i];
			}
			return language(o,t,input,ps);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String Language_determination(String string) {
		switch(string){
			case "China" :return "zh_CN";
			case "Hong Kong" :return "zh_HK";
			case "Macao" :return "zh_MO";
			case "Taiwan" :return "zh_TW";
			case "Russia" :return "ru-RU";
			default :return "en_US";
			//I didn't find a better way....
		}
	}

}

