package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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
	
	public static final List<MachineID> withDATAmachines = new ArrayList<MachineID>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		add(MachineID.M4500);
		add(MachineID.M5500);
		add(MachineID.M5600);
		add(MachineID.M6500);
	}};
	
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
		copyFile(localfile, remotefile, logtxt, 0);
	}
	
	private static void copyFile(File localfile, File remotefile, JTextPane logtxt, int times) {
		String loginfo = "";
		try {
			FileUtils.copyFile(localfile, remotefile);
			loginfo = df.format(new Date()) + " Copy succeessfully! Copy the new file: " + localfile.getAbsolutePath() + " to " + remotefile.getAbsolutePath() + "\n";
			try {
				logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("normal"));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} catch(IOException e) {
			if (times <3) {
				times++;
				loginfo = df.format(new Date())+ " Copy failed ! Try copy again for " + String.valueOf(times) + " times " + localfile.getAbsoluteFile() + " to " + remotefile.getAbsolutePath() + " \n";
				try {
					logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("red"));
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				remotefile.delete();
				copyFile(localfile, remotefile, logtxt, times);
			}else if (times==3) {
				loginfo = df.format(new Date()) + " Copy failed for 3 times!!! Please check it by hand. \n";
				try {
					logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("red"));
				} catch (BadLocationException e2) {
					e2.printStackTrace();
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
		String filterdirname = projectDirName + File.separator + "filter";
		File filterdir = new File(filterdirname);
		File filterfile = new File(filterdirname + File.separator + remotefile.getName());
		if (filterfile.exists()) {
			long time = filterfile.lastModified();
			String childrendirname = filterdirname + File.separator + fdf.format(new Date(time));
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
		return file.getParent() + File.separator + newfilename;
	}
	
	public static File getProcessRemoteFile(File pfile, File pdir, File remotedir) {
		String remotesub = remotedir.getAbsolutePath();
		String rfilepath = pfile.getAbsolutePath().replace(pdir.getAbsolutePath(), remotesub);
		return new File(rfilepath);
	}
	
	public static File getProteinRemoteFile(File lfile, File localDir, File remoteDir, String projectid, int groupnumber, FileType filetype) {
		String lfilepath = lfile.getAbsolutePath();
		String rfilepath = "";
		String remoteDirpath= remoteDir.getAbsolutePath();
		switch(filetype) {
		case projectFile_single:
			rfilepath = remoteDirpath + File.separator + projectid + File.separator + "RAWdata" + File.separator + lfile.getName();
			//System.out.println(rfilepath);
			break;
		case projectFile_group:
			String groupname = "group" + String.valueOf(groupnumber);
			rfilepath = remoteDirpath + File.separator + projectid + File.separator + groupname + File.separator + "RAWdata" + File.separator + lfile.getName();
			break;
		case QCFile:
			String QCdir = "";
			File tempfile = lfile.getParentFile();
			String remoteQCdir = "";
			while (true) {
				QCdir = tempfile.getAbsolutePath();
				if (!QCdir.endsWith("_QC")) {
					tempfile = tempfile.getParentFile();
				}else {
					remoteQCdir = remoteDirpath + File.separator + "Project_QC" + File.separator + tempfile.getName() + File.separator + projectid + File.separator + "RAWdata";
					break;
				}
			}
			rfilepath = lfilepath.replace(tempfile.getAbsolutePath() + File.separator + projectid, remoteQCdir);
			break;
		case processFile:
			String remotesub = remoteDir.getAbsolutePath();
			rfilepath = lfilepath.replace(localDir.getAbsolutePath(), remotesub);
			break;
		default:
			break;
		}
		return new File(rfilepath);
	}
	
	public static File getProteinRemoteFile(File lfile, File localDir, File remoteDir, String projectid, FileType filetype) {
		return getProteinRemoteFile( lfile, localDir, remoteDir, projectid, 0, filetype);
	}
	
	public static File getProteinRemoteFile(File lfile, File localDir, File remoteDir, int groupid, FileType filetype) {
		return getProteinRemoteFile( lfile, localDir, remoteDir, null, groupid, filetype);
	}
	
	public static File getProteinRemoteFile(File lfile, File localDir, File remoteDir, FileType filetype) {
		return getProteinRemoteFile( lfile, localDir, remoteDir, null, 0, filetype);
	}
	
	public static File getOtherRemoteFile(File lfile, File localDir, File remoteDir, String remoteParentDir) {
		String localfilename = lfile.getName();
		String localfilepath = lfile.getAbsolutePath();
		String remoteDirpath = remoteDir.getAbsolutePath();
		String rfilepath = remoteDirpath + File.separator + remoteParentDir + File.separator + "RAWdata";
		String rawfile = null;
		String posOrnegfile = null;		
		if (localfilepath.contains("_NEG")) {
			posOrnegfile = "neg";
		}else if (localfilepath.contains("_POS")) {
			posOrnegfile = "pos";
		}
		if (localfilepath.contains(".raw/") || localfilepath.contains(".d/") || localfilepath.contains(".D/")) {
			rawfile = lfile.getParent();
		}
		if (posOrnegfile != null) {
			rfilepath += File.separator + posOrnegfile;
		}
		if (rawfile != null) {
			rfilepath += File.separator + rawfile;
		}
		rfilepath += File.separator + localfilename;
		return new File(rfilepath);
	}
	
	public static HashMap<String, SampleFiles> readProteinSampleList(File samplelistfile) {
		HashMap<String, SampleFiles> samplehash = new HashMap<String, SampleFiles>();
		SampleFiles samplefiles = null;
		BufferedReader filereader = null;
		String line;
		String projectid;
		String sampleid;
		FileType filetype;
		int groupnumber;
		int filenumber;
		String patternstring = "([^_\\s]+)_(\\d+)";
		Pattern pattern = Pattern.compile(patternstring);
		try {
			filereader = new BufferedReader(new FileReader(samplelistfile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while ((line = filereader.readLine()) != null) {
				String[] num = line.split("\\s+");
				if (num.length <2) {
					num = line.split("\\t");
				}
				projectid = num[0].trim();
				sampleid = num[1].trim();
				filenumber = 0;
				Matcher matcher = pattern.matcher(num[1].trim());
				if (matcher.find()) {
					filetype = FileType.projectFile_group;
					groupnumber = Integer.parseInt(matcher.group(2));
				}else {
					filetype = FileType.projectFile_single;
					groupnumber = 0;
				}
				samplefiles = new SampleFiles(projectid, sampleid, groupnumber, filetype, filenumber);
				samplehash.put(sampleid, samplefiles);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return samplehash;
		
	}
	
	public static HashMap<String, String> readOtherSampleList(File samplelistfile){
		HashMap<String, String> samplehash = new HashMap<String, String>();
		BufferedReader filereader = null;
		try {
			filereader = new BufferedReader(new FileReader(samplelistfile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line;
		String transdir;
		String filemark;
		try {
			while (!(line = filereader.readLine()).equals(null) && !line.startsWith("Projectid") && !line.startsWith("projectid")) {
				String[] num = line.split("\\s+");
				if (num.length <2) {
					num = line.split("\\t");
				}
				if (num.length < 2) {
					continue;
				}
				transdir = num[0].trim();
				filemark = num[1].trim();
				samplehash.put(filemark, transdir);
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return samplehash;
	}
	
	public static SampleFiles ProteinSampleFilesFind(File file, HashMap<String, SampleFiles> samplehash, JTextPane logtxt) {
		String loginfo = "";
		Set<String> samplelist = samplehash.keySet();
		//System.out.println(samplelist);
		Iterator<String> samplelistiterator = samplelist.iterator();
		String sampleid = null;
		String filepath = file.getAbsolutePath();
		SampleFiles samplefiles = null;
		while (samplelistiterator.hasNext()) {
			sampleid = samplelistiterator.next();
			if (filepath.contains(sampleid)) {
				samplefiles = samplehash.get(sampleid);
			}
		}
		if (samplefiles == null) {
			loginfo = df.format(new Date()) + " No corresponding smpleid in samplelist for file: " + filepath +"\n";
			//System.out.println(sampleid);
			try {
				logtxt.getDocument().insertString( 0, loginfo, logtxt.getStyle("red"));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return samplefiles;
	}
	
	public static String otherTransPathFind(File file, HashMap<String, String> samplehash, JTextPane logtxt) {
		String loginfo = "";
		Set<String> filemarklist = samplehash.keySet();
		Iterator<String> filemarkiterator = filemarklist.iterator();
		String filemark = null;
		String filepath = file.getAbsolutePath();
		String transdir = null;
		while(filemarkiterator.hasNext()) {
			filemark = filemarkiterator.next();
			if (filepath.contains(filemark)) {
				transdir = samplehash.get(filemark);
			}
		}
		if (transdir == null) {
			loginfo = df.format(new Date()) + " No corresponding filemark in samplelist for file: " + filepath + "\n";
			try {
				logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("red"));
			}catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return transdir;
	}
	
	public static List<File> listfiles(File file, List<File> filelists, String containString, String filterString) {
		if (file.isDirectory()) {
			for (File scanfile: file.listFiles()) {
				if (scanfile.isFile()) {
					if (scanfile.getAbsolutePath().contains(containString) && !scanfile.getAbsolutePath().contains(filterString)) {
						filelists.add(scanfile);
					}
				}else {
					listfiles(scanfile, filelists, containString, filterString);
				}
			}
		}
		return filelists;
		
	}
	
	public static List<File> listfiles(File file, List<File> filelists, String containString, String filterString, boolean contain) {	
		if (file.isDirectory()) {
			for (File scanfile: file.listFiles()) {
				if (scanfile.isFile()) {
					if ((contain && scanfile.getAbsolutePath().contains(containString) && scanfile.getAbsolutePath().contains(filterString)) || 
							(!contain && !scanfile.getAbsolutePath().contains(containString) && !scanfile.getAbsolutePath().contains(filterString))) {
						filelists.add(scanfile);
					}
				}else {
					listfiles(scanfile, filelists, containString, filterString, contain);
				}
			}
		}
		return filelists;
	}
	
	public static List<File> listfiles(File file, List<File> filelists, String filterString, boolean contain){
		if (file.isDirectory()) {
			for (File scanfile: file.listFiles()) {
				if (scanfile.isFile()) {
					if ((contain && scanfile.getAbsolutePath().contains(filterString)) || (!contain && ! scanfile.getAbsolutePath().contains(filterString))){ 
						filelists.add(scanfile);
					}
				}else {
					listfiles(scanfile, filelists, filterString, contain);
				}
			}
		}
		return filelists;
	}
	
	public static List<File> listfiles(File file, List<File> filelists){
		if (file.isDirectory()) {
			for (File scanfile: file.listFiles()) {
				if (scanfile.isFile()) {
					filelists.add(scanfile);
				}else {
					listfiles(scanfile, filelists);
				}
			}
		}
		return filelists;
	}
	
	public static HashMap<String, String> listfilenames(File file, HashMap<String, String> filenamelists){
		if (file.isDirectory()) {
			for (File scanfile: file.listFiles()) {
				if (scanfile.isFile()) {
					filenamelists.put(scanfile.getName(), scanfile.getAbsolutePath());
				}else {
					listfilenames(scanfile, filenamelists);
				}
			}
		}
		return filenamelists;
	}
	
	public static HashMap<String, String> listfilenames(File file, HashMap<String, String> filenamelists, FileFilter filefilter){
		if (file.isDirectory()) {
			for (File scanfile: file.listFiles(filefilter)) {
				if (scanfile.isFile()) {
					filenamelists.put(scanfile.getName(), scanfile.getAbsolutePath());
				}else {
					listfilenames(scanfile, filenamelists, filefilter);
				}
			}
		}
		return filenamelists;
	}
	
	public static boolean isTransfer(File file, MachineID machineID) {
		String filepath = file.getAbsolutePath();
		boolean istransfer = false;
		if (withDATAmachines.contains(machineID)){
			if (filepath.contains("DATA") || filepath.contains("_QC")) {
				istransfer = true;
			}else {
				istransfer = false;
			}
		}else {
			istransfer = true;
		}
		return istransfer;
	}
	
}
