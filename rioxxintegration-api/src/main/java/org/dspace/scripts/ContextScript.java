/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.scripts;

import org.apache.commons.cli.CommandLine;
import org.dspace.core.Context;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 21 May 2014
 */
public class ContextScript extends Script {

    protected Context context;

    public ContextScript(Context context) {
        super();
        this.context = context;
    }

    public ContextScript() {
    }

    public static void main(String[] args) {
        Script Script = new ContextScript();
        Script.mainImpl(args);
    }

    public void mainImpl(String[] args) {
        context = null;
        try {
            context = new Context();
            context.turnOffAuthorisationSystem();

            super.mainImpl(args);

        } catch (Exception e) {
            printAndLogError(e);
        } finally {
            if (context != null) {
                context.abort();
            }
        }
    }

    @Override
    protected int processLine(CommandLine line) {
        // no extra arguments
        return 0;
    }


}
