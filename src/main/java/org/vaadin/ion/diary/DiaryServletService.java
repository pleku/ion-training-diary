package org.vaadin.ion.diary;

import java.io.IOException;
import java.util.List;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;

public class DiaryServletService extends VaadinServletService {

    public static final String DPR_REQUEST_PARAMETER = "v-dpr";
    private RequestHandler dprRequestHandler = new RequestHandler() {

        @Override
        public boolean handleRequest(VaadinSession session,
                VaadinRequest request, VaadinResponse response)
                throws IOException {
            if (isPPIRequest(request)) {
                String ppi = getPPI(request);
                for (UIProvider uiProvider : session.getUIProviders()) {
                    if (uiProvider instanceof DiaryUIProvider) {
                        ((DiaryUIProvider) uiProvider).setDPR(ppi);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    };

    public DiaryServletService(VaadinServlet servlet,
            DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        super(servlet, deploymentConfiguration);
    }

    protected boolean isPPIRequest(VaadinRequest request) {
        return "POST".equals(request.getMethod())
                && request.getParameter(DPR_REQUEST_PARAMETER) != null;
    }

    protected String getPPI(VaadinRequest request) {
        return request.getParameter(DPR_REQUEST_PARAMETER);
    }

    @Override
    protected List<RequestHandler> createRequestHandlers()
            throws ServiceException {
        List<RequestHandler> requestHandlers = super.createRequestHandlers();
        requestHandlers.add(dprRequestHandler);
        return requestHandlers;
    }
}
