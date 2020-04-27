
/*******************************************************
 * CS4551 Multimedia Software Systems
 * 
 * Spring 2020 Homework #2 Framework
 * 
 * ColorImageDCTCoder.java
 * 
 * By Yi Zhao 04/13/2020
 *******************************************************/

public class ColorImageDCTCoder {
	private int imgWidth, imgHeight; // input image resolution
	private int fullWidth, fullHeight; // full image resolution (multiple of 8)
	private int halfWidth, halfHeight; // half image resolution (Cb/Cr in 420, multiple of 8)
	private int[][] inpR444 = {{0}}, inpG444={{0}}, inpB444={{0}}; // input R/G/B planes
	private int[][] outR444, outG444, outB444; // coded R/G/B planes
	private double[][] inpY444 = {{0}}, inpCb444= {{0}}, inpCr444= {{0}}, inpCb420= {{0}}, inpCr420= {{0}}; // input Y/Cb/Cr planes
	private double[][] outY444, outCb444, outCr444, outCb420, outCr420; // coded Y/Cb/Cr planes
	private int[][] quantY, quantCb, quantCr; // quantized DCT coefficients for Y/Cb/Cr planes
	// TOFIX - add RGB/YCbCr conversion matrix
	private double[][] fwdColorConvMatrix;
	private double[][] invColorConvMatrix;
	// TOFIX - add minimum/maximum DCT coefficient range
	private double[][] YQuantTable = {{4,4,4,8,8,16,16,32},{4,4,8,8,16,16,32,32},{4,8,8,16,16,32,32,32},{8,8,16,16,32,32,32,32},{8,16,16,32,32,32,32,48},{16,16,32,32,32,32,48,48},{16,32,32,32,32,48,48,48},{32,32,32,32,48,48,48,48}};
	private double[][] YQuantTable1 = new double[8][8];
	private double[][] CbCrQuantTable = {{8,8,8,16,16,32,32,64},{8,8,16,16,32,32,64,64},{8,16,16,32,32,64,64,64},{16,16,32,32,64,64,64,64},{16,32,32,64,64,64,64,96},{32,32,64,64,64,64,96,96},{32,64,64,64,64,96,96,96},{64,64,64,64,96,96,96,96}};
	private double[][] CbCrQuantTable1=new double[8][8];
	private double dctCoefMinValue = -1024, dctCoefMaxValue = 1024;

	public ColorImageDCTCoder() {
		
	}

	// conduct DCT-based coding of one image with specified quality parameter
	public int process(String imgName, double n) {
		// open input image from file
		MImage inpImg = new MImage(imgName);
		// allocate work memory space
		int width = inpImg.getW();
		int height = inpImg.getH();
		allocate(width, height);
		// create output image
		MImage outImg = new MImage(width, height);
		// encode image
		encode(inpImg, n);
		// decode image
		decode(outImg, n);
		// write recovered image to files
		String token[] = imgName.split("\\.");
		String outName = token[0] + "-coded-n"+(int)n+".ppm";
		outImg.write2PPM(outName);
		return 0;
	}

	// encode one image
	protected int encode(MImage inpImg, double n) {
		// set work quantization table
		setWorkQuantTable(n);
		// E1. extract R/G/B planes from input image
		extractPlanes(inpImg, inpR444, inpG444, inpB444, imgWidth, imgHeight);
		// E2. RGB -> YCbCr, Cb/Cr 444 -> 420
		convertRGB2YCbCr(inpR444, inpG444, inpB444, inpY444, inpCb444, inpCr444, fullWidth, fullHeight);
		convert444To420(inpCb444, inpCb420, fullWidth, fullHeight);
		convert444To420(inpCr444, inpCr420, fullWidth, fullHeight);
		// E3/4. 8x8-based forward DCT, quantization
		encodePlane(inpY444, quantY, fullWidth, fullHeight, false);
		encodePlane(inpCb420, quantCb, halfWidth, halfHeight, true);
		encodePlane(inpCr420, quantCr, halfWidth, halfHeight, true);
		return 0;
	}

