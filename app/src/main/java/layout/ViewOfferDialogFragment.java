package layout;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import superstartupteam.nearby.Constants;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 9/16/16.
 */
public class ViewOfferDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private static Response response;
    private static Request request;
    private HistoryFragment.OnFragmentInteractionListener mListener;
    private Context context;
    private EditText offerPrice;
    private Spinner offerType;
    private EditText pickupLocation;
    private EditText returnLocation;
    private Button updateRequestBtn;
    private Button rejectRequestBtn;
    private ImageButton backButton;
    private TextView pickupTime;
    private TextView returnTime;
    private User user;


    public ViewOfferDialogFragment() {

    }

    public static ViewOfferDialogFragment newInstance(Response r, Request req) {
        response = r;
        request = req;
        ViewOfferDialogFragment fragment = new ViewOfferDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_offer_dialog, container, false);
        pickupTime = (TextView) view.findViewById(R.id.pickup_time);
        if (response.getExchangeTime() != null) {
            pickupTime.setText(response.getExchangeTime().toString());
        } else {
            String htmlString = "<b>SET</b>";
            pickupTime.setText(Html.fromHtml(htmlString));
        }

        pickupTime.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // date time picker
                    final View dateTimeView = View.inflate(context, R.layout.date_time_picker, null);
                    final AlertDialog alertDialog = new Builder(context).create();

                    dateTimeView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            DatePicker datePicker = (DatePicker) dateTimeView.findViewById(R.id.date_picker);
                            TimePicker timePicker = (TimePicker) dateTimeView.findViewById(R.id.time_picker);

                            Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                    datePicker.getMonth(),
                                    datePicker.getDayOfMonth(),
                                    timePicker.getCurrentHour(),
                                    timePicker.getCurrentMinute());

                            Date newPickupTime = new Date(calendar.getTimeInMillis());
                            pickupTime.setText(newPickupTime.toString());
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.setView(dateTimeView);
                    alertDialog.show();
                    return true;
                }
                return false;
            }
        });

        pickupLocation = (EditText) view.findViewById(R.id.pickup_location);
        pickupLocation.setText(response.getExchangeLocation());
        returnLocation = (EditText) view.findViewById(R.id.return_location);
        returnTime = (TextView) view.findViewById(R.id.return_time);
        if (!request.getRental()) {
            returnLocation.setVisibility(View.GONE);
            returnTime.setVisibility(View.GONE);
            TextView returnLocationLabel = (TextView) view.findViewById(R.id.return_location_label);
            returnLocationLabel.setVisibility(View.GONE);
            TextView returnTimeLabel = (TextView) view.findViewById(R.id.return_time_label);
            returnTimeLabel.setVisibility(View.GONE);
        } else {
            setDateTimeFunctionality(returnTime, false);
        }
        offerPrice = (EditText) view.findViewById(R.id.response_offer_price);
        offerPrice.setText(response.getOfferPrice().toString());
        offerType = (Spinner) view.findViewById(R.id.offer_type);
        if (response.getPriceType().toLowerCase().equals("flat")) {
            offerType.setSelection(0);
        } else if (response.getPriceType().toLowerCase().equals("per_hour")) {
            offerType.setSelection(1);
        } else {
            offerType.setSelection(2);
        }
        backButton = (ImageButton) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        updateRequestBtn = (Button) view.findViewById(R.id.accept_offer_button);
        updateRequestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateResponse();
            }
        });
        rejectRequestBtn = (Button) view.findViewById(R.id.reject_offer_button);
        rejectRequestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                declineResponse();
            }
        });
        return view;
    }

    private void declineResponse() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests/" + request.getId() + "/responses/" + response.getId());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");

                    if (request.getUser().getId().equals(user.getId())) {
                        response.setBuyerStatus(Response.BuyerStatus.ACCEPTED);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    Response r = response;
                    r.setSeller(null);
                    if (user.getId().equals(request.getUser().getId())) {
                        r.setBuyerStatus(Response.BuyerStatus.DECLINED);
                    } else {
                        r.setSellerStatus(Response.SellerStatus.WITHDRAWN);
                    }
                    String responseJson = mapper.writeValueAsString(r);
                    Log.i("updated response: ", responseJson);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("PUT /responses", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update offer: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    dismiss();
                    ((MainActivity) getActivity()).goToHistory();
                }
            }
        }.execute();
    }

    private void updateResponse() {
        updateResponseObject();
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests/" + request.getId() + "/responses/" + response.getId());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");

                    if (request.getUser().getId().equals(user.getId())) {
                        response.setBuyerStatus(Response.BuyerStatus.ACCEPTED);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    Response r = response;
                    r.setSeller(null);
                    String responseJson = mapper.writeValueAsString(r);
                    Log.i("updated response: ", responseJson);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("PUT /responses", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update offer: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    dismiss();
                    ((MainActivity) getActivity()).goToHistory();
                }
            }
        }.execute();
    }

    private void updateResponseObject() {
        double offer = Double.parseDouble(offerPrice.getText().toString());
        response.setOfferPrice(offer);
        String type = offerType.getSelectedItem().toString();
        if (type.equals("per day")) {
            type= "per_day";
        } else if (type.equals("per hour")) {
            type = "per_hour";
        }
        response.setPriceType(type);
        response.setExchangeLocation(pickupLocation.getText().toString());
        if (!pickupTime.getText().toString().equals("SET")) {
            Date eDate = new Date(pickupTime.getText().toString());
            response.setExchangeTime(eDate);
        }
        if (request.getRental()) {
            response.setReturnLocation(returnLocation.getText().toString());
            if (!returnTime.getText().toString().equals("SET")) {
                Date eDate = new Date(returnTime.getText().toString());
                response.setReturnTime(eDate);
            }
        }
    }

    private void setDateTimeFunctionality(final TextView textView, boolean pickup) {
        if (pickup ? response.getExchangeTime() != null : response.getReturnTime() != null) {
            textView.setText(pickup ? response.getExchangeTime().toString() : response.getReturnTime().toString());
        } else {
            String htmlString = "<b>SET</b>";
            textView.setText(Html.fromHtml(htmlString));
        }

        textView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // date time picker
                    final View dateTimeView = View.inflate(context, R.layout.date_time_picker, null);
                    final AlertDialog alertDialog = new Builder(context).create();

                    dateTimeView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            DatePicker datePicker = (DatePicker) dateTimeView.findViewById(R.id.date_picker);
                            TimePicker timePicker = (TimePicker) dateTimeView.findViewById(R.id.time_picker);

                            Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                    datePicker.getMonth(),
                                    datePicker.getDayOfMonth(),
                                    timePicker.getCurrentHour(),
                                    timePicker.getCurrentMinute());

                            Date newPickupTime = new Date(calendar.getTimeInMillis());
                            textView.setText(newPickupTime.toString());
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.setView(dateTimeView);
                    alertDialog.show();
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof HistoryFragment.OnFragmentInteractionListener) {
            mListener = (HistoryFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a radius from the spinner
        String value = (String) parent.getItemAtPosition(position);

    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
