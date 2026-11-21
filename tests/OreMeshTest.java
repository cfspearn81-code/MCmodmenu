package net.fastersprint;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
public class OreMeshTest {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  for(int x:new int[]{-5001,-1,0,1,5001})for(int y:new int[]{-5001,0,5001})for(int z:new int[]{-5001,0,5001})for(int axis=0;axis<3;axis++)for(int type=0;type<9;type++){
   long edge=OreMesh.edge(x,y,z,axis,type);check(OreMesh.x(edge)==x&&OreMesh.y(edge)==y&&OreMesh.z(edge)==z&&OreMesh.axis(edge)==axis&&OreMesh.type(edge)==type,"edge encoding");
  }
  var edges=new LongOpenHashSet();OreMesh.addBox(edges,0,0,0,0);check(edges.size()==12,"cube must have twelve edges");
  OreMesh.addBox(edges,1,0,0,0);check(edges.size()==20,"adjacent cubes must share four edges");
  edges.clear();OreMesh.addBox(edges,0,0,0,0);OreMesh.addBox(edges,1,0,0,1);check(edges.size()==24,"different colors must retain their edges");
  edges.clear();for(int x=0;x<16;x++)for(int y=0;y<16;y++)for(int z=0;z<16;z++)OreMesh.addBox(edges,x,y,z,0);
  check(edges.size()==3*16*17*17,"dense section edge count");
  var section=new OreSection();section.update(0,0);section.update(4095,8);check(section.size()==2,"coal at origin and max local index");
  check(!section.update(0,0),"unchanged event must be free");section.update(0,5);section.update(4095,-1);check(section.size()==1&&section.entry(0)==(5<<12),"replace/remove");section.update(0,-1);check(section.size()==0,"remove last");
  for(int i=0;i<4096;i++)section.update(i,0);
  for(int i=0;i<4096;i+=2)section.update(i,-1);
  for(int i=0;i<4096;i+=2)section.update(i,8);
  check(section.size()==4096,"dense update index lost entries");
  for(int i=0;i<section.size();i++){int entry=section.entry(i);check((entry>>>12)==((entry&1)==0?8:0),"dense replacement index corrupt");}
  System.out.println("PASS: edge encoding, deduplication, dense mesh and incremental ore updates");
 }
}
