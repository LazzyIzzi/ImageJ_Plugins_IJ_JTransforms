package IJ_JTransforms;

import ij.IJ;
import ij.ImagePlus;
//import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
//import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import IJ_JTransforms.IJJT_fft.ComplexArr;
import IJ_JTransforms.NameTagUtils.NameTagsFT;

/**
 * Computes and displays the 2D or 3D forward FFT using Piotr Wendykier's
  * @see <a href=
 *      "https://javadoc.io/doc/com.github.wendykierp/JTransforms/latest/index.html">JTransforms</a>
 
 * @author LazzyIzzi
 *
 */
public class Forward_FFT implements PlugInFilter{//, DialogListener {
	ImagePlus imp;
	ContrastEnhancer ce = new ContrastEnhancer();
	GenericDialog gd = new GenericDialog("Forward FFT");
	NameTagUtils ntu = new NameTagUtils();
	IJJT_fft ijft = new IJJT_fft();
	ComplexArr cpa = ijft.new ComplexArr();	
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);
	
	public void DoDialog() {
		String[] fftChoices = { "Current Slice 2D", "All Slices 2D", "All Slices 3D" };
		gd.addCheckbox("Compute_Centered FFT", false);
		gd.addCheckbox("Show_as_Magnitude and Phase:", false);
		if (imp.hasImageStack()) {
			gd.addChoice("FFT", fftChoices, fftChoices[0]);
		}
		gd.setBackground(myColor);
		gd.addHelp("https://lazzyizzi.github.io/JTransforms.html#Forward%20FFT");
		gd.showDialog();

	}
			
	@Override
	public void run(ImageProcessor ip) {
		
		DoDialog();
		if (gd.wasCanceled()) {
			return;
		}		
		DoRoutine();
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		if(isEven(imp.getWidth()) && isEven(imp.getHeight())) {
		return DOES_32+DOES_16+DOES_8G;
		}
		else {
			String msg = "Image width and height must be even\n"
					+ "For 3D FFT stack height must be even.";
			IJ.error(msg);
			return DONE;
		}
	}
	
	private void DoRoutine() {

		boolean centered = gd.getNextBoolean();
		boolean magPhase = gd.getNextBoolean();
		String fftChoice;
		if (imp.hasImageStack()) {
			fftChoice = gd.getNextChoice();
		} else {
			fftChoice = "Current Slice 2D";
		}

		int width = imp.getWidth();
		int height = imp.getHeight();
		int depth = imp.getNSlices();
		int curSlice = imp.getCurrentSlice();
		Object sliceData;
		String reMagTitle, imPhTitle;

		NameTagsFT nt;
		//Insert process Tags into output image names
		if (fftChoice.equals("Current Slice 2D")) {
			nt = ntu.makeNameTagsFT(true, centered, magPhase, fftChoice, curSlice);
		} else {
			nt = ntu.makeNameTagsFT(true, centered, magPhase, fftChoice);
		}
		
		reMagTitle = imp.getTitle();
		reMagTitle = ntu.removeTag(reMagTitle);
		reMagTitle = ntu.addTag(reMagTitle,nt.reTag);		
		imPhTitle = imp.getTitle();				
		imPhTitle = ntu.removeTag(imPhTitle);
		imPhTitle = ntu.addTag(imPhTitle,nt.imTag);
		
		if(fftChoice == "Current Slice 2D") depth = 1;		
		ImagePlus reImp = NewImage.createFloatImage(reMagTitle, width, height,depth, NewImage.FILL_BLACK);
		ImagePlus imImp = NewImage.createFloatImage(imPhTitle, width, height, depth, NewImage.FILL_BLACK);
	
		switch (fftChoice) {
		case "Current Slice 2D":			
			sliceData  =  imp.getStack().getProcessor(curSlice).getPixels();
			cpa= ijft.realForward_2D(sliceData,width,height,centered,magPhase);								
			reImp.getProcessor().setPixels(cpa.re);
			imImp.getProcessor().setPixels(cpa.im);
			break;
		case "All Slices 2D":
			for (int slice = 1; slice <= depth; slice++) {
				IJ.showProgress(slice, depth);
				sliceData =  imp.getStack().getProcessor(slice).getPixels();
				cpa= ijft.realForward_2D(sliceData, width, height,centered,magPhase);
				reImp.getStack().setPixels(cpa.re,slice);
				imImp.getStack().setPixels(cpa.im,slice);
			}
			reImp.setSlice(curSlice);
			imImp.setSlice(curSlice);
			break;
		case "All Slices 3D":
			IJ.showStatus("!3D FFT in progress, please wait");
			Object[] oVolData = imp.getStack().getImageArray();									
			cpa = ijft.realForward_3D(oVolData, width, height, depth, centered, magPhase);			
			reImp.getStack().setVoxels(0, 0, 0, width, height, depth, cpa.re);
			reImp.setSlice(depth/2);
			imImp.getStack().setVoxels(0, 0, 0, width, height, depth, cpa.im);
			imImp.setSlice(depth/2);
			break;
		}
		reImp.show();
		imImp.show();		
		ce.stretchHistogram(reImp, 0.05);
		ce.stretchHistogram(imImp, 0.05);
		reImp.updateAndDraw();
		imImp.updateAndDraw();
		
		setImageProperties(imp,reImp,imImp);
	}
	
	private boolean isEven(int number) {
		if (Math.pow(-1.0, (double) number) == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	private void setImageProperties(ImagePlus targImp, ImagePlus reImp,ImagePlus imImp) {
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
		destProps.add("Real Image");
		destProps.add(reImp.getTitle());
		destProps.add("Imaginary Image");
		destProps.add(imImp.getTitle());
		
		String[] destPropsArr = destProps.toArray(new String[destProps.size()]);

		reImp.setProperties(destPropsArr);
		imImp.setProperties(destPropsArr);
	}

	
}



