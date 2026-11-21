package preview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.fastersprint.ClientMenu;
import org.lwjgl.sdl.SDLMouse;
public final class VisualCheck {
 public static boolean focused=true;
 static boolean connecting,ready;static int ticks;
 public static void tick(){
  Minecraft g=Minecraft.getInstance();
  if(g.player==null||g.level==null){
   if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Menu preview","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;
  }
  g.options.pauseOnLostFocus=false;
  if(!ready){if(g.gui.screen()!=null)return;g.options.guiScale().set(2);g.options.renderDistance().set(3);ClientMenu.toggle();ready=true;}
  g.gui.toastManager().clear();
  ticks++;
  double left=(g.getWindow().getGuiScaledWidth()-460)/2.0,top=(g.getWindow().getGuiScaledHeight()-350)/2.0;
  double x=left+310+55*Math.sin(ticks*.45),y=top+190+15*Math.cos(ticks*.45);
  double sx=(double)g.getWindow().getScreenWidth()/g.getWindow().getGuiScaledWidth();
  double sy=(double)g.getWindow().getScreenHeight()/g.getWindow().getGuiScaledHeight();
  g.mouseHandler.onMove(g.getWindow().handle(),x*sx,y*sy,0,0);
  if(ticks==60)shot(g,"overview.png");
  if(ticks==80)ClientMenu.click(left+40,top+90);
  if(ticks==100)shot(g,"movement.png");
  if(ticks==110)ClientMenu.click(left+40,top+120);
  if(ticks==130)shot(g,"players.png");
  if(ticks==140)focused=false;
  if(ticks==142&&!SDLMouse.SDL_CursorVisible())throw new AssertionError("native cursor not restored on focus loss");
  if(ticks==145)focused=true;
  if(ticks==147&&SDLMouse.SDL_CursorVisible())throw new AssertionError("native arrow remains over custom dot");
  if(ticks==150){ClientMenu.toggle();g.mouseHandler.releaseMouse();if(!SDLMouse.SDL_CursorVisible())throw new AssertionError("cursor not restored after menu");System.out.println("PASS: real menu rendering, focus loss/restore and closing cursor lifecycle");}
  if(ticks==165)g.stop();
 }
 static void shot(Minecraft g,String name){Screenshot.grab(new java.io.File("/tmp/menu-preview"),name,g.gameRenderer.mainRenderTarget(),1,c->System.out.println("MENU SCREENSHOT "+name+" "+c.getString()));}
}
