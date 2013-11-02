package net.sabamiso.processing.qx10;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

import processing.core.PApplet;
import processing.core.PImage;

public class QX10 extends Thread {
	PApplet papplet;
	
	String host = "10.0.0.1";
	int control_port = 10000;
	int liveview_port = 60152;

	PImage liveview_image;

	Socket liveview_socket;
	Thread thread;

	Socket control_socket;
	InputStream control_is;
	OutputStream control_os;

	
	public QX10(PApplet papplet) {
		this.papplet = papplet;
	}

	public QX10(PApplet papplet, String host, int control_port, int liveview_port) {
		this.papplet = papplet;
		this.host = host;
		this.control_port = control_port;
		this.liveview_port = liveview_port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getControlPort() {
		return control_port;
	}

	public int getLiveviewPort() {
		return liveview_port;
	}

	private boolean isConnect() {
		if (liveview_socket == null) return false;
		return true;
	}

	synchronized void setLiveviewImage(PImage img) {
		this.liveview_image = img;
	}

	public synchronized PImage getLiveviewImage() {
		if (isConnect() == false)
			return null;
		return liveview_image;
	}

	boolean startLiveview() {
		stopLiveview();
		try {
			liveview_socket = new Socket(host, liveview_port);
			OutputStream os = liveview_socket.getOutputStream();
			String req = "GET /liveview.JPG?%211234%21http%2dget%3a%2a%3aimage%2fjpeg%3a%2a%21%21%21%21%21 HTTP/1.1\r\nHost: 10.0.0.1:60152\r\nConnection: Keep-Alive\r\nAccept-Encoding: gzip\r\n\r\n";
			os.write(req.getBytes());

			// thread start...
			start();
		} catch (Exception e) {
			e.printStackTrace();
			stopLiveview();
			return false;
		}

		return true;
	}

	void stopLiveview() {
		if (liveview_socket != null) {
			try {
				liveview_socket.close();
			}
			catch(Exception e) {
			}
			liveview_socket = null;
		}
	}
	
	private String readUntil(InputStream is, String end_str) throws Exception {
		String recv_data;
		byte[] buf = new byte[256];
		byte[] eos = end_str.getBytes();
		int size = 0;
		while (true) {
			int c = is.read();
			if (c < 0)
				throw new Exception("read_until() failed...");

			buf[size] = (byte) c;
			size += 1;

			// check eos
			if (size > end_str.length()) {
				boolean flag = true;
				for (int i = 0; i < eos.length; ++i) {
					if (buf[size - eos.length + i] != eos[i]) {
						flag = false;
						break;
					}
				}
				if (flag == true)
					break;
			}
		}

		recv_data = new String(buf, 0, size - end_str.length()); // remove
																	// end_str
		return recv_data;
	}

	private int readChunkSize(InputStream is) throws Exception {
		String recv_data = readUntil(is, "\r\n");
		if (recv_data == null || recv_data.length() == 0)
			throw new Exception("read_chunk_size() failed...");

		return Integer.parseInt(recv_data, 16); // size string is HEX...
	}

	private byte[] readChunkData(InputStream is, int buf_size)
			throws Exception {
		byte[] buf = new byte[buf_size];

		int size = 0;
		while (true) {
			size += is.read(buf, size, buf_size - size);
			if (size == buf_size)
				break;
		}

		// read "\r\n"
		int cr = is.read();
		int lf = is.read();
		if (cr != 13 || lf != 10) {
			throw new Exception("read_chunk_data() failed...");
		}

		return buf;
	}

	private void decodeJpegData(byte[] jpeg_buf, int size) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(jpeg_buf);
		BufferedImage bufferd_img = null;
		bufferd_img = ImageIO.read(bis);
		bis.close();

		int w = bufferd_img.getWidth();
		int h = bufferd_img.getHeight();

		Raster raster = bufferd_img.getRaster();
		DataBufferByte data_buffer = (DataBufferByte) (raster.getDataBuffer());
		byte[] buf = data_buffer.getData();
		PImage pimg = new PImage(w, h, PImage.ARGB);

		for (int y = 0; y < h; ++y) {
			for (int x = 0; x < w; ++x) {
				int buf_idx = x * 3 + y * w * 3;
				byte b = buf[buf_idx + 0];
				byte g = buf[buf_idx + 1];
				byte r = buf[buf_idx + 2];

				int c = (b << 0) & 0x000000ff | (g << 8) & 0x0000ff00
						| (r << 16) & 0x00ff0000 | (0xff << 24) & 0xff000000;

				int img_idx = x + y * w;
				pimg.pixels[img_idx] = c;
			}
		}
		pimg.updatePixels();

		setLiveviewImage(pimg);
		
		bufferd_img.flush();
		bufferd_img = null;
	}

	// read thread
	public void run() {
		try {
			InputStream is = liveview_socket.getInputStream();
			int size;
			byte[] buf;

			readUntil(is, "\r\n\r\n");

			while (true) {
				size = readChunkSize(is);
				buf = readChunkData(is, size);
				if (size > 1000) {
					decodeJpegData(buf, size);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean connect() {
		if (isConnect() == true)
			return true;

		if (startLiveview() == false) {
			System.err.println("err: startLiveview() failed...");
			return false;
		}

		return true;
	}

	public void close() {
		if (isConnect() == true) {
			stopLiveview();
		}
	}
	
	
	public PImage takePicture() {
		boolean rv;
		byte [] recv_buf = new byte[1024];
		
		// request take picture
		rv = control_send("POST /sony/camera HTTP/1.1\r\nContent-Length: 63\r\n\r\n{\"method\":\"actTakePicture\",\"params\":[],\"id\":10,\"version\":\"1.0\"}");
		if (rv == false) {
			close();
			return null;
		}

		// recv response
		int recv_size = control_recv(recv_buf, 0, recv_buf.length);
		if (recv_size <= 0) {
			close();
			return null;
		}

		// parse response & download picture
		PImage image = null;
		try {
			String res = new String(recv_buf, 0, recv_size);
			String body = res.split("\r\n\r\n")[1];
			int body_length = body.length();
			
			// {"id":10,"result":[["http://192.168.122.1:8080/postview/pict20130614_175010_0.JPG"]]}
			String url = body.substring(21, body_length - 4);
			System.out.println("url=" + url);
			image = papplet.loadImage(url);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		control_close();
		return image;
	}
	
	protected boolean control_connect() {
		try {
			control_socket = new Socket(host, control_port);
			control_is = control_socket.getInputStream();
			control_os = control_socket.getOutputStream();
		}
		catch(Exception e) {
			e.printStackTrace();
			control_close();
			return false;
		}
		
		return true;
	}
	
	protected void control_close() {
		if (control_is != null) {
			try {
				control_is.close();
			} catch (IOException e) {
			}
			control_is = null;
		}
		
		if (control_os != null) {
			try {
				control_os.close();
			} catch (IOException e) {
			}
			control_os = null;
		}
		
		if (control_socket != null) {
			try {
				control_socket.close();
			} catch (IOException e) {
			}
			control_socket = null;
		}
	}
	
	protected boolean control_send(String str) {
		boolean rv;
		
		if (control_socket == null ) { 
			rv = control_connect();
			if (rv == false) {
				control_close();
				return false;
			}
		}
		
		try {
			control_os.write(str.getBytes());
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	protected int control_recv(byte [] buf, int offset, int buf_size) {
		int read_size = -1;
		try {
			read_size = control_is.read(buf, offset, buf_size);
			return read_size;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}
}
