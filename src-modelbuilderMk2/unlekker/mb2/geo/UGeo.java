/*
 * modelbuilderMk2
 */
package unlekker.mb2.geo;

import java.util.ArrayList;

import unlekker.mb2.util.UMB;

import java.util.*;

import processing.core.PImage;

public class UGeo extends UMB  {
  /**
   * Master vertex list, contains all vertices for the face geometry
   * contained in the UGeo instance. Use <code>enable(NODUPL)</code>
   * to ensure no duplicate vertices exist. 
   */
  public UVertexList vl;
  
  /**
   * ArrayList of triangle faces. 
   */
  public ArrayList<UFace> faces;

  private UVertexList vltmp;
  private int shapeType;
  public ArrayList<UGeoGroup> faceGroups;
  
  public UEdgeList edges;

  
  public UGeo() {
    vl=new UVertexList();
    setOptions(NODUPL);
    faces=new ArrayList<UFace>();
    faceGroups=new ArrayList<UGeoGroup>();
  }

  public UGeo(UGeo v) {
    this();
    set(v);
  }

  /** Returns a copy of this UGeo instance.
   *  
   * @return
   */
  public UGeo copy() {
    return new UGeo(this);
  }

  public UGeo clear() {
    vl.clear();
    faces.clear();
    faceGroups.clear();
    return this;
  }

  public UGeo remove(int faceID) {
    if(faceID<sizeF()) {
      UFace ff=faces.get(faceID);
      faces.remove(ff);
      for(UGeoGroup gr:faceGroups) {
        gr.remove(ff);
      }
    }
    return this;
  }

  public UGeo remove(UFace ff) {
    int pos=faces.indexOf(ff);
    if(pos>-1) remove(pos);
    return this;
  }
  
  public UGeo removeDupl() {
    for(UFace ff:faces) ff.getV();
    getV().removeDupl(true);
    for(UFace ff:faces) {
      ff.getVID();
      ff.getV(true);
    }
    
    return this;
  }

  
  public UGeo setOptions(int opt) {
    super.setOptions(opt);
    if(vl!=null) vl.setOptions(opt);
    return this;
  }

  public UGeo enable(int opt) {
    super.setOptions(opt);
    vl.setOptions(opt);
    return this;
  }

  public UGeo disable(int opt) {
    super.disable(opt);
    return this;
  }

  /**
   * Copies the mesh data contained <code>model</code> to this UGeo instance,
   * replacing any existing data. The <code>model.vl</code> vertex list
   * is copied using {@see UVertexList#copy()}, then face data is copied
   * by creating new UFace instances using the {@see UFace#vID} vertex indices. 
   *  
   * @param model
   * @return
   */
  public UGeo set(UGeo model) {
   vl=model.getV().copy();
   faces=new ArrayList<UFace>();
   for(UFace ff:model.getF()) {
     UFace newFace=new UFace(this,ff.vID);
     newFace.setColor(ff.col);
     addFace(newFace);
   }
   return this;
  }
  
  public UGeo setColor(int col) {
    for(UFace ff:getF()) ff.setColor(col);
    return this;
  }


  private UGeo groupBegin() {
    return groupBegin(-1);
  }

  public UGeo groupBegin(int type) {
    if(groupTypeNames==null) {
      groupTypeNames=new HashMap<Integer, String>();
      groupTypeNames.put(TRIANGLE_FAN, "TRIANGLE_FAN");
      groupTypeNames.put(TRIANGLES, "TRIANGLES");
      groupTypeNames.put(QUAD_STRIP, "QUAD_STRIP");
      groupTypeNames.put(QUADS, "QUADS");
    }
    
    faceGroups.add(new UGeoGroup(this,type).begin());
    log("groupBegin "+groupTypeNames.get(type)+" "+faceGroups.size()+" "+sizeF());
    return this;
  }

  public UGeo groupEnd() {
    faceGroups.get(faceGroups.size()-1).end();
    return this;
  }

  
  public UGeoGroup getGroupID(int id) {
    return faceGroups.get(id);
  }

