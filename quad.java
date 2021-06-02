import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import java.util.*;
import java.awt.geom.Point2D;

public class quad {

	static int threshold = 45;
	static boolean circle = false;
	static boolean outline = false;
	static boolean mosaic = false;
	static List<Point2D>centers = new ArrayList<>();
	static int MIN_SZ = 4;

	static double entropy(BufferedImage img, int x, int y, int szX, int szY) {
		double s = 0;
		Color avg = avg(img, x, y, szX, szY);

		for (int i = x; i < x + szX; i++) {
			for (int j = y; j < y + szY; j++) {
				Color c = new Color(img.getRGB(i, j));
				s += Math.abs(avg.getRed() - c.getRed());
				s += Math.abs(avg.getGreen() - c.getGreen());
				s += Math.abs(avg.getBlue() - c.getBlue());
			}
		}
		return (double) s / (szX * szY);
	}

	static void chooseRandomPoint(BufferedImage img, int x, int y, int szX, int szY){
		Random rng = new Random();
		int rngX = rng.nextInt(szX) + x;
		int rngY = rng.nextInt(szY) + y;
		Point2D rnd = new Point2D.Double(rngX, rngY);
		centers.add(rnd);
	}

	
	static Point2D findClosestPoint(int x, int y){
		Point2D closest = null;
		double minDist = Double.POSITIVE_INFINITY;
		Point2D p = new Point2D.Double(x, y);

		for(Point2D point: centers){
			double dist = point.distance(p);
			if(dist < minDist){
				minDist = dist;
				closest = point;
			}
		}
		return closest;
	}

	static void color(BufferedImage img, BufferedImage out) {
		for(int i = 0; i < img.getWidth(); i++){
			for(int j = 0; j < img.getHeight(); j++){
				Point2D c = findClosestPoint(i, j);
				out.setRGB(i, j, img.getRGB((int)c.getX(), (int)c.getY()));
			}	
		}
	}

	static void fill(BufferedImage img, int x, int y, int szX, int szY, BufferedImage out) {
		Color avg = avg(img, x, y, szX, szY);
		Graphics g = out.getGraphics();
		g.setColor(avg);

		if (circle) {
			g.fillOval(x, y, szX, szY);
		} else {
			g.fillRect(x, y, szX, szY);
			if (outline) {
				g.setColor(Color.BLACK);
				g.drawRect(x, y, szX, szY);
			}
		}
		g.dispose();
	}

	static Color avg(BufferedImage img, int x, int y, int szX, int szY) {
		int sumRed = 0;
		int sumGreen = 0;
		int sumBlue = 0;
		int count = 0;
		for (int i = x; i < x + szX; i++) {
			for (int j = y; j < y + szY; j++) {
				Color c = new Color(img.getRGB(i, j));
				sumRed += c.getRed();
				sumGreen += c.getGreen();
				sumBlue += c.getBlue();
				count++;
			}
		}
		return new Color(sumRed / count, sumGreen / count, sumBlue / count);
	}

	static void rec(BufferedImage img, int x, int y, int szX, int szY, BufferedImage out) {
		if (szX <= MIN_SZ || szY <= MIN_SZ) {
			if(mosaic){
				chooseRandomPoint(img, x, y, szX, szY);
			}else{
				fill(img, x, y, szX, szY, out);
			}
			return;
		}

		if (entropy(img, x, y, szX, szY) > threshold) {
			rec(img, x + szX / 2, y, szX / 2, szY / 2, out);
			rec(img, x + szX / 2, y + szY / 2, szX / 2, szY / 2, out);
			rec(img, x, y, szX / 2, szY / 2, out);
			rec(img, x, y + szY / 2, szX / 2, szY / 2, out);
		} else {
			if(mosaic){
				chooseRandomPoint(img, x, y, szX, szY);				
			}else{
				fill(img, x, y, szX, szY, out);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		String pathToImage = args[0];

		if (args.length >= 2) {
			MIN_SZ = Integer.parseInt(args[1]);
		}

		if (args.length >= 3) {
			threshold = Integer.parseInt(args[2]);
		}

		Set<String> params = new HashSet<>(Arrays.asList(args));

		if (params.contains("-m")) {
			mosaic = true;
		}
		if (params.contains("-c")) {
			circle = true;
		}

		if (params.contains("-o")) {
			outline = true;
		}

		BufferedImage image = ImageIO.read(new File(pathToImage));
		BufferedImage out = ImageIO.read(new File(pathToImage));

		Graphics g = out.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, out.getWidth(), out.getHeight());
		g.dispose();

		int w = image.getWidth();
		int h = image.getHeight();

		rec(image, 0, 0, w, h, out);
		if(mosaic){
			color(image, out);
		}

		ImageIO.write(out, "png", new File("output.png"));
	}

}
