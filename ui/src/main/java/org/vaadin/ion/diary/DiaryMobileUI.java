package org.vaadin.ion.diary;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.vaadin.ion.diary.views.AddWorkoutView;

import com.vaadin.annotations.Widgetset;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.PhoneQualifier;
import com.vaadin.cdi.PhoneUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * The UI's "main" class
 */
@PhoneUI
@CDIUI
@Widgetset("org.vaadin.ion.diary.gwt.AppWidgetSet")
public class DiaryMobileUI extends UI {

    @Inject
    @PhoneQualifier
    private AddWorkoutView view;

    @Override
    protected void init(VaadinRequest request) {
        setContent(new CssLayout());
        final CssLayout cssLayout = new CssLayout();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Entry<String, String[]> entry : parameterMap.entrySet()) {
            cssLayout.addComponent(new Label(entry.getKey() + " "
                    + Arrays.toString(entry.getValue())));
        }
        cssLayout.addComponent((Component) view);
        setContent(cssLayout);
    }

}