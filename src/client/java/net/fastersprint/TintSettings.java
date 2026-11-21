package net.fastersprint;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.slf4j.LoggerFactory;

/** Independent saved switches/colors. Dragging previews in memory; release/close commits once. */
public final class TintSettings {
    public static final TintSettings PLAYER = new TintSettings("faster-player-tint.properties", .52F, .7F, 1);
    public static final TintSettings SKY = new TintSettings("faster-sky-tint.properties", .6F, .65F, 1);
    private final String fileName;
    private boolean loaded, enabled, dirty;
    private float hue, saturation, value;
    private int color;
    private Vector3fc vector;
    private TintSettings(String fileName,float hue,float saturation,float value) { this.fileName=fileName;this.hue=hue;this.saturation=saturation;this.value=value;refresh(); }
    public boolean enabled(){load();return enabled;}
    public float hue(){load();return hue;}
    public float saturation(){load();return saturation;}
    public float value(){load();return value;}
    public int color(){load();return color;}
    public Vector3fc vector(){load();return vector;}
    private void refresh(){color=ColorMath.rgb(hue,saturation,value);vector=new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F);}
    public void preview(float hue,float saturation,float value){
        load();float h=ColorMath.unit(hue),s=ColorMath.unit(saturation),v=ColorMath.unit(value);
        if(this.hue==h&&this.saturation==s&&this.value==v)return;
        this.hue=h;this.saturation=s;this.value=v;dirty=true;refresh();
    }
    public void setEnabled(boolean next)throws IOException {load();boolean previous=enabled;enabled=next;dirty=true;try{save();}catch(IOException failure){enabled=previous;throw failure;}}
    private Path path(){return FabricLoader.getInstance().getConfigDir().resolve(fileName);}
    private void load(){
        if(loaded)return;loaded=true;Path path=path();if(!Files.exists(path))return;
        try(Reader reader=Files.newBufferedReader(path)){
            Properties p=new Properties();p.load(reader);
            float h=ColorMath.unit(Float.parseFloat(p.getProperty("hue",Float.toString(hue))));
            float s=ColorMath.unit(Float.parseFloat(p.getProperty("saturation",Float.toString(saturation))));
            float v=ColorMath.unit(Float.parseFloat(p.getProperty("value",Float.toString(value))));
            hue=h;saturation=s;value=v;enabled=Boolean.parseBoolean(p.getProperty("enabled","false"));refresh();
        }catch(IOException|IllegalArgumentException failure){LoggerFactory.getLogger("fastersprint").warn("Could not read {}",fileName,failure);}
    }
    public void save()throws IOException {
        load();if(!dirty)return;Path path=path();Files.createDirectories(path.getParent());
        Properties p=new Properties();p.setProperty("enabled",Boolean.toString(enabled));p.setProperty("hue",Float.toString(hue));p.setProperty("saturation",Float.toString(saturation));p.setProperty("value",Float.toString(value));
        Path temp=Files.createTempFile(path.getParent(),fileName,".tmp");
        try{try(Writer writer=Files.newBufferedWriter(temp)){p.store(writer,"Faster tint settings");}Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);dirty=false;}finally{Files.deleteIfExists(temp);}
    }
}
