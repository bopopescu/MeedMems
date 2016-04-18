package jGit.sym.src;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import org.eclipse.jgit.api.errors.GitAPIException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kiran on 4/18/16.
 */
public class GeneralUtil {
    // HTTP POST request
    public static String sendPost(String data) throws Exception {

        String url = "https://gcm-http.googleapis.com/gcm/send";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "key=AIzaSyCf_1ropZcd-C5aqXNxZqW1H2WmHwDVaoA");

//        String data = "{ \"data\": {\n" +
//                "    \"score\": \"5x1\",\n" +
//                "    \"time\": \"15:10\"\n" +
//                "  },\n" +
//                "  \"to\" : \"efmgSA7UmlE:APA91bEaFNbzaX7MBDsq0P68gPmFyYjQLJLwvzzyuLPmDmnD5qY-kRCQ3Agh1mZDTHN5wFM1_vd7uBU5XZjWo2U1GUcmNIDEHqBULsuJb9s7AujjZLQH3Y5GKx4diEoU2AkPexX4R6Pz\"\n" +
//                "}";
        // Send post request

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        Log.d("POST call URL-", url);
        Log.d("Response Code : ", Integer.toString(responseCode));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Log.d("POST Call Response -",response.toString());
        return response.toString();

    }

    public static Map<String,String> localUpdateTokenMap(Context context) {

        String repoName="SYM";
        JGitOps gitUtil = new JGitOps(context);
        File f = new File(context.getFilesDir().getPath()+"/"+repoName);
        if(!f.isDirectory()) {
            try {
                gitUtil.setTracker(repoName);
                gitUtil.pull(repoName);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }else {
            try {
                gitUtil.clone(repoName);
                gitUtil.setTracker(repoName);
            } catch (GitAPIException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return readFile(f.getAbsolutePath()+"/tokensmetadata");

    }

    public static void remoteUpdateTokenMap(Context context, String emailid, String token) {

        String repoName="SYM";
        JGitOps gitUtil = new JGitOps(context);
        File f = new File(context.getFilesDir().getPath()+"/"+repoName);
        writeFile(f.getAbsolutePath()+"/tokensmetadata", emailid, token);

        try {
            gitUtil.setTracker(repoName);
            gitUtil.add(repoName);
            gitUtil.commit(repoName,"committing new entry for "+emailid);
            gitUtil.push(repoName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,String> readFile(String filePath) {

        Map<String,String> testMap = new HashMap<String, String>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim() != "") {
                    testMap.put(line.split("=")[0], line.split("=")[1]);
                }
            }
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return testMap;
    }

    public static void writeFile(String filePath, String emailid, String token) {
        try
        {
            FileWriter fw = new FileWriter(filePath,true); //the true will append the new data
            fw.write(emailid+"="+token+"\n");//appends the string to the file
            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }
}