package extension;

import java.io.*;
import java.net.*;
import java.util.*;
//Java

import arc.*;
import arc.util.*;
import arc.util.Timer;
import arc.util.CommandHandler.*;
//Arc

import mindustry.*;
import mindustry.core.*;
import mindustry.core.GameState.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.type.*;
import mindustry.entities.type.Player;
import mindustry.game.*;
import mindustry.game.Team;
import mindustry.game.Difficulty;
import mindustry.game.EventType.*;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.net.Administration.PlayerInfo ;
import mindustry.net.Packets.KickReason;
import mindustry.net.NetConnection;
import mindustry.plugin.Plugin;
import mindustry.Vars;
//Mindustry

import static mindustry.Vars.*;
import static mindustry.Vars.player;
//
import extension.extend.translation.Googletranslate;
import extension.extend.translation.Baidutranslate;
//import extension.extend.translation.Tencenttranslate;
import extension.auxiliary.Language;
//import extension.tool.A;
//GA-Exted
import static extension.tool.HttpRequest.doGet;
import static extension.tool.HttpRequest.doCookie;
import static extension.extend.Extend.*;
import static extension.tool.Json.*;
//Static

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
//Json


public class Main extends Plugin{

	Googletranslate googletranslate = new Googletranslate();
	Baidutranslate baidutranslate = new Baidutranslate();
	Language language = new Language();
	ClientCommands clientCommands = new ClientCommands();

	public Main(){

		Events.on(EventType.PlayerChatEvent.class, e -> {
			String check = String.valueOf(e.message.charAt(0));
			//check if command
			if(!check.equals("/")) {
				boolean valid = e.message.matches("\\w+");
				JSONObject date = getData("mods/GA/setting.json");
				boolean translateo = (boolean) date.get("translateo");
				// check if enable translate
				if (!valid && translateo) {
					try{
						Thread.currentThread().sleep(2000);
						String translationa = googletranslate.translate(e.message,"en");
						Call.sendMessage("["+e.player.name+"]"+"[green] : [] "+translationa+"   -From Google Translator");
						}catch(InterruptedException ie){
							ie.printStackTrace();
						}catch(Exception ie){
							return;
						}
				}
			}
		});

		if(!Core.settings.getDataDirectory().child("mods/GA/setting.json").exists()){
			Initialization();
		}
		
		//language.language();	

//Debugging part
/*
try{
	A a = new A();
	doCookie("https://fanyi.baidu.com/");
	a.getCookie("https://fanyi.baidu.com/");
	
	//System.out.println(">>>>>>ter:"+baidutranslate.translate("engilsh","zh"));
	}catch(Exception ie){
}
//很遗憾，我尝试获取cookie，可cookie均是过期：（
*/
}
		

