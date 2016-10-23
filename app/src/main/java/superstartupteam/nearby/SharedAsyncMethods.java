package superstartupteam.nearby;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.braintreepayments.api.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import layout.AccountFragment;
import layout.UpdateAccountDialogFragment;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 10/15/16.
 */
public class SharedAsyncMethods {

    public static String errorMessage;

    public static void getUserInfoFromServer(final User user, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    String output = AppUtils.getResponseContent(conn);
                    try {
                        User userFromServer = AppUtils.jsonStringToPojo(User.class, output);
                        userFromServer.setFacebookId(user.getFacebookId());
                        userFromServer.setAccessToken(user.getAccessToken());
                        PrefUtils.setCurrentUser(userFromServer, context);
                    } catch (IOException e) {
                        Log.e("Error", "Received an error while trying to read " +
                                "user info from server: " + e.getMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get user info from server: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    public static void updateUser(final User user, final Context context) {
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");
                    ObjectMapper mapper = new ObjectMapper();
                    String updateJson = mapper.writeValueAsString(user);
                    Log.i("updated account: ", updateJson);
                    byte[] outputInBytes = updateJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();

                    Log.i("PUT /users/me", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                    SharedAsyncMethods.getUserInfoFromServer(user, context);
                    return responseCode;
                } catch (IOException e) {
                    String errorMessage = "Could not update account: " + e.getMessage();
                    Log.e("ERROR ", errorMessage);
                }
                return 0;
            }

        }.execute();
    }

    public static void updateUser(final User user, final Context context,
                                  final MainActivity mainActivity, final View view) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");
                    ObjectMapper mapper = new ObjectMapper();
                    String updateJson = mapper.writeValueAsString(user);
                    Log.i("updated account: ", updateJson);
                    byte[] outputInBytes = updateJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();

                    Log.i("PUT /users/me", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                    SharedAsyncMethods.getUserInfoFromServer(user, context);
                    return responseCode;
                } catch (IOException e) {
                    errorMessage = "Could not update account: " + e.getMessage();
                    Log.e("ERROR ", errorMessage);
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode != null && responseCode == 200) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {

                    }
                    AccountFragment.dismissUpdateAccountDialog();
                    mainActivity.goToAccount("successfully updated account info");
                } else {
                    AccountFragment.dismissUpdateAccountDialog();
                    Snackbar snackbar = Snackbar
                            .make(view, errorMessage, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

        }.execute();
    }


}
