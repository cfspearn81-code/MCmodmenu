package check;

import java.lang.reflect.*;
import java.util.*;
import net.fastersprint.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.platform.InputConstants;

public class XrayCheck {
    static boolean connecting, ready;
    static int ticks;
    static final BlockPos[] positions = {new BlockPos(-4,82,6), new BlockPos(0,82,6), new BlockPos(4,82,6), new BlockPos(-4,84,6), new BlockPos(0,84,6), new BlockPos(4,84,6), new BlockPos(-4,86,6), new BlockPos(0,86,6), new BlockPos(4,86,6)};
    static final BlockPos far = new BlockPos(0,82,32);
    static Object field(String name) throws Exception { var f=Xray.class.getDeclaredField(name);f.setAccessible(true);return f.get(null); }
    static Set<BlockPos> cached() throws Exception {
        Set<BlockPos> result=new HashSet<>();
        for(Object chunk:((Map<?,?>)field("chunks")).values()) {
            var cf=chunk.getClass().getDeclaredField("sections");cf.setAccessible(true);
            var pf=chunk.getClass().getDeclaredField("pos");pf.setAccessible(true);
            var pos=(net.minecraft.world.level.ChunkPos)pf.get(chunk);
            Object[] sections=(Object[])cf.get(chunk);
            for(int i=0;i<sections.length;i++) {
                Object section=sections[i];if(section==null)continue;
                var size=section.getClass().getDeclaredMethod("size");size.setAccessible(true);
                var entry=section.getClass().getDeclaredMethod("entry",int.class);entry.setAccessible(true);
                for(int j=0;j<(int)size.invoke(section);j++) {
                    int local=(int)entry.invoke(section,j);
                    result.add(new BlockPos(pos.x()*16+(local&15),Minecraft.getInstance().level.getSectionYFromSectionIndex(i)*16+((local>>>8)&15),pos.z()*16+((local>>>4)&15)));
                }
            }
        }
        return result;
    }
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static void click(double x,double y){
        var g=Minecraft.getInstance();
        float scale=Math.min(1F,Math.min((g.getWindow().getGuiScaledWidth()-20F)/460,(g.getWindow().getGuiScaledHeight()-20F)/410));
        ClientMenu.click((g.getWindow().getGuiScaledWidth()-460*scale)/2+x*scale,(g.getWindow().getGuiScaledHeight()-410*scale)/2+y*scale);
    }
    static void typeRange(int value){
        click(370,123);
        for(char c:Integer.toString(value).toCharArray())ClientMenu.handleKey(InputConstants.PRESS,new KeyEvent(0,c,0));
        ClientMenu.handleKey(InputConstants.PRESS,new KeyEvent(InputConstants.KEY_RETURN,'\r',0));
    }
    public static void tick(){
        Minecraft g=Minecraft.getInstance();
        if(g.player==null||g.level==null){
            if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Xray test","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;
        }
        try{
            g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
            if(!ready){
                ready=true;g.options.guiScale().set(2);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);
                XraySettings.setEnabled(false);XraySettings.setRange(200);for(OreType ore:OreType.values())XraySettings.setOreEnabled(ore,true);
                g.player.connection.sendCommand("tp BridgeLive 0.5 81 0.5 0 0");
            }
            ticks++;g.player.setYRot(0);g.player.setXRot(-12);
            if(ticks==60){
                check(((Map<?,?>)field("chunks")).size()>0,"chunk lifecycle did not populate");
                check(OreType.of(Blocks.COPPER_ORE.defaultBlockState())==null,"copper included");
                check(OreType.of(Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState())==null,"deepslate copper included");
                for(int i=0;i<positions.length;i++)check(OreType.of(g.level.getBlockState(positions[i]))==OreType.values()[i],"wrong ore mapping "+i);
                check(OreType.of(Blocks.NETHER_GOLD_ORE.defaultBlockState())==OreType.GOLD,"nether gold missing");
                for(var b:List.of(Blocks.DEEPSLATE_COAL_ORE,Blocks.DEEPSLATE_IRON_ORE,Blocks.DEEPSLATE_GOLD_ORE,Blocks.DEEPSLATE_REDSTONE_ORE,Blocks.DEEPSLATE_LAPIS_ORE,Blocks.DEEPSLATE_DIAMOND_ORE,Blocks.DEEPSLATE_EMERALD_ORE))check(OreType.of(b.defaultBlockState())!=null,"deepslate mapping missing");
                Vec3 center=new Vec3(.5,.5,.5);
                for(int axis=0;axis<3;axis++)for(int sign:new int[]{-1,1}){
                    var p=new BlockPos(axis==0?sign*200:0,axis==1?sign*200:0,axis==2?sign*200:0);
                    check(Xray.inRange(p,center,200),"range boundary missing "+p);
                    check(!Xray.inRange(p,center,199),"outside boundary included "+p);
                }
                XraySettings.setEnabled(true);System.out.println("PASS ore mapping, copper exclusion, six-direction range boundaries");
            }
            if(ticks==140){
                var found=cached();for(var p:positions)check(found.contains(p),"ore not scanned "+p);
                check(found.contains(far),"distant ore not scanned");check(!found.contains(new BlockPos(6,83,6)),"copper cached");
                shot(g,"all-ores-through-wall.png");System.out.println("PASS default-200 actual loaded-chunk scan, "+found.size()+" ores");
            }
            if(ticks==145){ClientMenu.toggle();}
            if(ticks==150)shot(g,"overview.png");
            if(ticks==155){click(40,181);}
            if(ticks==175){check(ClientMenu.isOpen(),"menu closed");shot(g,"xray-menu-default.png");}
            if(ticks==185){
                for(OreType ore:OreType.values()){
                    click(120+ore.ordinal()%3*112,210+ore.ordinal()/3*34);check(!XraySettings.oreEnabled(ore),"toggle off failed "+ore);
                    click(120+ore.ordinal()%3*112,210+ore.ordinal()/3*34);check(XraySettings.oreEnabled(ore),"toggle on failed "+ore);
                }
                for(OreType ore:OreType.values())if(ore!=OreType.DIAMOND)XraySettings.setOreEnabled(ore,false);
                typeRange(1);check(XraySettings.range()==1,"typed minimum failed");
                typeRange(5001);check(XraySettings.range()==1,"invalid range accepted");
                typeRange(5000);check(XraySettings.range()==5000,"typed maximum failed");
                System.out.println("PASS nine UI ore toggles and typed 1/5000; 5001 rejected");
            }
            if(ticks==200){shot(g,"xray-menu-5000.png");ClientMenu.toggle();}
            if(ticks==240){
                check(((Collection<?>)field("pending")).size()<10000,"5000 range visited unloaded terrain");
                shot(g,"diamonds-only.png");
                XraySettings.setRange(1);
            }
            if(ticks==300){for(var p:cached())check(!Xray.inRange(p,g.player.position(),1),"range one included an ore outside radius");shot(g,"range-one.png");XraySettings.setRange(200);}
            if(ticks==350){
                check(cached().contains(positions[5]),"range restore missing diamond");
                g.level.setBlock(positions[5],Blocks.STONE.defaultBlockState(),3);
            }
            if(ticks==351)check(!cached().contains(positions[5]),"ore removal did not update immediately");
            if(ticks==355)g.level.setBlock(new BlockPos(0,100,0),Blocks.DIAMOND_ORE.defaultBlockState(),3);
            if(ticks==356)check(cached().contains(new BlockPos(0,100,0)),"new ore in empty section was not discovered");
            if(ticks==360)g.level.setBlock(new BlockPos(0,100,0),Blocks.AIR.defaultBlockState(),3);
            if(ticks==410){
                check(!cached().contains(positions[5]),"broken ore remained in cache");
                XraySettings.setEnabled(false);
            }
            if(ticks==415){check(cached().isEmpty(),"disabled retained cache");
                for(OreType ore:OreType.values())XraySettings.setOreEnabled(ore,true);XraySettings.setRange(200);
                System.out.println("XRAY ALL CHECKS COMPLETE: filter, range, updates and disabled cache");g.stop();}
        }catch(Exception e){throw new RuntimeException(e);}
    }
    static void shot(Minecraft g,String name){try{java.nio.file.Files.createDirectories(java.nio.file.Path.of("/tmp/xray-preview/screenshots"));}catch(java.io.IOException e){throw new RuntimeException(e);}Screenshot.grab(new java.io.File("/tmp/xray-preview"),name,g.gameRenderer.mainRenderTarget(),1,c->System.out.println("XRAY SCREENSHOT "+name+" "+c.getString()));}
}
