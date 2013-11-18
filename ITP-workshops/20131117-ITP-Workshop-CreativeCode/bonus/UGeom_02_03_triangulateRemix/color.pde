// methods to 


ArrayList<Integer> colList;

void applyColor() {
  buildColor();
  
  // set random face colors
  for(int i=0; i<geo.sizeF(); i++) geo.getF(i).setColor(randomColor());
  geo.enable(UMB.COLORFACE);

  // assign colors to vertices
  for(int i=0; i<geo.sizeV(); i++) geo.getV(i).setColor(randomColor());
//  for(UVertex v : geo.getV()) v.setColor(randomColor());
}

void buildColor() {
  colList=new ArrayList<Integer>();
  
  int[] grad=new int[] {
    color(255,0,0), color(255,255,0),
    color(255), color(0,150,200),
    color(255), color(255,0,128),
    color(0,50,100), color(0,255,255)
    
  };
  
  int ng=grad.length/2;
  for(int i=0; i<ng; i++) if(random(100)>30) {
    int num=(int)random(3,6);
    if(random(100)>90) num=20;
    
    for(int j=0; j<num; j++) {
      float t=map(j, 0,num-1, 0,1);
      int theCol=lerpColor(grad[i*2],grad[i*2+1], t);
      theCol=lerpColor(grad[i*2],grad[(i*2+7)%grad.length], t);
      colList.add(theCol);
    }
  }
  
  println(colList.size());
  for(int c:colList) println(UMB.hex(c));
}

int randomColor() {
  return colList.get((int)random(colList.size()));
}
