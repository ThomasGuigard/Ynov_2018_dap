package fr.ynov.dap.dap;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MissingServletRequestParameterException;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;

/**
 * The Class GoogleAccountService.
 */
@Service
public class GoogleAccountService extends GoogleService{
	
	/** The Constant SENSIBLE_DATA_LAST_CHAR. */
	private static final int SENSIBLE_DATA_LAST_CHAR = 0;
	
	/** The Constant SENSIBLE_DATA_FIRST_CHAR. */
	private static final int SENSIBLE_DATA_FIRST_CHAR = 0;
	
	/** The logger. */
	//TODO brc by Djer Final ?
	private static Logger logger = LogManager.getLogger(GoogleAccountService.class);

	/**
	 * Handle the Google response.
	 *
	 * @param code    The (encoded) code use by Google (token, expirationDate,...)
	 * @param request The HTTP Request
	 * @param session the HTTP Session
	 * @return the view to display
	 * @throws ServletException When Google account could not be connected to DaP.
	 */
    public String oAuthCallback(final String code, final HttpServletRequest request,
    		final HttpSession session) throws ServletException {
    	
        final String decodedCode = extracCode(request);

        final String redirectUri = buildRedirectUri(request, "/oAuth2Callback");

        final String userId = getUserid(session);
        try {
            GoogleAuthorizationCodeFlow flow = null;
			try {
				flow = getFlow();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
			    //TODO brc by Djer "e.printStackTrace()" affiche la trace dans la CONSOLE, pas très utile.
			    // Envoie la plutot dans les logs en Warning/Error !
				e.printStackTrace();
			}
			//TODO brc by Djer Pas de "séparateur" dans les Logs, il existe des outils pour lire les logs si besoin de "présentation"
			logger.info("----------");
			//TODO brc by Djer contextualise tes logs ! ("for user : " + userId)
			logger.info("decodedCode : " + decodedCode);
			logger.info("redirectUri : " + redirectUri);
			//TODO brc by Djer Par defaut (si pas de "toString() dans la classe) va afficher la pseudo adresse mémoire de l'instance
			// Jen e suis pas certains que cela soit très pertinent. Eventuellement en "debug" et encore.
			logger.info("flow : " + flow);
            final TokenResponse response = flow.newTokenRequest(decodedCode).setRedirectUri(redirectUri).execute();
            
            final Credential credential = flow.createAndStoreCredential(response, userId);
            if (null == credential || null == credential.getAccessToken()) {
                logger.warn("Trying to store a NULL AccessToken for user : " + userId);
            }

            if (logger.isDebugEnabled() && null != credential && null != credential.getAccessToken()) {
                    logger.debug("New user credential stored with userId : " + userId + "partial AccessToken : "
                            + credential.getAccessToken().substring(SENSIBLE_DATA_FIRST_CHAR,
                                    SENSIBLE_DATA_LAST_CHAR));
            }
            // onSuccess(request, resp, credential);
        } catch (IOException e) {
            logger.error("Exception while trying to store user Credential", e);
            throw new ServletException("Error while trying to conenct Google Account");
        }
        logger.info("oauthCallback OK");
        return "redirect:/";
    }

    /**
     * retrieve the User ID in Session.
     * @param session the HTTP Session
     * @return the current User Id in Session
     * @throws ServletException if no User Id in session
     */
    private String getUserid(final HttpSession session) throws ServletException {
        String userId = null;
        if (null != session && null != session.getAttribute("userId")) {
            userId = (String) session.getAttribute("userId");
        }

        if (null == userId) {
            logger.error("userId in Session is NULL in Callback");
            throw new ServletException("Error when trying to add Google acocunt : userId is NULL is User Session");
        }
        return userId;
    }

    /**
     * Extract OAuth2 Google code (from URL) and decode it.
     * @param request the HTTP request to extract OAuth2 code
     * @return the decoded code
     * @throws ServletException if the code cannot be decoded
     */
    private String extracCode(final HttpServletRequest request) throws ServletException {
        final StringBuffer buf = request.getRequestURL();
        if (null != request.getQueryString()) {
            buf.append('?').append(request.getQueryString());
        }
        final AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(buf.toString());
        final String decodeCode = responseUrl.getCode();

        if (decodeCode == null) {
            throw new MissingServletRequestParameterException("code", "String");
        }

        if (null != responseUrl.getError()) {
            logger.error("Error when trying to add Google acocunt : " + responseUrl.getError());
            throw new ServletException("Error when trying to add Google acocunt");
            // onError(request, resp, responseUrl);
        }

        return decodeCode;
    }

    /**
     * Build a current host (and port) absolute URL.
     * @param req         The current HTTP request to extract schema, host, port
     *                    informations
     * @param destination the "path" to the resource
     * @return an absolute URI
     */
    protected String buildRedirectUri(final HttpServletRequest req, final String destination) {
        final GenericUrl url = new GenericUrl(req.getRequestURL().toString());
        url.setRawPath(destination);
        return url.build();
    }
    
    /**
     * Add a Google account (user will be prompt to connect and accept required
     * access).
     *
     * @param userId  the user to store Data
     * @param request the HTTP request
     * @param session the HTTP session
     * @return the view to Display (on Error)
     * @throws GeneralSecurityException the general security exception
     */
    
    public String addAccount(final String userId, final HttpServletRequest request,
            final HttpSession session) throws GeneralSecurityException {
        String response = "errorOccurs";
        GoogleAuthorizationCodeFlow flow;
        Credential credential = null;
        try {
            flow = getFlow();
            //TODO brc by Djer Pseudo adresse mémoire vraiment utile ?
            logger.info("flow : " + flow);
            credential = flow.loadCredential(userId);

            if (credential != null && credential.getAccessToken() != null) {
                response = "AccountAlreadyAdded";
            } else {
                // redirect to the authorization flow
                final AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
                authorizationUrl.setRedirectUri(buildRedirectUri(request, cfg.getoAuth2CallbackUrl()));
                // store userId in session for CallBack Access
                session.setAttribute("userId", userId);
                response = "redirect:" + authorizationUrl.build();
            }
        } catch (IOException e) {
            logger.error("Error while loading credential (or Google Flow)", e);
        }
        // only when error occurs, else redirected BEFORE
        return response;
    }
}