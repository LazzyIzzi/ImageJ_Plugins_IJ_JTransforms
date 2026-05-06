package IJ_JTransforms;



/**MiddleWare converting between ImageJ 2D and 3D(stacks) image formats and JTransforms sequence format.  ImageJ 32,16 and 8 bit formats are converted to 1D double arrays. JTransform sequence formats are converted to ImageJ 1D float arrays.
 * 
 */
public class IJJT_tools {

	/**
	 * @param data a 1D array of double
	 * @return a float copy of the double data
	 */
	protected float[] doubleToFloat(double[] data) {
		int sliceSize = data.length;
		float[] fltArr = new float[sliceSize];
		for (int i = 0; i <sliceSize; i++) {
				fltArr[i] = (float) data[i];
			}		
		return fltArr;
	}
	
	
	/**
	 * @param data a 1D array of float
	 * @return a double data as float data
	 */
	protected double[] floatToDouble(float[] data) {
		int sliceSize = data.length;
		double[] dblArr = new double[sliceSize];
		for (int i = 0; i <sliceSize; i++) {
				dblArr[i] =  data[i];
			}		
		return dblArr;
	}


	/**
	 * Extracts the imaginary part of JTransform sequenced data
	 * into ImageJ stack Object[] format
	 * 
	 * @param jtReIm  the real-imaginary sequenced data
	 * @param nSlices The z, depth etc. AKA number of ImageJ stack slices
	 * @param ReorIm use "Re" to return the real part or "Im" to return the Imaginary
	 * @return the imaginary part as an array of float arrays where the first index
	 *         points to the slice and the second index points to the pixels in that
	 *         slice.
	 */
	public float[] getJTransformsImaginary(double[] jtReIm, int nSlices) {
		int pixPerSlice =  (jtReIm.length/nSlices) / 2;
		float[] imData = new float [nSlices*pixPerSlice];
		int offset=0;

		for (int slice = 0; slice < nSlices; slice++) {
			for (int i = 0; i < pixPerSlice; i++) {
				try {
					offset = 2 * (i + slice * pixPerSlice);
					imData[i+slice*pixPerSlice] = (float)jtReIm[offset + 1];
				} catch (Exception e) {
					System.out.println("Slice=" + slice + ", offset=" + offset
							+ ", i=" + i + ", i + slice * pixPerSlice = " +i + slice * pixPerSlice);
					break;
				}
			}
		}
		return imData;
	}
	
	/**
	 * Extracts the imaginary part of JTransform sequenced data
	 * into ImageJ stack Object[] format
	 * 
	 * @param jtReIm  the real-imaginary sequenced data
	 * @param nSlices The z, depth etc. AKA number of ImageJ stack slices
	 * @param ReorIm use "Re" to return the real part or "Im" to return the Imaginary
	 * @return the imaginary part as an array of float arrays where the first index
	 *         points to the slice and the second index points to the pixels in that
	 *         slice.
	 */
	public float[] getJTransformsReal(double[] jtReIm, int nSlices) {
		int pixPerSlice = (jtReIm.length/nSlices) / 2;
		float[] reData = new float [nSlices*pixPerSlice];
		int offset=0;
			
		for (int slice = 0; slice < nSlices; slice++) {
			for (int i = 0; i < pixPerSlice; i++) {
				try {
					offset = 2 * (i + slice * pixPerSlice);
					reData[i+slice*pixPerSlice] = (float) jtReIm[offset];
				} catch (Exception e) {
					System.out.println("Slice=" + slice + ", offset=" + offset
							+ ", i=" + i + ", i + slice * pixPerSlice = " +i + slice * pixPerSlice);
					break;
				}
			}
		}		
		return reData;
	}

	
	/**
	 * Extracts the imaginary part of JTransform sequenced data
	 * into ImageJ stack Object[] format
	 * 
	 * @param jtReIm  the real-imaginary sequenced data
	 * @param nSlices The z, depth etc. AKA number of ImageJ stack slices
	 * @param ReorIm use "Re" to return the real part or "Im" to return the Imaginary
	 * @return the imaginary part as an array of float arrays where the first index
	 *         points to the slice and the second index points to the pixels in that
	 *         slice.
	 */
	public float[] jtransformsToFloat1D(double[] jtReIm, int nSlices, String ReOrIm) {
		int pixelCnt = jtReIm.length / nSlices / 2;
		float[] stackData = new float [pixelCnt];
		int offset,val;
		if(ReOrIm=="Re") val=0;
		else val = 1;

		for (int slice = 0; slice < nSlices; slice++) {
			for (int i = 0; i < pixelCnt; i++) {
				offset = 2 * (i + slice * pixelCnt);
				stackData[i] = (float)jtReIm[offset + val];
			}
		}
		return stackData;
	}
	
	
	
