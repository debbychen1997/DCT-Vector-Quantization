
/*******************************************************
 * CS4551 Multimedia Software Systems
 * 
 * Spring 2020 Homework #2 Framework
 * 
 * ColorImageVectorQuantizer.java
 * 
 * By Yi Zhao 04/13/2020
 *******************************************************/

public class ColorImageVectorQuantizer {
	private int imgWidth, imgHeight; // image resolution
	private int blkWidth, blkHeight; // block resolution
	private int numBlock; // number of blocks in image
	private int numDimension; // number of vector dimension in VQ
	private int numCluster;// number of clusters in VQ
	private int maxIteration; // maximum number of iteration in VQ training
	private int[][] codeBook; // codebook in VQ
	private int inputVectors[][]; // vectors from input image
	private int quantVectors[][]; // vectors for quantized image
	private int quantIndices[]; // quantized indices for blocks
	private int array[]=new int[numBlock];
	private int prev[]=new int[numBlock];
	public ColorImageVectorQuantizer() {
		blkWidth = 2;
		blkHeight = 2;
		numDimension = blkWidth * blkHeight * 3;
		numCluster = 256;
		maxIteration = 100;
		numBlock=256;//not sure
	}

	public int process(String inputName) {
		// read 24-bit color image from PPM file
		MImage inputImage = new MImage(inputName);
		System.out.println(inputImage);
		String token[] = inputName.split("\\.");
		// set up workspace
		int width = inputImage.getW();
		int height = inputImage.getH();
		allocate(width, height);
		// form vector from input image
		image2Vectors(inputImage, inputVectors, imgWidth, imgHeight);
		// train vector quantizer
		train(inputVectors, numBlock);
		// display trained codebook
		display();
		// quantize input image vectors to indices
		quantize(inputVectors, numBlock, quantIndices);
		// TOFIX - add code to save indices as PPM file
		MImage indicesImage = new MImage(imgWidth, imgHeight);
		indices2Image(quantIndices, indicesImage, imgWidth, imgHeight);
		String indicesName = token[0] + "-indices.ppm";
		indicesImage.write2PPM(indicesName);
		// dequantize indices back to vectors
		dequantize(quantIndices, numBlock, quantVectors);
		
		// write quantized image to file
		MImage quantImage = new MImage(imgWidth, imgHeight);
		vectors2Image(quantVectors, quantImage, width, height);
		String quantName = token[0] + "-quant.ppm";
		quantImage.write2PPM(quantName);
		return 0;
	}
	
	// TOFIX - add code to set up work space
	protected int allocate(int width, int height) {
		numBlock = ((width+1)/2)*((height+1)/2);
		//why add 1 on width and height?
		inputVectors = new int[numBlock][numDimension];
		codeBook = new int[numCluster][numDimension];
		quantVectors = new int[numBlock][numDimension];
		
		array=new int[numBlock];
		prev=new int[numBlock];
		
		quantIndices = new int[numBlock];
	//	quantImages = new int[width][height];
		imgWidth = width;
		imgHeight = height;
		return 0;
	}

	// TOFIX - add code to convert one image to vectors in VQ
	protected void image2Vectors(MImage image, int vectors[][], int width, int height) {
		int[] rgb = new int[3];
		int counter = 0;//block counter
		int pixel_counter = 0;//0~12 dimensions
		for(int h = 0; h < height; h = h + 2) {
			for(int w = 0; w < width; w = w + 2) {
				image.getPixel(w, h, rgb);
				for(int c = 0; c < 3; c ++) {
					vectors[counter][pixel_counter%12] = rgb[c];
					pixel_counter ++;
				}
				image.getPixel(w+1, h, rgb);
				for(int c = 0; c < 3; c ++) {
					vectors[counter][pixel_counter%12] = rgb[c];
					pixel_counter ++;
				}
				image.getPixel(w, h+1, rgb);
				for(int c = 0; c < 3; c ++) {
					vectors[counter][pixel_counter%12] = rgb[c];
					pixel_counter ++;
				}
				image.getPixel(w+1, h+1, rgb);
				for(int c = 0; c < 3; c ++) {
					vectors[counter][pixel_counter%12] = rgb[c];
					pixel_counter ++;
				}
				counter++;
				
				
				}
			}
		}
	

	// TOFIX - add code to convert vectors to one image in VQ
	protected void vectors2Image(int vectors[][], MImage image, int width, int height) {
		int [] rgb = new int [3];
		int j = 0;
		int i = 0;
		
		
			j = 0;
			for(int h = 0; h < height; h = h + 2) {
				for(int w = 0; w < width; w = w + 2) {	
					for(int c = 0; c < 3; c ++) {
						rgb[c] = vectors[i][j];
						j++;
					}
					image.setPixel(w, h, rgb);
					for(int c = 0; c < 3; c ++) {
						rgb[c] = vectors[i][j];
						j++;
					}
					image.setPixel(w+1, h, rgb);
					for(int c = 0; c < 3; c ++) {
						rgb[c] = vectors[i][j];
						j++;
					}
					image.setPixel(w, h+1, rgb);
					for(int c = 0; c < 3; c ++) {
						rgb[c] = vectors[i][j];
						j++;
					}
					image.setPixel(w+1, h+1, rgb);
					j = 0;
					i++;
				}
			
			
		}image.write2PPM("Vector.ppm");
	}

