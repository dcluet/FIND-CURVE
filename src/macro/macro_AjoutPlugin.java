macro "AjoutPlugin" {


pathImagej=getDirectory("imagej");
Nameplugin="Animated_Gif.jar";
Resultat = findPlugin(pathImagej, Nameplugin);
//Resultat=0;

if(Resultat==0){

	Dialog.create("Warning Pluging missing");
	Dialog.addMessage("Pluging "+Nameplugin+" is missing");
	Dialog.addMessage("Click Help to display the dowmload page");
	Dialog.addHelp("https://imagej.nih.gov/ij/plugins/gif-stack-writer.html");
	Dialog.show();

}else{
	waitForUser("Plugin "+Nameplugin+" is installed.");
}

//FUNCTIONS________________________________________________________________________________________________________

function findPlugin(logicielpath, PluginName) {

	myres = 0;
	list = getFileList(logicielpath);
	for (i=0; i<list.length; i++) {




       	if (File.isDirectory(logicielpath+list[i])){
           		myres= findPlugin(""+logicielpath+list[i], PluginName);
			if (myres==1){
				i=list.length+1000; //exit
			}
       	}else{
			if(list[i]==PluginName){
				myres=1;
				i=list.length+1000; //exit
			}
	}



	}
	return myres;
}
}
//End of the macro





























}