	/**
	 * converts ImageJ stack Object[] to double[][]
	 * 
	 * @param imageData use Object[] stackData = imp.getStack().getImageArray()
	 * @param depth     the number of slices in the stack
	 * @return a copy of the input data as double[][]
	 */
	protected double[] imageToDouble(Object imageData) {
		if (imageData == null)
			throw new IllegalArgumentException("Input array must not be null.");
		Class<?> cls = imageData.getClass();
		if (!cls.isArray())
			throw new IllegalArgumentException("Input must be an array.");
		
		int j;
		
		double[] dblArr = null;
		if (imageData instanceof float[]) {
			float[] fArr = (float[]) imageData;
			dblArr = new double[fArr.length];
			for (j = 0; j < fArr.length; j++) {
				dblArr[j] = fArr[j];
			}
		} else if (imageData instanceof int[]) {
			int[] intArr = (int[]) imageData;
			dblArr = new double[intArr.length];
			for (j = 0; j < intArr.length; j++) {
				dblArr[j] = Integer.toUnsignedLong(intArr[j]);
			}
		} else if (imageData instanceof short[]) {
			short[] shrtArr = (short[]) imageData;
			dblArr = new double[shrtArr.length];
			for (j = 0; j < shrtArr.length; j++) {
				dblArr[j] = Short.toUnsignedLong(shrtArr[j]);
			}
		}
		else if (imageData instanceof byte[]) {
			byte[] byteArr = (byte[]) imageData;
			dblArr = new double[byteArr.length];
			for (j = 0; j < byteArr.length; j++) {
				dblArr[j] = Byte.toUnsignedLong(byteArr[j]);
			}
		} else if (imageData instanceof double[]) {
			double[] doubleSlice = (double[]) imageData;
			dblArr = new double[doubleSlice.length];
			for (j = 0; j < doubleSlice.length; j++) {
				dblArr[j] = doubleSlice[j];
			}
		} else {
			throw new IllegalArgumentException("Input type not recognised.");
		}
		return dblArr;
	}
	
