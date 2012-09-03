package net.cosrix;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity{
	
	private static final String FILENAME = "neuroconn_commnumber";
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        
        Button next = (Button) findViewById(R.id.save);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	
            	saveCommNumber();
            	
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }

        });
    }
    
    private void saveCommNumber(){

    	try{
        	EditText commNumberTextEdit = (EditText)findViewById(R.id.commnumber);
        	
        	String string = commNumberTextEdit.getText().toString();
	    	FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
	    	fos.write(string.getBytes());
	    	fos.close();
    	}
    	catch(FileNotFoundException fnfe){

    	}catch(IOException ioe){
    		
    	}
    }
}
