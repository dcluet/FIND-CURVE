macro "CloseImages" { 

/*

This macro closes each image window. 2017/03/20
Blandine VERGIER

*/



// array containing the list of image window titles 
ImageList = getList("image.titles");

	// loop for closing each activated image window 
	for(Im=0; Im<lengthOf(ImageList); Im++){
	
	selectWindow(ImageList[Im]);
	close();

	}

} // End Macro CloseImages
