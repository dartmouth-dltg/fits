#FITS

File Information Tool Set

For official releases and documentation visit http://fitstool.org

This branch contains changes specific for the needs of the Dartmouth College Library. 

##Features Added


###Video File Support

This fork of the harvard-lts/fits repository adds video validation for .mov and .mp4 files using ffmpeg and by extension ffprobe. In order to use the new tool both ffmpeg and ffprobe must be installed. Currently, this has only been tested on Mac OS X 9 and RHEL 6. 

This new tool could be adapted to validate other video formats, as the need arrises.

For more information about ffmpeg: https://www.ffmpeg.org/

###JHOVE

The version of the JHOVE in the Dartmouth branch is a specially updated version of the JHOVE in the official distribution of FITS.  Some key fixes have been backported into this version to support files commonly used at Dartmouth.
