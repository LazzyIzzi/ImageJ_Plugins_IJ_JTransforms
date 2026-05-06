package IJ_JTransforms;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.TextField;

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import jhd.ImageJAddins.GenericDialogAddin;
import jhd.ImageJAddins.GenericDialogAddin.NumericField;
import jhd.ImageJAddins.GenericDialogAddin.SliderField;

public class Apply_Centered_Threshold implements PlugInFilter, DialogListener {
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);

	ImagePlus imp;
	String maskChoice;
	double roiTop, roiLeft, roiWidth, roiHeight, roiRadius;

	GenericDialog gd = new GenericDialog("Mask Centered FFTs");
	GenericDialogAddin gda = new GenericDialogAddin();
	String[] maskChoices = { "Current Slice 2D", "All Slices 2D", "All Slices 3D" };
	
	int width, height, depth, type;
	SliderField radiusSF;
	NumericField leftNF, topNF, widthNF, heightNF;
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (e != null) {
			Object src = e.getSource();
			if (src instanceof TextField) {
				TextField txt = (TextField) src;
				String name = txt.getName();
				getSelections();
				switch (name) {
				case "radius":
					// draw a circular ROI at the center of the image
					int size = width;
					if (size > height)
						size = height;
					double top = (height - (roiRadius * size)) / 2;
					double left = (width - (roiRadius * size)) / 2;
					imp.setRoi(new OvalRoi(left, top, roiRadius * size, roiRadius * size), true);
					leftNF.setNumber(left);
					topNF.setNumber(top);
					widthNF.setNumber(roiRadius * size);
					heightNF.setNumber(roiRadius * size);
					break;
				}
			}
		}
		getSelections();
		return true;
	}

	public void DoRoutine() {
		imp.setRoi(new OvalRoi(roiLeft, roiTop, roiWidth, roiHeight));
		ImageProcessor ip = imp.getProcessor();
		Roi roi = imp.getRoi();

		switch (maskChoice) {
		case "Current Slice 2D":
			if (roi != null) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (!roi.contains(x, y)) {
							ip.putPixelValue(x, y, 0);
						}
					}
				}
			}
			break;

		case "All Slices 2D":
			for (int slice = 1; slice <= imp.getNSlices(); slice++) {
				imp.setSlice(slice);
				if (roi != null) {
					for (int y = 0; y < height; y++) {
						for (int x = 0; x < width; x++) {
							if (!roi.contains(x, y)) {
								ip.putPixelValue(x, y, 0);
							}
						}
					}
				}
			}
			break;

		case "All Slices 3D":
			Object[] oImageArr = imp.getStack().getImageArray();

			int i, j, k;
			float[] image;
			double radius = imp.getRoi().getFloatHeight() / 2;
			double radiusSquared, dx, dy, dz, distanceSquared;

			radiusSquared = radius * radius;
			double x0 = width / 2;
			;
			double y0 = height / 2;
			double z0 = depth / 2;

			for (k = 0; k < depth; k++) {
				image = (float[]) oImageArr[k];
				for (j = 0; j < height; j++) {
					for (i = 0; i < width; i++) {
						// vector lengths from x0,y0,z0
						dx = i - x0;
						dy = j - y0;
						dz = k - z0;
						distanceSquared = (dx * dx) + (dy * dy) + (dz * dz);

						if (distanceSquared > radiusSquared) {
							image[i + j * width] = 0;
						}
					}
				}
			}
			imp.setSlice(depth / 2);
			break;
		}
	}

	@Override
	public void run(ImageProcessor ip) {
		DoDialog();
		if (gd.wasOKed()) {
			getSelections();
			DoRoutine();
		}
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		width = imp.getWidth();
		height = imp.getHeight();
		depth = imp.getNSlices();
		return DOES_32;

	}

	private void DoDialog() {
		gd.setBackground(myColor);
		gd.addMessage("Use With 2D centered real-imaginary FFTs\r\n" + "Sets region outside of ROI to zero", myFont);
		gd.addSlider("Cutoff Frequency:", 0, 1, 0.99);
		radiusSF = gda.getSliderField(gd, null, null, "radius");
		if (imp.getNSlices() > 1) {
			gd.addChoice("Mask", maskChoices, maskChoices[0]);
		}
		// add the fields required for macro recorder
		// hide them from user
		gd.addNumericField("Left", 0);
		leftNF = gda.getNumericField(gd, null, "Left");
		leftNF.getLabel().setVisible(false);
		leftNF.getNumericField().setVisible(false);
		gd.addNumericField("Right", 0);
		topNF = gda.getNumericField(gd, null, "Top");
		topNF.getLabel().setVisible(false);
		topNF.getNumericField().setVisible(false);
		gd.addNumericField("Width", 0);
		widthNF = gda.getNumericField(gd, null, "Width");
		widthNF.getLabel().setVisible(false);
		widthNF.getNumericField().setVisible(false);
		gd.addNumericField("Height", 0);
		heightNF = gda.getNumericField(gd, null, "Height");
		heightNF.getLabel().setVisible(false);
		heightNF.getNumericField().setVisible(false);
		gd.pack();

		gd.addDialogListener(this);
		gd.showDialog();
	}

	private void getSelections() {
		gd.resetCounters();
		if (imp.getNSlices() > 1) {
			maskChoice = gd.getNextChoice();
		} else {
			maskChoice = "Current Slice 2D";
		}
		roiRadius = gd.getNextNumber();
		roiLeft = gd.getNextNumber();
		roiTop = gd.getNextNumber();
		roiWidth = gd.getNextNumber();
		roiHeight = gd.getNextNumber();
	}
}
