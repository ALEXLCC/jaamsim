/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2010-2012 Ausenco Engineering Canada Inc.
 * Copyright (C) 2016-2018 JaamSim Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaamsim.input;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import com.jaamsim.EntityProviders.EntityProvConstant;
import com.jaamsim.EntityProviders.EntityProvExpression;
import com.jaamsim.EntityProviders.EntityProvider;
import com.jaamsim.Samples.SampleConstant;
import com.jaamsim.Samples.SampleExpression;
import com.jaamsim.Samples.SampleProvider;
import com.jaamsim.Samples.TimeSeriesConstantDouble;
import com.jaamsim.StringProviders.StringProvExpression;
import com.jaamsim.StringProviders.StringProvSample;
import com.jaamsim.StringProviders.StringProvider;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.basicsim.Group;
import com.jaamsim.basicsim.ObjectType;
import com.jaamsim.datatypes.BooleanVector;
import com.jaamsim.datatypes.DoubleVector;
import com.jaamsim.datatypes.IntegerVector;
import com.jaamsim.math.Color4d;
import com.jaamsim.ui.NaturalOrderComparator;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.TimeUnit;
import com.jaamsim.units.Unit;
import com.jaamsim.units.UserSpecifiedUnit;

public abstract class Input<T> {
	protected static final String INP_ERR_COUNT = "Expected an input with %s value(s), received: %s";
	protected static final String INP_ERR_RANGECOUNT = "Expected an input with %d to %d values, received: %s";
	protected static final String INP_ERR_RANGECOUNTMIN = "Expected an input with at least %d values, received: %s";
	protected static final String INP_ERR_EVENCOUNT = "Expected an input with even number of values, received: %s";
	protected static final String INP_ERR_ODDCOUNT = "Expected an input with odd number of values, received: %s";
	protected static final String INP_ERR_BOOLEAN = "Expected a boolean value, received: %s";
	protected static final String INP_ERR_INTEGER = "Expected an integer value, received: %s";
	protected static final String INP_ERR_INTEGERRANGE = "Expected an integer between %d and %d, received: %d";
	protected static final String INP_ERR_DOUBLE = "Expected an numeric value, received: %s";
	protected static final String INP_ERR_DOUBLERANGE = "Expected a number between %f and %f, received: %f";
	protected static final String INP_ERR_TIME = "Expected a time value (hh:mm or hh:mm:ss), received: %s";
	protected static final String INP_ERR_TIMEVALUE = "Expected a numeric value, 12 numeric values, or a probabilty distribution, received: %s";
	protected static final String INP_ERR_BADSUM = "List must sum to %f, received:%f";
	protected static final String INP_ERR_MONOTONIC = "List must %s monotonically. Values starting at index %s are %s s, %s s, ...";
	protected static final String INP_ERR_BADCHOICE = "Expected one of %s, received: %s";
	protected static final String INP_ERR_ELEMENT = "Error parsing element %d: %s";
	protected static final String INP_ERR_ENTNAME = "Could not find an Entity named: %s";
	protected static final String INP_ERR_NOUNITFOUND = "A unit is required, could not parse '%s' as a %s";
	protected static final String INP_ERR_UNITNOTFOUND = "A unit of type '%s' is required";
	protected static final String INP_ERR_NOTUNIQUE = "List must contain unique entries, repeated entry: %s";
	protected static final String INP_ERR_NOTVALIDENTRY = "List must not contain: %s";
	protected static final String INP_ERR_ENTCLASS = "Expected a %s, %s is a %s";
	protected static final String INP_ERR_INTERFACE = "Expected an object implementing %s, %s does not";
	protected static final String INP_ERR_UNITS = "Unit types do not match";
	protected static final String INP_ERR_UNITUNSPECIFIED = "Unit type has not been specified";
	protected static final String INP_ERR_NOTSUBCLASS = "Expected a subclass of %s, got %s";
	protected static final String INP_ERR_BADDATE = "Expected a valid RFC8601 datetime, got: %s";
	protected static final String INP_ERR_BADEXP = "Error parsing expression: %s";
	protected static final String INP_VAL_LISTSET = "Values found for %s without %s being set";
	protected static final String INP_VAL_LISTSIZE = "%s and %s must be of equal size";

	protected static final String VALID_SAMPLE_PROV = "Accepts a number with units of type %s, an object that returns such a number, or an expression that returns such a number.";
	protected static final String VALID_SAMPLE_PROV_DIMLESS = "Accepts a dimensionless number, an object that returns such a number, or an expression that returns such a number.";
	protected static final String VALID_SAMPLE_PROV_UNIT = "Accepts a number with or without units, an object that returns such a number, or an expression that returns such a number. "
	                                                     + "An input to the UnitType keyword MUST BE PROVIDED before an input to this keyword can be entered.";
	protected static final String VALID_STRING_PROV = "Accepts an expression that returns a number, string, entity, array, or map.";
	protected static final String VALID_ENTITY_PROV = "Accepts an entity name or an expression that returns an entity.";
	protected static final String VALID_ENTITY_PROV_TYPE = "Accepts the name of an entity of type %s or an expression that returns such an entity.";
	protected static final String VALID_COLOUR = "Accepts a colour name, an RGB value, or an RGB/transparency value.";
	protected static final String VALID_INTEGER = "Accepts a dimensionless integer value.";
	protected static final String VALID_VALUE = "Accepts a number with units of type %s.";
	protected static final String VALID_VALUE_DIMLESS = "Accepts a dimensionless number.";
	protected static final String VALID_VALUE_UNIT = "Accepts a number with or without units. "
	                                               + "An input to the UnitType keyword MUST BE PROVIDED before an input to this keyword can be entered.";
	protected static final String VALID_ENTITY = "Accepts the name of an entity.";
	protected static final String VALID_ENTITY_TYPE = "Accepts the name of an entity of type %s.";
	protected static final String VALID_INTERFACE_ENTITY = "Accepts the name of an entity that supports the %s interface.";
	protected static final String VALID_BOOLEAN = "Accepts the text TRUE or FALSE (case sensitive).";
	protected static final String VALID_STRING = "Accepts a text string. The string must be enclosed by single quotes if it includes a space.";
	protected static final String VALID_FILE = "Accepts a file path enclosed by single quotes.";
	protected static final String VALID_DIR = "Accepts a directory path enclosed by single quotes.";
	protected static final String VALID_EXP = "Accepts an expression that returns a dimensionless number. A value of zero implies FALSE; non-zero implies TRUE.";
	protected static final String VALID_KEYEVENT = "Accepts a single character representing a key on the keyboard. "
	                                             + "For non-printing keys, enter the key's name such as HOME, ESCAPE, SPACE, F1, etc.";
	protected static final String VALID_VALUE_LIST = "Accepts a list of numbers separated by spaces, followed by a unit for these values, if required.";
	protected static final String VALID_VALUE_LIST_DIMLESS = "Accepts a list of dimensionless numbers separated by spaces.";
	protected static final String VALID_SAMPLE_LIST = "Accepts a list containing numbers with or without units, objects that return such a number, or expressions that return such a number. "
	                                                + "Each entry in the list must be enclosed by braces.";
	protected static final String VALID_SAMPLE_LIST_DIMLESS = "Accepts a list containing dimensionless numbers, objects that return such a number, or expressions that return such a number. "
	                                                        + "Each entry in the list must be enclosed by braces.";
	protected static final String VALID_UNIT_TYPE_LIST = "Accepts a list of unit types separated by spaces.";
	protected static final String VALID_STRING_PROV_LIST = "Accepts a list of expressions that return a number, string, entity, or array. "
	                                                     + "Each entry in the list must be enclosed by braces.";
	protected static final String VALID_COLOR_LIST = "Accepts a list of colours that are expressed as either a colour name or an RGB value. "
	                                               + "Each entry in the list must be enclosed by braces.";
	protected static final String VALID_ENTITY_LIST = "Accepts a list of entity names separated by spaces.";
	protected static final String VALID_FORMAT = "Accepts a Java format string for a number. For example, '%.3f' would print a number with three decimal places.";
	protected static final String VALID_VEC3D = "Accepts three numbers separated by spaces followed by a unit of type %s. "
	                                          + "If only two numbers are entered, the third value defaults to zero.";
	protected static final String VALID_VEC3D_DIMLESS = "Accepts three dimensionless numbers separated by spaces.";
	protected static final String VALID_VEC3D_LIST = "Accepts a list of vectors enclosed by braces. "
	                                               + "Each vector consists of three numbers separated by spaces followed by a unit of type %s. "
	                                               + "If a vector has only two numbers, the third value defaults to zero.";
	protected static final String VALID_ATTRIB_DEF = "Accepts a list of attribute definitions each consisting of an attribute name followed by an expression "
	                                               + "that sets the initial value for the attribute. "
	                                               + "Each definition in the list must be enclosed by braces.";
	protected static final String VALID_CUSTOM_OUT = "Accepts a list of custom output definitions each consisting of a custom output name, an expression, and a unit type (if required). "
	                                               + "Each definition in the list must be enclosed by braces.";

