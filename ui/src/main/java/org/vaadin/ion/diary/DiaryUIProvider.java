package org.vaadin.ion.diary;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIUIProvider;
import com.vaadin.cdi.DesktopUI;
import com.vaadin.cdi.PhoneUI;
import com.vaadin.cdi.TabletUI;
import com.vaadin.cdi.internal.AnnotationUtil;
import com.vaadin.cdi.internal.Conventions;
import com.vaadin.cdi.internal.UIBean;
import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

public class DiaryUIProvider extends DefaultUIProvider implements Serializable {

	private enum UITYPE {
		DESKTOP(DesktopUI.class), TABLET(TabletUI.class), PHONE(PhoneUI.class);

		private Class<? extends Annotation> annotation;

		UITYPE(Class<? extends Annotation> annotation) {
			this.annotation = annotation;
		}

		public Class<? extends Annotation> getAnnotation() {
			return annotation;
		}
	};

	@Override
	public UI createInstance(UICreateEvent uiCreateEvent) {
		Class<? extends UI> type = uiCreateEvent.getUIClass();
		int uiId = uiCreateEvent.getUiId();
		VaadinRequest request = uiCreateEvent.getRequest();
		
		int screenSize = getScreenSize(request);
		UITYPE uiType = getUIType(screenSize);
		
		Bean<?> bean = scanForBeans(type);
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
		int screenSize = getScreenSize(selectionEvent.getRequest());
		UITYPE uiType = getUIType(screenSize);
		
		VaadinRequest request = selectionEvent.getRequest();
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

	Class<? extends UI> rootUI(UITYPE uiType) {
		Set<Bean<?>> rootBeans = AnnotationUtil
				.getRootUiBeans(getBeanManager());
		if (rootBeans.isEmpty()) {
			return null;
		}
		if (rootBeans.size() > 1) {
			StringBuilder errorMessage = new StringBuilder();
			for (Bean<?> bean : rootBeans) {
				errorMessage.append(bean.getBeanClass().getName());
				errorMessage.append("\n");
			}
			throw new IllegalStateException(
					"Multiple beans are annotated with @CDIUI without context path: "
							+ errorMessage.toString());
		}
		Bean<?> uiBean = rootBeans.iterator().next();
		Class<?> rootUI = uiBean.getBeanClass();
		return rootUI.asSubclass(UI.class);
	}

	private Bean<?> getUIBeanWithMapping(String mapping, UITYPE uiType) {
		Set<Bean<?>> beans = AnnotationUtil.getUiBeans(getBeanManager());

		for (Bean<?> bean : beans) {
			// We need this check since the returned beans can also be producers
			if (UI.class.isAssignableFrom(bean.getBeanClass())) {
				Class<? extends UI> beanClass = bean.getBeanClass().asSubclass(
						UI.class);

				if (beanClass.isAnnotationPresent(CDIUI.class) && beanClass.isAnnotationPresent(uiType.getAnnotation())) {
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

	private Bean<?> scanForBeans(Class<? extends UI> type) {

		Set<Bean<?>> beans = getBeanManager().getBeans(type,
				new AnnotationLiteral<Any>() {
				});

		if (beans.isEmpty()) {
			getLogger().warning(
					"Could not find UI bean for " + type.getCanonicalName());
			return null;
		}

		if (beans.size() > 1) {
			getLogger().warning(
					"Found multiple UI beans for " + type.getCanonicalName());
			return null;
		}

		return beans.iterator().next();
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
	
	private UITYPE getUIType(int screenSize) {
		if(screenSize < 600) {
			return UITYPE.PHONE;
		}
		
		// TODO figure out how to detect tablet
		
		return UITYPE.DESKTOP;
		
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
		
		return screenWidthPx < screenHeightPx? screenWidthPx : screenHeightPx;
	}
}

// private static final String SCREEN_WIDTH_PARAMETER = "v-sw";
// private static final String SCREEN_HEIGHT_PARAMETER = "v-sh";
// private int dpr;
// private int screenHeightPX;
// private int screenWidthPX;
//
// @Override
// public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
// String height = event.getRequest()
// .getParameter(SCREEN_HEIGHT_PARAMETER);
// if (height != null) {
// screenHeightPX = Integer.parseInt(height);
// }
// String width = event.getRequest().getParameter(SCREEN_WIDTH_PARAMETER);
// if (width != null) {
// screenWidthPX = Integer.parseInt(width);
// }
// String userAgent = event.getRequest().getHeader("user-agent")
// .toLowerCase();
// System.out.println("USER-AGENT: " + userAgent + " screenWidthPX: "
// + screenWidthPX + " screenHeightPX:" + screenHeightPX + " DPR:"
// + dpr);
// if (userAgent.contains("webkit")) {
// return DiaryMobileUI.class;
// } else {
// return DiaryDesktopUI.class;
// }
// }
//
// public void setDPR(String dpr) {
// this.dpr = Integer.parseInt(dpr);
// }
//
// }
