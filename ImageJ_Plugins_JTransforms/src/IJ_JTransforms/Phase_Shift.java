package IJ_JTransforms;

import java.awt.Color;
import java.awt.Font;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**Phase Shift for FFTs
 * 
 */
public class Phase_Shift implements PlugInFilter {

	ImagePlus imp;
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);
	GenericDialog gd = new GenericDialog("Phase Shift");
	ContrastEnhancer ce = new ContrastEnhancer();


	public Phase_Shift() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		this.imp = imp;
		return DOES_32;
	}
	
	public void DoDialog() {
		String[] choices = { "Current Slice 2D", "All Slices 2D", "All Slices 3D" };
		gd.addChoice("FFT", choices, choices[0]);		
		gd.setBackground(myColor);
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

	private void DoRoutine() {
		String choice = gd.getNextChoice();
		Object oData;
		int width = imp.getWidth();
		int height = imp.getHeight();
		int depth = imp.getNSlices();

		switch (choice) {
		case "Current Slice 2D":
			oData = imp.getStack().getProcessor(imp.getCurrentSlice()).getPixels();
			phaseShift(oData, width, height);
			break;
		case "All Slices 2D":
			if (imp.hasImageStack()) {
				for (int slice = 1; slice <= depth; slice++) {
					IJ.showProgress(slice, imp.getNSlices());
					oData = imp.getStack().getProcessor(slice).getPixels();
					phaseShift(oData, imp.getWidth(), imp.getHeight());
				}
			} else {
				oData = imp.getProcessor().getPixels();
				phaseShift(oData, width, height);
			}
			break;
		case "All Slices 3D":
			if (imp.hasImageStack()) {
				Object[] oStkData = imp.getStack().getImageArray();
				stackPhaseShift(oStkData, width, height, depth);
				ce.setUseStackHistogram(true);
			} else {
				oData = imp.getProcessor().getPixels();
				phaseShift(oData, width, height);
			}
			break;
		}

		ce.stretchHistogram(imp, 0.35);
		imp.updateAndDraw();
	}


	/**
	 * Applies a phase shift to a 3D image.<br>
	 * Set depth=1 for 2D image. Set depth=1 and height=1 for a 1D image
	 * 
	 * @param imageData the data to phase shift
	 * @param width     the number of columns in the 3D data
	 * @param height    the number of rows in the 3D data
	 * @param depth     the number of slices in the 3D data
	 */
	private void stackPhaseShift(Object[] oData, int width, int height, int depth) {
		double phase;
		int i, j, k;

		if (oData[0] instanceof float[]) {
			for (k = 0; k < depth; k++) {
				IJ.showStatus("!3D Phase Shift");
				IJ.showProgress(k, depth);
				float[] fArr = (float[]) oData[k];
				for (j = 0; j < height; j++) {
					for (i = 0; i < width; i++) {
						phase = Math.pow(-1, (double) (i + j + k));
						fArr[i + j * width] *= phase;
					}
				}
			}
		}		
	}

	/**
	 * Applies a phase shift to a 2D image.<br>
	 * Set depth=1 for 2D image. Set height=1 for 1D data
	 * 
	 * @param imageData the data to phase shift
	 * @param width     the number of columns in the 3D data
	 * @param height    the number of rows in the 3D data
	 * @param depth     the number of slices in the 3D data
	 */
	private void phaseShift(Object oData, int width, int height) {
		double phase;
		int i, j, home;

		if (oData instanceof float[]) {
			float[] data = (float[]) oData;
			for (j = 0; j < height; j++) {
				for (i = 0; i < width; i++) {
					phase = Math.pow(-1, (double) (i + j));
					home = i + j * width;
					data[home] *= phase;
				}
			}
		}
	}

}
