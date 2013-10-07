package com.vaadin.cdi;

import org.json.JSONException;
import org.json.JSONObject;

import com.vaadin.server.PaintException;
import com.vaadin.server.communication.ServletBootstrapHandler;

public class CDIBootstrapHandler extends ServletBootstrapHandler {

	@Override
	protected JSONObject getApplicationParameters(BootstrapContext context)
			throws JSONException, PaintException {
		JSONObject jsonObject = super.getApplicationParameters(context);

		if (context.getRequest().getParameter("v-sh") == null) {
			jsonObject.remove("theme");
			jsonObject.remove("widgetset");
		}
		return jsonObject;
	}
}
