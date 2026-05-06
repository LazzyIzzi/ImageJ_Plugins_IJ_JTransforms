package IJ_JTransforms;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
//import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import jhd.ImageJAddins.GenericDialogAddin;
import jhd.ImageJAddins.GenericDialogAddin.*;

//import java.awt.event.*;
import java.awt.*;

/**
 * A tool for creating "tagged" images. Uses a Materials List JFrame viewer to
 * show tags. The viewer cannot be accessed by the user because of the parent
 * genericDialog The integer tag values are the indices of a list of material
 * names, formulas and densities. A tag's formula and density is used to compute
 * linear attenuation at a single photon energy. JHD 12/4/2021
 */

public class Apply_Threshold implements PlugInFilter, DialogListener// , ActionListener
{

	String[] matlNames;
	int[] matlTags;
	String[] filteredMatlNames;
	int[] filteredMatlTags;
	String matlFilter;

	ChoiceField materialCF;
	StringField filterSF;
	SliderField lowerSF, upperSF;
	ButtonField addMaterialBF;

	int matlIndex; // the position of the material in the list
	String path; // a file path for saving the dialog box values
	float low, high;
	int width, height, depth;

	GenericDialog gd;
	ImagePlus grayImp;
	ImagePlus tagImp;
	ImageProcessor ip;

	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff

	// *****************************************************************

	@Override
	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		this.grayImp = imp;
		return DOES_32 + DOES_16 + DOES_8G;
	}

	// *****************************************************************

	@Override
	public void run(ImageProcessor ip) {
		this.ip = ip;
		if (IJ.versionLessThan("1.53u")) {
			IJ.showMessage("Newer ImageJ Version Required",
					"Update ImageJ to version 1.53u or better to run this plugin");
			return;
		}

		width = grayImp.getWidth();
		height = grayImp.getHeight();
		depth = grayImp.getNSlices();

		GenericDialogAddin gda = new GenericDialogAddin();

		gd = new GenericDialog("Apply Threshold");
		gd.addDialogListener(this);

		ImageStatistics stats = grayImp.getStatistics();
		gd.addSlider("Lower", stats.min, stats.max, stats.min);
		lowerSF = gda.getSliderField(gd, null, null, "lowerSlider");
		gd.addSlider("Upper", stats.min, stats.max, stats.max);
		upperSF = gda.getSliderField(gd, null, null, "upperSlider");

		gd.setBackground(myColor);
		gd.showDialog();

		if (gd.wasOKed()) {
			ip.resetThreshold();
			Object oPixels = grayImp.getProcessor().getPixels();
			if(oPixels instanceof float[]) {
				float[] pixels = (float[])oPixels;
				for(int i= 0;i<pixels.length;i++) {
					if(pixels[i] < low || pixels[i]>high) {
						pixels[i]=0;
					}
				}				
			}
			if(oPixels instanceof short[]) {
				short[] pixels = (short[])oPixels;
				for(int i= 0;i<pixels.length;i++) {
					int pixel =  Short.toUnsignedInt( pixels[i]);  
					if(pixel < low || pixel>high) {
						pixels[i]=0;
					}
				}				
			}
			if(oPixels instanceof byte[]) {
				byte[] pixels = (byte[])oPixels;
				for(int i= 0;i<pixels.length;i++) {
					int pixel = Byte.toUnsignedInt(pixels[i]);
					if(pixel < low || pixel>high) {
						pixels[i]=0;
					}
				}				
			}
			
			grayImp.updateAndDraw();
		}
		if (gd.wasCanceled()) {
			tagImp.close();
			return;
		}
	}

	// *****************************************************************

	private boolean getSelections() {
		boolean settingsOK = true;
		low = (float) gd.getNextNumber();
		high = (float) gd.getNextNumber();
		if (Double.isNaN(low) || Double.isNaN(high))
			settingsOK = false;
		return settingsOK;
	}

	// *****************************************************************

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		boolean dialogOK = false;
		if (e != null) {
			dialogOK = getSelections();
			if (dialogOK) {
				Object src = e.getSource();
				if (src instanceof TextField) {
					TextField tf = (TextField) src;
					String name = tf.getName();
					switch (name) {
					case "lowerSlider":
					case "upperSlider":
						ip.setThreshold(low, high, ImageProcessor.RED_LUT);
						grayImp.updateAndDraw();
						break;
					}
				}
			}
		}
		return dialogOK;
	}

	// *****************************************************************

//	@Override
//	public void actionPerformed(ActionEvent e)
//	{
//		Button btn = (Button)e.getSource();
//		String btnLabel = btn.getLabel();
//		switch(btnLabel)
//		{
//		case "Add Material to Tag Image":
//			getSelections();
//			if(matlIndex>=0)
//			{		
//				float[] tagPix = (float[])tagImp.getStack().getVoxels(0, 0, 0, width, height, depth, null);
//				float[] grayPix = (float[])grayImp.getStack().getVoxels(0, 0, 0, width, height, depth, null);
//				int size = width*height*depth;	
//				for(int i=0;i<size;i++)
//				{
//					if(grayPix[i]<=high && grayPix[i] >= low)
//					{
//							tagPix[i] =filteredMatlTags[matlIndex];
//					}
//				}
//				grayImp.getProcessor().resetThreshold();
//				tagImp.getStack().setVoxels(0, 0, 0, width, height, depth, tagPix);
//				ImageStatistics stats = tagImp.getStatistics();
//				tagImp.getProcessor().setMinAndMax(stats.min, stats.max);
//				IJ.run(tagImp, "3-3-2 RGB", "");
//				tagImp.updateAndDraw();
//			break;
//			}
//		}
//	} 
}
