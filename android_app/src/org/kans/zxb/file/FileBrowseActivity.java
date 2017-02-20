package org.kans.zxb.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.kans.zxb.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowseActivity extends Activity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
	private FileBrowseAdapter mBrowseAdapter;
	private static final String ROOT_PATH = "/";
	private static final String PATH = "root_path";
	private FileBrowseUtil mFileUtil;
	private boolean backActivity = false;
	private ListView mListView;
	private FileAttribute mFileAttribute;
	private static String copyPath = null;
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		android.util.Log.e("xlan", getClass().getName());
		setContentView(R.layout.file_browse_activity);
		mFileUtil = FileBrowseUtil.getInstance(this);
		String path = getIntent().getStringExtra(PATH);
		if (TextUtils.isEmpty(path)) {
			path = ROOT_PATH;
		}
		mBrowseAdapter = new FileBrowseAdapter(this, path);
		mListView = (ListView) findViewById(R.id.file_browse_activity_list);
		mListView.setAdapter(mBrowseAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		showPasteButton(copyPath != null);
		DisplayMetrics dis = getResources().getDisplayMetrics();
		Log.e("xlan", "xdip:" + (dis.widthPixels / dis.density + 0.5f) + "  ydip:" + (dis.heightPixels / dis.density + 0.5f));
		Log.e("xlan", "xdp:" + 160 * (dis.widthPixels / dis.xdpi) + "  ydp:" + 160 * (dis.heightPixels / dis.ydpi));
	}

	private void showPasteButton(boolean isPasteButton) {
		View paste = findViewById(R.id.file_browser_paste);
		if (isPasteButton) {
			paste.setVisibility(View.VISIBLE);
		} else {
			paste.setVisibility(View.GONE);
		}
	}

	public String[] getChildsName(String filePath) {
		String[] childs = mFileUtil.list(filePath);
		if (childs == null || childs.length == 0) {
			File[] files = new File(filePath).listFiles();
			if (files != null && files.length > 0) {
				childs = new String[files.length];
				for (int i = 0; i < childs.length; i++) {
					childs[i] = files[i].getAbsolutePath();
				}
			}
		}
		return childs;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.file_browser_mkdir:
			showMkdir(this, mBrowseAdapter.getFilePath());
			break;
		case R.id.file_browser_paste:
			if (TextUtils.equals(new File(copyPath).getParent(), mBrowseAdapter.getFilePath())) {
				Toast.makeText(this, R.string.file_browser_choice_other_path, Toast.LENGTH_LONG).show();
				showPasteButton(true);
			} else {
				new FilePaste(copyPath, mBrowseAdapter.getFilePath(), this).start();
				copyPath = null;
				showPasteButton(false);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mBrowseAdapter != null) {
			String filePath = (String) view.getTag();
			if (isFile(filePath)) {
				if (runFile(filePath)) {
					return;
				}
				byte[] buffer = FileBrowseUtil.utilGetFileContent(filePath);
				String string = new String(buffer);
				browseFile(string);
			} else if (isDirectory(filePath)) {
				// mBrowseAdapter.setFilePath(filePath);
				Intent intent = new Intent(this, this.getClass());
				intent.putExtra(PATH, filePath);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (mBrowseAdapter != null) {
			String filePath = (String) view.getTag();
			if (mFileAttribute == null) {
				mFileAttribute = new FileAttribute(this, filePath);
			} else {
				mFileAttribute.setPath(filePath);
			}
			mFileAttribute.showAlertDialog();
			return true;
		}
		return false;
	}

	public void showInput(View view) {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(view, 0);
	}

	private void onBridgeApk(final File file) {
		final ProgressDialog mProgressDialog = new ProgressDialog(this);
		mProgressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mProgressDialog.setMessage("BridgeApk....");
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.show();
		new Thread(new Runnable() {

			private void makeDir(File file) {
				if (!file.getParentFile().exists()) {
					makeDir(file.getParentFile());
				} else {
					file.mkdirs();
				}
			}

			private String getEntryName(String name, String postfix) {
				if(name.contains(".")){
					return name.substring(0, name.lastIndexOf(".")+1)+postfix;
				}else{
					return name;
				}
			}

			@Override
			public void run() {
				String state = Environment.getExternalStorageState();
				if (state.equals(Environment.MEDIA_MOUNTED)) {
					File storageFile = Environment.getExternalStorageDirectory();
					File workSpace = new File(storageFile, "workSpace");
					makeDir(workSpace);
					File outApkFile = new File(workSpace, file.getName());
					if(outApkFile.exists()){
						outApkFile.delete();
					}
					try {
						ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outApkFile));
						if (!outApkFile.getAbsolutePath().equals(file.getAbsolutePath())) {
							ZipFile inZip = new ZipFile(file);
							Enumeration mZipEntry = inZip.entries();
							while (mZipEntry.hasMoreElements()) {
								ZipEntry inZipEntry = (ZipEntry) mZipEntry.nextElement();
								if(inZipEntry.isDirectory()){
									zos.putNextEntry(new ZipEntry(inZipEntry));
								}else{

									File temp = getApplication().getFileStreamPath("temp");
									FileOutputStream tempStream = new FileOutputStream(temp);
									byte[] buffer = new byte[1024];
									int len = 0;
									InputStream is = inZip.getInputStream(inZipEntry);
									while ((len = is.read(buffer, 0, buffer.length)) > 0) {
										tempStream.write(buffer, 0, len);
									}
									is.close();
									tempStream.flush();
									tempStream.close();
									String name = inZipEntry.getName();
									String entryName = name;
									if(name.lastIndexOf("qmg")>0||name.lastIndexOf("pio")>0||name.lastIndexOf("spr")>0||name.lastIndexOf("bmp")>0){
										entryName = getEntryName(name, "png");
									}
									ZipEntry outZipEntry = new ZipEntry(entryName);
									zos.putNextEntry(outZipEntry);
									Log.i("xlan", "ZipEntry item name:" + name);
									try {
										if (name.subSequence(name.length() - "qmg".length(), name.length()).equals("qmg") || name.subSequence(name.length() - "pio".length(), name.length()).equals("pio")) {
											
											try {
												Bitmap bitmap = BitmapFactory.decodeFile(temp.getAbsolutePath());
												if(bitmap!=null){
													tempStream = new FileOutputStream(temp);
													bitmap.compress(Bitmap.CompressFormat.PNG, 100, tempStream);
													tempStream.flush();
													tempStream.close();
													bitmap.recycle();
												}else{
													Log.e("xlan", "name err:"+name);
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
											
										} else if (name.subSequence(name.length() - "spr".length(), name.length()).equals("spr")) {
											
											try {
												Drawable mDrawable = Drawable.createFromPath(temp.getAbsolutePath());
												if(mDrawable!=null){
													Rect mRect = mDrawable.getBounds();
													int width = mRect.width() > 0 ? mRect.width() : 200;
													int height = mRect.height() > 0 ? mRect.height() : 200;
													Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
													Canvas mCanvas = new Canvas(bitmap);
													mDrawable.setBounds(0, 0, width, height);
													mDrawable.draw(mCanvas);
													tempStream = new FileOutputStream(temp);
													bitmap.compress(Bitmap.CompressFormat.PNG, 100, tempStream);
													tempStream.flush();
													tempStream.close();
													bitmap.recycle();
												}else{
													Log.e("xlan", "name err:"+name);
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
											
										}
										FileInputStream fis = new FileInputStream(temp);
										while ((len = fis.read(buffer, 0, buffer.length)) > 0) {
											zos.write(buffer, 0, len);
										}
										zos.flush();
										fis.close();
									} catch (Exception e) {
										e.printStackTrace();
									}	
								}
							}
						}
						zos.close();
					} catch (ZipException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
		}).start();
	}

	private boolean runFile(final String filePath) {
		final String APK = ".apk";
		final String PNG = ".png";
		final String JPG = ".jpg";
		final String MP4 = ".mp4";
		int indexApk = filePath.toLowerCase().lastIndexOf(APK);
		int indexPng = filePath.toLowerCase().lastIndexOf(PNG);
		int indexJpg = filePath.toLowerCase().lastIndexOf(JPG);
		int indexMp4 = filePath.toLowerCase().lastIndexOf(MP4);
		final File file = new File(filePath);
		if ((filePath.length() - APK.length()) == indexApk) {
			if (file.exists() || isFile(filePath)) {
				onBridgeApk(file);
				new AlertDialog.Builder(this).setTitle("what are you dong").setSingleChoiceItems(new CharSequence[] { "Bridge picture", "install app", "cancel" }, 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							onBridgeApk(file);
							break;
						case 1:
							Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
							intent.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
							intent.putExtra("android.intent.extra.START_APK", true);
							startService(intent);
							break;

						default:
							break;
						}
						dialog.dismiss();
					}
				}).create().show();
				Log.i("xlan", "apk file show()");
				return true;
			}
		} else if (((filePath.length() - PNG.length()) == indexPng) || ((filePath.length() - JPG.length()) == indexJpg)) {
			if (file.exists() || isFile(filePath)) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "image/*");
				startActivity(intent);
				return true;
			}
		} else if (((filePath.length() - MP4.length()) == indexMp4)) {
			if (file.exists() || isFile(filePath)) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "video/mp4");
				startActivity(intent);
				return true;
			}
		}
		return false;
	}

	private boolean isShowBrowseFile() {
		return findViewById(R.id.file_browse_activity_scrollView).getVisibility() == View.VISIBLE;
	}

	private void browseFile(String str) {
		View scrollView = findViewById(R.id.file_browse_activity_scrollView);
		TextView textView = (TextView) scrollView.findViewById(R.id.file_browse_activity_browse_file);
		if (str == null) {
			textView.setText("");
			scrollView.setVisibility(View.GONE);
		} else {
			textView.setText(str);
			scrollView.setVisibility(View.VISIBLE);
		}
	}

	private void setEmpty(String str) {
		TextView emptyTextView = (TextView) findViewById(R.id.file_browse_activity_null);
		if (str == null) {
			emptyTextView.setText("");
			emptyTextView.setVisibility(View.GONE);
		} else {
			emptyTextView.setText(str);
			emptyTextView.setVisibility(View.VISIBLE);
		}
	}

	public boolean isFile(String filePath) {
		return mFileUtil.isFile(filePath) || new File(filePath).isFile();
	}

	public boolean isDirectory(String filePath) {
		return mFileUtil.isDirectory(filePath) || new File(filePath).isDirectory();
	}

	public void onBackView(View view) {
		if (mBrowseAdapter != null) {
			if (isShowBrowseFile()) {
				browseFile(null);
				return;
			}
			String filePath = mBrowseAdapter.getFilePath();
			if (TextUtils.equals(ROOT_PATH, filePath)) {
				if (backActivity) {
					finish();
				} else {
					backActivity = true;
					Toast.makeText(this, R.string.onclick_double_back_goback, Toast.LENGTH_SHORT).show();
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							backActivity = false;
						}
					}, 5000);
				}
			} else {
				finish();
				overridePendingTransition(0, 0);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mBrowseAdapter != null) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mBrowseAdapter != null) {
			if (isShowBrowseFile()) {
				browseFile(null);
				return true;
			}
			String filePath = mBrowseAdapter.getFilePath();
			if (TextUtils.equals(ROOT_PATH, filePath)) {
				if (backActivity) {
					finish();
				} else {
					backActivity = true;
					Toast.makeText(this, R.string.onclick_double_back_goback, Toast.LENGTH_SHORT).show();
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							backActivity = false;
						}
					}, 5000);
				}
			} else {
				finish();
				overridePendingTransition(0, 0);
				/*
				 * String fileParentPath = new File(filePath).getParent();
				 * mBrowseAdapter.setFilePath(fileParentPath);
				 */
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void showMkdir(final Context context, String path) {
		final EditText editText = new EditText(context);
		final String currentPaht = path;

		new AlertDialog.Builder(context).setTitle(R.string.file_browser_mkdir).setView(editText).setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = editText.getEditableText().toString();
				if (name != null && !TextUtils.isEmpty(name)) {
					File file = new File(currentPaht, name);
					boolean isSuccessful = mFileUtil.mkdirs(file.getAbsolutePath());
					if (!isSuccessful) {
						isSuccessful = file.mkdirs();
					}
					String str = context.getText(R.string.file_browser_mkdir).toString() + (isSuccessful ? context.getText(R.string.file_browser_success).toString() : context.getText(R.string.file_browser_fail).toString());
					Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
					if (isSuccessful) {
						mBrowseAdapter.update();
					}
				} else {
					Toast.makeText(context, R.string.file_browser_mkdir_name_is_null, Toast.LENGTH_SHORT).show();
				}
			}
		}).show();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				showInput(editText);
			}
		}, 500);
	}

	public void rename(final String originalName, final Context context) {
		final String rename = getText(R.string.file_browser_attribute_rename).toString();
		final String file = getText(R.string.file_browser_file).toString();
		final String folder = getText(R.string.file_browser_folder).toString();
		final String fail = getText(R.string.file_browser_fail).toString();
		final String isnull = getText(R.string.file_browser_isnull).toString();
		final String isFileNameSame = getText(R.string.file_browser_name_is_same).toString();

		final boolean isFile = isFile(originalName);
		final EditText editText = new EditText(context);
		final String fileName = new File(originalName).getName();
		if (fileName != null && !TextUtils.isEmpty(fileName)) {
			editText.setText(fileName);
			editText.setSelection(fileName.length());
		}
		new AlertDialog.Builder(context).setTitle(rename + (isFile ? file + ":" : folder + ":") + (new File(originalName).getName())).setView(editText).setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = editText.getEditableText().toString();
				if (TextUtils.equals(name, fileName)) {
					Toast.makeText(context, isFileNameSame, Toast.LENGTH_LONG).show();
				} else if (name != null && !TextUtils.isEmpty(name)) {
					File currentFile = new File(new File(originalName).getParentFile(), name);
					boolean isSuccessful = mFileUtil.renameTo(originalName, currentFile.getAbsolutePath());
					if (!isSuccessful) {
						isSuccessful = new File(originalName).renameTo(currentFile);
					}
					if (isSuccessful) {
						mBrowseAdapter.update();
					} else {
						Toast.makeText(context, rename + (isFile ? file + ":" : folder + ":") + fail, Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(context, rename + (isFile ? file + ":" : folder + ":") + isnull, Toast.LENGTH_SHORT).show();
				}
			}
		}).show();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				showInput(editText);
			}
		}, 1000);
	}

	public class FilePaste implements Runnable, View.OnClickListener {
		private AlertDialog mAlertDialog;
		private String pathFrom, pathTo;
		private Context context;
		private TextView file_count, folder_count, copy_count_ing, copying_file_name;
		private ProgressBar copy_count_progressBar;
		private Button copy_cancel;
		private View copying_layout, search_layout;
		private ListView copy_scrollView;
		private List<String> fileList = new ArrayList<String>();
		private List<String> copyErrList = new ArrayList<String>();
		private Thread mThread;
		private final int UPDATE_FOLDER_COUNT = 0x10011;
		private final int UPDATE_FILE_COUNT = 0x10012;
		private final int UPDATE_COPY_FILE_COUNT = 0x10013;
		private final int UPDATE_COPY_SHOW_COPYING = 0x10014;
		private final int UPDATE_COPY_OVER = 0x10015;
		private boolean isLive = true;

		public FilePaste(String pathFrom, String pathTo, Context context) {
			super();
			this.pathFrom = pathFrom;
			this.pathTo = pathTo;
			this.context = context;
			init();
		}

		private Handler filePasteHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				final String folderCount = getText(R.string.file_browser_folder_count).toString();
				final String fileCount = getText(R.string.file_browser_file_count).toString();
				final String allCount = getText(R.string.file_browser_all_count).toString();
				final String current = getText(R.string.file_browser_current).toString();
				final String copy_fail_file = getText(R.string.file_browser_copy_fail_file).toString();
				final String copy_fail_file_count = getText(R.string.file_browser_copy_fail_file_count).toString();

				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_FOLDER_COUNT:
					folder_count.setText(folderCount + ":" + msg.arg1);
					break;
				case UPDATE_FILE_COUNT:
					file_count.setText(fileCount + ":" + msg.arg1);
					break;
				case UPDATE_COPY_SHOW_COPYING:
					copy_count_progressBar.setMax(fileList.size());
					copy_count_progressBar.setProgress(0);
					search_layout.setVisibility(View.GONE);
					copying_layout.setVisibility(View.VISIBLE);
					copy_scrollView.setVisibility(View.GONE);
					copy_count_ing.setText(allCount + ":" + msg.arg1 + "  " + current + ":" + 0);
					break;
				case UPDATE_COPY_FILE_COUNT:
					copy_count_progressBar.setProgress(msg.arg2);
					copy_count_ing.setText(allCount + ":" + msg.arg1 + "  " + current + ":" + msg.arg2);
					if (msg.obj instanceof String) {
						copying_file_name.setText((String) msg.obj);
					} else {
						copying_file_name.setText("");
					}
					break;
				case UPDATE_COPY_OVER:
					mBrowseAdapter.update();
					if (copyErrList.size() == 0) {
						stop();
					} else {
						mAlertDialog.setCancelable(true);
						copyErrList.add(0, copy_fail_file + ":" + copyErrList.size() + copy_fail_file_count + "\n");
						copy_scrollView.setAdapter(new BaseAdapter() {

							@Override
							public View getView(int position, View convertView, ViewGroup parent) {
								TextView tv;
								if (convertView != null) {
									tv = (TextView) convertView;
								} else {
									tv = new TextView(context);
									tv.setPadding(15, 10, 15, 10);
									tv.setTextColor(Color.BLACK);
								}
								if (position == 0) {
									tv.setBackgroundColor(0XFFFFFFFF);
									tv.setTextSize(18f);
									tv.setTextColor(Color.RED);
								} else {
									tv.setTextSize(15f);
									tv.setTextColor(Color.BLACK);
									if (position % 2 == 0) {
										tv.setBackgroundColor(0XFFEEEEEE);
									} else {
										tv.setBackgroundColor(0XFFDDDDDD);
									}
								}
								tv.setText(copyErrList.get(position));
								return tv;
							}

							@Override
							public long getItemId(int position) {
								return position;
							}

							@Override
							public Object getItem(int position) {
								return copyErrList.get(position);
							}

							@Override
							public int getCount() {
								return copyErrList.size();
							}
						});
						search_layout.setVisibility(View.GONE);
						copying_layout.setVisibility(View.GONE);
						copy_scrollView.setVisibility(View.VISIBLE);
					}
					break;
				default:
					break;
				}
			}

		};

		public void start() {
			removeMessages();
			mThread = new Thread(this);
			mThread.start();
		}

		private void removeMessages() {
			filePasteHandler.removeMessages(UPDATE_FOLDER_COUNT);
			filePasteHandler.removeMessages(UPDATE_FILE_COUNT);
			filePasteHandler.removeMessages(UPDATE_COPY_FILE_COUNT);
			filePasteHandler.removeMessages(UPDATE_COPY_SHOW_COPYING);
		}

		public void stop() {
			if (mThread != null) {
				isLive = false;
				mThread = null;
			}
			removeMessages();
			mAlertDialog.dismiss();
		}

		private void init() {
			View view = View.inflate(context, R.layout.file_browser_copy, null);
			copying_layout = view.findViewById(R.id.file_browser_copying_layout);
			copying_layout.setVisibility(View.GONE);
			search_layout = view.findViewById(R.id.file_browser_search_layout);
			search_layout.setVisibility(View.VISIBLE);
			file_count = (TextView) view.findViewById(R.id.file_browser_copy_file_count);
			folder_count = (TextView) view.findViewById(R.id.file_browser_copy_folder_count);
			copy_scrollView = (ListView) view.findViewById(R.id.file_browser_copy_scrollView);
			copy_scrollView.setVisibility(View.GONE);
			copy_count_ing = (TextView) view.findViewById(R.id.file_browser_copy_count_ing);
			copying_file_name = (TextView) view.findViewById(R.id.file_browser_copy_count_ing_file_name);
			copy_count_progressBar = (ProgressBar) view.findViewById(R.id.file_browser_copy_count_progressBar);
			copy_cancel = (Button) view.findViewById(R.id.file_browser_copy_cancel);
			copy_cancel.setOnClickListener(this);
			mAlertDialog = new AlertDialog.Builder(context).setView(view).setCancelable(false).show();
		}

		public void searchFile(List<String> listFile, List<String> listFolder, String filePath) {
			if (isLive) {
				if (isFile(filePath)) {
					listFile.add(filePath);
					if (listFile.size() % 5 == 0) {
						Message msg = filePasteHandler.obtainMessage(UPDATE_FILE_COUNT);
						msg.arg1 = listFile.size();
						filePasteHandler.removeMessages(UPDATE_FILE_COUNT);
						filePasteHandler.sendMessage(msg);
					}
				} else if (isDirectory(filePath)) {
					String[] childs = getChildsName(filePath);
					if (childs != null && childs.length > 0) {
						listFolder.add(filePath);
						Message msg = filePasteHandler.obtainMessage(UPDATE_FOLDER_COUNT);
						msg.arg1 = listFolder.size();
						filePasteHandler.removeMessages(UPDATE_FOLDER_COUNT);
						filePasteHandler.sendMessage(msg);
						for (String child : childs) {
							searchFile(listFile, listFolder, child);
						}
					}
				}
			}

		}

		private String getFileToFileName(String filePath, String pathFrom, String pathTo) {
			if (isLive) {
				int index = 0;
				if (isFile(pathFrom)) {
					index = new File(pathFrom).getParent().length();
				} else {
					index = new File(pathFrom).getParent().length() - 1;
					// index = pathFrom.length();
				}
				String newFilePath = pathTo + filePath.substring(index);
				return newFilePath;
			} else {
				return null;
			}
		}

		@Override
		public void run() {
			if (pathFrom != null && pathTo != null) {
				List<String> listFile = new ArrayList<String>();
				List<String> listFolder = new ArrayList<String>();
				searchFile(listFile, listFolder, pathFrom);
				fileList.clear();
				fileList.addAll(listFile);
				Message msg = filePasteHandler.obtainMessage(UPDATE_COPY_SHOW_COPYING);
				msg.arg1 = listFile.size();
				filePasteHandler.removeMessages(UPDATE_COPY_SHOW_COPYING);
				filePasteHandler.sendMessage(msg);
				listFolder.clear();
				copyErrList = listFolder;
				List<String> copyed = new ArrayList<String>();
				File file = new File(FileBrowseUtil.DATA_CACHE_PATH);
				if (!mFileUtil.mkdirs(file.getAbsolutePath())) {
					Log.w("xlan", "file mkdir err:" + file.getAbsolutePath());
				}
				for (int i = 0; i < listFile.size() && isLive; i++) {
					String fileName = listFile.get(i);
					copyFile(fileName, getFileToFileName(fileName, pathFrom, pathTo), copyErrList, copyed);
				}
				filePasteHandler.sendEmptyMessage(UPDATE_COPY_OVER);

			}
		}

		private boolean inSdcardPath(String path) {
			String filePath = path.toLowerCase();
			if (filePath.indexOf("/sdcard") == 0 || filePath.indexOf("/mnt/sdcard") == 0 || filePath.indexOf("/storage/sdcard") == 0) {
				return true;
			}
			return false;
		}

		private void copyFile(String fileFrom, String fileTo, List<String> errList, List<String> copyed) {
			if ((fileFrom.indexOf("/system") == 0 && fileTo.indexOf("/data") != 0) || (inSdcardPath(fileFrom) && inSdcardPath(fileTo))) {
				boolean isSucceed = FileBrowseUtil.utilCopyFile(new File(fileFrom), new File(fileTo));
				if (!isSucceed || (new File(fileFrom).length() != new File(fileTo).length())) {
					errList.add(fileFrom);
				}
			} else {
				File temp = new File(FileBrowseUtil.DATA_CACHE_PATH, "copyTemp");
				boolean isSucceed = FileBrowseUtil.utilCopyFile(new File(fileFrom), temp);
				if (isSucceed) {
					isSucceed = FileBrowseUtil.utilCopyFile(temp, new File(fileTo));
				}
				if (!isSucceed) {
					errList.add(fileFrom);
				}
			}
			copyed.add(fileFrom);
			Message msg = filePasteHandler.obtainMessage(UPDATE_COPY_FILE_COUNT);
			msg.obj = fileFrom;
			msg.arg1 = fileList.size();
			msg.arg2 = copyed.size();
			filePasteHandler.removeMessages(UPDATE_COPY_FILE_COUNT);
			filePasteHandler.sendMessage(msg);
		}

		@Override
		public void onClick(View v) {
			stop();
		}

	}

	public class FileDel implements Runnable, View.OnClickListener {
		private AlertDialog mAlertDialog;
		private String path;
		private Context context;
		private TextView file_count, folder_count, del_count_ing, deling_file_name;
		private ProgressBar del_count_progressBar;
		private Button del_cancel;
		private View deling_layout, search_layout;
		private ListView del_scrollView;
		private List<String> fileList = new ArrayList<String>();
		private List<String> delErrList = new ArrayList<String>();
		private Thread mThread;
		private final int UPDATE_FOLDER_COUNT = 0x10011;
		private final int UPDATE_FILE_COUNT = 0x10012;
		private final int UPDATE_DEL_FILE_COUNT = 0x10013;
		private final int UPDATE_DEL_SHOW_COPYING = 0x10014;
		private final int UPDATE_DEL_OVER = 0x10015;
		private boolean isLive = true;

		public FileDel(String path, Context context) {
			super();
			this.path = path;
			this.context = context;
			init();
		}

		private Handler fileDelHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				final String folderCount = getText(R.string.file_browser_folder_count).toString();
				final String fileCount = getText(R.string.file_browser_file_count).toString();
				final String allCount = getText(R.string.file_browser_all_count).toString();
				final String current = getText(R.string.file_browser_current).toString();
				final String del_fail_file = getText(R.string.file_browser_del_fail_file).toString();
				final String del_fail_file_count = getText(R.string.file_browser_copy_fail_file_count).toString();

				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_FOLDER_COUNT:
					folder_count.setText(folderCount + ":" + msg.arg1);
					break;
				case UPDATE_FILE_COUNT:
					file_count.setText(fileCount + ":" + msg.arg1);
					break;
				case UPDATE_DEL_SHOW_COPYING:
					del_count_progressBar.setMax(fileList.size());
					del_count_progressBar.setProgress(0);
					search_layout.setVisibility(View.GONE);
					deling_layout.setVisibility(View.VISIBLE);
					del_scrollView.setVisibility(View.GONE);
					del_count_ing.setText(allCount + ":" + msg.arg1 + "  " + current + ":" + 0);
					break;
				case UPDATE_DEL_FILE_COUNT:
					del_count_progressBar.setProgress(msg.arg2);
					del_count_ing.setText(allCount + ":" + msg.arg1 + "  " + current + ":" + msg.arg2);
					if (msg.obj instanceof String) {
						deling_file_name.setText((String) msg.obj);
					} else {
						deling_file_name.setText("");
					}
					break;
				case UPDATE_DEL_OVER:
					mBrowseAdapter.update();
					if (delErrList.size() == 0) {
						stop();
					} else {
						mAlertDialog.setCancelable(true);
						delErrList.add(0, del_fail_file + ":" + delErrList.size() + del_fail_file_count + "\n");
						del_scrollView.setAdapter(new BaseAdapter() {

							@Override
							public View getView(int position, View convertView, ViewGroup parent) {
								TextView tv;
								if (convertView != null) {
									tv = (TextView) convertView;
								} else {
									tv = new TextView(context);
									tv.setPadding(15, 10, 15, 10);
									tv.setTextColor(Color.BLACK);
								}
								if (position == 0) {
									tv.setBackgroundColor(0XFFFFFFFF);
									tv.setTextSize(18f);
									tv.setTextColor(Color.RED);
								} else {
									tv.setTextSize(15f);
									tv.setTextColor(Color.BLACK);
									if (position % 2 == 0) {
										tv.setBackgroundColor(0XFFEEEEEE);
									} else {
										tv.setBackgroundColor(0XFFDDDDDD);
									}
								}
								tv.setText(delErrList.get(position));
								return tv;
							}

							@Override
							public long getItemId(int position) {
								return position;
							}

							@Override
							public Object getItem(int position) {
								return delErrList.get(position);
							}

							@Override
							public int getCount() {
								return delErrList.size();
							}
						});
						search_layout.setVisibility(View.GONE);
						deling_layout.setVisibility(View.GONE);
						del_scrollView.setVisibility(View.VISIBLE);
					}
					break;
				default:
					break;
				}
			}

		};

		public void start() {
			removeMessages();
			mThread = new Thread(this);
			mThread.start();
		}

		private void removeMessages() {
			fileDelHandler.removeMessages(UPDATE_FOLDER_COUNT);
			fileDelHandler.removeMessages(UPDATE_FILE_COUNT);
			fileDelHandler.removeMessages(UPDATE_DEL_FILE_COUNT);
			fileDelHandler.removeMessages(UPDATE_DEL_SHOW_COPYING);
		}

		public void stop() {
			if (mThread != null) {
				isLive = false;
				mThread = null;
			}
			removeMessages();
			mAlertDialog.dismiss();
		}

		private void init() {
			View view = View.inflate(context, R.layout.file_browser_del, null);
			deling_layout = view.findViewById(R.id.file_browser_deling_layout);
			deling_layout.setVisibility(View.GONE);
			search_layout = view.findViewById(R.id.file_browser_search_layout);
			search_layout.setVisibility(View.VISIBLE);
			file_count = (TextView) view.findViewById(R.id.file_browser_del_file_count);
			folder_count = (TextView) view.findViewById(R.id.file_browser_del_folder_count);
			del_scrollView = (ListView) view.findViewById(R.id.file_browser_del_scrollView);
			del_scrollView.setVisibility(View.GONE);
			del_count_ing = (TextView) view.findViewById(R.id.file_browser_del_count_ing);
			deling_file_name = (TextView) view.findViewById(R.id.file_browser_del_count_ing_file_name);
			del_count_progressBar = (ProgressBar) view.findViewById(R.id.file_browser_del_count_progressBar);
			del_cancel = (Button) view.findViewById(R.id.file_browser_del_cancel);
			del_cancel.setOnClickListener(this);
			mAlertDialog = new AlertDialog.Builder(context).setView(view).setCancelable(false).show();
		}

		public void searchFile(List<String> listFile, List<String> listFolder, String filePath) {
			if (isLive) {
				if (isFile(filePath)) {
					listFile.add(filePath);
					if (listFile.size() % 5 == 0) {
						Message msg = fileDelHandler.obtainMessage(UPDATE_FILE_COUNT);
						msg.arg1 = listFile.size();
						fileDelHandler.removeMessages(UPDATE_FILE_COUNT);
						fileDelHandler.sendMessage(msg);
					}
				} else if (isDirectory(filePath)) {
					String[] childs = getChildsName(filePath);
					if (childs != null && childs.length > 0) {
						listFolder.add(filePath);
						Message msg = fileDelHandler.obtainMessage(UPDATE_FOLDER_COUNT);
						msg.arg1 = listFolder.size();
						fileDelHandler.removeMessages(UPDATE_FOLDER_COUNT);
						fileDelHandler.sendMessage(msg);
						for (String child : childs) {
							searchFile(listFile, listFolder, child);
						}
					}
				}
			}
		}

		@Override
		public void run() {
			if (path != null) {
				List<String> listFile = new ArrayList<String>();
				List<String> listFolder = new ArrayList<String>();
				searchFile(listFile, listFolder, path);
				fileList.clear();
				fileList.addAll(listFile);
				Message msg = fileDelHandler.obtainMessage(UPDATE_DEL_SHOW_COPYING);
				msg.arg1 = listFile.size();
				fileDelHandler.removeMessages(UPDATE_DEL_SHOW_COPYING);
				fileDelHandler.sendMessage(msg);
				listFolder.clear();
				delErrList = listFolder;

				for (int i = 0; i < listFile.size() && isLive; i++) {
					String fileName = listFile.get(i);
					File currentFile = new File(fileName);
					if (!mFileUtil.delete(fileName)) {
						if (!currentFile.delete()) {
							delErrList.add(fileName);
						}
					}
					File arentFile = currentFile.getParentFile();
					String[] listChild = mFileUtil.list(arentFile.getAbsolutePath());
					if (listChild == null || listChild.length == 0 || arentFile.list() == null || arentFile.list().length == 0) {
						if (!mFileUtil.delete(arentFile.getAbsolutePath())) {
							arentFile.delete();
						}
					}
					if (i % 6 == 0) {
						Message update_msg = fileDelHandler.obtainMessage(UPDATE_DEL_FILE_COUNT);
						update_msg.obj = fileName;
						update_msg.arg1 = fileList.size();
						update_msg.arg2 = i;
						fileDelHandler.removeMessages(UPDATE_DEL_FILE_COUNT);
						fileDelHandler.sendMessage(update_msg);
					}
				}
				if (isLive) {
					mFileUtil.delete(path);
					FileBrowseUtil.deleFiles(path);
				}

				fileDelHandler.removeMessages(UPDATE_DEL_OVER);
				fileDelHandler.sendEmptyMessage(UPDATE_DEL_OVER);
			}
		}

		@Override
		public void onClick(View v) {
			stop();
		}

	}

	public class FileComputerLeng implements Runnable, View.OnClickListener {
		private AlertDialog mAlertDialog;
		private String path;
		private Context context;
		private TextView file_count, folder_count, file_path, file_leng;
		View computer_progressbar;
		private Button del_cancel;
		private List<String> fileList = new ArrayList<String>();
		private Thread mThread;
		private final int UPDATE_FOLDER_COUNT = 0x10011;
		private final int UPDATE_FILE_COUNT = 0x10012;
		private final int UPDATE_FILE_LENG = 0x10013;
		private final int UPDATE_FILE_SEACH_OVER = 0x10014;
		private boolean isLive = true;

		public FileComputerLeng(String path, Context context) {
			super();
			this.path = path;
			this.context = context;
			init();
		}

		private Handler fileDelHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				final String folderCount = getText(R.string.file_browser_folder_count).toString();
				final String fileCount = getText(R.string.file_browser_file_count).toString();
				final String big_or_small = getText(R.string.file_browser_big_or_small).toString();

				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_FOLDER_COUNT:
					folder_count.setText(folderCount + ":" + msg.arg1);
					break;
				case UPDATE_FILE_COUNT:
					file_count.setText(fileCount + ":" + msg.arg1);
					break;
				case UPDATE_FILE_LENG:
					if (msg.obj != null && msg.obj instanceof String) {
						file_leng.setText(big_or_small + ":" + ((String) msg.obj));
					}
					break;
				case UPDATE_FILE_SEACH_OVER:
					mAlertDialog.setCancelable(true);
					del_cancel.setVisibility(View.GONE);
					computer_progressbar.setVisibility(View.GONE);
					break;
				default:
					break;
				}
			}

		};

		public void start() {
			removeMessages();
			mThread = new Thread(this);
			mThread.start();
		}

		private void removeMessages() {
			fileDelHandler.removeMessages(UPDATE_FOLDER_COUNT);
			fileDelHandler.removeMessages(UPDATE_FILE_COUNT);
			fileDelHandler.removeMessages(UPDATE_FILE_LENG);
			fileDelHandler.removeMessages(UPDATE_FILE_SEACH_OVER);
		}

		public void stop() {
			if (mThread != null) {
				isLive = false;
				mThread = null;
			}
			removeMessages();
			mAlertDialog.dismiss();
		}

		private void init() {
			View view = View.inflate(context, R.layout.file_browser_computer_leng, null);
			file_path = (TextView) view.findViewById(R.id.file_browser_computer_file_path);
			file_path.setText(getText(R.string.file_browser_attribute_path).toString() + ":" + path);
			computer_progressbar = view.findViewById(R.id.file_browser_computer_progressbar);
			file_count = (TextView) view.findViewById(R.id.file_computer_file_count);
			folder_count = (TextView) view.findViewById(R.id.file_computer_folder_count);
			file_leng = (TextView) view.findViewById(R.id.file_browser_computer_file_leng);
			del_cancel = (Button) view.findViewById(R.id.file_browser_del_cancel);
			del_cancel.setOnClickListener(this);
			mAlertDialog = new AlertDialog.Builder(context).setView(view).setCancelable(false).show();
		}

		public long computerFile(List<String> listFile, List<String> listFolder, String filePath, long fileLeng) {
			if (isLive) {
				if (isFile(filePath)) {
					listFile.add(filePath);
					long len = mFileUtil.length(filePath);
					if (len == 0) {
						len = new File(filePath).length();
					}
					if (listFile.size() % 5 == 0) {
						Message msg = fileDelHandler.obtainMessage(UPDATE_FILE_COUNT);
						msg.arg1 = listFile.size();
						fileDelHandler.removeMessages(UPDATE_FILE_COUNT);
						fileDelHandler.sendMessage(msg);
					}

					if (len > 0) {
						fileLeng = fileLeng + len;
						Message msg = fileDelHandler.obtainMessage(UPDATE_FILE_LENG);
						msg.obj = Formatter.formatFileSize(context, fileLeng);
						fileDelHandler.removeMessages(UPDATE_FILE_LENG);
						fileDelHandler.sendMessage(msg);
					}
				} else if (isDirectory(filePath)) {
					String[] childs = getChildsName(filePath);
					if (childs != null && childs.length > 0) {
						listFolder.add(filePath);
						Message msg = fileDelHandler.obtainMessage(UPDATE_FOLDER_COUNT);
						msg.arg1 = listFolder.size();
						fileDelHandler.removeMessages(UPDATE_FOLDER_COUNT);
						fileDelHandler.sendMessage(msg);
						for (String child : childs) {
							fileLeng = computerFile(listFile, listFolder, child, fileLeng);
						}
					}
				}
			}

			return fileLeng;
		}

		@Override
		public void run() {
			if (path != null) {
				List<String> listFile = new ArrayList<String>();
				List<String> listFolder = new ArrayList<String>();
				long fileLeng = 0;
				fileLeng = computerFile(listFile, listFolder, path, fileLeng);

				Message filMsg = fileDelHandler.obtainMessage(UPDATE_FILE_COUNT);
				filMsg.arg1 = listFile.size();
				fileDelHandler.removeMessages(UPDATE_FILE_COUNT);
				fileDelHandler.sendMessage(filMsg);

				Message folderMsg = fileDelHandler.obtainMessage(UPDATE_FOLDER_COUNT);
				folderMsg.arg1 = listFolder.size();
				fileDelHandler.removeMessages(UPDATE_FOLDER_COUNT);
				fileDelHandler.sendMessage(folderMsg);

				Message computerMsg = fileDelHandler.obtainMessage(UPDATE_FILE_LENG);
				computerMsg.obj = Formatter.formatFileSize(context, fileLeng);
				fileDelHandler.removeMessages(UPDATE_FILE_LENG);
				fileDelHandler.sendMessage(computerMsg);

				Message msg = fileDelHandler.obtainMessage(UPDATE_FILE_SEACH_OVER);
				fileDelHandler.removeMessages(UPDATE_FILE_SEACH_OVER);
				fileDelHandler.sendMessage(msg);

			}
		}

		@Override
		public void onClick(View v) {
			stop();
		}

	}

	public class FileAttribute implements Runnable, View.OnClickListener, DialogInterface.OnDismissListener {
		private AlertDialog.Builder builder;
		private AlertDialog mAlertDialog;
		private Context context;
		private String path;
		private Thread runThread;
		private TextView nameView, pathView, copyView, delView, renameView, lengView;

		private final int UPDATE_AMOUNT = 0x1101;
		private final int UPDATE_FILE_COUNT = 0x1102;

		private Handler attributeHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_AMOUNT:

					break;
				case UPDATE_FILE_COUNT:

					break;

				default:
					break;
				}
			}

		};

		public void hideAlertDialog() {
			if (runThread != null && runThread.isAlive()) {
				runThread.stop();
				runThread = null;
			}
			if (mAlertDialog != null) {
				if (mAlertDialog.isShowing()) {
					mAlertDialog.hide();
				}
			}
		}

		public void showAlertDialog() {
			if (mAlertDialog != null) {
				if (mAlertDialog.isShowing()) {
					mAlertDialog.hide();
				}
			}
			if (mAlertDialog == null) {
				builder = new AlertDialog.Builder(context);
				View view = View.inflate(context, R.layout.file_browser_attribute, null);
				nameView = (TextView) view.findViewById(R.id.file_browser_attribute_name);
				pathView = (TextView) view.findViewById(R.id.file_browser_attribute_path);
				copyView = (TextView) view.findViewById(R.id.file_browser_attribute_copy);
				copyView.setOnClickListener(this);
				delView = (TextView) view.findViewById(R.id.file_browser_attribute_del);
				delView.setOnClickListener(this);
				renameView = (TextView) view.findViewById(R.id.file_browser_attribute_rename);
				renameView.setOnClickListener(this);
				lengView = (TextView) view.findViewById(R.id.file_browser_attribute_leng);
				lengView.setOnClickListener(this);
				builder.setView(view);
				mAlertDialog = builder.create();
				mAlertDialog.setOnDismissListener(this);
			}
			setText();
			mAlertDialog.show();
		}

		private void setText() {
			if (nameView != null) {
				nameView.setText(getString(R.string.file_browser_attribute_name) + ":  " + new File(path).getName());
			}
			if (pathView != null) {
				pathView.setText(getString(R.string.file_browser_attribute_path) + ":  " + new File(path).getParent());
			}
			if ((isFile(path)) || (path.length() > 5 && (path.indexOf("/sys/") == 0)) || (path.length() < 5 && (path.indexOf("/sys") == 0))) {
				lengView.setVisibility(View.GONE);
			} else {
				lengView.setVisibility(View.VISIBLE);
			}
		}

		public void setPath(String path) {
			this.path = path;
			if (runThread != null && runThread.isAlive()) {
				runThread.stop();
				runThread = null;
			}
			attributeHandler.removeMessages(UPDATE_AMOUNT);
			attributeHandler.removeMessages(UPDATE_FILE_COUNT);
			runThread = new Thread(this);
			runThread.start();
		}

		public FileAttribute(Context context, String path) {
			super();
			this.context = context;
			this.path = path;
			setPath(path);
		}

		@Override
		public void run() {

		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.file_browser_attribute_copy:
				showPasteButton(true);
				copyPath = this.path;
				break;
			case R.id.file_browser_attribute_del:
				new FileDel(path, context).start();
				break;
			case R.id.file_browser_attribute_rename:
				rename(path, context);
				break;
			case R.id.file_browser_attribute_leng:
				new FileComputerLeng(path, context).start();
				break;

			default:
				break;
			}
			hideAlertDialog();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			if (runThread != null && runThread.isAlive()) {
				runThread.stop();
				runThread = null;
			}
			attributeHandler.removeMessages(UPDATE_AMOUNT);
			attributeHandler.removeMessages(UPDATE_FILE_COUNT);
		}
	}

	public class FileBrowseAdapter extends BaseAdapter {
		private TextView path;
		private Context context;
		private String filePath;
		private String[] files;
		private final String EMPTY = "is empty !";
		private Formatter formatter;
		private String file_leng, item_content_folder_1, item_content_folder_2;

		public String getFilePath() {
			return filePath == null ? ROOT_PATH : filePath;
		}

		public FileBrowseAdapter(Context context, String filePath) {
			super();
			this.context = context;
			path = (TextView) findViewById(R.id.file_browse_activity_path);
			formatter = new Formatter();
			setFilePath(filePath);
			file_leng = context.getString(R.string.file_browser_file_big_or_small).toString();
			item_content_folder_1 = context.getString(R.string.file_browser_file_item_content_folder_1).toString();
			item_content_folder_2 = context.getString(R.string.file_browser_file_item_content_folder_1).toString();

		}

		public void update() {
			files = mFileUtil.list(filePath);
			if (files == null || files.length == 0 || files[0].indexOf("/") != 0) {
				File file = new File(filePath);
				File[] items = file.listFiles();
				if (items != null && items.length > 0) {
					files = new String[items.length];
					for (int i = 0; i < files.length; i++) {
						files[i] = items[i].getAbsolutePath();
					}
				}
				if (files == null || files.length == 0) {
					files = new String[] { EMPTY };
				}
			}
			if (files.length == 0 || TextUtils.equals(files[0], EMPTY)) {
				setEmpty(EMPTY);
			} else {
				setEmpty(null);
				sort();
			}
			if (mListView != null) {
				notifyDataSetChanged();
			}
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
			path.setText(filePath);
			update();
		}

		private void sort() {
			if (files.length == 0) {
				return;
			}
			// long time = System.currentTimeMillis();
			int allFilesCount = 0;
			String[] allFiles = new String[files.length];
			int allDirCount = 0;
			String[] allDir = new String[files.length];
			int allOtherCount = 0;
			String[] allOther = new String[files.length];

			for (int i = 0; i < files.length; i++) {
				if (isFile(files[i])) {
					allFiles[allFilesCount] = files[i];
					allFilesCount++;
				} else if (isDirectory(files[i])) {
					allDir[allDirCount] = files[i];
					allDirCount++;
				} else {
					allOther[allOtherCount] = files[i];
					allOtherCount++;
				}
			}

			String[] newAllDir = new String[allDirCount];
			for (int i = 0; i < allDirCount; i++) {
				newAllDir[i] = allDir[i];
			}
			allDir = FileBrowseUtil.sort(newAllDir);

			String[] newAllFiles = new String[allFilesCount];
			for (int i = 0; i < allFilesCount; i++) {
				newAllFiles[i] = allFiles[i];
			}
			allFiles = FileBrowseUtil.sort(newAllFiles);

			String[] newAllOther = new String[allOtherCount];
			for (int i = 0; i < allOtherCount; i++) {
				newAllOther[i] = allOther[i];
			}
			allOther = FileBrowseUtil.sort(newAllOther);

			for (int i = 0; i < files.length; i++) {
				if (i < allDirCount) {
					files[i] = allDir[i];
				} else if (i < (allDirCount + allFilesCount)) {
					files[i] = allFiles[i - allDirCount];
				} else {
					files[i] = allOther[i - allDirCount - allFilesCount];
				}
			}
		}

		@Override
		public int getCount() {
			return files.length;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0 && TextUtils.equals(files[position], EMPTY)) {
				return 0;
			} else {
				return 1;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public Object getItem(int position) {
			return files[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (position == 0 && TextUtils.equals(files[position], EMPTY)) {
				TextView tv = new TextView(context);
				tv.setText(EMPTY);
				tv.setGravity(Gravity.CENTER);
				return tv;
			} else {
				View rootView;
				String fileName = files[position];
				if (convertView != null) {
					rootView = convertView;
				} else {
					rootView = View.inflate(context, R.layout.file_browse_list_view_item, null);
				}
				ImageView icoView = (ImageView) rootView.findViewById(R.id.file_browse_list_view_item_ico);
				TextView nameView = (TextView) rootView.findViewById(R.id.file_browse_list_view_item_name);
				TextView contentView = (TextView) rootView.findViewById(R.id.file_browse_list_view_item_content);
				String name = new File(fileName).getName();
				nameView.setText(name);
				File currentFile = new File(fileName);
				if (isFile(fileName)) {
					icoView.setImageResource(R.drawable.file_ico_txt);
					long size = mFileUtil.length(fileName);
					if (size == 0) {
						size = currentFile.length();
					}
					contentView.setText(file_leng + ":" + formatter.formatFileSize(context, size));
				} else if (isDirectory(fileName)) {

					long mFreeSpace = mFileUtil.getFreeSpace(fileName);
					if (mFreeSpace == 0) {
						mFreeSpace = currentFile.getFreeSpace();
					}
					String freeSpace = formatter.formatFileSize(context, mFreeSpace);
					long mTotalSpace = mFileUtil.getTotalSpace(fileName);
					if (mTotalSpace == 0) {
						mTotalSpace = currentFile.getTotalSpace();
					}
					String totalSpace = formatter.formatFileSize(context, mTotalSpace);
					icoView.setImageResource(R.drawable.file_ico_folder);
					contentView.setText(item_content_folder_1 + freeSpace + item_content_folder_2 + totalSpace);
				} else {
					icoView.setImageResource(R.drawable.file_ico_unknown);
					contentView.setText(formatter.formatFileSize(context, mFileUtil.length(fileName)));
				}
				rootView.setTag(files[position]);
				return rootView;
			}
		}

	}
}
