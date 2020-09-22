package ion.web.app;

import ion.core.IonException;
import ion.web.app.util.PageContext;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface IActionHandler {
    Object executeAction(PageContext context, MultipartHttpServletRequest request) throws IonException;
}