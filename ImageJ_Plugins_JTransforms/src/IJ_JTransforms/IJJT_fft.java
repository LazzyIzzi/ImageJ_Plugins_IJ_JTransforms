package IJ_JTransforms;

import org.jtransforms.fft.DoubleFFT_2D;
import org.jtransforms.fft.DoubleFFT_3D;
import org.jtransforms.fft.DoubleFFT_1D;

/**
 * 2D and 3D Forward and Inverse FFT of ImageJ's image Objects using Piotr
 * Wendykier's JTransforms<br>
 * Image width and height must be even. Depth must be even for 3D transforms.
 * 
 * @see <a href=
 *      "https://javadoc.io/doc/com.github.wendykierp/JTransforms/latest/index.html">JTransforms</a>
 * @author LazzyIzzi
 */
public class IJJT_fft {

	public class ComplexArr {
		public float[] re;
		public float[] im;
	}

	IJJT_tools ijtls = new IJJT_tools();

	/**
	 * @param reData   1D float array e.g. fDataArr =
	 *                 srcPlot.getDataObjectArrays(reIndex);
	 * @param imData   1D float array e.g. fDataArr =
	 *                 srcPlot.getDataObjectArrays(imIndex);
	 * @param centered
	 * @param magPhase
	 * @return the 1D fft of the input data
	 */
	public ComplexArr complexInverse_1D(float[] reData, float[] imData, boolean centered, boolean magPhase) {
		double[] dblRe = ijtls.imageToDouble(reData);
		double[] dblIm = ijtls.imageToDouble(imData);

		if (magPhase) {
			ijtls.magPhToReIm(dblRe, dblIm);
		}

		double[] jtReIm = ijtls.reimToJTransformsComplex(dblRe, dblIm);
		DoubleFFT_1D fft = new DoubleFFT_1D(dblRe.length);
		fft.complexInverse(jtReIm, true);
		ComplexArr cpa = new ComplexArr();
		cpa.re = ijtls.getJTransformsReal(jtReIm, 1);
		cpa.im = ijtls.getJTransformsImaginary(jtReIm, 1);

		if (centered) {
			ijtls.phaseShift(cpa.re, cpa.re.length, 1, 1);
			ijtls.phaseShift(cpa.im, cpa.re.length, 1, 1);
		}
		return cpa;
	}
	
	
	/**
	 * @param oRe      The Object returned by "Object oRe =
	 *                 reImp.getProcessor().getPixels();"
	 * @param oIm      The Object returned by "Object oIm =
	 *                 imImp.getProcessor().getPixels();"
	 * @param width    the image width in pixels returned by imp.getWidth()
	 * @param height   the image height in pixels returned by imp.getHeight()
	 * @param centered true for a centered FFT
	 * @param magPhase true for a magnitude phase FFT
	 * @return the inverse 2D FFT of the oRe, oIm
	 */
	public ComplexArr complexInverse_2D(Object oRe, Object oIm, int width, int height, boolean centered,
			boolean magPhase) {
		ComplexArr cpa = new ComplexArr();

		double[] dblRe = ijtls.imageToDouble(oRe);
		double[] dblIm = ijtls.imageToDouble(oIm);

		if (magPhase) {
			ijtls.magPhToReIm(dblRe, dblIm);
		}

		double[] jtReIm = ijtls.reimToJTransformsComplex(dblRe, dblIm);

		DoubleFFT_2D fft = new DoubleFFT_2D(height, width);

		fft.complexInverse(jtReIm, true);

		cpa.re = ijtls.getJTransformsReal(jtReIm, 1);
		cpa.im = ijtls.getJTransformsImaginary(jtReIm, 1);

		if (centered) {
			ijtls.phaseShift(cpa.re, width, height, 1);
			ijtls.phaseShift(cpa.im, width, height, 1);
		}
		return cpa;
	}
	