	public static final String POSITIVE_INFINITY = "Infinity";
	public static final String NEGATIVE_INFINITY = "-Infinity";
	public static final String SEPARATOR = "  ";
	public static final String BRACE_SEPARATOR = " ";

	private String keyword; // the preferred name for the input keyword
	private final String category;

	protected T defValue;
	protected T value;

	private boolean edited; // indicates if input has been edited for this entity
	private boolean promptReqd; // indicates whether to prompt the user to save the configuration file
	private boolean hidden; // Hide this input from the EditBox
	protected boolean isDef; // Is this input still the default value?
	protected String[] valueTokens; // value from .cfg file
	private String defText; // special text to show in the default column of the Input Editor
	private boolean isReqd;     // indicates whether this input must be provided by the user
	private boolean isValid;  // if false, the input is no longer valid and must be re-entered

	public static final Comparator<Object> uiSortOrder = new NaturalOrderComparator();

	public Input(String key, String cat, T def) {
		keyword = key;
		category = cat;
		setDefaultValue(def);

		edited = false;
		promptReqd = true;
		isDef = true;
		hidden = false;
		valueTokens = null;
		defText = null;
		isReqd = false;
		isValid = true;
	}

	public void reset() {
		this.setDefaultValue( this.getDefaultValue() );
		valueTokens = null;
		edited = false;
		isDef = true;
		isValid = true;
	}

	/**
	 * Assigns the internal state for this input to the same values as the
	 * specified input.
	 * @param in - input object to be copied.
	 */
	public void copyFrom(Input<?> in) {

		@SuppressWarnings("unchecked")
		Input<T> inp = (Input<T>) in;

		// Copy the internal state
		value = inp.value;
		valueTokens = inp.valueTokens;
		isDef = false;
		edited = true;
		isValid = true;
	}

	/**
	 * Assigns the internal state for this input to the same values as the
	 * specified input.
	 * <p>
	 * This method provides the same function as copyFrom by re-parsing the input data instead of
	 * copying the internal variables. This operation is much slower, but is needed for inputs that
	 * cannot be copied successfully using copyFrom, such as inputs that accept an expression.
	 * @param in - input object to be copied.
	 */
	public void parseFrom(Input<?> in) {
		ArrayList<String> toks = new ArrayList<>(Arrays.asList(valueTokens));
		KeywordIndex kw = new KeywordIndex(in.getKeyword(), toks, null);
		parse(kw);
	}

	/**
	 * Deletes any use of the specified entity from this input.
	 * @param ent - entity whose references are to be deleted
	 */
	public void removeReferences(Entity ent) {}

	/**
	 * Describes the valid inputs for this type of input.
	 * @return description of valid inputs
	 */
	public String getValidInputDesc() {
		return null;
	}

	@Override
	public String toString() {
		return String.format("%s", value);
	}

	public final String getKeyword() {
		return keyword;
	}

	public void setKeyword(String str) {
		keyword = str;
	}

	public final String getCategory() {
		return category;
	}

	public boolean isSynonym() {
		return false;
	}

	public void setDefaultText(String str) {
		defText = str;
	}

	public String getDefaultText() {
		return defText;
	}

	public void setDefaultValue(T val) {
		defValue = val;
		value = val;
	}

	public T getDefaultValue() {
		return defValue;
	}

	public String getDefaultString() {
		if (defValue == null)
			return "";
		return defValue.toString();
	}

	public T getValue() {
		return value;
	}

	public void setHidden(boolean hide) {
		hidden = hide;
	}

	public boolean getHidden() {
		return hidden;
	}

