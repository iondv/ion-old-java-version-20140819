package ion.web.app.controllers.action;

import ion.core.IonException;
import ion.web.app.IActionHandler;
import ion.web.app.util.PageContext;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public class TestActionHandler implements IActionHandler {

	@Override
	public Object executeAction(PageContext context,
			MultipartHttpServletRequest request) throws IonException {
        System.out.println(String.format("%s.executeAction(): %s.%s", this.getClass().getSimpleName(), context.Class.getName(), context.Id));
		return null;
	}

}
