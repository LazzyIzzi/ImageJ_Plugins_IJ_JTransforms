package IJ_JTransforms;

/**
 * Class for managing Fourier operations Name Tags.<br>
 * A name tag inserts "#Descriptor" between the imageName and the file
 * extension.<br>
 * 
 * @author LazzyIzzi
 */
public class NameTagUtils {

	/** Tags for plugins in FFT_Stuff */
	public class NameTagsFT {
		/** The tag for the real or magnitude image */
		String reTag;
		/** The tag for the imaginary or phase image */
		String imTag;
	}

	/**
	 * @param forward   pass true if forward FT, false if inverse
	 * @param centered  pass true if centered FT, false if edge format
	 * @param magPhase  pass true if magPhase FT, false if Real-imaginary
	 * @param fftChoice "Current Slice 2D", "All Slices 2D", "All Slices 3D"
	 * @param curSlice  the slice shown in the selected stack
	 * @return A list of tags that describe the process used to create the FT
	 */
	public NameTagsFT makeNameTagsFT(boolean forward, boolean centered, boolean magPhase, String fftChoice,
			int curSlice) {
		NameTagsFT nt = makeNameTagsFT(forward, centered, magPhase, fftChoice);
		nt.reTag += curSlice;
		nt.imTag += curSlice;
		return nt;
	}

	/**
	 * @param forward   pass true if forward FT, false if inverse
	 * @param centered  pass true if centered FT, false if edge format
	 * @param magPhase  pass true if magPhase FT, false if Real-imaginary
	 * @param fftChoice "Current Slice 2D", "All Slices 2D", "All Slices 3D"
	 * @return A list of tags that describe the process used to create the FT
	 */
	public NameTagsFT makeNameTagsFT(boolean forward, boolean centered, boolean magPhase, String fftChoice) {
		NameTagsFT nt = new NameTagsFT();
		if (forward) {
			nt.reTag = "Fwd";
			nt.imTag = "Fwd";
		} else {
			nt.reTag = "Inv";
			nt.imTag = "Inv";
		}
		if (centered) {
			nt.reTag = nt.reTag + "Ctr";
			nt.imTag = nt.imTag + "Ctr";
		}
		if (magPhase) {
			nt.reTag = nt.reTag + "Mg";
			nt.imTag = nt.imTag + "Ph";
		} else {
			nt.reTag = nt.reTag + "Re";
			nt.imTag = nt.imTag + "Im";
		}
		switch (fftChoice) {
		case "Current Slice 2D":
		case "All Slices 2D":
			nt.reTag = nt.reTag + "2D";
			nt.imTag = nt.imTag + "2D";
			break;
		case "All Slices 3D":
			nt.reTag = nt.reTag + "3D";
			nt.imTag = nt.imTag + "3D";
			break;
		}
		return nt;
	}

	/**
	 * Removes a tag from a string, does nothing if no tag is present
	 * 
	 * @param title usually an image title, but any string really.
	 * @return the string with the tag removed.
	 */
	public String removeTag(String title) {
		String baseName;
		String ext;
		int tagLoc = title.lastIndexOf('#');
		int dotLoc = title.lastIndexOf('.');
		// image name has a tag and extension
		if (tagLoc > -1 && dotLoc > -1) {
			ext = title.substring(dotLoc);
			baseName = title.substring(0, tagLoc);
			title = baseName + ext;
		}
		// image name has a tag and no extension
		else if (tagLoc > -1 && dotLoc < 0) {
			baseName = title.substring(0, tagLoc);
			title = baseName;
		}
		return title;
	}

	/**
	 * Adds a tag to a string
	 * 
	 * @param title usually an image title, but any string really.
	 * @return the string with the tag added to the end of the string and before any
	 *         file extension.
	 */
	public String addTag(String title, String tag) {
		String baseName;
		String ext;
		int dotLoc = title.lastIndexOf('.');
		// image name has no tag and has extension
		if (dotLoc > -1) {
			ext = title.substring(dotLoc);
			baseName = title.substring(0, dotLoc);
			title = baseName + "#" + tag + ext;
		}
		// image name has no tag and no extension
		else if (dotLoc < 0) {
			title = title + "#" + tag;
		}
		return title;
	}

	/**
	 * Replaces an existing tag with a new one
	 * 
	 * @param title usually an image title, but any string really.
	 * @param tag   the new tag
	 * @return title with the new tag
	 */
	public String replaceTag(String title, String tag) {
		removeTag(title);
		addTag(title, tag);
		return title;
	}
}
