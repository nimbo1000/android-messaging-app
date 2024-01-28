package mcc_2018_g15.chatapp.holders;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import de.hdodenhof.circleimageview.CircleImageView;
import mcc_2018_g15.chatapp.Message;
import mcc_2018_g15.chatapp.MessageActivity;
import mcc_2018_g15.chatapp.R;
public class CustomIncomingTextMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<Message> {
    private View onlineIndicator;
    private TextView username,messageText;
    private LinearLayout the_chat;
    private CircleImageView messageUserAvatar;
    private LinearLayout.LayoutParams lp;


    public CustomIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);

        //onlineIndicator = (TextView) itemView.findViewById(R.id.onlineIndicator);
        username = itemView.findViewById(R.id.messageusername);
        the_chat = itemView.findViewById(R.id.bubble);
        messageText = itemView.findViewById(R.id.messageText);
        messageUserAvatar = itemView.findViewById(R.id.messageUserAvatar);
        //lp = (LinearLayout.LayoutParams) messageText.getLayoutParams();

        //messageText.setLayoutParams(lp);



    }
    @Override
    public void onBind(Message message) {
        super.onBind(message);

        boolean isOnline = message.getUser().isBoolean();
//        if (isOnline) {
//            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
//        } else {
//            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
//        }
        Log.d("debugthat" , message.getText()  + ", " + message.getId() + "has left the group");
        if(message.getText().equals(message.getId() + "has left the chat")){
            Log.d("chatLeaving", "is being done");
            messageText.setTextColor(Color.parseColor("#ff0000"));
            messageText.setText(message.getUser().getName() + " has left the chat");
            messageText.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
            username.setVisibility(View.GONE);
            messageUserAvatar.setVisibility(View.GONE);
            return;

        }
        username.setText(message.getUser().getName());

        //We can set click listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (payload != null && payload.avatarClickListener != null) {
                    payload.avatarClickListener.onAvatarClick();
                }
            }
        });
    }

    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }

    public interface OnAvatarClickListener {
        void onAvatarClick();
    }

}
