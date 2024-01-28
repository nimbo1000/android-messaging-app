package mcc_2018_g15.chatapp.holders;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.github.siyamed.shapeimageview.mask.PorterImageView;
import com.stfalcon.chatkit.messages.MessageHolders;

import mcc_2018_g15.chatapp.Message;
import mcc_2018_g15.chatapp.MessageActivity;
import mcc_2018_g15.chatapp.R;

public class CustomOutcomingTextMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message> {
    private LinearLayout the_chat;
    public CustomOutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);


    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);


        //time.setText(message.getStatus() + " " + time.getText());
    }
}
