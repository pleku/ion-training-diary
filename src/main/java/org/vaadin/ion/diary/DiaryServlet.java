package org.vaadin.ion.diary;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.json.JSONObject;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.vaadin.addon.touchkit.server.TouchKitServlet;
import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServletService;

@WebServlet("/*")
public class DiaryServlet extends TouchKitServlet {

    private DiaryUIProvider uiProvider = new DiaryUIProvider();

    private BootstrapListener listener = new BootstrapListener() {

        @Override
        public void modifyBootstrapPage(BootstrapPageResponse response) {
            // add an extra script to get the DPR and POST it
            Element last = response.getDocument().getElementsByTag("script")
                    .last();
            Element ppiScript = createPPiScript();
            last.before(ppiScript);
        }

        @Override
        public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
        }
    };

    @Override
    protected VaadinServletService createServletService(
            DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        VaadinServletService service = new DiaryServletService(this,
                deploymentConfiguration);
        service.init();
        return service;
    }

    /**
     * Returns an script element containing javascript for posting the PPI to
     * server.
     */
    protected Element createPPiScript() {
        Element ppiScript = new Element(Tag.valueOf("script"), "").attr("type",
                "text/javascript");

        StringBuilder builder = new StringBuilder();
        builder.append("//<![CDATA[\n");
        builder.append("if (window.devicePixelRatio !== undefined) {\n");
        builder.append("var dpr = ");
        builder.append(JSONObject.quote(""));
        builder.append(" + window.devicePixelRatio;\n");
        builder.append("var url = window.location.href.replace(/#.*/,'');\n");
        builder.append("url += ");
        builder.append(JSONObject.quote("?"
                + DiaryServletService.DPR_REQUEST_PARAMETER + "="));
        builder.append(" + dpr;\n");
        builder.append("var r;\n");
        builder.append("try {\n");
        builder.append("r = new XMLHttpRequest();\n");
        builder.append("} catch (e) {\n");
        builder.append("r = new ActiveXObject(");
        builder.append(JSONObject.quote("MSXML2.XMLHTTP.3.0"));
        builder.append(");\n");
        builder.append("}\n");
        builder.append("r.open('POST', url, true);\n");
        builder.append("r.send(null);\n");
        builder.append("ppiElement.remove();\n");
        builder.append("}\n");
        builder.append("//]]>");
        ppiScript.appendChild(new DataNode(builder.toString(), ppiScript
                .baseUri()));
        return ppiScript;
    }

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event)
                    throws ServiceException {
                event.getSession().addBootstrapListener(listener);
                event.getSession().addUIProvider(uiProvider);
            }
        });
    }
}
