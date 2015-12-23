package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMapping;

/**
 * 
 * @author
 * 
 */
public class SoapTagContext {

	private StringWriter sw;
	private PrintWriter out;

	private Map attributes;

	private LinkedList removeFirstPrependStack;
	private LinkedList iterateContextStack;

	private List parameterMappings = new ArrayList();

	public SoapTagContext() {
		sw = new StringWriter();
		out = new PrintWriter(sw);
		attributes = new HashMap();
		removeFirstPrependStack = new LinkedList();
		iterateContextStack = new LinkedList();
	}

	public PrintWriter getWriter() {
		return out;
	}

	public String getBodyText() {
		out.flush();
		return sw.getBuffer().toString();
	}

	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	public Object getAttribute(Object key) {
		return attributes.get(key);
	}

	public void addParameterMapping(ParameterMapping mapping) {
		parameterMappings.add(mapping);
	}

	public List getParameterMappings() {
		return parameterMappings;
	}

	public boolean isEmptyRemoveFirtPrepend() {
		return removeFirstPrependStack.size() <= 0;
	}

	/**
	 * examine the value of the top RemoveFirstPrependMarker object on the stack.
	 * 
	 * @return was the first prepend removed
	 */
	public boolean peekRemoveFirstPrependMarker(SoapTag sqlTag) {

		RemoveFirstPrependMarker removeFirstPrepend = (RemoveFirstPrependMarker) removeFirstPrependStack.get(1);

		return removeFirstPrepend.isRemoveFirstPrepend();
	}

	/**
	 * pop the first RemoveFirstPrependMarker once the recursion is on it's way out
	 * of the recursion loop and return it's internal value.
	 * 
	 * @param tag
	 */
	public void popRemoveFirstPrependMarker(SoapTag tag) {

		RemoveFirstPrependMarker removeFirstPrepend = (RemoveFirstPrependMarker) removeFirstPrependStack.getFirst();

		if (tag == removeFirstPrepend.getSqlTag()) {
			removeFirstPrependStack.removeFirst();
		}
	}

	/**
	 * push a new RemoveFirstPrependMarker object with the specified internal state
	 * 
	 * @param tag
	 */
	public void pushRemoveFirstPrependMarker(SoapTag tag) {

		if (tag.getHandler() instanceof DynamicTagHandler) {
			// this was added to retain default behavior
			if (tag.isPrependAvailable()) {
				removeFirstPrependStack.addFirst(new RemoveFirstPrependMarker(tag, true));
			} else {
				removeFirstPrependStack.addFirst(new RemoveFirstPrependMarker(tag, false));
			}
		} else if ("true".equals(tag.getRemoveFirstPrepend()) || "iterate".equals(tag.getRemoveFirstPrepend())) {
			// you must be specific about the removal otherwise it
			// will function as ibatis has always functioned and add
			// the prepend
			removeFirstPrependStack.addFirst(new RemoveFirstPrependMarker(tag, true));
		} else if (!tag.isPrependAvailable() && !"true".equals(tag.getRemoveFirstPrepend())
			&& !"iterate".equals(tag.getRemoveFirstPrepend()) && tag.getParent() != null) {
			// if no prepend or removeFirstPrepend is specified
			// we need to look to the parent tag for default values
			if ("true".equals(tag.getParent().getRemoveFirstPrepend())
				|| "iterate".equals(tag.getParent().getRemoveFirstPrepend())) {
				removeFirstPrependStack.addFirst(new RemoveFirstPrependMarker(tag, true));
			}
		} else {
			removeFirstPrependStack.addFirst(new RemoveFirstPrependMarker(tag, false));
		}

	}

	/**
	 * set a new internal state for top RemoveFirstPrependMarker object
	 * 
	 */
	public void disableRemoveFirstPrependMarker() {
		((RemoveFirstPrependMarker) removeFirstPrependStack.get(1)).setRemoveFirstPrepend(false);
	}

	public void reEnableRemoveFirstPrependMarker() {
		((RemoveFirstPrependMarker) removeFirstPrependStack.get(0)).setRemoveFirstPrepend(true);
	}

	/**
	 * iterate context is stored here for nested dynamic tags in
	 * the body of the iterate tag
	 * 
	 * @param iterateContext
	 */
	public void pushIterateContext(IterateContext iterateContext) {
		iterateContextStack.addFirst(iterateContext);
	}

	/**
	 * iterate context is removed here from the stack when iterate tag is finished being
	 * processed
	 * 
	 * @return the top element of the context stack
	 */
	public IterateContext popIterateContext() {
		IterateContext retVal = null;
		if (!iterateContextStack.isEmpty()) {
			retVal = (IterateContext) iterateContextStack.removeFirst();
		}
		return retVal;
	}

	/**
	 * iterate context is removed here from the stack when iterate tag is finished being
	 * processed
	 * 
	 * @return the top element on the context stack
	 */
	public IterateContext peekIterateContext() {
		IterateContext retVal = null;
		if (!iterateContextStack.isEmpty()) {
			retVal = (IterateContext) iterateContextStack.getFirst();
		}
		return retVal;
	}

}

/**
 * 
 * This inner class i used strictly to house whether the
 * removeFirstPrepend has been used in a particular nested
 * situation.
 * 
 * @author Brandon Goodin
 */
class RemoveFirstPrependMarker {

	private boolean removeFirstPrepend;
	private SoapTag tag;

	/**
   * 
   */
	public RemoveFirstPrependMarker(SoapTag tag, boolean removeFirstPrepend) {
		this.removeFirstPrepend = removeFirstPrepend;
		this.tag = tag;
	}

	/**
	 * @return Returns the removeFirstPrepend.
	 */
	public boolean isRemoveFirstPrepend() {
		return removeFirstPrepend;
	}

	/**
	 * @param removeFirstPrepend
	 *            The removeFirstPrepend to set.
	 */
	public void setRemoveFirstPrepend(boolean removeFirstPrepend) {
		this.removeFirstPrepend = removeFirstPrepend;
	}

	/**
	 * @return Returns the sqlTag.
	 */
	public SoapTag getSqlTag() {
		return tag;
	}

}
