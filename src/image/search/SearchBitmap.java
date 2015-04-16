package image.search;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.sikuli.api.DesktopScreenRegion;
import org.sikuli.api.ImageTarget;
import org.sikuli.api.MultiStateTarget;
import org.sikuli.api.ScreenRegion;
import org.sikuli.api.Target;
import org.sikuli.api.robot.desktop.DesktopScreen;

public class SearchBitmap {
	public static void main(String[] args) {
		
//		String[] args_cust = new String[]{"D:\\JavaWorkspace\\nuance\\searchbitmap\\test\\config.ini"};
//		args = args_cust;
//		System.out.println("Java "+System.getProperty("sun.arch.data.model")); 
		
		if (args.length!=1){
			System.out.println("Wrong number of input arguments");
			System.out.println("Cmd start example:\n>>java -jar app_name.jar config.ini");
			System.exit(1);
		}
		
		Properties prop = new Properties();
		
		try {
			String fileContent = FileUtils.readFileToString(new File(args[0]));

			// To avoid Java backslash problem
			prop.load(new StringReader(fileContent.replace("\\", "\\\\")));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String imageFilePath 	= prop.getProperty("imageFilePath");
		String[] imagesToFind = imageFilePath.split(";");
		String screen_area 		= prop.getProperty("screen_area");
		int timeToWaitForImage 	= Integer.parseInt(prop.getProperty("timeToWait"));
		int screen_id 			= Integer.parseInt(prop.getProperty("screen_id"));
		double min_score		= Double.parseDouble(prop.getProperty("min_score"));
		String lastImagePath	= prop.getProperty("last_image_path");
		
		ScreenRegion sr = new DesktopScreenRegion();
		
		DesktopScreen screend = new DesktopScreen(screen_id);
		sr.setScreen(screend);
		
		boolean bSearchByRectangle = false;
		Rectangle customZoneRect = new Rectangle();
		if (screen_area.contains(";")){
			String[] coords = screen_area.split(";");
			if (coords.length==4){
				int x1 = Integer.parseInt(coords[0]);
				int y1 = Integer.parseInt(coords[1]);
				int x2 = Integer.parseInt(coords[2]);
				int y2 = Integer.parseInt(coords[3]);
				
				customZoneRect.setBounds(x1, y1, x2, y2);
				sr.setBounds(customZoneRect);
				bSearchByRectangle = true;
			}
			else{
				System.out.println("Wrong rectangle format for parameter 'screen_area': " + screen_area);
				System.out.println("Example parameter format: screen_area=100;100;150;200");
				System.exit(2);
			}
		}
		
//		Target multiTarget = new ImageTarget(new File(imagesToFind[0]));
		
		MultiStateTarget multiTarget = new MultiStateTarget();
		
		for (String str:imagesToFind){
			Target target = new ImageTarget(new File(str));
			target.setMinScore(min_score);
			multiTarget.addState(target, str);
		}

		long iStart = System.currentTimeMillis();
		
		ScreenRegion foundRegion = sr.wait(multiTarget, timeToWaitForImage*1000);
		
		long searchTime =(System.currentTimeMillis()-iStart); 
		System.out.println(searchTime);
		
		iStart = System.currentTimeMillis();
		
		foundRegion = sr.wait(multiTarget, timeToWaitForImage*1000);
		
		searchTime =(System.currentTimeMillis()-iStart); 
		System.out.println(searchTime);
		
		if (lastImagePath!=""){
			System.out.println("Latest screenshot: "
					+ saveImage(sr.getLastCapturedImage(), lastImagePath));
		}
		
		if (foundRegion==null){
			System.out.println("Image not found in " + searchTime + ": "
					+ imageFilePath);
			System.exit(3);
		}
		else{
			System.out.println("Image found in: " + searchTime);
		}

		int l, t, r, b;
		Rectangle foundRect = foundRegion.getBounds();
		
		l = foundRect.x;
		t = foundRect.y;
		r = (int) (l + foundRect.getWidth());
		b = (int) (t + foundRect.getHeight());
		
		int lOut, tOut, rOut, bOut;
		
		if (bSearchByRectangle) {
			lOut = (int) (l - customZoneRect.getX());
			tOut = (int) (t - customZoneRect.getY());
			rOut = (int) (lOut + foundRect.getWidth());
			bOut = (int) (tOut + foundRect.getHeight());

		} else {
			lOut = l;
			tOut = t;
			rOut = r;
			bOut = b;
		}
		
//		String foundImageName = (String) foundRegion.getState();
		

		StringBuilder stringToPrint = new StringBuilder();
		stringToPrint.append((String)foundRegion.getState());
		stringToPrint.append("|");
		stringToPrint.append(lOut);
		stringToPrint.append(";");
		stringToPrint.append(tOut);
		stringToPrint.append(";");
		stringToPrint.append(rOut);
		stringToPrint.append(";");
		stringToPrint.append(bOut);
		stringToPrint.append("|");
		stringToPrint.append("Score:"+foundRegion.getScore());
		System.out.println(stringToPrint.toString());
		System.out.println(System.currentTimeMillis());
	}

	static String saveImage(BufferedImage image, String fileName) {
		File newImageFile = new File(fileName);

		try {
			ImageIO.write(image, "png", newImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newImageFile.getAbsolutePath();
	}
}
