p5_qx10
========
Sony DSC-QX10 remote control library for Processsing. 

how to use
========

liveview
--------
<pre>
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
  PImage image = qx10.getLiveviewImage();
  if (image != null) {
    image(image, 0, 0, width, height);
  }
}
</pre>

take picture
--------
<pre>
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
</pre>


zoom
--------
<pre>
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
  // show liveview
  PImage image = qx10.getLiveviewImage();
  if (image != null) {
    image(image, 0, 0, width, height);
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
</pre>


autofocus
--------
<pre>
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
  // show liveview
  PImage image = qx10.getLiveviewImage();
  if (image != null) {
    image(image, 0, 0, width, height);
  }
}

public void mousePressed() {
  float x = mouseX / (float)width * 100.0f;  // 0.0-100.0 ?
  float y = mouseY / (float)height * 100.0f; //

  qx10.setTouchAFPosition(x, y);
}
</pre>
