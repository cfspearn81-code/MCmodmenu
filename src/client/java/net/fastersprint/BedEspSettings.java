package net.fastersprint;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class BedEspSettings {
    public static final int DEFAULT_RANGE=200, MAX_RANGE=5000;
    private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("faster-bedesp.properties");
    private static boolean loaded,enabled;
    private static int range=DEFAULT_RANGE;
    private BedEspSettings() { }
    public static boolean enabled(){load();return enabled;}
    public static int range(){load();return range;}
    public static void setEnabled(boolean value)throws IOException{load();save(value,range);}
    public static void setRange(int value)throws IOException{
        if(value<1||value>MAX_RANGE)throw new IllegalArgumentException("Range must be 1–5000");
        load();save(enabled,value);
    }
    private static void load(){
        if(loaded)return;loaded=true;if(!Files.exists(FILE))return;
        try(Reader reader=Files.newBufferedReader(FILE)){
            Properties p=new Properties();p.load(reader);enabled=Boolean.parseBoolean(p.getProperty("enabled","false"));
            try{int value=Integer.parseInt(p.getProperty("range","200"));if(value>=1&&value<=MAX_RANGE)range=value;}catch(NumberFormatException ignored){}
        }catch(IOException|IllegalArgumentException failure){LoggerFactory.getLogger("fastersprint").warn("Could not read BedEsp settings",failure);}
    }
    private static void save(boolean nextEnabled,int nextRange)throws IOException{
        Files.createDirectories(FILE.getParent());Properties p=new Properties();p.setProperty("enabled",Boolean.toString(nextEnabled));p.setProperty("range",Integer.toString(nextRange));
        Path temp=Files.createTempFile(FILE.getParent(),"faster-bedesp-",".tmp");
        try{try(Writer writer=Files.newBufferedWriter(temp)){p.store(writer,"Faster BedEsp");}Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);enabled=nextEnabled;range=nextRange;}
        finally{Files.deleteIfExists(temp);}
    }
}
