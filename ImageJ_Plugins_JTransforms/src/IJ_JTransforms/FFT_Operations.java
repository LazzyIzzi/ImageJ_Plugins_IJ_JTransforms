package IJ_JTransforms;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import jhd.ImageJAddins.GenericDialogAddin;
import jhd.ImageJAddins.GenericDialogAddin.ChoiceField;
import IJ_JTransforms.IJJT_fft.ComplexArr;

public class FFT_Operations implements PlugInFilter, DialogListener {
	class DialogParams {
		ImagePlus targImp, kernImp;
		String sliceChoice,fftOpChoice;
		//double noiseCutoff;
	}
	ContrastEnhancer ce = new ContrastEnhancer();
	GenericDialog gd = new GenericDialog("FFT Operations");
	GenericDialogAddin gda = new GenericDialogAddin();
	final String[] fftOpChoices = { "correlate", "convolve", "deconvolve" };
	final String[] imageChoices = { "Slice - Slice 2D", "Stack - Slice 2D", "Stack - Stack 2D", "Stack - Stack 3D" };
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff
	
	IJJT_fft ijft = new IJJT_fft();
	DialogParams dp = new DialogParams();

	ChoiceField targCF, kernCF, imageCF;

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		boolean result = true;
		getSelections();

		if (e != null) {
			Object src = e.getSource();

			if (src instanceof Choice) {
				Choice choice = (Choice) src;
				String name = choice.getName();
				if (name.equals("target") || name.equals("kernel") || name.equals("image")) {
					int targWidth = dp.targImp.getWidth();
					int targHeight = dp.targImp.getHeight();
					int targDepth = dp.targImp.getNSlices();
					int kernWidth = dp.kernImp.getWidth();
					int kernHeight = dp.kernImp.getHeight();
					int kernDepth = dp.kernImp.getNSlices();
					switch (imageCF.getChoice().getSelectedItem()) {
					case "Slice - Slice 2D":
						if ((whEven(dp.targImp, dp.kernImp) == false) || targWidth != kernWidth
								|| targHeight != kernHeight) {
							IJ.error("Target and Kernel slices must have even and equal width and height");
							result = false;
						}
						break;
					case "Stack - Slice 2D":
						if (targDepth == 1) {
							IJ.error("Target is not a stack");
							result = false;
						} else {
							if ((whEven(dp.targImp, dp.kernImp) == false) || targWidth != kernWidth
									|| targHeight != kernHeight) {
								IJ.error("Target and Kernel slices must have even and equal width and height");
								result = false;
							}
						}
						break;
					case "Stack - Stack 2D":
					case "Stack - Stack 3D":
						if (targDepth == 1 || kernDepth == 1) {
							IJ.error("Target and kernel must be same size stacks");
							result = false;
						} else {
							if ((whdEven(dp.targImp, dp.kernImp) == false) || targWidth != kernWidth
									|| targHeight != kernHeight || targDepth != kernDepth) {
								IJ.error("Target and Kernel slices must have even and equal dimensions");
								result = false;
							}
						}
						break;
					}
				}

			}
		}
		return result;
	}
	@Override
	public void run(ImageProcessor ip) {
		String dir = IJ.getDir("plugins");
		String jarName = "JTransforms-3.1-with-dependencies.jar";
		JarChecker jarck = new JarChecker();
		if (jarck.findJarRecursively(dir, jarName) == false) {
			IJ.error(jarName + " Not Found in the ImageJ plugins folders.\n" + "Click \"Help\" for more information");
		}
		DoDialog();
		if (gd.wasOKed()) {
			DoRoutine();
		}
	}
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		return DOES_32+DOES_16+DOES_8G;
	}
	
	private void DoDialog(){
		String msg = "This plugin requires two same-size even dimension images.";
		String[] titles = WindowManager.getImageTitles();

		// Default to the same image for both choices
		// so that the initial same size criterion is met by default
		gd.addChoice("Target Image/Stack", titles, titles[0]);
		targCF = gda.getChoiceField(gd, null, "target");
		gd.addChoice("Kernel Image/Stack", titles, titles[0]);
		kernCF = gda.getChoiceField(gd, null, "kernel");
		gd.addChoice("FFT_Operation", fftOpChoices, fftOpChoices[0]);
		gd.addChoice("Target_and_Kernel", imageChoices, imageChoices[0]);
		imageCF = gda.getChoiceField(gd, null, "image");
		gd.addMessage(msg, myFont);
		gd.setBackground(myColor);
		gd.addDialogListener(this);
		gd.addHelp("https://lazzyizzi.github.io/JTransforms.html#FFT%20Operations");
		gd.showDialog();		
	}
	
	private void DoRoutine() {
		ComplexArr cpa;
		ImagePlus corrImp = null;
		Object oTarg, oKern;
		int i;
		getSelections();
		
		int targWidth =  dp.targImp.getWidth();
		int targHeight =  dp.targImp.getHeight();
		int targDepth =  dp.targImp.getNSlices();
		String newTitle = dp.targImp.getTitle();
		
		int loc = newTitle.lastIndexOf(".");
		if(loc == -1) {
			newTitle+="_"+ dp.fftOpChoice;
		} else {
			newTitle = newTitle.subSequence(0, loc)+"_"+ dp.fftOpChoice;
		}	

		switch (dp.sliceChoice) {
		case "Slice - Slice 2D":
			int targSlice = dp.targImp.getCurrentSlice();
			int kernSlice = dp.kernImp.getCurrentSlice();
			oTarg = dp.targImp.getStack().getProcessor(targSlice).getPixels();
			oKern = dp.kernImp.getStack().getProcessor(kernSlice).getPixels();
			cpa = ijft.fourierOperation_2D(dp.fftOpChoice, oTarg, oKern, targWidth, targHeight);
			corrImp = IJ.createImage(newTitle,targWidth, targHeight, 1, 32);
			corrImp.getProcessor().setPixels(cpa.re);
			break;

		case "Stack - Stack 2D":
			corrImp = IJ.createImage(newTitle,targWidth, targHeight, targDepth, 32);
			for (i = 1; i <= targDepth; i++) {
				IJ.showProgress(i, targDepth);
				oTarg = dp.targImp.getStack().getProcessor(i).getPixels();
				oKern = dp.kernImp.getStack().getProcessor(i).getPixels();
				cpa = ijft.fourierOperation_2D(dp.fftOpChoice, oTarg, oKern, targWidth, targHeight);
				corrImp.getStack().setPixels(cpa.re, i);
			}
			break;

		case "Stack - Slice 2D":
			corrImp = IJ.createImage(newTitle, targWidth, targHeight, targDepth, 32);
			oKern = dp.kernImp.getProcessor().getPixels();
			for (i = 1; i <= targDepth; i++) {
				IJ.showProgress(i, targDepth);
				oTarg = dp.targImp.getStack().getProcessor(i).getPixels();
				cpa = ijft.fourierOperation_2D(dp.fftOpChoice, oTarg, oKern, targWidth, targHeight);
				corrImp.getStack().setPixels(cpa.re, i);
			}
			break;

		case "Stack - Stack 3D":
			corrImp = IJ.createImage(newTitle, targWidth, targHeight, targDepth, 32);
			Object[] oTarg3D = dp.targImp.getStack().getImageArray();
			Object[] oKern3D = dp.kernImp.getStack().getImageArray();
			cpa = ijft.fourierOperation_3D(dp.fftOpChoice, oTarg3D, oKern3D, targWidth, targHeight, targDepth);
			corrImp.getStack().setVoxels(0, 0, 0, targWidth, targHeight, targDepth, cpa.re);
			break;
		}
		if(dp.sliceChoice!= "Slice - Slice 2D") {
			ce.setUseStackHistogram(true);
		}
		ce.stretchHistogram(corrImp, .35);
		corrImp.show();
		setImageProperties(dp.targImp,dp.kernImp,corrImp,dp.fftOpChoice,dp.sliceChoice);

	}


	

	private void getSelections() {
		gd.resetCounters();
		dp.targImp = WindowManager.getImage(gd.getNextChoice());
		dp.kernImp = WindowManager.getImage(gd.getNextChoice());
		dp.fftOpChoice = gd.getNextChoice();
		dp.sliceChoice = gd.getNextChoice();
	}
	
	private boolean isEven(int number) {
		if (Math.pow(-1.0, (double) number) == 1) {
			return true;
		} else {
			return false;
		}
	}

	private void setImageProperties(ImagePlus targImp, ImagePlus kernImp,ImagePlus corrImp, String operation, String slices) {
		Properties srcProps = targImp.getImageProperties();
		ArrayList<String> destProps = new ArrayList<String>();
		if (srcProps != null) {
			// Copy the projector properties
			Enumeration<Object> srcKey = srcProps.keys();
			Enumeration<Object> srcElem = srcProps.elements();
			while (srcKey.hasMoreElements()) {
				destProps.add((String) srcKey.nextElement());
				destProps.add((String) srcElem.nextElement());
			}
		}
		
		destProps.add("Target Image");
		destProps.add(targImp.getTitle());
		destProps.add("Kernel Image");
		destProps.add(kernImp.getTitle());
		destProps.add("Result Image");
		destProps.add(corrImp.getTitle());
		destProps.add("Operation");
		destProps.add(operation);
		destProps.add("Slices");
		destProps.add(slices);
		
		String[] destPropsArr = destProps.toArray(new String[destProps.size()]);
		corrImp.setProperties(destPropsArr);
	}
	
	private boolean whdEven(ImagePlus targImp, ImagePlus kernImp) {
		boolean result = false;
		if( isEven(dp.targImp.getWidth()) && 
				isEven(dp.targImp.getHeight()) && 
				isEven(dp.targImp.getNSlices()) && 
				isEven(dp.kernImp.getWidth()) && 
				isEven(dp.kernImp.getHeight()) &&
				isEven(dp.kernImp.getNSlices())){
			result = true;
		}
		return result;
	}

	private boolean whEven(ImagePlus targImp, ImagePlus kernImp) {
		boolean result = false;
		if( isEven(dp.targImp.getWidth()) &&
				isEven(dp.targImp.getHeight()) && 
				isEven(dp.kernImp.getWidth()) && 
				isEven(dp.kernImp.getHeight())){
			result = true;
		}
		return result;
	}
}
