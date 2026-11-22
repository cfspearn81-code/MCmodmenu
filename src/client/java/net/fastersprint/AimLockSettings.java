package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class AimLockSettings {
    private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("faster-aimlock.properties");
    private static boolean loaded,enabled;
    private static int key=InputConstants.KEY_R;
    private AimLockSettings() { }
    public static boolean enabled(){load();return enabled;}
    public static int key(){load();return key;}
    public static boolean valid(int value){return value>=InputConstants.KEY_A&&value<512&&value!=InputConstants.KEY_P&&value!=InputConstants.KEY_ESCAPE;}
    public static void setEnabled(boolean next)throws IOException{load();save(next,key);}
    public static void setKey(int next)throws IOException{if(!valid(next))throw new IllegalArgumentException("Choose a key other than P or Escape");load();save(enabled,next);}
    private static void load(){
        if(loaded)return;loaded=true;if(!Files.exists(FILE))return;
        try(Reader reader=Files.newBufferedReader(FILE)){
            Properties p=new Properties();p.load(reader);enabled=Boolean.parseBoolean(p.getProperty("enabled","false"));
            try{int value=Integer.parseInt(p.getProperty("key",Integer.toString(key)));if(valid(value))key=value;}catch(NumberFormatException ignored){}
        }catch(IOException|IllegalArgumentException failure){LoggerFactory.getLogger("fastersprint").warn("Could not read Aim Lock settings",failure);}
    }
    private static void save(boolean nextEnabled,int nextKey)throws IOException{
        Files.createDirectories(FILE.getParent());Properties p=new Properties();p.setProperty("enabled",Boolean.toString(nextEnabled));p.setProperty("key",Integer.toString(nextKey));
        Path temp=Files.createTempFile(FILE.getParent(),"faster-aimlock-",".tmp");
        try{try(Writer writer=Files.newBufferedWriter(temp)){p.store(writer,"Faster Aim Lock (hold key, SDL scancode)");}Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);enabled=nextEnabled;key=nextKey;}finally{Files.deleteIfExists(temp);}
    }
}
