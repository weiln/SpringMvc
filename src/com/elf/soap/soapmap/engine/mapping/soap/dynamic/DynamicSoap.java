package com.elf.soap.soapmap.engine.mapping.soap.dynamic;

import com.elf.soap.soapmap.engine.impl.SoapMapExecutorDelegate;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMap;
import com.elf.soap.soapmap.engine.mapping.parameter.InlineParameterMapParser;
import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMapping;
import com.elf.soap.soapmap.engine.mapping.result.ResultMap;
import com.elf.soap.soapmap.engine.mapping.soap.Soap;
import com.elf.soap.soapmap.engine.mapping.soap.SoapChild;
import com.elf.soap.soapmap.engine.mapping.soap.SoapText;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.DynamicParent;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.IterateContext;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.SoapTag;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.SoapTagContext;
import com.elf.soap.soapmap.engine.mapping.soap.dynamic.elements.SoapTagHandler;
import com.elf.soap.soapmap.engine.mapping.soap.simple.SimpleDynamicSoap;
import com.elf.soap.soapmap.engine.mapping.statement.MappedStatement;
import com.elf.soap.soapmap.engine.scope.StatementScope;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author
 * 
 */
public class DynamicSoap implements Soap, DynamicParent {

	private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();

	private List children = new ArrayList();
	private SoapMapExecutorDelegate delegate;

	public DynamicSoap(SoapMapExecutorDelegate delegate) {
		this.delegate = delegate;
	}

	public String getSoap(StatementScope statementScope, Object parameterObject) {
		String sql = statementScope.getDynamicSql();
		if (sql == null) {
			process(statementScope, parameterObject);
			sql = statementScope.getDynamicSql();
		}
		return sql;
	}

	public ParameterMap getParameterMap(StatementScope statementScope, Object parameterObject) {
		ParameterMap map = statementScope.getDynamicParameterMap();
		if (map == null) {
			process(statementScope, parameterObject);
			map = statementScope.getDynamicParameterMap();
		}
		return map;
	}

	public ResultMap getResultMap(StatementScope statementScope, Object parameterObject) {
		return statementScope.getResultMap();
	}

	public void cleanup(StatementScope statementScope) {
		statementScope.setDynamicSql(null);
		statementScope.setDynamicParameterMap(null);
	}

	private void process(StatementScope statementScope, Object parameterObject) {
		SoapTagContext ctx = new SoapTagContext();
		List localChildren = children;
		processBodyChildren(statementScope, ctx, parameterObject, localChildren.iterator());

		ParameterMap map = new ParameterMap(delegate);
		map.setId(statementScope.getStatement().getId() + "-InlineParameterMap");
		map.setParameterClass(((MappedStatement) statementScope.getStatement()).getParameterClass());
		map.setParameterMappingList(ctx.getParameterMappings());

		String dynSql = ctx.getBodyText();

		// Processes $substitutions$ after DynamicSql
		if (SimpleDynamicSoap.isSimpleDynamicSql(dynSql)) {
			dynSql = new SimpleDynamicSoap(delegate, dynSql).getSoap(statementScope, parameterObject);
		}

		statementScope.setDynamicSql(dynSql);
		statementScope.setDynamicParameterMap(map);
	}

	private void processBodyChildren(StatementScope statementScope, SoapTagContext ctx, Object parameterObject,
		Iterator localChildren) {
		PrintWriter out = ctx.getWriter();
		processBodyChildren(statementScope, ctx, parameterObject, localChildren, out);
	}

	private void processBodyChildren(StatementScope statementScope, SoapTagContext ctx, Object parameterObject,
		Iterator localChildren, PrintWriter out) {
		while (localChildren.hasNext()) {
			SoapChild child = (SoapChild) localChildren.next();
			if (child instanceof SoapText) {
				SoapText sqlText = (SoapText) child;
				String sqlStatement = sqlText.getText();
				if (sqlText.isWhiteSpace()) {
					out.print(sqlStatement);
				} else if (!sqlText.isPostParseRequired()) {

					// BODY OUT
					out.print(sqlStatement);

					ParameterMapping[] mappings = sqlText.getParameterMappings();
					if (mappings != null) {
						for (int i = 0, n = mappings.length; i < n; i++) {
							ctx.addParameterMapping(mappings[i]);
						}
					}
				} else {

					IterateContext itCtx = ctx.peekIterateContext();

					if (null != itCtx && itCtx.isAllowNext()) {
						itCtx.next();
						itCtx.setAllowNext(false);
						if (!itCtx.hasNext()) {
							itCtx.setFinal(true);
						}
					}

					if (itCtx != null) {
						StringBuffer sqlStatementBuffer = new StringBuffer(sqlStatement);
						iteratePropertyReplace(sqlStatementBuffer, itCtx);
						sqlStatement = sqlStatementBuffer.toString();
					}

					sqlText = PARAM_PARSER.parseInlineParameterMap(delegate.getTypeHandlerFactory(), sqlStatement);

					ParameterMapping[] mappings = sqlText.getParameterMappings();
					out.print(sqlText.getText());
					if (mappings != null) {
						for (int i = 0, n = mappings.length; i < n; i++) {
							ctx.addParameterMapping(mappings[i]);
						}
					}
				}
			} else if (child instanceof SoapTag) {
				SoapTag tag = (SoapTag) child;
				SoapTagHandler handler = tag.getHandler();
				int response = SoapTagHandler.INCLUDE_BODY;
				do {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);

					response = handler.doStartFragment(ctx, tag, parameterObject);
					if (response != SoapTagHandler.SKIP_BODY) {

						processBodyChildren(statementScope, ctx, parameterObject, tag.getChildren(), pw);
						pw.flush();
						pw.close();
						StringBuffer body = sw.getBuffer();
						response = handler.doEndFragment(ctx, tag, parameterObject, body);
						handler.doPrepend(ctx, tag, parameterObject, body);

						if (response != SoapTagHandler.SKIP_BODY) {
							if (body.length() > 0) {
								out.print(body.toString());
							}
						}

					}
				} while (response == SoapTagHandler.REPEAT_BODY);

				ctx.popRemoveFirstPrependMarker(tag);

				if (ctx.peekIterateContext() != null && ctx.peekIterateContext().getTag() == tag) {
					ctx.setAttribute(ctx.peekIterateContext().getTag(), null);
					ctx.popIterateContext();
				}

			}
		}
	}

	/**
	 * 
	 * @param bodyContent
	 * @param iterate
	 */
	protected void iteratePropertyReplace(StringBuffer bodyContent, IterateContext iterate) {
		if (iterate != null) {
			String[] mappings = new String[] { "#", "$" };
			for (int i = 0; i < mappings.length; i++) {
				int startIndex = 0;
				int endIndex = -1;
				while (startIndex > -1 && startIndex < bodyContent.length()) {
					startIndex = bodyContent.indexOf(mappings[i], endIndex + 1);
					endIndex = bodyContent.indexOf(mappings[i], startIndex + 1);
					if (startIndex > -1 && endIndex > -1) {
						bodyContent.replace(startIndex + 1, endIndex,
							iterate.addIndexToTagProperty(bodyContent.substring(startIndex + 1, endIndex)));
					}
				}
			}
		}
	}

	protected static void replace(StringBuffer buffer, String find, String replace) {
		int pos = buffer.toString().indexOf(find);
		int len = find.length();
		while (pos > -1) {
			buffer.replace(pos, pos + len, replace);
			pos = buffer.toString().indexOf(find);
		}
	}

	public void addChild(SoapChild child) {
		children.add(child);
	}

}