  /**
   * Returns a list of UFace instances belonging to a given 
   * group of faces. 
   * 
   * When groups of triangles are added to UGeo by methods like
   * triangleFan(), quadstrip() or add(UGeo), the start and end IDs
   * for the faces produced by that operation is stored internally so
   * that the group can later be retrieved.
   * 
   * @param id
   * @return
   */
  
  public ArrayList<UFace> getGroupF(int id) {
    return faceGroups.get(id).getF();
  }

  public UGeoGroup getGroup(int id) {
    return faceGroups.get(id);
  }

  public UVertexList getGroupV(int id) {
    return getGroup(id).getV();
  }


  public int sizeGroup() {
    return faceGroups.size();
  }



  public UGeo add(UGeo model) {
    log(model.str());
    
    taskTimerStart("UGeo.add(UGeo)");
    
    groupBegin(TRIANGLES);
    int cnt=0;
    ArrayList<UFace> theFaces=model.getF();
    for(UFace ff:theFaces) {
      ff.v=null;
      
      UVertex vv[]=ff.getV();
      addFace(vv);
      getF(sizeF()-1).setColor(ff.col);
      taskTimerUpdate(map(cnt++,0,theFaces.size()-1, 0,100));
    }    
    groupEnd();
    
    taskTimerDone();
    
/*    if(model.sizeGroup()>0) {
      int gn=model.sizeGroup();
      for(int i=0; i<gn; i++) {
        int id[]=model.getGroupID(i);
        groupBegin(id[2]);
        ArrayList<UFace> gr=model.getGroup(i);
        for(UFace ff:gr) {
          UVertex vv[]=ff.getV();
          addFace(vv);
        }    
        groupEnd();
      }
    }
    
    else {
      for(UFace ff:model.getF()) {
        UVertex vv[]=ff.getV();
        addFace(vv);
      }    
    }
*/    
    return this;
    
  }
  
  //////////////////////////////////////////
  // BOUNDING BOX, DIMENSIONS 


  public UBB bb() {
    return bb(false);
  }

  public UBB bb(boolean force) {
    return vl.bb(force);
  }

  /** 
   * @return The centroid of this mesh, found by calling
   * <code>vl.bb().centroid()</code>.
   */
  public UVertex centroid() {
    return bb().centroid;
  }

  /** 
   * Translates mesh so that its centroid lies at
   * the origin.
   */
  public UGeo center() {
    return translateNeg(bb().centroid);
  }

  public static void center(ArrayList<UGeo> models) {
    UBB btmp=new UBB();
    for(UGeo geo:models) btmp.add(geo.bb());
    btmp.calc();
    UVertex c=btmp.centroid;
    for(UGeo geo:models) geo.translateNeg(c);
    
  }

  /**
   * Translates mesh so that its centroid lies at
   * at the provided point in space.
   * @param v1 Point where mesh will be centered
   * @return
   */
  public UGeo centerAt(UVertex v1) {
    return center().translate(v1);
  }

  /**
   * Translate mesh by the negative of the given vertex,
   * equivalent to <code>translate(-v1.x,-v1.y,-v.z)</code>.
   * @param v1
   * @return
   */
  public UGeo translateNeg(UVertex v1) {
    return translate(-v1.x,-v1.y,-v1.z);    
  }

  public UGeo translate(UVertex v1) {    
    return translate(v1.x,v1.y,v1.z);    
  }

  public UGeo translate(float mx,float my) {
    return translate(mx,my,0);
  }
  
  public UGeo translate(float mx,float my,float mz) {
    vl.translate(mx, my, mz);
    return this;    
  }
  
  public UGeo rotX(float deg) {
    vl.rotX(deg);
    return this;
  }

  public UGeo rotY(float deg) {
    vl.rotY(deg);
    return this;
  }

  public UGeo rotZ(float deg) {
    vl.rotZ(deg);
    return this;
  }

  public UGeo scaleToDim(float max) {
    float m=max/bb().dimMax();
    return scale(m,m,m);
  }

