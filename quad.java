import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class quad {

	static int threshold = 45;
	static boolean circle;
	
	static double entropy(BufferedImage img, int x, int y, int sz) {

		double s = 0;
		Color avg = avg(img, x, y, sz);
		for (int i = x; i < x + sz; i++) {
			for (int j = y; j < y + sz; j++) {
				Color c = new Color(img.getRGB(i, j));
				int red = c.getRed();
				int green = c.getGreen();
				int blue = c.getBlue();
				s += Math.abs(avg.getRed() - red);
				s += Math.abs(avg.getGreen() - green);
				s += Math.abs(avg.getBlue() - blue);
			}
		}
		return (double) s / (sz * sz);
	}

	static void fill(BufferedImage img, int x, int y, int sz, BufferedImage out) {
		Color avg = avg(img, x, y, sz);

		Graphics g = out.getGraphics();
		g.setColor(avg);
		if(circle){
			g.fillOval(x,y, sz, sz);
		}else{
			g.fillRect(x, y, sz, sz);
		}
		g.dispose();
	}

	static void drawRectangle(BufferedImage img, int x, int y, int sz, BufferedImage out) {
		Graphics g = out.getGraphics();
		g.setColor(Color.BLACK);
		g.drawRect(x, y, sz, sz);
		g.dispose(); 
	}

	static Color avg(BufferedImage img, int x, int y, int sz) {
		int sumRed = 0;
		int sumGreen = 0;
		int sumBlue = 0;
		int count = 0;
		for (int i = x; i < x + sz; i++) {
			for (int j = y; j < y + sz; j++) {
				int red = new Color(img.getRGB(i, j)).getRed();
				int green = new Color(img.getRGB(i, j)).getGreen();
				int blue = new Color(img.getRGB(i, j)).getBlue();
				sumRed += red;
				sumGreen += green;
				sumBlue += blue;
				count++;
			}
		}
		return new Color(sumRed / count, sumGreen / count, sumBlue / count);
	}

	static void rec(BufferedImage img, int x, int y, int sz, BufferedImage out) {
		if (sz <= 4) {
			fill(img, x, y, sz, out);
			if(!circle){
				drawRectangle(img, x, y, sz, out);
			}
			return;
		}

		if (entropy(img, x, y, sz) > threshold) {
			rec(img, x + sz / 2, y, sz / 2, out);
			rec(img, x + sz / 2, y + sz / 2, sz / 2, out);
			rec(img, x, y, sz / 2, out);
			rec(img, x, y + sz / 2, sz / 2, out);
		} else {
			fill(img, x, y, sz, out);
			if(!circle){
				drawRectangle(img, x, y, sz, out);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		circle = false;
		String pathToImage = args[0];
		if (args.length >= 2) {
			threshold = Integer.parseInt(args[1]);
		}

		if(args.length >= 3){
			if(args[2].equals("c")){
				circle = true;
			}
		}

		BufferedImage image = ImageIO.read(new File(pathToImage));
		BufferedImage out = ImageIO.read(new File(pathToImage));
		Graphics g = out.getGraphics();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, out.getWidth(), out.getHeight());
		g.dispose();

		int w = image.getWidth();
		System.out.println(w +" " + image.getHeight());
		rec(image, 0, 0, w, out);

		ImageIO.write(out, "png", new File("output.png"));
	}

}