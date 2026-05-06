package IJ_JTransforms;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.ContrastEnhancer;
import ij.plugin.PlugIn;
import jhd.ImageJAddins.GenericDialogAddin;
import jhd.ImageJAddins.GenericDialogAddin.ChoiceField;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.awt.*;
import IJ_JTransforms.NameTagUtils.*;
import IJ_JTransforms.IJJT_fft.ComplexArr;

/**
 * Computes and displays the 2D or 3D inverse FFT using Piotr Wendykier's
  * @see <a href=
 *      "https://javadoc.io/doc/com.github.wendykierp/JTransforms/latest/index.html">JTransforms</a>
 * @author LazzyIzzi
 *
 */
public class Inverse_FFT implements PlugIn, DialogListener {
	

	class DialogParams {
		ImagePlus reImp, imImp;
		boolean centered, magPhase;
		String fftChoice;
	}

	ContrastEnhancer ce = new ContrastEnhancer();
	GenericDialog gd = new GenericDialog("Inverse FFT");
	GenericDialogAddin gda = new GenericDialogAddin();
	DialogParams dp = new DialogParams();
	ChoiceField reCF, imCF, ftCF;
	NameTagUtils ntu = new NameTagUtils();
	IJJT_fft ijft = new IJJT_fft();
	ComplexArr cpa = ijft.new ComplexArr();
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);

	final String[] fftChoices = { "Current Slice 2D", "All Slices 2D", "All Slices 3D" };
	final String[] fftChoice2D = { "Current Slice 2D" };

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		boolean result = true;
		if (e != null) {
			Object src = e.getSource();
			if (src instanceof Choice) {
				getSelections();
				Choice choice = (Choice) src;
				String name = choice.getName();
				switch (name) {
				case "reImage":
				case "imImage":
					gd.resetCounters();
					ImagePlus reImp = WindowManager.getImage(gd.getNextChoice());
					ImagePlus imImp = WindowManager.getImage(gd.getNextChoice());
					int wRe,hRe,dRe, wIm,hIm,dIm;
					wRe = reImp.getWidth();
					hRe = reImp.getHeight();
					dRe = reImp.getNSlices();
					wIm = imImp.getWidth();
					hIm = imImp.getHeight();
					dIm = imImp.getNSlices();
					// Images must be the same size
					if (wRe != wIm || hRe != hIm || dRe != dIm || !isEven(wRe) || !isEven(hRe)) {
						result = false;
					}
					if (dRe > 1 && dRe == dIm) {
						ftCF.setChoices(fftChoices);
					} else {
						ftCF.setChoices(fftChoice2D);
					}
				}
			}
		}
		getSelections();
		return result;
	}

	@Override
	public void run(String arg) {

		DoDialog();
		if (gd.wasOKed()) {
			DoRoutine();
		}
	}

	private void DoDialog() {
		String msg = "This plugin requires two same-size even dimension images.";
		String[] titles = WindowManager.getImageTitles();

		// Default to the same image for both choices
		// so that the initial same size criterion is met by default
		gd.addChoice("Real_or_Magnitude Image", titles, titles[0]);
		reCF = gda.getChoiceField(gd, null, "reImage");
		gd.addChoice("Imaginary_or_Phase Image", titles, titles[0]);
		imCF = gda.getChoiceField(gd, null, "imImage");
		gd.addChoice("Inverse_FFT", fftChoices, fftChoices[0]);
		ftCF = gda.getChoiceField(gd, null, "ft");
		if (WindowManager.getImage(titles[0]).getNSlices() == 1) {
			ftCF.setChoices(fftChoice2D);
		}
		gd.addCheckbox("Input is Centered FFT", false);
		gd.addCheckbox("Input is Magnitude and Phase:", false);
		gd.addMessage(msg, myFont);
		gd.addDialogListener(this);
		gd.setBackground(myColor);
		gd.showDialog();

		getSelections();
	}

	private void DoRoutine() {

		if (gd.wasCanceled())
			return;

		getSelections();

		int width = dp.reImp.getWidth();
		int height = dp.reImp.getHeight();
		int depth = dp.reImp.getNSlices();
		int curSlice = dp.reImp.getCurrentSlice();
		String reMagTitle, imPhTitle;
		ImagePlus reInvImp = null, imInvImp = null;

		NameTagsFT nt;
		// Insert process Tags into output image names
		if (dp.fftChoice.equals("Current Slice 2D")) {
			nt = ntu.makeNameTagsFT(false, dp.centered, dp.magPhase, dp.fftChoice, curSlice);
		} else {
			nt = ntu.makeNameTagsFT(false, dp.centered, dp.magPhase, dp.fftChoice);
		}
		reMagTitle = dp.reImp.getTitle();
		reMagTitle = ntu.removeTag(reMagTitle);
		reMagTitle = ntu.addTag(reMagTitle, nt.reTag);
		imPhTitle = dp.imImp.getTitle();
		imPhTitle = ntu.removeTag(imPhTitle);
		imPhTitle = ntu.addTag(imPhTitle, nt.imTag);
		
		if(dp.fftChoice == "Current Slice 2D") depth = 1;
		reInvImp = NewImage.createFloatImage(reMagTitle, width, height, depth, NewImage.FILL_BLACK);
		imInvImp = NewImage.createFloatImage(imPhTitle, width, height, depth, NewImage.FILL_BLACK);

		switch (dp.fftChoice) {
		case "Current Slice 2D":
			Object reData = dp.reImp.getStack().getProcessor(curSlice).getPixels();
			Object imData = dp.imImp.getStack().getProcessor(curSlice).getPixels();
			cpa = ijft.complexInverse_2D(reData, imData, width, height, dp.centered, dp.magPhase);
			reInvImp.getProcessor().setPixels(cpa.re);
			imInvImp.getProcessor().setPixels(cpa.im);
			break;
		case "All Slices 2D":
			for (int slice = 1; slice <= depth; slice++) {
				IJ.showProgress(slice, depth);
				Object oRe = dp.reImp.getStack().getProcessor(slice).getPixels();
				Object oIm = dp.imImp.getStack().getProcessor(slice).getPixels();
				cpa = ijft.complexInverse_2D(oRe, oIm, width, height,dp.centered, dp.magPhase);
				reInvImp.getStack().setPixels(cpa.re, slice);
				imInvImp.getStack().setPixels(cpa.im, slice);
			}
			reInvImp.setSlice(curSlice);
			imInvImp.setSlice(curSlice);
			break;
		case "All Slices 3D":
			IJ.showStatus("!3D FFT in progress, please wait");
			Object[] oRe = dp.reImp.getStack().getImageArray();
			Object[] oIm = dp.imImp.getStack().getImageArray();			
			cpa = ijft.complexInverse_3D(oRe,oIm, width, height, depth, dp.centered, dp.magPhase);			
			reInvImp.getStack().setVoxels(0, 0, 0, width, height, depth, cpa.re);
			reInvImp.setSlice(depth/2);			
			imInvImp.getStack().setVoxels(0, 0, 0, width, height, depth, cpa.im);
			imInvImp.setSlice(depth/2);
			break;
		}		
		reInvImp.show();
		imInvImp.show();		
		ce.stretchHistogram(reInvImp, 0.35);		
		ce.stretchHistogram(imInvImp, 0.35);
		reInvImp.updateAndDraw();
		imInvImp.updateAndDraw();
		
		setImageProperties(reInvImp,imInvImp,dp);
	}
	
	private void getSelections() {
		gd.resetCounters();
		dp.centered = gd.getNextBoolean();
		dp.magPhase = gd.getNextBoolean();
		dp.reImp = WindowManager.getImage(gd.getNextChoice());
		dp.imImp = WindowManager.getImage(gd.getNextChoice());
		dp.fftChoice = gd.getNextChoice();
	}

	private void setImageProperties(ImagePlus reImp, ImagePlus imImp, DialogParams dp) {
		ArrayList<String> destProps = new ArrayList<String>();
		Properties srcProps = dp.reImp.getImageProperties();
		if (srcProps != null) {
			// Copy the projector properties
			Enumeration<Object> srcKey = srcProps.keys();
			Enumeration<Object> srcElem = srcProps.elements();
			while (srcKey.hasMoreElements()) {
				destProps.add((String) srcKey.nextElement());
				destProps.add((String) srcElem.nextElement());
			}
		}
		srcProps = dp.imImp.getImageProperties();
		if (srcProps != null) {
			// Copy the projector properties
			Enumeration<Object> srcKey = srcProps.keys();
			Enumeration<Object> srcElem = srcProps.elements();
			while (srcKey.hasMoreElements()) {
				destProps.add((String) srcKey.nextElement());
				destProps.add((String) srcElem.nextElement());
			}
		}

		String formatStr;
		if (dp.centered) {
			formatStr = "Centered";
		} else {
			formatStr = "Normal";
		}
		if (dp.magPhase) {
			formatStr += " Mag-Phase";
		} else {
			formatStr += " Re-Im";
		}

		destProps.add("Input Real Image");
		destProps.add(dp.reImp.getTitle());
		destProps.add("Input Imaginary Image");
		destProps.add(dp.imImp.getTitle());
		destProps.add("Slice Choice");
		destProps.add(dp.fftChoice);
		destProps.add("Input Images Format");
		destProps.add(formatStr);
		destProps.add("Output Real Image");
		destProps.add(reImp.getTitle());
		destProps.add("Output Imaginary Image");
		destProps.add(imImp.getTitle());

		String[] destPropsArr = destProps.toArray(new String[destProps.size()]);

		reImp.setProperties(destPropsArr);
		imImp.setProperties(destPropsArr);
	}
	
	private boolean isEven(int number) {
		if (Math.pow(-1.0, (double) number) == 1) {
			return true;
		} else {
			return false;
		}
	}
}