package superstartupteam.nearby;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.ArrayList;
import java.util.List;

import layout.HistoryFragment;
import superstartupteam.nearby.model.History;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.Transaction;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 8/28/16.
 */
public class HistoryCardAdapter extends ExpandableRecyclerAdapter<HistoryCardAdapter.HistoryCardViewHolder, HistoryCardAdapter.ResponseViewHolder> {
    private List<History> recentHistory;
    private User user;
    private LayoutInflater mInflater;
    private HistoryFragment historyFragment;
    private Context context;

    public HistoryCardAdapter(Context context, List<ParentObject> parentItemList, HistoryFragment fragment) {
        super(context, parentItemList);
        List<History> objs = new ArrayList<>();
        for (ParentObject p : parentItemList) {
            objs.add((History) p);
        }
        this.recentHistory = objs;
        historyFragment = fragment;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public void onBindChildViewHolder(final ResponseViewHolder responseViewHolder, int i, Object obj) {
        final Response r = (Response) obj;
        String requestStatus = "";
        for (History h: recentHistory) {
            if (h.getRequest().getId().equals(r.getRequestId())) {
                requestStatus = h.getRequest().getStatus();
            }
        }
        responseViewHolder.mOfferAmount.setText(r.getOfferPrice().toString());
        responseViewHolder.mResponderName.setText(r.getSeller().getFirstName());
        if (r.getPriceType().equals("per_hour")) {
            responseViewHolder.mPriceType.setText(" per hour");
        } else if (r.getPriceType().equals("per_day")) {
            responseViewHolder.mPriceType.setText(" per day");
        }

        if (r.getResponseStatus().toString().toLowerCase().equals("closed")
                || requestStatus.toLowerCase().equals("closed")) {
            responseViewHolder.mResponseDetailsButton.setVisibility(View.GONE);
        } else {
            responseViewHolder.mResponseDetailsButton.setVisibility(View.VISIBLE);
        }
        responseViewHolder.mResponseStatus.setText(r.getResponseStatus().toString());
        setResponseStatusColor(responseViewHolder.mResponseStatus, r.getResponseStatus().toString());
        responseViewHolder.mResponseDetailsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                historyFragment.showResponseDialog(r);
            }
        });
    }

    @Override
    public void onBindParentViewHolder(final HistoryCardViewHolder requestViewHolder, int i, Object obj) {
        final History h = (History) obj;
        Request r = h.getRequest();
        if (h.getTransaction() != null && !r.getStatus().toLowerCase().equals("closed")) {
            setUpTransactionCard(requestViewHolder, r, h);
        } else if (user.getId().equals(r.getUser().getId())) { // this is a request the user made
           setUpRequestCard(requestViewHolder, r, h);
        } else { //this is an offer the user made
            setUpOfferCard(requestViewHolder, r, h);
        }
        if (!requestViewHolder.showConfirmChargeIcon && !requestViewHolder.showEditIcon &&
                !requestViewHolder.showCancelTransactionIcon && !requestViewHolder.showExchangeIcon) {
            requestViewHolder.menuBtn.setVisibility(View.GONE);
        }
        requestViewHolder.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(requestViewHolder.menuBtn, h, requestViewHolder);
            }
        });
    }

    @Override
    public HistoryCardViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        user = PrefUtils.getCurrentUser(viewGroup.getContext());
        Context context = viewGroup.getContext();
        this.context = context;
        View view = mInflater.inflate(R.layout.my_history_card, viewGroup, false);
        return new HistoryCardViewHolder(context, view);
    }

    @Override
    public ResponseViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.request_responses, viewGroup, false);
        return new ResponseViewHolder(view);
    }


    public void swap(List<History> newHistory) {
        recentHistory.clear();
        recentHistory.addAll(newHistory);
        notifyDataSetChanged();
    }

    private void setResponseStatusColor(TextView responseStatus, String status) {
        status = status.toLowerCase();
        switch (status) {
            case "accepted":
                responseStatus.setTextColor(Color.parseColor("#4EE2EC"));
                break;
            case "pending":
                responseStatus.setTextColor(Color.parseColor("#FFD700"));
                break;
            case "closed":
                responseStatus.setTextColor(Color.parseColor("#E52B50"));
                break;
        }
    }

    private void setRequestStatusColor(TextView requestStatus, String status) {
        status = status.toLowerCase();
        switch (status) {
            case "open":
                requestStatus.setTextColor(Color.GREEN);
                break;
            case "closed":
                requestStatus.setTextColor(Color.parseColor("#E52B50"));
                break;
            case "fulfilled":
                requestStatus.setTextColor(Color.parseColor("#4EE2EC"));
                break;
        }
    }

    private void setUpOfferCard(HistoryCardViewHolder requestViewHolder, Request r, final History h) {
        requestViewHolder.showExchangeIcon = false;
        requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
        requestViewHolder.mCardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.request_card_background));
        requestViewHolder.showEditIcon = false;
        final Response resp = h.getResponses().get(0);
        String buyerName = r.getUser().getFirstName() != null ?
                r.getUser().getFirstName() : r.getUser().getFullName();
        String htmlString = "offered a <b>" +
                r.getItemName() + "</b> to " + buyerName;
        String diff = AppUtils.getTimeDiffString(h.getResponses().get(0).getResponseTime());
        requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
        requestViewHolder.vPostedDate.setText(diff);
        requestViewHolder.vCategoryName.setText("");
        requestViewHolder.vCategoryName.setVisibility(View.GONE);
        requestViewHolder.vDescription.setText("");
        requestViewHolder.vDescription.setVisibility(View.GONE);
        requestViewHolder.vStatus.setText(resp.getResponseStatus().toString());
        setResponseStatusColor(requestViewHolder.vStatus, resp.getResponseStatus().toString());
        requestViewHolder.vParentDropDownArrow.setVisibility(View.GONE);
        if (resp.getResponseStatus().equals(Response.Status.CLOSED)) {
            requestViewHolder.showEditIcon = false;
        } else {
            requestViewHolder.showEditIcon = true;
        }
    }

    private void setUpRequestCard(HistoryCardViewHolder requestViewHolder, Request r, final History h) {
        requestViewHolder.vPostedDate.setVisibility(View.VISIBLE);
        requestViewHolder.mCardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.request_card_background));
        requestViewHolder.showExchangeIcon = false;
        String htmlString = "requested a <b>" +
                r.getItemName() + "</b>";
        requestViewHolder.vItemName.setText(Html.fromHtml(htmlString));
        String diff = AppUtils.getTimeDiffString(r.getPostDate());
        requestViewHolder.vPostedDate.setText(diff);
        if (r.getCategory() != null) {
            requestViewHolder.vCategoryName.setText(r.getCategory().getName());
        } else {
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
        }

        if (r.getDescription() != null) {
            requestViewHolder.vDescription.setText(r.getDescription());
        } else {
            requestViewHolder.vDescription.setVisibility(View.GONE);
        }
        requestViewHolder.vStatus.setText(r.getStatus());
        setRequestStatusColor(requestViewHolder.vStatus, r.getStatus());
            /*
             * only display the edit button if the request is open...they shouldn't need to edit
             * closed requests
             */
        if (!r.getStatus().equals("OPEN")) {
            requestViewHolder.showEditIcon = false;
        } else {
            requestViewHolder.showEditIcon = true;
        }
        if (h.getResponses() == null || h.getResponses().size() < 1) {
            requestViewHolder.vParentDropDownArrow.setVisibility(View.GONE);
        } else {
            requestViewHolder.vParentDropDownArrow.setVisibility(View.VISIBLE);
        }
    }

    private void setUpTransactionCard(HistoryCardViewHolder requestViewHolder, Request r, final History h) {
        boolean isBuyer = user.getId().equals(r.getUser().getId());
        final boolean isSeller = !isBuyer;
        requestViewHolder.mCardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.card_border_left));
        requestViewHolder.showEditIcon = false;
        requestViewHolder.vParentDropDownArrow.setVisibility(View.GONE);
        requestViewHolder.vPostedDate.setVisibility(View.GONE);
        requestViewHolder.vCategoryName.setVisibility(View.VISIBLE);
        requestViewHolder.vDescription.setVisibility(View.VISIBLE);
        Response resp = null;
        final Transaction transaction = h.getTransaction();
        for (Response res : h.getResponses()) {
            if (res.getId().equals(transaction.getResponseId())) {
                resp = res;
                break;
            }
        }
        String topDescription;
        Boolean complete = r.getRental() ? transaction.getExchanged() && transaction.getReturned() :
                transaction.getExchanged();
        String beginning;
        if (complete && r.getRental()) {
            beginning = isBuyer ? "Borrowed a " : "Loaned a ";
        } else if (complete) {
            beginning = isBuyer ? "Bought a " : "Sold a ";
        } else if (r.getRental()) {
            beginning = isBuyer ? "Borrowing a " : "Loaning a ";
        } else {
            beginning = isBuyer ? "Buying a " : "Selling a ";
        }
        if (isBuyer) {
            topDescription = beginning + r.getItemName() +
                    " from " + resp.getSeller().getFirstName();
        } else {
            topDescription = beginning + r.getItemName() +
                    " to " + r.getUser().getFirstName();
        }
        requestViewHolder.vItemName.setText(topDescription);
        if (!transaction.getExchanged()) {
            requestViewHolder.showExchangeIcon = true;
            String exchangeTime = "<b>exchange time:</b> " + resp.getExchangeTime();
            requestViewHolder.vCategoryName.setText(Html.fromHtml(exchangeTime));
            String exchangeLocation = "<b>exchange location:</b> " + resp.getExchangeLocation();
            requestViewHolder.vDescription.setText(Html.fromHtml(exchangeLocation));
            requestViewHolder.vStatus.setText("Awaiting initial exchange");
            Transaction.ExchangeOverride exchangeOverride = transaction.getExchangeOverride();
            requestViewHolder.showCancelTransactionIcon = true;
            if (exchangeOverride != null && !exchangeOverride.buyerAccepted && !exchangeOverride.declined) {
                requestViewHolder.showExchangeIcon = false;
                requestViewHolder.vStatus.setText("Pending exchange override approval");
                if (isBuyer) {
                    String description = resp.getSeller().getFirstName() +
                            " submitted an exchange override. Did you exchange the " +
                            r.getItemName() + " at the time above?";
                    historyFragment.showConfirmExchangeOverrideDialog(
                            transaction.getExchangeOverride().time.toString(),
                            description, transaction.getId(), isSeller);
                }
            }
        } else if (!transaction.getReturned() && r.getRental()) {
            requestViewHolder.showExchangeIcon = true;
            String returnTime = "<b>return time:</b> " + resp.getReturnTime();
            requestViewHolder.vCategoryName.setText(Html.fromHtml(returnTime));
            String returnLocation = "<b>return location:</b> " + resp.getReturnLocation();
            requestViewHolder.vDescription.setText(Html.fromHtml(returnLocation));
            requestViewHolder.vStatus.setText("Awaiting return");
            Transaction.ExchangeOverride returnOverride = transaction.getReturnOverride();
            if (returnOverride != null && !returnOverride.sellerAccepted && !returnOverride.declined) {
                //requestViewHolder.mExchangeIcon.setVisibility(View.GONE);
                requestViewHolder.showExchangeIcon = false;
                requestViewHolder.vStatus.setText("Pending return override approval");
                if (isSeller) {
                    String description = r.getUser().getFirstName() +
                            " submitted a return override. Was the " +
                            r.getItemName() + " returned at the time above?";
                    historyFragment.showConfirmExchangeOverrideDialog(
                            transaction.getExchangeOverride().time.toString(),
                            description,
                            transaction.getId(),
                            isSeller);
                }
            }
        } else if (r.getStatus().equals("TRANSACTION_PENDING")) {
            String calculatedPrice = "<b>calculated price:</b> " + transaction.getCalculatedPrice();
            requestViewHolder.vCategoryName.setText(Html.fromHtml(calculatedPrice));
            requestViewHolder.vDescription.setVisibility(View.GONE);
            if (isBuyer) {
                requestViewHolder.vStatus.setText("Processing Payment");
                requestViewHolder.showExchangeIcon = false;
            } else {
                requestViewHolder.showExchangeIcon = false;
                final String description = "For " + (r.getRental() ? "loaning " : "selling ") + "your " +
                        r.getItemName() + " to " + r.getUser().getFirstName();
                historyFragment.showConfirmChargeDialog(transaction.getCalculatedPrice(),
                        description, transaction.getId());
                requestViewHolder.vStatus.setText("CONFIRM CHARGE!");
                requestViewHolder.vStatus.setTextColor(Color.parseColor("#E52B50"));
                requestViewHolder.showEditIcon = false;
                requestViewHolder.showConfirmChargeIcon = true;
            }
        } else {
            requestViewHolder.showExchangeIcon = false;
            if (isBuyer) {
                String price = "<b>Payment:</b> -$" + transaction.getFinalPrice();
                requestViewHolder.vDescription.setText(Html.fromHtml(price));
            } else {
                String price = "<b>Payment:</b> $" + transaction.getFinalPrice();
                requestViewHolder.vDescription.setText(Html.fromHtml(price));
            }
            requestViewHolder.vCategoryName.setVisibility(View.GONE);
            requestViewHolder.vStatus.setText(r.getStatus());
        }
    }

    private void showPopupMenu(View view, History h, HistoryCardViewHolder rvh) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.history_card_menu, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        if (!rvh.showExchangeIcon) {
            popupMenu.removeItem(R.id.exchange_icon);
        }
        if (!rvh.showCancelTransactionIcon) {
            popupMenu.removeItem(R.id.cancel_transaction_button);
        }
        if (!rvh.showEditIcon) {
            popupMenu.removeItem(R.id.edit_button);
        }
        if (!rvh.showConfirmChargeIcon) {
            popupMenu.removeItem(R.id.confirm_charge);
        }
        popup.setOnMenuItemClickListener(new MenuClickListener(h));
        popup.show();
    }

    class MenuClickListener implements PopupMenu.OnMenuItemClickListener {
        private History history;
        private Transaction transaction;
        public MenuClickListener(History h) {
            history = h;
            transaction = h.getTransaction();
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            boolean isSeller = !user.getId().equals(history.getRequest().getUser().getId());
            switch (menuItem.getItemId()) {
                case R.id.cancel_transaction_button:
                    historyFragment.showCancelTransactionDialog(transaction.getId());
                    return true;
                case R.id.edit_button:
                    historyFragment.showRequestDialog(history);
                    return true;
                case R.id.exchange_icon:
                    if (isSeller) {
                        if (!transaction.getExchanged()) {
                            historyFragment.showExchangeCodeDialog(history.getTransaction(), false);
                        } else {
                            historyFragment.showScanner(history.getTransaction().getId(), false);
                        }
                    } else {
                        if (!transaction.getExchanged()) {
                            historyFragment.showScanner(history.getTransaction().getId(), true);
                        } else {
                            historyFragment.showExchangeCodeDialog(history.getTransaction(), true);
                        }
                    }
                    return true;
                case R.id.confirm_charge:
                    Request r = history.getRequest();
                    String description = "For " + (r.getRental() ? "loaning " : "selling ") + "your " +
                            r.getItemName() + " to " + r.getUser().getFirstName();
                    historyFragment.showConfirmChargeDialog(transaction.getCalculatedPrice(),
                            description, transaction.getId());
                default:
            }
            return false;
        }
    }

    public static class ResponseViewHolder extends ChildViewHolder {

        public TextView mResponderName;
        public TextView mItemName;
        public TextView mOfferAmount;
        public TextView mPriceType;
        public TextView mResponseStatus;
        private ImageButton mResponseDetailsButton;

        public ResponseViewHolder(View itemView) {
            super(itemView);
            mResponderName = (TextView) itemView.findViewById(R.id.responder_name);
            mItemName = (TextView) itemView.findViewById(R.id.item_name);
            mOfferAmount = (TextView) itemView.findViewById(R.id.offer_amount);
            mResponseStatus = (TextView) itemView.findViewById(R.id.response_status);
            mResponseDetailsButton = (ImageButton) itemView.findViewById(R.id.view_response_button);
        }
    }


    public static class HistoryCardViewHolder extends ParentViewHolder {
        protected TextView vItemName;
        protected TextView vCategoryName;
        protected TextView vPostedDate;
        protected TextView vDescription;
        protected Context context;
        private ImageButton vParentDropDownArrow;
        private TextView vStatus;
        public RelativeLayout mCardBackground;
        private CardView historyCard;
        protected ImageView menuBtn;
        public boolean showExchangeIcon = false;
        public boolean showCancelTransactionIcon = false;
        public boolean showEditIcon = true;
        public boolean showConfirmChargeIcon = false;


        protected FrameLayout cardView;

        public HistoryCardViewHolder(Context context, View v) {
            super(v);
            vItemName = (TextView) v.findViewById(R.id.item_name);
            vCategoryName = (TextView) v.findViewById(R.id.category_name);
            vPostedDate = (TextView) v.findViewById(R.id.posted_date);
            vDescription = (TextView) v.findViewById(R.id.description);
            cardView = (FrameLayout) itemView.findViewById(R.id.my_history_card_view);
            vParentDropDownArrow = (ImageButton) itemView.findViewById(R.id.parent_list_item_expand_arrow);
            vStatus = (TextView) v.findViewById(R.id.history_card_status);
            this.context = context;
            mCardBackground = (RelativeLayout) itemView.findViewById(R.id.card_layout);
            historyCard = (CardView) v.findViewById(R.id.my_history_card_view);
            historyCard.setMaxCardElevation(7);
            menuBtn = (ImageView) v.findViewById(R.id.card_menu);

        }
    }
}