	// TOFIX - add code to convert indices to one image in VQ
	protected void indices2Image(int indices[], MImage image, int width, int height) {
		int [] rgb = new int [3];
		int i = 0;
	
			for(int h = 0; h < height; h = h + 2) {
				for(int w = 0; w < width; w = w + 2) {
					for(int c = 0; c < 3; c ++) {
						rgb[c] = indices[i];
					}
					i++;
					
					image.setPixel(w, h, rgb);
				}
			}
		
	}
	protected boolean checkZero(int vectors[]) {
		int count_zero = 0;
		for(int i = 0; i < 12; i ++) {
			
			if(vectors[i]==0) {
				count_zero ++;
			}
		}
		if(count_zero == 12)
			return true;
		else 
			return false;
	}
	protected boolean checkSame(int vectors[], int vectors1[]) {
		int count_zero = 0;
		for(int i = 0; i < 12; i ++) {
			if(vectors[i]==vectors1[i]) {
				count_zero ++;
			}
		}
		if(count_zero == 12)
			return true;
		else 
			return false;
	}
	// TOFIX - add code to train codebook with K-means clustering algorithm
	protected void train(int vectors[][], int count) {
		for( int i = 0; i < numCluster;i ++) {
				int random = (int)(Math.random()*numBlock);
				while(checkZero(inputVectors[random])) {
					random = (int)(Math.random()*numBlock);
				}
				for(int k = 0; k < i; k ++) {
					while(checkSame(codeBook[k],inputVectors[random])){
						random = (int)(Math.random()*numBlock);	
					}
				}
				for(int j = 0; j < numDimension; j ++) {
					
					codeBook[i][j] = inputVectors[random][j];
				}
			}
		for(int round = 0; round < 100; round ++) {
			//random select numCluster's vectors as the codeBook vectors
			int sum = 0;
			int dist = 0;
			
			int min = 0;
			for(int i = 0; i < numBlock; i ++) {
				
				for(int c = 0; c < numCluster; c ++) {
					sum = 0;
					for(int j = 0; j < numDimension; j ++) {
						sum += (inputVectors[i][j]-codeBook[c][j])*(inputVectors[i][j]-codeBook[c][j]);
					}	
					dist = (int)Math.sqrt(sum);
					//compute the distance between vector and codeBookvectors
					if(c == 0||dist<min) {
						min = dist;
						array[i]=c;
						if(round == 0) {
							prev[i]=array[i];
						}
						
					}//save the initial codeword and distance
					
				}
			}
			//see if the centroid change after training
			int count_same = 0;
			for(int i = 0; i < numBlock; i ++) {
				if(prev[i]==array[i]&&round!=0) {
					count_same ++;
				}	
				prev[i]=array[i];
			}
			//count if the codeword change
			
			if(round != 0 && count_same == 0) {
				break;	
			}//if the codeword does not change anymore, stop the for loop
			int counter = 0;//how many vector are add into the codebook
		
			for(int c = 0; c < numCluster; c ++) {
				for(int j = 0; j < numDimension; j ++) {
					codeBook[c][j]=0;
				}//initialize the codebook
				counter = 0;
				for(int i = 0; i < numBlock; i ++) {
					if(array[i]==c) {//if the dist of this vector and the centroid is min
						for(int j = 0; j < numDimension; j ++) {
							codeBook[c][j]+=inputVectors[i][j];				
						}//add it to the codeword vector 
						counter++;
					}
				}
				for(int j = 0; j < numDimension; j ++) {
					if(counter!=0)
						codeBook[c][j]=codeBook[c][j]/counter;
					//calculate centroid
				}
			}
		}
			//need to check whether the codebook vector will change after training
			//maybe I need to save a previous array and see if they change or not
			//if they change I add 1, and I assign array to prev afterwards
			//is numBlock different than 256?
			//if this is the case, than vector has (numblock, dimensional)
		
	}

	// TOFIX - add code to display codebook
	protected void display() {
		for(int c = 0; c < numCluster; c ++) {
			for(int j = 0; j < numDimension; j ++) {
				System.out.printf("%d ",codeBook[c][j]);
			}
			System.out.printf("\n");
		}
	}

	// TOFIX - add code to quantize vectors to indices
	//what does quantization do?
	//according to codebook, we out figure which codebook vectors vectors belong to
	//but does the array[i] means indices??
	protected void quantize(int vectors[][], int count, int indices[]) {
		
		double dist = 0;
		double min = 0;
		for(int i = 0 ; i < numBlock; i ++) {
			for(int c = 0 ; c < numCluster; c ++) {
				int sum = 0;
				
				for(int j = 0 ; j < numDimension; j ++) {
					sum += (vectors[i][j]-codeBook[c][j])*(vectors[i][j]-codeBook[c][j]);
				}
				dist = Math.sqrt(sum);
				if(c == 0||dist<min) {
					min = dist;
					indices[i]=c;
				}
				
			}
		}
		
	}

	// TOFIX - add code to dequantize indices to vectors
	protected void dequantize(int indices[], int count, int vectors[][]) {
		for(int i = 0 ; i < numBlock; i ++) {
			for( int c = 0 ; c < numCluster; c ++) {
				if(c == indices[i]) {
					for(int j = 0; j < numDimension; j ++) {
						vectors[i][j]=codeBook[c][j];
					}
					
				}
			}
		}
	}
}
