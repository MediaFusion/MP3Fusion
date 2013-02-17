package com.mediafusion.mp3fusion;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btn = (Button) findViewById(R.id.submit);
		ImageView facebook = (ImageView) findViewById(R.id.facebook);
		ImageView twitter = (ImageView) findViewById(R.id.twitter);

		Bundle Intents = getIntent().getExtras();
		if (Intents != null) {
			String IntentBullshit = Intents.getString(Intent.EXTRA_TEXT)
					.toString();
			((EditText) findViewById(R.id.url)).setText(IntentBullshit);
		}

		AdView adView = (AdView) findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

		twitter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("twitter://user?screen_name=MediaFusionLTD")));
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("https://twitter.com/MediaFusionLTD")));
				}
			}
		});
		facebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					MainActivity.this.getPackageManager().getPackageInfo(
							"com.facebook.katana", 0);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("fb://profile/153931891423903")));
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("https://www.facebook.com/MediaFusionLTD")));
				}
			}
		});
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// first check that there is input

				String input = ((EditText) findViewById(R.id.url)).getText()
						.toString();
				if (input.length() == 0) {
					Toast.makeText(MainActivity.this,
							"Please enter Youtube Video", Toast.LENGTH_LONG)
							.show();
				} else {
					new DownloadFileAsync().execute(input);
				}
			}
		});
		if (!Online()) {
			Toast.makeText(MainActivity.this, "No Internet Connection !",
					Toast.LENGTH_LONG).show();
		}
		SharedPreferences runCheck = getSharedPreferences("hasRunBefore",
				Context.MODE_PRIVATE);
		Boolean hasRun = runCheck.getBoolean("hasRun", false);
		if (!hasRun) {
			SharedPreferences settings = getSharedPreferences("hasRunBefore", 0);
			SharedPreferences.Editor edit = settings.edit();
			edit.putBoolean("hasRun", true);
			edit.commit();
			show_help();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

	public void social_networks(String social) {
		if (social == "facebook") {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.facebook.com"));
			startActivity(browserIntent);
		}
		if (social == "twitter") {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.twitter.com"));
			startActivity(browserIntent);
		}
		if (social == "gplus") {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.google.com"));
			startActivity(browserIntent);
		}

	}

	public boolean Online() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == R.id.menu_help) {
			show_help();
		}
		return true;
	}

	public void show_help() {
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
				.create(); // Read Update
		alertDialog.setTitle("Need Help ?");
		alertDialog
				.setMessage("There are two methods to use this software :\n\n"
						+ "1)You can copy paste Youtube video Link into the form OR\n\n"
						+ "2)You can launch Youtube Application,"
						+ "Click on Share button,"
						+ "From the menu pick 'MP3Fusion'\n\n"
						+ "Finally Hit Download ! to start downloading your MP3");
		alertDialog.show();
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {

		public TextView pt = (TextView) findViewById(R.id.pt);
		public TextView sn = (TextView) findViewById(R.id.sn);
		public TextView dl = (TextView) findViewById(R.id.dl);
		public View adView = (View) findViewById(R.id.adView);
		public ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
		public Button bt = (Button) findViewById(R.id.submit);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			visibility_switcher("visible");
			// disallow screen rotation
			int current_orientation = getResources().getConfiguration().orientation;
			if (current_orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			if (progress[0] == "error") {
				visibility_switcher("invisible");
				Toast.makeText(MainActivity.this, progress[1],
						Toast.LENGTH_LONG).show();
				this.cancel(true);
			} else {
				((ProgressBar) pb).setProgress(Integer.parseInt(progress[1]));
				((TextView) pt).setText(Integer.parseInt(progress[1]) + "%");
				sn.setText(progress[2]);
			}

		}

		@Override
		protected void onPostExecute(String result) {
			visibility_switcher("invisible");
			Toast.makeText(MainActivity.this, "File Downloaded Successfuly !",
					Toast.LENGTH_LONG).show();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

		}

		public void visibility_switcher(String mode) {
			if (mode == "visible") {
				pb.setVisibility(View.VISIBLE);
				pt.setVisibility(View.VISIBLE);
				sn.setVisibility(View.VISIBLE);
				dl.setVisibility(View.VISIBLE);
				adView.setVisibility(View.VISIBLE);
				bt.setVisibility(View.INVISIBLE);
			} else if (mode == "invisible") {
				pb.setVisibility(View.GONE);
				pt.setVisibility(View.GONE);
				sn.setVisibility(View.GONE);
				dl.setVisibility(View.GONE);
				adView.setVisibility(View.GONE);
				bt.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;
			URL apilink = null;
			URL json = null;
			String Json = null;
			String pushItem;
			String hash = null;
			String Dl_link = null;
			BufferedReader in = null;
			BufferedReader out = null;
			long timenow = System.currentTimeMillis();
			try {

				String vid = null;
				try {
					String url = aurl[0].toString();
					int vindex = url.indexOf("v=");
					int ampIndex = url.indexOf("&", vindex);

					if (ampIndex < 0) {
						vid = url.substring(vindex + 2);
					} else {
						vid = url.substring(vindex + 2, ampIndex);
					}
				} catch (Exception e) {
					String[] data = new String[2];
					data[0] = "error";
					data[1] = "Invalid Youtube Link or malformed";
					publishProgress(data);
				}
				apilink = new URL(
						"http://www.youtube-mp3.org/api/pushItem/?item=http://www.youtube.com/watch?v="
								+ vid.toString() + "&xy=yx&bf=false&r="
								+ timenow);
				in = new BufferedReader(new InputStreamReader(
						apilink.openStream()));
				pushItem = in.readLine();
				// now we check for server errors
				if (in.readLine() == "$$$ERROR$$$") {
					throw new Exception("$$$ERROR$$$");
				}
				if (in.readLine() == "$$$LIMIT$$$") {
					throw new Exception("$$$LIMIT$$$");
				}
				json = new URL(
						"http://www.youtube-mp3.org/api/itemInfo/?video_id="
								+ pushItem.toString() + "&ac=www&r=" + timenow);
				out = new BufferedReader(new InputStreamReader(
						json.openStream()));
				Json = out.readLine();
				hash = Json.substring(7);
				hash = hash.substring(0, hash.length() - 1);

				JSONObject myjson = new JSONObject(hash);
				String title = "" + myjson.get("title").toString();
				Dl_link = "http://www.youtube-mp3.org/get?video_id=" + pushItem
						+ "&h=" + myjson.get("h").toString() + "&r=" + timenow;
				URL url = new URL(Dl_link);
				URLConnection conexion = url.openConnection();
				conexion.connect();
				char fileSep = '/';
				char escape = '%';

				int len = title.length();
				StringBuilder sb = new StringBuilder(len);
				for (int i = 0; i < len; i++) {
					char ch = title.charAt(i);
					if (ch < ' ' || ch >= 0x7F || ch == fileSep
							|| (ch == '.' && i == 0) || ch == escape) {
						sb.append(escape);
						if (ch < 0x10) {
							sb.append('0');
						}
						sb.append(Integer.toHexString(ch));
					} else {
						sb.append(ch);
					}
				}
				// create folder if it doesn't exist
				File __DIR__ = new File(Environment
						.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_MUSIC).toString()
						+ "/MP3Fusion/");
				// have the object build the directory structure, if needed.
				__DIR__.mkdirs();
				int lenghtOfFile = conexion.getContentLength();
				InputStream input = new BufferedInputStream(url.openStream());

				OutputStream output = new FileOutputStream(Environment
						.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_MUSIC).toString()
						+ "/MP3Fusion/" + sb.toString() + ".mp3");

				byte data[] = new byte[1024];
				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					String[] progress = new String[3];
					progress[0] = "progress";
					progress[1] = "" + (int) ((total * 100) / lenghtOfFile);
					progress[2] = sb.toString();
					publishProgress(progress);
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();

			} catch (MalformedURLException e) {
				String[] data = new String[2];
				data[0] = "error";
				data[1] = "ERROR : Invalid Youtube link";
				publishProgress(data);
			} catch (IOException e) {
				String[] data = new String[2];
				data[0] = "error";
				data[1] = "ERROR : Video Copyrighted";
				publishProgress(data);

			} catch (JSONException e) {
				String[] data = new String[2];
				data[0] = "error";
				data[1] = "ERROR : Cannot validate video content";
				publishProgress(data);
			} catch (Exception e) {
				if (e.getMessage() == "$$$ERROR$$$") {
					String[] data = new String[2];
					data[0] = "error";
					data[1] = "Cannot download MP3 : Video exceeded 20 minutes or is copyrighted";
					publishProgress(data);
				} else if (e.getMessage() == "$$$LIMIT$$$") {
					String[] data = new String[2];
					data[0] = "error";
					data[1] = "Limit Exceeded : only 15 video conversion is allowed per 30 minutes";
					publishProgress(data);
				} else {
					String[] data = new String[2];
					data[0] = "error";
					data[1] = "EXCEPTION : not enough space or unknown issue...";
					Log.d("expection", Log.getStackTraceString(e));
					publishProgress(data);
				}
			}

			return null;

		}

	}
}
