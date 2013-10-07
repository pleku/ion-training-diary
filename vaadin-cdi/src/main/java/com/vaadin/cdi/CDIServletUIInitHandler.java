package com.vaadin.cdi;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.vaadin.server.LegacyApplicationUIProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.communication.ServletUIInitHandler;
import com.vaadin.server.communication.UIInitHandler;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.shared.ui.ui.UIConstants;
import com.vaadin.ui.UI;

public class CDIServletUIInitHandler extends ServletUIInitHandler {

	@Override
	public boolean synchronizedHandleRequest(VaadinSession session,
			VaadinRequest request, VaadinResponse response) throws IOException {
		if (!isInitRequest(request)) {
			return false;
		}

		StringWriter stringWriter = new StringWriter();

		try {
			assert UI.getCurrent() == null;

			// Set browser information from the request
			session.getBrowser().updateRequestDetails(request);

			JSONObject params = new JSONObject();
			UI uI = getBrowserDetailsUI(request, session, params);

			session.getCommunicationManager().repaintAll(uI);

			params.put(UIConstants.UI_ID_PARAMETER, uI.getUIId());
			String initialUIDL = getInitialUidl(request, uI);
			params.put("uidl", initialUIDL);

			stringWriter.write(params.toString());
		} catch (JSONException e) {
			throw new IOException("Error producing initial UIDL", e);
		} finally {
			stringWriter.close();
		}

		return commitJsonResponse(request, response, stringWriter.toString());
	}

	private UI getBrowserDetailsUI(VaadinRequest request, VaadinSession session, JSONObject params) {
		VaadinService vaadinService = request.getService();

		List<UIProvider> uiProviders = session.getUIProviders();

		UIClassSelectionEvent classSelectionEvent = new UIClassSelectionEvent(
				request);

		UIProvider provider = null;
		Class<? extends UI> uiClass = null;
		for (UIProvider p : uiProviders) {
			// Check for existing LegacyWindow
			if (p instanceof LegacyApplicationUIProvider) {
				LegacyApplicationUIProvider legacyProvider = (LegacyApplicationUIProvider) p;

				UI existingUi = legacyProvider
						.getExistingUI(classSelectionEvent);
				if (existingUi != null) {
					reinitUI(existingUi, request);
					return existingUi;
				}
			}

			uiClass = p.getUIClass(classSelectionEvent);
			if (uiClass != null) {
				provider = p;
				break;
			}
		}

		if (provider == null || uiClass == null) {
			return null;
		}
		

		// Check for an existing UI based on window.name

		// Special parameter sent by vaadinBootstrap.js
		String windowName = request.getParameter("v-wn");

		Map<String, Integer> retainOnRefreshUIs = session
				.getPreserveOnRefreshUIs();
		if (windowName != null && !retainOnRefreshUIs.isEmpty()) {
			// Check for a known UI

			Integer retainedUIId = retainOnRefreshUIs.get(windowName);

			if (retainedUIId != null) {
				UI retainedUI = session.getUIById(retainedUIId.intValue());
				if (uiClass.isInstance(retainedUI)) {
					reinitUI(retainedUI, request);
					return retainedUI;
				} else {
					getLogger().info(
							"Not using retained UI in " + windowName
									+ " because retained UI was of type "
									+ retainedUI.getClass() + " but " + uiClass
									+ " is expected for the request.");
				}
			}
		}

		// No existing UI found - go on by creating and initializing one

		Integer uiId = Integer.valueOf(session.getNextUIid());

		// Explicit Class.cast to detect if the UIProvider does something
		// unexpected
		UICreateEvent event = new UICreateEvent(request, uiClass, uiId);
		UI ui = uiClass.cast(provider.createInstance(event));
		try {
			params.append("theme", provider.getTheme(event));
			params.append("widgetset", provider.getTheme(event));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Initialize some fields for a newly created UI
		if (ui.getSession() != session) {
			// Session already set for LegacyWindow
			ui.setSession(session);
		}

		PushMode pushMode = provider.getPushMode(event);
		if (pushMode == null) {
			pushMode = session.getService().getDeploymentConfiguration()
					.getPushMode();
		}
		ui.getPushConfiguration().setPushMode(pushMode);

		Transport transport = provider.getPushTransport(event);
		if (transport != null) {
			ui.getPushConfiguration().setTransport(transport);
		}

		// Set thread local here so it is available in init
		UI.setCurrent(ui);

		ui.doInit(request, uiId.intValue());

		session.addUI(ui);

		// Remember if it should be remembered
		if (vaadinService.preserveUIOnRefresh(provider, event)) {
			// Remember this UI
			if (windowName == null) {
				getLogger().warning(
						"There is no window.name available for UI " + uiClass
								+ " that should be preserved.");
			} else {
				session.getPreserveOnRefreshUIs().put(windowName, uiId);
			}
		}

		return ui;
	}

	private void reinitUI(UI ui, VaadinRequest request) {
		UI.setCurrent(ui);

		// Fire fragment change if the fragment has changed
		String location = request.getParameter("v-loc");
		if (location != null) {
			ui.getPage().updateLocation(location);
		}
	}

	private static final Logger getLogger() {
		return Logger.getLogger(UIInitHandler.class.getName());
	}

	static boolean commitJsonResponse(VaadinRequest request,
			VaadinResponse response, String json) throws IOException {
		// The response was produced without errors so write it to the client
		response.setContentType("application/json; charset=UTF-8");

		// Ensure that the browser does not cache UIDL responses.
		// iOS 6 Safari requires this (#9732)
		response.setHeader("Cache-Control", "no-cache");

		// NOTE! GateIn requires, for some weird reason, getOutputStream
		// to be used instead of getWriter() (it seems to interpret
		// application/json as a binary content type)
		OutputStreamWriter outputWriter = new OutputStreamWriter(
				response.getOutputStream(), "UTF-8");
		try {
			outputWriter.write(json);
			// NOTE GateIn requires the buffers to be flushed to work
			outputWriter.flush();
		} finally {
			outputWriter.close();
		}

		return true;
	}
}