	// decode one image
	protected int decode(MImage outImg, double n) {
		// set work quantization table
		setWorkQuantTable(n);
		// D1/2. 8x8-based dequantization, inverse DCT
		decodePlane(quantY, outY444, fullWidth, fullHeight, false);
		
		decodePlane(quantCb, outCb420, halfWidth, halfHeight, true);
		decodePlane(quantCr, outCr420, halfWidth, halfHeight, true);
		// D3. Cb/Cr 420 -> 444, YCbCr -> RGB
		convert420To444(outCb420, outCb444, fullWidth, fullHeight);
		convert420To444(outCr420, outCr444, fullWidth, fullHeight);
		convertYCbCr2RGB(outY444, outCb444, outCr444, outR444, outG444, outB444, fullWidth, fullHeight);
		// D4. combine R/G/B planes into output image
		combinePlanes(outImg, outR444, outG444, outB444, imgWidth, imgHeight);
		return 0;
	}

	// TOFIX - add code to set up full/half resolutions and allocate memory space
	// used in DCT-based coding
	protected int allocate(int width, int height) {
		
		imgWidth = width;
		imgHeight = height;
		fullWidth=((width/8)+1)*8;
		fullHeight=((height/8)+1)*8;
		halfWidth=((fullWidth/2)/8+1)*8;
		halfHeight=((fullHeight/2)/8+1)*8;		inpR444 = new int[fullWidth][fullHeight];
		inpG444 = new int[fullWidth][fullHeight];
		inpB444 = new int[fullWidth][fullHeight];
	
		inpY444 = new double[fullWidth][fullHeight];
		inpCb444 = new double[fullWidth][fullHeight];
		inpCr444 = new double[fullWidth][fullHeight];
		
		inpCb420 = new double[halfWidth][halfHeight];
		inpCr420 = new double[halfWidth][halfHeight];
		
		quantY = new int[fullWidth][fullHeight];
		quantCb = new int[halfWidth][halfHeight];
		quantCr = new int[halfWidth][halfHeight];
		
		
		outCb420 = new double[halfWidth][halfHeight];
		outCr420 = new double[halfWidth][halfHeight];
		
		outCb444 = new double[fullWidth][fullHeight];
		outCr444 = new double[fullWidth][fullHeight];
		outY444 = new double[fullWidth][fullHeight];
		
		outR444 = new int[fullWidth][fullHeight];
		outG444 = new int[fullWidth][fullHeight];
		outB444 = new int[fullWidth][fullHeight];
		
		return 0;
	}

	// TOFIX - add code to set up work quantization table
	protected void setWorkQuantTable(double n) {
		for(int i = 0 ; i < 8; i ++) {
			for(int j = 0 ; j < 8; j ++) {
				YQuantTable1[i][j] = 0;
				CbCrQuantTable1[i][j]=0;
			}
		}
		for(int i = 0 ; i < 8; i ++) {
			for(int j = 0 ; j < 8; j ++) {
				YQuantTable1[i][j]=YQuantTable[i][j]*Math.pow(2, n);
				CbCrQuantTable1[i][j]=CbCrQuantTable[i][j]*Math.pow(2, n);
			}
		}
	}//which array do i write the quantTable in


	// TOFIX - add code to extract R/G/B planes from MImage
	protected void extractPlanes(MImage inpImg, int R444[][], int G444[][], int B444[][], int width, int height) {
		int rgb[] = new int[3];
		for(int w = 0; w < width; w ++) {
			for(int h = 0; h < height; h++) {
				for(int c = 0; c < 3; c ++) {
					inpImg.getPixel(w, h, rgb);
					if(c == 0) {
						R444[w][h] = rgb[c];
					}
					else if(c == 1) {
						G444[w][h] = rgb[c];
					}
					else if(c == 2) {
						B444[w][h] = rgb[c];
					}	
					
				}
				
			}
		
		}
	
	}//it only assign the original array size
	// how can i initialize 0 in java or it is already initialized to 0

	// TOFIX - add code to combine R/G/B planes to MImage
	protected void combinePlanes(MImage outImg, int R444[][], int G444[][], int B444[][], int width, int height) {
		int[] rgb = new int[3];
		for(int w = 0; w < width; w ++) {
			for(int h = 0; h < height; h++) {
				for(int c = 0; c < 3; c ++) {
					if(c == 0) {
						rgb[c]=R444[w][h];
					}
					else if(c == 1) {
						rgb[c]=G444[w][h];
					}
					else if(c == 2) {
						rgb[c]=B444[w][h];
					}
					
				}
				outImg.setPixel(w, h, rgb);
			}
		}
	}

