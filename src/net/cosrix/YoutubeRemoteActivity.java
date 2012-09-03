package net.cosrix;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class YoutubeRemoteActivity extends ListActivity {
	
	private ProgressDialog m_ProgressDialog = null;
    private ArrayList<Order> m_orders = null;
    private OrderAdapter m_adapter;
    private Runnable viewOrders;
    private EditText searchBox;
    private String keywords;
    private InputMethodManager mgr;
    private static final String FILENAME = "neuroconn_commnumber";
    private String commNumber;
	
    /** Called when the activity is first created. */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                showSettings(this.getWindow().getDecorView().findViewById(android.R.id.content));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void showSettings(View view){
    	Intent myIntent = new Intent(view.getContext(), SettingsActivity.class);
    	myIntent.putExtra("commNumberObject", commNumber);
        startActivityForResult(myIntent, 0);
    }
    
    public void getCommNumber(){
        try
        {        	
        	byte[] fileContent = new byte[1024];
        	FileInputStream fis = openFileInput(FILENAME);
   
        	int trueLenght = fis.read(fileContent);
        	
        	commNumber = new String(fileContent, 0, trueLenght);
        	Log.v("Idiot leghttttttttt", trueLenght+"");

    	   fis.close();
    	   if(commNumber == null){
    		   showSettings(this.getWindow().getDecorView().findViewById(android.R.id.content));
    	   }
        }
        catch(FileNotFoundException	fnfe)
        {
        	//If the file is not found we create it
        	Log.v("NO fileeeeeee", "No freaking foileeeeee");
        	showSettings(this.getWindow().getDecorView().findViewById(android.R.id.content));
        }
        catch(IOException ioe)
        {
        	showSettings(this.getWindow().getDecorView().findViewById(android.R.id.content));
        }
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	getCommNumber();
    	Log.v("Wha loooooooool", commNumber);
    }
    
    @Override    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        getCommNumber();
        //Binding the search process to the enter click on the EditText search box
        searchBox = (EditText) findViewById(R.id.search_box);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            	//if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            	mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            	mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
            	m_adapter.clear();
            	m_orders.clear(); 
            	keywords = v.getText().toString();
            	keywords = keywords.replace(" ", "+");
            	executeSearch();
                return true;
            }
        });
        
        //--------------------------------------
        //Showing the keyboard on startup
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //-------------------------------------
        m_orders = new ArrayList<Order>();
        this.m_adapter = new OrderAdapter(this, R.layout.row, m_orders);
        setListAdapter(this.m_adapter);
        //Setting the listener to get the videos IDs
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
              // When clicked, show a toast with the TextView text
            	Order order = m_orders.get(position);
            	//Context context = getApplicationContext();
            	String ytId = order.getYtId().toString();            	
            	sendCommand(ytId);
            }
         });
        //------------------------------------------
        viewOrders = new Runnable(){
            @Override
            public void run() {
                getOrders();
            }
	        private void getOrders(){
	            try{
	            	HttpClient client = new DefaultHttpClient();
	            	String completeURL = "https://gdata.youtube.com/feeds/api/videos?q="+keywords+"&max-results=50&v=2&alt=jsonc";
	                // Perform a GET request to YouTube for a JSON list of all the videos by a specific user
	                HttpUriRequest request = new HttpGet(completeURL);
	                // Get the response that YouTube sends back
	                HttpResponse response = client.execute(request);
	                // Convert this response into a readable string
	                String jsonString = StreamUtils.convertToString(response.getEntity().getContent());
	                // Create a JSON object that we can use from the String
	                JSONObject json = new JSONObject(jsonString);
	     
	                // For further information about the syntax of this request and JSON-C
	                // see the documentation on YouTube http://code.google.com/apis/youtube/2.0/developers_guide_jsonc.html
	     
	                // Get are search result items
	                JSONArray jsonArray = json.getJSONObject("data").getJSONArray("items");
	                
	                
	                m_orders = new ArrayList<Order>();
	                for (int i = 0; i < jsonArray.length(); i++) {
	                    JSONObject jsonObject = jsonArray.getJSONObject(i);
	                    // The title of the video
	                    String title = jsonObject.getString("title");
	                    String ytId = jsonObject.getString("id");
	                    
	                    // A url to the thumbnail image of the video
	                    // We will use this later to get an image using a Custom ImageView
	                    // Found here http://blog.blundell-apps.com/imageview-with-loading-spinner/
	                    //String thumbUrl = jsonObject.getJSONObject("thumbnail").getString("url");
	                    
	                    String thumbUrl = jsonObject.getJSONObject("thumbnail").getString("sqDefault");
	                    // Create the video object and add it to our list
	                    
	                    String rawDuration = jsonObject.getString("duration");
	                    
	                    String formatedDuration = calculateDuration(rawDuration);
	                    
	                    Order v = new Order();
	                    v.setOrderName(title);
	                    v.setOrderStatus(formatedDuration);
	                    v.setThumb(thumbUrl);
	                    v.setYtId(ytId);
	                    Log.i("order_name", v.getOrderName());
	                    m_orders.add(v);
	                }
	                
	                Log.i("ARRAY", ""+ m_orders.size());
	              } catch (Exception e) {
	                Log.e("BACKGROUND_PROC", e.getMessage());
	              }
	              runOnUiThread(returnRes);
	          }
	        
	        private String calculateDuration(String rawDuration){
	        	String finalDuration = null;
	        	int intRawDuration = Integer.parseInt(rawDuration);
	        	int min = intRawDuration / 60;
	        	int sec = intRawDuration % 60;
	        	finalDuration = min+":"+sec;
	        	return finalDuration;
	        }
	        
	        private Runnable returnRes = new Runnable() {

	            @Override
	            public void run() {
	                if(m_orders != null && m_orders.size() > 0){
	                    m_adapter.notifyDataSetChanged();
	                    for(int i=0;i<m_orders.size();i++) m_adapter.add(m_orders.get(i));
	                }
	                m_ProgressDialog.dismiss();
	                m_adapter.notifyDataSetChanged();
	            }
	          };            
        };

    }
    
    
    private void executeSearch(){
    	Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(this,    
              "Please wait...", "Retrieving data ...", true);
    }
    
    private void sendCommand(String ytVideoId){
    	//Code to send messages to RabbitMq
    	//Building the message from the input elements to Json
    	HashMap<String, String> commands = new HashMap<String, String>();
    	String payload = "{\"videoid\":\""+ytVideoId+"\"}";
    	commands.put("payload", payload);
    	commands.put("addr", commNumber);
    	JSONObject jsonObject = new JSONObject(commands);
        String jsonMessageString = jsonObject.toString();
        sendToRelay(jsonMessageString);
    }
    
    private void sendToRelay(String jsonMessageString){
    	try {
    		Socket sendSocket =  new Socket("184.72.57.4", 1337);
    		if (sendSocket.isConnected()){
    			OutputStream ops = sendSocket.getOutputStream();
    			ops.write(jsonMessageString.getBytes());
    			sendSocket.close();
    		}
    	}catch (Exception e){
    		
    	}
    }

    
    private class OrderAdapter extends ArrayAdapter<Order> {

        private ArrayList<Order> items;

        public OrderAdapter(Context context, int textViewResourceId, ArrayList<Order> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                
                
                Order o = items.get(position);
                
                if (o != null) {
                		
                        TextView tt = (TextView) v.findViewById(R.id.toptext);
                        TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                        UrlImageView img = (UrlImageView) v.findViewById(R.id.icon);
                        if (tt != null) {
                              tt.setText(o.getOrderName());                            }
                        if(bt != null){
                              bt.setText(o.getOrderStatus());
                        }
                        
                        if (img != null){
                        	
                        	img.setImageDrawable(o.getThumb());
                        	
                        }
                }
                return v;
        }
        
        public OnClickListener clickListener = new OnClickListener() {
        	public void onClick(View v) {
        		
        	}
        };

    }
    
}

