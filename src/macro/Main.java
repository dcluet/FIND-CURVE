macro "Main"{

	version = "1.0a 2017/03/28";

	requires("1.49t");
	setBatchMode(true);

	//Closing all images
	runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"CloseImages.java");

	//Ask where images are located
	PathInput = getDirectory("PLEASE CHOOSE THE FOLDER CONTAINING THE CELLS TO PROCESS");

	//Ask where to store the results
	PathOutput = getDirectory("PLEASE CHOOSE THE FOLDER TO STORE THE RESULTS OF THE ANALYSIS");

	//Create the listing file
	Temp = File.open(PathOutput+File.separator()+"ListFiles.txt");
	File.close(Temp);

	//Find all tif images
	Arg1 = ""+PathInput+"\t"+".tif"+"\t"+0+"\t"+PathOutput;
	runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"Explorer.java", Arg1);

	//Retrieve all Found Images
	ListImage = File.openAsString(PathOutput+File.separator()+"ListFiles.txt");
	ListImage = split(ListImage,"\n");

	Dialog.create("Find-Curve");
	Dialog.addMessage("The program will process all images present in the folder:");
	Dialog.addString("Path: ", PathInput, 75)
	Dialog.addMessage(""+"\n"+ lengthOf(ListImage) +" IMAGES HAVE BEEN IDENTIFIED" +"\n");
	Dialog.addMessage("The results will be stored in the folder:");
	Dialog.addString("Path: ", PathOutput, 75)
	Dialog.show();

	//Loop of analysis
	for (i=0; i<lengthOf(ListImage); i++){
		setBatchMode(true);

		//Create the orthogonal views
		open(ListImage[i]);

		myListeOrtho = newArray(3);



		T = getTitle;
		TnoExt = File.nameWithoutExtension();

		myListeOrtho[0] = PathOutput+File.separator()+TnoExt+"_Z.tif";
		myListeOrtho[1] = PathOutput+File.separator()+TnoExt+"_X.tif";
		myListeOrtho[2] = PathOutput+File.separator()+TnoExt+"_Y.tif";

		getPixelSize(unit, pixelWidth, pixelHeight);
		selectWindow(T);
		run("Reslice [/]...", "output="+pixelWidth+" start=Right avoid");
		saveAs("Tiff", myListeOrtho[1]);
		close();

		selectWindow(T);
		run("Reslice [/]...", "output="+pixelWidth+" start=Bottom avoid");
		saveAs("Tiff", myListeOrtho[2]);
		close();

		selectWindow(T);
		saveAs("Tiff", myListeOrtho[0]);
		close();
		for(ort = 0; ort<lengthOf(myListeOrtho); ort++){

			Arg2 = ""+myListeOrtho[ort]+"\t"+PathOutput;
			//Treating the image
			runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"Find_Curve.java", Arg2);
			//Delete the image (spare memory)
			D = File.delete(myListeOrtho[ort]);
            //Cleaning the memory
            runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"CleanMemory.java");

		}
		//Cleaning the memory
		runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"CleanMemory.java");


	}
	waitForUser("ANALYSIS IS OVER");
}//END OF MACRO
