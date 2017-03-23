package foo.org.chatapp.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.*;

import butterknife.BindView;
import butterknife.ButterKnife;

import foo.org.chatapp.models.ChatMessage;
import foo.org.chatapp.R;
import foo.org.chatapp.util.ChatClient;

import com.github.siyamed.shapeimageview.CircularImageView;

/**
 * Created by enoks on 21.1.2017.
 */

public class ChatMessageAdapter extends BaseAdapter {

    private List<ChatMessage> msgs;
    private LayoutInflater inflater;
    private Context context;
    private String imageBaseUrl;
    private final String AVATAR_IMAGE_FORMAT = ".png";

    public ChatMessageAdapter(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.msgs = new ArrayList<ChatMessage>();
        this.imageBaseUrl = ChatClient.getInstance().getImageUrl();
    }

    @Override
    public int getCount() {
        return msgs.size();
    }

    @Override
    public Object getItem(int position) {
        return msgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ChatMessage msg = msgs.get(position);
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.chat_row, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        String avatarPictureUrl = imageBaseUrl + msg.getUserGuid() + AVATAR_IMAGE_FORMAT;
        Picasso.with(context).load(avatarPictureUrl).into(holder.avatarImageView);
        holder.contentEditText.setText(msg.getContent());

        return view;
    }

    public void updateMessages(@NonNull List<ChatMessage> msgs) {
        this.msgs = msgs;
        this.notifyDataSetChanged();
    }

    static class ViewHolder {
        @BindView(R.id.avatarImageView) CircularImageView avatarImageView;
        @BindView(R.id.chatContentTextView)  TextView contentEditText;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
