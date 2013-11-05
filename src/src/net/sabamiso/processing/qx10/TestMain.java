package net.sabamiso.processing.qx10;

import processing.core.PApplet;
import processing.core.PImage;

public class TestMain extends PApplet{
	private static final long serialVersionUID = -6299440719946513389L;

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
		// show liveview
		PImage liveview_image = qx10.getLiveviewImage();
		if (liveview_image != null) {
			image(liveview_image, 0, 0, width, height); // 640 x 480
		}
		
		// show still image
		if (still_image != null) {
			image(still_image, 0, height - still_image.height/8, still_image.width/8, still_image.height/8);
		}
	}

	public void mousePressed() {
		float x = mouseX / (float)width * 100.0f;
		float y = mouseY / (float)height * 100.0f;
		
		qx10.setTouchAFPosition(x, y);
	}

	public void keyPressed() {
		switch(key) {
		case ' ':
			still_image = qx10.takePicture();
			break;
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

	public static void main(String[] args) {
		PApplet.main(new String[] { "net.sabamiso.processing.qx10.TestMain" });
	}
}
