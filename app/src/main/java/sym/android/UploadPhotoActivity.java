package sym.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jGit.sym.src.GeneralUtil;
//import android.support.v7.app.AppCompatActivity;

/**
 * Created by Bhavya on 4/16/2016.
 */
public class UploadPhotoActivity extends Activity implements View.OnClickListener {
   private final int CONTACTS_RETURN = 200;
    private Button btnUploadPhots;
    Uri fileUri;
    ArrayList<String> imagesPathList;
    String filePath;
    DatabaseHandler db;
    private LinearLayout lnrImages;
    private Bitmap yourbitmap;
    MetaData mData;
    String groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        System.out.println("Inside Upload ");

       //showChangeLangDialog();
        db = new DatabaseHandler(this);
        lnrImages = (LinearLayout) findViewById(R.id.lnrImages1);
        imagesPathList = new ArrayList<String>();
        long id=0;

        Intent intent = getIntent();
        ArrayList<Uri> imagesPath ;
        imagesPath = intent.<Uri>getParcelableArrayListExtra("uris");

        Intent uploadIntent = new Intent(this, ContactsActivity.class);
        //uploadIntent.putParcelableArrayListExtra("uris", mArrayUri);
        startActivityForResult(uploadIntent, CONTACTS_RETURN);

        mData = PathMethods.processURIArray(this, imagesPath,"Test Group");
        //setting the created date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        mData.setCreatedDate(date);

/*
        try{
            lnrImages.removeAllViews();
        }catch (Throwable e){
            e.printStackTrace();
        }
        for (int i=0;i<imagesPath.length;i++){
            imagesPathList.add(imagesPath[i]);
            id = db.getMaxId()+1;
            yourbitmap = BitmapFactory.decodeFile(imagesPath[i]);

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(yourbitmap);
            imageView.setAdjustViewBounds(true);
            lnrImages.addView(imageView);
        }
*/
        btnUploadPhots = (Button) findViewById(R.id.btnUploadPhots);
        btnUploadPhots.setOnClickListener(this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==200){
            ArrayList<String> emailId = data.getStringArrayListExtra("data");
            mData.setUserList(emailId);

            mData.setGroupName("hari-1234");

            JSONObject j = new JSONObject();
            Map<String,String> map = ((TokenFetcher) this.getApplication()).getTokenMap();
            String token = map.get(mData.getUserList().get(0));
            Log.d("SendMessage","Token: "+token);
            String dataJson = "{ \"data\": {\n" +
                "    \"Message\": \""+mData.getGroupName()+"\",\n" +
                "    \"Created\": \""+mData.getCreatedDate()+"\"\n" +
                "  },\n" +
                "  \"to\" : \""+token+"\"\n" +
                "}";
            try {
                GeneralUtil.sendPost(dataJson);
                Log.e("Message","Message Sent");
            } catch (Exception e) {
                Log.e("Message","Message not send");
            }
            if(GeneralUtil.pushPhotos(mData))
                Log.d("Push Photos","Success");
            else
                Log.e("Push Photos", "Error");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {

             showChangeLangDialog();
/*
            try {
              //  addMetaData(metaDataList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
*/
            Intent i = new Intent();
            setResult(Activity.RESULT_OK, i);
            finish();

    }

    public void addMetaData(List<MetaData> metaDataList) throws JSONException {


        for(MetaData md : metaDataList){
            db.addMetaData(md);
        }

        List<MetaData> metadataReturn = db.getAllMetaData();
        for (MetaData md : metadataReturn) {
            String log = "Id: "+md.getId()+" ,Created Date: " + md.getCreatedDate() + " ,UserList: "
                    + md.getUserList()+" ,fileName: "+md.getFileNameList() + " ,groupName:"+md.getGroupName();
            // Writing Contacts to log
            System.out.println("Data: "+ log);
            System.out.println("Max Id"+db.getMaxId());
        }
        db.close();
        //Call method to add meta data to DB
    }

    public void showChangeLangDialog() {
        Log.i("ChgDialog","ShowChangeLangDialog");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        final String result;
        dialogBuilder.setView(dialogView);

        final EditText userInput = new EditText(getApplicationContext()); //(EditText) dialogView.findViewById(R.id.edit1);

        dialogBuilder.setTitle("Group Name");
        dialogBuilder.setMessage("Enter Group Name");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();

                 userInput.getText().toString();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                dialog.cancel();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();

        Log.i("ChgDialog", "Ended");

    }



}
