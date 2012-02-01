package org.deephacks.tools4j.config.admin.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
    private static final String PERSPECTIVE_ID = "config.admin.rcp.perspective";

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    public String getInitialWindowPerspectiveId() {
        return PERSPECTIVE_ID;
    }

    @Override
    public void initialize(IWorkbenchConfigurer configurer) {

    }

    @Override
    public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
        configurer.setInitialSize(new Point(500, 300));
        configurer.setShowMenuBar(true);
    }
}
