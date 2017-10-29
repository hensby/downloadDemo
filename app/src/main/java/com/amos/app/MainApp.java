package com.amos.app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import com.amos.download.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author yangxiaolong
 * @2014-5-6
 */
public class MainApp extends Activity implements OnClickListener {

	private static final String TAG = MainApp.class.getSimpleName();


	private TextView mMessageView;

	private ProgressBar mProgressbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progress_activity);
		findViewById(R.id.download_btn).setOnClickListener(this);
		mMessageView = (TextView) findViewById(R.id.download_message);
		mProgressbar = (ProgressBar) findViewById(R.id.download_progress);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.download_btn) {
			doDownload();
		}
	}

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			mProgressbar.setProgress(msg.getData().getInt("size"));

			float temp = (float) mProgressbar.getProgress()
					/ (float) mProgressbar.getMax();

			int progress = (int) (temp * 100);
			if (progress == 100) {
				Toast.makeText(MainApp.this, "successful", Toast.LENGTH_LONG).show();
			}
			mMessageView.setText("downloading:" + progress + " %");

		}
	};

	private void doDownload() {

		String path = Environment.getExternalStorageDirectory()
				+ "/amosdownload/";
		File file = new File(path);

		if (!file.exists()) {
			file.mkdir();
		}

		mProgressbar.setProgress(0);


		String downloadUrl = "http://gdown.baidu.com/data/wisegame/91319a5a1dfae322/baidu_16785426.apk";
		String fileName = "baidu_16785426.apk";
		int threadNum = 5;
		String filepath = path + fileName;
		Log.d(TAG, "download file  path:" + filepath);
		downloadTask task = new downloadTask(downloadUrl, threadNum, filepath);
		task.start();
	}


	class downloadTask extends Thread {
		private String downloadUrl;
		private int threadNum;
		private String filePath;
		private int blockSize;

		public downloadTask(String downloadUrl, int threadNum, String fileptah) {
			this.downloadUrl = downloadUrl;
			this.threadNum = threadNum;
			this.filePath = fileptah;
		}

		@Override
		public void run() {

			FileDownloadThread[] threads = new FileDownloadThread[threadNum];
			try {
				URL url = new URL(downloadUrl);
				Log.d(TAG, "download file http path:" + downloadUrl);
				URLConnection conn = url.openConnection();

				int fileSize = conn.getContentLength();
				if (fileSize <= 0) {
					System.out.println("failed");
					return;
				}

				mProgressbar.setMax(fileSize);


				blockSize = (fileSize % threadNum) == 0 ? fileSize / threadNum
						: fileSize / threadNum + 1;

				Log.d(TAG, "fileSize:" + fileSize + "  blockSize:"+blockSize);

				File file = new File(filePath);
				for (int i = 0; i < threads.length; i++) {

					threads[i] = new FileDownloadThread(url, file, blockSize,
							(i + 1));
					threads[i].setName("Thread:" + i);
					threads[i].start();
				}

				boolean isfinished = false;
				int downloadedAllSize = 0;
				while (!isfinished) {
					isfinished = true;

					downloadedAllSize = 0;
					for (int i = 0; i < threads.length; i++) {
						downloadedAllSize += threads[i].getDownloadLength();
						if (!threads[i].isCompleted()) {
							isfinished = false;
						}
					}

					Message msg = new Message();
					msg.getData().putInt("size", downloadedAllSize);
					mHandler.sendMessage(msg);
					// Log.d(TAG, "current downloadSize:" + downloadedAllSize);
					Thread.sleep(1000);
				}
				Log.d(TAG, " all of downloadSize:" + downloadedAllSize);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

}
