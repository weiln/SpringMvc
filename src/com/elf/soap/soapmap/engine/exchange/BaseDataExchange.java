package com.elf.soap.soapmap.engine.exchange;

/**
 * Base implementation for the DataExchange interface.
 */
public abstract class BaseDataExchange implements DataExchange {

	private DataExchangeFactory dataExchangeFactory;

	protected BaseDataExchange(DataExchangeFactory dataExchangeFactory) {
		this.dataExchangeFactory = dataExchangeFactory;
	}

	/**
	 * Getter for the factory that created this object
	 * 
	 * @return - the factory
	 */
	public DataExchangeFactory getDataExchangeFactory() {
		return dataExchangeFactory;
	}

}
