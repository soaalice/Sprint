package dev.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.annotation.authentification.Authentified;
import mg.itu.prom16.FrontController;

import java.util.Arrays;

public class Authentificator {

    public static boolean isAuthorized(HttpServletRequest request, Authentified authentifiedAnnotation) {
	HttpSession session = request.getSession();

	Object authentifiedObject = session.getAttribute(FrontController.SESSION_AUTHENTIFIED);
	if (authentifiedObject == null)
	    return false;

	boolean authentified = (boolean) authentifiedObject;
	if (authentifiedAnnotation.roles().length == 0)
	    return authentified;

	Object roleObject = session.getAttribute(FrontController.SESSION_ROLE);
	if (roleObject == null)
	    return false;

	String role = roleObject.toString();
	String[] authorizedRoles = authentifiedAnnotation.roles();
	return authentified && Arrays.asList(authorizedRoles).contains(role);
    }

}
