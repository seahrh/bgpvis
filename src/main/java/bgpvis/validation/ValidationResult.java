package bgpvis.validation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class ValidationResult {
	private static final Logger log = LoggerFactory.getLogger(ValidationResult.class);
	private List<String> errors = new ArrayList<String>();
	private Object correctedValue = null;
	

	private ValidationResult() {
		// Do not allow no-arg constructor; this is an immutable class.
	}
	
	public ValidationResult(List<String> errors, Object correctedValue) {
		this.errors = errors;
		this.correctedValue = correctedValue;
	}
	
	public ValidationResult(List<String> errors) {
		this(errors, null);
	}
	
	public Optional<List<String>> errors() {
		if (errors.isEmpty()) {
			return Optional.absent();
		}
		return Optional.of(errors);
	}
	
	public Optional<Object> correctedValue() {
		if (correctedValue == null) {
			return Optional.absent();
		}
		return Optional.of(correctedValue);
	}

}
