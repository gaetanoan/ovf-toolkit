package org.ow2.contrail.common.implementation.application_extended;



/***
 * This abstract class represent a generic transformation given an Input data and a Filter 
 * data (generic information on input that adds semantics to transformation)
 *  
 * @author Giacomo Maestrelli (giacomo.maestrelli@gmail.com) under supervision of Massimo Coppola
 *  
 * @param <Input> data 
 * @param <Output> result of the transformation 
 * @param <Filter> additional information
 */
public abstract class ExtensionDataGenerator<Input,Output,Filter> {
	
	protected Input data;
	
	
	public ExtensionDataGenerator(Input data){
		super();
		this.data=data;
	}
	
	/**
	 * transforms Input in a Output with Filter informations 
	 * <br>
	 * @param typeOfSearch 
	 * @return  Input transformed
	 */
	public Output getData(Filter typeOfSearch){
		return generateData(typeOfSearch);
		
	}

	/**
	 * realizes transformation on input data
	 * <br><br>
	 * <em>subclasses</em> of this class should implement this method 
	 * to gain transformation
	 * 
	 * @param typeOfSearch
	 * @return result of transformation
	 */
	protected abstract Output generateData(Filter typeOfSearch);
	
}
