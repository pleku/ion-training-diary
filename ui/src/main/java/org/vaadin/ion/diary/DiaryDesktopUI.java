package org.vaadin.ion.diary;

import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.DesktopUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * This UI is served for browsers that don't support TouchKit.
 *
 */
@DesktopUI
@CDIUI
public class DiaryDesktopUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        setContent(new Label(
                "This app is only designed for desktop webkit based devices"));

    }

}
