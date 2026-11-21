package net.fastersprint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fastersprint.mixin.GuiRenderStateAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Event-driven client cache. Unchanged sections are scanned once, never each second. */
public final class Xray {
    private static final int MAX_OUTLINES=4096;
    private static final OreSection EMPTY=new OreSection();
    private static ClientLevel world;
    private static final Map<ChunkPos,CachedChunk> chunks=new HashMap<>();
    private static final ArrayDeque<Work> pending=new ArrayDeque<>();
    private static int scanRange, scanSection, lastMask, meshRange;
    private static boolean queueDirty, meshDirty=true;
    private static Vec3 scanCenter=Vec3.ZERO, meshCenter=Vec3.ZERO;
    private static float meshYaw,meshPitch;
    private static long nextMesh;
    private static OreMesh mesh=OreMesh.EMPTY;
    private static final Matrix4f projection=new Matrix4f();

    private Xray() { }
    private static void adopt(ClientLevel level) {
        if(world==level)return;
        world=level;chunks.clear();pending.clear();scanRange=0;meshRange=0;lastMask=0;mesh=OreMesh.EMPTY;nextMesh=0;queueDirty=true;invalidate();
    }
    private static void invalidate() { meshDirty=true; }
    private static void clearScan() {
        for(var chunk:chunks.values()){Arrays.fill(chunk.sections,null);chunk.unscanned.set(0,chunk.sections.length);}
        pending.clear();scanRange=0;mesh=OreMesh.EMPTY;invalidate();
    }
    public static void chunkLoaded(ClientLevel level,ChunkPos pos) {
        adopt(level);chunks.put(pos,new CachedChunk(pos,level.getSectionsCount()));queueDirty=true;invalidate();
    }
    public static void chunkUnloaded(ClientLevel level,ChunkPos pos) {
        if(world!=level)return;
        chunks.remove(pos);queueDirty=true;invalidate();
        // Discard the selected mesh immediately; it may contain the unloaded chunk.
        mesh=OreMesh.EMPTY;
    }
    public static void blockChanged(ClientLevel level,BlockPos pos,BlockState state) {
        if(world!=level||scanRange==0)return;
        var chunk=chunks.get(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
        if(chunk==null)return;
        int index=world.getSectionIndex(pos.getY());
        if(index<0||index>=chunk.sections.length)return;
        OreSection section=chunk.sections[index];
        if(section==null)return; // Not scanned yet; the queued scan will read the current state.
        OreType ore=OreType.of(state);
        if(section==EMPTY){if(ore==null)return;section=new OreSection();chunk.sections[index]=section;}
        int local=(pos.getX()&15)|((pos.getZ()&15)<<4)|((pos.getY()&15)<<8);
        if(section.update(local,ore==null?-1:ore.ordinal())){
            invalidate();nextMesh=0; // Next extraction reflects this change, without a full rescan.
        }
    }
    public static void tick() {
        Minecraft game=Minecraft.getInstance();adopt(game.level);
        if(!XraySettings.enabled()||game.player==null||world==null){if(scanRange!=0)clearScan();return;}
        int mask=XraySettings.mask();
        if(mask!=lastMask){lastMask=mask;invalidate();nextMesh=0;}
        if(mask==0)return;
        Vec3 center=game.player.position();int range=XraySettings.range();
        if(queueDirty||range!=scanRange||center.distanceToSqr(scanCenter)>=64)prepare(center,range);
        if(pending.isEmpty())return;
        long deadline=System.nanoTime()+350_000;
        int populated=0;
        for(int checked=0;checked<32&&populated<4&&!pending.isEmpty();checked++){
            Work work=pending.peekFirst();CachedChunk cached=work.chunk;
            if(chunks.get(cached.pos)!=cached||cached.unscanned.isEmpty()){pending.removeFirst();continue;}
            int offset=work.offset++;
            if(work.offset>=cached.sections.length*2)pending.removeFirst();
            int index=scanSection+((offset&1)==1?-(offset+1)/2:offset/2);
            if(index<0||index>=cached.sections.length||cached.sections[index]!=null)continue;
            int y=world.getSectionYFromSectionIndex(index)*16;
            if(!sectionInRange(cached.pos.x()*16,y,cached.pos.z()*16,center,range))continue;
            LevelChunk chunk=world.getChunkSource().getChunk(cached.pos.x(),cached.pos.z(),ChunkStatus.FULL,false);
            if(chunk==null)continue;
            LevelChunkSection section=chunk.getSection(index);
            if(section.hasOnlyAir()||!section.maybeHas(state->OreType.of(state)!=null)){cached.sections[index]=EMPTY;}
            else{
                populated++;
                OreSection found=new OreSection();
                for(int py=0;py<16;py++)for(int pz=0;pz<16;pz++)for(int px=0;px<16;px++){
                    OreType type=OreType.of(section.getBlockState(px,py,pz));
                    if(type!=null)found.add(px|(pz<<4)|(py<<8),type.ordinal());
                }
                cached.sections[index]=found.size()==0?EMPTY:found;invalidate();
            }
            cached.unscanned.clear(index);
            if(System.nanoTime()>=deadline)break;
        }
    }
    private static void prepare(Vec3 center,int range) {
        scanCenter=center;scanRange=range;queueDirty=false;invalidate();
        scanSection=Math.clamp(world.getSectionIndex((int)Math.floor(center.y)),0,world.getSectionsCount()-1);
        // Sort only loaded chunks, then generate section work incrementally within the tick budget.
        // A radius of 5000 never allocates/sorts a work item for every section up front.
        List<CachedChunk> work=new ArrayList<>();
        int minIndex=Math.max(0,world.getSectionIndex((int)Math.floor(center.y-range)));
        int maxIndex=Math.min(world.getSectionsCount()-1,world.getSectionIndex((int)Math.floor(center.y+range)));
        for(var chunk:chunks.values()) {
            int unscanned=chunk.unscanned.nextSetBit(minIndex);
            if(unscanned<0||unscanned>maxIndex)continue;
            double dx=Math.max(0,Math.abs(chunk.pos.x()*16+8-center.x)-8);
            double dz=Math.max(0,Math.abs(chunk.pos.z()*16+8-center.z)-8);
            if(dx*dx+dz*dz>(double)range*range)continue;
            for(int i=unscanned;i>=0&&i<=maxIndex;i=chunk.unscanned.nextSetBit(i+1)) {
                if(sectionInRange(chunk.pos.x()*16,world.getSectionYFromSectionIndex(i)*16,chunk.pos.z()*16,center,range)){work.add(chunk);break;}
            }
        }
        work.sort(Comparator.comparingDouble(c->distance(c.pos.x()*16,(int)center.y-8,c.pos.z()*16,center)));
        pending.clear();for(var chunk:work)pending.addLast(new Work(chunk));
    }
    private static double distance(int x,int y,int z,Vec3 center){double dx=x+8-center.x,dy=y+8-center.y,dz=z+8-center.z;return dx*dx+dy*dy+dz*dz;}
    private static boolean sectionInRange(int x,int y,int z,Vec3 center,int range){
        double dx=Math.max(0,Math.abs(x+8-center.x)-8),dy=Math.max(0,Math.abs(y+8-center.y)-8),dz=Math.max(0,Math.abs(z+8-center.z)-8);
        return dx*dx+dy*dy+dz*dz<=(double)range*range;
    }
    public static boolean inRange(BlockPos pos,Vec3 center,int range){return pos.distToCenterSqr(center)<=(double)range*range;}
    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft game=Minecraft.getInstance();
        if(!XraySettings.enabled()||world==null||game.level!=world||game.player==null||XraySettings.mask()==0
                ||game.gui.hud.isHidden()||game.gui.screen()!=null||ClientMenu.isOpen())return;
        var camera=game.gameRenderer.mainCamera();if(!camera.isInitialized())return;
        Vec3 center=game.player.position(),cameraPos=camera.position();
        camera.getViewRotationProjectionMatrix(projection);
        int range=XraySettings.range(),mask=XraySettings.mask();long now=System.nanoTime();
        boolean changedView=center.distanceToSqr(meshCenter)>.25||Math.abs(camera.yRot()-meshYaw)>4||Math.abs(camera.xRot()-meshPitch)>4;
        if(range!=meshRange||mask!=lastMask){meshDirty=true;nextMesh=0;lastMask=mask;}
        if(now>=nextMesh&&(meshDirty||changedView)){
            select(center,cameraPos,range,mask);
            meshCenter=center;meshYaw=camera.yRot();meshPitch=camera.xRot();meshRange=range;meshDirty=false;nextMesh=now+100_000_000;
        }
        if(mesh.edges.length==0)return;
        ((GuiRenderStateAccessor)graphics).fastersprint$guiRenderState().addGuiElement(new OreOutlineState(mesh,projection,cameraPos,graphics.pose(),graphics.guiWidth(),graphics.guiHeight()));
    }
    private static void select(Vec3 center,Vec3 camera,int range,int mask){
        OreProjection frustum=new OreProjection(projection);
        List<Candidate> ordered=new ArrayList<>();
        for(var chunk:chunks.values())for(int i=0;i<chunk.sections.length;i++){
            var section=chunk.sections[i];if(section==null||section.size()==0)continue;
            int x=chunk.pos.x()*16,y=world.getSectionYFromSectionIndex(i)*16,z=chunk.pos.z()*16;
            if(sectionInRange(x,y,z,center,range)&&frustum.visible(x-camera.x,y-camera.y,z-camera.z,16))ordered.add(new Candidate(section,x,y,z,distance(x,y,z,center)));
        }
        ordered.sort(Comparator.comparingDouble(Candidate::distance));
        int originX=(int)Math.floor(center.x),originY=(int)Math.floor(center.y),originZ=(int)Math.floor(center.z);
        LongOpenHashSet edges=new LongOpenHashSet();int count=0;
        outer:for(var candidate:ordered)for(int i=0;i<candidate.section.size();i++){
            int entry=candidate.section.entry(i),type=entry>>>12;if((mask&(1<<type))==0)continue;
            int x=candidate.x+(entry&15),y=candidate.y+((entry>>>8)&15),z=candidate.z+((entry>>>4)&15);
            double dx=x+.5-center.x,dy=y+.5-center.y,dz=z+.5-center.z;
            if(dx*dx+dy*dy+dz*dz>(double)range*range||!frustum.visible(x-camera.x,y-camera.y,z-camera.z,1))continue;
            OreMesh.addBox(edges,x-originX,y-originY,z-originZ,type);
            if(++count>=MAX_OUTLINES)break outer;
        }
        mesh=edges.isEmpty()?OreMesh.EMPTY:new OreMesh(originX,originY,originZ,edges.toLongArray());
    }
    private static final class CachedChunk {
        final ChunkPos pos;final OreSection[] sections;final BitSet unscanned;
        CachedChunk(ChunkPos pos,int count){this.pos=pos;sections=new OreSection[count];unscanned=new BitSet(count);unscanned.set(0,count);}
    }
    private static final class Work {
        final CachedChunk chunk;int offset;
        Work(CachedChunk chunk){this.chunk=chunk;}
    }
    private record Candidate(OreSection section,int x,int y,int z,double distance) { }
}