	public void setEdited(boolean bool) {
		edited = bool;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setPromptReqd(boolean bool) {
		promptReqd = bool;
	}

	public boolean isPromptReqd() {
		return promptReqd;
	}

	public void setRequired(boolean bool) {
		isReqd = bool;
	}

	public boolean isRequired() {
		return isReqd;
	}

	public void setValid(boolean bool) {
		isValid = bool;
	}

	public boolean isValid() {
		return isValid;
	}

	public void validate() throws InputErrorException {
		if (isReqd && isDef && !hidden)
			throw new InputErrorException("An input must be provided for the keyword '%s'.", keyword);
	}

	public void setTokens(KeywordIndex kw) {
		isDef = false;
		valueTokens = kw.getArgArray();
	}

	/**
	 * Add the given tokens to the present value tokens
	 */
	public void addTokens(String[] args) {

		// Create an array sized for the addition of new tokens
		String[] newValueTokens;
		if (valueTokens == null) {
			newValueTokens = new String[args.length - 1];

			// Copy the new tokens into the array
			System.arraycopy(args, 1, newValueTokens, 0, args.length - 1);
		}
		else {
			newValueTokens = new String[valueTokens.length + args.length - 1];

			// Copy the old tokens into the array
			System.arraycopy(valueTokens, 0, newValueTokens, 0, valueTokens.length);

			// Copy the new tokens into the array
			System.arraycopy(args, 1, newValueTokens, valueTokens.length, args.length - 1);
		}

		valueTokens = newValueTokens;
	}

	/**
	 * Remove the given tokens from the present value tokens
	 * @return - true if all the tokens were successfully removed
	 */
	public boolean removeTokens(String[] args) {

		int newSize = valueTokens.length - (args.length - 1);
		if( newSize >= 0 ) {

			// Create an array sized for the removal of tokens
			String[] newValueTokens = new String[newSize];
			int index = 0;

			// Loop through the original tokens
			for (int i = 0; i < valueTokens.length; i++ ) {

				// Determine if this token is to be kept
				boolean keep = true;
				for (int j = 1; j < args.length; j++) {
					if (args[j].equals(valueTokens[i])) {
						keep = false;
						break;
					}
				}

				// If the token is to be kept, add it to the array
				if (keep) {
					newValueTokens[index] = valueTokens[i];
					index++;
				}
			}

			// If the correct number of items were kept, reset valueTokens
			if (index == newSize) {
				valueTokens = newValueTokens;
				return true;
			}
		}
		return false;
	}

	/**
	 * Append the given tokens to the present value tokens
	 */
	public void appendTokens(String[] args) {

		// Determine if braces need to be added around original tokens
		boolean addBracesAroundOriginalTokens = false;

		// Determine the size for an array with original and new tokens
		int newSize;
		int valueTokensLength = 0;
		if (valueTokens == null)
			newSize = args.length;
		else {
			valueTokensLength = valueTokens.length;
			newSize = valueTokens.length + args.length;

			if (! valueTokens[0].equals( "{" )) {
				addBracesAroundOriginalTokens = true;
				newSize += 2;
			}
		}

		// Determine if braces need to be added around new tokens
		boolean addBracesAroundNewTokens = false;
		if (! args[0].equals( "{" )) {
			addBracesAroundNewTokens = true;
			newSize += 2;
		}

		// Create an array sized for the addition of new tokens
		String[] newValueTokens = new String[newSize];

		// Copy the old and new tokens into the array
		if (addBracesAroundOriginalTokens) {
			newValueTokens[0] = "{";
			System.arraycopy(valueTokens, 0, newValueTokens, 1, valueTokens.length);
			newValueTokens[valueTokens.length + 1] = "}";

			if (addBracesAroundNewTokens) {
				newValueTokens[valueTokens.length + 2] = "{";
				System.arraycopy(args, 0, newValueTokens, valueTokens.length+3, args.length);
				newValueTokens[newSize-1] = "}";
			}
			else {
				System.arraycopy(args, 0, newValueTokens, valueTokens.length+2, args.length);
			}
		}
		else {
			if (valueTokens != null)
				System.arraycopy(valueTokens, 0, newValueTokens, 0, valueTokens.length);

			if (addBracesAroundNewTokens) {
				newValueTokens[valueTokensLength] = "{";
				System.arraycopy(args, 0, newValueTokens, valueTokensLength+1, args.length);
				newValueTokens[newSize-1] = "}";
			}
			else {
				System.arraycopy(args, 0, newValueTokens, valueTokensLength, args.length);
			}
		}

		valueTokens = newValueTokens;
	}

	public boolean isDefault() {
		return isDef;
	}

	public void getValueTokens(ArrayList<String> toks) {
		if (valueTokens == null)
			return;

		for (String each : valueTokens)
			toks.add(each);
	}

	public final String getValueString() {
		if (isDefault()) return "";

		ArrayList<String> tmp = new ArrayList<>();
		try {
			getValueTokens(tmp);
		} catch (Exception e) {
			InputAgent.logMessage("Error in input, value has been cleared. Keyword: %s",
					this.getKeyword());
			InputAgent.logStackTrace(e);
			this.reset();
		}
		if (tmp.size() == 0) return "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tmp.size(); i++) {
			String dat = tmp.get(i);
			if (dat == null) continue;
			if (i > 0) {
				if (dat.equals("{") || dat.equals("}") || tmp.get(i-1).equals("{")) {
					sb.append(Input.BRACE_SEPARATOR);
				}
				else {
					sb.append(Input.SEPARATOR);
				}
			}

			if (Parser.needsQuoting(dat) && !dat.equals("{") && !dat.equals("}"))
				sb.append("'").append(dat).append("'");
			else
				sb.append(dat);
		}
		return sb.toString();
	}

	public abstract void parse(KeywordIndex kw) throws InputErrorException;


	public static void assertCount(DoubleVector input, int... counts)
	throws InputErrorException {
		// If there is no constraint on the element count, return
		if (counts == null || counts.length == 0)
			return;

		// If there is an exact constraint, check the count
		for (int each : counts) {
			if (each == input.size())
				return;
		}

		// Input size is not equal to any of the specified counts
		throw new InputErrorException(INP_ERR_COUNT, Arrays.toString(counts), input.toString());
	}

