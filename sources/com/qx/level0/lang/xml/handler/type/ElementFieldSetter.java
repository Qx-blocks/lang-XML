package com.qx.level0.lang.xml.handler.type;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;

import com.qx.level0.lang.xml.XML_ContextBuilder;
import com.qx.level0.lang.xml.annotation.XML_SetElement;
import com.qx.level0.lang.xml.parser.Parsed;
import com.qx.level0.lang.xml.parser.ParsedObjectElement;
import com.qx.level0.lang.xml.parser.XML_ParsingException;
import com.qx.level0.lang.xml.parser.XML_StreamReader;


public abstract class ElementFieldSetter {


	public static abstract class Generator {

		private boolean areContextualTagsEnabled = false;

		protected String tag;

		/**
		 * 
		 * @param method
		 * @param tag
		 */
		public Generator(String tag) {
			super();
			this.tag = tag;
			areContextualTagsEnabled = true;
		}

		public String getFieldTag() {
			return tag;
		}


		public abstract boolean hasContextualTags();

		/**
		 * 
		 * @return the set of all contextual tags <b>EXCLUDING</b> the standard tag.
		 */
		public abstract Set<String> getContextualTags();

		public void disableContextualTags() {
			areContextualTagsEnabled = false;
		}

		public boolean areContextualTagsEnabled() {
			return areContextualTagsEnabled;
		}


		/**
		 * 
		 * @param tags0
		 * @param tags1
		 * @return
		 */
		public boolean isContextuallyConflictingWith(ElementFieldSetter.Generator right) {
			Set<String> leftSet = getContextualTags();
			Set<String> rightSet = right.getContextualTags();
			if(leftSet!=null && rightSet!=null) {
				for(String tag : rightSet) {
					if(leftSet.contains(tag)) {
						return true;
					}
				}	
			}
			return false;
		}



		public abstract void getStandardSetters(TypeHandler.Putter putter) throws XML_TypeCompilationException;


		/**
		 * 
		 * @param setters
		 * @throws XML_TypeCompilationException 
		 */
		public abstract void getContextualSetters(TypeHandler.Putter putter) throws XML_TypeCompilationException;
	}




	/**
	 *  the XML tag for mapping purposes
	 */
	protected String tag;


	public ElementFieldSetter(String tag) {
		super();
		this.tag = tag;
	}


	public abstract Parsed getParsedElement(ParsedObjectElement parent, XML_StreamReader.Point point) 
			throws XML_ParsingException;


	public String getTag() {
		return tag;
	}



	public static void listDependencies(XML_ContextBuilder contextBuilder, Method method, 
			List<TypeBuilder> dependencies) throws XML_TypeCompilationException {

		Class<?>[] parameters = method.getParameterTypes();
		if(parameters.length!=1){
			throw new XML_TypeCompilationException("Illegal number of parameters for a setter");
		}

		Class<?> fieldType = parameters[0];

		if(PrimitiveElementFieldSetter.isPrimitive(fieldType)){
			// no dependencies
		}
		else if(fieldType.isArray()){
			Class<?> componentType = fieldType.getComponentType();
			contextBuilder.register(componentType);
			TypeBuilder subTypeHandler = contextBuilder.getTypeBuilder(componentType);
			if(subTypeHandler==null) {
				throw new XML_TypeCompilationException("Cannot find component type: "+fieldType);
			}
			dependencies.add(subTypeHandler);
		}
		else if(List.class.isAssignableFrom(fieldType)){
			Class<?> componentType =
					(Class<?>) ((ParameterizedType) fieldType.getGenericSuperclass()).getActualTypeArguments()[0];
			contextBuilder.register(componentType);
			TypeBuilder subTypeHandler = contextBuilder.getTypeBuilder(componentType);
			if(subTypeHandler==null) {
				throw new XML_TypeCompilationException("Cannot find component type: "+fieldType);
			}
			dependencies.add(subTypeHandler);
		}
		else{
			contextBuilder.register(fieldType);
			TypeBuilder subTypeHandler = contextBuilder.getTypeBuilder(fieldType);
			if(subTypeHandler==null) {
				throw new XML_TypeCompilationException("Cannot find type: "+fieldType);
			}
			dependencies.add(subTypeHandler);
		}
	}

	
	/**
	 * <p>We assume that all dependencies has been resolved at this point</p>
	 * 
	 * @param context
	 * @param method
	 * @param factory
	 * @return
	 * @throws XML_TypeCompilationException
	 */
	public static ElementFieldSetter.Generator create(
			XML_ContextBuilder context, 
			Method method, 
			CollectionElementFieldSetter.Factory factory) throws XML_TypeCompilationException {

		XML_SetElement setElementAnnotation = method.getAnnotation(XML_SetElement.class);
		String tag = setElementAnnotation.tag();

		Class<?>[] parameters = method.getParameterTypes();
		if(parameters.length!=1){
			throw new XML_TypeCompilationException("Illegal number of parameters for a setter");
		}

		Class<?> fieldType = parameters[0];

		if(PrimitiveElementFieldSetter.isPrimitive(fieldType)){
			return PrimitiveElementFieldSetter.create(context, method, fieldType, tag);
		}
		else if(fieldType.isArray()){
			Class<?> componentType = fieldType.getComponentType();
			TypeBuilder subTypeHandler = context.getTypeBuilder(componentType);
			return new ArrayElementFieldSetter.Generator(tag, method, factory.createEntry(subTypeHandler.getHandler()));
		}
		else if(List.class.isAssignableFrom(fieldType)){
			Class<?> componentType = 
					(Class<?>) ((ParameterizedType) fieldType.getGenericSuperclass()).getActualTypeArguments()[0];
			TypeBuilder subTypeHandler = context.getTypeBuilder(componentType);
			return new ListElementFieldSetter.Generator(tag, method, factory.createEntry(subTypeHandler.getHandler()));
		}
		else{
			TypeBuilder subTypeHandler = context.getTypeBuilder(fieldType);
			return new ObjectElementFieldSetter.Generator(tag, method, subTypeHandler.getHandler());
		}
	}
}
