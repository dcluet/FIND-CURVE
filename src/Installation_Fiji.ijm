macro "Installation"{

version = "1.0b 2017/04/25";


//IJ version verification and close the macro's window
//selectWindow("Installation.ijm");
//run("Close");
requires("1.49g");

//Initialisation of the error counter
Errors=0;

//GUI Message
Dialog.create("Installation wizard for the FIND-CURVE macro");
Dialog.addMessage("Version\n" + version);
Dialog.addMessage("Cluet David\nResearch Ingeneer,PHD\nCNRS, ENS-Lyon, LBMC");
Dialog.addMessage("This program will install the FIND-CURVE macro.\nShortcuts will be added in the Plugins/Macros menu.");
Dialog.show();

//Prepare key paths
PathSUM = getDirectory("macros")+File.separator+"StartupMacros.fiji.ijm";
PathFolderInput =File.directory+File.separator+"Files"+File.separator;
PathOutput = getDirectory("macros")+"Find-Curve"+File.separator;

//Listing of the files to instal
Listing = newArray("Main.java",
                    "Find_Curve.java",
                    "Trigo.tif",
                    "ROIeraser.java",
                    "CleanMemory.java",
                    "CloseImages.java",
                    "HTML_Curve.html",
                    "style_Curve.css",
                    "UCBL.jpg",
                    "LBMC.jpg",
                    "CNRS.jpg",
                    "ENS.jpg",
                    "tableline.html",
                    "Explorer.java",
                    "macro_AjoutPlugin.java" );

//Create the installation folder if required
if(File.exists(PathOutput)==0){
File.makeDirectory(getDirectory("macros")+File.separator+"Find-Curve");
}

//Installation of all files of the listing
for(i=0; i<lengthOf(Listing); i++){
	if(File.exists(PathFolderInput+Listing[i])==0){
	waitForUser(""+Listing[i]+" file is missing");
	Errors = Errors + 1;
	}else{
		if(Listing[i]!="Settings.txt"){
			Transfer=File.copy(PathFolderInput+Listing[i], PathOutput+Listing[i]);
		}else{
			if(File.exists(PathOutput+"Settings.txt")==1){
				waitForUser("Your current settings have been preserved!");
			}else{
				Transfer=File.copy(PathFolderInput+"Settings.txt", PathOutput+"Settings.txt");
			}

	}
}

}

//Create the shortcut in IJ macro menu for the first installation
PCommandLine = PathFolderInput+ "Startup_CL.txt";
SUM = File.openAsString(PathSUM);
pos =lastIndexOf(SUM, "//End_Find-Curve");
if(pos == -1){
	SUM = SUM + "\n\n" + File.openAsString(PCommandLine);
	Startup = File.open(PathSUM);
	print(Startup, SUM);
	File.close(Startup);
}

//The program prompts the user of the success or failure of the installation.
if(Errors == 0){
waitForUser("Installation has been performed sucessfully!\nRestart your ImageJ program.");
} else {
waitForUser("Files were missing!\nInstallation is incomplete.");
}

//Find plugin
runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"macro_AjoutPlugin.ijm");

}
