/*
 * Button Clicker
 * Sample Implementation of the In-App Purchasing APIs
 * 
 * © 2012, Amazon.com, Inc. or its affiliates.
 * All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * http://aws.amazon.com/apache2.0/
 * or in the "license" file accompanying this file.
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.amazon.sample.buttonclicker;

import com.amazon.sample.buttonclicker.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;


/**
 * Helper class to abstract dialog creation from the rest of the application 
 */
public class DialogCommandWrapper implements OnClickListener {
    private final Runnable command;

    public DialogCommandWrapper(Runnable command) {
        this.command = command;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        command.run();
    }

    /**
     * Creates a generic confirmation dialog.
     * 
     * @param context
     *            Context to build dialog for
     * @param title
     *            Description title for dialog
     * @param confirmText
     *            Text for confirmation button
     * @param dismissText
     *            Text for cancel/dismiss button
     * @param command
     *            Runnable object that is invoked when user presses the confirmation button
     * @return
     */
    public static Dialog createConfirmationDialog(Context context, String title, String confirmText,
        String dismissText, Runnable command) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setIcon(R.drawable.icon);
        dialogBuilder.setTitle(title);
        dialogBuilder.setInverseBackgroundForced(true);
        dialogBuilder.setPositiveButton(confirmText, new DialogCommandWrapper(command));
        dialogBuilder.setNegativeButton(dismissText, new DialogCommandWrapper(NO_OP));
        return dialogBuilder.create();
    }

    /**
     * Static "Do Nothing" command when the user dismisses a dialog
     */
    public static final Runnable NO_OP = new Runnable() {
        @Override
        public void run() {
            // Do Nothing
        }
    };
}