	// TOFIX - add code to convert RGB to YCbCr
	protected void convertRGB2YCbCr(int R[][], int G[][], int B[][], double Y[][], double Cb[][], double Cr[][],
			int width, int height) {
		MImage outImg = new MImage(width, height);
		int [] rgb = new int[3];
		for(int w = 0; w < width; w ++) {
			for(int h = 0; h < height; h++) {
				if(R[w][h]==0&&G[w][h]==0&&B[w][h]==0){
					Y[w][h] = 0.0;
					Cb[w][h]=0.0;
					Cr[w][h] = 0.0;
				}
				
				Y[w][h] = 0.299*R[w][h]+0.587*G[w][h]+0.114*B[w][h];
				Cb[w][h] = -1*0.1687*R[w][h]-0.3313*G[w][h]+0.5*B[w][h];
				Cr[w][h] = 0.5*R[w][h]-0.4187*G[w][h]-0.0813*B[w][h];
				
				Y[w][h]=clip(Y[w][h],0,255);
				Cb[w][h]=clip(Cb[w][h],-127.5,127.5);
				Cr[w][h]=clip(Cr[w][h],-127.5,127.5);
				
				Y[w][h] = Y[w][h] - 128;
				Cb[w][h] = Cb[w][h] - 0.5;
				Cr[w][h] = Cr[w][h] - 0.5;
				
			}
		}
		
	}

	// TOFIX - add code to convert YCbCr to RGB
	protected void convertYCbCr2RGB(double Y[][], double Cb[][], double Cr[][], int R[][], int G[][], int B[][],
			int width, int height) {

		MImage outImg = new MImage(width, height);
		int [] rgb = new int[3];
		for(int w = 0; w < width; w ++) {
			for(int h = 0; h < height; h++) {
				Y[w][h] = Y[w][h] + 128;
				Cb[w][h] = Cb[w][h] + 0.5;
				Cr[w][h] = Cr[w][h] + 0.5;
				
				R[w][h] = (int)(1*Y[w][h]+1.402*Cr[w][h]);
				G[w][h] = (int)(1*Y[w][h]-0.3441*Cb[w][h]-0.7141*Cr[w][h]);
				B[w][h] = (int)(1*Y[w][h]+1.772*Cb[w][h]);
				R[w][h]=clip(R[w][h],0,255);
				G[w][h]=clip(G[w][h],0,255);
				B[w][h]=clip(B[w][h],0,255);
			}
		}
	}

	// TOFIX - add code to convert chrominance from 444 to 420
	protected void convert444To420(double CbCr444[][], double CbCr420[][], int width, int height) {
		for(int w = 0; w < width; w = w + 2) {
			for(int h = 0; h < height; h=h+2) {
				CbCr420[w/2][h/2]=(CbCr444[w][h]+CbCr444[w][h+1]+CbCr444[w+1][h]+CbCr444[w+1][h+1]+2)/4;
			}
		}
			
			if((width/2)%8!=0&&(height/2)%8==0) {
			for(int i = width/2 ; i < ((width/(2*8))+1)*8; i ++) {
				for(int j = 0; j < height/2; j++) {
					CbCr420[i][j]=0;
				}
			}
		}
		else if((width/2)%8==0&&(height/2)%8!=0) {
			for(int i = 0 ; i < width/2; i ++) {
				for(int j = height/2; j < ((height/(2*8))+1)*8; j++) {
					CbCr420[i][j]=0;
				}
			}
		}
		else if((width/2)%8!=0&&(height/2)%8!=0) {
			for(int i = width/2 ; i < ((width/(2*8))+1)*8; i ++) {
				for(int j = height/2; j < ((height/(2*8))+1)*8; j++) {
					CbCr420[i][j]=0;
				}
			}
		}
	}