	/**
	 * @param oRe      The Object[] returned by Object[] oRe =
	 *                 imp.getStack().getImageArray();
	 * @param width    the image width in pixels returned by imp.getWidth()
	 * @param height   the image height in pixels returned by imp.getHeight()
	 * @param depth    the image depth in pixels returned by imp.getNSlices()
	 * @param centered true for a centered FFT
	 * @param magPhase true for a magnitude phase FFT
	 * @return the inverse 3D FFT of the oRe,oIm
	 */
	public ComplexArr complexInverse_3D(Object[] oRe, Object[] oIm, int width, int height, int depth, boolean centered,
			boolean magPhase) {
		ComplexArr cpa = null;
		cpa = new ComplexArr();

		double[] reData = ijtls.stackToDouble(oRe, depth);
		double[] imData = ijtls.stackToDouble(oIm, depth);

		if (magPhase) {
			ijtls.magPhToReIm(reData, imData);
		}

		DoubleFFT_3D fft = new DoubleFFT_3D(depth, height, width);
		double[] jtReIm = ijtls.reImToJTransformsComplex(reData, imData);
		fft.complexInverse(jtReIm, true);

		cpa.re = ijtls.getJTransformsReal(jtReIm, depth);
		cpa.im = ijtls.getJTransformsImaginary(jtReIm, depth);

		if (centered) {
			ijtls.phaseShift(cpa.re, width, height, depth);
			ijtls.phaseShift(cpa.im, width, height, depth);
		}
		return cpa;
	}

	


	/**
	 * ImageJ Plots use 1D data
	 * 
	 * @param operation "correlate", "convolve", "deconvolve" <br>
	 * Note that deconvolution is a simplified version that works only for top-hat PSFs
	 * @param oTarget The Object returned by "Object oTarget = targetImp.getProcessor().getPixels();"
	 * @param oKernel The Object returned by "Object oKernel = kernelImp.getProcessor().getPixels();"
	 * @param width The width in pixels of both images
	 * @param height The height in pixels of both images
	 * @return the processed image real and imaginary components
	 * 
	 *         Plot Example<br>
	 *         <pre>
	 * float[][] data1 = plot1.getDataObjectArrays(index1);<br>
	 * float[][] data2 = plot2.getDataObjectArrays(index2);<br>
	 * ComplexArr cpa = ijtft.fourierOperation_1D(fftChoice,data1[yAxis], data2[yAxis]);
	 *         </pre>
	 * 
	 *         where index1 and index2 are the indices of the selected items in a
	 *         Choice and yAxis is a constant = 1;
	 */
	public ComplexArr fourierOperation_1D(String operation,Object oTarget, Object oKernel,int width) {
		//int width = oTarget.length;
		double[] dblTarg = ijtls.imageToDouble(oTarget);
		double[] dblKern = ijtls.imageToDouble(oKernel);
		ComplexArr cpaTarg = new ComplexArr();
		DoubleFFT_1D fft = new DoubleFFT_1D(width);
		if (dblTarg.length != dblKern.length) {
			throw new IllegalArgumentException("Target and Sample arrays must be the same size.");
		} else {
			double[] jtReImTarg = ijtls.realToJTransformsComplex(dblTarg);
			double[] jtReImKern = ijtls.realToJTransformsComplex(dblKern);

			fft.complexForward(jtReImTarg);
			fft.complexForward(jtReImKern);
			jtReImTarg = jtReImOperation(jtReImTarg, jtReImKern, operation, width, 1, 1);
			// ijtls.jtReImPhaseShift(jtReImTarg, 1, 1, width);

			fft.complexInverse(jtReImTarg, true);

			cpaTarg.re = ijtls.getJTransformsReal(jtReImTarg, 1);
			cpaTarg.im = ijtls.getJTransformsImaginary(jtReImTarg, 1);
		}
		return cpaTarg;
	}

