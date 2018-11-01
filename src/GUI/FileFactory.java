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
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private FileFactory() {
		
	}

	public static boolean fileEqual(File file1, File file2) {
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
	
	public static void copyFile(File localfile, File remotefile, JTextPane logtxt) {
		String loginfo = "";
		try {
			FileUtils.copyFile(localfile, remotefile);
			loginfo = df.format(new Date()) + " Copy succeessfully! Copy the new file: " + localfile.getAbsolutePath() + "\n";
			try {
				logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("normal"));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} catch(IOException e) {
			int times =0;
			while(times<3) {
				times++;
				loginfo = df.format(new Date())+ " Copy failed ! Try copy again for " + String.valueOf(times) + " times " + localfile.getAbsoluteFile() +  " \n";
				try {
					logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("red"));
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				remotefile.delete();
					try {
					FileUtils.copyFile(localfile, remotefile);
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				if (times==3) {
					loginfo = df.format(new Date()) + " Copy failed for 3 times!!! Please check it by hand. \n";
					try {
						logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("red"));
					} catch (BadLocationException e3) {
						e3.printStackTrace();
					}
					break;
				}
			}
		}
	}
	
	public static void removeExsitsFile(File remotefile, JTextPane logtxt) {
		
		SimpleDateFormat fdf = new SimpleDateFormat("YYYYMMdd");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String loginfo="";
		File tmpdir = remotefile.getParentFile();
		String projectDirName = tmpdir.getParent();
		while (true) {
			String tmpfilename = tmpdir.getName();
			if (tmpfilename.equals("RAWdata")) {
				projectDirName = tmpdir.getParent();
				break;
			}else {
				tmpdir = tmpdir.getParentFile();
			}
		}
		String filterdirname = projectDirName + "\\filter";
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
	
	public static String getNewFilePath(File file) {
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
	
	public static File getRemoteFile(File lfile, File localDir, File remoteDir, FileType filetype) {
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
			String QCdir = "";
			File tempfile = lfile.getParentFile();
			while (true) {
				String parentname = tempfile.getName();
				if (!parentname.endsWith("_QC")) {
					QCdir = parentname + "\\" + QCdir;
					tempfile = tempfile.getParentFile();
				}else {
					QCdir = parentname + "\\" + QCdir;
					break;
				}
			}
			rfilepath = remoteDirname + "\\project_QC\\" + QCdir + "\\RAWdata\\" + lfile.getName();
			break;
		case processFile:
			String remotesub = remoteDir.getParentFile().getParent();
			rfilepath = lfilepath.replace(localDir.getAbsolutePath(), remotesub);
//			String processingtype = lfile.getParentFile().getName();
//			if (gm.find() && lfilepath.contains("group")) {
//				String groupname = lfile.getParentFile().getParentFile().getName();
//				String projectname = lfile.getParentFile().getParentFile().getParentFile().getName();
//				rfilepath = remoteDirname + "\\" + projectname + "\\" + groupname + "\\" + processingtype + "\\" + filename;
//			}else {
//				File projectfile = lfile.getParentFile().getParentFile();
//				String projectname = projectfile.getName();
//				rfilepath = remoteDirname + "\\" + projectname + "\\" + processingtype + "\\" + filename;
//			}
			break;
		default:
			break;
		}
		return new File(rfilepath);
	}
	
}
