import net.sabamiso.processing.qx10.*;

QX10 qx10;
PImage still_image;

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
  // show still image
  if (still_image != null) {
    image(still_image, 0, 0, width, height);
  }
}

public void keyPressed() {
  switch(key) {
  case ' ':
    still_image = qx10.takePicture();
    break;
  }
}

