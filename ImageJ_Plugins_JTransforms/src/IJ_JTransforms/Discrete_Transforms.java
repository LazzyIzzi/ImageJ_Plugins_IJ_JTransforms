package IJ_JTransforms;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.jtransforms.dct.DoubleDCT_2D;
import org.jtransforms.dct.DoubleDCT_3D;
import org.jtransforms.dst.DoubleDST_2D;
import org.jtransforms.dst.DoubleDST_3D;
import org.jtransforms.dht.DoubleDHT_2D;
import org.jtransforms.dht.DoubleDHT_3D;

/**
 * Computes and displays the 2D or 3D Discrete Transform of images or stacks using Piotr Wendykier's
 * JTransforms
 * @see
 * @author LazzyIzzi
 */
public class Discrete_Transforms implements PlugInFilter, DialogListener {
	ImagePlus imp;
	ContrastEnhancer ce = new ContrastEnhancer();
	GenericDialog gd = new GenericDialog("Discrete Transforms");
	String dataChoice,fwdInvChoice,transformChoice;
	
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff
	final String[] dataChoices = { "Current Slice 2D", "All Slices 2D", "All Slices 3D" };
	//final String[] transformChoices = { "Cosine", "Sine", "Fourier", "Hartley" };
	final String[] transformChoices = { "Cosine", "Sine", "Hartley" };
	final String[] fwdInvChoices = { "Forward", "Inverse" };
	IJJT_tools ijtls = new IJJT_tools();

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		getSettings();
		boolean result = true;
		if(e!=null) {
			Object src = e.getSource();
			if (src instanceof Choice) {
				if(!imp.hasImageStack() && dataChoice != "Current Slice 2D") {
					result = false;
				}
			}			
		}
		return result;
	}
	
	
	@Override
	public void run(ImageProcessor ip) {
		
		DoDialog();		
		if (gd.wasCanceled()) {
			return;
		}		
		getSettings();
		DoRoutine();		
	}
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_32;
	}
	
	private void DoDialog() {
		gd.addChoice("Data", dataChoices, dataChoices[0]);
		gd.addChoice("Transform", transformChoices, transformChoices[0]);
		gd.addRadioButtonGroup("Direction", fwdInvChoices, 1, 2, fwdInvChoices[0]);
		gd.setBackground(myColor);
		gd.addDialogListener(this);
		gd.addHelp("https://lazzyizzi.github.io/JTransforms.html#Discrete%20Transforms");
		gd.showDialog();
	}
	
	private void DoRoutine() {			
		int cols = imp.getWidth();
		int rows = imp.getHeight();
		int nSlices = imp.getNSlices();
		Object[] oStkArr;
		Object oImgArr;
		double[] dblData;

		String title = transformChoice + "_" + dataChoice + "_" + fwdInvChoice;
		ImagePlus srcImp = null;
		
		DoubleDCT_2D dctDo2D;
		DoubleDCT_3D dctDo3D;
		DoubleDST_2D dstDo2D;
		DoubleDST_3D dstDo3D;
		DoubleDHT_2D dhtDo2D;
		DoubleDHT_3D dhtDo3D;
							
		switch (dataChoice) {
		case "Current Slice 2D":
			srcImp = imp.crop("whole-slice");

			oImgArr = srcImp.getProcessor().getPixels();
			dblData = ijtls.imageToDouble(oImgArr);
			switch (transformChoice) {
			case "Cosine":
				dctDo2D = new DoubleDCT_2D(rows, cols);
				switch (fwdInvChoice) {
				case "Forward":
					dctDo2D.forward(dblData, true);
					break;
				case "Inverse":
					dctDo2D.inverse(dblData, true);
					break;
				}
				srcImp.getProcessor().setPixels(ijtls.doubleToFloat(dblData));
				srcImp.setTitle(title);
				break;
			case "Sine":
				dstDo2D = new DoubleDST_2D(rows, cols);
				switch (fwdInvChoice) {
				case "Forward":
					dstDo2D.forward(dblData, true);
					break;
				case "Inverse":
					dstDo2D.inverse(dblData, true);
					break;
				}
				srcImp.getProcessor().setPixels(ijtls.doubleToFloat(dblData));
				srcImp.setTitle(title);
				break;
			case "Hartley":
				dhtDo2D = new DoubleDHT_2D(rows, cols);
				switch (fwdInvChoice) {
				case "Forward":
					dhtDo2D.forward(dblData);
					break;
				case "Inverse":
					dhtDo2D.inverse(dblData, true);
					break;
				}
				srcImp.getProcessor().setPixels(ijtls.doubleToFloat(dblData));
				srcImp.setTitle(title);
				break;
			}
			srcImp.show();
			ce.stretchHistogram(srcImp, 0.35);
			srcImp.updateAndDraw();
			setImageProperties(imp,srcImp);
			break;

		case "All Slices 2D":
			srcImp = imp.duplicate();			
				switch (transformChoice) {
				case "Cosine":
					dctDo2D = new DoubleDCT_2D(rows, cols);
					switch (fwdInvChoice) {
					case "Forward":
						for (int slice = 1; slice <= nSlices; slice++) {
							IJ.showProgress(slice, nSlices);
							oImgArr = srcImp.getStack().getProcessor(slice).getPixels();
							dblData = ijtls.imageToDouble(oImgArr);
							dctDo2D.forward(dblData, true);
							srcImp.getStack().setPixels( ijtls.doubleToFloat(dblData), slice);
						}
						break;
					case "Inverse":
						for (int slice = 1; slice <= nSlices; slice++) {
							IJ.showProgress(slice, nSlices);
							oImgArr = srcImp.getStack().getProcessor(slice).getPixels();
							dblData = ijtls.imageToDouble(oImgArr);
							dctDo2D.inverse(dblData, true);
							srcImp.getStack().setPixels( ijtls.doubleToFloat(dblData), slice);
						}
						break;
						}
					
					break;
				case "Sine":
					dstDo2D = new DoubleDST_2D(rows, cols);
					switch (fwdInvChoice) {
					case "Forward":
						for (int slice = 1; slice <= nSlices; slice++) {
							IJ.showProgress(slice, nSlices);
							oImgArr = srcImp.getStack().getProcessor(slice).getPixels();
							dblData = ijtls.imageToDouble(oImgArr);
							dstDo2D.forward(dblData, true);
							srcImp.getStack().setPixels( ijtls.doubleToFloat(dblData), slice);
						}
						break;
					case "Inverse":
						for (int slice = 1; slice <= nSlices; slice++) {
							IJ.showProgress(slice, nSlices);
							oImgArr = srcImp.getStack().getProcessor(slice).getPixels();
							dblData = ijtls.imageToDouble(oImgArr);
							dstDo2D.inverse(dblData, true);
							srcImp.getStack().setPixels( ijtls.doubleToFloat(dblData), slice);
						}
						break;
					}
					break;
				case "Hartley":
					dhtDo2D = new DoubleDHT_2D(rows, cols);
					switch (fwdInvChoice) {
					case "Forward":
						for (int slice = 1; slice <= nSlices; slice++) {
							IJ.showProgress(slice, nSlices);
							oImgArr = srcImp.getStack().getProcessor(slice).getPixels();
							dblData = ijtls.imageToDouble(oImgArr);
							dhtDo2D.forward(dblData);
							srcImp.getStack().setPixels( ijtls.doubleToFloat(dblData), slice);
						}
						break;
					case "Inverse":
						for (int slice = 1; slice <= nSlices; slice++) {
							IJ.showProgress(slice, nSlices);
							oImgArr = srcImp.getStack().getProcessor(slice).getPixels();
							dblData = ijtls.imageToDouble(oImgArr);
							dhtDo2D.inverse(dblData, true);
							srcImp.getStack().setPixels( ijtls.doubleToFloat(dblData), slice);
						}
						break;
					}
					break;
				}
			
			srcImp.setSlice(1);
			srcImp.setTitle(title);
			srcImp.show();
			ce.stretchHistogram(srcImp, 0.35);
			srcImp.updateAndDraw();
			setImageProperties(imp,srcImp);
			break;

		case "All Slices 3D":
			srcImp = imp.duplicate();
			srcImp.setTitle(title);
			dctDo3D = new DoubleDCT_3D(nSlices, rows, cols);
	
			oStkArr = srcImp.getStack().getImageArray();
			dblData = null;
			dblData =  ijtls.stackToDouble(oStkArr, srcImp.getNSlices());

			switch (transformChoice) {
			case "Cosine":
				dctDo3D = new DoubleDCT_3D(nSlices, rows, cols);
				switch (fwdInvChoice) {
				case "Forward":
					dctDo3D.forward(dblData, true);
					break;
				case "Inverse":
					dctDo3D.inverse(dblData, true);
					break;
				}
				break;
			case "Sine":
				dstDo3D = new DoubleDST_3D(nSlices, rows, cols);
				switch (fwdInvChoice) {
				case "Forward":
					dstDo3D.forward(dblData, true);
					break;
				case "Inverse":
					dstDo3D.inverse(dblData, true);
					break;
				}
				break;
			case "Hartley":
				dhtDo3D = new DoubleDHT_3D(nSlices, rows, cols);
				switch (fwdInvChoice) {
				case "Forward":
					dhtDo3D.forward(dblData);
					break;
				case "Inverse":
					dhtDo3D.inverse(dblData, true);
					break;
				}
				break;
			}

			float[][] stackData =  ijtls.doubleArrToFloatStack(dblData, nSlices, cols, rows) ;
			srcImp = NewImage.createFloatImage(title, cols, rows, nSlices, NewImage.FILL_BLACK);
			for (int slice = 0; slice < nSlices; slice++) {
				srcImp.getStack().setPixels(stackData[slice], slice + 1);
			}
			break;
		}
		srcImp.setTitle(title);
		srcImp.show();
		ce.setUseStackHistogram(true);
		ce.stretchHistogram(srcImp, 0.35);
		srcImp.updateAndDraw();
		setImageProperties(imp,srcImp);
	}

	private void getSettings() {
		gd.resetCounters();
		dataChoice = gd.getNextChoice();
		transformChoice = gd.getNextChoice();
		fwdInvChoice = gd.getNextRadioButton();
	}
	
	private void setImageProperties(ImagePlus srcImp, ImagePlus resultImp) {
		Properties srcProps = srcImp.getImageProperties();
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
		
		destProps.add("Src Image");
		destProps.add(srcImp.getTitle());
		destProps.add("This Image");
		destProps.add(resultImp.getTitle());
		
		String[] destPropsArr = destProps.toArray(new String[destProps.size()]);

		resultImp.setProperties(destPropsArr);
	}

}
