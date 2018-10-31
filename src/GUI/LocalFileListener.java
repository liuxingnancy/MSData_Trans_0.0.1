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
	
	private File localFile;
	private File remoteFile;
	private long timeout;
	private JTextPane logtxt;
	
	public LocalFileListener (String localFile, String remoteFile, long timeout, JTextPane logtxt) {
		this(new File(localFile), new File(remoteFile), timeout, logtxt);
	}
	public LocalFileListener (File localFile, File remoteFile, long timeout, JTextPane logtxt) {
		this.localFile = localFile;
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
		File remotefile = FileFactory.getRemoteFile(file, localFile, remoteFile, filetype);
		FileEntry fileEntry = new FileEntry(file);
		fileEntry.refresh(file);
		
		if (remotefile.exists()) {
			FileFactory.removeExsitsFile(remotefile, logtxt);
			while (true) {
				if(!fileEntry.refresh(file, timeout)) {
					FileFactory.copyFile(file, remotefile, logtxt);
					break;
				}
			}
		}
		
	}

	@Override
	public void onFileCreate(File file) {
		String filepath = file.getAbsolutePath();
		FileType filetype = filepath.contains("_QC")? FileType.QCFile : FileType.projectFile ;
		File remotefile = FileFactory.getRemoteFile(file, localFile, remoteFile, filetype);
		FileEntry fileEntry = new FileEntry(file);
		fileEntry.refresh(file);
		
		if (remotefile.exists()) {
			FileFactory.removeExsitsFile(remotefile, logtxt);
		}
		Runnable copyRunnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (!fileEntry.refresh(file, timeout)) {
						FileFactory.copyFile(file, remotefile, logtxt);
						break;
					}
				}
			}
		};
		Thread copyThread = new Thread(copyRunnable);
		copyThread.run();
			
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
