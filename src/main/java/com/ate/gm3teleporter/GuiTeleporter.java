package com.ate.gm3teleporter;

import java.awt.Color;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class GuiTeleporter extends GuiScreen {
	public GuiScreen Last;
	String message;
	public GuiTextField player;
	public GuiTeleporter(GuiScreen Last){
		this.Last=Last;
	}
	public void initGui() {
		player=new GuiTextField(0, fontRendererObj, width/2-99, height/2-21, 198, 18);
		buttonList.add(new GuiButton(0, width/2-99, height/2, 100, 20, I18n.format("gui.done")));
		buttonList.add(new GuiButton(1, width/2+1, height/2, 100, 20, I18n.format("Teleport")));
		message="Teleporter";
		super.initGui();
	}
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		String s = I18n.format("Name : ");
		fontRendererObj.drawString(s, player.xPosition-fontRendererObj.getStringWidth(s), player.yPosition+player.height/2-fontRendererObj.FONT_HEIGHT/2, new Color(255, 170, 0).getRGB());
		player.drawTextBox();
		drawCenteredString(fontRendererObj, message, width/2, height/2-22-fontRendererObj.FONT_HEIGHT, new Color(85, 255, 255).getRGB());
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id==0)mc.displayGuiScreen(Last);
		if(button.id==1)ModMain.teleport(player.getText());
		super.actionPerformed(button);
	}
	public void updateScreen() {
		player.updateCursorCounter();
		super.updateScreen();
	}
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		player.textboxKeyTyped(typedChar, keyCode);
		super.keyTyped(typedChar, keyCode);
	}
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		player.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
