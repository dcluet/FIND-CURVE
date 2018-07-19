macro "Find_Curve" {
	setBatchMode(true);
	Argument = getArgument();
	Arguments = split(Argument,"\t");
	//_______________________________________________
	Path = Arguments[0];		//Path of the image to process
	PathOutput = Arguments[1];	//Path of the folder to store the results
    BestSlice = parseFloat(Arguments[2]);   //Manual slice identified

	version = "Find-Curve 2018/04/09";		//Version of the macro
	WHTML=400;			//width of the low-resolution images diplayed on the html
	unit = "pix";			//Defaul unit for distance calculation
	NZ = 10;			//Value of the numeric zoom
	t=10;				//space between points to calculate angle
	lissage=5;			//smotthing window
	SliceN = -1;			//Default value for bestslice
	minTol=2;			//Minimum tolerance to identify summit
	maxTol=100;			//Maximum tolerance to identify summit
	PrecisionVoulue= 10;		//Precision in % compared to the polygon model
	TresAngleNeg = 200;
	//_______________________________________________

    /*
        GO TO LINE 241 TO HARDCODED SIZE DEPENDANT ANGLE DEFINITION
    */


	T1 = getTime();

	//Opening Trigo to have at least an image open for setting the parameters (linewidth...)
	open(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"Trigo.tif");
	rename("Trigo");

	//Setting linewidth for direct draw or run("Draw")
	setLineWidth(1);
	run("Line Width...", "line=1");

	//Cleaning of the ROIManager before anycalculation/image manipulation
	CleanManager();

	//Open the Stack
	open(Path);
	setLocation(0, 0);

	//Harvest properties
	getPixelSize(unit, pixelWidth, pixelHeight);
	T = getTitle();
	N = nSlices();
	W = getWidth();
	H = getHeight();

	//Resetting pixel value to pixel unit to avoid incompatibility of micron with some IJ functions
	run("Properties...", "channels=1 slices="+N+" frames=1 unit=pixel pixel_width=1 pixel_height=1 voxel_depth=1");

	//Create the folder that will contain the images
	myLocalName = File.nameWithoutExtension();
	PathStorage = PathOutput+myLocalName;
	Temp = File.makeDirectory(PathStorage);

	//Transfert the images (LBMC.... css) to create a viable HTML
	Listing = newArray("style_Curve.css","UCBL.jpg", "LBMC.jpg", "CNRS.jpg", "ENS.jpg");
	for(f=0; f<lengthOf(Listing); f++){
		Transfer=File.copy(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+Listing[f], PathStorage+File.separator()+Listing[f]);
	}

	//Create the Z PROJECTION image of the stack
	run("Z Project...", "projection=[Average Intensity]");
	rename("Projection");
	//Giving color
	run("Cyan Hot");
	//Saving High Resolution
	SaveNative("Projection", WHTML, PathStorage+File.separator()+"Projection_Cell.jpg");
	//Saving Low Resolution
	SaveLowRes("Projection_Cell.jpg", WHTML, PathStorage+File.separator()+"Projection_Cell_Low-Res.jpg");

	if (endsWith(T, "Z.tif")){
		Zspace = pixelWidth;
		myAxis = "Z";


	}else if (endsWith(T, "X.tif")){
		Zspace = 1;
		myAxis = "X";

	}else if (endsWith(T, "Y.tif")){
		Zspace = 1;
		myAxis = "Y";
	}

	//Create the 3D RECONSTRUCTION animation
	selectWindow(T);
	run("3D Project...", "projection=[Nearest Point] axis=Z-Axis slice="+Zspace+" initial=0 total=360 rotation=5 lower=1 upper=255 opacity=0 surface=100 interior=50 interpolate");
	//Giving color
	run("Cyan Hot");
	//Saving High Resolution gif
	run("Animated Gif... ", "save=["+PathStorage+File.separator()+"Animated_Cell.gif]");


	Wa= getWidth();
	Ha= getHeight();
	ratioScale = WHTML/(Wa);
	Hreduction = Ha*ratioScale;
	makeRectangle(0,0,Wa, Ha);
	//Saving Low Resolution gif
	run("Scale...", "x=- y=- width="+WHTML+" height="+Hreduction+" interpolation=Bilinear average process create title=Low-Res");
	run("Animated Gif... ", "save=["+PathStorage+File.separator()+"Animated_Cell_Low-Res.gif]");
	close();

	//Initialisation
	if (BestSlice == -1){
        Surface = 0;

    	//Searching for optimale (biggest surface) slice
    	for(S=1; S<=N; S++){

    		selectWindow(T);
    		setSlice(S);
    		makeRectangle(0,0, W, H);

    		//Set the Threshold
    		setAutoThreshold("Default");

    		//Find the cell
    		run("Analyze Particles...", "size=1-Infinity add slice");

    		//Research the biggest ROI on the slice
    		SurfaceLocale = 0;
    		Index = -1;

    		//Security if we have several hits
    		for(roi = 0; roi<roiManager("count"); roi++){
    			//select the ROI of roi index
    			roiManager("Select", roi);
                //Measure the area
    			List.setMeasurements;
    			A = List.getValue("Area");

    			//If surface>SurfaceLocale
    			if(A>SurfaceLocale){
    				SurfaceLocale = A;
    				Index = roi;
    			}
    		}

    		//Measure the surface and transfer the value into variable Surface if bigger
    		if (SurfaceLocale>Surface){
    			Surface = SurfaceLocale;
    			BestSlice = S;
    			roiManager("Select", Index);
    			//Harvest the coordinates of the perimeter peaks
    			Roi.getCoordinates(Xcoords, Ycoords);
    			getStatistics(areaGM, meanGM, minGM, maxGM, stdGM, histogramGM);
    		}
    		//Cleaning of the ROIManager
    		CleanManager();
    	}//End of the loop to identify the optimal slice

    }else{
        //Upstream manual identification
        selectWindow(T);
        setSlice(BestSlice);
        makeRectangle(0,0, W, H);

        //Set the Threshold
        setAutoThreshold("Default");

        //Find the cell
        run("Analyze Particles...", "size=1-Infinity add slice");

        //Research the biggest ROI on the slice
        SurfaceLocale = 0;
        Index = -1;

        //Security if we have several hits
        for(roi = 0; roi<roiManager("count"); roi++){
            //select the ROI of roi index
            roiManager("Select", roi);
            //Measure the area
            List.setMeasurements;
            A = List.getValue("Area");

            //If surface>SurfaceLocale
            if(A>SurfaceLocale){
                SurfaceLocale = A;
                Index = roi;
            }
        }

        roiManager("Select", Index);
        //Harvest the coordinates of the perimeter peaks
        Roi.getCoordinates(Xcoords, Ycoords);
        getStatistics(areaGM, meanGM, minGM, maxGM, stdGM, histogramGM);

    }//Identification or not of the best slice.

	//Close all images
	runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"CloseImages.java");

	//Opening Trigo
	open(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"Trigo.tif");
	rename("Trigo");

	//Create an array Angle on same lentgh than the ROI coordinate (angle)
	Angle = newArray(lengthOf(Xcoords));

	//Create a new image "Cell" of same Width and Height than original one (8bit black)
	newImage("Cell", "8-bit black", W, H, 1);
	setLocation(0, 0);

	//Set ForegroundColor (255,255,255) (white)
	setForegroundColor(255,255,255);

	//Construct an ROI with ALL points of the perimeter
	CoordTchou = Perimeter(Xcoords, Ycoords);
	Xcoords = SplitF(CoordTchou[0],"\t");
	Ycoords = SplitF(CoordTchou[1],"\t");

	//Make the polygon of the cell
	selectWindow("Cell");
	makeSelection("polygon", Xcoords, Ycoords);
	getStatistics(areaGM, meanGM, minGM, maxGM, stdGM, histogramGM);
	List.setMeasurements();
	Xcell = List.getValue("X");
	Ycell = List.getValue("Y");

	XcellHiRes = NZ*Xcell;
	YcellHiRes = NZ*Ycell;

	//Draw it on "Cell"
	run("Draw");

	//Saving High Resolution
	SaveNative("Cell", WHTML, PathStorage+File.separator()+"Detected_Cell.jpg");
	//Saving Low Resolution
	SaveLowRes("Detected_Cell.jpg", WHTML, PathStorage+File.separator()+"Detected_Cell_Low-Res.jpg");

	//Preparing arrays for angle calculation and smoothing
	realsize = lengthOf(Xcoords);

    //HARDCODED size dependant window for angle calculation
    // t = t * realsize / 216;

	Xcoords = Array.concat(Xcoords,Xcoords,Xcoords);
	Ycoords = Array.concat(Ycoords,Ycoords,Ycoords);
	Angle = newArray(lengthOf(Xcoords));

	//Calculate "instant angle" for each point of the perimeter (Xi, Yi)

	for(i=t; i<lengthOf(Xcoords)-t; i++){
		X1 = Xcoords[i-t];
		Y1 = Ycoords[i-t];
		X2 = Xcoords[i+t];
		Y2 = Ycoords[i+t];

		//CosSin(Xi, Yi, X1, Y1, X2, Y2)
		CS = CosSin(Xcoords[i], Ycoords[i], X1, Y1, X2, Y2);
		myCos = CS[0];
		mySin = CS[1];
		Angle[i]=FindDegree(myCos, mySin);
	}

	//Smoothing
	for(i=t+(lissage-1)/2; i<lengthOf(Xcoords)-(lissage-1)/2; i++){
		Alocal = 0;
		for(k= i-(lissage-1)/2; k<= i +(lissage-1)/2; k++){
			Alocal = Alocal + Angle[k];
		}
	Angle[i] = Alocal/lissage;
	}

	//remove 10 non calculated points at both extremities
	Angle = Array.slice(Angle, (t+(lissage-1)/2), lengthOf(Angle)-(t+(lissage-1)/2) );
	Xcoords=Array.slice(Xcoords, (t+(lissage-1)/2), lengthOf(Xcoords)-(t+(lissage-1)/2) );
	Ycoords=Array.slice(Ycoords, (t+(lissage-1)/2), lengthOf(Ycoords)-(t+(lissage-1)/2) );


	//robtain position of min angle
	SortedAngle = Array.rankPositions(Angle);

	//Reslice the arrays (starting from a min) to the correct initial size
	Angle=Array.slice(Angle,SortedAngle[0], SortedAngle[0]+realsize);
	Xcoords=Array.slice(Xcoords,SortedAngle[0], SortedAngle[0]+realsize);
	Ycoords=Array.slice(Ycoords,SortedAngle[0], SortedAngle[0]+realsize);

	//Create High resolution coodinates sets
	XcoordsHiRes = NumericZoom(Xcoords, NZ);
	YcoordsHiRes = NumericZoom(Ycoords, NZ);

	//Detection of real summits

	//Remove angle values <200
	CleanedA = DecapMax(Angle, TresAngleNeg, TresAngleNeg);
    CleanedB = DecapMin(Angle, TresAngleNeg, TresAngleNeg);
	/*
	Array.show(CleanedA);
 	Array.show(Angle);
	waitForUser("");
	*/

	//Loop For sur la tolerance 20
	for (max = minTol+5; max<=maxTol; max +=10){

		Tfiable= "Undefined";
		alltolerance = "";
		delta = "";

		for (tolerance = minTol; tolerance<=max; tolerance ++){
		//for (tolerance = minTol; tolerance<=maxTol; tolerance ++){

			//Get Maxima for the local tolerance

			M = Array.findMaxima(Angle, tolerance);
			n = lengthOf(M);
			Pics = "";
			realAngle="";

			if (n >= 4){



				SumAngle =0;
				//Obtain the sum of the angles for each maxima
				for(iM=0; iM< lengthOf(M); iM++){
					if(Angle[M[iM]] > TresAngleNeg){
						realAngle= realAngle+""+M[iM]+"\t";
						SumAngle+= 360-Angle[M[iM]];
					}
				}
				realAngle = SplitF(realAngle, "\t");
				n = lengthOf(realAngle);
				deltalocal = 100*(abs(SumAngle - 180*(n-2))/(180*(n-2)));
				delta = delta + deltalocal + "\t";
				alltolerance = alltolerance + tolerance + "\t";
			}

		}

		delta = SplitF(delta, "\t");
		alltolerance = SplitF(alltolerance, "\t");
		Array.getStatistics(delta, mind, maxd, meand, stdDev);


		if (mind<PrecisionVoulue){
			//We already have a good model
			max = 10*maxTol; //Exit the loops
		}


		Mindelta = Array.rankPositions(delta);
		Tfiable = alltolerance[Mindelta[0]];
	}



	tolerancebest = newArray(1);
	deltabest = newArray(1);
	tolerancebest[0] = Tfiable;
	deltabest[0] = delta[Mindelta[0]];




	//Recreate maxima with the optimal tolerance
	M = Array.findMaxima(Angle, Tfiable);
	n = lengthOf(M);
	Pics = "";
	realAngle="";
	for(iM=0; iM< lengthOf(M); iM++){
		if(Angle[M[iM]] > TresAngleNeg){
			realAngle= realAngle+""+M[iM]+"\t";
			SumAngle+= 360-Angle[M[iM]];
			Pics = Pics+ ""+ Angle[M[iM]]+ "\t";
		}
	}

    M = SplitF(realAngle,"\t");
	Pics = SplitF(Pics, "\t");

    Mmin = Array.findMinima(Angle, Tfiable);
	n2 = lengthOf(Mmin);
	Pics2 = "";
	realAngle2="";
	for(iM=0; iM< lengthOf(Mmin); iM++){
		if(Angle[Mmin[iM]] < TresAngleNeg){
			realAngle2= realAngle2+""+Mmin[iM]+"\t";
			SumAngle2+= 360-Angle[Mmin[iM]];
			Pics2 = Pics2+ ""+ Angle[Mmin[iM]]+ "\t";
		}
	}

    Mmin = SplitF(realAngle2,"\t");
	Pics2 = SplitF(Pics2, "\t");



	//reate the Graph of the best polygon fitting dependingon tolerance
	Plot.create("Best Tolerance", "Tolerance", "Delta", alltolerance, delta);
	Plot.setFrameSize(1000,720);
	Plot.setColor("red");
	Plot.add("circle", tolerancebest, deltabest);
	Plot.setColor("gray");
	Plot.show();
	TitrePlotTolerance = getTitle();

	//Prepare to save the Graph
	selectWindow(TitrePlotTolerance);
	WP = getWidth();
	HP = getHeight();
	makeRectangle( 0,0, WP,HP);
	run("Copy");
	newImage("Graph", "RGB black", WP, HP, 1);
	makeRectangle( 0,0, WP,HP);
	run("Paste");

	//Saving High Resolution
	SaveNative("Graph", WHTML, PathStorage+File.separator()+"GraphTol.jpg");
	//Saving Low Resolution
	SaveLowRes("GraphTol.jpg", WHTML, PathStorage+File.separator()+"GraphTol_Low-Res.jpg");

	//Create the line Treshold
	Tres = newArray(lengthOf(CleanedA));
	for(t=0; t<lengthOf(Tres); t++){
		Tres[t] = TresAngleNeg;
	}

	//Create the Graph of the best angles peaks obtained with the best tolerance
	Plot.create("Angle with tolerance = "+Tfiable, "Point", "Angle (°)", Tres);
	Plot.setFrameSize(1000,720);
	Plot.setLegend("Threshold\tInstantaneous Angle\tValid values\tSummits", "bottom-left transparent");
	Plot.setLimits(0, lengthOf(CleanedA), 0,300);

	Plot.setColor("gray");
	Plot.add("line", Angle);

	Plot.setColor("orange", "orange");
	Plot.add("circle", CleanedA);

	Plot.setColor("red", "red");
	Plot.add("circle", M, Pics);

    Plot.setColor("blue", "blue");
	Plot.add("circle", CleanedB);

	Plot.setColor("cyan", "cyan");
	Plot.add("circle", Mmin, Pics2);

	Plot.setColor("green");
	Plot.setLineWidth(3);
	Plot.show();
	setLocation(1000,1000);
	TitrePlot = getTitle();

	//Prepare to save the Graph
	selectWindow(TitrePlot);
	WP = getWidth();
	HP = getHeight();
	makeRectangle( 0,0, WP,HP);
	run("Copy");
	newImage("Graph", "RGB black", WP, HP, 1);
	makeRectangle( 0,0, WP,HP);
	run("Paste");

	//Saving High Resolution
	SaveNative("Graph", WHTML, PathStorage+File.separator()+"Graph.jpg");
	//Saving Low Resolution
	SaveLowRes("Graph.jpg", WHTML, PathStorage+File.separator()+"Graph_Low-Res.jpg");

	/*
	WARNING M WILL BE RESORTED FROM HERE!!!!!!!!
	*/

	//Creating the array containing the "geometric" model of the cell
	Array.sort(M);

	segX ="";
	segY ="";
	M2 = "";

	for(iS=0; iS<lengthOf(M); iS++){
		segX =segX +Xcoords[M[iS]]+"\t";
		segY =segY +Ycoords[M[iS]]+"\t";
		M2 = M2 + M[iS]+ "\t";
	}

	//The first summit is added at the en to have easy access to segments
	segX =segX +Xcoords[M[0]];
	segY =segY +Ycoords[M[0]];
	M2 = M2 + M[0];

	//Float arrays of coordinates and index of the summits
	segX = SplitF(segX, "\t");
	segY = SplitF(segY, "\t");
	M2 = SplitF(M2, "\t");

	//High resolution coordinates
	segXHiRes = NumericZoom(segX, NZ);
	segYHiRes = NumericZoom(segY, NZ);

	//Create a new picture "Report" RGB(24bit) for report
	newImage("Report", "RGB black", W*NZ+300, H*NZ, 1);
	setLocation(0,0);
	ColorScale(W*NZ+50, 50, W*NZ+150, H*NZ-50, TresAngleNeg);

	//Create a new picture "Shape" 8Bit black for measure
	newImage("Shape", "8-bit black", W, H, 1);
	setLocation(0,1000);

	//Draw the geometric model
	setForegroundColor(125,125,125);
	selectWindow("Shape");
	makeSelection("polygon", segX,segY);
	setLineWidth(1);
	run("Fill");

	setForegroundColor(125,125,125);
	selectWindow("Report");
	makeSelection("polygon", segXHiRes,segYHiRes);
	setLineWidth(1);
	run("Fill");

	//Creating temp table file for HTML
	F3 = File.open(PathStorage+File.separator()+"table.txt");
	File.close(F3);

	Header = newArray("ANALYSIS", "RADIUS ("+unit+")","STANDARD DEVIATION","NUMBER OF POINTS","SURFACE (% OF THE CELL)");
	Tableau = "DETECTED SEGMENTS WITH CIRCLE MODEL\n"+"<table>\n"+"<thead>\n";

	for(C=0; C<lengthOf(Header);C++){
		Tableau +=  "<td><B>"+ Header[C]+"</B></td>";
	}

	Tableau += "</thead>"+"\n"+ "<body>";
	File.append(Tableau, PathStorage+File.separator()+"table.txt");

	//Annotate the segments
	setFont("Serif", 9*NZ*1.5, "antiliased");
	AlphaB = newArray("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P", "Q","R","S","T","U","V","W","X","Y","Z");

	//Prepare the futures arrays to store all values obtained during the analysis of the segments
	ListType="";
	ListSurface ="";
	ListXcenter ="";
	ListYcenter ="";
	ListRadiusRaw = "";
	ListRadiusMean = "";
	ListStdDev = "";
	ListNPoints = "";


	clearException();
	for (iS =0; iS< lengthOf(segX)-1; iS++){

		//Open the canonical HTML table line
		RH = File.openAsString(getDirectory("macros")+"Find-Curve"+File.separator()+"tableline.html");

		IndexDebutSeg = M2[iS];
		IndexFinSeg = M2[iS+1];

		//Identify the circle fitting with the segment
		CVex = CaVex(IndexDebutSeg, IndexFinSeg, Xcoords, Ycoords);

		ListType+="" +CVex[0] +"\t";
		ListSurface +=""+CVex[1] +"\t";
		ListXcenter +=""+CVex[2] +"\t";
		ListYcenter +=""+CVex[3] +"\t";
		ListRadiusRaw += ""+CVex[4] +"\t";
		ListRadiusMean += ""+CVex[5] +"\t";
		ListStdDev += ""+CVex[6] +"\t";
		ListNPoints += ""+CVex[7] +"\t";

		S = d2s(100*parseFloat(CVex[1])/areaGM ,3);

		//Create a new picture to display the actual segment AND the theoritical Circle on High resolution scale
		newImage("Segment_"+AlphaB[iS], "RGB black", W*NZ, H*NZ, 1);

		selectWindow("Segment_"+AlphaB[iS]);
		//Global shape
		run("Line Width...", "line=10");
		setForegroundColor(255,255,255);
		makeSelection("polygon", XcoordsHiRes, YcoordsHiRes);
		run("Draw");

		if((CVex[5]!="Infinity")||(CVex[5]!="Failed to identify")){
			//Theoritical Circle
			setForegroundColor(100,100,100);
			makeOval(CVex[2]*NZ-CVex[5]*NZ, CVex[3]*NZ-CVex[5]*NZ, 2*CVex[5]*NZ, 2*CVex[5]*NZ);
			run("Draw", "slice");
			CVex[4] = d2s(CVex[4]*pixelWidth,2);
			CVex[5] = d2s(CVex[5]*pixelWidth,2);
			CVex[6] = d2s(CVex[6]*pixelWidth,2);

		}

		//Draw all points of segment
		if(IndexDebutSeg>IndexFinSeg){
			for(i=IndexDebutSeg; i<lengthOf(XcoordsHiRes); i++){
				setForegroundColor(49,133,156);
				makeOval(XcoordsHiRes[i]-NZ, YcoordsHiRes[i]-NZ, 2*NZ, 2*NZ);
				run("Fill");
			}
			for(i=0; i<=IndexFinSeg; i++){
				setForegroundColor(49,133,156);
				makeOval(XcoordsHiRes[i]-NZ, YcoordsHiRes[i]-NZ, 2*NZ, 2*NZ);
				run("Fill");
			}

		}else{
			for(i=IndexDebutSeg; i<=IndexFinSeg; i++){
				setForegroundColor(49,133,156);
				makeOval(XcoordsHiRes[i]-NZ, YcoordsHiRes[i]-NZ, 2*NZ, 2*NZ);
				run("Fill");
			}
		}

		//Add Info as String on the picture
		TexteToImage = ""+AlphaB[iS]+"\n"+"R="+CVex[5]+" +/- "+CVex[6];
		setForegroundColor(49,133,156);
		setJustification("center");
		Ws = getStringWidth(TexteToImage);
		drawString(TexteToImage , XcellHiRes, YcellHiRes);

		//Saving High Resolution
		SaveNative("Segment_"+AlphaB[iS], WHTML, PathStorage+File.separator()+"Segment_"+AlphaB[iS]+".jpg");
		//Saving Low Resolution
		SaveLowRes("Segment_"+AlphaB[iS]+".jpg", WHTML, PathStorage+File.separator()+"Segment_"+AlphaB[iS]+"_Low-Res.jpg");

		//Prepare the HTML
		RH = replace(RH, "MONTITREIMAGE_Segment", "Segment "+AlphaB[iS]+" is "+CVex[0]);
		RH = replace(RH, "NOMFICHIERIMAGE_Segment", "Segment_"+AlphaB[iS]+".jpg");//Image Haute Resolution
		RH = replace(RH, "PATHIMAGE_Segment", "Segment_"+AlphaB[iS]+"_Low-Res.jpg");//Image Basse Resolution
		RH = replace(RH, "ALTIMAGE_Segment", "Segment_"+AlphaB[iS]+".jpg");//Nom alternatif
		RH = replace(RH, "NOMIMAGE_Segment", "Segment_"+AlphaB[iS]+".jpg");//Nom alternatif
		RH = replace(RH, "MONSOUSTITREIMAGE_Segment", "Click to display the high resolution version");//Sous Titre

		RH = replace(RH, "RADIUS", "Raw radius = "+CVex[4]+"\n"+"Corrected to "+CVex[5]);
		RH = replace(RH, "STDEV", ""+CVex[6]);
		RH = replace(RH, "POINTS", ""+CVex[7]);
		RH = replace(RH, "SURFACE", ""+S);

		File.append(RH, PathStorage+File.separator()+"table.txt");
		clearException();
	}
	//Reset linewidth
	run("Line Width...", "line=1");

	//Finishing table for html
	Tableau = "</body></table>";
	File.append(Tableau,PathStorage+File.separator()+"table.txt");


	//Draw the circles of the maxima
	for(i=0; i<lengthOf(M); i++){
		Raffich =5;
		selectWindow("Report");
		setColor(255,255,255);
		setLineWidth(10);
		drawOval(XcoordsHiRes[M[i]]-Raffich*NZ, YcoordsHiRes[M[i]]-Raffich*NZ, Raffich*2*NZ, Raffich*2*NZ);
		run("Draw");
		setLineWidth(1);
	}

    //Draw the squares of the minima
    /*
	for(i=0; i<lengthOf(M2); i++){
		Raffich =5;
		selectWindow("Report");
		setColor(255,255,255);
		setLineWidth(10);
		drawRect(XcoordsHiRes[Mmin[i]]-Raffich*NZ, YcoordsHiRes[Mmin[i]]-Raffich*NZ, Raffich*2*NZ, Raffich*2*NZ);
		run("Draw");
		setLineWidth(1);
	}
    */

	//Draw each point of the perimeter with the color code of the external angle
	for(i=0; i<lengthOf(Angle); i++){
		selectWindow("Report");
		Col = Mycolor(Angle[i], TresAngleNeg);
		setForegroundColor(Col[0],Col[1],Col[2]);
		makeOval(XcoordsHiRes[i]-NZ, YcoordsHiRes[i]-NZ, 2*NZ, 2*NZ);
		run("Fill");
	}

	//Retrieve all values obtained during the analysis of the segments
	ListType= split(ListType, "\t");
	ListSurface = SplitF(ListSurface , "\t");
	ListXcenter = SplitF(ListXcenter , "\t");
	ListYcenter = SplitF(ListYcenter , "\t");
	ListRadiusRaw = SplitF(ListRadiusRaw , "\t");
	ListRadiusMean = SplitF(ListRadiusMean , "\t");
	ListStdDev = SplitF(ListStdDev , "\t");
	ListNPoints = SplitF(ListNPoints , "\t");

	//Display the fragment name
	for (iS =0; iS< lengthOf(segX)-1; iS++){
		IndexDebutSeg = M2[iS];
		IndexFinSeg = M2[iS+1];

		Xs = segXHiRes[iS] + (segXHiRes[iS+1]-segXHiRes[iS])/2;
		Ys = segYHiRes[iS] + (segYHiRes[iS+1]-segYHiRes[iS])/2;

		SegmentType = ListType[iS];
		Surf = ListSurface[iS];
		XcenterS = ListXcenter[iS];
		YcenterS = ListYcenter[iS];
		RR = ListRadiusRaw[iS];
		RO = ListRadiusMean[iS];
		STDV = ListStdDev[iS];
		Npoints = ListNPoints[iS];

		Score = 100*Surf/areaGM;

		if (SegmentType=="FLAT"){
			setForegroundColor(255,255,0);
		}else if (SegmentType=="CONVEX"){
			setForegroundColor(0,0,255);
		}else if (SegmentType=="CONCAVE"){
			setForegroundColor(255,0,0);
		}

		drawString(""+AlphaB[iS], Xs, Ys);
	}

	//Saving High Resolution
	SaveNative("Report", WHTML, PathStorage+File.separator()+"Report.jpg");
	//Saving Low Resolution
	SaveLowRes("Report.jpg", WHTML, PathStorage+File.separator()+"Report_Low-Res.jpg");




	/*--------------------------------------------------------------------
				CREATION HTML
	----------------------------------------------------------------------*/

	//Retrieve system info
	keys = getList('java.properties');

	//Get Time
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	DateAnalysis=""+year+"/"+month+"/"+dayOfMonth+" "+hour+":"+minute;

	//Get Tme point (msec)
	T2 = getTime();

	//Parameters
	duration = (T2-T1)/1000;
	fiability = 100 - deltabest[0];
	pathAssociatedFolder = "./"+myLocalName;

	HTMLT = File.openAsRawString(PathStorage+File.separator()+"table.txt");
	HTML = File.openAsRawString(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"HTML_Curve.html");

	HTML = replace(HTML, "TABLEAU1", HTMLT);
	HTML = replace(HTML, "MYFILE", myLocalName);

	HTML = replace(HTML, "PRECISION", PrecisionVoulue);
	HTML = replace(HTML, "MINTOL", minTol);
	HTML = replace(HTML, "MAXTOL", maxTol);
	HTML = replace(HTML, "ANGLEMIN", TresAngleNeg);

	HTML = replace(HTML, "MON_TITRE_ANALYSE", "FIND CURVE OF "+myLocalName);
	HTML = replace(HTML, "PATHDOSSIER", pathAssociatedFolder);
	HTML = replace(HTML, "MONCHEMIN_BILAN1", pathAssociatedFolder);
	HTML = replace(HTML, "USERNAME", ""+getInfo(keys[37]));
	HTML = replace(HTML, "DATE", ""+DateAnalysis);
	HTML = replace(HTML, "DURATION", ""+duration+" sec");
	HTML = replace(HTML, "FIABILITY", ""+fiability+" %");
	HTML = replace(HTML, "OSNAME", ""+getInfo(keys[23]));
	HTML = replace(HTML, "OSVersion", ""+getInfo(keys[29]));
	HTML = replace(HTML, "OSArch", ""+getInfo(keys[19]));
	HTML = replace(HTML, "JVersion", ""+getInfo(keys[16]));
	HTML = replace(HTML, "JVMVersion", ""+getInfo(keys[5]));
	HTML = replace(HTML, "IJVersion", ""+getVersion());
	HTML = replace(HTML, "IJMaxMem", ""+IJ.maxMemory());
	HTML = replace(HTML, "MacroVersion", ""+version);

	HTML = replace(HTML, "MONTITREIMAGE_3D", "3D Reconstruction ("+myAxis+" rotation)");
	HTML = replace(HTML, "NOMFICHIERIMAGE_3D", "Animated_Cell.gif");//Image Haute Resolution
	HTML = replace(HTML, "PATHIMAGE_3D", "Animated_Cell_Low-Res.gif");//Image Basse Resolution
	HTML = replace(HTML, "ALTIMAGE_3D", "Animated_Cell.gif");//Nom alternatif
	HTML = replace(HTML, "NOMIMAGE_3D", "Animated_Cell.gif");//Nom alternatif
	HTML = replace(HTML, "MONSOUSTITREIMAGE_3D", "Click to display the high resolution version");//Sous Titre

	HTML = replace(HTML, "MONTITREIMAGE_ZPro", ""+myAxis+" Projection (Mean grey value)");
	HTML = replace(HTML, "NOMFICHIERIMAGE_ZPro", "Projection_Cell.jpg");//Image Haute Resolution
	HTML = replace(HTML, "PATHIMAGE_ZPro", "Projection_Cell_Low-Res.jpg");//Image Basse Resolution
	HTML = replace(HTML, "ALTIMAGE_ZPro", "Projection_Cell.jpg");//Nom alternatif
	HTML = replace(HTML, "NOMIMAGE_ZPro", "Projection_Cell.jpg");//Nom alternatif
	HTML = replace(HTML, "MONSOUSTITREIMAGE_ZPro", "Click to display the high resolution version");//Sous Titre

	HTML = replace(HTML, "MONTITREIMAGE_BestSlice", "Best slice for analysis ("+BestSlice+")");
	HTML = replace(HTML, "NOMFICHIERIMAGE_BestSlice", "Detected_Cell.jpg");//Image Haute Resolution
	HTML = replace(HTML, "PATHIMAGE_BestSlice", "Detected_Cell_Low-Res.jpg");//Image Basse Resolution
	HTML = replace(HTML, "ALTIMAGE_BestSlice", "Detected_Cell.jpg");//Nom alternatif
	HTML = replace(HTML, "NOMIMAGE_BestSlice", "Detected_Cell.jpg");//Nom alternatif
	HTML = replace(HTML, "MONSOUSTITREIMAGE_BestSlice", "No high resolution version available");//Sous Titre

	HTML = replace(HTML, "MONTITREIMAGE_Report", "FINAL RESULT OF THE ANALYSIS");
	HTML = replace(HTML, "NOMFICHIERIMAGE_Report", "Report.jpg");//Image Haute Resolution
	HTML = replace(HTML, "PATHIMAGE_Report", "Report_Low-Res.jpg");//Image Basse Resolution
	HTML = replace(HTML, "ALTIMAGE_Report", "Report.jpg");//Nom alternatif
	HTML = replace(HTML, "NOMIMAGE_Report", "Report.jpg");//Nom alternatif
	HTML = replace(HTML, "MONSOUSTITREIMAGE_Report", "Click to display the high resolution version");//Sous Titre

	HTML = replace(HTML, "MONTITREIMAGE_Tolerance", "FITTING POLYGONE MODEL DEPENDING ON TOLERANCE");
	HTML = replace(HTML, "NOMFICHIERIMAGE_Tolerance", "GraphTol.jpg");//Image Haute Resolution
	HTML = replace(HTML, "PATHIMAGE_Tolerance", "GraphTol_Low-Res.jpg");//Image Basse Resolution
	HTML = replace(HTML, "ALTIMAGE_Tolerance", "GraphTol.jpg");//Nom alternatif
	HTML = replace(HTML, "NOMIMAGE_Tolerance", "GraphTol.jpg");//Nom alternatif
	HTML = replace(HTML, "MONSOUSTITREIMAGE_Tolerance", "Click to display the high resolution version");//Sous Titre

	HTML = replace(HTML, "MONTITREIMAGE_Angles", "DETECTED ANGLES WITH TOLERANCE = "+Tfiable+"\n"+"PERIMETER = "+realsize);
	HTML = replace(HTML, "NOMFICHIERIMAGE_Angles", "Graph.jpg");//Image Haute Resolution
	HTML = replace(HTML, "PATHIMAGE_Angles", "Graph_Low-Res.jpg");//Image Basse Resolution
	HTML = replace(HTML, "ALTIMAGE_Angles", "Graph.jpg");//Nom alternatif
	HTML = replace(HTML, "NOMIMAGE_Angles", "Graph.jpg");//Nom alternatif
	HTML = replace(HTML, "MONSOUSTITREIMAGE_Angles", "Click to display the high resolution version");//Sous Titre

	File.saveString(HTML, PathOutput+File.separator()+myLocalName+".html");

	//waitForUser("Analysis is over\nAfterclicking all images will be closed");
	runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"CloseImages.java");