  public UGeo scale(float m) {return scale(m,m,m);}

  public UGeo scale(float mx,float my,float mz) {
    vl.scale(mx,my,mz);
    return this;
  }

  /**
   * Returns UVertex instance where x,y,z represent the
   * size of this mesh in the X,Y,Z dimensions.  
   * @return
   */
  public UVertex dim() {
    return bb().dim;
  }


  public float dimX() {return bb().dimX();}
  public float dimY() {return bb().dimY();}
  public float dimZ() {return bb().dimZ();}
  public float dimMax() {return bb().dimMax();}


  //////////////////////////////////////////
  // LIST TOOLS


  /**
   * @param models List of UGeo instances.
   * @return The total number of faces contained in all
   * meshes in the list. 
   */
  public static int sizeF(ArrayList<UGeo> models) {
    int n=0;
    for(UGeo gg:models) if(gg!=null) n+=gg.sizeF();
    return n;
  }

  /**
   * @return Number of triangle faces contained in this mesh.
   */
  public int sizeF() {
    return faces.size();
  }

  /**
   * @return Number of vertices contained in this mesh.
   */
  public int sizeV() {
    return vl.size();
  }

  public UEdgeList getEdgeList() {
    if(edges==null) edges=new UEdgeList(this);
    return edges;
  }
  
  public ArrayList<UFace> getF() {
    return faces;
  }

  public UFace getF(int id) {
    return faces.get(id);
  }

  /**
   * @return A direct reference to the {@link UVertexList} that
   * is the master vertex list for this mesh.
   */
  public UVertexList getV() {
    return vl;
  }
  
  public UVertex getV(int id) {
    return vl.get(id);
  }

  public static UMB drawModels(ArrayList<UGeo> models) {
    for(UGeo geo:models) geo.draw();
    return UMB.UMB;

  }

  public UGeo drawTextured(PImage texture) {
    if(checkGraphicsSet()) {
      g.beginShape(TRIANGLES);
      g.texture(texture);
      g.textureMode(g.NORMAL);
      
      for(UFace f:faces) {
        UVertex vv[]=f.getV();
        pvertex(vv, true);
      }
      
      g.endShape();
    }
    return this;
  }
    
  public UGeo draw() {
    return draw(options);
  }

  public UGeo drawNormals(float w) {
    for(UFace ff:faces) ff.drawNormal(w);
    return this;
  }

  /**
   * Flips all face normals by calling {@link UFace#reverse()} 
   * @return
   */
  public UGeo reverseNormals() {
    log("reverseNormals "+getF(0).normal().str());
    for(UFace ff:faces) ff.reverse();
    log("reverseNormals "+getF(0).normal().str());
    return this;
  }
  
  public UGeo draw(int theOptions) {
    if(checkGraphicsSet()) {
      g.beginShape(TRIANGLES);
      
      
      int opt=(isEnabled(theOptions,COLORFACE) ? COLORFACE : 0);
      for(UFace f:faces) {
        if(opt==COLORFACE) g.fill(f.col);
        pvertex(f.getV());
      }
      g.endShape();
    }
    return this;
  }

  ///////////////////////////////////////////////////
  // BEGINSHAPE / ENDSHAPE METHODS
  
  
  /**
   * Starts building a new series of faces, using the same logic 
   * as <a href="http://processing.org/reference/beginShape_.html">PApplet.beginShape()</a>.
   * Currently supports the following types: TRIANGLE_FAN, TRIANGLE_STRIP, TRIANGLES, QUADS, QUAD_STRIP
   * 
   * While shape is being built vertices are stored in a temporary 
   * array, and only the ones that are used are copied to the vert vertexlist.
   * @param _type Shape type (TRIANGLE_FAN, TRIANGLE_STRIP, TRIANGLES, QUADS, QUAD_STRIP)
   */
  public UGeo beginShape(int _type) {
    
    if(vltmp==null) vltmp=new UVertexList();
    else vltmp.clear();
    vl.bb=null;
    
//    shapeRecord=new UStrip(_type);

    shapeType=_type;
    
    return this;
  }