	// TOFIX - add code to convert chrominance from 420 to 444
	protected void convert420To444(double CbCr420[][], double CbCr444[][], int width, int height) {
		
		
		for(int i = 0 ; i < width/2; i ++) {
			for(int j = 0; j < height/2 ; j ++) {
			
					CbCr444[2*i][2*j]=CbCr420[i][j];
					CbCr444[2*i+1][2*j]=CbCr420[i][j];
					CbCr444[2*i][2*j+1]=CbCr420[i][j];
					CbCr444[2*i+1][2*j+1]=CbCr420[i][j];
				
			}
		}
	}

	// TOFIX - add code to encode one plane with 8x8 FDCT and quantization
	protected void encodePlane(double plane[][], int quant[][], int width, int height, boolean chroma) {
		double [][] F = new double[width][height];
		double Cu = 0, Cv = 0; 
		for(int count_h = 0; count_h < height; count_h = count_h + 8) {
			for(int count_w = 0; count_w < width; count_w = count_w + 8 ) {
				for(int u = 0; u < 8; u ++) {
					for(int v = 0; v < 8; v ++) {
						F[u][v]=0;
					}
				}
				for(int v = 0; v < 8; v ++) {
					for(int u = 0; u < 8; u ++ ) {
					
							if(u == 0) {
								Cu = (double)1/Math.sqrt(2);
							}
							else {
								Cu = 1.0;
							}
							if(v == 0) {
								Cv = (double)1/Math.sqrt(2);
							}
							else {
								Cv = 1.0;
							}
							for(int h = count_h; h < count_h + 8; h ++) {
								for(int w = count_w ; w < count_w + 8; w ++) {
									F[u][v] += plane[w][h] * Math.cos((2.0*((double)w%8.0)+1.0)*(double)u*Math.PI/16.0)* Math.cos(((2.0*((double)h%8.0)+1.0)*(double)v*Math.PI)/16.0);						
								}
							}
							F[u][v] = Cu*Cv*(1.0/4.0)*F[u][v];
							F[u][v]=clip(F[u][v],dctCoefMinValue,dctCoefMaxValue);
							if(width == fullWidth) {
								quant[count_w+u][count_h+v]=(int)Math.round(F[u][v]/(double)YQuantTable1[u][v]);
							}
							else
								quant[count_w+u][count_h+v]=(int)Math.round(F[u][v]/(double)CbCrQuantTable1[u][v]);
				
						}
					}
				}		
				
						
			
		}
	}

	// TOFIX - add code to decode one plane with 8x8 dequantization and IDCT
	protected void decodePlane(int quant[][], double plane[][], int width, int height, boolean chroma) {
		double [][] F= new double[width][height];
		double Cu = 0, Cv = 0;
	
		for(int h = 0; h < height; h ++) {
			for(int w = 0 ; w < width; w ++) {
				if(width == fullWidth) {
					F[w][h] = (double)quant[w][h] * YQuantTable1[w%8][h%8];
				}
				else {					
					F[w][h] = (double)quant[w][h] * CbCrQuantTable1[w%8][h%8];
				}
				plane[w][h]=0;
			}
		}
		for(int count_h = 0; count_h < height; count_h = count_h +8) {
			for(int count_w = 0; count_w < width; count_w = count_w + 8) {
				for(int h = count_h; h < count_h + 8; h ++) {
					for(int w = count_w ; w < count_w + 8; w ++) {
						for(int v = 0; v < 8; v ++) {
							for(int u = 0; u < 8; u ++ ) {
						
								if(u == 0) {
									Cu = (double)1/Math.sqrt(2);
								}
								else {
									Cu = 1.0;
								}
								if(v == 0) {
									Cv = (double)1/Math.sqrt(2);
								}
								else {
									Cv = 1.0;
								}
								plane[w][h] += ( Cu * Cv * (double)F[count_w+u][count_h+v] * Math.cos(((2.0*((double)w%8.0)+1.0)*u*Math.PI)/(16.0))* Math.cos(((2.0*((double)h%8.0)+1.0)*v*Math.PI)/(16.0)));
							
							}
						}
						plane[w][h] = plane[w][h]*(1.0/4.0);
					}
				}
			}
		}
	}

	// clip one integer
	protected int clip(int x, int a, int b) {
		if (x < a)
			return a;
		else if (x > b)
			return b;
		else
			return x;
	}

	// clip one double
	protected double clip(double x, double a, double b) {
		if (x < a)
			return a;
		else if (x > b)
			return b;
		else
			return x;
	}
}
