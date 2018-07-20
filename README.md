Automated calculation of radius of curvature of cytoplasmic membrane
===

The `Find-Curve` macro for the ImageJ program ([Schneider et al. 2012](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5554542/)) automatically processes all cell stacks contained in a root folder indicated by the user.
For each file, the program performs the following steps:
1. Identification of the slice with the **largest area**.

|![Original](src/doc/3D.jpg)|![Identified biggest slice](src/doc/Slice.jpg)|
|---|---|
|**Original image (3D representation)**|**Identified "Best" Slice**|
2. On this slice the **angle (OA, OB) is calculated** for every point (O) of the perimeter, with A and B two neighbors points respectively upstream and downstream to O.

|![Segments identification](src/doc/Identification.jpg)|
|---|
|**Angle values along the cell perimeter**|   |
3. The identification of **local maximal angle values** along the perimeter allows to delimit **segments** and draw the **regular polygon model** for the cell. The **concave** or **convex** property of the segments is determined using the regular polygon as reference.

|![Regular Polygon and summits](src/doc/Avatar.jpg)|
|---|
|**Identified summits (white circles)<br/>Regular fiiting polygon (grey  shape).<br/>Convex segments (blue letters)<br/>Concave segment (red letters)**|

4. For all segments, **the radius of the best fitting osculating circle** is calculated ([Mesmoudi et al. 2010](https://www.researchgate.net/profile/Leila_De_Floriani2/publication/220868972_A_Geometric_Approach_to_Curvature_Estimation_on_Triangulated_3D_Shapes/links/00b7d535728d79a2fb000000/A-Geometric-Approach-to-Curvature-Estimation-on-Triangulated-3D-Shapes.pdf?origin=publication_detail), [Berutti et al. 2012](http://www.zahnheilkunde.de/beitragpdf/pdf_7398.pdf)). This value is the **curvature radius R** of the studied segment.

|![osculating circle](src/doc/Osculating_circle.jpg) |![calculated Radius of curvature](src/doc/Segment.jpg)|
|---|----|
|**Principle of the Osculating circle.**   |**Application to the determination<br/>of the curvature radius<br/>of segment B**   |
5. A **.html report file** is automatically generated.


**Contributors**
--

| ![LBMC Logo](src/doc/Logo_LBMC.jpg) ![CNRS Logo](src/doc/Logo_cnrs.jpg) ![ENS Logo](src/doc/Logo_ens.jpg) ||
|-----------------------------|------------|
|**CLUET David**|     [david.cluet@ens-lyon.fr](david.cluet@ens-lyon.fr)|
|**VERGIER Blandine**| [blandine.vergier@unistra.fr](blandine.vergier@etu.unistra.fr)  |


License
--

Copyright CNRS 2013


>This software is a computer program whose purpose is to **automatically identify segments of the cytoplasmic membrane and calculate their radius of curvature**.
>
>This software is governed by the CeCILL  license under French law and abiding
by the rules of distribution of free software. You can use, modify and/ or
redistribute the software under the terms of the CeCILL license as circulated
by CEA, CNRS and INRIA at the following URL:
http://www.cecill.info/index.en.html
>
>As a counterpart to the access to the source code and  rights to copy, modify
and redistribute granted by the license, users are provided only with a limited
warranty  and the software's author,the holder of the economic rights, and the
successive licensors have only limited liability.
>
>In this respect, the user's attention is drawn to the risks associated with
loading, using, modifying and/or developing or reproducing the software by the
user in light of its specific status of free software, that may mean  that it
is complicated to manipulate, and that also therefore means  that it is
reserved for developers  and  experienced professionals having in-depth
computer knowledge. Users are therefore encouraged to load and test the
software's suitability as regards their requirements in conditions enabling
the security of their systems and/or data to be ensured and, more generally,
to use and operate it in the same conditions as regards security.
>
>The fact that you are presently reading this means that you have had knowledge
of the CeCILL license and that you accept its terms.


**Requirements**
--
The `FIND_CURVE` macro requires `ImageJ v1.49g` or higher ([Download](https://imagej.nih.gov/ij/download.html)).

For ImageJ, the conversion of the analyzed stacks into animated GIFs requires the ([Gif-Stack-Writer Plugin](https://imagej.nih.gov/ij/plugins/gif-stack-writer.html)).


**Files**
--
- [] **src**
    - README.md
    - LICENSE
    - `Installation.ijm`
    - `Installation_FIJI.ijm`
    - [] **doc**
        - *3D.jpg*
        - *Avatar.jpg*
        - *FIJI.jpg*
        - *Identification.jpg*
        - *IJ.jpg*
        - *Logo_cnrs.jpg*
        - *Logo_ens.jpg*
        - *Logo_LBMC.jpg*
        - *Osculating_circle.jpg*
        - *Segment.jpg*
        - *Slice.jpg*
    - [] **macro**
        - `CleanMemory.java`
        - `CloseImage.java`
        - `CNRS.jpg`
        - `ENS.jpg`
        - `Explorer.java`
        - `Find_Curve.java`
        - `HTML_Curve.html`
        - `LBMC.jpg`
        - `macro_AboutPlugin.java`
        - `Main.java`
        - `ROIeraser.java`
        - `Startup_CL.txt`
        - `style_Curve.css`
        - `tableline.html`
        - `Trigo.tif`
        - `UCBL.jpg`


**Installation**
--
The `FIND-CURVE` macro can be automatically installed with all required files in `ImageJ` and `FIJI`. Please follow the specific instructions described below.


![ImageJ Logo](src/doc/IJ.jpg)
---
1. Open `ImageJ`.
2. Open the `src` folder of the `FIND-CURVE` macro.
3. Drag the `Installation.ijm` file on `ImageJ` Menu bar to open it.
4. In the Menu bar of the macro select the `Macros/Run Macro` option.
5. The window will be closed automatically and all required files will be installed in the `ImageJ/macros/Find-Curve` folder. The shortcut `Plugins/Macros/FIND-CURVE` will be added in the Menu bar.
6. Restart `ImageJ` to refresh the Menu bar.


![FIJI Logo](src/doc/FIJI.jpg)
---
1. Open `FIJI`.
2. Open the `src` folder of the `FIND-CURVE` macro.
3. Drag the `Installation_Fiji.ijm` file on `FIJI` Menu bar to open it.
4. In the console select the `Run` option.
5. All required files will be installed in the `Fiji.app/macros/Find-Curve` folder. The shortcut `Plugins/Macros/FIND-CURVE` will be added in the Menu bar.
6. Restart `FIJI` to refresh the Menu bar.


Update
---
Follow the same instructions as for the installation process.
