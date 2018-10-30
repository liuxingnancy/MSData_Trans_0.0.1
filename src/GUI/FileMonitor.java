package GUI;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

import monitor.*;

/**
 * 
 * @author liuxing
 * @email liuxing2@genomics.cn
 * @date 2018_10_30
 *
 */

public class FileMonitor {

	private File localfile;
	private File remotefile;
	private File processingfile;
	private long monitortimeout;
	private long fileChangeCheckTimeout;
	private FileFilter processingfilefilter;
	private FileAlterationMonitor filemonitor;
	private JTextPane logtxt;
	private SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	private FileFactory filefactory;
	
	
	public FileMonitor(String localfile, String remotefile, String processingfile, long monitortimeout, long fileChangeCheckTimeout, JTextPane logtxt) {
		this (new File(localfile), new File(remotefile), new File(processingfile), monitortimeout, fileChangeCheckTimeout, logtxt);
	}
	
	public FileMonitor(File localfile, File remotefile, File processingfile, long monitortimeout, long fileChangeCheckTimeout, JTextPane logtxt){
		this.localfile = localfile;
		this.remotefile = remotefile;
		this.processingfile = processingfile;
		this.monitortimeout = monitortimeout;
		this.fileChangeCheckTimeout = fileChangeCheckTimeout;
		this.logtxt = logtxt;
		this.processingfilefilter = new RAWDataFileFilter();
		this.filemonitor = new FileAlterationMonitor(this.monitortimeout*1000);
		LocalFileListener filelistener = new LocalFileListener(this.remotefile, this.fileChangeCheckTimeout, this.logtxt);
		FileAlterationObserver fileobserver = new FileAlterationObserver(this.localfile, this.fileChangeCheckTimeout);
		fileobserver.addListener(filelistener);
		ProcessingFileListener processingfilelistener = new ProcessingFileListener(this.remotefile, this.fileChangeCheckTimeout, this.logtxt);
		FileAlterationObserver processingfileobserver = new FileAlterationObserver(this.processingfile, this.processingfilefilter, this.fileChangeCheckTimeout);
		processingfileobserver.addListener(processingfilelistener);
		this.filemonitor.addObserver(fileobserver);
		this.filemonitor.addObserver(processingfileobserver);
		this.filefactory = new FileFactory();
	}
	
	public class QCfileFilter implements FileFilter {

		@Override
		public boolean accept(File file) {
			String filepath = file.getAbsolutePath();
			return filepath.contains("_QC");
		}		
	}
	
	public class projectFileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			String filepath = file.getAbsolutePath();
			return !filepath.contains("_QC");
		}
	}
	
	public class RAWDataFileFilter implements FileFilter {
		
		@Override
		public boolean accept(File file) {
			String filepath = file.getAbsolutePath();
			return !filepath.contains("RAWdata") && !filepath.contains("filter");
		}
	}
	
	public void checkLocal() {
		List<File> qcfiles = new ArrayList<File>();
		qcfiles = listfiles(this.localfile, qcfiles, new QCfileFilter());
		List<File> localprojectfiles = new ArrayList<File>();
		localprojectfiles = listfiles(this.localfile, localprojectfiles, new projectFileFilter());

		for (File lfile: localprojectfiles) {
			File rfile = filefactory.getRemoteFile(lfile, this.remotefile, FileType.projectFile);
			if (!filefactory.fileEqual(lfile, rfile)) {
				if (rfile.exists()) {
					filefactory.removeExsitsFile(rfile, logtxt);
				}
				filefactory.copyFile(lfile, rfile, logtxt);
			}
		}
		
		for (File qcfile : qcfiles) {
			File remoteQCfile = filefactory.getRemoteFile(qcfile, this.remotefile, FileType.QCFile);
			if (!filefactory.fileEqual(qcfile, remoteQCfile)) {
				if (remoteQCfile.exists()) {
					filefactory.removeExsitsFile(remoteQCfile, logtxt);
				}
				filefactory.copyFile(qcfile, remoteQCfile, logtxt);
			}
		}
	}
	
	public void checkProcess() {
		List<File> processfiles = new ArrayList<File>();
		processfiles = listfiles(this.processingfile, processfiles, this.processingfilefilter);
		for (File processfile : processfiles) {
			File rfile = filefactory.getRemoteFile(processfile, this.remotefile, FileType.processFile);
			if (!filefactory.fileEqual(processfile, rfile)) {
				filefactory.copyFile(processfile, rfile, logtxt);
			}
		}
	}
	
	public void start(){
		String loginfo = "";
		loginfo = df.format(new Date()) + " Check the local directory and copy the new generated files ! \n";
		try {
			logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("blue"));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		checkLocal();
		loginfo = df.format(new Date()) + " Check the processing directory and copy the new generated files ! \n";
		try {
			logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("blue"));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		checkProcess();
		try {
			this.filemonitor.start();
			loginfo = df.format(new Date()) +" File monitor start !\n";
			logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("blue"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop(){
		
		try {
			this.filemonitor.stop();
			String loginfo = df.format(new Date()) +" File monitor stop !\n";
			logtxt.getDocument().insertString(0, loginfo, logtxt.getStyle("blue"));
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}
	
	public List<File> listfiles(File file, List<File> filelists, FileFilter filefilter) {
		if (file.isDirectory()) {
			for (File scanfile: file.listFiles(filefilter)) {
				if (scanfile.isFile()) {
					filelists.add(scanfile);
				}else {
					listfiles(scanfile, filelists, filefilter);
				}
			}
		}
		return filelists;
	}

}
