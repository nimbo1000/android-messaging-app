package mcc_2018_g15.chatapp.holders;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;


import mcc_2018_g15.chatapp.Message;
import mcc_2018_g15.chatapp.R;

/*
 * Created by troy379 on 05.04.17.
 */
public class CustomIncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<Message> {

    private View onlineIndicator;

    public CustomIncomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        //onlineIndicator =   itemView.findViewById(R.id.onlineIndicator);

    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);

//        boolean isOnline = message.getUser().isBoolean();
//        if (isOnline) {
//            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
//        } else {
//            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
//        }
    }
}