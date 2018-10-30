package GUI;

import java.io.File;
import javax.swing.JTextPane;
import monitor.FileAlterationListener;
import monitor.FileAlterationObserver;
import monitor.FileEntry;

/**
 * 
 * @author liuxing
 * @email liuxing2@genomics.cn
 * @date 2018_10_30
 *
 */

public class ProcessingFileListener implements FileAlterationListener {
	
	private File remoteFile;
	private long timeout;
	private JTextPane logtxt;
	private FileFactory filefactory = new FileFactory();

	public ProcessingFileListener (String remoteFile, long timeout, JTextPane logtxt) {
		this(new File(remoteFile), timeout, logtxt);
	}
	public ProcessingFileListener (File remoteFile, long timeout, JTextPane logtxt) {
		this.remoteFile = remoteFile;
		this.timeout = timeout;
		this.logtxt = logtxt;
	}
	
	@Override
	public void onStart(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDirectoryCreate(File directory) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDirectoryChange(File directory) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDirectoryDelete(File directory) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onFileCreate(File file) {
		File remotefile = filefactory.getRemoteFile(file, this.remoteFile, FileType.processFile);
		FileEntry fileEntry = new FileEntry(file);
		fileEntry.refresh(file);
		while (true) {
			if (!fileEntry.refresh(file, this.timeout)) {
				filefactory.copyFile(file, remotefile, logtxt);
				break;
			}
		}
		
	}
	@Override
	public void onFileChange(File file) {
		File remotefile = filefactory.getRemoteFile(file, this.remoteFile, FileType.processFile);
		FileEntry fileEntry = new FileEntry(file);
		fileEntry.refresh(file);
		while (true) {
			if (!fileEntry.refresh(file, this.timeout)) {
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
	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		
	}

}
