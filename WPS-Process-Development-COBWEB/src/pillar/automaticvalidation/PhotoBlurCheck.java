package pillar.automaticvalidation;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;



public class PhotoBlurCheck extends AbstractAlgorithm{
	static Logger LOG = Logger.getLogger(PhotoBlurCheck.class);
	
	
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData)
			throws ExceptionReport {

		List<IData> baseList = inputData.get("baseURL");
		
		String url = ((LiteralStringBinding) baseList.get(0)).getPayload();
		
		LOG.warn("URL = " + url);
		URL urlS;
		
		
        BufferedImage original = null;
		
		try {
			urlS = new URL(url);
			original = ImageIO.read(urlS);
			LOG.warn("image size = " + original.getWidth() + " " + original.getHeight());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		GenericFileData lapRtn = null;
		GenericFileData histRtn = null;
		
		File tempLap;
		try {
			tempLap = File.createTempFile("tempLap", "jpeg");
			File tempHist = File.createTempFile("tempHist", "jpeg");
			File tempGrey = File.createTempFile("tempGrey", "jpeg");
			RenderedImage riL = getLaplaceImage(original);
			RenderedImage hiL = histogramEqualization(original);
			RenderedImage giL = convertImageToGrey(original);
			ImageIO.write(riL, "jpeg", tempLap );
			ImageIO.write(hiL, "jpeg", tempHist);
			ImageIO.write(giL, "jpeg", tempGrey);
			
			lapRtn = new GenericFileData(tempLap, "image/jpeg" );
			histRtn = new GenericFileData(tempHist, "image/jpeg" );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		HashMap<String,IData> result = new HashMap<String, IData>();
	
		
		result.put("HistogramImage", new GenericFileDataBinding(histRtn));
		result.put("LaplaceImage", new GenericFileDataBinding(lapRtn));
		
		
		
		
		
		
		return result;
	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		
		
		if (id.equalsIgnoreCase("baseURL")){
			return LiteralStringBinding.class;
		}

		
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		// TODO Auto-generated method stub
		if(id.equalsIgnoreCase("HistogramImage")){
			return GTRasterDataBinding.class;
		}
		if(id.equalsIgnoreCase("LaplaceImage")){
		return GTRasterDataBinding.class;
		}
		return null;
		
	}
	
	private BufferedImage getLaplaceImage(BufferedImage original){
		
		
		
		
		BufferedImage pic1 = histogramEqualization(original);
		
		BufferedImage pic2 = new BufferedImage(pic1.getWidth(), pic1.getHeight(), BufferedImage.TYPE_INT_RGB);
		int height = pic1.getHeight();
		int width = pic1.getWidth();
		for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                Color c00 = new Color(pic1.getRGB(x-1, y-1));
                Color c01 = new Color(pic1.getRGB(x-1, y  ));
                Color c02 = new Color(pic1.getRGB(x-1, y+1));
                Color c10 = new Color(pic1.getRGB(x  , y-1));
                Color c11 = new Color(pic1.getRGB(x  , y  ));
                Color c12 = new Color(pic1.getRGB(x  , y+1));
                Color c20 = new Color(pic1.getRGB(x+1, y-1));
                Color c21 = new Color(pic1.getRGB(x+1, y  ));
                Color c22 = new Color(pic1.getRGB(x+1, y+1));
                int r = -c00.getRed() -   c01.getRed() - c02.getRed() +
                        -c10.getRed() + 8*c11.getRed() - c12.getRed() +
                        -c20.getRed() -   c21.getRed() - c22.getRed();
                int g = -c00.getGreen() -   c01.getGreen() - c02.getGreen() +
                        -c10.getGreen() + 8*c11.getGreen() - c12.getGreen() +
                        -c20.getGreen() -   c21.getGreen() - c22.getGreen();
                int b = -c00.getBlue() -   c01.getBlue() - c02.getBlue() +
                        -c10.getBlue() + 8*c11.getBlue() - c12.getBlue() +
                        -c20.getBlue() -   c21.getBlue() - c22.getBlue();
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));
                Color c = new Color(r, g, b);
                
                pic2.setRGB(x, y, c.getRGB());
                
                
                
            }
		
		
	}
		LOG.warn("pic2 size " + pic2.getWidth() + " " + pic2.getHeight());
		try {
			File file = new File ("/Users/lgzsam/Downloads/test.jpg");
      	  
      	  ImageIO.write(pic2, "jpeg", file);
		} catch (IOException e) {
			LOG.error("IOException " + e);
			e.printStackTrace();
		}
		
		
		return pic2;
}
	
	private static BufferedImage histogramEqualization(BufferedImage original) {
		 
        int red;
        int green;
        int blue;
        int alpha;
        int newPixel = 0;
 
        // Get the Lookup table for histogram equalization
        ArrayList<int[]> histLUT = histogramEqualizationLUT(original);
 
        BufferedImage histogramEQ = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
 
        for(int i=0; i<original.getWidth(); i++) {
            for(int j=0; j<original.getHeight(); j++) {
 
                // Get pixels by R, G, B
                alpha = new Color(original.getRGB (i, j)).getAlpha();
                red = new Color(original.getRGB (i, j)).getRed();
                green = new Color(original.getRGB (i, j)).getGreen();
                blue = new Color(original.getRGB (i, j)).getBlue();
 
                // Set new pixel values using the histogram lookup table
                red = histLUT.get(0)[red];
                green = histLUT.get(1)[green];
                blue = histLUT.get(2)[blue];
 
                // Return back to original format
                newPixel = colorToRGB(alpha, red, green, blue);
 
                // Write pixels into image
                histogramEQ.setRGB(i, j, newPixel);
 
            }
        }
        
        LOG.warn("Histogram Image " + histogramEQ.getWidth() + " " + histogramEQ.getHeight());
       
		try {
			File file = new File ("/Users/lgzsam/Downloads/test1.jpg");
      	  
      	  ImageIO.write(histogramEQ, "jpeg", file);
		} catch (IOException e) {
			LOG.error("IOException " + e);
			e.printStackTrace();
		}
		
 
        return histogramEQ;
 
    }
 
    // Get the histogram equalization lookup table for separate R, G, B channels
    private static ArrayList<int[]> histogramEqualizationLUT(BufferedImage input) {
 
        // Get an image histogram - calculated values by R, G, B channels
        ArrayList<int[]> imageHist = imageHistogram(input);
 
        // Create the lookup table
        ArrayList<int[]> imageLUT = new ArrayList<int[]>();
 
        // Fill the lookup table
        int[] rhistogram = new int[256];
        int[] ghistogram = new int[256];
        int[] bhistogram = new int[256];
 
        for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
        for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
        for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;
 
        long sumr = 0;
        long sumg = 0;
        long sumb = 0;
 
        // Calculate the scale factor
        float scale_factor = (float) (255.0 / (input.getWidth() * input.getHeight()));
 
        for(int i=0; i<rhistogram.length; i++) {
            sumr += imageHist.get(0)[i];
            int valr = (int) (sumr * scale_factor);
            if(valr > 255) {
                rhistogram[i] = 255;
            }
            else rhistogram[i] = valr;
 
            sumg += imageHist.get(1)[i];
            int valg = (int) (sumg * scale_factor);
            if(valg > 255) {
                ghistogram[i] = 255;
            }
            else ghistogram[i] = valg;
 
            sumb += imageHist.get(2)[i];
            int valb = (int) (sumb * scale_factor);
            if(valb > 255) {
                bhistogram[i] = 255;
            }
            else bhistogram[i] = valb;
        }
 
        imageLUT.add(rhistogram);
        imageLUT.add(ghistogram);
        imageLUT.add(bhistogram);
 
        return imageLUT;
 
    }
 
    // Return an ArrayList containing histogram values for separate R, G, B channels
    public static ArrayList<int[]> imageHistogram(BufferedImage input) {
 
        int[] rhistogram = new int[256];
        int[] ghistogram = new int[256];
        int[] bhistogram = new int[256];
 
        for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
        for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
        for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;
 
        for(int i=0; i<input.getWidth(); i++) {
            for(int j=0; j<input.getHeight(); j++) {
 
                int red = new Color(input.getRGB (i, j)).getRed();
                int green = new Color(input.getRGB (i, j)).getGreen();
                int blue = new Color(input.getRGB (i, j)).getBlue();
 
                // Increase the values of colors
                rhistogram[red]++; ghistogram[green]++; bhistogram[blue]++;
 
            }
        }
 
        ArrayList<int[]> hist = new ArrayList<int[]>();
        hist.add(rhistogram);
        hist.add(ghistogram);
        hist.add(bhistogram);
 
        return hist;
 
    }
 
    // Convert R, G, B, Alpha to standard 8 bit
    private static int colorToRGB(int alpha, int red, int green, int blue) {
 
        int newPixel = 0;
        newPixel += alpha; newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;
 
        return newPixel;
 
    }
    
    private BufferedImage convertImageToGrey(BufferedImage original){
		
    	
    	BufferedImage greyImage = new BufferedImage(original.getWidth(), 
    			original.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
    	
        int  width = original.getWidth();
        int  height = original.getHeight();
         
         for(int i=0; i<height; i++){
         
            for(int j=0; j<width; j++){
            
               Color c = new Color(original.getRGB(j, i));
               int red = (int)(c.getRed() * 0.299);
               int green = (int)(c.getGreen() * 0.587);
               int blue = (int)(c.getBlue() *0.114);
               Color newColor = new Color(red+green+blue,
               
               red+green+blue,red+green+blue);
               
               greyImage.setRGB(j,i,newColor.getRGB());
            }
         }
         
         LOG.warn("Histogram Image " + greyImage.getWidth() + " " + greyImage.getHeight());
         
 		try {
 			File file = new File ("/Users/lgzsam/Downloads/test2.jpg");
       	  
       	  ImageIO.write(greyImage, "jpeg", file);
 		} catch (IOException e) {
 			LOG.error("IOException " + e);
 			e.printStackTrace();
 		}
 		
    	
    	
    	return original;
    	
    }
 }