/*
______________________________________________
FUNCTIONS
______________________________________________
*/

function DecapMax(MyArray, seuil, correction){

/*
This function returns a new array contening the values of the original array with the corrected values for those below the threshold.
*/

	Arraydecap = newArray(lengthOf(MyArray));

	for(inde=0; inde<lengthOf(MyArray); inde++){
		if(MyArray[inde]> seuil){
			Arraydecap[inde] = MyArray[inde];
		}else{
			Arraydecap[inde] = correction;
		}
	}
	return Arraydecap;

}// End function Decap


function DecapMin(MyArray, seuil, correction){

/*
This function returns a new array contening the values of the original array with the corrected values for those below the threshold.
*/

	Arraydecap = newArray(lengthOf(MyArray));

	for(inde=0; inde<lengthOf(MyArray); inde++){
		if(MyArray[inde]< seuil){
			Arraydecap[inde] = MyArray[inde];
		}else{
			Arraydecap[inde] = correction;
		}
	}
	return Arraydecap;

}// End function Decap








function clearException() {
/*
Close all java exception reports
*/
	if (isOpen("Exception")){
		selectWindow("Exception");
		run("Close");
	}
}//End clearException


function ColorScale(Xdep,Ydep,Xarr,Yarr,TresAngleNeg) {


// Put the color dependant of the Y value
for(Y=Ydep; Y<Yarr; Y++){

	AngleY = 360-(((Y-Ydep)/(Yarr-Ydep))*360);
	scale = Mycolor(AngleY, TresAngleNeg);
	setColor(scale[0], scale[1], scale[2]);
	drawLine(Xdep, Y, Xarr, Y);

}//End ColorScale





function SaveNative(Window, minWidth, PathToSave){
/*
Save the specified window to the path.
If the width id to small for HTML display its size is increased.
*/
	selectWindow(Window);
	Wi= getWidth();
	Hi= getHeight();

	if(Wi<minWidth){
		ratioScale = minWidth/Wi;
		Hreduction = Hi*ratioScale;
		makeRectangle(0,0,Wi, Hi);
		run("Scale...", "x=- y=- width="+minWidth+" height="+Hreduction+" interpolation=Bilinear average create title=High-Res");
	}
	saveAs("Jpeg", PathToSave);
}


function SaveLowRes(Window, ToWidth, PathToSave){
/*
Save the specified window to the path.
The image is reducedto fit to ToWidth if necessary
*/
	selectWindow(Window);
	Wi= getWidth();
	Hi= getHeight();

	if(Wi!=ToWidth){
		ratioScale = ToWidth/(Wi);
		Hreduction = Hi*ratioScale;
		makeRectangle(0,0,Wi, Hi);
		run("Scale...", "x=- y=- width="+ToWidth+" height="+Hreduction+" interpolation=Bilinear average create title=Low-Res");
	}
	saveAs("Jpeg", PathToSave);
}




function VerifCos(Xcand, Ycand, Xo, Yo, Xc, Yc){
/*
Determine which center position is correct, using the cosinus between vectors Center-O and Center-C.
*/
//print("VerifCos");

	Xcenter1 = Xcand[0];
	Xcenter2 = Xcand[1];

	Ycenter1 = Ycand[0];
	Ycenter2 = Ycand[1];


	bestCos =newArray(2);

	Xoc = Xc-Xo;
	Yoc = Yc-Yo;

	Xocenter1 = Xcenter1-Xo;
	Yocenter1 = Ycenter1-Yo;

	Xocenter2 = Xcenter2-Xo;
	Yocenter2 = Ycenter2-Yo;

	CO = sqrt((Xoc)*(Xoc)+(Yoc)*(Yoc));
	CCenter1 = sqrt((Xocenter1)*(Xocenter1)+(Yocenter1)*(Yocenter1));
	CCenter2 = sqrt((Xocenter2)*(Xocenter2)+(Yocenter2)*(Yocenter2));

	CosCenter1 = ((Xoc)*(Xocenter1)+(Yoc)*(Yocenter1))/(CO*CCenter1);
	CosCenter2 = ((Xoc)*(Xocenter2)+(Yoc)*(Yocenter2))/(CO*CCenter2);

	if(CosCenter1>CosCenter2){
		bestCos[1] = 0;
		bestCos[0] = 1;
	}else{
		bestCos[1] = 1;
		bestCos[0] = 0;
	}

	return bestCos;
}// End function VerifCos


function VerifDistance(Xcand, Ycand, Xo, Yo, Xc, Yc){
/*
Determine which center position is correct, using the distance between Center and O and Center and C.
*/
//print("VerifDistance");

	Xcenter1 = Xcand[0];
	Xcenter2 = Xcand[1];

	Ycenter1 = Ycand[0];
	Ycenter2 = Ycand[1];

	distcand =newArray(2);

	distcand[0] = sqrt( (Xcenter1-Xo)*(Xcenter1-Xo) + (Ycenter1-Yo)*(Ycenter1-Yo))  - sqrt( (Xcenter1-Xc)*(Xcenter1-Xc) + (Ycenter1-Yc)*(Ycenter1-Yc));
	distcand[1] = sqrt( (Xcenter2-Xo)*(Xcenter2-Xo) + (Ycenter2-Yo)*(Ycenter2-Yo))  - sqrt( (Xcenter2-Xc)*(Xcenter2-Xc) + (Ycenter2-Yc)*(Ycenter2-Yc));

	R = Array.rankPositions(distcand);

	return R;
}// End function VerifDistance



function CreateVirtualSeg(X1, Y1, XO, YO, X2, Y2,precisionLocale){
/*
Creates "artificial points" between experimental ones using an increased resolution (<1pixel)
*/
//print("CreateVirtualSeg "+ precisionLocale);

	myXs = "";
	myYs = "";

	//First segment (aO)
	if(X1!=XO){

	S1=(YO-Y1)/(XO-X1);
	Cst1 = Y1-S1*X1;

	signe = abs(XO-X1)/(XO-X1);
		if (signe>0){
			for(x=X1; x<=XO; x+=precisionLocale){
				myXs+= ""+ x+"\t";
				Yhere = S1*x+Cst1;
				myYs += ""+ Yhere+"\t";
				//print(""+x+"  "+Yhere);
			}
		}else{
			for(x=X1; x>=XO; x-=precisionLocale){
				myXs+= ""+ x+"\t";
				Yhere = S1*x+Cst1;
				myYs += ""+ Yhere+"\t";
				//print(""+x+"  "+Yhere);
			}

		}
	}else{
	signe = abs(YO-Y1)/(YO-Y1);
		if (signe>0){
			for(y=Y1; y<=YO; y+=precisionLocale){
				myXs+= ""+ XO+"\t";
				myYs += ""+ y+"\t";
				//print(""+XO+"  "+y);
			}
		}else{
			for(y=Y1; y>=YO; y-=precisionLocale){
				myXs+= ""+ XO+"\t";
				myYs += ""+ y+"\t";
				//print(""+XO+"  "+y);
			}
		}
	}

	//Second segment (Ob)
	if(X2!=XO){

	S2=(Y2-YO)/(X2-XO);
	Cst2 = Y2-S2*X2;

	signe = abs(X2-XO)/(X2-XO);
		if (signe>0){
			for(x=XO+precisionLocale; x<=X2; x+=precisionLocale){
			myXs+= ""+ x+"\t";
			Yhere = S2*x+Cst2;
			myYs += ""+ Yhere+"\t";
			//print(""+x+"  "+Yhere);
			}
		}else{
			for(x=XO+precisionLocale; x>=X2; x-=precisionLocale){
			myXs+= ""+ x+"\t";
			Yhere = S2*x+Cst2;
			myYs += ""+ Yhere+"\t";
			//print(""+x+"  "+Yhere);
			}
		}

	}else{
	signe = abs(Y2-YO)/(Y2-YO);
		if (signe>0){
			for(y=YO; y<=Y2; y+=precisionLocale){
			myXs+= ""+ XO+"\t";
			myYs += ""+ y+"\t";
			//print(""+XO+"  "+y);
			}
		}else{
			for(y=YO; y>=Y2; y-=precisionLocale){
			myXs+= ""+ XO+"\t";
			myYs += ""+ y+"\t";
			//print(""+XO+"  "+y);
			}
		}
	}

	R = newArray(myXs,myYs);

	return R;
}


function FindO(segX,segY, S2,Cst){
/*
Find the closest experimental point to the curve Y = SX+cst
*/
//print("FindO");
	MyFit = newArray(lengthOf(segX));
	MyFit[0] = 10;
	MyFit[lengthOf(segX)-1] = 10;


	for(Ind=1; Ind<lengthOf(segX)-1; Ind++){

		Yc=segX[Ind]*S2+Cst;	//calculate the difference between experimental Y and theoritical one
		MyFit[Ind] = abs(Yc-segY[Ind]);
	}
	MyMinFit = Array.rankPositions(MyFit);	//find the closest point (min difference)

	//Plot.create("Precision to Perpendicular", "Point", "Precision", MyFit);
	//Plot.show();
	//waitForUser("");

	Res =newArray(MyMinFit[0], MyFit[MyMinFit[0]]);

	return Res;
}


function FindOH(segX,segY, Xref){
/*
Find the closest experimental point to the curve Y = SX+cst
*/
//print("FindOH");
	MyFit = newArray(lengthOf(segX));
	MyFit[0] = 10;
	MyFit[lengthOf(segX)-1] = 10;


	for(Ind=1; Ind<lengthOf(segX)-1; Ind++){

		//calculate the difference between experimental X and theoritical one
		MyFit[Ind] = abs(Xref-segX[Ind]);
	}
	MyMinFit = Array.rankPositions(MyFit);	//find the closest point (min difference)

	//Plot.create("Precision to Perpendicular", "Point", "Precision", MyFit);
	//Plot.show();
	//waitForUser("");

	Res =newArray(MyMinFit[0], MyFit[MyMinFit[0]]);

	return Res;
}


function Perimeter(ValX, ValY){
/*
Input arrays of coordinates of ROI points
Output 1 array of 2 strings containing the coordinates of All points of the perimeter
*/
//print("Perimeter");
	//Insert the first point at the end to close the perimeter
	ValX = Array.concat(ValX,ValX[0]);
	ValY = Array.concat(ValY,ValY[0]);

	PerX = "";
	PerY = "";

	precision = 0.1;

	//Loop For on the segments
	for(p=0; p<(lengthOf(ValX)-1); p++){
		Xa = ValX[p];
		Ya = ValY[p];

		Xb = ValX[p+1];
		Yb = ValY[p+1];

		//print("A("+Xa+","+Ya+")  B("+Xb+","+Yb+")");

		//Vertical line
		if(Xa==Xb){
			//Going up in Y
			if(Ya<Yb){
				//print("Vertical line going up in Y");
				for(Y=Ya; Y<Yb; Y++){
					PerX+= ""+Xa +"\t";
					PerY+= ""+Y +"\t";
					//print("      ("+Xa+","+Y+")");
				}
			}
			//Going down in Y
			if(Ya>Yb){
				//print("Vertical line going down in Y");
				for(Y=Ya; Y>Yb; Y--){
					PerX+= ""+Xa +"\t";
					PerY+= ""+Y +"\t";
					//print("      ("+Xa+","+Y+")");
				}
			}
		}

		//Horizontal line
		if(Ya==Yb){
			//Going up in X
			if(Xa<Xb){
				//print("Horizontal line going up in X");
				for(X=Xa; X<Xb; X++){
					PerX+= ""+X +"\t";
					PerY+= ""+Ya +"\t";
					//print("      ("+X+","+Ya+")");
				}
			}
			//Going down in X
			if(Xa>Xb){
				//print("Horizontal line going down in X");
				for(X=Xa; X>Xb; X--){
					PerX+= ""+X +"\t";
					PerY+= ""+Ya +"\t";
					//print("      ("+X+","+Ya+")");
				}
			}
		}
		//Diagonal
		if((Ya!=Yb)&&(Xa!=Xb)){
			S = (Yb-Ya)/(Xb-Xa);
			//print("Diagonal with slope ="+S);
			signe = abs(Xb-Xa)/(Xb-Xa);
			Xref=-1;
			Yref=-1;
			for(X=Xa; X>0; X+=signe*precision){
				Y = floor(S(X-Xa)+Ya);
				X = floor(X);

				if((X==Xb)&&(Y==Yb)){
					X=-10000	//EXIT the loop to not incorporate B
				}else if((X!=Xref)||(Y!=Yref)){
					//print("      ("+X+","+Y+")");
					PerX+= ""+X +"\t";
					PerY+= ""+Y +"\t";
					Xref = X;
					Yref = Y;
				}
			}


		}

	//waitForUser("");
	} //END Loop For on the segments

	CS = newArray(PerX,PerY);

	return CS;
}//End function Perimeter




function NumericZoom(MyArray, Factor){
/*
Increase the values of the array using the multiplicator Factor
*/
//print("NumericZoom");
	ResArray = newArray(lengthOf(MyArray));

	for (a=0; a<lengthOf(MyArray); a++){
		ResArray[a] = Factor*MyArray[a];
	}
	return ResArray;
}//End function NumericZoom


function SplitF(MyString, MyChar){
/*
Input is a string containing numerical values separated by MyChar
Output is an array of Float values
*/
	A = split(MyString, MyChar);
	B = newArray(lengthOf(A));

	for (a=0; a < lengthOf(A); a++){
		B[a] = parseFloat(A[a]);
	}
	return B;
}//End function SpliF


function Radius(segX,segY){
/*
Input two arrays containing coordinates of the points of the membrane's segment studied
Output the position of the center of the fitting circle.
*/
//print("Radius");
	//Output Array
	CoordCenter = newArray(0,0, 0, 0, 0);

	//Arrays containing the coordinates of potential Center position
	Xcand=newArray(2);	//array that will contain both possible X position for the center
	Ycand=newArray(2);	//array that will contain both possible X position for the center

	//Coordinates of A
	Xa = segX[0];
	Ya = segY[0];

	//Coordinates of B
	Xb = segX[lengthOf(segX)-1];
	Yb = segY[lengthOf(segY)-1];

	//Identification of C (midde point of AB straight segment)
	Xc=(Xa + Xb)/2;
	Yc=(Ya + Yb)/2;

	// Identification de O

	/*------------------------------------------------------------------------------------------------------------
		Vertical segment
	------------------------------------------------------------------------------------------------------------*/

	if(Xb==Xa){

		precisionLocale = 1;	//Initial precision to find O, belonging to the mediatrice of AB && point of the ROI
		segXl = segX;		//local array (size will change with resolution) containing X coordinates of points of interest of the ROI
		segYl = segY;		//local array (size will change with resolution) containing Y coordinates of points of interest of the ROI
		Cst = Yc;
		S2 = 0;

		for (test = 0; test<1; test +=0){
			Ident = FindO(segXl,segYl, S2,Cst); //Identify the closest point of the ROI to the mediatrice
			fiability = Ident[1];		//distance in pixel between the position of O (ROI) and what it should be on the mediatrice
			indexO = Ident[0];		//index of O in segXl and segYl


			//Handling Exception where O can't be found
			if ( (indexO<2) || (indexO>lengthOf(segXl)-3) ){
				test= 2000;	//we do not have got O
			}else{

				if (fiability<0.1){
					test= 1000;	//we got O
					Xo = segXl[indexO];
					Yo = segYl[indexO];
				}else{	//Increase resolution between point n-2 and point n+2 compare to point n (minimum)
					precisionLocale=precisionLocale/10;	//update precision

					//Security to avoid crash due to O impossible to find
					if(precisionLocale>0.0001){
						NewCoords = CreateVirtualSeg(segXl[indexO-2], segYl[indexO-2],segXl[indexO], segYl[indexO], segXl[indexO+2], segYl[indexO+2],precisionLocale); //create a new set of coordinate
 						segXl = SplitF(NewCoords[0],"\t");
						segYl = SplitF(NewCoords[1],"\t");
					}else{
						test= 2000;	//we do not have got O
					}
				}
			}
		}

		//Calculation only if O has been found
		if(test<2000){
			d = sqrt((Yo-Yc)*(Yo-Yc) +(Xo-Xc)*(Xo-Xc));	//distance OC
			AB = sqrt((Ya-Yb)*(Ya-Yb)+(Xa-Xb)*(Xa-Xb));	//distance AB
			R = d/2 +(AB*AB)/(8*d);	//resolving R using d and AB

			//Resolving X and Y of Center using R and the curve of the mediatrice
			Xcand[0] = Xo-R;
			Xcand[1] = Xo+R;

			Ycand[0] = Yc;
			Ycand[1] = Yc;
		}
	}


	/*------------------------------------------------------------------------------------------------------------
		Horizontal segment
	------------------------------------------------------------------------------------------------------------*/

	if(Yb==Ya){

		precisionLocale = 1;	//Initial precision to find O, belonging to the mediatrice of AB && point of the ROI
		segXl = segX;		//local array (size will change with resolution) containing X coordinates of points of interest of the ROI
		segYl = segY;		//local array (size will change with resolution) containing Y coordinates of points of interest of the ROI
		for (test = 0; test<1; test +=0){
			Ident = FindOH(segXl,segYl, Xc); //Identify the closest point of the ROI to the mediatrice
			fiability = Ident[1];		//distance in pixel between the position of O (ROI) and what it should be on the mediatrice
			indexO = Ident[0];		//index of O in segXl and segYl

			//Handling Exception where O can't be found
			if ( (indexO<2) || (indexO>lengthOf(segXl)-3) ){
				test= 2000;	//we do not have got O
			}else{

				if (fiability<0.1){
					test= 1000;	//we got O
					Xo = segXl[indexO];
					Yo = segYl[indexO];
				}else{	//Increase resolution between point n-2 and point n+2 compare to point n (minimum)
					precisionLocale=precisionLocale/10;	//update precision

					//Security to avoid crash due to O impossible to find
					if(precisionLocale>0.0001){
					NewCoords = CreateVirtualSeg(segXl[indexO-2], segYl[indexO-2],segXl[indexO], segYl[indexO], segXl[indexO+2], segYl[indexO+2],precisionLocale); //create a new set of coordinate
 					segXl = SplitF(NewCoords[0],"\t");
					segYl = SplitF(NewCoords[1],"\t");
					}else{
						test= 2000;	//we do not have got O
					}
				}
			}
		}

		//Calculation only if O has been found
		if(test<2000){
			d = sqrt((Yo-Yc)*(Yo-Yc) +(Xo-Xc)*(Xo-Xc));	//distance OC
			AB = sqrt((Ya-Yb)*(Ya-Yb)+(Xa-Xb)*(Xa-Xb));	//distance AB
			R = d/2 +(AB*AB)/(8*d);	//resolving R using d and AB

			//Resolving X and Y of Center using R and the curve of the mediatrice
			Xcand[0] = Xc;
			Xcand[1] = Xc;

			Ycand[0] = Yo-R;
			Ycand[1] = Yo+R;
		}
	}



	/*------------------------------------------------------------------------------------------------------------
		Most common situation
	------------------------------------------------------------------------------------------------------------*/


	if((Xb!=Xa)&&(Yb!=Ya)){

		S=(Yb-Ya)/(Xb-Xa);	//slope of AB fragment
		S2 = -1/S;		//slope of the perpendicular cirve to AB (Y = S2X+Cst)
		Cst = Yc-Xc*S2;		//constante of the perpendicular curve to AB crossing on C (meiatrice)
		precisionLocale = 1;	//Initial precision to find O, belonging to the mediatrice of AB && point of the ROI
		segXl = segX;		//local array (size will change with resolution) containing X coordinates of points of interest of the ROI
		segYl = segY;		//local array (size will change with resolution) containing Y coordinates of points of interest of the ROI

		for (test = 0; test<1; test +=0){

			Ident = FindO(segXl,segYl, S2,Cst); //Identify the closest point of the ROI to the mediatrice
			fiability = Ident[1];		//distance in pixel between the position of O (ROI) and what it should be on the mediatrice
			indexO = Ident[0];		//index of O in segXl and segYl

			//Handling Exception where O can't be found
			if ( (indexO<2) || (indexO>lengthOf(segXl)-3) ){
				test= 2000;	//we do not have got O
			}else{

				if (fiability<0.1){
					test= 1000;	//we got O
					Xo = segXl[indexO];
					Yo = segYl[indexO];
				}else{	//Increase resolution between point n-2 and point n+2 compare to point n (minimum)
					precisionLocale=precisionLocale/10;	//update precision

					//Security to avoid crash due to O impossible to find
					if(precisionLocale>0.0001){
					NewCoords = CreateVirtualSeg(segXl[indexO-2], segYl[indexO-2],segXl[indexO], segYl[indexO], segXl[indexO+2], segYl[indexO+2],precisionLocale); //create a new set of coordinate
 					segXl = SplitF(NewCoords[0],"\t");
					segYl = SplitF(NewCoords[1],"\t");
					}else{
						test= 2000;	//we do not have got O
					}
				}
			}
		}

		//Calculation only if O has been found
		if(test<2000){
			d = sqrt((Yo-Yc)*(Yo-Yc) +(Xo-Xc)*(Xo-Xc));	//distance OC
			AB = sqrt((Ya-Yb)*(Ya-Yb)+(Xa-Xb)*(Xa-Xb));	//distance AB
			R = d/2 +(AB*AB)/(8*d);	//resolving R using d and AB

			//Resolving X and Y of Center using R and the curve of the mediatrice
			Xcenter1 = Xo+sqrt(R*R/(S2*S2+1));
			Xcenter2 = Xo-sqrt(R*R/(S2*S2+1));

			Xcand[0] = Xcenter1;
			Xcand[1] = Xcenter2;

			Ycenter1 = -Xcenter1/S + Cst;
			Ycenter2 = -Xcenter2/S + Cst;

			Ycand[0] = Ycenter1;
			Ycand[1] = Ycenter2;
		}

	}


	//Calculation only if O has been found
	if(test<2000){
		//Determine which solution is correct using David's (distance approach)
		CorrectDist = VerifDistance(Xcand, Ycand, Xo, Yo, Xc, Yc);

		//Determine which solution is correct using Blandine's (distance approach)
		CorrectCos =VerifCos(Xcand, Ycand, Xo, Yo, Xc, Yc);

		if (CorrectDist[1]!=CorrectCos[1]){
			print("Dual check of center's position failed");
		}

		//Attribute the definitive position of the center
		XcenterF = Xcand[CorrectDist[1]];
		YcenterF = Ycand[CorrectDist[1]];

		//Challenge this position with all experimental points of the segment
		myFiability = newArray(lengthOf(segX)-1);

		for(point=0; point <lengthOf(myFiability); point++){
			myFiability[point] = sqrt( (segX[point]-XcenterF)*(segX[point]-XcenterF)+(segY[point]-YcenterF)*(segY[point]-YcenterF));
		}
		Array.getStatistics(myFiability, minR, maxR, meanR, stdDevR);

	}else{
		//Give data for O failed
		XcenterF = "Failed to identify";
		YcenterF = "Failed to identify";
		R = "Failed to identify";
		meanR = "Failed to identify";
		stdDevR = "Failed to identify";
	}

	//create the output array of the function
	CoordCenter = newArray(XcenterF,YcenterF, R, meanR, stdDevR);
	return CoordCenter;

}//end Function Radius






function CaVex(IndexD, IndexF, ArrayX, ArrayY){
/*
Input begining and end index of the A and B points (identified summit) and the arrays containing the coordinates of all points
Extract the points of interest (between A and B)
Output determine if the segment AB is concave or convex
*/
//print("CaVex");

	Xrois ="";
	Yrois ="";

	if (IndexD>IndexF){
		for(i=IndexD; i<lengthOf(ArrayX); i++){
			Xrois+= ""+ArrayX[i]+"\t";
			Yrois+= ""+ArrayY[i]+"\t";
		}
		for(i=0; i<IndexF; i++){
			Xrois+= ""+ArrayX[i]+"\t";
			Yrois+= ""+ArrayY[i]+"\t";
		}
	}else{
		for(i=IndexD; i<=IndexF; i++){
			Xrois+= ""+ArrayX[i]+"\t";
			Yrois+= ""+ArrayY[i]+"\t";
		}
	}

	Xrois = SplitF(Xrois, "\t");
	Yrois = SplitF(Yrois, "\t");

	selectWindow("Shape");
	makeSelection("polygon", Xrois, Yrois);
	getStatistics(area, mean, min, max, std, histogram);

	Res = (255-mean)-125;
	//print("Measured gray level= "+Res);

	if (Res<64){
		SegType = "CONCAVE";
	}else if (Res >70){
		SegType = "CONVEX";
	}else {
		SegType = "FLAT";
	}

	MyCenter = Radius(Xrois,Yrois);

	//myRes("Type de concavitude", "Surface locale", "X du centre", "Y du centre", "Rayon", "Rayon moyen sur le segment pour X et Y", "StdDev", "Nombre de points")

	myRes = newArray(SegType, ""+area, ""+MyCenter[0], ""+MyCenter[1], ""+MyCenter[2], ""+MyCenter[3], ""+MyCenter[4], ""+lengthOf(Xrois));

	return myRes;
}//End function Cavex




function processCell(x0,y0){
/*
Display as a tracker the function Tchoutchou
*/


	//waitForUser(""+x0+"  "+y0);

	selectWindow("Cell");
	setPixel(x0,y0,125);
	updateDisplay();

}
//End processCell

function Trigo(){
//Create a new image "Trigo" 16bit to embed the angle value expressed as a grey value depending on cos/sin combination

	newImage("Trigo", "16-bit black", 400, 400, 1);
	setLocation(1000,0);

	//For every angle between 0 and 2PI (one turn) step being 0.00001rad
	for(a=0; a<2*PI; a+=0.00001){
		//transform cos as a X value (center of the trigo circle is (200,200)
		X =cos(a)*200+200;
		//transform cos as a X value (center of the trigo circle is (200,200)
		Y =sin(a)*200+200;
		//convert the rad value as degre color (100 times increased, to see it)
		setColor(100*a*360/(2*PI));
		//draw the line (center, point defined with the rad angle)
		drawLine(200,200,X,Y);
		/*
		Warning: due to IJ coordinates reference (0,0) top left, the trigo circle is visually flipped vertically!!!
		*/
	}
}


function FindDegree(MyCos, MySin){
//Find the angle value in degree from the cos and sin using the Trigo image


	//Select the Trigo image
	selectWindow("Trigo");

	//Identify the grey value of the pixel of interest
	X=198*MyCos+200;
	Y=198*MySin+200;

	//Get Pixel value and convert into degree
	V = getPixel(X,Y)/100;

	return V;
}


function CosSin(CenterX, CenterY, X1, Y1, X2, Y2){
/*
Input Coordinates of points C, A and B
Output cos and sin of (CA, CB) angle
*/


	CS = newArray(2);

	//Calculate V1 and V2 vectors X and Y
	//V(X1-CenterX, Y1-CenterY)
	XV1 = X1-CenterX;
	YV1 = Y1-CenterY;

	//Vp(X2-CenterX, Y2-CenterY)
	XV2 = X2-CenterX;
	YV2 = Y2-CenterY;

	//Calculate length of V ||V|| = root2(X^2+Y^2)
	nV1= sqrt(XV1*XV1+YV1*YV1);

	//Calculate length of Vp ||Vp|| = root2(Xp^2+Yp^2)
	nV2= sqrt(XV2*XV2+YV2*YV2);

	//waitForUser("nV1="+nV1+"\n"+"nV2="+nV2);

	//COS = (X*Xp+Y*Yp)/(||V||*||Vp||)		Scalar product
	CS[0] = (XV1*XV2+YV1*YV2)/(nV1*nV2);
	//SIN = (X*Yp-Y*Xp)/(||V||*||Vp||)		Vectorial product
	CS[1] = (XV1*YV2-YV1*XV2)/(nV1*nV2);
	//Return [COS, SIN]
	return CS;
}
//End function CosSin

function CleanManager(){
/*
Remove all existing ROI in the ROImanager
*/
runMacro(getDirectory("macros")+File.separator()+"Find-Curve"+File.separator()+"ROIeraser.java");
}
//End of function cleanManager



function Mycolor(Angle, TresAngleNeg){
/*
Colorscale for the angle values
*/




	if (Angle>TresAngleNeg) {
		Blue0 = 0;
		Red0 = 255*(Angle-TresAngleNeg)/(250-TresAngleNeg);
		Green0 = 255-255*(Angle-TresAngleNeg)/(250-TresAngleNeg);
	}else{
		Blue0 = 255-255*(Angle-140)/(TresAngleNeg-140);
		Green0 = 255*(Angle-140)/(TresAngleNeg-140);
		Red0 = 0;
	}

	mycolor = newArray(Red0,Green0,Blue0);

	for(i=0; i<lengthOf(mycolor); i++){
		if (mycolor[i] <0){
			mycolor[i]=0;
		}

		if (mycolor[i] >255){
			mycolor[i]=255;
		}
	}

	return mycolor;
}//End myColor

}
//End Macro Find-Curve