	/**
	 * Verifies that the correct number of inputs have been provided.
	 * @param kw - object containing the inputs.
	 * @param counts - list of valid numbers of inputs. All other numbers are invalid.
	 * @throws InputErrorException
	 */
	public static void assertCount(KeywordIndex kw, int... counts)
	throws InputErrorException {
		// If there is no constraint on the element count, return
		if (counts.length == 0)
			return;

		// If there is an exact constraint, check the count
		for (int each : counts) {
			if (each == kw.numArgs())
				return;
		}

		// Input size is not equal to any of the specified counts
		if (counts.length == 1)
			throw new InputErrorException(INP_ERR_COUNT, counts[0], kw.argString());
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(counts[0]);
			for (int i=1; i<counts.length-1; i++) {
				sb.append(", ").append(counts[i]);
			}
			sb.append(" or ").append(counts[counts.length-1]);
			throw new InputErrorException(INP_ERR_COUNT, sb.toString(), kw.argString());
		}
	}

	/**
	 * Verifies that the correct number of inputs have been provided.
	 * @param kw - object containing the inputs.
	 * @param min - minimum number of inputs that are valid
	 * @param max - maximum number of inputs that are valid
	 * @throws InputErrorException
	 */
	public static void assertCountRange(KeywordIndex kw, int min, int max)
	throws InputErrorException {
		// For a range with a single value, fall back to the exact test
		if (min == max) {
			Input.assertCount(kw, min);
			return;
		}

		if (kw.numArgs() < min || kw.numArgs() > max) {
			if (max == Integer.MAX_VALUE)
				throw new InputErrorException(INP_ERR_RANGECOUNTMIN, min, kw.argString());
			throw new InputErrorException(INP_ERR_RANGECOUNT, min, max, kw.argString());
		}
	}

	public static void assertCount(List<String> input, int... counts)
	throws InputErrorException {
		// If there is no constraint on the element count, return
		if (counts.length == 0)
			return;

		// If there is an exact constraint, check the count
		for (int each : counts) {
			if (each == input.size())
				return;
		}

		// Input size is not equal to any of the specified counts
		throw new InputErrorException(INP_ERR_COUNT, Arrays.toString(counts), input.toString());
	}

	public static void assertCountRange(DoubleVector input, int min, int max)
	throws InputErrorException {
		// For a range with a single value, fall back to the exact test
		if (min == max) {
			Input.assertCount(input, min);
			return;
		}

		if (input.size() < min || input.size() > max) {
			if (max == Integer.MAX_VALUE)
				throw new InputErrorException(INP_ERR_RANGECOUNTMIN, min, input.toString());
			throw new InputErrorException(INP_ERR_RANGECOUNT, min, max, input.toString());
		}
	}

	public static void assertCountRange(List<String> input, int min, int max)
	throws InputErrorException {
		// For a range with a single value, fall back to the exact test
		if (min == max) {
			Input.assertCount(input, min);
			return;
		}

		if (input.size() < min || input.size() > max) {
			if (max == Integer.MAX_VALUE)
				throw new InputErrorException(INP_ERR_RANGECOUNTMIN, min, input.toString());
			throw new InputErrorException(INP_ERR_RANGECOUNT, min, max, input.toString());
		}
	}

	public static void assertCountEven(KeywordIndex kw)
	throws InputErrorException {
		if ((kw.numArgs() % 2) != 0)
			throw new InputErrorException(INP_ERR_EVENCOUNT, kw.argString());
	}

	public static void assertCountOdd(KeywordIndex kw)
	throws InputErrorException {
		if ((kw.numArgs() % 2) == 0)
			throw new InputErrorException(INP_ERR_ODDCOUNT, kw.argString());
	}

	public static <T extends Entity> void assertNotPresent(ArrayList<? super T> list, T ent)
	throws InputErrorException {
		if (list.contains(ent))
			throw new InputErrorException(INP_ERR_NOTVALIDENTRY, ent.getName());
	}

	public static void assertSumTolerance(DoubleVector vec, double sum, double tol)
	throws InputErrorException {
		// Vector sum is within tolerance of given sum, no error
		if (Math.abs(vec.sum() - sum) < tol)
			return;

		throw new InputErrorException(INP_ERR_BADSUM, sum, vec.sum());
	}

	public static void assertMonotonic(DoubleVector vec, int direction)
	throws InputErrorException {
		if (direction == 0)
			return;

		for (int i=1; i<vec.size(); i++) {
			double diff = vec.get(i) - vec.get(i-1);

			if (direction > 0 && diff < 0.0)
				throw new InputErrorException(INP_ERR_MONOTONIC, "increase", i-1, vec.get(i-1), vec.get(i));

			if (direction < 0 && diff > 0.0)
				throw new InputErrorException(INP_ERR_MONOTONIC, "decrease", i-1, vec.get(i-1), vec.get(i));
		}
	}

	/**
	 * Converts a file path entry in a configuration file to a URI.
	 * @param kw - keyword input containing the file path data
	 * @return the URI corresponding to the file path data.
	 * @throws InputErrorException
	 */
	public static URI parseURI(KeywordIndex kw)
	throws InputErrorException {
		Input.assertCount(kw, 1);

		String arg = kw.getArg(0);

		// Convert the file path to a URI
		URI uri = null;
		try {
			if (kw.context != null)
				uri = InputAgent.getFileURI(kw.context.context, arg, kw.context.jail);
			else
				uri = InputAgent.getFileURI(null, arg, null);
		}
		catch (URISyntaxException ex) {
			throw new InputErrorException("File Entity parse error: %s", ex.getMessage());
		}

		if (uri == null)
			throw new InputErrorException("Unable to parse the file path:\n%s", arg);

		if (!uri.isOpaque() && uri.getPath() == null)
			 throw new InputErrorException("Unable to parse the file path:\n%s", arg);

		return uri;
	}

	public static boolean parseBoolean(String data)
	throws InputErrorException {
		if ("TRUE".equals(data)) {
			return true;
		}

		if ("FALSE".equals(data)) {
			return false;
		}

		throw new InputErrorException(INP_ERR_BOOLEAN, data);
	}

	public static BooleanVector parseBooleanVector(KeywordIndex kw)
	throws InputErrorException {
		BooleanVector temp = new BooleanVector(kw.numArgs());

		for (int i = 0; i < kw.numArgs(); i++) {
			try {
				boolean element = Input.parseBoolean(kw.getArg(i));
				temp.add(element);
			} catch (InputErrorException e) {
				throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	public static BooleanVector parseBooleanVector(List<String> input)
	throws InputErrorException {
		BooleanVector temp = new BooleanVector(input.size());

		for (int i = 0; i < input.size(); i++) {
			try {
				boolean element = Input.parseBoolean(input.get(i));
				temp.add(element);
			} catch (InputErrorException e) {
				throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	public static ArrayList<Color4d> parseColorVector(KeywordIndex kw)
	throws InputErrorException {
		ArrayList<KeywordIndex> subArgs = kw.getSubArgs();
		ArrayList<Color4d> temp = new ArrayList<>(subArgs.size());

		for (int i = 0; i < subArgs.size(); i++) {
			try {
				Color4d element = Input.parseColour(subArgs.get(i));
				temp.add(element);
			} catch (InputErrorException e) {
				throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	public static Class<? extends Entity> parseClass( String data ) {
		Class<? extends Entity> entProto = null;
		try {
			Class<?> proto = Class.forName( data );
			entProto = proto.asSubclass(Entity.class);
		}
		catch (ClassNotFoundException e) {
			throw new InputErrorException( "Class not found " + data );
		}
		return entProto;
	}

	public static int parseInteger(String data)
	throws InputErrorException {
		return Input.parseInteger(data, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public static int parseInteger(String data, int minValue, int maxValue)
	throws InputErrorException {
		int temp;
		try {
			temp = Integer.parseInt(data);
		}
		catch (NumberFormatException e) {
			throw new InputErrorException(INP_ERR_INTEGER, data);
		}

		if (temp < minValue || temp > maxValue)
			throw new InputErrorException(INP_ERR_INTEGERRANGE, minValue, maxValue, temp);

		return temp;
	}

	public static boolean isInteger(String val) {
		try {
			Integer.parseInt(val);
			return true;
		}
		catch (NumberFormatException e) { return false; }
	}

	public static boolean isDouble(String val) {
		try {
			Double.parseDouble(val);
			return true;
		}
		catch (NumberFormatException e) { return false; }
	}

	public static IntegerVector parseIntegerVector(List<String> input, int minValue, int maxValue)
	throws InputErrorException {
		IntegerVector temp = new IntegerVector(input.size());

		for (int i = 0; i < input.size(); i++) {
			try {
				int element = Input.parseInteger(input.get(i), minValue, maxValue);
				temp.add(element);
			} catch (InputErrorException e) {
				throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	public static IntegerVector parseIntegerVector(KeywordIndex kw, int minValue, int maxValue)
	throws InputErrorException {
		IntegerVector temp = new IntegerVector(kw.numArgs());

		for (int i = 0; i <kw.numArgs(); i++) {
			try {
				int element = Input.parseInteger(kw.getArg(i), minValue, maxValue);
				temp.add(element);
			} catch (InputErrorException e) {
				throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	public static double parseTime(String data, double minValue, double maxValue)
	throws InputErrorException {
		return Input.parseTime(data, minValue, maxValue, 1.0);
	}

	/**
	 * Convert the given String to a double and apply the given conversion factor
	 */
	public static double parseTime(String data, double minValue, double maxValue, double factor )
	throws InputErrorException {
		double value = 0.0d;

		// check for hh:mm:ss or hh:mm
		if (data.indexOf(':') > -1) {
			String[] splitDouble = data.split( ":" );
			if (splitDouble.length != 2 && splitDouble.length != 3)
				throw new InputErrorException(INP_ERR_TIME, data);

			try {
				double hour = Double.valueOf(splitDouble[0]);
				double min = Double.valueOf(splitDouble[1]);
				double sec = 0.0d;

				if (splitDouble.length == 3)
					sec = Double.valueOf(splitDouble[2]);

				value = hour + (min / 60.0d) + (sec / 3600.0d);
			}
			catch (NumberFormatException e) {
				throw new InputErrorException(INP_ERR_TIME, data);
			}
		} else {
			value = Input.parseDouble(data);
		}
		value = value * factor;

		if (value < minValue || value > maxValue)
			throw new InputErrorException(INP_ERR_DOUBLERANGE, minValue, maxValue, value);

		return value;
	}

	private static final Pattern is8601date = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	private static final Pattern is8601time = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}");
	private static final Pattern is8601full = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}\\.\\d{1,6}");
	private static final Pattern isextendtime = Pattern.compile("\\d{1,}:\\d{2}:\\d{2}");
	private static final Pattern isextendfull = Pattern.compile("\\d{1,}:\\d{2}:\\d{2}.\\d{1,6}");
	private static final long usPerSec = 1000000;
	private static final long usPerMin = 60 * usPerSec;
	private static final long usPerHr  = 60 * usPerMin;
	private static final long usPerDay = 24 * usPerHr;
	public static final long usPerYr  = 365 * usPerDay;

	public static boolean isRFC8601DateTime(String input) {
		if (is8601time.matcher(input).matches()) return true;
		if (is8601full.matcher(input).matches()) return true;
		if (is8601date.matcher(input).matches()) return true;
		if (isextendtime.matcher(input).matches()) return true;
		if (isextendfull.matcher(input).matches()) return true;
		return false;
	}

	/**
	 * Parse an RFC8601 date time and return it as an offset in microseconds from
	 * 0AD. This assumes a very simple concept of a 365 day year with no leap years
	 * and no leap seconds.
	 *
	 * An RFC8601 date time looks like YYYY-MM-DD HH:MM:SS.mmm or YYYY-MM-DDTHH:MM:SS.mmm
	 *
	 * @param input
	 * @param datumYear
	 * @return
	 */
	public static long parseRFC8601DateTime(String input) {
		if (is8601time.matcher(input).matches()) {
			int YY = Integer.parseInt(input.substring(0, 4));
			int MM = Integer.parseInt(input.substring(5, 7));
			int DD = Integer.parseInt(input.substring(8, 10));
			int hh = Integer.parseInt(input.substring(11, 13));
			int mm = Integer.parseInt(input.substring(14, 16));
			int ss = Integer.parseInt(input.substring(17, 19));
			return getUS(input, YY, MM, DD, hh, mm, ss, 0);
		}

		if (is8601full.matcher(input).matches()) {
			int YY = Integer.parseInt(input.substring(0, 4));
			int MM = Integer.parseInt(input.substring(5, 7));
			int DD = Integer.parseInt(input.substring(8, 10));
			int hh = Integer.parseInt(input.substring(11, 13));
			int mm = Integer.parseInt(input.substring(14, 16));
			int ss = Integer.parseInt(input.substring(17, 19));

			// grab the us values and zero-pad to a full 6-digit number
			String usChars =  input.substring(20, input.length());
			int us = 0;
			switch (usChars.length()) {
			case 1: us =  Integer.parseInt(usChars) * 100000; break;
			case 2: us =  Integer.parseInt(usChars) *  10000; break;
			case 3: us =  Integer.parseInt(usChars) *   1000; break;
			case 4: us =  Integer.parseInt(usChars) *    100; break;
			case 5: us =  Integer.parseInt(usChars) *     10; break;
			case 6: us =  Integer.parseInt(usChars) *      1; break;
			}
			return getUS(input, YY, MM, DD, hh, mm, ss, us);
		}

		if (is8601date.matcher(input).matches()) {
			int YY = Integer.parseInt(input.substring(0, 4));
			int MM = Integer.parseInt(input.substring(5, 7));
			int DD = Integer.parseInt(input.substring(8, 10));
			return getUS(input, YY, MM, DD, 0, 0, 0, 0);
		}

		if (isextendtime.matcher(input).matches()) {
			int len = input.length();
			int hh = Integer.parseInt(input.substring(0, len - 6));
			int mm = Integer.parseInt(input.substring(len - 5, len - 3));
			int ss = Integer.parseInt(input.substring(len - 2, len));

			if (mm < 0 || mm > 59 || ss < 0 || ss > 59)
				throw new InputErrorException(INP_ERR_BADDATE, input);

			long ret = 0;
			ret += hh * usPerHr;
			ret += mm * usPerMin;
			ret += ss * usPerSec;
			return ret;
		}

		if (isextendfull.matcher(input).matches()) {
			int len = input.indexOf('.');
			int hh = Integer.parseInt(input.substring(0, len - 6));
			int mm = Integer.parseInt(input.substring(len - 5, len - 3));
			int ss = Integer.parseInt(input.substring(len - 2, len));

			if (mm < 0 || mm > 59 || ss < 0 || ss > 59)
				throw new InputErrorException(INP_ERR_BADDATE, input);

			// grab the us values and zero-pad to a full 6-digit number
			String usChars =  input.substring(len + 1, input.length());
			int us = 0;
			switch (usChars.length()) {
			case 1: us =  Integer.parseInt(usChars) * 100000; break;
			case 2: us =  Integer.parseInt(usChars) *  10000; break;
			case 3: us =  Integer.parseInt(usChars) *   1000; break;
			case 4: us =  Integer.parseInt(usChars) *    100; break;
			case 5: us =  Integer.parseInt(usChars) *     10; break;
			case 6: us =  Integer.parseInt(usChars) *      1; break;
			}
			long ret = 0;
			ret += hh * usPerHr;
			ret += mm * usPerMin;
			ret += ss * usPerSec;
			ret += us;
			return ret;
		}

		throw new InputErrorException(INP_ERR_BADDATE, input);
	}

	private static final int[] daysInMonth;
	private static final int[] firstDayOfMonth;

	static {
		daysInMonth = new int[12];
		daysInMonth[0] = 31;
		daysInMonth[1] = 28;
		daysInMonth[2] = 31;
		daysInMonth[3] = 30;
		daysInMonth[4] = 31;
		daysInMonth[5] = 30;
		daysInMonth[6] = 31;
		daysInMonth[7] = 31;
		daysInMonth[8] = 30;
		daysInMonth[9] = 31;
		daysInMonth[10] = 30;
		daysInMonth[11] = 31;

		firstDayOfMonth = new int[12];
		firstDayOfMonth[0] = 1;
		for (int i = 1; i < firstDayOfMonth.length; i++) {
			firstDayOfMonth[i] = firstDayOfMonth[i - 1] + daysInMonth[i - 1];
		}
	}

	private static final long getUS(String input, int YY, int MM, int DD, int hh, int mm, int ss, int us) {
		// Validate ranges
		if (MM <= 0 || MM > 12)
			throw new InputErrorException(INP_ERR_BADDATE, input);

		if (DD <= 0 || DD > daysInMonth[MM - 1])
			throw new InputErrorException(INP_ERR_BADDATE, input);

		if (hh < 0 || hh > 23)
			throw new InputErrorException(INP_ERR_BADDATE, input);

		if (mm < 0 || mm > 59 || ss < 0 || ss > 59)
			throw new InputErrorException(INP_ERR_BADDATE, input);

		long ret = 0;
		ret += YY * usPerYr;
		ret += (firstDayOfMonth[MM - 1] - 1) * usPerDay;
		ret += (DD - 1) * usPerDay;
		ret += hh * usPerHr;
		ret += mm * usPerMin;
		ret += ss * usPerSec;
		ret += us;

		return ret;
	}

	public static double parseDouble(String data)
	throws InputErrorException {
		return Input.parseDouble(data, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public static double parseDouble(String data, double minValue, double maxValue)
	throws InputErrorException {
		return Input.parseDouble(data, minValue, maxValue, 1.0);
	}

	/**
	 * Convert the given String to a double and apply the given conversion factor
	 */
	public static double parseDouble(String data, double minValue, double maxValue, double factor)
	throws InputErrorException {
		double temp;
		try {
			temp = Double.parseDouble(data) * factor;
		}
		catch (NumberFormatException e) {
			throw new InputErrorException(INP_ERR_DOUBLE, data);
		}

		if (temp < minValue || temp > maxValue)
			throw new InputErrorException(INP_ERR_DOUBLERANGE, minValue, maxValue, temp);

		return temp;
	}

	/**
	 * Convert the given input to a DoubleVector and apply the given conversion factor
	 */
	public static DoubleVector parseDoubleVector(List<String> input, double minValue, double maxValue, double factor)
	throws InputErrorException {
		DoubleVector temp = new DoubleVector(input.size());

		for (int i = 0; i < input.size(); i++) {
			try {
				double element = Input.parseDouble(input.get(i), minValue, maxValue, factor);
				temp.add(element);
			} catch (InputErrorException e) {
				throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	/**
	 * Convert the given input to a DoubleVector and apply the given conversion factor
	 */
	public static DoubleVector parseDoubles(KeywordIndex kw, double minValue, double maxValue, Class<? extends Unit> unitType)
	throws InputErrorException {

		if (unitType == UserSpecifiedUnit.class)
			throw new InputErrorException(INP_ERR_UNITUNSPECIFIED);

		double factor = 1.0d;
		int numArgs = kw.numArgs();
		int numDoubles = numArgs;
		boolean includeIndex = true;

		// Parse the unit portion of the input
		String unitName = Parser.removeEnclosure("[", kw.getArg(numArgs-1), "]");
		Unit unit = Input.tryParseUnit(unitName, unitType);


		// A unit is mandatory except for dimensionless values and time values in RFC8601 date/time format
		if (unit == null && unitType != DimensionlessUnit.class && unitType != TimeUnit.class)
			throw new InputErrorException(INP_ERR_NOUNITFOUND, kw.getArg(numArgs-1), unitType.getSimpleName());

		if (unit != null) {
			factor = unit.getConversionFactorToSI();
			numDoubles = numArgs - 1;
		}

		// Parse the numeric portion of the input
		DoubleVector temp = new DoubleVector(numDoubles);
		for (int i = 0; i < numDoubles; i++) {
			try {
				// Time input
				if (unitType == TimeUnit.class) {

					// RFC8601 date/time format
					if (Input.isRFC8601DateTime(kw.getArg(i))) {
						double element = Input.parseRFC8601DateTime(kw.getArg(i))/1e6;
						if (element < minValue || element > maxValue)
							throw new InputErrorException(INP_ERR_DOUBLERANGE, minValue, maxValue, temp);
						temp.add(element);
					}
					// Normal format
					else {
						if (unit == null) {
							includeIndex = false;
							throw new InputErrorException(INP_ERR_NOUNITFOUND, kw.getArg(numArgs-1), unitType.getSimpleName());
						}
						double element = Input.parseDouble(kw.getArg(i), minValue, maxValue, factor);
						temp.add(element);
					}
				}
				// Non-time input
				else {
					double element = Input.parseDouble(kw.getArg(i), minValue, maxValue, factor);
					temp.add(element);
				}
			} catch (InputErrorException e) {
				if (includeIndex && numDoubles > 1)
					throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
				else
					throw e;
			}
		}
		return temp;
	}

	/**
	 * Convert the given input to a DoubleVector and apply the given conversion factor
	 */
	public static DoubleVector parseDoubles(List<String> input, double minValue, double maxValue, Class<? extends Unit> unitType)
	throws InputErrorException {

		if (unitType == UserSpecifiedUnit.class)
			throw new InputErrorException(INP_ERR_UNITUNSPECIFIED);

		double factor = 1.0d;
		int numDoubles = input.size();

		// Parse the unit portion of the input
		String unitName = Parser.removeEnclosure("[", input.get(numDoubles-1), "]");
		Unit unit = Input.tryParseUnit(unitName, unitType);

		// A unit is mandatory except for dimensionless values and time values in RFC8601 date/time format
		if (unit == null && unitType != DimensionlessUnit.class && unitType != TimeUnit.class)
			throw new InputErrorException(INP_ERR_NOUNITFOUND, input.get(numDoubles-1), unitType.getSimpleName());

		if (unit != null) {
			factor = unit.getConversionFactorToSI();
			numDoubles = numDoubles - 1;
		}

		// Parse the numeric portion of the input
		DoubleVector temp = new DoubleVector(numDoubles);
		for (int i = 0; i < numDoubles; i++) {
			try {
				// Time input
				if (unitType == TimeUnit.class) {

					// RFC8601 date/time format
					if (Input.isRFC8601DateTime(input.get(i))) {
						double element = Input.parseRFC8601DateTime(input.get(i))/1e6;
						if (element < minValue || element > maxValue)
							throw new InputErrorException(INP_ERR_DOUBLERANGE, minValue, maxValue, temp);
						temp.add(element);
					}
					// Normal format
					else {
						if (unit == null)
							throw new InputErrorException(INP_ERR_NOUNITFOUND, input.get(numDoubles-1), unitType.getSimpleName());
						double element = Input.parseDouble(input.get(i), minValue, maxValue, factor);
						temp.add(element);
					}
				}
				// Non-time input
				else {
					double element = Input.parseDouble(input.get(i), minValue, maxValue, factor);
					temp.add(element);
				}
			} catch (InputErrorException e) {
				if (numDoubles == 1)
					throw e;
				else
					throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	public static String parseString(String input, ArrayList<String> validList)
	throws InputErrorException {
		return parseString(input, validList, false);
	}

	public static String parseString(String input, ArrayList<String> validList, boolean caseSensitive)
	throws InputErrorException {
		for (String valid : validList) {
			if (caseSensitive && valid.equals(input))
				return valid;

			if (!caseSensitive && valid.equalsIgnoreCase(input))
				return valid;
		}

		throw new InputErrorException(INP_ERR_BADCHOICE, validList.toString(), input);
	}

	public static ArrayList<String> parseStrings(KeywordIndex kw, ArrayList<String> validList, boolean caseSensitive)
	throws InputErrorException {
		ArrayList<String> temp = new ArrayList<>(kw.numArgs());

		for (int i = 0; i < kw.numArgs(); i++) {
			try {
				String element = Input.parseString(kw.getArg(i), validList);
				temp.add(element);
			} catch (InputErrorException e) {
				throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	public static <T extends Enum<T>> T parseEnum(Class<T> aClass, String input) {
		try {
			return Enum.valueOf(aClass, input);
		} catch (IllegalArgumentException e) {
			throw new InputErrorException(INP_ERR_BADCHOICE, Arrays.toString(aClass.getEnumConstants()), input);
		} catch (NullPointerException e) {
			throw new InputErrorException(INP_ERR_BADCHOICE, Arrays.toString(aClass.getEnumConstants()), input);
		}
	}

	public static <T extends Enum<T>> ArrayList<T> parseEnumList(Class<T> aClass, KeywordIndex kw) {
		ArrayList<T> ret = new ArrayList<>(kw.numArgs());
		for (int i=0; i<kw.numArgs(); i++) {
			try {
				ret.add(Enum.valueOf(aClass, kw.getArg(i)));
			} catch (IllegalArgumentException e) {
				throw new InputErrorException(INP_ERR_BADCHOICE, Arrays.toString(aClass.getEnumConstants()), kw.getArg(i));
			} catch (NullPointerException e) {
				throw new InputErrorException(INP_ERR_BADCHOICE, Arrays.toString(aClass.getEnumConstants()), kw.getArg(i));
			}
		}
		return ret;
	}

	public static Class<? extends Entity> parseEntityType(String input)
	throws InputErrorException {
		ObjectType type = Input.tryParseEntity( input, ObjectType.class );
		if (type == null)
			throw new InputErrorException("Entity type not found: %s", input);

		Class<? extends Entity> klass = type.getJavaClass();
		if (klass == null)
			throw new InputErrorException("ObjectType %s does not have a java class set", input);

		return klass;
	}

	public static void assertUnitsMatch(Class<? extends Unit> u1, Class<? extends Unit> u2)
	throws InputErrorException {
		if (u1 != u2)
			throw new InputErrorException(INP_ERR_UNITS);
	}

	public static <T extends Entity> Class<? extends T> checkCast(Class<? extends Entity> klass, Class<T> parent) {
		try {
			return klass.asSubclass(parent);
		}
		catch (ClassCastException e) {
			throw new InputErrorException(INP_ERR_NOTSUBCLASS, parent.getName(), klass.getName());
		}
	}

	public static <T> T castImplements(Entity ent, Class<T> klass)
	throws InputErrorException {
		try {
			return klass.cast(ent);
		}
		catch (ClassCastException e) {
			throw new InputErrorException(INP_ERR_INTERFACE, klass.getName(), ent.getName());
		}
	}

	private static <T extends Entity> T castEntity(Entity ent, Class<T> aClass)
	throws InputErrorException {
		try {
			return aClass.cast(ent);
		}
		catch (ClassCastException e) {
			return null;
		}
	}

	public static <T extends Entity> T parseEntity(String choice, Class<T> aClass)
	throws InputErrorException {
		Entity ent = Entity.getNamedEntity(choice);
		if (ent == null) {
			throw new InputErrorException(INP_ERR_ENTNAME, choice);
		}
		T t = Input.castEntity(ent, aClass);
		if (t == null) {
			throw new InputErrorException(INP_ERR_ENTCLASS, aClass.getSimpleName(), choice, ent.getClass().getSimpleName());
		}
		return t;
	}

	public static <T extends Entity> T tryParseEntity(String choice, Class<T> aClass) {
		return Input.castEntity(Entity.getNamedEntity(choice), aClass);
	}

	public static <T extends Unit> T tryParseUnit(String choice, Class<T> aClass) {
		return Input.castEntity(Entity.getNamedEntity(choice), aClass);
	}

	public static Unit parseUnit(String str)
	throws InputErrorException {
		Unit u = Input.tryParseUnit(str, Unit.class);
		if (u == null)
			throw new InputErrorException("Could not find a unit named: %s", str);

		return u;
	}

	public static <T extends Entity> ArrayList<T> parseEntityList(KeywordIndex kw, Class<T> aClass, boolean unique)
	throws InputErrorException {
		ArrayList<T> temp = new ArrayList<>(kw.numArgs());

		for (int i = 0; i < kw.numArgs(); i++) {
			Entity ent = Entity.getNamedEntity(kw.getArg(i));
			if (ent == null) {
				throw new InputErrorException(INP_ERR_ENTNAME, kw.getArg(i));
			}

			// If we found a group, expand the list of Entities
			if (ent instanceof Group && aClass != Group.class) {
				ArrayList<Entity> gList = ((Group)ent).getList();
				for (int j = 0; j < gList.size(); j++) {
					T t = Input.castEntity(gList.get(j), aClass);
					if (t == null) {
						throw new InputErrorException(INP_ERR_ENTCLASS, aClass.getSimpleName(), gList.get(j), gList.get(j).getClass().getSimpleName());
					}
					temp.add(t);
				}
			} else {
				T t = Input.castEntity(ent, aClass);
				if (t == null) {
					throw new InputErrorException(INP_ERR_ENTCLASS, aClass.getSimpleName(), kw.getArg(i), ent.getClass().getSimpleName());
				}
				temp.add(t);
			}
		}

		if (unique)
			Input.assertUnique(temp);

		return temp;
	}


	public static <T extends Entity> ArrayList<T> parseEntityList(List<String> input, Class<T> aClass, boolean unique)
	throws InputErrorException {
		ArrayList<T> temp = new ArrayList<>(input.size());

		for (int i = 0; i < input.size(); i++) {
			Entity ent = Entity.getNamedEntity(input.get(i));
			if (ent == null) {
				throw new InputErrorException(INP_ERR_ENTNAME, input.get(i));
			}

			// If we found a group, expand the list of Entities
			if (ent instanceof Group && aClass != Group.class) {
				ArrayList<Entity> gList = ((Group)ent).getList();
				for (int j = 0; j < gList.size(); j++) {
					T t = Input.castEntity(gList.get(j), aClass);
					if (t == null) {
						throw new InputErrorException(INP_ERR_ENTCLASS, aClass.getSimpleName(), gList.get(j), gList.get(j).getClass().getSimpleName());
					}
					temp.add(t);
				}
			} else {
				T t = Input.castEntity(ent, aClass);
				if (t == null) {
					throw new InputErrorException(INP_ERR_ENTCLASS, aClass.getSimpleName(), input.get(i), ent.getClass().getSimpleName());
				}
				temp.add(t);
			}
		}

		if (unique)
			Input.assertUnique(temp);

		return temp;
	}


	public static <T extends Entity> ArrayList<ArrayList<T>> parseListOfEntityLists(KeywordIndex kw, Class<T> aClass, boolean unique)
	throws InputErrorException {
		ArrayList<KeywordIndex> subArgs = kw.getSubArgs();
		ArrayList<ArrayList<T>> temp = new ArrayList<>(subArgs.size());

		for (int i = 0; i < subArgs.size(); i++) {
			try {
				ArrayList<T> element = Input.parseEntityList(subArgs.get(i), aClass, unique);
				temp.add(element);
			} catch (InputErrorException e) {
				throw new InputErrorException(INP_ERR_ELEMENT, i+1, e.getMessage());
			}
		}
		return temp;
	}

	public static <T> T parseInterfaceEntity(String choice, Class<T> aClass) {

		Entity ent = Entity.getNamedEntity(choice);
		if (ent == null) {
			throw new InputErrorException(INP_ERR_ENTNAME, choice);
		}

		T temp = Input.castImplements(ent, aClass);
		if (temp == null) {
			throw new InputErrorException(INP_ERR_ENTCLASS, aClass.getSimpleName(), choice, ent.getClass().getSimpleName());
		}

		return temp;
	}

	public static <T> ArrayList<T> parseInterfaceEntityList(KeywordIndex kw, Class<T> aClass, boolean unique)
	throws InputErrorException {
		ArrayList<T> temp = new ArrayList<>(kw.numArgs());

		for (int i = 0; i < kw.numArgs(); i++) {
			Entity ent = Entity.getNamedEntity(kw.getArg(i));
			if (ent == null) {
				throw new InputErrorException(INP_ERR_ENTNAME, kw.getArg(i));
			}

			// If we found a group, expand the list of Entities
			if (ent instanceof Group) {
				ArrayList<Entity> gList = ((Group)ent).getList();
				for (int j = 0; j < gList.size(); j++) {
					T t = Input.castImplements(gList.get(j), aClass);
					if (t == null) {
						throw new InputErrorException(INP_ERR_ENTCLASS, aClass.getSimpleName(), gList.get(j), gList.get(j).getClass().getSimpleName());
					}
					temp.add(t);
				}
			} else {
				T t = Input.castImplements(ent, aClass);
				if (t == null) {
					throw new InputErrorException(INP_ERR_ENTCLASS, aClass.getSimpleName(), kw.getArg(i), ent.getClass().getSimpleName());
				}
				temp.add(t);
			}
		}

		if (unique)
			Input.assertUniqueInterface(temp);

		return temp;
	}

	public static Color4d parseColour(KeywordIndex kw) {

		Input.assertCountRange(kw, 1, 4);

		// Color names
		if (kw.numArgs() <= 2) {
			Color4d colAtt = ColourInput.getColorWithName(kw.getArg(0).toLowerCase());
			if( colAtt == null )
				throw new InputErrorException( "Color " + kw.getArg( 0 ) + " not found" );

			if (kw.numArgs() == 1)
				return colAtt;

			double a = Input.parseDouble(kw.getArg(1), 0.0d, 255.0d);
			if (a > 1.0f)
				a /= 255.0d;
			return new Color4d(colAtt.r, colAtt.b, colAtt.g, a);
		}

		// RGB
		else {
			DoubleVector dbuf = Input.parseDoubles(kw, 0.0d, 255.0d, DimensionlessUnit.class);
			double r = dbuf.get(0);
			double g = dbuf.get(1);
			double b = dbuf.get(2);
			double a = 1.0d;
			if (dbuf.size() == 4)
				a = dbuf.get(3);

			if (r > 1.0f || g > 1.0f || b > 1.0f) {
				r /= 255.0d;
				g /= 255.0d;
				b /= 255.0d;
			}
			if (a > 1.0f) {
				a /= 255.0d;
			}

			return new Color4d(r, g, b, a);
		}
	}

	public static <T extends Entity> EntityProvider<T> parseEntityProvider(KeywordIndex kw, Entity thisEnt, Class<T> entClass) {
		assertCount(kw, 1);

		// Parse the input as an Entity
		try {
			T ent = parseEntity(kw.getArg(0), entClass);
			return new EntityProvConstant<>(ent);
		}
		catch (InputErrorException e) {}

		// Parse the input as an Expression
		try {
			return new EntityProvExpression<>(kw.getArg(0), thisEnt, entClass);
		}
		catch (ExpError e) {
			throw new InputErrorException(e);
		}
	}

	public static StringProvider parseStringProvider(KeywordIndex kw, Entity thisEnt, Class<? extends Unit> unitType) {

		// Parse the input as a StringProvExpression
		if (kw.numArgs() == 1) {
			try {
				return new StringProvExpression(kw.getArg(0), thisEnt, unitType);
			} catch (ExpError e) {}
		}

		// Parse the input as a SampleProvider object
		SampleProvider samp = Input.parseSampleExp(kw, thisEnt, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, unitType);
		return new StringProvSample(samp);
	}

	public static SampleProvider parseSampleExp(KeywordIndex kw,
			Entity thisEnt, double minValue, double maxValue, Class<? extends Unit> unitType) {

		if (unitType == UserSpecifiedUnit.class)
			throw new InputErrorException(INP_ERR_UNITUNSPECIFIED);

		// More than two inputs is an error
		if (kw.numArgs() > 2)
			throw new InputErrorException(INP_ERR_RANGECOUNT, 1, 2, kw.argString());

		// If there are exactly two inputs, then it must be a number and its unit
		if (kw.numArgs() == 2) {
			if (unitType == DimensionlessUnit.class)
				throw new InputErrorException(INP_ERR_COUNT, 1, kw.argString());
			DoubleVector tmp = Input.parseDoubles(kw, minValue, maxValue, unitType);
			return new SampleConstant(unitType, tmp.get(0));
		}

		// If there is only one input, it could be a SampleProvider, a dimensionless constant, or an expression

		// 1) Try parsing a SampleProvider object
		SampleProvider s = null;
		try {
			Entity ent = Input.parseEntity(kw.getArg(0), Entity.class);
			s = Input.castImplements(ent, SampleProvider.class);
		}
		catch (InputErrorException e) {}

		if (s != null) {
			Input.assertUnitsMatch(unitType, s.getUnitType());
			return s;
		}

		// 2) Try parsing a constant value
		DoubleVector tmp = null;
		try {
			tmp = Input.parseDoubles(kw, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, DimensionlessUnit.class);
		}
		catch (InputErrorException e) {}

		if (tmp != null) {
			if (unitType != DimensionlessUnit.class)
				throw new InputErrorException(INP_ERR_UNITNOTFOUND, unitType.getSimpleName());
			if (tmp.get(0) < minValue || tmp.get(0) > maxValue)
				throw new InputErrorException(INP_ERR_DOUBLERANGE, minValue, maxValue, tmp.get(0));
			return new SampleConstant(unitType, tmp.get(0));
		}

		// 3) Try parsing an expression
		try {
			String expString = kw.getArg(0);
			return new SampleExpression(expString, thisEnt, unitType);
		}
		catch (ExpError e) {
			throw new InputErrorException(e);
		}
	}

	private static void assertUnique(ArrayList<? extends Entity> list) {
		for (int i = 0; i < list.size(); i++) {
			Entity ent = list.get(i);
			for (int j = i + 1; j < list.size(); j++) {
				if (ent == list.get(j)) {
					throw new InputErrorException(INP_ERR_NOTUNIQUE, ent.getName());
				}
			}
		}
	}

	private static void assertUniqueInterface(ArrayList<?> list) {
		for (int i = 0; i < list.size(); i++) {
			Entity ent = (Entity)list.get(i);
			for (int j = i + 1; j < list.size(); j++) {
				if (ent == list.get(j)) {
					throw new InputErrorException(INP_ERR_NOTUNIQUE, ent.getName());
				}
			}
		}
	}

	public static void validateIndexedLists(ListInput<?> keys, ListInput<?> vals)
	throws InputErrorException {
		// If no values set, no validation to be done
		if (vals.getValue() == null)
			return;

		// values are set but indexed list has not
		if (keys.getValue() == null)
			throw new InputErrorException(INP_VAL_LISTSET, keys.getKeyword(), vals.getKeyword());

		// Both are set, but of differing size
		if (keys.getListSize() != vals.getListSize())
			throw new InputErrorException(INP_VAL_LISTSIZE, keys.getKeyword(), vals.getKeyword());
	}

	public static void validateInputSize(ListInput<?> list1, ListInput<?> list2)
	throws InputErrorException {
		// One list is set but not the other
		if (list1.getValue() != null && list2.getValue() == null)
			throw new InputErrorException(INP_VAL_LISTSIZE, list1.getKeyword(), list2.getKeyword());

		if (list1.getValue() == null && list2.getValue() != null)
			throw new InputErrorException(INP_VAL_LISTSIZE, list1.getKeyword(), list2.getKeyword());

		// Both are set, but of differing size
		if (list1.getListSize() != list2.getListSize())
			throw new InputErrorException(INP_VAL_LISTSIZE, list1.getKeyword(), list2.getKeyword() );
	}

	public static void validateIndexedLists(ArrayList<?> keys, DoubleVector values, String keyName, String valueName)
	throws InputErrorException {
		// If no values set, no validation to be done
		if (values == null)
			return;

		// values are set but indexed list has not
		if (keys == null)
			throw new InputErrorException(INP_VAL_LISTSET, valueName, keyName);

		// Both are set, but of differing size
		if (keys.size() != values.size())
			throw new InputErrorException(INP_VAL_LISTSIZE, keyName, valueName);
	}

	/**
	 * Returns a list of valid options if the input has limited number of
	 * choices (e.g TRUE or FALSE for BooleanInput).
	 * <p>
	 * This method must be overridden for an input to be shown with a drop-down
	 * menu in the Input Editor.
	 */
	public ArrayList<String> getValidOptions() {
		if (defValue != null && defValue.getClass() == Boolean.class) {
			ArrayList<String> validOptions = new ArrayList<>();
			validOptions.add("TRUE");
			validOptions.add("FALSE");
			return validOptions;
		}
		else
			return null;
	}

	public String getDefaultStringForKeyInputs(Class<? extends Unit> unitType) {

		if (defValue == null)
			return "";

		if (defValue.getClass() == Boolean.class) {
			if((Boolean)defValue)
				return "TRUE";

			return "FALSE";
		}

		StringBuilder tmp = new StringBuilder();
		if (defValue.getClass() == Double.class ||
		   defValue.getClass() == Integer.class ||
		   Entity.class.isAssignableFrom(Double.class)) {
			if (defValue.equals(Integer.MAX_VALUE) || defValue.equals(Double.POSITIVE_INFINITY))
				return POSITIVE_INFINITY;

			if (defValue.equals(Integer.MIN_VALUE) || defValue.equals(Double.NEGATIVE_INFINITY))
				return NEGATIVE_INFINITY;

			tmp.append(defValue);
		} else if (defValue.getClass() == SampleConstant.class ||
					defValue.getClass() == TimeSeriesConstantDouble.class ) {
			return defValue.toString();
		} else if (defValue.getClass() == DoubleVector.class) {
			DoubleVector def = (DoubleVector)defValue;
			if (def.size() == 0)
				return "";

			tmp.append(def.get(0));
			for (int i = 1; i < def.size(); i++) {
				tmp.append(SEPARATOR);
				tmp.append(def.get(i));
			}
		} else if (defValue.getClass() == IntegerVector.class) {
			IntegerVector def = (IntegerVector)defValue;
			if (def.size() == 0)
				return "";

			tmp.append(def.get(0));
			for (int i = 1; i < def.size(); i++) {
				tmp.append(SEPARATOR);
				tmp.append(def.get(i));
			}
		} else if ( Entity.class.isAssignableFrom( defValue.getClass() ) ) {
			tmp.append(((Entity)defValue).getName());
		}
		else {
			return "?????";
		}

		tmp.append(SEPARATOR);
		tmp.append(Unit.getSIUnit(unitType));
		return tmp.toString();
	}
}
