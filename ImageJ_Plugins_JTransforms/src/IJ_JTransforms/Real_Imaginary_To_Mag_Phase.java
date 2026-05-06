package IJ_JTransforms;

import java.awt.Color;
import java.awt.Font;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Real_Imaginary_To_Mag_Phase implements PlugInFilter {
	final Color myColor = new Color(240,230,190);//slightly darker than buff
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		String[] titles = WindowManager.getImageTitles();
		GenericDialog gd = new GenericDialog("Real-Imaginary To Magnitude-Phase");
		gd.addMessage("3D ops require even sized width, height and depth.\n"
				+ "Deconvolution requires square images.", myFont);
		gd.addChoice("Real Image", titles, titles[0]);
		gd.addChoice("Imaginary Image", titles, titles[1]);
		gd.setBackground(myColor);
		gd.showDialog();

		if (gd.wasCanceled()) {
			return;
		}
		
		ImagePlus reImp = WindowManager.getImage(gd.getNextChoice());
		ImagePlus imImp = WindowManager.getImage(gd.getNextChoice());
		
		IJJT_fft jtu = new IJJT_fft();
		
		Object[] re =  reImp.getStack().getImageArray();
		Object[] im =  imImp.getStack().getImageArray();
		jtu.reImToMagPh(re, im, reImp.getNSlices());		
		reImp.updateAndDraw();
		imImp.updateAndDraw();
		
		reImp.setTitle("MagnitudeFFT");
		imImp.setTitle("PhaseFFT");
		IJ.run(reImp, "Enhance Contrast", "saturated=0.35");
		IJ.run(imImp, "Enhance Contrast", "saturated=0.35");

	}

}
