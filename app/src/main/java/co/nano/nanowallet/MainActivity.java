package co.nano.nanowallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.ui.intro.IntroWelcomeFragment;

public class MainActivity extends AppCompatActivity implements WindowControl {
    private WebSocketClient mWebSocketClient;
    private FragmentUtility mFragmentUtility;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initUi() {
        // set main content view
        setContentView(R.layout.activity_main);

        // create fragment utility instance
        mFragmentUtility = new FragmentUtility(getSupportFragmentManager());
        mFragmentUtility.setContainerViewId(R.id.container);

        // set up toolbar
        mToolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.toolbar_title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // set the intro welcome fragment as the first fragment
        // TODO: Add logic to see if this is a first time user or not
        SharedPreferences pref = this.getSharedPreferences("co.nano.nanowallet", Context.MODE_PRIVATE);
        // if we dont have a wallet, start the intro
        mFragmentUtility.replace(new IntroWelcomeFragment());
        // if we do have a wallet, load and configure
        //mFragmentUtility.replace(new HomeFragment());

        connectWebSocket();
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("wss://raicast.lightrai.com:443");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("{\"action\":\"account_subscribe\",\"account\":\"xrb_3t6k35gi95xu6tergt6p69ck76ogmitsa8mnijtpxm9fkcm736xtoncuohr3\"}");
                mWebSocketClient.send("{\"action\":\"block_count\"}");
                mWebSocketClient.send("{\"action\":\"price_data\",\"currency\":\"usd\"}");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                Log.i("Websocket", message);
                //runOnUiThread(new Runnable() {
                //    @Override
                //    public void run() {
                //        TextView textView = (TextView)findViewById(R.id.messages);
                //        textView.setText(textView.getText() + "\n" + message);
                //    }
                //});
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.message);
        mWebSocketClient.send(editText.getText().toString());
        editText.setText("");
    }

    @Override
    public FragmentUtility getFragmentUtility() {
        return mFragmentUtility;
    }

    /**
     * Set the status bar to a particular color
     *
     * @param color color resource id
     */
    @Override
    public void setStatusBarColor(int color) {
        // we can only set it 5.x and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, color));
        }
    }

    @Override
    public void setDarkIcons(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    /**
     * Set visibility of app toolbar
     * @param visible
     */
    @Override
    public void setToolbarVisible(boolean visible) {
        if (mToolbar != null) {
            mToolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Set title of the app toolbar
     * @param title
     */
    @Override
    public void setTitle(String title) {
        if (mToolbarTitle != null) {
            mToolbarTitle.setText(title);
        }
        setToolbarVisible(true);
    }

    /**
     * Set title drawable of app toolbar
     * @param drawable
     */
    @Override
    public void setTitleDrawable(int drawable) {
        if (mToolbarTitle != null) {
            mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
        }
        setToolbarVisible(true);
    }

    @Override
    public void setBackEnabled(boolean enabled) {
        if (mToolbar != null) {
            if (enabled) {
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                mToolbar.setNavigationOnClickListener(view -> mFragmentUtility.pop());
            } else {
                mToolbar.setNavigationIcon(null);
                mToolbar.setNavigationOnClickListener(null);
            }
        }
    }
}
