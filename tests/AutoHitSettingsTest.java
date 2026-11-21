import net.fastersprint.PlayerDisplaySettings;
public final class AutoHitSettingsTest {
 public static void main(String[] args)throws Exception {
  if(args[0].equals("write")){
   if(PlayerDisplaySettings.autoHitEnabled())throw new AssertionError("default must be off");
   PlayerDisplaySettings.setAutoHitEnabled(true);PlayerDisplaySettings.setRange(700);
   PlayerDisplaySettings.setNameLimit(12);PlayerDisplaySettings.setSpeedMultiplier(2);
   PlayerDisplaySettings.setGuiMoveEnabled(true);PlayerDisplaySettings.setEnabled(false);
   PlayerDisplaySettings.setAutoBridgeEnabled(true);PlayerDisplaySettings.setAutoBridgeKey(10);
  }
  if(!PlayerDisplaySettings.autoHitEnabled()||PlayerDisplaySettings.range()!=700
    ||PlayerDisplaySettings.nameLimit()!=12||PlayerDisplaySettings.speedMultiplier()!=2
    ||!PlayerDisplaySettings.guiMoveEnabled()||PlayerDisplaySettings.enabled()
    ||!PlayerDisplaySettings.autoBridgeEnabled()||PlayerDisplaySettings.autoBridgeKey()!=10)
   throw new AssertionError("settings preservation/reload");
  System.out.println("PASS: auto-hit settings "+args[0]);
 }
}
