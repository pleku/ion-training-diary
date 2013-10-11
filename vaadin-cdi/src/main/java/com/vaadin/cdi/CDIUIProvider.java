/*
 * Copyright 2000-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.cdi;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.vaadin.cdi.internal.AnnotationUtil;
import com.vaadin.cdi.internal.Conventions;
import com.vaadin.cdi.internal.UIBean;
import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

public class CDIUIProvider extends DefaultUIProvider implements Serializable {

	protected UITypeSolver getUITypeSolver() {
		return new UITypeSolver() {

			@Override
			public UIType solveUIType(VaadinRequest request) {
				if (getScreenSize(request) < 800) {
					Logger.getAnonymousLogger().log(Level.INFO,
							"Phone UI, screen size " + getScreenSize(request));
					return UIType.PHONE;
				}

				// TODO figure out how to detect tablet

				Logger.getAnonymousLogger().log(Level.INFO,
						"Desktop UI, screen size " + getScreenSize(request));
				return UIType.DESKTOP;
			}

			private int getScreenSize(VaadinRequest request) {
				String height = request.getParameter("v-sh");
				int screenHeightPx = -1;
				int screenWidthPx = -1;
				if (height != null) {
					screenHeightPx = Integer.parseInt(height);
				}
				String width = request.getParameter("v-sw");
				if (width != null) {
					screenWidthPx = Integer.parseInt(width);
				}

				return screenWidthPx < screenHeightPx ? screenWidthPx
						: screenHeightPx;
			}
		};
	}

	@Override
	public UI createInstance(UICreateEvent uiCreateEvent) {
		Class<? extends UI> type = uiCreateEvent.getUIClass();
		int uiId = uiCreateEvent.getUiId();
		VaadinRequest request = uiCreateEvent.getRequest();

		UIType uiType = getUITypeSolver().solveUIType(request);
		Bean<?> bean = scanForBeans(type, uiType);
		String uiMapping = "";
		if (bean == null) {
			if (type.isAnnotationPresent(CDIUI.class)) {
				uiMapping = parseUIMapping(request);
				bean = getUIBeanWithMapping(uiMapping, uiType);
			} else {
				throw new IllegalStateException("UI class: " + type.getName()
						+ " with mapping: " + uiMapping
						+ " is not annotated with CDIUI!");
			}
		}
		UIBean uiBean = new UIBean(bean, uiId);
		try {
			// Make the UIBean available to UIScopedContext when creating nested
			// injected objects
			CurrentInstance.set(UIBean.class, uiBean);
			return (UI) getBeanManager().getReference(uiBean, type,
					getBeanManager().createCreationalContext(bean));
		} finally {
			CurrentInstance.set(UIBean.class, null);
		}
	}

	@Override
	public Class<? extends UI> getUIClass(UIClassSelectionEvent selectionEvent) {
		VaadinRequest request = selectionEvent.getRequest();
		UIType uiType = getUITypeSolver().solveUIType(request);

		String uiMapping = parseUIMapping(request);
		if (isRoot(request)) {
			return rootUI(uiType);
		}
		Bean<?> uiBean = getUIBeanWithMapping(uiMapping, uiType);

		if (uiBean != null) {
			return uiBean.getBeanClass().asSubclass(UI.class);
		}

		if (uiMapping.isEmpty()) {
			// See if UI is configured to web.xml with VaadinCDIServlet. This is
			// done only if no specific UI name is given.
			return super.getUIClass(selectionEvent);
		}

		return null;
	}

	boolean isRoot(VaadinRequest request) {
		String pathInfo = request.getPathInfo();

		if (pathInfo == null) {
			return false;
		}

		return pathInfo.equals("/");
	}

	Class<? extends UI> rootUI(UIType uiType) {
		Set<Bean<?>> rootBeans = AnnotationUtil
				.getRootUiBeans(getBeanManager());
		if (rootBeans.isEmpty()) {
			return null;
		}

		Bean<?> uiBean = null;
		if (rootBeans.size() > 1) {
			boolean multipleRootsFound = false;
			StringBuilder errorMessage = new StringBuilder();
			for (Bean<?> bean : rootBeans) {
				if (bean.getBeanClass().isAnnotationPresent(
						uiType.getAnnotation())
						|| (!isDeviceAnnotationPresent(bean) && uiType
								.getAnnotation().equals(DesktopUI.class))) {
					if (uiBean == null && !multipleRootsFound) {
						uiBean = bean;
						errorMessage.append(bean.getBeanClass().getName());
						errorMessage.append("\n");
						continue;
					} else {
						uiBean = null;
						multipleRootsFound = true;
					}
				}

			}
			if (multipleRootsFound) {
				throw new IllegalStateException(
						"Multiple beans are annotated with @CDIUI without context path: "
								+ errorMessage.toString());
			}
		} else {
			uiBean = rootBeans.iterator().next();
		}

		Class<?> rootUI = uiBean.getBeanClass();
		return rootUI.asSubclass(UI.class);
	}

	private Bean<?> getUIBeanWithMapping(String mapping, UIType uiType) {
		Set<Bean<?>> beans = AnnotationUtil.getUiBeans(getBeanManager());

		for (Bean<?> bean : beans) {
			// We need this check since the returned beans can also be producers
			if (UI.class.isAssignableFrom(bean.getBeanClass())) {
				Class<? extends UI> beanClass = bean.getBeanClass().asSubclass(
						UI.class);

				if (beanClass.isAnnotationPresent(CDIUI.class)) {
					String computedMapping = Conventions
							.deriveMappingForUI(beanClass);
					if (mapping.equals(computedMapping)) {
						return bean;
					}
				}
			}
		}

		return null;
	}

	private Bean<?> scanForBeans(Class<? extends UI> type, UIType uiType) {
		Set<Bean<?>> beans = getBeanManager().getBeans(type,
				new AnnotationLiteral<Any>() {
				});

		if (beans.isEmpty()) {
			getLogger().warning(
					"Could not find UI bean for " + type.getCanonicalName());
			return null;
		}

		// Bean instance containing the device annotation
		Bean<?> uiBean = null;

		// Bean instance containing NO device annotations
		Bean<?> noAnnotation = null;

		for (Bean<?> bean : beans) {
			if (bean.getBeanClass().isAnnotationPresent(uiType.getAnnotation())) {
				if (uiBean != null) {
					getLogger().warning(
							"Found multiple UI beans for "
									+ type.getCanonicalName());
					return null;
				}
				uiBean = bean;
				continue;
			}

			if (!isDeviceAnnotationPresent(bean)) {
				noAnnotation = bean;
			}
		}

		// Primarily return the bean containing device annotation. If no such
		// beans are found, return the bean with no device annotations.
		return uiBean != null ? uiBean : noAnnotation;
	}

	private boolean isDeviceAnnotationPresent(Bean<?> bean) {
		return bean.getBeanClass().isAnnotationPresent(DesktopUI.class)
				|| bean.getBeanClass().isAnnotationPresent(PhoneUI.class)
				|| bean.getBeanClass().isAnnotationPresent(TabletUI.class);
	}

	String parseUIMapping(VaadinRequest request) {
		return parseUIMapping(request.getPathInfo());
	}

	String parseUIMapping(String requestPath) {
		if (requestPath != null && requestPath.length() > 1) {
			String path = requestPath;
			if (requestPath.endsWith("/")) {
				path = requestPath.substring(0, requestPath.length() - 1);
			}
			if (!path.contains("!")) {
				int lastIndex = path.lastIndexOf('/');
				return path.substring(lastIndex + 1);
			} else {
				int lastIndexOfBang = path.lastIndexOf('!');
				// strip slash with bank => /!
				String pathWithoutView = path.substring(0, lastIndexOfBang - 1);
				int lastSlashIndex = pathWithoutView.lastIndexOf('/');
				return pathWithoutView.substring(lastSlashIndex + 1);
			}
		}
		return "";
	}

	// TODO a better way to do this could be custom injection management in the
	// Extension if feasible
	private BeanManager beanManager;

	private BeanManager getBeanManager() {
		if (beanManager == null) {
			// as the CDIUIProvider is not injected, need to use JNDI lookup
			try {
				InitialContext initialContext = new InitialContext();
				beanManager = (BeanManager) initialContext
						.lookup("java:comp/BeanManager");
			} catch (NamingException e) {
				getLogger().severe("Could not get BeanManager through JNDI");
				beanManager = null;
			}
		}
		return beanManager;
	}

	private static Logger getLogger() {
		return Logger.getLogger(CDIUIProvider.class.getCanonicalName());
	}
}
