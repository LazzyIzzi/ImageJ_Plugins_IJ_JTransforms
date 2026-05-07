package IJ_JTransforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.awt.*;
import ij.gui.DialogListener;

import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.PlugIn;
import jhd.ImageJAddins.GenericDialogAddin;
import jhd.ImageJAddins.GenericDialogAddin.*;
import IJ_JTransforms.IJJT_fft.ComplexArr;
import org.jtransforms.dst.FloatDST_1D;
import org.jtransforms.dct.FloatDCT_1D;
import org.jtransforms.dht.FloatDHT_1D;

public class Plot_Transforms implements PlugIn, DialogListener {

	class DialogParams {
		String plotTitleChoice, reChoice, imChoice, directionChoice, transformChoice;
		boolean centered, magPhase;
	}
	private final static int xAxis = 0;
	private final static int yAxis = 1;
	GenericDialog gd = new GenericDialog("Plot Data Transforms");
	GenericDialogAddin gda = new GenericDialogAddin();
	DialogParams dp = new DialogParams();
	IJJT_fft ijft = new IJJT_fft();
	IJJT_tools ijtls = new IJJT_tools();

	ChoiceField plotCF, reCF, imCF, transformCF;
	StringField newPlotSF;
	CheckboxField centeredCBF, mgPhCBF;

