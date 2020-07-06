package org.apache.android.xmpp;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Gather the xmpp settings and create an XMPPConnection
 */
public class SettingsDialog extends Dialog implements android.view.View.OnClickListener {
    private XMPPClient xmppClient;
    private AbstractXMPPConnection mConnection;
    public SettingsDialog(XMPPClient xmppClient) {
        super(xmppClient);
        this.xmppClient = xmppClient;
    }

    protected void onStart() {
        super.onStart();
        setContentView(R.layout.settings);
        getWindow().setFlags(4, 4);
        setTitle("XMPP Settings");
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
    }

    public void onClick(View v) {
        String host = getText(R.id.host);
        String port = getText(R.id.port);
        String service = getText(R.id.service);
        String username = getText(R.id.userid);
        String password = getText(R.id.password);
        InetAddress addr = null;
        try {

            // inter your ip4address now checking it
            addr = InetAddress.getByName("srv3.mjunoon.tv");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return false;
            }
        };
        DomainBareJid serviceName = null;
        try {
            serviceName = JidCreate.domainBareFrom("srv3.mjunoon.tv");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword("saqib.ahsan37","123789")
                .setPort(5222)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                .setXmppDomain(serviceName)
//                .setHostnameVerifier(verifier)
                .setSendPresence(true)
                .setHostAddress(addr)
                .setKeystoreType(null)
                .build();
        mConnection = new XMPPTCPConnection(config);
        try {
            mConnection.connect();
            Log.i("XMPPClient", "[SettingsDialog] Connected to " + mConnection.getHost());
        } catch (XMPPException ex) {
            Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + mConnection.getHost());
            Log.e("XMPPClient", ex.toString());
            xmppClient.setConnection(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }
        try {
            mConnection.login();
            Log.i("XMPPClient", "Logged in as " + mConnection.getUser());

            // Set the status to available
            Presence presence = new Presence(Presence.Type.available);
            mConnection.sendStanza(presence);
            xmppClient.setConnection(mConnection);
        } catch (XMPPException ex) {
            Log.e("XMPPClient", "[SettingsDialog] Failed to log in as " + username);
            Log.e("XMPPClient", ex.toString());
                xmppClient.setConnection(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }
        dismiss();
    }

    private String getText(int id) {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }
}
