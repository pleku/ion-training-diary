package org.vaadin.ion.diary;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

public class DiaryUIProvider extends UIProvider {

    private static final String SCREEN_WIDTH_PARAMETER = "v-sw";
    private static final String SCREEN_HEIGHT_PARAMETER = "v-sh";
    private int dpr;
    private int screenHeightPX;
    private int screenWidthPX;

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        String height = event.getRequest()
                .getParameter(SCREEN_HEIGHT_PARAMETER);
        if (height != null) {
            screenHeightPX = Integer.parseInt(height);
        }
        String width = event.getRequest().getParameter(SCREEN_WIDTH_PARAMETER);
        if (width != null) {
            screenWidthPX = Integer.parseInt(width);
        }
        String userAgent = event.getRequest().getHeader("user-agent")
                .toLowerCase();
        System.out.println("USER-AGENT: " + userAgent + " screenWidthPX: "
                + screenWidthPX + " screenHeightPX:" + screenHeightPX + " DPR:"
                + dpr);
        if (userAgent.contains("webkit")) {
            return DiaryMobileUI.class;
        } else {
            return DiaryDesktopUI.class;
        }
    }

    public void setDPR(String dpr) {
        this.dpr = Integer.parseInt(dpr);
    }

}
