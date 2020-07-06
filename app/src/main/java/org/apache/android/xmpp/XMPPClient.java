package org.apache.android.xmpp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.StringUtils;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;

public class XMPPClient extends Activity {

    private ArrayList<String> messages = new ArrayList();
    private Handler mHandler = new Handler();
    private SettingsDialog mDialog;
    private EditText mRecipient;
    private EditText mSendText;
    private ListView mList;
    private XMPPConnection connection;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.i("XMPPClient", "onCreate called");
        setContentView(R.layout.main);

        mRecipient = (EditText) this.findViewById(R.id.recipient);
        Log.i("XMPPClient", "mRecipient = " + mRecipient);
        mSendText = (EditText) this.findViewById(R.id.sendText);
        Log.i("XMPPClient", "mSendText = " + mSendText);
        mList = (ListView) this.findViewById(R.id.listMessages);
        Log.i("XMPPClient", "mList = " + mList);
        setListAdapter();

        // Dialog for getting the xmpp settings
        mDialog = new SettingsDialog(this);

        // Set a listener to show the settings dialog
        Button setup = (Button) this.findViewById(R.id.setup);
        setup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mDialog.show();
                    }
                });
            }
        });

        // Set a listener to send a chat text message
        Button send = (Button) this.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String to = mRecipient.getText().toString();
                String text = mSendText.getText().toString();

                Log.i("XMPPClient", "Sending text [" + text + "] to [" + to + "]");
                Message msg = null;
                try {
                    msg = new Message(to, String.valueOf(Message.Type.chat));
                    msg.setBody(text);
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }

                try {
                    connection.sendStanza(msg);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                messages.add(connection.getUser() + ":");
                messages.add(text);
                setListAdapter();
            }
        });
    }

    /**
     * Called by Settings dialog when a connection is establised with the XMPP server
     *
     * @param connection
     */
    public void setConnection
            (XMPPConnection
                    connection) {
        this.connection = connection;
        if (connection != null) {
            // Add a packet listener to get messages sent to us
            StanzaFilter filter = new AndFilter(MessageTypeFilter.CHAT);
            connection.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    Message message = (Message) packet;
                    if (message.getBody() != null) {
//                        String fromName = StringUtils.parseBareAddress(message.getFrom());
//                        Log.i("XMPPClient", "Got text [" + message.getBody() + "] from [" + fromName + "]");
//                        messages.add(fromName + ":");
                        messages.add(message.getBody());
                        // Add the incoming message to the list view
                        mHandler.post(new Runnable() {
                            public void run() {
                                setListAdapter();
                            }
                        });
                    }
                }
            }, filter);
        }
    }

    private void setListAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.multi_line_list_item,
                messages);
        mList.setAdapter(adapter);
    }
}
