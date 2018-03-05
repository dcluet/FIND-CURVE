macro "CleanMemory" {

/*
/*

This macro returns the amount of memory in bytes currently used by ImageJ before and after cleaning the memory. Blandine VERGIER 2017/03/20

*/

// Maximum of memory
MaxMem = parseFloat(IJ.maxMemory());

// Current memory
CurrentMem = parseFloat(IJ.currentMemory());
//print(""+CurrentMem+"/"+MaxMem);

// Memory cleaning
call("java.lang.System.gc");

// Memory after cleaning
CurrentMem = parseFloat(IJ.currentMemory());
//print(""+CurrentMem+"/"+MaxMem);


} // End Macro CleanMemory
