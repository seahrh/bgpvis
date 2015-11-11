package bgpvis.validation;

import static bgpvis.util.StringUtil.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class ValidationResult {
	private static final Logger log = LoggerFactory.getLogger(ValidationResult.class);
	private static final String ERROR_MESSAGE_SEPARATOR = "\n\t";
	private List<String> errors = new ArrayList<String>();
	private Object inputValue = null;
	private String inputName = null;
	
	public ValidationResult(List<String> errors, String name, Object value) {
		this.errors = errors;
		this.inputName = name;
		this.inputValue = value;
	}
	
	public boolean hasErrors() {
		if (errors.isEmpty()) {
			return false;
		}
		return true;
	}
	
	public Object inputValue() {
		return inputValue;
	}
	
	public String inputName() {
		return inputName;
	}
	
	public Optional<List<String>> errors() {
		if (errors.isEmpty()) {
			return Optional.absent();
		}
		return Optional.of(errors);
	}
	
	@Override
	public String toString() {
		if (! hasErrors()) {
			return "No validation errors";
		}
		List<String> msg = new ArrayList<String>();
		msg.add(concat(inputName, " validation errors"));
		msg.addAll(errors);
		msg.add(concat("Input value [", inputValue, "]"));
		return concat(msg, ERROR_MESSAGE_SEPARATOR);
	}

}