	/**
	 * @param operation "correlate", "convolve", "deconvolve" <br>
	 *                  Note that deconvolution is a simplified version that works
	 *                  only for top-hat PSFs
	 * @param oTarget   The Object returned by "Object oTarget =
	 *                  targetImp.getProcessor().getPixels();"
	 * @param oKernel   The Object returned by "Object oKernel =
	 *                  kernelImp.getProcessor().getPixels();"
	 * @param width     The width in pixels of both images
	 * @param height    The height in pixels of both images
	 * @return the processed image real and imaginary components
	 */
	public ComplexArr fourierOperation_2D(String operation, Object oTarget, Object oKernel, int width, int height) {

		double[] dblTarg = ijtls.imageToDouble(oTarget);
		double[] dblKern = ijtls.imageToDouble(oKernel);
		ComplexArr cpaTarg = new ComplexArr();
		DoubleFFT_2D fft = new DoubleFFT_2D(height, width);

		if (dblTarg.length != dblKern.length) {
			throw new IllegalArgumentException("Target and Sample arrays must be the same size.");
		} else {
			double[] jtReImTarg = ijtls.realToJTransformsComplex(dblTarg);
			double[] jtReImKern = ijtls.realToJTransformsComplex(dblKern);
			fft.complexForward(jtReImTarg);
			fft.complexForward(jtReImKern);
			jtReImTarg = jtReImOperation(jtReImTarg, jtReImKern, operation, width, height, 1);
			fft.complexInverse(jtReImTarg, true);
			cpaTarg.re = ijtls.getJTransformsReal(jtReImTarg, 1);
			cpaTarg.im = ijtls.getJTransformsImaginary(jtReImTarg, 1);
		}
		return cpaTarg;
	}

	/**
	 * @param oTarget The Object returned by "Object oTarget =
	 *                targetImp.getProcessor().getPixels();"
	 * @param oKernel The Object returned by "Object oKernel =
	 *                kernelImp.getProcessor().getPixels();"
	 * @param width   The width in pixels of both images
	 * @param height  The height in pixels of both images
	 * @return the correlated image real and imaginary components
	 */
	public ComplexArr fourierOperation_3D(String operation, Object[] oTarget, Object[] oKernel, int width, int height,
			int depth) {
		double[] dblTarg = ijtls.stackToDouble(oTarget, depth);
		double[] dblKern = ijtls.stackToDouble(oKernel, depth);
		ComplexArr cpaTarg = new ComplexArr();
		DoubleFFT_3D fft = new DoubleFFT_3D(depth, height, width);
		if (dblTarg.length != dblKern.length) {
			throw new IllegalArgumentException("Target and Sample arrays must be the same size.");
		} else {
			double[] jtReImTarg = ijtls.realToJTransformsComplex(dblTarg);
			double[] jtReImKern = ijtls.realToJTransformsComplex(dblKern);
			fft.complexForward(jtReImTarg);
			fft.complexForward(jtReImKern);
			jtReImTarg = jtReImOperation(jtReImTarg, jtReImKern, operation, width, height, depth);
			fft.complexInverse(jtReImTarg, true);
			cpaTarg.re = ijtls.getJTransformsReal(jtReImTarg, 1);
			cpaTarg.im = ijtls.getJTransformsImaginary(jtReImTarg, 1);
		}
		return cpaTarg;
	}

	/**
	 * Converts In-Place FFT magnitude-phase to real-imaginary.<br>
	 * Requires 32-bit (float) data with the same width, height and number of
	 * slices..<br>
	 * ImageJ Example:<br>
	 * Object[] magnitude = magnitudeImp.getStack().getImageArray();<br>
	 * Object[] phase = phaseImp.getStack().getImageArray();<br>
	 * magPhToReIm(magnitude,phase,magnitudeImp.getNSlices();<br>
	 * 
	 * @param magnitude the magnitude part of an FFT
	 * @param phase     the phase part of an FFT
	 * @param nSlices   the depth of both source images.
	 */
	public void magPhToReIm(Object[] magnitude, Object[] phase, int nSlices) {
		float magVal, phVal;
		float[] magData;
		float[] phData;

		if (magnitude[0] instanceof float[] && phase[0] instanceof float[]) {
			for (int k = 0; k < nSlices; k++) {
				magData = (float[]) magnitude[k];
				phData = (float[]) phase[k];

				for (int i = 0; i < magData.length; i++) {
					magVal = magData[i];
					phVal = phData[i];
					magData[i] = (float) (magVal * Math.cos(phVal));
					phData[i] = (float) (magVal * Math.cos(Math.PI / 2 - phVal));
				}
			}
		}
	}

