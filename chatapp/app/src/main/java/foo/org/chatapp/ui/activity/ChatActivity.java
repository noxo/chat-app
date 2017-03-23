package foo.org.chatapp.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.google.gson.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import foo.org.chatapp.R;
import foo.org.chatapp.models.ChatChannel;
import foo.org.chatapp.models.ChatMessage;
import foo.org.chatapp.ui.adapters.ChatMessageAdapter;
import foo.org.chatapp.util.ChatClient;

import com.github.amlcurran.showcaseview.*;
import com.github.amlcurran.showcaseview.targets.*;

import com.orhanobut.logger.Logger;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.chatMessagesList)
    ListView chatMessagesList;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.sendChatMessageButton)
    ImageButton sendChatButton;
    @BindView(R.id.progress)
    ProgressBar mProgressView;
    @BindView(R.id.chatMessageEditText)
    EditText chatMessageEditText;
    @BindView(R.id.mainLayout)
    CoordinatorLayout coordinatorLayout;

    private boolean showChannelHint = true;
    private ChatMessageAdapter chatMessageAdapter;
    private ChatClient chatClient;

    private int currentChannel = 0;
    private List<ChatChannel> channels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.no_channel);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        chatMessagesList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        ViewCompat.setNestedScrollingEnabled(chatMessagesList, false);
        chatMessageAdapter = new ChatMessageAdapter(this);
        chatMessagesList.setAdapter(chatMessageAdapter);
        chatClient = ChatClient.getInstance();

        setSupportActionBar(toolbar);
        pullChannels();

    }

    private void pullChannels()
    {
        showProgress(true);
        chatClient.getChannels(new ChatClient.ChatRestClientHandler() {
            @Override
            public void success(String jsonString) {
                Gson gson = new Gson();
                channels = Arrays.asList(gson.fromJson(jsonString, ChatChannel[].class));
                Logger.i("channels pulled");
                showProgress(false);
                invalidateOptionsMenu();
            }

            @Override
            public void fail(Throwable error, String frienlyErrorMsg) {
                Logger.e(error, "failed pulling channels");
                showProgress(false);
            }
        });
    }

    private void pullMessages(int channel) {

        showProgress(true);
        String channelGuid = channels.get(channel).getGuid();
        chatClient.getChannelMessages(channelGuid,new ChatClient.ChatRestClientHandler() {
            @Override
            public void success(String jsonString) {
                Gson gson = new Gson();
                List<ChatMessage> msgs = Arrays.asList(gson.fromJson(jsonString, ChatMessage[].class));
                chatMessageAdapter.updateMessages(msgs);
                Logger.i("messages pulled");
                showProgress(false);
            }

            @Override
            public void fail(Throwable error, String frienlyErrorMsg) {
                Logger.e(error, "failed pulling messages");
                showProgress(false);
            }
        });

    }
    @OnClick(R.id.sendChatMessageButton)
     void sendMessage() {

        chatMessageEditText.setEnabled(false);

        String content = chatMessageEditText.getText().toString();
        String channelGuid = channels.get(currentChannel).getGuid();

        Gson gson = new Gson();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setChannelGuid(channelGuid);

        showProgress(true);

        chatClient.postMessage(this, channelGuid,gson.toJson(chatMessage), new ChatClient.ChatRestClientHandler() {
            @Override
            public void success(String jsonString) {
                chatMessageEditText.setEnabled(true);
                chatMessageEditText.setText("");
                Gson gson = new Gson();
                List<ChatMessage> msgs = Arrays.asList(gson.fromJson(jsonString, ChatMessage[].class));
                chatMessageAdapter.updateMessages(msgs);
                Logger.i("messages pulled, after sending");
                showProgress(false);
            }

            @Override
            public void fail(Throwable error, String frienlyErrorMsg) {
                chatMessageEditText.setEnabled(true);
                Logger.e(error, "failed sending message");
                showError(frienlyErrorMsg);
                showProgress(false);
            }
        });

    }

    private void showError(String msg) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(Color.RED);
        snackbar.show();
    }

    private void setCurrentChannel(int channel, boolean reloadMessages){
        ChatChannel selected = channels.get(channel);
        setTitle(selected.getName());
        if (reloadMessages) {
           pullMessages(channel);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (menu.size() == 0) {
            boolean gotChannels = false;

            for (int i=0;i<channels.size();i++) {
                ChatChannel channel = channels.get(i);
                menu.add(0,i,0, channel.getName());
                gotChannels = true;
            }

            if (gotChannels)
            {
                setCurrentChannel(0, true);
                if (showChannelHint) {
                    showChannelHint = false;
                    showChannelChangeHint();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setCurrentChannel(item.getItemId(), true);
        return true;
    }

    private void showChannelChangeHint()
    {
       ;
        ViewTarget target = new ViewTarget(R.id.toolbar, this);
        new ShowcaseView.Builder(this)
                .setTarget(target)
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.hint)
                .setContentText(R.string.switch_channel_help)
                .hideOnTouchOutside()
                .build();
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
