package GUI;

import java.io.File;
import javax.swing.JTextPane;
import monitor.FileAlterationListener;
import monitor.FileAlterationObserver;
import monitor.FileEntry;

/**
 * 
 * @author liuxing2
 * @email liuxing2@genomics.cn
 * @date 2018_10_30
 *
 */

public class LocalFileListener implements FileAlterationListener{
	
	private File remoteFile;
	private long timeout;
	private JTextPane logtxt;
	private FileFactory filefactory = new FileFactory();
	
	public LocalFileListener (String remoteFile, long timeout, JTextPane logtxt) {
		this(new File(remoteFile), timeout, logtxt);
	}
	public LocalFileListener (File remoteFile, long timeout, JTextPane logtxt) {
		this.remoteFile = remoteFile;
		this.timeout = timeout;
		this.logtxt = logtxt;
	}
	
	@Override
	public void onDirectoryChange(File file) {			
	}

	@Override
	public void onDirectoryCreate(File file) {
	}

	@Override
	public void onDirectoryDelete(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFileChange(File file) {
		String filepath = file.getAbsolutePath();
		FileType filetype = filepath.contains("_QC")? FileType.QCFile : FileType.projectFile ;
		File remotefile = filefactory.getRemoteFile(file, remoteFile, filetype);
		FileEntry fileEntry = new FileEntry(file);
		fileEntry.refresh(file);
		
		if (remotefile.exists()) {
			filefactory.removeExsitsFile(remotefile, logtxt);
		}
		while (true) {
			if (!fileEntry.refresh(file, timeout)) {
				if (! remotefile.getParentFile().exists()){
					remotefile.getParentFile().mkdirs();
				}
				filefactory.copyFile(file, remotefile, logtxt);
			}
			break;
		}
	}

	@Override
	public void onFileCreate(File file) {
		String filepath = file.getAbsolutePath();
		FileType filetype = filepath.contains("_QC")? FileType.QCFile : FileType.projectFile ;
		File remotefile = filefactory.getRemoteFile(file, remoteFile, filetype);
		FileEntry fileEntry = new FileEntry(file);
		fileEntry.refresh(file);
		
		if (remotefile.exists()) {
			filefactory.removeExsitsFile(remotefile, logtxt);
		}
		
		while (true) {
			if (!fileEntry.refresh(file, timeout)){
				filefactory.copyFile(file, remotefile, logtxt);
				break;
			}
		}
			
	}


	@Override
	public void onFileDelete(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart(FileAlterationObserver fileAlterationObserver) {
		
	}

	@Override
	public void onStop(FileAlterationObserver fileAlterationObserver) {
		// TODO Auto-generated method stub
		
	}

}
