package IJ_JTransforms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.awt.*;
import ij.gui.DialogListener;

import IJ_JTransforms.IJJT_fft.ComplexArr;
import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.PlugIn;

import jhd.ImageJAddins.GenericDialogAddin;
import jhd.ImageJAddins.GenericDialogAddin.*;

public class Plot_Operations implements PlugIn, DialogListener {

	class DialogParams {
		String plotName1, plotName2, opChoice, newPlotName;
		int index1, index2;
		boolean newPlot;
	}

	final static int xAxis = 0, yAxis = 1;
	GenericDialog gd = new GenericDialog("Plot Operations");
	GenericDialogAddin gda = new GenericDialogAddin();
	IJJT_fft ijtft = new IJJT_fft();

	DialogParams dp = new DialogParams();
	ChoiceField plot1CF, data1CF, plot2CF, data2CF;
	MessageField msgSF;
	CheckboxField newPlotCBX;
	final Color myColor = new Color(240, 230, 190);// slightly darker than buff
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		boolean result = true;
		if (e != null) {
			Object src = e.getSource();
			if (src instanceof Choice) {
				String plot1Name = plot1CF.getChoice().getSelectedItem();
				PlotWindow plot1Win = (PlotWindow) WindowManager.getWindow(plot1Name);
				Plot plot1 = plot1Win.getPlot();
				String[] dodList1 = plot1.getDataObjectDesignations();
				float[][] data1 = plot1.getDataObjectArrays(0);

				String plot2Name = plot2CF.getChoice().getSelectedItem();
				PlotWindow plot2Win = (PlotWindow) WindowManager.getWindow(plot2Name);
				Plot plot2 = plot2Win.getPlot();
				String[] dodList2 = plot2.getDataObjectDesignations();
				float[][] data2 = plot2.getDataObjectArrays(0);

				Choice choice = (Choice) src;
				String name = choice.getName();

				switch (name) {
				case "plot1Choice":
					data1CF.setChoices(dodList1);
					result = checkData1(data1[xAxis], data2[xAxis]);
					break;
				case "dataChoice1":
					result = checkData1(data1[xAxis], data2[xAxis]);
				case "plot2Choice":
					data2CF.setChoices(dodList2);
					result = checkData2(data1[xAxis], data2[xAxis]);
					break;
				case "data2Choice":
					result = checkData2(data1[xAxis], data2[xAxis]);
					break;
				}
			}
		}
		getSelections();
		return result;
	}

	public void DoRoutine() {

		if (gd.wasCanceled())
			return;

		getSelections();

		// Get the dialog data
		PlotWindow plotWin1 = (PlotWindow) WindowManager.getWindow(dp.plotName1);
		Plot plot1 = plotWin1.getPlot();

		PlotWindow plotWin2 = (PlotWindow) WindowManager.getWindow(dp.plotName2);
		Plot plot2 = plotWin2.getPlot();

		float[][] data1 = plot1.getDataObjectArrays(dp.index1);
		float[][] data2 = plot2.getDataObjectArrays(dp.index2);

		ComplexArr cpa = ijtft.fourierOperation_1D(dp.opChoice, data1[yAxis], data2[yAxis], data2[yAxis].length);

		float[] xData = data1[xAxis];

		String newPlotName = dp.plotName1 + "_" + dp.opChoice;// + "_" + dp.directionChoice;

		Plot opPlot = null;
		if (WindowManager.getImage(newPlotName) == null) {
			opPlot = new Plot(newPlotName, plot1.getLabel('x'), plot1.getLabel('y'));
			opPlot.setBackgroundColor(myColor);
		} else {
			plotWin1 = (PlotWindow) WindowManager.getWindow(newPlotName);
			opPlot = plotWin1.getPlot();
		}
		// Inherit style when adding an FFT to a new plot
		// can we inherit custom symbols?
		String style = plot1.getPlotObjectStyle(dp.index1);
		String label1 = plot1.getPlotObjectLabel(dp.index1);
		// Add the FFT data to the new Plot
		opPlot.addPoints(xData, cpa.re, Plot.LINE);
		int nPlotObjects = opPlot.getNumPlotObjects();
		opPlot.setPlotObjectStyle(nPlotObjects - 1, style);
		opPlot.setPlotObjectLabel(nPlotObjects - 1, label1 + dp.opChoice);

		// Build the legend list
		String legend = "";
		String delimiter = "\n";
		for (int i = 0; i < nPlotObjects; i++) {
			legend += opPlot.getPlotObjectLabel(i) + delimiter;
		}
		opPlot.addLegend(legend);
		// fftPlot.replace(index, delimiter, null, null);
		opPlot.setLimitsToFit(true);
		opPlot.show();
		setPlotProperties(plot1, plot2, opPlot, dp.opChoice);

	}

	@Override
	public void run(String arg) {

		DoDialog();
		if (gd.wasOKed()) {
			DoRoutine();
		}
	}

	private boolean bothEqualValues(float[] data1, float[] data2) {
		boolean result = true;
		if (data1.length == data2.length) {
			for (int i = 0; i < data1.length; i++) {
				if (data1[i] != data2[i]) {
					result = false;
					break;
				}
			}
		} else {
			result = false;
		}
		return result;
	}

	private boolean checkData1(float[] data1, float[] data2) {
		boolean result = false;

		if (data1.length != data2.length) {
			msgSF.setLabel("Lengths Not Equal");
			data1CF.getChoice().setBackground(Color.PINK);
		} else if (!isEven(data2.length)) {
			msgSF.setLabel("target is odd");
			data1CF.getChoice().setBackground(Color.PINK);
		} else if (!bothEqualValues(data1, data2)) {
			msgSF.setLabel("X axis values");
			data1CF.getChoice().setBackground(Color.PINK);
		} else {
			msgSF.setLabel(" ");
			data1CF.getChoice().setBackground(Color.WHITE);
			data2CF.getChoice().setBackground(Color.WHITE);
			result = true;
		}
		return result;
	}

	private boolean checkData2(float[] data1, float[] data2) {
		boolean result = false;

		if (data1.length != data2.length) {
			msgSF.setLabel("Lengths Not Equal");
			data2CF.getChoice().setBackground(Color.PINK);
		} else if (!isEven(data2.length)) {
			msgSF.setLabel("kernel is odd");
			data2CF.getChoice().setBackground(Color.PINK);
		} else if (!bothEqualValues(data1, data2)) {
			msgSF.setLabel("X axis values");
			data2CF.getChoice().setBackground(Color.PINK);
		} else {
			msgSF.setLabel(" ");
			data1CF.getChoice().setBackground(Color.WHITE);
			data2CF.getChoice().setBackground(Color.WHITE);
			result = true;
		}
		return result;
	}

	private void DoDialog() {
		String[] plotTitles = getPlotTitles();
		if (plotTitles.length == 0) {
			IJ.error("There are no Plot windows with\r\n" + "an even number of X axis values open.\r\n");
			return;
		}
		// Initialize the dialog data
		String[] opChoices = { "convolve", "correlate", "deconvolve" };
		PlotWindow plotWin1 = (PlotWindow) WindowManager.getWindow(plotTitles[0]);
		Plot plot1 = plotWin1.getPlot();
		String[] podList = plot1.getDataObjectDesignations();

		gd.addMessage("Both plot X axes must be the same\n"
				+ "with an even number of data points\n", myFont);
		// Add items, name and get item references
		gd.addChoice("Target Plot", plotTitles, plotTitles[0]);
		plot1CF = gda.getChoiceField(gd, null, "plot1Choice");

		gd.addChoice("Target Data", podList, podList[0]);
		data1CF = gda.getChoiceField(gd, null, "data1Choice");

		// Add items, name and get item references
		gd.addChoice("Kernel Plot", plotTitles, plotTitles[0]);
		plot2CF = gda.getChoiceField(gd, null, "plot2Choice");

		gd.addChoice("Kernel Data", podList, podList[0]);
		data2CF = gda.getChoiceField(gd, null, "data2Choice");

		gd.addMessage("   ", myFont);
		msgSF = gda.getMessageField(gd, "msgField");

		gd.addRadioButtonGroup("Operation:", opChoices, 1, 3, opChoices[0]);

		gd.addDialogListener(this);
		gd.setBackground(myColor);
		gd.addHelp("https://lazzyizzi.github.io/JTransforms.html#Plot%20Operations");
		gd.showDialog();
	}

	/**
	 * @return an array of open Plot titles with an even number of x values
	 */
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
		dp.plotName1 = gd.getNextChoice();
		dp.index1 = gd.getNextChoiceIndex();
		dp.plotName2 = gd.getNextChoice();
		dp.index2 = gd.getNextChoiceIndex();
		dp.opChoice = gd.getNextRadioButton();
	}

	private boolean isEven(int number) {
		if (Math.pow(-1.0, (double) number) == 1) {
			return true;
		} else {
			return false;
		}
	}

//	private boolean bothEvenLength(float[] data1, float[] data2) {
//		boolean result = false;
//
//		if (isEven(data1.length) && isEven(data2.length)) {
//			result = true;
//		}
//		return result;
//	}

	private void setPlotProperties(Plot plot1, Plot plot2, Plot opPLot, String operation) {
		Properties srcProps = plot1.getImagePlus().getImageProperties();

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
		destProps.add(plot1.getTitle());
		destProps.add("Kernel Image");
		destProps.add(plot2.getTitle());
		destProps.add("Operation");
		destProps.add(operation);

		String[] destPropsArr = destProps.toArray(new String[destProps.size()]);

		opPLot.getImagePlus().setProperties(destPropsArr);
		opPLot.getImagePlus().setProperties(destPropsArr);
	}

}
