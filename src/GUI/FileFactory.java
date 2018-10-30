package GUI;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.JTextPane;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author liuxing
 * @email liuxing2@genomics.cn
 * @date 2018_10_30
 *
 */

public class FileFactory {

	public boolean fileEqual(File file1, File file2) {
		boolean equal = true;
		if (!file1.exists() || !file2.exists()) {
			equal = false;
		}else if (file1.getName().equals(file2.getName()) && file1.lastModified() == file2.lastModified() &&
				file1.length() == file2.length()) {
			equal = true;
		}else {
			equal = false;
		}
		return equal;
	}
	
	public void copyFile(File localfile, File remotefile, JTextPane logtxt) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String loginfo;

		loginfo = df.format(new Date()) + " Copy the new file: " + localfile.getAbsolutePath() + "\n";
		try {
			logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("normal"));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		try {
			FileUtils.copyFile(localfile, remotefile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		verifyCopy(localfile, remotefile, logtxt);
	}
	
	
	public void verifyCopy(File localfile, File remotefile, JTextPane logtxt) {
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String loginfo="";

		boolean copysuccess = fileEqual(localfile, remotefile);
		int times=0;
		if(copysuccess) {
			loginfo = df.format(new Date()) + " Copy succeed! \n";
			try {
				logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("normal"));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		while(!copysuccess) {
			times++;
			loginfo = df.format(new Date())+ " Copy failed ! Try copy again for " + String.valueOf(times) + " times " + localfile.getAbsoluteFile() +  " \n";
			try {
				logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("red"));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			remotefile.delete();
			try {
				FileUtils.copyFile(localfile, remotefile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (times>=3) {
				loginfo = df.format(new Date()) + " Copy failed for 3 times!!! Please check it by hand. \n";
				try {
					logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("red"));
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	public void removeExsitsFile(File remotefile, JTextPane logtxt) {
		
		SimpleDateFormat fdf = new SimpleDateFormat("YYYYMMdd");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String loginfo="";
		
		String filterdirname = remotefile.getParent() + "\\filter";
		File filterdir = new File(filterdirname);
		File filterfile = new File(filterdirname + "\\" + remotefile.getName());
		if (filterfile.exists()) {
			long time = filterfile.lastModified();
			String childrendirname = filterdirname + "\\" + fdf.format(new Date(time));
			loginfo = df.format(new Date()) + " Move exists filter file to new directory: " + filterfile.getAbsolutePath() + "\n";
			try {
				logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("normal"));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			File childrendir = new File(childrendirname);
			try {
				FileUtils.moveFileToDirectory(filterfile, childrendir, true);
			} catch (IOException e) {
				e.printStackTrace();
			}				
		}

		loginfo = df.format(new Date()) + " Move the exists file to filter directory: " + remotefile.getAbsolutePath() +"\n";
		try {
			logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("normal"));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		try {
			FileUtils.moveFileToDirectory(remotefile, filterdir, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
							
	}
	
	public String getNewFilePath(File file) {
		String filename = file.getName();
		File[] filelist = file.getParentFile().listFiles(new FileFilter() {
			public boolean accept(File listfile) {
				return listfile.getName().indexOf(filename) != -1;
			}			
		});
		int subfilenumber = filelist.length + 1;
		String[] filenamesplit = filename.split("\\.", 2);
		String newfilename = filenamesplit[0] + "_sub" + String.valueOf(subfilenumber) + "." + filenamesplit[1];
		return file.getParent() + "\\" + newfilename;
	}
	
	public File getRemoteFile(File lfile, File remoteDir, FileType filetype) {
		String filename = lfile.getName();
		String lfilepath = lfile.getAbsolutePath();
		String remoteDirname = remoteDir.getAbsolutePath();
		String rfilepath = "";
		String gpattern = "[^_\\s]+_(\\d+)_\\d+.wiff";
		Pattern gr = Pattern.compile(gpattern);
		Matcher gm = gr.matcher(filename);

		switch(filetype) {
		case projectFile:
			String pfilename = lfile.getParentFile().getName();						
			if (!gm.find()) {
				rfilepath = remoteDirname + "\\" + pfilename + "\\RAWdata\\" + filename;
			}else {
				String groupname = "group"+gm.group(1);
				rfilepath = remoteDirname + "\\" +pfilename + "\\" + groupname +"\\RAWdata\\"  + filename;
			}
			break;
		case QCFile:
			rfilepath = remoteDirname + "\\project_QC\\" + lfile.getParentFile().getName() + "\\RAWData\\" + lfile.getName();
			break;
		case processFile:
			String processingtype = lfile.getParentFile().getName();
			if (gm.find() && lfilepath.contains("group")) {
				String groupname = lfile.getParentFile().getParentFile().getName();
				String projectname = lfile.getParentFile().getParentFile().getParentFile().getName();
				rfilepath = remoteDirname + "\\" + projectname + "\\" + groupname + "\\" + processingtype + "\\" + filename;
			}else {
				File projectfile = lfile.getParentFile().getParentFile();
				String projectname = projectfile.getName();
				rfilepath = remoteDirname + "\\" + projectname + "\\" + processingtype + "\\" + filename;
			}
			break;
		default:
			break;
		}
		return new File(rfilepath);
	}
	
}
