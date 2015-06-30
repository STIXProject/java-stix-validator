package org.mitre.stix.validator;
import com.google.gson.Gson;

import spark.Response;
import spark.ResponseTransformer;

import java.util.HashMap;

/**
 * Used to transform the ValidationResuts into json.
 * 
 * @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
 *
 */
public class JsonTransformer implements ResponseTransformer {

	private Gson gson = new Gson();

	@Override
	public String render(Object model) {
		if (model instanceof Response) {
			return gson.toJson(new HashMap<>());
		}
		
		return gson.toJson(model);
	}
}