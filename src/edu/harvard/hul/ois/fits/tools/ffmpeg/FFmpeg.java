/* 
 * Copyright 2015 Trustees of Dartmouth College
 * 
 * This file is part of FITS (File Information Tool Set).
 *
 * This class adds FFmpeg as a tool to FITS. The main purpose of 
 * adding FFmpeg is to identify and validate video files, thought tools 
 * like ffmpeg and ffprobe could be used to provide other valuable 
 * information about the file itself (ex: frame rate, codec). 
 *
 * FFmpeg binary executables were added to the FITS for both 32-bit and 
 * 64-bit Linux and Mac OS X. Windows executables are not added, but 
 * could be at a later point. Currently the version number of FFmpeg in use is 
 * 2.5.3
 *
 * TODO: Add something about ffmpeg licensing.
 *
 * 
 * FITS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FITS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FITS.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hul.ois.fits.tools.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.exceptions.FitsException;
import edu.harvard.hul.ois.fits.exceptions.FitsToolCLIException;
import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import edu.harvard.hul.ois.fits.tools.ToolBase;
import edu.harvard.hul.ois.fits.tools.ToolInfo;
import edu.harvard.hul.ois.fits.tools.ToolOutput;
import edu.harvard.hul.ois.fits.tools.utils.CommandLine;

/** 
 *  The glue class for invoking FFmpeg under FITS.
 *
 * 	The purpose adding FFmpeg to FITS is to identify and validate video files. This class 
 * 	generates FITS XML and FFprobe XML. The FFprobe XML is transformed to use the
 * 	FITS schema.
 *
 * 	FFmpeg, as used in this class is limited to identifiying and validating video files 
 * 	ONLY. Thought it could be used to validate other formats (image and audio), in 
 * 	the future. 
 *
 * 	There is logic for running this tool in Linux and Mac OS X machines, not Windows.
 * 
 */
public class FFmpeg extends ToolBase {

	public final static String XSLT = Fits.FITS_XML + "ffmpeg/ffmpeg_to_fits.xslt";

	private final static String MAC_64BIT	= Fits.FITS_TOOLS + "ffmpeg/Mac_OS_X/64bit/";
	private final static String MAC_32BIT	= Fits.FITS_TOOLS + "ffmpeg/Mac_OS_X/32bit/";
	private final static String LINUX_64BIT	= Fits.FITS_TOOLS + "ffmpeg/Linux/64bit/";
	private final static String LINUX_32BIT	= Fits.FITS_TOOLS + "ffmpeg/Linux/32bit/";
	private String ffmpegBuild;

	private final static String TOOL_NAME = "FFmpeg";
	private boolean enabled = true;

    private static final Logger logger = Logger.getLogger(FFmpeg.class);

	public FFmpeg() throws FitsException {
        logger.debug ("Initializing ffmpeg");

		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");

		info = new ToolInfo();
		info.setName(TOOL_NAME);

		// Figure out which ffmpeg executable to run.
		if (osName.startsWith("Mac OS X")) {
			if (osArch.contains("64") || osArch.contains("86")) {
				ffmpegBuild = MAC_64BIT;
				info.setNote("FFmpeg for Mac OS X 64 bit");
				logger.debug("FFmpeg will use Mac OS X 64 bit executable.");
			} else {
				ffmpegBuild = MAC_32BIT;
				info.setNote("FFmpeg for Mac OS X 32 bit");
				logger.debug("FFmpeg will use Mac OS X 32 bit executable.");
			}
		} else if (osName.startsWith("Linux")){
			if (osArch.contains("64") || osArch.contains("86")){
				ffmpegBuild = LINUX_64BIT;
				info.setNote("FFmpeg for Linux 64 bit");
            	logger.debug("FFmpeg will use Linux 64 bit executable.");
            } else {
            	ffmpegBuild = LINUX_32BIT;
				info.setNote("FFmpeg for Linux 32 bit");
            	logger.debug("FFmpeg will use Linux 32 bit executable.");
            }
		} else {
		    logger.error ("Could not find executables for " + osName + " " + osArch + "not running ffmpeg.");
			throw new FitsToolException("FFmpeg cannot be used on this system");
		}

		// Extract version number. Each tool needs to set the version number in the constructor.
		List<String> versionCommand = new ArrayList<String>(
			Arrays.asList (ffmpegBuild + "ffprobe", "-hide_banner", "-show_entries", 
				"program_version=version", "-print_format", "default=nw=1:nk=1"));
		info.setVersion(CommandLine.exec(versionCommand, null).trim());

	}

	// Called by FITS to invoke the tool against the input file. It must return a ToolOutput object
	// containing valid FITS XML.
	public ToolOutput extractInfo(File file) throws FitsToolException {
        logger.debug("Exiftool.extractInfo starting on " + file.getName());
		long startTime = System.currentTimeMillis();

		// Validating file.
		List<String> valCommand = new ArrayList<String>(
			Arrays.asList(ffmpegBuild + "ffmpeg", "-v", "error", 
				"-i", file.getPath(), "-f", "null", "-"));

		// Extracting identification information.
		List<String> idenCommand = new ArrayList<String>(
			Arrays.asList(ffmpegBuild + "ffprobe", "-v", "quiet", "-show_format", 
				"-show_error", "-show_program_version", "-print_format", "xml", file.getPath()));
		
		logger.debug("Launching ffmpeg, command = " + valCommand);
		String valOut = CommandLine.exec(valCommand, null);
		
		logger.debug("Launching ffmpeg, command = " + idenCommand);
		String idenOut = CommandLine.exec(idenCommand, null);
		
		logger.debug("Finished running FFmpeg");
		
		Document ffprobeXML = createXml(idenOut, valOut);

		Document fitsXML = transform(XSLT, ffprobeXML);

		output = new ToolOutput(this, fitsXML, ffprobeXML);
		
		duration = System.currentTimeMillis() - startTime;
		runStatus = RunStatus.SUCCESSFUL;
        logger.debug("FFmpeg.extractInfo finished on " + file.getName());
		return output;
	}
	
	private Document createXml(String ffprobeOut, String ffmpegOut) throws FitsToolException {    	
    	
    	Document ffprobeXML = null;
		try {
			ffprobeXML = saxBuilder.build(new StringReader(ffprobeOut));
		} catch (Exception e) {
		    logger.debug("Error parsing ffprobe XML Output: " + e.getClass().getName());
			throw new FitsToolException("Error parsing ffprobe XML Output", e);
		}

		if (!ffmpegOut.isEmpty()){ // or check if its null?
			Element error = ffprobeXML.getRootElement().getChild("error");
			if (error == null){
				error = new Element("error");
				error.setAttribute(new Attribute("string", ffmpegOut));
				ffprobeXML.getRootElement().addContent(error);
			}
		}
		
        return ffprobeXML;
    }

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		enabled = value;		
	}
	
}