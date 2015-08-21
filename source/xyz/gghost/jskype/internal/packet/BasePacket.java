package xyz.gghost.jskype.internal.packet;

import lombok.Getter;
import lombok.Setter;
import xyz.gghost.jskype.api.LocalAccount;
import xyz.gghost.jskype.api.SkypeAPI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BasePacket {
    protected SkypeAPI api;
    //TODO: Recode -> this is from an older version of jSkype
    @Getter @Setter private String data = "";
    @Getter @Setter private String url = "";
    @Getter @Setter private RequestType type = null;
    @Getter @Setter private Boolean isForm = false;
    @Getter private ArrayList<Header> headers = new ArrayList<Header>();
    @Getter private HttpURLConnection con;

    public BasePacket(SkypeAPI api) {
        this.api = api;
    }

    @Deprecated
    private void addLogin(LocalAccount usr) {
        addHeader(new Header("RegistrationToken", usr.getRegToken()));
        addHeader(new Header("X-Skypetoken", usr.getXSkypeToken()));
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    public String makeRequest(LocalAccount usr) {
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod((type == RequestType.GET ? "GET" : (type == RequestType.POST ? "POST" : (type == RequestType.PUT ? "PUT" : (type == RequestType.DELETE ? "DELETE" : "OPTIONS")))));

            con.setRequestProperty("Content-Type", isForm ? "application/x-www-form-urlencoded" : "application/json");
            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            //con.setRequestProperty("User-Agent", "JavaSkypeProject");
            con.setRequestProperty("Cookie", api.cookies);
            con.setDoOutput(true);
            addLogin(usr);
            for (Header s : headers) {
                con.addRequestProperty(s.getType(), s.getData());
            }
            if (!(data.getBytes().length == 0)) {
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.write(data.getBytes());
                wr.flush();
                wr.close();
            }
            int responseCode = con.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString() == null ? "" : response.toString();

            } else if (responseCode == 401) {
                System.out.println("\n\nBad login...");
                System.out.println(this.url + " returned 401. \nHave you been running jSkype for more than 2 days?\nWithin 4 seconds the ping-er should relog you in.\n\n");
                return "---";
            } else {
                //GetProfile will handle the debugging info
                if(url.equals("https://api.skype.com/users/self/contacts/profiles"))
                    return null;
                //Debug info
                System.out.println("Error contacting skype\nUrl: "+ url + "\nCode: "+responseCode + "\nData: " + data );
                for (Header header : headers){
                    System.out.println(header.getType() + ": " + header.getData());
                }
                return null;
            }
        } catch (Exception e) {
            System.out.println("================================================");
            System.out.println("========Unable to request  the skype api========");
            System.out.println("================================================");
            e.printStackTrace();
            return null;
        }
    }

    /***
     * a simple request WILL NOT process any data received, you'll have to do that.
     *
     * @return
     */
    public HttpURLConnection simpleRequest() {
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod((type == RequestType.GET ? "GET" : (type == RequestType.POST ? "POST" : (type == RequestType.PUT ? "PUT" : (type == RequestType.DELETE ? "DELETE" : "OPTIONS")))));

            con.setRequestProperty("Content-Type", isForm ? "application/x-www-form-urlencoded" : "application/json");
            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            con.setRequestProperty("Content-Language", "en-US");
            con.setRequestProperty("User-Agent", "JavaSkypeProject");
            con.setRequestProperty("cookie", api.cookies);
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);

            for (Header s : headers) {
                con.setRequestProperty(s.getType(), s.getData());
            }

            if (!(data.getBytes().length == 0)) {
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();
            }
            return con;
        } catch (Exception e) {
            System.out.println("================================================");
            System.out.println("========Unable to request  the skype api========");
            System.out.println("================================================");
            e.printStackTrace();
            return null;
        }
    }

}