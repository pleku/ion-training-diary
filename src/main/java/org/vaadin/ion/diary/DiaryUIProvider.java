package org.vaadin.ion.diary;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

public class DiaryUIProvider extends UIProvider {
    
    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
        if(userAgent.contains("webkit")) {
            return DiaryMobileUI.class;
        } else {
            return DiaryDesktopUI.class;
        }
    }

}
