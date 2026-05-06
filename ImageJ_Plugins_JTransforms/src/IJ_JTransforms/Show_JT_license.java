package IJ_JTransforms;

import java.awt.Color;
import java.awt.Font;

import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Show_JT_license implements PlugIn {
	final Color myColor = new Color(240,230,190);//slightly darker than buff
	Font myFont = new Font(Font.DIALOG, Font.BOLD, 12);
	
	@Override
	public void run(String arg) {
		//String license = jtu.getJTransformsLicense();
		String license  = "JTransforms FFT library\r\n\r\n" + "JTransforms\r\n"
				+ "Copyright (c) 2007 onward, Piotr Wendykier\r\n" + "All rights reserved.\r\n" + "\r\n"
				+ "Redistribution and use in source and binary forms, with or without\r\n"
				+ "modification, are permitted provided that the following conditions are met:\r\n" + "\r\n"
				+ "1. Redistributions of source code must retain the above copyright notice, this\r\n"
				+ "   list of conditions and the following disclaimer. \r\n"
				+ "2. Redistributions in binary form must reproduce the above copyright notice,\r\n"
				+ "   this list of conditions and the following disclaimer in the documentation\r\n"
				+ "   and/or other materials provided with the distribution.\r\n" + "\r\n"
				+ "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND\r\n"
				+ "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED\r\n"
				+ "WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\r\n"
				+ "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR\r\n"
				+ "ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES\r\n"
				+ "(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;\r\n"
				+ "LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND\r\n"
				+ "ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\r\n"
				+ "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\r\n"
				+ "SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
		GenericDialog gd = new GenericDialog("JTransforms License");
		gd.addMessage(license,myFont);
		gd.setBackground(myColor);
		gd.hideCancelButton();
		gd.showDialog();
	}
}
