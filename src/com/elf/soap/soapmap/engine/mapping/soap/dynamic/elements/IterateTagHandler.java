package com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements;

import com.elf.soap.common.beans.Probe;
import com.elf.soap.common.beans.ProbeFactory;

/**
 * 
 * @author
 * 
 */
public class IterateTagHandler extends BaseTagHandler {

	private static final Probe PROBE = ProbeFactory.getProbe();

	public int doStartFragment(SoapTagContext ctx, SoapTag tag, Object parameterObject) {
		IterateContext iterate = (IterateContext) ctx.getAttribute(tag);
		if (iterate == null) {
			IterateContext parentIterate = ctx.peekIterateContext();

			ctx.pushRemoveFirstPrependMarker(tag);

			Object collection;
			String prop = tag.getPropertyAttr();
			if (!"".equals(prop)) {
				if (null != parentIterate && parentIterate.isAllowNext()) {
					parentIterate.next();
					parentIterate.setAllowNext(false);
					if (!parentIterate.hasNext()) {
						parentIterate.setFinal(true);
					}
				}

				if (parentIterate != null) {
					prop = parentIterate.addIndexToTagProperty(prop);
				}

				collection = PROBE.getObject(parameterObject, prop);
			} else {
				collection = parameterObject;
			}
			iterate = new IterateContext(collection, tag, parentIterate);

			iterate.setProperty(null == prop ? "" : prop);

			ctx.setAttribute(tag, iterate);
			ctx.pushIterateContext(iterate);
		} else if ("iterate".equals(tag.getRemoveFirstPrepend())) {
			ctx.reEnableRemoveFirstPrependMarker();
		}

		if (iterate != null && iterate.hasNext()) {
			return INCLUDE_BODY;
		} else {
			return SKIP_BODY;
		}
	}

	public int doEndFragment(SoapTagContext ctx, SoapTag tag, Object parameterObject, StringBuffer bodyContent) {
		IterateContext iterate = (IterateContext) ctx.getAttribute(tag);

		if (iterate.hasNext() || iterate.isFinal()) {

			if (iterate.isAllowNext()) {
				iterate.next();
			}

			if (bodyContent.toString().trim().length() > 0) {
				// the sub element produced a result. If it is the first one
				// to produce a result, then we need to add the open
				// text. If it is not the first to produce a result then
				// we need to add the conjunction text
				if (iterate.someSubElementsHaveContent()) {
					if (tag.isConjunctionAvailable()) {
						bodyContent.insert(0, tag.getConjunctionAttr());
					}
				} else {
					// we need to specify that this is the first content
					// producing element so that the doPrepend method will
					// add the prepend
					iterate.setPrependEnabled(true);

					if (tag.isOpenAvailable()) {
						bodyContent.insert(0, tag.getOpenAttr());
					}
				}
				iterate.setSomeSubElementsHaveContent(true);
			}

			if (iterate.isLast() && iterate.someSubElementsHaveContent()) {
				if (tag.isCloseAvailable()) {
					bodyContent.append(tag.getCloseAttr());
				}
			}

			iterate.setAllowNext(true);
			if (iterate.isFinal()) {
				return super.doEndFragment(ctx, tag, parameterObject, bodyContent);
			} else {
				return REPEAT_BODY;
			}

		} else {
			return super.doEndFragment(ctx, tag, parameterObject, bodyContent);
		}
	}

	public void doPrepend(SoapTagContext ctx, SoapTag tag, Object parameterObject, StringBuffer bodyContent) {
		IterateContext iterate = (IterateContext) ctx.getAttribute(tag);
		if (iterate.isPrependEnabled()) {
			super.doPrepend(ctx, tag, parameterObject, bodyContent);
			iterate.setPrependEnabled(false); // only do the prepend one time
		}
	}

	public boolean isPostParseRequired() {
		return true;
	}

}