	RadioButtonField fwdInvRBF;
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff

	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		boolean result = true;
		if (e != null) {
			getSelections();

			Object src = e.getSource();
			if (src instanceof Choice) {
				Choice choice = (Choice) src;
				String name = choice.getName();
				switch (name) {
				case "plotChoice":
					String plotName = plotCF.getChoice().getSelectedItem();
					PlotWindow plotWin = (PlotWindow) WindowManager.getWindow(plotName);
					Plot plot = plotWin.getPlot();
					String[] podList = plot.getDataObjectDesignations();
					reCF.setChoices(podList);
					imCF.setChoices(podList);
					break;
				case "transform":
					String transformName = transformCF.getChoice().getSelectedItem();
					switch(transformName) {
					case "Fourier":
						imCF.getChoice().setVisible(true);
						centeredCBF.getCheckBox().setVisible(true);
						mgPhCBF.getCheckBox().setVisible(true);
							break;
					case "Hartley":
						imCF.getChoice().setVisible(false);
						centeredCBF.getCheckBox().setVisible(true);
						mgPhCBF.getCheckBox().setVisible(false);
						break;
					case "Sine": case "Cosine":
						centeredCBF.getCheckBox().setVisible(false);
						mgPhCBF.getCheckBox().setVisible(false);
						break;						
					}
					break;
				}
			}

			if (src instanceof Checkbox) {
				Checkbox ckbx = (Checkbox) src;
				String name = ckbx.getLabel();
				switch (name) {
				case "Forward":
					imCF.getChoice().setVisible(false);
					//gd.pack();
					gd.showDialog();
					break;
				case "Inverse":
					if (dp.transformChoice == "Fourier") {
						imCF.getChoice().setVisible(true);
					} else {
						imCF.getChoice().setVisible(false);
					}
					//gd.pack();
					gd.showDialog();
					break;
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
		String[] plotTitles = getPlotTitles();
		if (plotTitles.length == 0) {
			IJ.error("There are no Plot windows open.");
			return;
		}
		String[] transformChoices = { "Fourier", "Sine", "Cosine", "Hartley" };
		String[] directionChoices = { "Forward", "Inverse" };

		PlotWindow plotWin = (PlotWindow) WindowManager.getWindow(plotTitles[0]);
		Plot plot = plotWin.getPlot();
		String[] plotDataObjs = plot.getDataObjectDesignations();

		// create the dialog
		// Add items, name and get item references
		gd.addChoice("plot", plotTitles, plotTitles[0]);
		plotCF = gda.getChoiceField(gd, null, "plotChoice");

		gd.addChoice("Real Data", plotDataObjs, plotDataObjs[0]);
		reCF = gda.getChoiceField(gd, null, "reChoice");
		// gd.addToSameRow();
		gd.addChoice("Imaginary Data", plotDataObjs, plotDataObjs[0]);
		imCF = gda.getChoiceField(gd, null, "imChoice");
		imCF.getChoice().setVisible(false);
	
		gd.addChoice("Transform", transformChoices, transformChoices[0]);
		transformCF = gda.getChoiceField(gd, null, "transform");

		gd.addRadioButtonGroup("Direction", directionChoices, 1, 2, directionChoices[0]);
		fwdInvRBF = gda.getRadioButtonField(gd, null, "fwdInv");

		gd.addCheckbox("Compute_Centered Transform", false);
		centeredCBF = gda.getCheckboxField(gd, "centered");
		gd.addCheckbox("Show_as_Magnitude and Phase:", false);
		mgPhCBF = gda.getCheckboxField(gd, "magPhase");

		gd.addDialogListener(this);
		gd.setBackground(myColor);
		gd.addHelp("https://lazzyizzi.github.io/JTransforms.html#Plot%20Transforms");
		gd.showDialog();

	}

	private void doDTs() {
		float[][] reDataObjArr;
		float[] yData, xData;

		// get the source plot
		PlotWindow plotWin = (PlotWindow) WindowManager.getWindow(dp.plotTitleChoice);
		Plot srcPlot = plotWin.getPlot();

		// get the index of the selected data item
		int reIndex = reCF.getChoice().getSelectedIndex();
		String reLabel = srcPlot.getPlotObjectLabel(reIndex);

		// get the xAxis data and copy the yAxis Data
		reDataObjArr = srcPlot.getDataObjectArrays(reIndex);
		xData = reDataObjArr[xAxis];
		yData = Arrays.copyOf(reDataObjArr[yAxis], reDataObjArr[yAxis].length);

		// Do the transform
		switch (dp.transformChoice) {
		case "Sine":
			FloatDST_1D dst = new FloatDST_1D(xData.length);
			switch (dp.directionChoice) {
			case "Forward":
				dst.forward(yData, true);
				break;
			case "Inverse":
				dst.inverse(yData, true);
				break;
			}
			break;
		case "Cosine":
			FloatDCT_1D dct = new FloatDCT_1D(xData.length);
			switch (dp.directionChoice) {
			case "Forward":
				dct.forward(yData, true);
				break;
			case "Inverse":
				dct.inverse(yData, true);
				break;
			}
			break;
		case "Hartley":
			FloatDHT_1D dht = new FloatDHT_1D(xData.length);
			switch (dp.directionChoice) {
			case "Forward":
				if(dp.centered) {
					ijtls.phaseShift(yData, yData.length, 1, 1);
				}				
				dht.forward(yData);
				break;
			case "Inverse":
				dht.inverse(yData, true);
				if(dp.centered) {
					ijtls.phaseShift(yData, yData.length, 1, 1);
				}				
				break;
			}
			break;
		}

		// Create a name for the output plot
		String newPlotName = dp.plotTitleChoice + "_" + dp.transformChoice + "_" + dp.directionChoice;
		if (dp.centered) {
			newPlotName += "_Centered";
		}

		// If the requested Plot name does not exist, create it
		Plot transformPlot = null;
		if (WindowManager.getImage(newPlotName) == null) {
			transformPlot = new Plot(newPlotName, srcPlot.getLabel('x'), srcPlot.getLabel('y'));
			transformPlot.setBackgroundColor(myColor);
		} else {
			plotWin = (PlotWindow) WindowManager.getWindow(newPlotName);
			transformPlot = plotWin.getPlot();
		}

		// Add the transform data to the new Plot
		transformPlot.addPoints(xData, yData, Plot.LINE);
		transformPlot.setStyle(0, "black,none,1.5,Line");

		String fftLabel = reLabel + "_" + dp.directionChoice;
		transformPlot.addLegend(fftLabel, "Auto");
		transformPlot.setLimitsToFit(true);
		transformPlot.show();
	}

	private void doFourier() {
		float[][] reDataObjArr, imDataObjArr;
		float[] xData = null;
		String fftLabel;

		// get the source plot
		PlotWindow plotWin = (PlotWindow) WindowManager.getWindow(dp.plotTitleChoice);
		Plot srcPlot = plotWin.getPlot();

		// get the index of the selected data item
		int reIndex = reCF.getChoice().getSelectedIndex();
		int imIndex = imCF.getChoice().getSelectedIndex();
		String reLabel = srcPlot.getPlotObjectLabel(reIndex);
		String imLabel = srcPlot.getPlotObjectLabel(imIndex);

		// Do the FFT
		ComplexArr cpa = ijft.new ComplexArr();
		switch (dp.directionChoice) {
		case "Forward":
			reDataObjArr = srcPlot.getDataObjectArrays(reIndex);
			xData = reDataObjArr[xAxis];
			cpa = ijft.realForward_1D(reDataObjArr[yAxis], dp.centered, dp.magPhase);
			break;
		case "Inverse":
			imDataObjArr = srcPlot.getDataObjectArrays(imIndex);
			reDataObjArr = srcPlot.getDataObjectArrays(reIndex);
			xData = reDataObjArr[xAxis];
			cpa = ijft.complexInverse_1D(reDataObjArr[yAxis], imDataObjArr[yAxis], dp.centered, dp.magPhase);
			break;
		}

		// Create a name for the output plot
		String newPlotName = dp.plotTitleChoice + "_" + dp.transformChoice + "_" + dp.directionChoice;
		if (dp.centered) {
			newPlotName += "_Centered";
		}
		if (dp.magPhase) {
			newPlotName += "_MgPh";
		} else {
			newPlotName += "_ReIm";
		}

		// If the requested Plot name does not exist, create it
		Plot fftPlot = null;
		if (WindowManager.getImage(newPlotName) == null) {
			fftPlot = new Plot(newPlotName, srcPlot.getLabel('x'), srcPlot.getLabel('y'));
			fftPlot.setBackgroundColor(myColor);
		} else {
			plotWin = (PlotWindow) WindowManager.getWindow(newPlotName);
			fftPlot = plotWin.getPlot();
		}

		// Add the FFT data to the new Plot
		fftPlot.addPoints(xData, cpa.re, Plot.LINE);
		fftPlot.setStyle(0, "black,none,2.0,Line");
		fftPlot.addPoints(xData, cpa.im, Plot.LINE);
		fftPlot.setStyle(1, "red,none,2.0,Line");

		if (dp.magPhase) {
			fftLabel = reLabel + "_" + dp.directionChoice + "_Mg\n" + imLabel + "_" + dp.directionChoice + "_Ph";
		} else {
			fftLabel = reLabel + "_" + dp.directionChoice + "_Re\n" + imLabel + "_" + dp.directionChoice + "_Im";
		}

		fftPlot.addLegend(fftLabel, "Auto");
		fftPlot.setLimitsToFit(true);
		fftPlot.show();
		
		setPlotProperties(srcPlot,fftPlot,dp.transformChoice+", "+dp.directionChoice);

	}
	
	private void DoRoutine() {

		getSelections();

		switch (dp.transformChoice) {
		case "Fourier":
			doFourier();
			break;
		default:
			doDTs();
			break;

		}

	}


	private String[] getPlotTitles() {
		String[] titles = WindowManager.getImageTitles();
		ArrayList<String> plotTitles = new ArrayList<String>();

		for (int i = 0; i < titles.length; i++) {
			Window win = WindowManager.getWindow(titles[i]);
			if (win instanceof PlotWindow) {
				plotTitles.add(titles[i]);
			}
		}
		return plotTitles.toArray(new String[plotTitles.size()]);
	}

	private void getSelections() {
		gd.resetCounters();
		dp.plotTitleChoice = gd.getNextChoice();
		dp.reChoice = gd.getNextChoice();
		dp.imChoice = gd.getNextChoice();
		dp.directionChoice = gd.getNextRadioButton();
		dp.transformChoice = gd.getNextChoice();
		dp.centered = gd.getNextBoolean();
		dp.magPhase = gd.getNextBoolean();
	}

	private void setPlotProperties(Plot srcPlot, Plot fftPlot, String operation) {
		Properties srcProps = srcPlot.getImagePlus().getImageProperties();
		
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
		
		destProps.add("Source Plot");
		destProps.add(srcPlot.getTitle());
		destProps.add("Operation");
		destProps.add(operation);
		
		String[] destPropsArr = destProps.toArray(new String[destProps.size()]);

		fftPlot.getImagePlus().setProperties(destPropsArr);
		fftPlot.getImagePlus().setProperties(destPropsArr);
	}

}
