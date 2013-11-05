import net.sabamiso.processing.qx10.*;

QX10 qx10;

public void setup() {
  size(640, 480);

  qx10 = new QX10(this);
  boolean rv = qx10.connect();
  if (rv == false) {
    println("error: qx10.start() failed...");
    return;
  }
}

public void draw() {
  PImage img = qx10.getLiveviewImage();
  if (img != null) {
    image(img, 0, 0, width, height); // 640 x 480
  }
}

public void keyPressed() {
  switch(key) {
  case '1':
    qx10.zoomIn1shot();
    break;
  case '2':
    qx10.zoomOut1shot();
    break;
  case '3':
    qx10.zoomInStart();
    break;
  case '4':
    qx10.zoomInStop();
    break;
  case '5':
    qx10.zoomOutStart();
    break;
  case '6':
    qx10.zoomOutStop();
    break;
  }
}

