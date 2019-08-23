import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class quad {

	public static void makeGray(BufferedImage img)
	{
	    for (int x = 0; x < img.getWidth(); ++x)
	    for (int y = 0; y < img.getHeight(); ++y)
	    {
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

	static double entropy(BufferedImage img, int x, int y, int sz){
		int[] pdf = new int[256];
		double s = 0;
		int avg = avg(img,x,y,sz);
		for (int i = x; i < x + sz; i++) {
			for (int j = y; j < y + sz; j++) {
				int red = new Color(img.getRGB(i, j)).getRed();
				pdf[red]++;
			}
		}
		double entropy = 0;
		int sum = 0;
		// int count = 0;
		for(int a : pdf){sum+=a;}

		for(int i = 0; i < pdf.length; i++){
			if(pdf[i] != 0){
				double p = (double)pdf[i]/sum;
				entropy -= p * Math.log(p);
			}
		}
		System.out.println(entropy);
		return entropy;
	}

	static double entropy1(BufferedImage img, int x, int y, int sz){
		
		double s = 0;
		int avg = avg(img,x,y,sz);
		for (int i = x; i < x + sz; i++) {
			for (int j = y; j < y + sz; j++) {
				int red = new Color(img.getRGB(i, j)).getRed();
				s+= Math.abs(avg-red);
			}
		}
		return (double)s/(sz*sz);
	}

	static void fill(BufferedImage img, int x, int y, int sz, BufferedImage out){
		int avg = avg(img, x, y, sz);
		for (int i = x; i < x + sz; i++) {
			for (int j = y; j < y + sz; j++) {
				int red = new Color(img.getRGB(i, j )).getRed();
				out.setRGB(i, j, new Color(avg, avg, avg).getRGB());
			}
		}
	}

	static void drawRectangle(BufferedImage img, int x, int y, int sz, BufferedImage out){
		for (int i = 0; i < sz; i++) {
			out.setRGB(x, y+i, new Color(0, 0, 0).getRGB());
			out.setRGB(x+i, y, new Color(0, 0, 0).getRGB());
			out.setRGB(x+sz-1, y+i, new Color(0, 0, 0).getRGB());
			out.setRGB(x+i, y+sz-1, new Color(0, 0, 0).getRGB());
		}
	}

	static int avg(BufferedImage img, int x, int y, int sz){
		int sum = 0;
		int count = 0;
		for (int i = x; i < x + sz; i++) {
			for (int j = y; j < y + sz; j++) {
				int red = new Color(img.getRGB(i, j)).getRed();
				sum += red;
				count++;
			}
		}
		return sum/count;
	}

	static void rec(BufferedImage img, int x, int y, int sz, BufferedImage out){
		if(sz <= 2){
			fill(img, x, y, sz, out);
			drawRectangle(img,x,y,sz, out);
			return;
		}
		
		if(sz > 64 || entropy1(img, x, y, sz) > 25){
			rec(img, x + sz/2, y, sz/2, out);
			rec(img, x + sz/2, y + sz/2, sz/2, out);
			rec(img, x, y, sz/2, out);
			rec(img, x, y + sz/2, sz/2, out);
		}else{
			fill(img, x, y, sz, out);
			drawRectangle(img,x,y,sz, out);	
		}
	}

	public static void main(String[] args) throws IOException {

		BufferedImage image = ImageIO.read(new File("cat.jpeg"));
		BufferedImage out = ImageIO.read(new File("cat.jpeg"));
		int w = image.getWidth();
		System.out.println(w);
		makeGray(image);
		rec(image, 0, 0, w, out);

		ImageIO.write(out, "jpeg", new File("output.jpeg"));
	}

}