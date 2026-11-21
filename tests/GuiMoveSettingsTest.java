import net.fastersprint.PlayerDisplaySettings;
import java.nio.file.*;
import java.util.Properties;
public class GuiMoveSettingsTest {
 public static void main(String[] args)throws Exception {
  if(args[0].equals("legacy")){
   if(!PlayerDisplaySettings.guiMoveEnabled())throw new AssertionError("old enabled value not migrated");
   PlayerDisplaySettings.setAutoHitEnabled(true);
   var p=new Properties();try(var reader=Files.newBufferedReader(Path.of(System.getProperty("test.config"),"faster-movement.properties"))){p.load(reader);}
   if(!"true".equals(p.getProperty("guiMove.enabled")))throw new AssertionError("new setting not saved");
  }else if(PlayerDisplaySettings.guiMoveEnabled())throw new AssertionError("new false must override legacy true");
  System.out.println("PASS: GuiMove "+args[0]);
 }
}