  public UGeo endShape() {
    groupBegin(shapeType);

    
    int[] vID=vl.addID(vltmp);
//    log(str(vID));
    
    switch (shapeType) {
      case TRIANGLE_FAN: {
        UVertex cp=vltmp.first();
        int n=(vltmp.size()-1)-1;
        int id=1;
        
        
        for(int i=0; i<n; i++) {
          addFace(new int[] {vID[0],vID[id++],vID[id]});

//          addFace(cp,vltmp.get(id++),vltmp.get(id));
        }
      }
      break;

      case TRIANGLES: {
        int n=(vltmp.size())/3;
        int id=0;
        
        for(int i=0; i<n; i++) {
          addFace(vltmp.get(id++),vltmp.get(id++),vltmp.get(id++));
        }
      }
      break;

      case TRIANGLE_STRIP: {
        log("UGeo: TRIANGLE_STRIP currently unsupported.");

//        int stop = bvCnt - 2;
//        for (int i = 0; i < stop; i++) {
//          // HANDED-NESS ISSUE
////          if(i%2==1) addFace(bv[i], bv[i+2], bv[i+1]);
////          else addFace(bv[i], bv[i+1], bv[i+2]);
//          if(i%2==1) addFace(new UVertex[] {bv[i], bv[i+2], bv[i+1]});
//          else addFace(new UVertex[] {bv[i], bv[i+1], bv[i+2]});
//        }
      }
      break;

      // Processing order: bottom left,bottom right,top right,top left
//      addTriangle(i, i+1, i+2);
//      addTriangle(i, i+2, i+3);
      case QUADS: {
        int n=(vltmp.size())/4;
        int id=0;
        UVertex v0,v2;
        
        for(int i=0; i<n; i++) {
          v0=vltmp.get(id);
          v2=vltmp.get(id+2);          
          addFace(v0,vltmp.get(id+1),v2);
          addFace(v0,v2,vltmp.get(id+3));
          id+=4;
        }
      }
      break;

      /* From PGraphics3D.java, 1.5.1
       for (int i = shapeFirst; i < stop; i += 2) {
        // first triangle
        addTriangle(i+0, i+2, i+1);
        // second triangle
        addTriangle(i+2, i+3, i+1);
      }

       */
      case QUAD_STRIP: {
        
        int n=vltmp.size()/2;
        int id=0;
        UVertex v0=null,v1=null,v2=null,v3=null;
        
/*        for(int i=0; i<n; i++) {
          v2=vltmp.get(id);          
          v3=vltmp.get(id+1);         
          if(i>0) {
//            addFace(new int[] {vID[id],vID[id+2],vID[id+1]});
//            addFace(new int[] {vID[id+3],vID[id+1],vID[id+2]});
            addFace(v0,v2,v1);
            addFace(v3,v1,v2);
          }
          v0=v2;
          v1=v3;
          id+=2;
        }
*/
        int vid0= 0,vid1= 0,vid2,vid3;
        
        for(int i=1; i<n; i++) {
          addFace(new int[] {vID[id],vID[id+2],vID[id+1]});
          addFace(new int[] {vID[id+3],vID[id+1],vID[id+2]});
          id+=2;
        }

        
//        for(int i=0; i<n; i++) {
//          vid2=vID[id];
//          vid3=vID[id+1];
//          if(i>0) {
//            addFace(new int[] {vid0,vid2,vid1});
//            addFace(new int[] {vid2,vid3,vid1});
//          }
//          
//          vid0=vid2;
//          vid1=vid3;
////          addFace(new int[] {vID[id],vID[id+2],vID[id+1]});
////          addFace(new int[] {vID[id+3],vID[id+1],vID[id+2]});
////          addFace(new int[] {vID[id+2],vID[id+1],vID[id]});
////          addFace(new int[] {vID[id+2],vID[id+3],vID[id+1]});
//          id+=2;
//        }

//        for(int i=0; i<n; i++) {
//          v2=vltmp.get(id);          
//          v3=vltmp.get(id+1);         
//          if(i>0) {
//            addFace(v0,v2,v1);
//            addFace(v3,v1,v2);
//          }
//          v0=v2;
//          v1=v3;
//          id+=2;
//        }
      }
      break;

      case POLYGON:{
        log("UGeo: POLYGON currently unsupported.");
      }
      break;
    }
    
    vltmp.clear();
    
    groupEnd();
    
//    UUtil.log("Faces: "+faceNum);
    return this;

  }
  
