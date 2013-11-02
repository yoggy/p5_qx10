package net.sabamiso.processing.qx10;

import processing.core.PApplet;
import processing.core.PImage;

public class TestMain extends PApplet{
	private static final long serialVersionUID = -6299440719946513389L;

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
			image(image, 0, 0, width, height); // 640 x 480
		}
	}

	public void mousePressed() {
		PImage image = qx10.takePicture();
		if (image == null) {
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
		}
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "net.sabamiso.processing.qx10.TestMain" });
	}
}
