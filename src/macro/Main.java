macro "Main"{

    //INFOS
    tag = "v1.0.0"
    lastStableCommit = "11677e4a"
    gitlaburl = "http://gitlab.biologie.ens-lyon.fr/dcluet/Find_Curve"

    //Welcome
    Welcome(tag, lastStableCommit, gitlaburl);

	requires("1.49t");

	//Closing all images
	runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"CloseImages.java");

    //Main interface
    choices = newArray("Automated file detection",
                        "Manual processing");

    Dialog.create("Find Curve");
    Dialog.addChoice("Analysis mode:", choices);
    Dialog.addMessage("Indicate which axis you want to process:");
    Dialog.addCheckbox("X axis", 0);
    Dialog.addCheckbox("Y axis", 0);
    Dialog.addCheckbox("Z axis", 1);
    Dialog.show();

    mode = Dialog.getChoice();
    myListeAxis = newArray(3);
    myListeAxis[0] = Dialog.getCheckbox();
    myListeAxis[1] = Dialog.getCheckbox();
    myListeAxis[2] = Dialog.getCheckbox();


	//Ask where images are located
	PathInput = getDirectory("PLEASE CHOOSE THE FOLDER CONTAINING THE CELLS TO PROCESS");
	//Ask where to store the results
	PathOutput = getDirectory("PLEASE CHOOSE THE FOLDER TO STORE THE RESULTS OF THE ANALYSIS");

    //Create the listing file
	Temp = File.open(PathOutput+File.separator()+"ListFiles.txt");
	File.close(Temp);

    if (mode=="Manual processing"){
        PathSF = File.openDialog("Choose File to process:");
        File.append(PathSF, PathOutput+File.separator()+"ListFiles.txt");

    }else{
        //Find all tif images
    	Arg1 = ""+PathInput+"\t"+".tif"+"\t"+0+"\t"+PathOutput;
    	runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"Explorer.java", Arg1);
    }

	//Retrieve all Found Images
	ListImage = File.openAsString(PathOutput+File.separator()+"ListFiles.txt");
	ListImage = split(ListImage,"\n");

    myMessage = "<p>The program will process all images present in the folder:<br>";
    myMessage += "" + PathInput + "<br>";
    myMessage += "<b>" + lengthOf(ListImage) +" images </b> have been identified." +"<br>";
    myMessage += "The results will be stored in the folder:" +"<br>";
    myMessage += "" + PathOutput + "<br></p>";
    DisplayInfo(myMessage);

	//Loop of analysis
	for (i=0; i<lengthOf(ListImage); i++){
		setBatchMode(true);
        BestSlice = newArray(-1,-1,-1);

		//Create the orthogonal views
		open(ListImage[i]);

		myListeOrtho = newArray(3);


		T = getTitle;
		TnoExt = File.nameWithoutExtension();

		myListeOrtho[0] = PathOutput+File.separator()+TnoExt+"_X.tif";
		myListeOrtho[1] = PathOutput+File.separator()+TnoExt+"_Y.tif";
		myListeOrtho[2] = PathOutput+File.separator()+TnoExt+"_Z.tif";

		getPixelSize(unit, pixelWidth, pixelHeight);


        if (myListeAxis[0]==1){
            selectWindow(T);
    		run("Reslice [/]...", "output="+pixelWidth+" start=Right avoid");
    		saveAs("Tiff", myListeOrtho[0]);

            if (mode=="Manual processing"){
                setBatchMode("show");
                waitForUser("X Axis\nSelect the best slice");
                BestSlice[0] = getSliceNumber();
                setBatchMode("hide");
            }
    		close();
        }

        if (myListeAxis[1]==1){
            selectWindow(T);
    		run("Reslice [/]...", "output="+pixelWidth+" start=Bottom avoid");
    		saveAs("Tiff", myListeOrtho[1]);

            if (mode=="Manual processing"){
                setBatchMode("show");
                waitForUser("Y Axis\nSelect the best slice");
                BestSlice[1] = getSliceNumber();
                setBatchMode("hide");
            }
    		close();
        }

		selectWindow(T);

        if ((mode=="Manual processing") && (myListeAxis[2]==1)){
            setBatchMode("show");
            waitForUser("Z Axis\nSelect the best slice");
            BestSlice[2] = getSliceNumber();
            setBatchMode("hide");
        }

		saveAs("Tiff", myListeOrtho[2]);
		close();
		for(ort = 0; ort<lengthOf(myListeOrtho); ort++){

            if (myListeAxis[ort]==1){
                Arg2 = ""+myListeOrtho[ort]+"\t"+PathOutput;
                Arg2 += "\t" + BestSlice[ort];

                //Treating the image
                runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"Find_Curve.java", Arg2);
                //Delete the image (spare memory)
                D = File.delete(myListeOrtho[ort]);
                //Cleaning the memory
                runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"CleanMemory.java");
            }
		}
		//Cleaning the memory
		runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"CleanMemory.java");


	}
	myMessage = "<b>Analysis is over!</b>";
    DisplayInfo(myMessage);

/*
================================================================================
*/

function Welcome(myTag, myCommit, url){
    showMessage("WELCOME", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Find Curve Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
			+"<ul>"
			+"<li>Version: " + myTag + "</li>"
			+"<li>Last stable commit: " + myCommit + "</li>"
			+"</ul>"
			+"<p><font color=rgb(100,100,100)>Cluet David<br>"
            +"Research Ingeneer,PHD<br>"
            +"<font color=rgb(77,172,174)>CNRS, ENS-Lyon, LBMC</p>"
			);
}//END WELCOME

/*
================================================================================
*/

function DisplayInfo(Message){
    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Find Curve Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>" + Message + "</p>"
			);
}//END DisplayInfo

}//END OF MACRO
