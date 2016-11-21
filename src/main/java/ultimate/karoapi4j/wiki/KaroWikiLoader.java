package ultimate.karoapi4j.wiki;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.KaroWikiURLs;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.URLLoaderUtil;
import ultimate.karoapi4j.utils.web.SimpleCookieHandler;

public class KaroWikiLoader
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger logger = LoggerFactory.getLogger(KaroWikiLoader.class);

	public KaroWikiLoader()
	{
		if(CookieHandler.getDefault() == null)
			CookieHandler.setDefault(new SimpleCookieHandler());
	}

	@SuppressWarnings("unchecked")
	public boolean login(String username, String password) throws IOException
	{
		Map<String, String> parameters;
		String json;
		Map<String, Object> jsonObject;
		
		logger.debug("Performing login: \"" + username + "\"...");

		logger.debug("  Obtaining token...");
		parameters = new HashMap<String, String>();
		parameters.put(KaroWikiURLs.PARAMETER_ACTION, KaroWikiURLs.ACTION_LOGIN);
		parameters.put(KaroWikiURLs.PARAMETER_FORMAT, KaroWikiURLs.FORMAT_JSON);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_USER, username);

		json = URLLoaderUtil.load(new URL(KaroWikiURLs.API_BASE), URLLoaderUtil.formatParameters(parameters));
		// System.out.println(json);
		jsonObject = (Map<String, Object>) JSONUtil.deserialize(json);

		String token = (String) ((Map<String, Object>) jsonObject.get("login")).get("token");

		logger.debug("  Performing login...");

		parameters = new HashMap<String, String>();
		parameters.put(KaroWikiURLs.PARAMETER_ACTION, KaroWikiURLs.ACTION_LOGIN);
		parameters.put(KaroWikiURLs.PARAMETER_FORMAT, KaroWikiURLs.FORMAT_JSON);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_USER, username);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_PASSWORD, password);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_TOKEN, token);

		json = URLLoaderUtil.load(new URL(KaroWikiURLs.API_BASE), URLLoaderUtil.formatParameters(parameters));
		// System.out.println(json);
		jsonObject = (Map<String, Object>) JSONUtil.deserialize(json);
		String result = (String) ((Map<String, Object>) jsonObject.get("login")).get("result");
		String resultUser = (String) ((Map<String, Object>) jsonObject.get("login")).get("lgusername");

		boolean success = "success".equalsIgnoreCase(result) && username.equalsIgnoreCase(resultUser);

		logger.debug("  " + (success ? "Successful!" : "Failed!"));

		return success;
	}

	public boolean logout() throws IOException
	{
		Map<String, String> parameters;
		String json;

		logger.debug("Performing logout...");
		parameters = new HashMap<String, String>();
		parameters.put(KaroWikiURLs.PARAMETER_ACTION, KaroWikiURLs.ACTION_LOGOUT);
		parameters.put(KaroWikiURLs.PARAMETER_FORMAT, KaroWikiURLs.FORMAT_JSON);

		json = URLLoaderUtil.load(new URL(KaroWikiURLs.API_BASE), URLLoaderUtil.formatParameters(parameters));
		// System.out.println(json);

		boolean success = "[]".equalsIgnoreCase(json);

		logger.debug("  " + (success ? "Successful!" : "Failed!"));

		return success;
	}	

	@SuppressWarnings("unchecked")
	public Map<String, Object> query(String title, String prop, String propParam, String... propertiesList) throws IOException
	{
		Map<String, String> parameters;
		String json;
		Map<String, Object> jsonObject;

		StringBuffer properties = new StringBuffer();
		for(String s : propertiesList)
		{
			if(properties.length() > 0)
				properties.append("|");
			properties.append(s);
		}

		logger.debug("Performing prop=" + prop + " for page \"" + title + "\"...");
		parameters = new HashMap<String, String>();
		parameters.put(KaroWikiURLs.PARAMETER_ACTION, KaroWikiURLs.ACTION_QUERY);
		parameters.put(KaroWikiURLs.PARAMETER_FORMAT, KaroWikiURLs.FORMAT_JSON);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_QUERY_PROP, prop);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_QUERY_TITLES, title);
		parameters.put(propParam, properties.toString());

		json = URLLoaderUtil.load(new URL(KaroWikiURLs.API_BASE), URLLoaderUtil.formatParameters(parameters));
		System.out.println(json);
		jsonObject = (Map<String, Object>) JSONUtil.deserialize(json);

		Map<String, Object> pages = (Map<String, Object>) ((Map<String, Object>)jsonObject.get("query")).get("pages");
		
		if(pages.size() != 1)
		{
			logger.debug("  Wrong number of results!");
			return null;
		}
		else if(pages.containsKey("-1"))
		{
			logger.debug("  Page not existing");
			return (Map<String, Object>) pages.get("-1");
		}
		else
		{
			String id = pages.keySet().iterator().next();
			logger.debug("  Page existing with id " + id);
			return (Map<String, Object>) pages.get(id);
		}
	}
	
	public Map<String, Object> queryRevisionProperties(String title, String... propertiesList) throws IOException
	{
		return query(title, KaroWikiURLs.PARAMETER_ACTION_QUERY_PROP_RV, KaroWikiURLs.PARAMETER_ACTION_QUERY_RVPROP, propertiesList);
	}
	
	public Map<String, Object> queryInfoProperties(String title, String... propertiesList) throws IOException
	{
		return query(title, KaroWikiURLs.PARAMETER_ACTION_QUERY_PROP_IN, KaroWikiURLs.PARAMETER_ACTION_QUERY_INPROP, propertiesList);
	}

	public boolean edit(String title, String content, String summary, boolean ignoreConflicts) throws IOException
	{
		Map<String, String> parameters;
		String json;
		Map<String, Object> jsonObject;
		boolean success;

		logger.debug("Performing edit of page \"" + title + "\"...");

		logger.debug("  Obtaining token...");
		String token = getToken(title, "edit");

		logger.debug("  Performing edit...");
		parameters = new HashMap<String, String>();
		parameters.put(KaroWikiURLs.PARAMETER_ACTION, KaroWikiURLs.ACTION_EDIT);
		parameters.put(KaroWikiURLs.PARAMETER_FORMAT, KaroWikiURLs.FORMAT_JSON);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_EDIT_TITLE, title);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_EDIT_TEXT, content);
		if(summary != null)
			parameters.put(KaroWikiURLs.PARAMETER_ACTION_EDIT_SUMMARY, summary);
		if(!ignoreConflicts)
		{
			String baseTimestamp = getTimestamp(title); 
			parameters.put(KaroWikiURLs.PARAMETER_ACTION_EDIT_BASETIMESTAMP, baseTimestamp);
		}
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_EDIT_TOKEN, token);

		json = URLLoaderUtil.load(new URL(KaroWikiURLs.API_BASE), URLLoaderUtil.formatParameters(parameters));
		System.out.println(json);
		jsonObject = (Map<String, Object>) JSONUtil.deserialize(json);

		String result = (String) ((Map<String, Object>) jsonObject.get("edit")).get("result");
		success = "success".equalsIgnoreCase(result);
		if(!success)
		{
			// TODO captcha
		}
		logger.debug("  " + (success ? "Successful!" : "Failed!"));
		return success;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getContent(String title) throws IOException
	{
		Map<String, Object> properties = queryRevisionProperties(title, "content");
		if(properties.containsKey("missing"))
			return null;
		
		List<?> revisions = (List) properties.get("revisions");
		return (String) ((Map<String, Object>) revisions.get(revisions.size()-1)).get("*");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getTimestamp(String title) throws IOException
	{
		Map<String, Object> properties = queryRevisionProperties(title, "timestamp");
		if(properties.containsKey("missing"))
			return null;
		
		List<?> revisions = (List) properties.get("revisions");
		return (String) ((Map<String, Object>) revisions.get(revisions.size()-1)).get("timestamp");
	}

	public String getToken(String title, String action) throws IOException
	{
		Map<String, Object> properties = query(title, KaroWikiURLs.PARAMETER_ACTION_QUERY_PROP_IN, KaroWikiURLs.PARAMETER_ACTION_QUERY_INTOKEN, action);
		return (String) properties.get(action + "token");
	}

	private String getCaptchaAnswer(String question)
	{
		return null;
	}
}