	/**
	 * @param reData   1D float array e.g. fDataArr =
	 *                 srcPlot.getDataObjectArrays(reIndex);
	 * @param centered
	 * @param magPhase
	 * @return the 1D fft of the input data
	 */
	public ComplexArr realForward_1D(float[] reData, boolean centered, boolean magPhase) {

		double[] dblData = ijtls.imageToDouble(reData);
		if (centered) {
			ijtls.phaseShift(dblData, dblData.length, 1, 1);
		}

		double[] jtReIm = ijtls.realToJTransformsComplex(dblData);
		DoubleFFT_1D fft = new DoubleFFT_1D(dblData.length);
		fft.complexForward(jtReIm);
		ComplexArr cpa = new ComplexArr();
		cpa.re = ijtls.getJTransformsReal(jtReIm, 1);
		cpa.im = ijtls.getJTransformsImaginary(jtReIm, 1);
		if (magPhase) {
			ijtls.reImToMagPh(cpa.re, cpa.im);
		}
		return cpa;
	}

	/**
	 * @param oReal    The Object returned by "Object oReal =
	 *                 imp.getProcessor().getPixels();"
	 * @param width    the image width in pixels returned by imp.getWidth()
	 * @param height   the image height in pixels returned by imp.getHeight()
	 * @param centered true for a centered FFT
	 * @param magPhase true for a magnitude phase FFT
	 * @return the forward 2D FFT of the oReal
	 */
	public ComplexArr realForward_2D(Object oReal, int width, int height, boolean centered, boolean magPhase) {

		double[] dblData = ijtls.imageToDouble(oReal);

		if (centered) {
			ijtls.phaseShift(dblData, width, height, 1);
		}

		double[] jtReIm = ijtls.realToJTransformsComplex(dblData);

		DoubleFFT_2D fft = new DoubleFFT_2D(height, width);
		fft.complexForward(jtReIm);
		ComplexArr cpa = new ComplexArr();
		cpa.re = ijtls.getJTransformsReal(jtReIm, 1);
		cpa.im = ijtls.getJTransformsImaginary(jtReIm, 1);

		if (magPhase) {
			ijtls.reImToMagPh(cpa.re, cpa.im);
		}
		return cpa;

	}
	// reDataArr = srcPlot.getDataObjectArrays(reIndex);

	/**
	 * @param oReal    The Object[] returned by "Object[] oReal =
	 *                 imp.getStack().getImageArray();"
	 * @param width    the image width in pixels returned by imp.getWidth()
	 * @param height   the image height in pixels returned by imp.getHeight()
	 * @param depth    the image depth in pixels returned by imp.getNSlices()
	 * @param centered true for a centered FFT
	 * @param magPhase true for a magnitude phase FFT
	 * @return the 3D FFT of the oReal
	 */
	public ComplexArr realForward_3D(Object[] oReal, int width, int height, int depth, boolean centered,
			boolean magPhase) {
		ComplexArr cpa = null;
		cpa = new ComplexArr();

		double[] stackData = ijtls.stackToDouble(oReal, depth);

		if (centered) {
			ijtls.phaseShift(stackData, width, height, depth);
		}

		DoubleFFT_3D fft = new DoubleFFT_3D(depth, height, width);
		double[] jtReIm = ijtls.realToJTransformsComplex(stackData);
		fft.complexForward(jtReIm);

		cpa.re = ijtls.getJTransformsReal(jtReIm, depth);
		cpa.im = ijtls.getJTransformsImaginary(jtReIm, depth);

		if (magPhase) {
			ijtls.reImToMagPh(cpa.re, cpa.im);
		}
		return cpa;
	}

