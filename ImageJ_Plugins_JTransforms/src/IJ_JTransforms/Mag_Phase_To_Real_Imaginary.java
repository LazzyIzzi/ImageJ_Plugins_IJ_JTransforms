package IJ_JTransforms;

import java.awt.Color;
import java.awt.Font;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Mag_Phase_To_Real_Imaginary implements PlugInFilter {
	final Color myColor = new Color(240,230,190);
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);

	@Override
	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		return DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		String[] titles = WindowManager.getImageTitles();
		GenericDialog gd = new GenericDialog("Magnitude-Phase To Real-Imaginary");
		gd.addMessage("3D ops require even sized width, height and depth.\n"
				+ "Deconvolution requires square images.",myFont);
		gd.addChoice("Magnitude Image", titles, titles[0]);
		gd.addChoice("Phase Image", titles, titles[1]);
		gd.setBackground(myColor);
		gd.showDialog();

		if (gd.wasCanceled()) {
			return;
		}
		
		ImagePlus magImp = WindowManager.getImage(gd.getNextChoice());
		ImagePlus phImp = WindowManager.getImage(gd.getNextChoice());
		
		IJJT_fft ijtft = new IJJT_fft();
		
		Object[] re =  magImp.getStack().getImageArray();
		Object[] im =  phImp.getStack().getImageArray();
		ijtft.magPhToReIm(re, im, magImp.getNSlices());		
		magImp.updateAndDraw();
		phImp.updateAndDraw();
		
		magImp.setTitle("RealFFT");
		phImp.setTitle("ImaginaryFFT");
		IJ.run(magImp, "Enhance Contrast", "saturated=0.35");
		IJ.run(phImp, "Enhance Contrast", "saturated=0.35");

	}

}