  public boolean contains(UFace ff) {
    return faces.indexOf(ff)>-1;
  }

  public int[] addID(UVertexList v1) {
    return vl.addID(v1);
  }

  public UGeo add(UVertexList v1) {
    vl.add(v1);    
    return this;
  }

  public UGeo add(UVertex v1) {
    vl.add(v1);    
    return this;
  }
  
  public UGeo addFace(UVertex vv[]) {
    return addFace(vv[0], vv[1], vv[2]);
  }

  public UGeo addFace(ArrayList<UFace> f) {
    for(UFace ff:f) addFace(ff);
    return this;
  }

  public UGeo addFace(UFace f) {
    faces.add(f);
    return this;
  }

  public UGeo addFace(int vID[]) {
    faces.add(new UFace(this,vID));
//    addFace(vl.get(vID[0]),vl.get(vID[1]),vl.get(vID[2]));
    return this;
  }
  
  public UGeo addFace(UVertex v1, UVertex v2, UVertex v3) {
    if(!UFace.check(v1,v2,v3)) {
      log("Invalid face");
      return this;
    }
    
    UFace ff=new UFace(this, v1, v2, v3);
    if(duplicateF(ff)) {
      log("Duplicate face");
      return this;
    }
    
//    faces.add(new UFace(this, v1,v2,v3));
    faces.add(ff);
    vl.bb=null;
    return this;
  }