	/**
	 * Converts In-Place FFT real-imaginary to magnitude-phase.<br>
	 * Requires 32-bit (float) data with the same width, height and number of
	 * slices..<br>
	 * ImageJ Example:<br>
	 * Object[] real = realImp.getStack().getImageArray();<br>
	 * Object[] imaginary = imaginaryImp.getStack().getImageArray();<br>
	 * reImToMagPh(real,imaginary,realImp.getNSlices();<br>
	 * 
	 * @param real      the real part of an FFT
	 * @param imaginary the imaginary part of an FFT
	 * @param nSlices   the depth of both source images.
	 */
	public void reImToMagPh(Object[] real, Object[] imaginary, int nSlices) {
		float reVal, imVal;
		float[] reData;
		float[] imData;

		if (real[0] instanceof float[] && imaginary[0] instanceof float[]) {
			for (int k = 0; k < nSlices; k++) {
				reData = (float[]) real[k];
				imData = (float[]) imaginary[k];

				for (int i = 0; i < reData.length; i++) {
					reVal = reData[i];
					imVal = imData[i];
					// Put magnitude data in real array
					reData[i] = (float) Math.sqrt(reVal * reVal + imVal * imVal);
					// Put phase data in imaginary array
					imData[i] = (float) Math.atan2((double) imVal, (double) reVal);
				}
			}
		}
	}

	/**
	 * @param jtReImTarg
	 * @param jtReImKern
	 * @param operation
	 * @param width
	 * @param height
	 * @param depth
	 * @return
	 */
	private double[] jtReImOperation(double[] jtReImTarg, double[] jtReImKern, String operation, int width, int height,
			int depth) {
		int reIndex, imIndex;
		double A, B, C, D, X;
		switch (operation) {
		case "correlate":
			for (int i = 0; i < jtReImTarg.length / 2; i++) {
				reIndex = 2 * i;
				imIndex = reIndex + 1;
				A = jtReImTarg[reIndex];
				B = jtReImTarg[imIndex];
				C = jtReImKern[reIndex];
				D = jtReImKern[imIndex];
				jtReImTarg[reIndex] = (A * C + B * D);
				jtReImTarg[imIndex] = (B * C - A * D);
			}
			break;
		case "convolve":
			for (int i = 0; i < jtReImTarg.length / 2; i++) {
				reIndex = 2 * i;
				imIndex = reIndex + 1;
				A = jtReImTarg[reIndex];
				B = jtReImTarg[imIndex];
				C = jtReImKern[reIndex];
				D = jtReImKern[imIndex];
				jtReImTarg[reIndex] = (A * C - B * D);
				jtReImTarg[imIndex] = (A * D + B * C);
			}
			break;
		case "deconvolve":
			for (int i = 0; i < jtReImTarg.length / 2; i++) {
				reIndex = 2 * i;
				imIndex = reIndex + 1;
				A = jtReImTarg[reIndex];
				B = jtReImTarg[imIndex];
				C = jtReImKern[reIndex];
				D = jtReImKern[imIndex];
				X = C * C + D * D;
				// xArr[i] = (float)X;
				// Limit noise caused by dividing by small number
				if (X <= 1E-12) {
					jtReImTarg[reIndex] = 0.0f;
					jtReImTarg[imIndex] = 0.0f;
				} else {
					jtReImTarg[reIndex] = ((A * C + B * D) / X);
					jtReImTarg[imIndex] = ((B * C - A * D) / X);
				}
			}
			break;
		}
		ijtls.jtReImPhaseShift(jtReImTarg, depth, height, width);
		return jtReImTarg;
	}
}
