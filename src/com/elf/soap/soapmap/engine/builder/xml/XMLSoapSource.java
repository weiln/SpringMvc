package com.elf.soap.soapmap.engine.builder.xml;

import com.elf.soap.common.xml.NodeletUtils;
import com.elf.soap.soapmap.engine.config.SoapSource;
import com.elf.soap.soapmap.engine.mapping.parameter.InlineParameterMapParser;
import com.elf.soap.soapmap.engine.mapping.soap.Soap;
import com.elf.soap.soapmap.engine.mapping.soap.SoapText;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.DynamicSoap;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.DynamicParent;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.IterateTagHandler;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.SoapTag;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.SoapTagHandler;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.SoapTagHandlerFactory;
import com.elf.soap.soapmap.engine.mapping.soap.raw.RawSoap;

import java.util.Properties;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author
 * 
 */
public class XMLSoapSource implements SoapSource {

	private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();

	private XmlParserState state;
	private Node parentNode;

	public XMLSoapSource(XmlParserState config, Node parentNode) {
		this.state = config;
		this.parentNode = parentNode;
	}

	public Soap getSoap() {
		state.getConfig().getErrorContext().setActivity("processing an SOAP statement");

		boolean isDynamic = false;
		StringBuffer sqlBuffer = new StringBuffer();
		DynamicSoap dynamic = new DynamicSoap(state.getConfig().getClient().getDelegate());
		isDynamic = parseDynamicTags(parentNode, dynamic, sqlBuffer, isDynamic, false);
		String sqlStatement = sqlBuffer.toString();
		if (isDynamic) {
			return dynamic;
		} else {
			return new RawSoap(sqlStatement);
		}
	}

	private boolean parseDynamicTags(Node node, DynamicParent dynamic, StringBuffer sqlBuffer, boolean isDynamic,
		boolean postParseRequired) {
		state.getConfig().getErrorContext().setActivity("parsing dynamic SOAP tags");

		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String nodeName = child.getNodeName();
			if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE) {

				String data = ((CharacterData) child).getData();
				data = NodeletUtils.parsePropertyTokens(data, state.getGlobalProps());

				SoapText sqlText;

				if (postParseRequired) {
					sqlText = new SoapText();
					sqlText.setPostParseRequired(postParseRequired);
					sqlText.setText(data);
				} else {
					sqlText =
						PARAM_PARSER.parseInlineParameterMap(state.getConfig().getClient().getDelegate()
							.getTypeHandlerFactory(), data, null);
					sqlText.setPostParseRequired(postParseRequired);
				}

				dynamic.addChild(sqlText);

				sqlBuffer.append(data);
			} else if ("include".equals(nodeName)) {
				Properties attributes = NodeletUtils.parseAttributes(child, state.getGlobalProps());
				String refid = (String) attributes.get("refid");
				Node includeNode = (Node) state.getSqlIncludes().get(refid);
				if (includeNode == null) {
					String nsrefid = state.applyNamespace(refid);
					includeNode = (Node) state.getSqlIncludes().get(nsrefid);
					if (includeNode == null) {
						throw new RuntimeException("Could not find SQL statement to include with refid '" + refid + "'");
					}
				}
				isDynamic = parseDynamicTags(includeNode, dynamic, sqlBuffer, isDynamic, false);
			} else {
				state.getConfig().getErrorContext().setMoreInfo("Check the dynamic tags.");

				SoapTagHandler handler = SoapTagHandlerFactory.getSqlTagHandler(nodeName);
				if (handler != null) {
					isDynamic = true;

					SoapTag tag = new SoapTag();
					tag.setName(nodeName);
					tag.setHandler(handler);

					Properties attributes = NodeletUtils.parseAttributes(child, state.getGlobalProps());

					tag.setPrependAttr(attributes.getProperty("prepend"));
					tag.setPropertyAttr(attributes.getProperty("property"));
					tag.setRemoveFirstPrepend(attributes.getProperty("removeFirstPrepend"));

					tag.setOpenAttr(attributes.getProperty("open"));
					tag.setCloseAttr(attributes.getProperty("close"));

					tag.setComparePropertyAttr(attributes.getProperty("compareProperty"));
					tag.setCompareValueAttr(attributes.getProperty("compareValue"));
					tag.setConjunctionAttr(attributes.getProperty("conjunction"));

					// an iterate ancestor requires a post parse

					if (dynamic instanceof SoapTag) {
						SoapTag parentSqlTag = (SoapTag) dynamic;
						if (parentSqlTag.isPostParseRequired() || tag.getHandler() instanceof IterateTagHandler) {
							tag.setPostParseRequired(true);
						}
					} else if (dynamic instanceof DynamicSoap) {
						if (tag.getHandler() instanceof IterateTagHandler) {
							tag.setPostParseRequired(true);
						}
					}

					dynamic.addChild(tag);

					if (child.hasChildNodes()) {
						isDynamic = parseDynamicTags(child, tag, sqlBuffer, isDynamic, tag.isPostParseRequired());
					}
				}
			}
		}
		state.getConfig().getErrorContext().setMoreInfo(null);
		return isDynamic;
	}

}
