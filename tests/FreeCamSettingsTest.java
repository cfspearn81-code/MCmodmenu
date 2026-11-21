import net.fastersprint.FreeCamSettings;
import com.mojang.blaze3d.platform.InputConstants;
public class FreeCamSettingsTest {
 public static void main(String[] args)throws Exception{
  if(args[0].equals("write")){
   if(FreeCamSettings.key()!=InputConstants.KEY_F6)throw new AssertionError("default F6");
   for(int invalid:new int[]{-1,0,512,InputConstants.KEY_P,InputConstants.KEY_ESCAPE}){
    try{FreeCamSettings.setKey(invalid);throw new AssertionError("accepted invalid binding");}catch(IllegalArgumentException expected){}
   }
   FreeCamSettings.setKey(InputConstants.KEY_K);
  }else if(FreeCamSettings.key()!=InputConstants.KEY_K)throw new AssertionError("binding did not reload");
  System.out.println("PASS Free Cam settings "+args[0]);
 }
}