  public boolean duplicateF(UFace ff) {
    int cnt=0;
    
    for(UFace theFace:faces) if(theFace.equals(ff)) return true;
    
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Add vertex to shape being built by <code>beginShape() / endShape()</code>
   * @param x
   * @param y
   * @param z
   * @return 
   */
  public UGeo vertex(float x,float y,float z) {
    vertex(new UVertex(x,y,z));
    return this;
  }

  /**
   * Add UVertex vertex to shape being built by <code>beginShape() / endShape()</code>
   * The vertex information is copied, leaving the original UVertex instance unchanged.
   * @param v
   * @return 
   */
  public UGeo vertex(UVertex v) {
    vltmp.add(v);
    return this;
  }

  /**
   * Add vertex list to shape being built by <code>beginShape() / endShape()</code>. 
   * All vertices are copied, leaving the original instances unchanged.
   * @param vvl Vertex list
   * @param reverseOrder Add in reverse order?
   * @return UGeo
   */
  public UGeo vertex(UVertexList vvl,boolean reverseOrder) {
    if(reverseOrder) {
      for(int i=vvl.size()-1; i>-1; i--) vertex(vvl.get(i));
    }
    else {
      for(UVertex vv:vvl) vertex(vv);
    }
    
    return this;
  }

  /**
   * Adds vertex list to shape being built by <code>beginShape() / endShape()</code>. 
   * All vertices are copied, leaving the original instances unchanged.
   * @return 
   */
  public UGeo vertex(UVertexList vvl) {
    return vertex(vvl, false);
  }

  public int[] getVID(UVertex vv[]) {    
    return getVID(vv,null);
  }

  public int[] getVID(UVertex vv[],int vid[]) {    
    return vl.getVID(vv,vid);
  }

  /**
   * Get the list index of the provided vertex using 
   * {@link UVertexList.getVID()}. Returns -1 if vertex is not
   * found in list.
   * @param vv
   * @return
   */
  public int getVID(UVertex vv) {    
    return vl.getVID(vv);
  }

  /**
   * Get the list indices of an array of provided vertices using 
   * {@link UVertexList.getVID()}. Index values may include -1 for
   *  vertices that are not found.
   * @param vv
   * @return
   */
  public UVertex[] getVByID(int vID[]) {
    return vl.get(vID);
  }
  
  public UVertex[] getVByID(int vID[],UVertex tmp[]) {
    return vl.get(vID,tmp);
  }

  public UVertex getVertex(int vID) {    
    return vl.get(vID);
  }

  public int addVertex(UVertex v1) {    
    int id=vl.indexOf(v1);
    if(id<0) id=vl.addID(v1);
    return id;
  }

  public UGeo quadstrip(ArrayList<UVertexList> vl2) {
    UVertexList last=null;  
    
    long tD,t=System.currentTimeMillis();
    long start=t;
    int cnt=0;

    logDivider("quadstrip(ArrayList<UVertexList>\t");

    ArrayList<int[]> vID=new ArrayList<int[]>();
    taskTimerStart("quadstrip(ArrayList<UVertexList>");
    for(UVertexList vvl:vl2) {
      vID.add(addID(vvl));
      taskTimerUpdate(map(vID.size(),0,vl2.size()-1,0,50));

    }
    
    int n=vID.get(0).length;
    int qID[]=new int[n*2];
    
    
    String s="";
//    for(int i=0; i<vID.size(); i++) s+=(i>0 ? "\t":"") +
//        vID.get(i).length+"|"+vl2.get(i).size();
//    
    
    cnt=0;
    for(UVertexList vvl:vl2) {
      if(last!=null) {
        int id=0;
        int id1[]=vID.get(cnt-1);
        int id2[]=vID.get(cnt);
        
        for(int i=0; i<n; i++) {
          qID[id++]=id1[i];
          qID[id++]=id2[i];
        }
        
        quadstrip(last,vvl,qID);
      }
      last=vvl;
      cnt++;
      
      taskTimerUpdate(map(cnt,0,vl2.size()-1,50,100));
    }
    
    taskTimerDone();
    
    s="";
    for(int i=0; i<vID.size(); i++) s+=(i>0 ? "\t":"") +
        vID.get(i).length+"|"+vl2.get(i).size();
    logDivider("done - quadstrip(ArrayList<UVertexList>\t"+n+" "+qID.length+" | "+s);
    logDivider();

    return this;
  }

  /**
   * Add faces representing a triangulation of the provided
   * vertex list. Primarily useful for meshing irregular
   * polygons and point sets representing 2.5D topologies,
   * as well as filling holes in meshes (for instance "capping" 
   * cylindrical structures.) 
   * 
   * The triangulation logic acts in a 2D plane, hence point clouds 
   * representing true 3D volumes will give poor results, requiring
   * convex hull or other re-meshing strategies.
   *  
   *  See {@link UTriangulate} for details. 
   * @param vl
   * @return
   */
  public UGeo triangulation(UVertexList vl) {
    return triangulation(vl,false);
  }

  public UGeo triangulation(UVertexList vl,boolean reverse) {
    int oldSize=sizeF();
    new UTriangulate(this, vl);

    if(reverse) {
      // reverse all new faces
      for(int i=oldSize; i<sizeF(); i++) faces.get(i).reverse();      
    }
    
    return this;
  }

  public UGeo triangleFan(UVertex c,UVertexList vl) {
    return triangleFan(c, vl,false);
  }

  public UGeo triangleFan(UVertex c,UVertexList vl,boolean reverse) {
    beginShape(TRIANGLE_FAN);
    vertex(c);
    vertex(vl,reverse);
    endShape();
    
    return this;
  }

  /**
   * Add triangle fan using the vertices in <code>vl</code>, with the centroid of
   * <code>vl</code> as the central vertex.
   * @param vl
   * @return
   */
  public UGeo triangleFan(UVertexList vl) {
    return triangleFan(vl,false);
  }

  /**
   * Add triangle fan using the vertices in <code>vl</code>, with the centroid of
   * <code>vl</code> as the central vertex. 
   * @param vl
   * @param reverse Flag to indicate whether vertices should be added in reverse order
   * @return
   */
  public UGeo triangleFan(UVertexList vl,boolean reverse) {
    UVertex c=vl.centroid();
//    c=UVertex.centroid(vl.toArray());
    return triangleFan(vl,c,reverse);
  }

  /**
   * Add triangle fan using the vertices in <code>vl</code>, using <code>c</code> 
   * as the central vertex.
   * @param vl
   * @param c Central vertex of the fan.
   * @return
   */
  public UGeo triangleFan(UVertexList vl,UVertex c) {
    return triangleFan(vl,c,false);
  }

  /**
   * Add triangle fan using the vertices in <code>vl</code>, using <code>c</code> 
   * as the central vertex.
   * @param vl
   * @param c Central vertex of the fan.
   * @param reverse Flag to indicate whether vertices should be added in reverse order
   * @return
   */
  public UGeo triangleFan(UVertexList vl,UVertex c,boolean reverse) {
    
    beginShape(TRIANGLE_FAN);
    vertex(c);
    vertex(vl,reverse);
    endShape();
    
    return this;

  }

  protected UGeo quadstrip(UVertexList vl, UVertexList vl2,int vID[]) {
    groupBegin(QUAD_STRIP);
    int n=vID.length/2;
    int id=0;
    UVertex v0=null,v1=null,v2=null,v3=null;

    for(int i=1; i<n; i++) {
      addFace(new int[] {vID[id],vID[id+2],vID[id+1]});
      addFace(new int[] {vID[id+3],vID[id+1],vID[id+2]});
      id+=2;
    }
    groupEnd();    
    
    return this;
  }

  
  public UGeo quadstrip(UVertexList vl, UVertexList vl2) {
    beginShape(QUAD_STRIP);
    
    int id=0;
    for(int i=0; i<vl.size(); i++) {
      vertex(vl.get(i));
      vertex(vl2.get(i));
//      vertex(vl2.get(id++));
    }
    endShape();

//    ArrayList<UVertexList> stack=new ArrayList<UVertexList>();
//    stack.add(vl);
//    stack.add(vl2);
//    return quadstrip(stack);
    return this;
  }
  
  
  public boolean writeSTL(String filename) {
    return UGeoIO.writeSTL(filename, this);
  }

  
  public static boolean writeSTL(String filename,ArrayList<UGeo> models) {
    return UGeoIO.writeSTL(filename, models);
  }

  public String str() {return str(false);}

  public String str(boolean complete) {
    StringBuffer buf=strBufGet();
    
    buf.append(UGEO).append(TAB).append("f="+sizeF());
    buf.append(TAB).append("v="+sizeV());      

    if(complete) {
      buf.append(NEWLN).append(vl.str());
      
      buf.append(NEWLN).append("Face ID\t");
      int cnt=0;
      for(UFace ff:faces) {
        if(cnt++>0) buf.append(TAB);
        buf.append(ff.vID[0]).append(TAB);
        buf.append(ff.vID[1]).append(TAB);
        buf.append(ff.vID[2]);
      }
    }
      
    
    return "["+strBufDispose(buf)+"]";
  }


  public ArrayList<String> strGroup() {
    ArrayList<String> s=new ArrayList<String>();
    for(int i=0; i<sizeGroup(); i++) {
      s.add(strGroup(i));
    }
    return s;
  }

  public String strGroup(int id) {
    return getGroup(id).str();
  }

  
  ////////////////////////////////
  // GEOMETRY PRIMITIVES
  
  
  public static UGeo box(float w,float h,float d) {
    return UGeoGenerator.box(w,h,d);
  } 
  
  public static UGeo box(float w) {
    return UGeoGenerator.box(w);
  }    
  
  public static UGeo cyl(float w,float h,int steps) {
    return UGeoGenerator.cyl(w, h, steps);
  }
}
