import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;

public class quad {

	static int threshold = 45;
	static boolean circle = false;
	static boolean outline = false;

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

	static void fill(BufferedImage img, int x, int y, int szX, int szY, BufferedImage out) {
		Color avg = avg(img, x, y, szX, szY);
		Graphics g = out.getGraphics();
		g.setColor(avg);

		if(circle){
			g.fillOval(x,y, szX, szY);
		}else{
			g.fillRect(x, y, szX, szY);
			if(outline){
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

	static int[] histo = new int[1024];

	static int count = 0;
	static void rec(BufferedImage img, int x, int y, int szX, int szY, BufferedImage out) {
		if (szX <= 4 || szY <= 4) {
						histo[szX]++;
			count++;
			fill(img, x, y, szX, szY, out);
			return;
		}

		if (entropy(img, x, y, szX, szY) > threshold) {
			rec(img, x + szX / 2, y, szX / 2, szY / 2, out);
			rec(img, x + szX / 2, y + szY / 2, szX / 2, szY / 2, out);
			rec(img, x, y, szX / 2, szY / 2, out);
			rec(img, x, y + szY / 2, szX / 2, szY / 2, out);
		} else {
						histo[szX]++;
			count++;
			fill(img, x, y, szX, szY, out);
		}
	}

	public static void main(String[] args) throws IOException {
		String pathToImage = args[0];

		if (args.length >= 2) {
			threshold = Integer.parseInt(args[1]);
		}

		Set<String> params = Set.of(args);

		if(params.contains("-c")){
			circle = true;
		}

		if(params.contains("-o")){
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
		
		System.out.println(w + " " + image.getHeight());
		
		rec(image, 0, 0, w, h, out);
		// System.out.println(count);
		for(int i =0; i < histo.length; i++){
			if(histo[i] != 0){
				System.out.println(i + " : " + histo[i]);
			}
		}
		ImageIO.write(out, "png", new File("output.png"));
	}

}