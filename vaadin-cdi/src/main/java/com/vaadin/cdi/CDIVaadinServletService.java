package com.vaadin.cdi;
import java.util.List;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.communication.PushRequestHandler;


public class CDIVaadinServletService extends VaadinServletService {
	
	// FIXME
	private final static boolean atmosphereAvailable = false;
	

	public CDIVaadinServletService(VaadinServlet servlet,
			DeploymentConfiguration deploymentConfiguration)
			throws ServiceException {
		super(servlet, deploymentConfiguration);
	}

	@Override
	protected List<RequestHandler> createRequestHandlers()
			throws ServiceException {
		List<RequestHandler> handlers = super.createRequestHandlers();
		handlers.remove(0);
        handlers.add(0, new CDIBootstrapHandler());
        handlers.add(new CDIServletUIInitHandler());
        if (atmosphereAvailable) {
            handlers.add(new PushRequestHandler(this));
        }
        return handlers;
	}

}
