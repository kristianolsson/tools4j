package org.deephacks.tools4j.config.admin.rcp;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;

public class Action implements IActionDelegate {

    public void run(IAction action) {
        System.out.println("Hello");
    }

    public void selectionChanged(IAction action, ISelection selection) {
        System.out.println("Hello");

    }

}
