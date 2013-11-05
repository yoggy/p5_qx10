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
    image(img, 0, 0, width, height);
  }
}