	/**
	 * Converts In-Place FFT magnitude-phase to real-imaginary.<br>
	 * Requires 32-bit (float) data with the same width, height and number of
	 * slices..<br>
	 * ImageJ Example:<br>
	 * float[] magnitude = magnitudeImp.getProcesor().getPixels();<br>
	 * Object[] phase = phaseImp..getProcesor().getPixels();<br>
	 * magPhToReIm(magnitude,phase);<br>
	 * 
	 * @param magnitude the magnitude part of an FFT
	 * @param phase     the phase part of an FFT
	 */
	protected void magPhToReIm(double[] magnitude, double[] phase) {
		double magVal, phVal;

		for (int i = 0; i < magnitude.length; i++) {
			try {
				magVal = magnitude[i];
				phVal = phase[i];
				magnitude[i] = (float) (magVal * Math.cos(phVal));
				phase[i] = (float) (magVal * Math.cos(Math.PI / 2 - phVal));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	/**
	 * Applies a phase shift to an image.<br>
	 * Set depth=1 for 2D image. Set depth=1 and height=1 for a 1D image
	 * 
	 * @param imageData the 2D data to phase shift
	 * @param width     the number of columns in the 3D data
	 * @param height    the number of rows in the 3D data
	 * @param depth     the number of slices in the 3D data
	 */
	protected void phaseShift(double[] imageData, int width, int height, int depth) {
		double phase;
		int i, j, k, home;

		for (k = 0; k < depth; k++) {
			for (j = 0; j < height; j++) {
				for (i = 0; i < width; i++) {
					phase = Math.pow(-1, (double) (i + j + k));
					home = i + j * width + k * width * height;
					imageData[home] *= phase;
				}
			}
		}
	}
	


	/**
	 * Applies a phase shift to an image.<br>
	 * Set depth=1 for 2D image. Set depth=1 and height=1 for a 1D image
	 * 
	 * @param imageData the 2D data to phase shift
	 * @param width     the number of columns in the 3D data
	 * @param height    the number of rows in the 3D data
	 * @param depth     the number of slices in the 3D data
	 */
	protected void phaseShift(float[] imageData, int width, int height, int depth) {
		float phase;
		int i, j, k, home;

		for (k = 0; k < depth; k++) {
			for (j = 0; j < height; j++) {
				for (i = 0; i < width; i++) {
					phase =(float) Math.pow(-1, (double) (i + j + k));
					home = i + j * width + k * width * height;
					imageData[home] *= phase;
				}
			}
		}
	}
	
	
	/**
	 * Convert real array to JTranforms sequenced array with zeros in the imaginary
	 * part
	 * 
	 * @param re The real data in a 1D array
	 * @return A re,im,re,im... sequenced array in JTransforms format with zeros in the imaginary
	 *         part
	 */
	protected double[] realToJTransformsComplex(double[] re) {
		double[] jtReIm = new double[re.length * 2];
		for (int i = 0; i < re.length; i++) {
			jtReIm[2 * i] = re[i];
			jtReIm[2 * i + 1] = 0;
		}
		return jtReIm;
	}
	
	
//	/**
//	 * Converts a real array to a 1D JTransforms sequenced array with zeros in the
//	 * imaginary part
//	 * 
//	 * @param real    The real data from imp.getStack().getImageArray();
//	 * @param nSlices the depth of the source images.
//	 * @return A sequenced array in JTransforms format with zeros in the imaginary
//	 *         part
//	 * @throws IllegalArgumentException if Object[] is not an array of float arrays
//	 */
//	private double[] stackRealToJTransformsComplex(Object[] real, int nSlices) {
//		double[] jtReIm = null;
//		float[] sliceData;
//		int offset;
//
//		if (real[0] instanceof float[]) {
//			sliceData = (float[]) real[0];
//			int pixelCnt = sliceData.length;
//			jtReIm = new double[2 * pixelCnt * nSlices];
//			for (int slice = 0; slice < nSlices; slice++) {
//				sliceData = (float[]) real[slice];
//				for (int i = 0; i < pixelCnt; i++) {
//					offset = 2 * (i + slice * pixelCnt);
//					jtReIm[offset] = sliceData[i];
//					jtReIm[offset + 1] = 0;
//				}
//			}
//		} else {
//			throw new IllegalArgumentException("argument not from a 32-bit ImageStack");
//		}
//		return jtReIm;
//	}


	/**
	 * Converts a real array to a 1D JTransforms sequenced array with zeros in the
	 * imaginary part
	 * 
	 * @param real    The real data from imp.getStack().getImageArray();
	 * @param nSlices the depth of the source images.
	 * @return A sequenced array in JTransforms format with zeros in the imaginary
	 *         part
	 * @throws IllegalArgumentException if Object[] is not an array of float arrays
	 */
	/**
	 * Convert separate real and imaginary arrays to JTranforms sequenced array.
	 * 
	 * @param real      The real data in a 1D array
	 * @param imaginary The imaginary data in a 1D array
	 * @return A sequenced array in JTransforms format
	 */
	protected double[] reimToJTransformsComplex(double[] real, double[] imaginary) {
		double[] jtReIm = new double[real.length * 2];
		for (int i = 0; i < real.length; i++) {
			jtReIm[2 * i] = real[i];
			jtReIm[2 * i + 1] = imaginary[i];
		}
		return jtReIm;
	}
	
	/**
	 * Convert real array to JTranforms sequenced array with zeros in the imaginary
	 * part
	 * 
	 * @param re The real data in a 1D array
	 * @return A re,im,re,im... sequenced array in JTransforms format with zeros in the imaginary
	 *         part
	 */
	protected double[] reImToJTransformsComplex(double[] re,double[]im) {
		double[] jtReIm = new double[re.length * 2];
		for (int i = 0; i < re.length; i++) {
			jtReIm[2 * i] = re[i];
			jtReIm[2 * i + 1] = im[i];
		}
		return jtReIm;
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
	protected void reImToMagPh(float[] reData, float[] imData) {
		float reVal, imVal;

		for (int i = 0; i < reData.length; i++) {
			reVal = reData[i];
			imVal = imData[i];
			try {
				// Put magnitude data in real array
				reData[i] = (float) Math.sqrt(reVal * reVal + imVal * imVal);
				// Put phase data in imaginary array
				imData[i] = (float) Math.atan2((double) imVal, (double) reVal);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Converts a 1D float array to ImageJ stack data format<br>
	 * The float array must have nSlices*width*height elements
	 * 
	 * @param doubleArr a 1D float array
	 * @param nSlices  the number of slices in the stack
	 * @param width    stack image width
	 * @param height   stack image height
	 * @return returns a 2D array of float[slice][sliceData] compatible with
	 *         ImageJ's Stack format
	 */
	public float[][] doubleArrToFloatStack(double[] doubleArr, int nSlices, int width, int height) {
		float[][] stackData = new float[nSlices][width * height];
		for (int slice = 0, index = 0; slice < nSlices; slice++) {
			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					stackData[slice][col + row * width] = (float)doubleArr[index];
					index++;
				}
			}
		}
		return stackData;
	}

	
	/**
	 * converts ImageJ stack Object[] 2D array to double[] 1D array
	 * 
	 * @param stackData use Object[] stackData = imp.getStack().getImageArray()
	 * @param depth     the number of slices in the stack
	 * @return a copy of the input data as double[] 1D array
	 */
	protected double[] stackToDouble(Object[] stackData, int depth) {
		if (stackData[0] == null)
			throw new IllegalArgumentException("Input array must not be null.");
		Class<?> cls = stackData[0].getClass();
		if (!cls.isArray())
			throw new IllegalArgumentException("Input must be an array.");
		
		int i, j, k;
		double[] dblArr = null;
		
		if (stackData[0] instanceof float[]) {
			float[] fArr = (float[]) stackData[0];
			dblArr = new double[depth*fArr.length];	
			for (i = 0,k = 0; i < depth; i++) {
				fArr = (float[]) stackData[i];
				for (j = 0; j < fArr.length; j++) {
					dblArr[k] = fArr[j];
					k++;
				}
			}
		} else if (stackData[0] instanceof int[]) {
			int[] intArr = (int[]) stackData[0];
			dblArr = new double[depth*intArr.length];
			for (i = 0,k=0; i < depth; i++) {
				intArr = (int[]) stackData[i];
				for (j = 0; j < intArr.length; j++) {
					dblArr[k] = Integer.toUnsignedLong(intArr[j]);
					k++;
				}
			}
		} else if (stackData[0] instanceof short[]) {
			short[] shrtArr = (short[]) stackData[0];
			dblArr = new double[depth*shrtArr.length];
			for (i = 0,k=0; i < depth; i++) {
				shrtArr = (short[]) stackData[i];
				for (j = 0; j < shrtArr.length; j++) {
					dblArr[k] = Short.toUnsignedLong(shrtArr[j]);
					k++;
				}
			}
		}

		else if (stackData[0] instanceof byte[]) {
			byte[] byteArr = (byte[]) stackData[0];
			dblArr = new double[depth*byteArr.length];
			for (i = 0,k=0; i < depth; i++) {
				byteArr = (byte[]) stackData[i];
				for (j = 0; j < byteArr.length; j++) {
					dblArr[k] = Byte.toUnsignedLong(byteArr[j]);
					k++;
				}
			}
		} else if (stackData[0] instanceof double[]) {
			double[] doubleSlice = (double[]) stackData[0];
			dblArr = new double[depth*doubleSlice.length];
			for (i = 0,k=0; i < depth; i++) {
				doubleSlice = (double[]) stackData[i];
				for (j = 0; j < doubleSlice.length; j++) {
					dblArr[k] = doubleSlice[j];
					k++;
				}
			}
		} else {
			throw new IllegalArgumentException("Input type not recognised.");
		}
		return dblArr;
	}
	
	/**
	 * Applies a phase shift to a JTransforms Complex array.<br>
	 * Set depth=1 for 2D image. Set depth=1 and height=1 for a 1D image
	 * 
	 * @param jtReIm a 1D array the 2D JTransforms sequence data
	 * @param depth  the number of slices in the 3D data
	 * @param height the number of rows in the 3D data
	 * @param width  the number of columns in the 3D data
	 */
	public void jtReImPhaseShift(double[] jtReIm, int depth, int height, int width) {
		double phase;
		int i, j, k, home;

		for (k = 0; k < depth; k++) {
			for (j = 0; j < height; j++) {
				for (i = 0; i < width; i++) {
					home = 2 * (i + j * width + k * width * height);
					phase = Math.pow(-1.0, (double) (i + j + k));
					jtReIm[home] *= phase;
					jtReIm[home + 1] *= phase;
				}
			}
		}
	}

	
	
//	/**
//	 * Converts a real array to a 1D JTransforms sequenced array with zeros in the
//	 * imaginary part
//	 * 
//	 * @param re3D  The real data from imp.getStack().getImageArray();
//	 * @param im3D  The imaginary data from imp.getStack().getImageArray();
//	 * @param depth the depth of the source images.
//	 * @return A sequenced array in JTransforms format with zeros in the imaginary
//	 *         part
//	 * @throws IllegalArgumentException if Object[] is not an array of float arrays
//	 */
//	private double[] stackReImToJTransformsComplex(Object[] re3D, Object[] im3D, int depth) {
//		double[] jtReIm = null;
//		double[] reData, imData;
//		int offset;
//
//			reData = (double[]) re3D[0];
//			imData = (double[]) im3D[0];
//			int pixelCnt = reData.length;
//			jtReIm = new double[2 * pixelCnt * depth];
//			for (int slice = 0; slice < depth; slice++) {
//				reData = (double[]) re3D[slice];
//				imData = (double[]) im3D[slice];
//				for (int i = 0; i < pixelCnt; i++) {
//					offset = 2 * (i + slice * pixelCnt);
//					jtReIm[offset] = reData[i];
//					jtReIm[offset + 1] = imData[i];
//				}
//			}
//		return jtReIm;
//	}


	

}
