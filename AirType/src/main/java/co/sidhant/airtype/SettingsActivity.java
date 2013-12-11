package co.sidhant.airtype;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class SettingsActivity extends Activity {

    private ProgressBar progressBar;
    private TextView progressText;
    private Button setupButton;
    private boolean asyncStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupButton = (Button) findViewById(R.id.setupButton);
        asyncStart = false;
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    public class TrieGenTask extends AsyncTask<Void, Integer, Void>
    {
        public AssetManager am;
        //Before running code in separate thread
        @Override
        protected void onPreExecute()
        {
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressText = (TextView) findViewById(R.id.progressText);
        }

        //The code to be executed in a background thread.
        @Override
        protected Void doInBackground(Void... params)
        {
            InputStream is = null;
            try {
                is = am.open("permutations.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Count the number of lines in permutations.txt
            int lineCount = 0;
            try {
                byte[] c = new byte[1024];
                lineCount = 0;
                int readChars;
                while ((readChars = is.read(c)) != -1) {
                    for (int i = 0; i < readChars; ++i) {
                        if (c[i] == '\n') {
                            ++lineCount;
                        }
                    }
                }
                is.reset();
            }catch (IOException e)
            {
                e.printStackTrace();
            }

            TreeMap<String, ArrayList<String>> permutationMap = new TreeMap<String, ArrayList<String>>(new AirType.stringLengthComparator());
            Scanner s = new Scanner(is);
            String curLine;
            int curLineNum = 0;
            int progress;
            String curKey = "";
            ArrayList<String> curList;
            while(s.hasNextLine())
            {
                curLine = s.nextLine();
                curLineNum++;
                if(curLine.endsWith(":"))
                {
                    curKey = curLine.substring(0, curLine.length() - 2);
                    curList = new ArrayList<String>();
                    curList.add(s.nextLine());
                    permutationMap.put(curKey, curList);
                }
                else
                {
                    curList = permutationMap.get(curKey);
                    curList.add(curLine);
                    permutationMap.put(curKey, curList);
                }
                progress  = (int) (((float)curLineNum / (float)lineCount) * 25.0f);
                publishProgress(progress);
            }

            TrieMaker.trieGenTask = this;

            publishProgress(25);

            AirTrie curTrie = TrieMaker.makeTrie(new FingerMap(), permutationMap);

            publishProgress(50);

            EncodedTrie encodedTrie= new EncodedTrie(curTrie, this);
            try {
                encodedTrie.writeBitSets(getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            publishProgress(100);

            return null;
        }

        // Publish the progress from elsewhere
        public void publish(int i)
        {
            publishProgress(i);
        }

        //Update the progress
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            //set the current progress of the progress dialog
            progressBar.setProgress(values[0]);
            progressText.setText(values[0].toString() + "%");
            if(values[0] == 100)
            {
                TextView text = (TextView) findViewById(R.id.textView);
                text.setText("Initial setup complete! Type away!");
                setupButton.setText(":)");
            }
        }

        //after executing the code in the thread
        @Override
        protected void onPostExecute(Void result)
        {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startAsyncTask(View view)
    {
        if(!asyncStart)
        {
            TrieGenTask trieGenTask = new TrieGenTask();
            trieGenTask.am = getAssets();
            trieGenTask.execute();
            if(setupButton == null)
            {
                setupButton = (Button) findViewById(R.id.setupButton);
            }
            setupButton.setText("Please Wait");
            setupButton.setClickable(false);
        }
        asyncStart = true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
            return rootView;
        }
    }

}
