import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class quad {

	static int threshold = 45;

	public static void makeGray(BufferedImage img) {
		for (int x = 0; x < img.getWidth(); ++x)
			for (int y = 0; y < img.getHeight(); ++y) {
				int rgb = img.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb & 0xFF);

				// Normalize and gamma correct:
				double rr = Math.pow(r / 255.0, 2.2);
				double gg = Math.pow(g / 255.0, 2.2);
				double bb = Math.pow(b / 255.0, 2.2);

				// Calculate luminance:
				double lum = 0.2126 * rr + 0.7152 * gg + 0.0722 * bb;

				// Gamma compand and rescale to byte range:
				int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / 2.2));
				int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
				img.setRGB(x, y, gray);
			}
	}

	/*
	 * static double entropy(BufferedImage img, int x, int y, int sz) { int[]
	 * pdf = new int[256]; double s = 0; int avg = avg(img, x, y, sz); for (int
	 * i = x; i < x + sz; i++) { for (int j = y; j < y + sz; j++) { int red =
	 * new Color(img.getRGB(i, j)).getRed(); pdf[red]++; } } double entropy = 0;
	 * int sum = 0; // int count = 0; for(int a : pdf){sum+=a;}
	 * 
	 * for (int i = 0; i < pdf.length; i++) { if (pdf[i] != 0) { double p =
	 * (double) pdf[i] / sum; entropy -= p * Math.log(p); } }
	 * System.out.println(entropy); return entropy; }
	 */
	static double entropy1(BufferedImage img, int x, int y, int sz) {

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
		for (int i = x; i < x + sz; i++) {
			for (int j = y; j < y + sz; j++) {
				out.setRGB(i, j, avg.getRGB());
			}
		}
	}

	static void drawRectangle(BufferedImage img, int x, int y, int sz, BufferedImage out) {
		for (int i = 0; i < sz; i++) {
			out.setRGB(x, y + i, new Color(0, 0, 0).getRGB());
			out.setRGB(x + i, y, new Color(0, 0, 0).getRGB());
			out.setRGB(x + sz - 1, y + i, new Color(0, 0, 0).getRGB());
			out.setRGB(x + i, y + sz - 1, new Color(0, 0, 0).getRGB());
		}
//		Graphics g = out.getGraphics();
//		g.setColor(Color.RED);
//		g.fillOval(x, y, sz, sz);

//		g.dispose(); // get rid of the Graphics context to save resources

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
			drawRectangle(img, x, y, sz, out);
			return;
		}

		if (entropy1(img, x, y, sz) > 45) {
			rec(img, x + sz / 2, y, sz / 2, out);
			rec(img, x + sz / 2, y + sz / 2, sz / 2, out);
			rec(img, x, y, sz / 2, out);
			rec(img, x, y + sz / 2, sz / 2, out);
		} else {
			fill(img, x, y, sz, out);
			drawRectangle(img, x, y, sz, out);
		}
	}

	public static void main(String[] args) throws IOException {

		String pathToImage = args[0];
		if (args.length >= 2) {
			threshold = Integer.parseInt(args[1]);
		}

		BufferedImage image = ImageIO.read(new File(pathToImage));
		BufferedImage out = ImageIO.read(new File(pathToImage));
		Graphics g = out.getGraphics();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, out.getWidth(), out.getHeight());
		g.dispose();

		int w = image.getWidth();

		rec(image, 0, 0, w, out);

		ImageIO.write(out, "png", new File("output.png"));
	}

}