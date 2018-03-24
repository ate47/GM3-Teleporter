package com.ate.gm3teleporter;

import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C18PacketSpectate;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

@Mod(modid = ModMain.MODID, version = ModMain.VERSION)
public class ModMain
{
    public static final String MODID = "gm3teleporter";
    public static final String VERSION = "1.0";
    public static KeyBinding teleporter;
    @EventHandler
    public void init(FMLInitializationEvent event){
        ClientRegistry.registerKeyBinding(teleporter=new KeyBinding("Teleporter", Keyboard.KEY_Y, "GM3 Teleporter"));
        FMLCommonHandler.instance().bus().register(instance);
    }
    @Instance(MODID)
    public static ModMain instance;
    @SubscribeEvent
    public void onKey(KeyInputEvent ev){
    	if(teleporter.isPressed())Minecraft.getMinecraft().displayGuiScreen(new GuiTeleporter(Minecraft.getMinecraft().currentScreen));
    }
    private static final Ordering field_178674_a = Ordering.from(new Comparator()
    {
        private static final String __OBFID = "CL_00001921";
        public int func_178746_a(NetworkPlayerInfo p_178746_1_, NetworkPlayerInfo p_178746_2_)
        {
            return ComparisonChain.start().compare(p_178746_1_.getGameProfile().getId(), p_178746_2_.getGameProfile().getId()).result();
        }
        public int compare(Object p_compare_1_, Object p_compare_2_)
        {
            return this.func_178746_a((NetworkPlayerInfo)p_compare_1_, (NetworkPlayerInfo)p_compare_2_);
        }
    });
	public static String teleport(String username){
		Minecraft mc=Minecraft.getMinecraft();
		NetHandlerPlayClient nethandlerplayclient = mc.thePlayer.sendQueue;
		ArrayList<NetworkPlayerInfo> list = (ArrayList) field_178674_a.sortedCopy(nethandlerplayclient.func_175106_d());
		ArrayList<String> names=new ArrayList<String>();
		boolean b=true;
		for (NetworkPlayerInfo a : list) {
			String name  =(a.getGameProfile().getName());
			if(name.contains(username)){
				NetworkPlayerInfo currentPlayer=a;
				try {
					boolean creative=mc.thePlayer.capabilities.isCreativeMode;
					if(creative)mc.thePlayer.sendChatMessage("/gamemode 3");
					mc.thePlayer.addChatComponentMessage(
							new ChatComponentText("[Teleporter] Teleport to "+currentPlayer.getGameProfile().getName())
							.setChatStyle(
									new ChatStyle().setColor(EnumChatFormatting.DARK_GREEN)
									.setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ChatComponentText("Click to send message").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))))
									.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg "+currentPlayer.getGameProfile().getName()+" "))
								)
							);
					mc.thePlayer.sendQueue.addToSendQueue(new C18PacketSpectate(currentPlayer.getGameProfile().getId()));
					if(creative)mc.thePlayer.sendChatMessage("/gamemode 1");
				} catch (Exception e) {}
				b=false;
			}
		}
		if(b)return "Done";
		else return "No player found";
	}
}
