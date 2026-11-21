package net.fastersprint;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class TintControls {
    private static int drag=-1;
    private static final int WHITE=0xFFF3F1FF,MUTED=0xFF9996B5;
    private TintControls() { }
    static boolean begin(int page,double x,double y){
        if(page!=4)return false;
        if(inside(x,y,127,195,310,26))drag=0;
        else if(inside(x,y,127,247,310,26))drag=1;
        else if(inside(x,y,127,299,310,26))drag=2;
        if(drag<0)return false;update(x,y);return true;
    }
    static void update(double x,double y){
        if(drag<0)return;var setting=TintSettings.SKY;
        float h=setting.hue(),s=setting.saturation(),v=setting.value();
        float position=Math.clamp((float)(x-127)/310,0,1);
        switch(drag){case 0->h=position;case 1->s=position;case 2->v=position;}
        setting.preview(h,s,v);
    }
    static void release()throws IOException{if(drag<0)return;drag=-1;TintSettings.SKY.save();}
    private static boolean inside(double x,double y,int left,int top,int w,int h){return x>=left&&x<=left+w&&y>=top&&y<=top+h;}
    static void render(GuiGraphicsExtractor g,int page){
        g.nextStratum();var font=Minecraft.getInstance().font;var setting=TintSettings.SKY;
        float hue=setting.hue(),saturation=setting.saturation(),value=setting.value();int color=setting.color();
        g.text(font,"Sky color",127,158,WHITE);MenuPaint.rounded(g,331,150,107,24,5,color);g.text(font,String.format(java.util.Locale.ROOT,"#%06X",color&0xFFFFFF),250,158,WHITE);
        g.text(font,"Hue",127,181,MUTED);g.text(font,Math.round(hue*360)+" deg",390,181,WHITE);
        bar(g,127,200,310,hue,0xFFFF0000,0xFFFFFF00,0xFF00FF00,0xFF00FFFF,0xFF0000FF,0xFFFF00FF,0xFFFF0000);
        g.text(font,"Saturation",127,233,MUTED);g.text(font,Math.round(saturation*100)+"%",406,233,WHITE);
        bar(g,127,252,310,saturation,ColorMath.rgb(hue,0,value),ColorMath.rgb(hue,1,value));
        g.text(font,"Brightness",127,285,MUTED);g.text(font,Math.round(value*100)+"%",406,285,WHITE);
        bar(g,127,304,310,value,0xFF000000,ColorMath.rgb(hue,saturation,1));
        g.text(font,"Sky, horizon and distant haze share this color.",127,340,MUTED);
        g.text(font,"Fog distances and underwater colors stay normal.",127,358,MUTED);
    }
    private static void bar(GuiGraphicsExtractor g,int x,int y,int width,float position,int... stops){
        MenuPaint.rounded(g,x-2,y-2,width+4,18,4,0xFF4C425F);GuiColorMesh.gradient(g,x,y,width,14,stops);
        float left=x+width*position;g.fill((int)left-2,y-4,(int)left+2,y+18,0xFF181324);g.fill((int)left-1,y-3,(int)left+1,y+17,WHITE);
    }
}
