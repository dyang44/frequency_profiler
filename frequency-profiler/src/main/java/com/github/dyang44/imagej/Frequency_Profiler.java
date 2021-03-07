package com.github.dyang44.imagej;

import ij.*;
import ij.ImagePlus;
import ij.gui.*;
import ij.measure.Measurements;
import ij.plugin.*;

import org.jtransforms.fft.*;

public class Frequency_Profiler extends ZAxisProfiler implements PlugIn, Measurements, PlotMaker {
	private static String[] choices = {"time", "z-axis"};
	private static String choice = choices[0];
	private boolean showingDialog;
	private ImagePlus imp;
	private boolean isPlotMaker;
	private boolean timeProfile;
	private boolean firstTime = true;
	private String options;
	
	
	@Override
	public void run(String arg) {
		super.run(arg);
		
		isPlotMaker = true;
		Plot plot = getPlot();
		/**if(plot != null) {
			if(isPlotMaker) {
				plot.setPlotMaker(this);
			}
			plot.show();
		}*/
	}

	public static Plot getPlot(ImagePlus imp) {
		return getPlot(imp, "time");
	}
	
	public static Plot getPlot(ImagePlus imp, String options) {
		Frequency_Profiler fp = new Frequency_Profiler();
		fp.imp = imp;
		fp.options = options;
		fp.isPlotMaker = true;
		Plot plot = fp.getPlot();
		return plot;
	}
	
	public Plot getPlot() {
		float[] y = super.getPlot().getYValues();
		
		double ceil = Math.ceil(Math.log(y.length)/Math.log(2)); // number of elements in y rounded up to the nearest power of 2
		float[] y_transform = new float[(int)Math.pow(2, ceil)*2]; // array that holds 2^n number of elements
		
		for(int i = 0; i < y.length; i++) { // copy y values into array
			y_transform[i] = y[i];
		}
		for(int j = y.length; j < y_transform.length; j++) { // zeroes for additional positions
			y_transform[j] = 0;
		}
		
		FloatFFT_1D fft = new FloatFFT_1D((long)Math.pow(2, ceil));
		
		fft.realForward(y_transform); // perform transform
		
		double[] ymag = new double[(int)Math.pow(2,  ceil)];
		for(int k = 0; k < y_transform.length; k++) {
			double mag = Math.sqrt(Math.pow(y_transform[k],  2) + Math.pow(y_transform[++k],  2)); // magnitude of complex + real components
			ymag[k/2] = mag;
		}
		
		float[] x = new float[ymag.length/2];
		int len = (int) Math.pow(2, ceil);
		for(double i = 0.0; i < x.length; i++) { // store x values
			x[(int)i] = (float)(i/len);
		}
		
		double[] xvals = convertToDouble(x);
		
		
		Plot plot = new Plot("Frequency Profile", "Frequency", "Amplitude", xvals, ymag);
		return plot;
	}

	public static double[] convertToDouble(float[] input) {
		if(input == null) {
			return null;
		}
		double[] output = new double[input.length];
		for(int i = 0; i < input.length; i++) {
			output[i] = input[i];
		}
		return output;
	}

	/*
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 *
	public static void main(String[] args) throws Exception {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		// see: https://stackoverflow.com/a/7060464/1207769
		Class<?> clazz = Frequency_Profiler.class;
		java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
		java.io.File file = new java.io.File(url.toURI());
		System.setProperty("plugins.dir", file.getAbsolutePath());

		// start ImageJ
		new ImageJ();

		// open the Clown sample
		ImagePlus image = IJ.openImage("C:\\Users\\darre\\Downloads\\2021-02-08-12hr-20min-28sec-000001-528nm-frac.tiff");
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}*/
}
