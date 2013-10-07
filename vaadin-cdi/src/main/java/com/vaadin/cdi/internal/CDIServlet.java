package com.vaadin.cdi.internal;

import com.vaadin.cdi.CDIVaadinServletService;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;

public class CDIServlet extends VaadinServlet {

	@Override
	protected VaadinServletService createServletService(
			DeploymentConfiguration deploymentConfiguration)
			throws ServiceException {
		CDIVaadinServletService service = new CDIVaadinServletService(this,
				deploymentConfiguration);
		service.init();
		return service;
	}

}