	@Override
	public void registerClientCommands(CommandHandler handler){

		handler.<Player>register("info",language.getinput("info"), (args, player) -> {
			String ip = Vars.netServer.admins.getInfo(player.uuid).lastIP;
			String Country = doGet("http://ip-api.com/line/"+ip+"?fields=country");
			player.sendMessage(language.getinput("info.load"));
			try{
				Thread.currentThread().sleep(2000);
				}catch(InterruptedException ie){
					ie.printStackTrace();
				}
			player.sendMessage(language.getinput("info.name",player.name));
			player.sendMessage(language.getinput("info.uuid",player.uuid));
			player.sendMessage(language.getinput("info.equipment",String.valueOf(player.isMobile)));
			player.sendMessage(language.getinput("info.ip",ip));
			player.sendMessage(language.getinput("info.country",Country));
		});

		handler.<Player>register("status",language.getinput("status"), (args, player) -> {
			player.sendMessage("FPS:"+clientCommands.status("getfps")+"  Occupied memory:"+clientCommands.status("getmemory")+"MB");
			player.sendMessage(language.getinput("status.number",String.valueOf(Vars.playerGroup.size())));
			player.sendMessage(language.getinput("status.ban",clientCommands.status("getbancount")));
		});


		handler.<Player>register("getpos",language.getinput("getpos"), (args, player) -> player.sendMessage(language.getinput("getpos.info",String.valueOf(Math.round(player.x/8)),String.valueOf(Math.round(player.y/8)))));

		handler.<Player>register("tpp","<player> <player>",language.getinput("tpp"), (args, player) -> {
			if(!player.isAdmin){
				player.sendMessage(language.getinput("admin.no"));
			} else {
				try{
					int x = Integer.parseInt(args[0])*8;
					int y = Integer.parseInt(args[1])*8;
					player.setNet((float)x, (float)y);
					player.set((float)x, (float)y);
				} catch (Exception e){
				player.sendMessage(language.getinput("tpp.fail"));
				}
			}
		});

		handler.<Player>register("tp","<player...>",language.getinput("tp"), (args, player) -> {
			Player other = Vars.playerGroup.find(p->p.name.equalsIgnoreCase(args[0]));
			if(!player.isAdmin){
				player.sendMessage(language.getinput("admin.no"));
			} else {
				if(other == null){
					player.sendMessage(language.getinput("tp.fail"));
					return;
				}
				player.setNet(other.x, other.y);
			}
		});

		handler.<Player>register("suicide",language.getinput("suicide"), (args, player) -> {
				player.onPlayerDeath(player);
				Call.sendMessage(language.getinput("suicide.tips",player.name));
		});

		handler.<Player>register("team",language.getinput("team"), (args, player) ->{
			//change team
			if(!player.isAdmin){
				player.sendMessage(language.getinput("admin.no"));
				} else {
				if (!Vars.state.rules.pvp){
					player.sendMessage(language.getinput("team.fail"));
					return;
				}
				int index = player.getTeam().id+1;
				while (index != player.getTeam().id){
					if (index >= Team.all().length){
						index = 0;
					}
					if (!Vars.state.teams.get(Team.all()[index]).cores.isEmpty()){
						player.setTeam(Team.all()[index]);
						break;
					}
					index++;
				}
				//kill player
				Call.onPlayerDeath(player);
			}

		});

		handler.<Player>register("difficulty", "<difficulty>", language.getinput("difficulty"), (args, player) -> {
			if(!player.isAdmin){
				player.sendMessage("[green]Careful: [] You're not admin!");
			} else {
				try{
					Difficulty.valueOf(args[0]);
					player.sendMessage(language.getinput("difficulty.success",args[0]));
				}catch(IllegalArgumentException e){
					player.sendMessage(language.getinput("difficulty.fail",args[0]));
				}
			}
		});

		handler.<Player>register("gameover","",language.getinput("gameover"), (args, player) -> {
			
			if(!player.isAdmin){
				player.sendMessage(language.getinput("admin.no"));
			} else {
				Events.fire(new GameOverEvent(Team.crux));
			}

		});


		handler.<Player>register("host","<mapname> [mode]",language.getinput("host"), (args, player) -> {
			if(!player.isAdmin){
				player.sendMessage(language.getinput("admin.no"));
			} else {
				String result=clientCommands.host(args[0],args[1],"N");
				if (result != "Y") {
					player.sendMessage(language.getinput("host.mode",args[1]));
				}else{
					Call.sendMessage(language.getinput("host.re"));
					clientCommands.host(args[0],args[1],"Y");
				}
			}
		});
		//It can be used normally. :)

		handler.<Player>register("runwave",language.getinput("runwave"), (args, player) -> {
			if(!player.isAdmin){
				player.sendMessage(language.getinput("admin.no"));
			} else {
				logic.runWave();
			}
		});

		handler.<Player>register("time",language.getinput("time"), (args, player) -> player.sendMessage(language.getinput("time.info",clientCommands.timee())));

		handler.<Player>register("tr","<text> <Output-language>",language.getinput("tr"), (args, player) -> {
			//No spaces are allowed in the input language??
			player.sendMessage(language.getinput("tr.tips"));
			player.sendMessage(language.getinput("tr.tips1"));
			String text = args[0].replace('-',' ');	
			try{
				Thread.currentThread().sleep(2500);
				}catch(InterruptedException ie){
					ie.printStackTrace();
				} //[Original-language],args[2]
			try{
				String translationm = googletranslate.translate(text,args[1]);
				//String translationm = baidutranslate.translate(text,args[1]);
				Call.sendMessage("["+player.name+"]"+"[green] : [] "+translationm+"   -From Google Translator");
				}catch(Exception e){
					return;
				}
			
			});
/*
		handler.<Player>register("vote", "<gameover/kick> [playername...]", "Vote", (args, player) -> {
			switch(args[0]) {
				case "kick":
					Player result = Vars.playerGroup.find(p -> p.name.equalsIgnoreCase(args[1]));
					if (result == null) {
						player.sendMessage(language.getinput("vote.err.no"));
						return;
					}
					if (result.isAdmin){
						player.sendMessage(language.getinput("vote.err.admin"));
						return;
					}
					Vote vote = new Vote(player, args[0], other);
					vote.command();
					break;
				case "gameover": 
					Player result = Vars.playerGroup.find(p -> Events.fire(new GameOverEvent(Team.crux)));
					if (result == null) {
						player.sendMessage(language.getinput("vote.err.no"));
						return;
					}
					Vote vote = new Vote(player, args[0], other);
					vote.command();
					break;
				default:
					player.sendMessage(language.getinput("vote.err.no"));
					break;
			}
		});

		handler.<Player>register("setting","<text> [text]",language.getinput("setting"), (args, player) -> {
			if(!player.isAdmin){
				player.sendMessage(language.getinput("admin.no"));
				return;
			}
			switch(args[0]) {
				case "help":
					player.sendMessage(language.getinput("setting.help"));
					break;
				case "Automatic-translation":
					JSONObject date = getData();
					if (args.length == 1 && args[0].equals("on")) {
						date.put("translateo", true);
						Core.settings.getDataDirectory().child("mods/GA/setting.json").writeString((String.valueOf(date)));
						player.sendMessage(language.getinput("setting.trr.on"));
					}else{
						date.put("translateo", false);
						Core.settings.getDataDirectory().child("mods/GA/setting.json").writeString((String.valueOf(date)));
						player.sendMessage(language.getinput("setting.trr.off"));
					}
					break;
				case "language":
					player.sendMessage(language.getinput("setting.language.info"));
					String result = setting_language(args[0],args[1]);
					if(result = "Y")
					break;
				default:
					break;
			}
		});
*/
	}

}

/*2020/1/4 10:64:33
 *本项目使用算法
 *名称					使用算法	  			来源
 *UTF8Control.Java		UTF8Control  		https://answer-id.com/52120414
 *Googletranslate.Java	Googletranslate		https://github.com/PopsiCola/GoogleTranslate
 *Main.Java 			info 				https://github.com/Kieaer/Essentials
*/
